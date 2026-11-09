package com.mindcluster.safediary.scheduling.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindcluster.safediary.scheduling.domain.model.*
import com.mindcluster.safediary.scheduling.infrastructure.remote.RetrofitCareSchedulingGateway
import com.mindcluster.safediary.scheduling.infrastructure.remote.SchedulingResult
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** The agenda works in the clinic timezone, the same one the backend uses for availability. */
internal val AGENDA_ZONE: TimeZone = TimeZone.getTimeZone("America/Lima")

internal fun startOfDay(millis: Long): Long = Calendar.getInstance(AGENDA_ZONE).apply {
    timeInMillis = millis
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

internal fun addDays(day: Long, days: Int): Long =
    Calendar.getInstance(AGENDA_ZONE).apply { timeInMillis = day; add(Calendar.DAY_OF_YEAR, days) }.timeInMillis

/** Monday to Sunday of the week that contains [day]. */
internal fun weekDays(day: Long): List<Long> {
    val calendar = Calendar.getInstance(AGENDA_ZONE).apply { timeInMillis = startOfDay(day) }
    val sinceMonday = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val monday = addDays(calendar.timeInMillis, -sinceMonday)
    return (0..6).map { addDays(monday, it) }
}

internal fun agendaDate(day: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = AGENDA_ZONE }.format(Date(day))

data class ClinicianAgendaUiState(
    val clinicianId: Long? = null,
    val requests: List<ContactRequest> = emptyList(),
    val agenda: List<Appointment> = emptyList(),
    val messages: Map<Long, List<CoordinationMessage>> = emptyMap(),
    val selectedRequestId: Long? = null,
    val selectedDay: Long = startOfDay(System.currentTimeMillis()),
    val slots: List<BookableSlot> = emptyList(),
    val slotsLoading: Boolean = false,
    val slotsError: String? = null,
    val selectedSlot: BookableSlot? = null,
    val sessions: Map<Long, ClinicalSession> = emptyMap(),
    val now: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val isWorking: Boolean = false,
    val loadError: String? = null,
    val actionError: String? = null,
    val notice: String? = null,
    val joinUrl: String? = null
) {
    val openRequests: List<ContactRequest> get() = requests.filter { it.isOpen }
    val selectedRequest: ContactRequest? get() = openRequests.firstOrNull { it.id == selectedRequestId }
    val freeSlots: Int get() = slots.count { it.availability == SlotAvailability.FREE }
    fun request(id: Long) = requests.firstOrNull { it.id == id }
    fun appointment(id: Long) = agenda.firstOrNull { it.id == id }
    /** Latest proposal of a chat that is still waiting for the patient. */
    fun proposalFor(contactRequestId: Long) = agenda
        .filter { it.contactRequestId == contactRequestId && it.status == AppointmentStatus.REQUESTED }
        .maxByOrNull { it.id }
}

/** Psychologist side of Care Scheduling: requests, chat, proposals from the agenda (US-043) and the attention (US-044). */
class ClinicianAgendaViewModel(
    private val gateway: RetrofitCareSchedulingGateway = RetrofitCareSchedulingGateway()
) : ViewModel() {
    private val _uiState = MutableStateFlow(ClinicianAgendaUiState())
    val uiState = _uiState.asStateFlow()

    fun start(clinicianId: Long) {
        _uiState.update { it.copy(clinicianId = clinicianId) }
        refresh()
        loadSlots()
    }

    fun tick(now: Long = System.currentTimeMillis()) { _uiState.update { it.copy(now = now) } }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null) }
            val requestsCall = async { gateway.getContactRequests() }
            val agendaCall = async { gateway.getClinicianAgenda() }
            val requests = requestsCall.await()
            val agenda = agendaCall.await()
            _uiState.update { state ->
                val newRequests = (requests as? SchedulingResult.Success)?.value?.sortedByDescending { it.id } ?: state.requests
                val stillSelected = newRequests.any { it.id == state.selectedRequestId && it.isOpen }
                state.copy(
                    requests = newRequests,
                    agenda = (agenda as? SchedulingResult.Success)?.value ?: state.agenda,
                    selectedRequestId = if (stillSelected) state.selectedRequestId else newRequests.firstOrNull { it.isOpen }?.id,
                    isLoading = false,
                    loadError = (requests as? SchedulingResult.Failure)?.message ?: (agenda as? SchedulingResult.Failure)?.message
                )
            }
        }
    }

    fun selectDay(day: Long) {
        _uiState.update { it.copy(selectedDay = startOfDay(day), selectedSlot = null) }
        loadSlots()
    }

    fun shiftWeek(weeks: Int) = selectDay(addDays(_uiState.value.selectedDay, 7 * weeks))

    fun selectRequest(id: Long) { _uiState.update { it.copy(selectedRequestId = id) } }

    fun selectSlot(slot: BookableSlot) {
        if (slot.availability != SlotAvailability.FREE) return
        _uiState.update { it.copy(selectedSlot = if (it.selectedSlot == slot) null else slot) }
    }

    /** US-043 scenario 1: registers the proposal; no reservation or charge is created until the patient accepts. */
    fun propose() {
        val state = _uiState.value
        val request = state.selectedRequest ?: return
        val slot = state.selectedSlot ?: return
        working {
            when (val result = gateway.proposeSchedule(request.id, slot.startsAtIso)) {
                is SchedulingResult.Success -> {
                    _uiState.update {
                        it.copy(selectedSlot = null, notice = "Propuesta enviada a ${patientLabel(request.patientAccountId)}. " +
                            "Aún no hay reserva ni cobro: el paciente debe aceptarla.")
                    }
                    refresh()
                    loadSlots()
                }
                is SchedulingResult.Failure -> {
                    fail(when (result.status) {
                        409 -> "Ese horario ya no está libre o hay una reserva de este chat esperando pago. Elige otro horario."
                        422 -> "No se puede proponer este horario: debe estar dentro de tu disponibilidad y tu ficha debe seguir publicada."
                        else -> result.message
                    })
                    loadSlots()
                }
            }
        }
    }

    /** US-043 scenario 2: closes the request and informs the patient without any charge. */
    fun reject(id: Long) {
        working {
            when (val result = gateway.respondContactRequest(id, accept = false)) {
                is SchedulingResult.Success -> {
                    _uiState.update { it.copy(notice = "Solicitud rechazada. El paciente verá que no puedes atenderla; no se generó ningún cobro.") }
                    refresh()
                }
                is SchedulingResult.Failure -> fail(
                    if (result.status == 409) "Solo se pueden rechazar solicitudes que aún no se han respondido." else result.message
                )
            }
        }
    }

    fun loadMessages(id: Long, silent: Boolean = false) {
        viewModelScope.launch {
            val patientAccountId = _uiState.value.request(id)?.patientAccountId
            when (val result = gateway.getMessages(id, patientAccountId)) {
                is SchedulingResult.Success -> _uiState.update { it.copy(messages = it.messages + (id to result.value)) }
                is SchedulingResult.Failure -> if (!silent) fail(result.message)
            }
        }
    }

    /** Replying opens the chat: the backend marks the request as accepted. */
    fun sendMessage(id: Long, text: String) {
        if (text.isBlank()) return
        working {
            when (val result = gateway.sendMessage(id, text.trim())) {
                is SchedulingResult.Success -> { loadMessages(id); refresh() }
                is SchedulingResult.Failure -> fail(result.message)
            }
        }
    }

    fun loadSession(appointmentId: Long) {
        viewModelScope.launch {
            (gateway.getSession(appointmentId) as? SchedulingResult.Success)?.value?.let { session ->
                _uiState.update { it.copy(sessions = it.sessions + (appointmentId to session)) }
            }
        }
    }

    /** US-044: the backend validates the participant and the access window before returning the call URL. */
    fun join(appointmentId: Long) {
        working {
            when (val result = gateway.joinSession(appointmentId)) {
                is SchedulingResult.Success -> {
                    _uiState.update { it.copy(joinUrl = result.value) }
                    loadSession(appointmentId)
                }
                is SchedulingResult.Failure -> fail(
                    if (result.status == 409 || result.status == 422)
                        "La videollamada solo está disponible para una cita confirmada y dentro de su horario de acceso."
                    else result.message
                )
            }
        }
    }

    /** US-044: records the operational outcome; COMPLETED enables the review, the others apply the refund policy. */
    fun close(appointmentId: Long, outcome: SessionOutcome, onClosed: () -> Unit) {
        working {
            when (val result = gateway.closeSession(appointmentId, outcome)) {
                is SchedulingResult.Success -> {
                    _uiState.update {
                        it.copy(sessions = it.sessions + (appointmentId to result.value), notice = when (outcome) {
                            SessionOutcome.COMPLETED -> "Atención completada. El paciente ya puede reseñar la cita."
                            SessionOutcome.NO_SHOW -> "Se registró la inasistencia. Pagos aplicará la política de devolución."
                            SessionOutcome.NOT_HELD -> "Se registró que la sesión no se realizó. Pagos aplicará la política de devolución."
                        })
                    }
                    refresh()
                    onClosed()
                }
                is SchedulingResult.Failure -> fail(
                    if (result.status == 409 || result.status == 422)
                        "Aún no se puede cerrar: la cita debe estar confirmada, su ventana de acceso abierta y, para completarla, la sesión debe haberse iniciado."
                    else result.message
                )
            }
        }
    }

    fun consumeJoinUrl() { _uiState.update { it.copy(joinUrl = null) } }
    fun showError(message: String) = fail(message)
    fun dismissError() { _uiState.update { it.copy(actionError = null) } }
    fun dismissNotice() { _uiState.update { it.copy(notice = null) } }

    private fun loadSlots() {
        val clinicianId = _uiState.value.clinicianId ?: return
        val day = _uiState.value.selectedDay
        viewModelScope.launch {
            _uiState.update { it.copy(slotsLoading = true, slotsError = null) }
            val result = gateway.getSlots(clinicianId, agendaDate(day))
            _uiState.update { state ->
                if (state.selectedDay != day) return@update state.copy(slotsLoading = false)
                when (result) {
                    is SchedulingResult.Success -> state.copy(slots = result.value, slotsLoading = false)
                    is SchedulingResult.Failure -> state.copy(
                        slots = emptyList(), slotsLoading = false,
                        slotsError = if (result.status == 404) "Tu ficha debe estar verificada y publicada en el directorio para mostrar horarios." else result.message
                    )
                }
            }
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
