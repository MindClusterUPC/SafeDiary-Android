package com.mindcluster.safediary.scheduling.domain.model

/** Same lifecycle as Care Scheduling: REQUESTED is a proposal, HELD a one-hour reservation waiting for payment. */
enum class AppointmentStatus { REQUESTED, HELD, CONFIRMED, CANCELLED, EXPIRED, COMPLETED }

data class Appointment(
    val id: Long,
    val contactRequestId: Long,
    val clinicianId: Long,
    val startsAt: Long,
    val endsAt: Long,
    val amount: Double,
    val currency: String,
    val status: AppointmentStatus,
    val holdExpiresAt: Long? = null,
    val accessOpensAt: Long? = null,
    val accessClosesAt: Long? = null,
    val patientAccountId: Long? = null,
    val paymentReference: String? = null
) {
    val durationMinutes: Int get() = ((endsAt - startsAt) / 60_000).toInt()
    val amountLabel: String get() = (if (currency == "PEN") "S/ " else "$currency ") + "%.2f".format(java.util.Locale.US, amount)

    fun isHoldActive(now: Long) = status == AppointmentStatus.HELD && (holdExpiresAt ?: 0) > now

    /** Only a confirmed appointment inside the access window published by the backend can be joined. */
    fun canJoin(now: Long) = status == AppointmentStatus.CONFIRMED && accessOpensAt != null && accessClosesAt != null &&
        now in accessOpensAt..accessClosesAt

    fun remainingSeconds(now: Long) = ((holdExpiresAt ?: now) - now).coerceAtLeast(0) / 1000
}

enum class ContactRequestStatus { PENDING, ACCEPTED, REJECTED, CLOSED }

data class ContactRequest(
    val id: Long,
    val clinicianId: Long,
    val status: ContactRequestStatus,
    val lastMessage: CoordinationMessage? = null,
    val patientAccountId: Long? = null
) {
    val isOpen: Boolean get() = status == ContactRequestStatus.PENDING || status == ContactRequestStatus.ACCEPTED
}

data class CoordinationMessage(val id: Long, val text: String, val fromPatient: Boolean, val sentAt: Long)

/** The minimum Care Scheduling needs from Clinician Directory to show who the appointment is with. */
data class ClinicianSummary(val id: Long, val name: String, val initials: String)

/** Agenda owner view of a bookable slot: FREE can be proposed, the rest are held, confirmed or outside availability. */
enum class SlotAvailability { FREE, HELD, CONFIRMED, UNAVAILABLE }

/** [startsAtIso] is kept exactly as the backend sent it so a proposal reuses the same instant. */
data class BookableSlot(val startsAt: Long, val startsAtIso: String, val availability: SlotAvailability)

/** Operational outcome of an attention; no clinical content is recorded. */
enum class SessionOutcome { COMPLETED, NO_SHOW, NOT_HELD }

data class ClinicalSession(val status: String, val startedAt: Long?, val endedAt: Long?, val attendedMinutes: Int)

/** Patients are shown without personal data: Care Scheduling only knows their account reference. */
fun patientLabel(patientAccountId: Long?) = if (patientAccountId != null) "Paciente #$patientAccountId" else "Paciente"
