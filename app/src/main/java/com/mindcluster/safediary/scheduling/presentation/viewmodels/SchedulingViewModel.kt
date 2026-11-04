package com.mindcluster.safediary.scheduling.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.mindcluster.safediary.scheduling.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class SchedulingUiState(
    val appointments: List<Appointment>,
    val conversations: List<CoordinationConversation>,
    val now: Long = System.currentTimeMillis(),
    val activeConversationId: String? = null
)

/** A local UI preview. Nothing here sends messages, takes payments or starts a call. */
class SchedulingViewModel : ViewModel() {
    private val initialNow = System.currentTimeMillis()
    private val _uiState = MutableStateFlow(SchedulingUiState(
        appointments = listOf(
            Appointment("reservation", "laura", initialNow + 86_400_000, AppointmentStatus.RESERVED, initialNow + 2_538_000, "laura"),
            Appointment("confirmed", "andres", initialNow + 5 * 60_000, AppointmentStatus.CONFIRMED),
            Appointment("completed", "sarah", initialNow - 7 * 86_400_000, AppointmentStatus.COMPLETED)
        ),
        conversations = listOf(CoordinationConversation("laura", "laura", initialNow + 86_400_000, listOf(
            CoordinationMessage("Hola, me gustaría coordinar una sesión para hablar de cómo me he sentido.", true),
            CoordinationMessage("Hola. Podemos conversar con calma en este horario. ¿Te funciona?", false)
        )))
    ))
    val uiState = _uiState.asStateFlow()

    fun tick(now: Long = System.currentTimeMillis()) {
        _uiState.update { state -> state.copy(now = now, appointments = state.appointments.map {
            if (it.status == AppointmentStatus.RESERVED && (it.expiresAt ?: Long.MAX_VALUE) <= now) it.copy(status = AppointmentStatus.EXPIRED) else it
        }) }
    }

    fun startConversation(professionalId: String, message: String) {
        val id = professionalId
        _uiState.update { state ->
            val existing = state.conversations.find { it.id == id }
            val conversation = existing?.copy(messages = existing.messages + CoordinationMessage(message, true))
                ?: CoordinationConversation(id, professionalId, state.now + 86_400_000, listOf(
                    CoordinationMessage(message, true), CoordinationMessage("Gracias por escribirme. Te comparto un horario de ejemplo para coordinar nuestra sesión.", false)
                ))
            state.copy(conversations = state.conversations.filterNot { it.id == id } + conversation, activeConversationId = id)
        }
    }
    fun openConversation(id: String) { _uiState.update { it.copy(activeConversationId = id) } }
    fun closeConversation() { _uiState.update { it.copy(activeConversationId = null) } }
    fun sendMessage(id: String, text: String) {
        if (text.isBlank()) return
        _uiState.update { it.copy(conversations = it.conversations.map { chat -> if (chat.id == id) chat.copy(messages = chat.messages + CoordinationMessage(text.trim(), true)) else chat }) }
    }
    fun reserve(conversationId: String): String? {
        tick()
        val state = _uiState.value
        val existing = state.appointments.firstOrNull { it.conversationId == conversationId && it.status in listOf(AppointmentStatus.RESERVED, AppointmentStatus.CONFIRMED) }
        if (existing != null) return existing.id
        val chat = state.conversations.firstOrNull { it.id == conversationId } ?: return null
        val appointment = Appointment(UUID.randomUUID().toString(), chat.professionalId, chat.proposedAt, AppointmentStatus.RESERVED, state.now + 3_600_000, chat.id)
        _uiState.update { it.copy(appointments = it.appointments + appointment, activeConversationId = null) }
        return appointment.id
    }
    fun simulatePayment(id: String): Boolean {
        tick()
        val appointment = _uiState.value.appointments.find { it.id == id } ?: return false
        if (appointment.status != AppointmentStatus.RESERVED) return false
        change(id) { it.copy(status = AppointmentStatus.CONFIRMED, expiresAt = null) }
        return true
    }
    fun cancel(id: String) {
        change(id) { if (it.status in listOf(AppointmentStatus.RESERVED, AppointmentStatus.CONFIRMED)) it.copy(status = AppointmentStatus.CANCELLED, expiresAt = null) else it }
    }
    fun shareContext(id: String, enabled: Boolean) { change(id) { it.copy(shareContext = enabled) } }
    fun completePreview(id: String) { change(id) { if (it.status == AppointmentStatus.CONFIRMED) it.copy(status = AppointmentStatus.COMPLETED) else it } }
    fun rate(id: String, rating: Int, review: String) {
        if (rating !in 1..5) return
        change(id) { if (it.status == AppointmentStatus.COMPLETED && it.rating == null) it.copy(rating = rating, review = review.trim()) else it }
    }
    private fun change(id: String, transform: (Appointment) -> Appointment) {
        _uiState.update { state -> state.copy(appointments = state.appointments.map { if (it.id == id) transform(it) else it }) }
    }
}
