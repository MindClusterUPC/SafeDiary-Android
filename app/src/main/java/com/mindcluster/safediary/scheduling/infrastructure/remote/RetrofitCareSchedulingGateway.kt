package com.mindcluster.safediary.scheduling.infrastructure.remote

import android.util.Log
import com.mindcluster.safediary.BuildConfig
import com.mindcluster.safediary.scheduling.domain.model.Appointment
import com.mindcluster.safediary.scheduling.domain.model.AppointmentStatus
import com.mindcluster.safediary.scheduling.domain.model.BookableSlot
import com.mindcluster.safediary.scheduling.domain.model.ClinicalSession
import com.mindcluster.safediary.scheduling.domain.model.ClinicianSummary
import com.mindcluster.safediary.scheduling.domain.model.ContactRequest
import com.mindcluster.safediary.scheduling.domain.model.ContactRequestStatus
import com.mindcluster.safediary.scheduling.domain.model.CoordinationMessage
import com.mindcluster.safediary.scheduling.domain.model.SessionOutcome
import com.mindcluster.safediary.scheduling.domain.model.SlotAvailability
import com.mindcluster.safediary.scheduling.infrastructure.remote.api.CareSchedulingApiService
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.AppointmentDto
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.ClinicalSessionDto
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.CloseSessionDto
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.ConsultationCheckoutRequestDto
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.ContactRequestDto
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.CoordinationMessageDto
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.ProposeScheduleDto
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.RespondContactRequestDto
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.SendContactRequestDto
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.SendCoordinationMessageDto
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.SimulatedAppointmentPaymentDto
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.parseInstantMillis
import com.mindcluster.safediary.shared.infrastructure.network.RetrofitClientProvider
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.net.URI

sealed interface SchedulingResult<out T> {
    data class Success<T>(val value: T) : SchedulingResult<T>
    /** [status] is the HTTP status, or null when the backend could not be reached. */
    data class Failure(val status: Int?, val message: String) : SchedulingResult<Nothing>
}

class RetrofitCareSchedulingGateway(
    private val apiService: CareSchedulingApiService = RetrofitClientProvider.createService()
) {
    private val tag = "CareSchedulingGateway"
    val canSimulatePayment = localPaymentSimulationAvailable(BuildConfig.DEBUG, BuildConfig.API_BASE_URL)

    suspend fun sendContactRequest(clinicianId: Long, message: String): SchedulingResult<ContactRequest> =
        call("sendContactRequest") { apiService.sendContactRequest(SendContactRequestDto(clinicianId, message)) }
            .map { it.toDomain() ?: throw IllegalStateException("Unknown contact request status") }

    suspend fun getContactRequests(): SchedulingResult<List<ContactRequest>> =
        call("getContactRequests") { apiService.getContactRequests() }.map { list -> list.mapNotNull { it.toDomain() } }

    /** [patientAccountId] tells which messages were written by the patient. */
    suspend fun getMessages(contactRequestId: Long, patientAccountId: Long?): SchedulingResult<List<CoordinationMessage>> =
        call("getMessages") { apiService.getMessages(contactRequestId) }
            .map { list -> list.map { it.toDomain(patientAccountId) }.sortedBy { it.sentAt } }

    suspend fun sendMessage(contactRequestId: Long, body: String): SchedulingResult<Unit> =
        call("sendMessage") { apiService.sendMessage(contactRequestId, SendCoordinationMessageDto(body)) }.map { }

    suspend fun getAppointments(): SchedulingResult<List<Appointment>> =
        call("getAppointments") { apiService.getAppointments() }.map { list -> list.mapNotNull { it.toDomain() } }

    suspend fun acceptProposal(appointmentId: Long): SchedulingResult<Appointment> =
        call("acceptProposal") { apiService.acceptProposal(appointmentId) }
            .map { it.toDomain() ?: throw IllegalStateException("Unknown appointment status") }

    /** Development-only payment; the server remains the source of truth for confirmation. */
    suspend fun simulatePayment(appointment: Appointment): SchedulingResult<Appointment> {
        if (!canSimulatePayment) return SchedulingResult.Failure(403, "El pago simulado solo está disponible en el entorno local de desarrollo.")
        if (!appointment.isHoldActive(System.currentTimeMillis()))
            return SchedulingResult.Failure(409, "La reserva ya no está activa. Actualiza tus citas.")
        val reference = appointment.paymentReference?.takeIf { it.isNotBlank() }
            ?: return SchedulingResult.Failure(422, "La reserva no tiene una referencia de pago. Actualiza tus citas.")
        val result = call("simulatePayment") {
            apiService.simulatePayment(appointment.id, SimulatedAppointmentPaymentDto(reference, true, appointment.amount, appointment.currency))
        }
        if (result is SchedulingResult.Failure) return result
        return when (val refreshed = getAppointments()) {
            is SchedulingResult.Failure -> refreshed
            is SchedulingResult.Success -> {
                val confirmed = refreshed.value.firstOrNull { it.id == appointment.id && it.status == AppointmentStatus.CONFIRMED }
                if (confirmed != null) SchedulingResult.Success(confirmed)
                else SchedulingResult.Failure(409, "La cita aún no figura confirmada. Actualiza para consultar el resultado del pago.")
            }
        }
    }

    /** US-037: Stripe Checkout URL for the held appointment. */
    suspend fun startCheckout(appointmentId: Long): SchedulingResult<String> =
        call("startCheckout") { apiService.startCheckout(ConsultationCheckoutRequestDto(appointmentId)) }
            .map { it.checkoutUrl?.takeIf { url -> url.isNotBlank() } ?: throw IllegalStateException("Missing checkout URL") }

    suspend fun cancelAppointment(appointmentId: Long): SchedulingResult<Appointment> =
        call("cancelAppointment") { apiService.cancelAppointment(appointmentId) }
            .map { it.toDomain() ?: throw IllegalStateException("Unknown appointment status") }

    /** Returns the video call URL issued by the backend for this participant. */
    suspend fun joinSession(appointmentId: Long): SchedulingResult<String> =
        call("joinSession") { apiService.joinSession(appointmentId) }
            .map { it.joinUrl?.takeIf { url -> url.isNotBlank() } ?: throw IllegalStateException("Missing join URL") }

    suspend fun getClinician(clinicianId: Long): ClinicianSummary? =
        (call("getClinician") { apiService.getClinician(clinicianId) } as? SchedulingResult.Success)?.value?.let { dto ->
            val name = dto.displayName?.trim().orEmpty().ifBlank { return@let null }
            ClinicianSummary(dto.id, name, initialsOf(name))
        }

    // Psychologist side (US-043 / US-044)

    /** Id of the signed-in psychologist's Clinician Directory profile; a 404 means there is no profile yet. */
    suspend fun getOwnClinicianId(): SchedulingResult<Long> =
        call("getOwnClinicianId") { apiService.getOwnClinicianProfile() }.map { it.id }

    suspend fun respondContactRequest(contactRequestId: Long, accept: Boolean): SchedulingResult<ContactRequest> =
        call("respondContactRequest") { apiService.respondContactRequest(contactRequestId, RespondContactRequestDto(accept)) }
            .map { it.toDomain() ?: throw IllegalStateException("Unknown contact request status") }

    /** [date] uses the agenda format yyyy-MM-dd. */
    suspend fun getSlots(clinicianId: Long, date: String): SchedulingResult<List<BookableSlot>> =
        call("getSlots") { apiService.getSlots(clinicianId, date) }.map { list ->
            list.mapNotNull { dto ->
                val iso = dto.startsAt ?: return@mapNotNull null
                val start = parseInstantMillis(iso) ?: return@mapNotNull null
                BookableSlot(start, iso, enumValueOrNull<SlotAvailability>(dto.availability) ?: SlotAvailability.UNAVAILABLE)
            }.sortedBy { it.startsAt }
        }

    suspend fun proposeSchedule(contactRequestId: Long, startsAtIso: String): SchedulingResult<Appointment> =
        call("proposeSchedule") { apiService.proposeSchedule(ProposeScheduleDto(contactRequestId, startsAtIso)) }
            .map { it.toDomain() ?: throw IllegalStateException("Unknown appointment status") }

    suspend fun getClinicianAgenda(): SchedulingResult<List<Appointment>> =
        call("getClinicianAgenda") { apiService.getClinicianAgenda() }.map { list -> list.mapNotNull { it.toDomain() } }

    suspend fun getSession(appointmentId: Long): SchedulingResult<ClinicalSession> =
        call("getSession") { apiService.getSession(appointmentId) }.map { it.toSession() }

    suspend fun closeSession(appointmentId: Long, outcome: SessionOutcome): SchedulingResult<ClinicalSession> =
        call("closeSession") { apiService.closeSession(appointmentId, CloseSessionDto(outcome.name)) }.map { it.toSession() }

    private suspend fun <T> call(name: String, block: suspend () -> Response<T>): SchedulingResult<T> = try {
        val response = block()
        val body = response.body()
        if (response.isSuccessful && body != null) {
            SchedulingResult.Success(body)
        } else {
            Log.w(tag, "$name error ${response.code()}: ${response.message()}")
            SchedulingResult.Failure(response.code(), messageFor(response.code()))
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(tag, "$name network error: ${e.message}")
        SchedulingResult.Failure(null, "No pudimos conectar con SafeDiary. Revisa tu conexión e inténtalo de nuevo.")
    }

    private fun <T, R> SchedulingResult<T>.map(transform: (T) -> R): SchedulingResult<R> = when (this) {
        is SchedulingResult.Success -> try {
            SchedulingResult.Success(transform(value))
        } catch (e: Exception) {
            Log.w(tag, "Unexpected response: ${e.message}")
            SchedulingResult.Failure(null, "Recibimos una respuesta inesperada. Inténtalo de nuevo.")
        }
        is SchedulingResult.Failure -> this
    }

    private fun messageFor(status: Int) = when (status) {
        401 -> "Tu sesión expiró. Vuelve a iniciar sesión."
        403 -> "No tienes permiso para realizar esta acción."
        404 -> "No encontramos esta información. Actualiza e inténtalo de nuevo."
        409 -> "Esta acción ya no es posible con el estado actual. Actualiza e inténtalo de nuevo."
        400, 422 -> "No se pudo completar la solicitud con los datos actuales."
        else -> "Ocurrió un problema en el servidor. Inténtalo más tarde."
    }
}

private fun ContactRequestDto.toDomain(): ContactRequest? {
    val parsedStatus = enumValueOrNull<ContactRequestStatus>(status) ?: return null
    return ContactRequest(id, clinicianId, parsedStatus, lastMessage?.toDomain(patientAccountId), patientAccountId)
}

private fun CoordinationMessageDto.toDomain(patientAccountId: Long?) = CoordinationMessage(
    id = id,
    text = body.orEmpty(),
    fromPatient = patientAccountId != null && senderAccountId == patientAccountId,
    sentAt = parseInstantMillis(sentAt) ?: 0L
)

private fun AppointmentDto.toDomain(): Appointment? {
    val parsedStatus = enumValueOrNull<AppointmentStatus>(status) ?: return null
    val start = parseInstantMillis(startsAt) ?: return null
    return Appointment(
        id = id,
        contactRequestId = contactRequestId,
        clinicianId = clinicianId,
        startsAt = start,
        endsAt = parseInstantMillis(endsAt) ?: start,
        amount = amount ?: 0.0,
        currency = currency ?: "PEN",
        status = parsedStatus,
        holdExpiresAt = parseInstantMillis(holdExpiresAt),
        accessOpensAt = parseInstantMillis(accessOpensAt),
        accessClosesAt = parseInstantMillis(accessClosesAt),
        patientAccountId = patientAccountId,
        paymentReference = paymentReference
    )
}

internal fun localPaymentSimulationAvailable(debugBuild: Boolean, baseUrl: String): Boolean =
    debugBuild && runCatching {
        val uri = URI(baseUrl)
        uri.scheme == "http" && uri.host?.lowercase() in setOf("localhost", "127.0.0.1", "10.0.2.2", "[::1]", "::1")
    }.getOrDefault(false)

private fun ClinicalSessionDto.toSession() = ClinicalSession(
    status = status.orEmpty(),
    startedAt = parseInstantMillis(startedAt),
    endedAt = parseInstantMillis(endedAt),
    attendedMinutes = attendedMinutes ?: 0
)

private inline fun <reified E : Enum<E>> enumValueOrNull(value: String?): E? =
    enumValues<E>().firstOrNull { it.name == value?.uppercase() }

private fun initialsOf(name: String): String =
    name.split(" ").filter { it.isNotBlank() && !it.endsWith(".") }
        .take(2).joinToString("") { it.first().uppercase() }
        .ifBlank { "PS" }
