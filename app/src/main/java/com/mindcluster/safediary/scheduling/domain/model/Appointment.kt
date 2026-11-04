package com.mindcluster.safediary.scheduling.domain.model

enum class AppointmentStatus { RESERVED, CONFIRMED, COMPLETED, CANCELLED, EXPIRED }

data class Appointment(
    val id: String,
    val professionalId: String,
    val startsAt: Long,
    val status: AppointmentStatus,
    val expiresAt: Long? = null,
    val conversationId: String? = null,
    val shareContext: Boolean = false,
    val rating: Int? = null,
    val review: String = ""
) {
    fun canJoin(now: Long) = status == AppointmentStatus.CONFIRMED && now in (startsAt - 10 * 60_000)..(startsAt + 50 * 60_000)
    fun remainingSeconds(now: Long) = ((expiresAt ?: now) - now).coerceAtLeast(0) / 1000
}

data class CoordinationMessage(val text: String, val fromPatient: Boolean)
data class CoordinationConversation(val id: String, val professionalId: String, val proposedAt: Long, val messages: List<CoordinationMessage>)
