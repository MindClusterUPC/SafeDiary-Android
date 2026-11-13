package com.mindcluster.safediary.scheduling.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindcluster.safediary.scheduling.domain.model.*
import com.mindcluster.safediary.scheduling.infrastructure.remote.RetrofitCareSchedulingGateway
import com.mindcluster.safediary.scheduling.infrastructure.remote.SchedulingResult
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AgendaMode { PATIENT, PSYCHOLOGIST, PSYCHOLOGIST_WITHOUT_PROFILE, UNSUPPORTED }

data class SchedulingUiState(
    val appointments: List<Appointment> = emptyList(),
    val contactRequests: List<ContactRequest> = emptyList(),
    val clinicians: Map<Long, ClinicianSummary> = emptyMap(),
    val messages: Map<Long, List<CoordinationMessage>> = emptyMap(),
    val now: Long = System.currentTimeMillis(),
    val activeConversationId: Long? = null,
    val isLoading: Boolean = false,
    val isWorking: Boolean = false,
    /** Which agenda the signed-in account can use, deduced from what the backend allows. */
    val mode: AgendaMode = AgendaMode.PATIENT,
    /** Clinician Directory profile id of a signed-in psychologist. */
    val ownClinicianId: Long? = null,
    val loadError: String? = null,
    val actionError: String? = null,
    /** Video call URL waiting to be opened by the screen. */
    val joinUrl: String? = null,
    /** Stripe Checkout opened for [checkoutAppointmentId]. */
    val checkoutUrl: String? = null,
    val checkoutAppointmentId: Long? = null,
    /** Local consent preference: nothing is shared with the clinician from this screen yet. */
    val sharedContext: Set<Long> = emptySet()
) {
    fun clinician(id: Long) = clinicians[id] ?: ClinicianSummary(id, "Psicólogo", "PS")
    fun contactRequest(id: Long) = contactRequests.firstOrNull { it.id == id }
    /** Latest proposal of a chat that the patient can still accept. */
    fun proposalFor(contactRequestId: Long) = appointments
        .filter { it.contactRequestId == contactRequestId && it.status == AppointmentStatus.REQUESTED }
        .maxByOrNull { it.id }
    fun heldFor(contactRequestId: Long) = appointments
        .firstOrNull { it.contactRequestId == contactRequestId && it.status == AppointmentStatus.HELD }
}

/** Patient side of Care Scheduling: contact, coordination chat, proposal acceptance, hold, cancellation and video call. */
class SchedulingViewModel(
    private val gateway: RetrofitCareSchedulingGateway = RetrofitCareSchedulingGateway()
) : ViewModel() {
    private val _uiState = MutableStateFlow(SchedulingUiState())
    val uiState = _uiState.asStateFlow()
    val canSimulatePayment get() = gateway.canSimulatePayment

    /** A hold past its expiry is shown as expired right away; the backend releases the slot on its own. */
    fun tick(now: Long = System.currentTimeMillis()) {
        _uiState.update { state -> state.copy(now = now, appointments = state.appointments.map { it.expiredLocally(now) }) }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null) }
            val appointmentsCall = async { gateway.getAppointments() }
            val requestsCall = async { gateway.getContactRequests() }
            val appointments = appointmentsCall.await()
            val requests = requestsCall.await()
            if ((appointments as? SchedulingResult.Failure)?.status == 403) {
                // Only patients list appointments; a psychologist manages the agenda of their directory profile.
                val own = gateway.getOwnClinicianId()
                val mode = when {
                    own is SchedulingResult.Success -> AgendaMode.PSYCHOLOGIST
                    (own as? SchedulingResult.Failure)?.status == 404 -> AgendaMode.PSYCHOLOGIST_WITHOUT_PROFILE
                    else -> AgendaMode.UNSUPPORTED
                }
                _uiState.update {
                    it.copy(isLoading = false, mode = mode, ownClinicianId = (own as? SchedulingResult.Success)?.value,
                        loadError = null, appointments = emptyList(), contactRequests = emptyList())
                }
                return@launch
            }
            _uiState.update { state ->
                state.copy(
                    appointments = (appointments as? SchedulingResult.Success)?.value?.map { it.expiredLocally(state.now) } ?: state.appointments,
                    contactRequests = (requests as? SchedulingResult.Success)?.value?.sortedByDescending { it.id } ?: state.contactRequests,
                    isLoading = false,
                    mode = AgendaMode.PATIENT,
                    ownClinicianId = null,
                    loadError = (appointments as? SchedulingResult.Failure)?.message ?: (requests as? SchedulingResult.Failure)?.message
                )
            }
            loadMissingClinicians()
        }
    }

    /** US-048: opens the private chat with a verified clinician, or reuses the open one. */
    fun startConversation(professionalId: String, message: String) {
        val clinicianId = professionalId.toLongOrNull() ?: return
        working {
            when (val result = gateway.sendContactRequest(clinicianId, message)) {
                is SchedulingResult.Success -> {
                    val request = result.value
                    _uiState.update { state ->
                        state.copy(activeConversationId = request.id,
                            contactRequests = listOf(request) + state.contactRequests.filterNot { it.id == request.id })
                    }
                    refresh()
                    loadMessages(request.id)
                }
                is SchedulingResult.Failure -> fail(
                    when (result.status) {
                        422 -> "Este psicólogo ya no está disponible para nuevos contactos."
                        403 -> "Solo las cuentas de paciente pueden solicitar contacto con un especialista."
                        else -> result.message
                    }
                )
            }
        }
    }

    fun openConversation(id: Long) {
        _uiState.update { it.copy(activeConversationId = id) }
        loadMessages(id)
    }

    fun closeConversation() { _uiState.update { it.copy(activeConversationId = null) } }

    /** Reloads the chat and the proposals the clinician may have sent meanwhile. */
    fun refreshConversation(id: Long, silent: Boolean = false) {
        loadMessages(id, silent)
        refresh()
    }

    fun sendMessage(id: Long, text: String) {
        if (text.isBlank()) return
        working {
            when (val result = gateway.sendMessage(id, text.trim())) {
                is SchedulingResult.Success -> loadMessages(id)
                is SchedulingResult.Failure -> fail(result.message)
            }
        }
    }

    /** US-049 / US-014: accepting the proposal holds the slot for one hour; the appointment is not confirmed yet. */
    fun accept(appointmentId: Long, onHeld: (Long) -> Unit) {
        working {
            when (val result = gateway.acceptProposal(appointmentId)) {
                is SchedulingResult.Success -> {
                    replace(result.value)
                    onHeld(result.value.id)
                }
                is SchedulingResult.Failure -> {
                    fail(
                        if (result.status == 409 || result.status == 422)
                            "Este horario ya no está disponible o la propuesta cambió. Puedes seguir coordinando por el chat."
                        else result.message
                    )
                    refresh()
                }
            }
        }
    }

    fun simulatePayment(id: Long) {
        val appointment = _uiState.value.appointments.firstOrNull { it.id == id } ?: return
        working {
            when (val result = gateway.simulatePayment(appointment)) {
                is SchedulingResult.Success -> replace(result.value)
                is SchedulingResult.Failure -> {
                    fail(result.message)
                    refresh()
                }
            }
        }
    }

    /** US-037: opens Stripe Checkout for the held appointment; the amount is decided by the backend. */
    fun pay(id: Long) {
        working {
            when (val result = gateway.startCheckout(id)) {
                is SchedulingResult.Success -> _uiState.update { it.copy(checkoutUrl = result.value, checkoutAppointmentId = id) }
                is SchedulingResult.Failure -> {
                    fail(
                        if (result.status == 422) "La reserva ya no está activa o el especialista aún no puede recibir pagos. Actualiza tus citas o coordina por el chat."
                        else result.message
                    )
                    refresh()
                }
            }
        }
    }

    /**
     * Closes Stripe Checkout. After a completed payment the appointment is confirmed by the Stripe webhook, so the
     * list is reloaded a few times until the confirmation arrives.
     */
    fun closeCheckout(completed: Boolean) {
        val id = _uiState.value.checkoutAppointmentId
        _uiState.update { it.copy(checkoutUrl = null, checkoutAppointmentId = null) }
        if (!completed || id == null) return
        working {
            repeat(PAYMENT_CONFIRMATION_ATTEMPTS) { attempt ->
                if (attempt > 0) delay(PAYMENT_CONFIRMATION_INTERVAL_MS)
                val result = gateway.getAppointments()
                if (result is SchedulingResult.Success) {
                    result.value.firstOrNull { it.id == id }?.let { replace(it) }
                    if (result.value.any { it.id == id && it.status == AppointmentStatus.CONFIRMED }) return@working
                }
            }
            fail("Tu pago se está procesando. La cita aparecerá confirmada en cuanto se apruebe; actualiza en unos minutos.")
        }
    }

    /** US-050: cancels a proposal, a hold or a confirmed appointment and releases the slot. */
    fun cancel(id: Long) {
        working {
            when (val result = gateway.cancelAppointment(id)) {
                is SchedulingResult.Success -> replace(result.value)
                is SchedulingResult.Failure -> {
                    fail(result.message)
                    refresh()
                }
            }
        }
    }

    /** US-038: the backend validates the participant and the access window before returning the call URL. */
    fun join(id: Long) {
        working {
            when (val result = gateway.joinSession(id)) {
                is SchedulingResult.Success -> _uiState.update { it.copy(joinUrl = result.value) }
                is SchedulingResult.Failure -> fail(
                    if (result.status == 409 || result.status == 422)
                        "La videollamada solo está disponible para una cita confirmada y dentro de su horario de acceso."
                    else result.message
                )
            }
        }
    }

    fun consumeJoinUrl() { _uiState.update { it.copy(joinUrl = null) } }
    fun showError(message: String) = fail(message)
    fun dismissError() { _uiState.update { it.copy(actionError = null) } }
    fun shareContext(id: Long, enabled: Boolean) {
        _uiState.update { it.copy(sharedContext = if (enabled) it.sharedContext + id else it.sharedContext - id) }
    }

    private fun loadMessages(id: Long, silent: Boolean = false) {
        viewModelScope.launch {
            val patientAccountId = _uiState.value.contactRequest(id)?.patientAccountId
            when (val result = gateway.getMessages(id, patientAccountId)) {
                is SchedulingResult.Success -> _uiState.update { it.copy(messages = it.messages + (id to result.value)) }
                is SchedulingResult.Failure -> if (!silent) fail(result.message)
            }
        }
    }

    private suspend fun loadMissingClinicians() {
        val state = _uiState.value
        val missing = (state.appointments.map { it.clinicianId } + state.contactRequests.map { it.clinicianId })
            .distinct().filterNot { it in state.clinicians }
        missing.forEach { id ->
            gateway.getClinician(id)?.let { clinician -> _uiState.update { it.copy(clinicians = it.clinicians + (id to clinician)) } }
        }
    }

    private fun replace(appointment: Appointment) {
        _uiState.update { state ->
            val others = state.appointments.filterNot { it.id == appointment.id }
            state.copy(appointments = listOf(appointment.expiredLocally(state.now)) + others)
        }
    }

    private fun working(block: suspend () -> Unit) {
        if (_uiState.value.isWorking) return
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true) }
            try { block() } finally { _uiState.update { it.copy(isWorking = false) } }
        }
    }

    private fun fail(message: String) { _uiState.update { it.copy(actionError = message) } }
}

private const val PAYMENT_CONFIRMATION_ATTEMPTS = 6
private const val PAYMENT_CONFIRMATION_INTERVAL_MS = 2_000L

internal fun Appointment.expiredLocally(now: Long) =
    if (status == AppointmentStatus.HELD && holdExpiresAt != null && holdExpiresAt <= now) copy(status = AppointmentStatus.EXPIRED) else this
