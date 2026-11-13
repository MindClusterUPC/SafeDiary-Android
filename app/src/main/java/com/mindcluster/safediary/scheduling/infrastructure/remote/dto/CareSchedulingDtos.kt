package com.mindcluster.safediary.scheduling.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class SendContactRequestDto(
    @SerializedName("clinicianId") val clinicianId: Long,
    @SerializedName("message") val message: String
)

data class SendCoordinationMessageDto(
    @SerializedName("body") val body: String
)

data class ContactRequestDto(
    @SerializedName("id") val id: Long,
    @SerializedName("patientAccountId") val patientAccountId: Long?,
    @SerializedName("clinicianId") val clinicianId: Long,
    @SerializedName("status") val status: String?,
    @SerializedName("lastMessage") val lastMessage: CoordinationMessageDto?
)

data class CoordinationMessageDto(
    @SerializedName("id") val id: Long,
    @SerializedName("senderAccountId") val senderAccountId: Long?,
    @SerializedName("body") val body: String?,
    @SerializedName("sentAt") val sentAt: String?
)

data class AppointmentDto(
    @SerializedName("id") val id: Long,
    @SerializedName("contactRequestId") val contactRequestId: Long,
    @SerializedName("clinicianId") val clinicianId: Long,
    @SerializedName("startsAt") val startsAt: String?,
    @SerializedName("endsAt") val endsAt: String?,
    @SerializedName("amount") val amount: Double?,
    @SerializedName("currency") val currency: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("holdExpiresAt") val holdExpiresAt: String?,
    @SerializedName("accessOpensAt") val accessOpensAt: String?,
    @SerializedName("accessClosesAt") val accessClosesAt: String?,
    @SerializedName("patientAccountId") val patientAccountId: Long? = null,
    @SerializedName("paymentReference") val paymentReference: String? = null
)

data class SimulatedAppointmentPaymentDto(
    @SerializedName("paymentReference") val paymentReference: String,
    @SerializedName("approved") val approved: Boolean,
    @SerializedName("amount") val amount: Double,
    @SerializedName("currency") val currency: String
)

/** Payments and Payouts derives the patient, the psychologist and the amount from the held appointment. */
data class ConsultationCheckoutRequestDto(
    @SerializedName("appointmentId") val appointmentId: Long
)

data class ConsultationCheckoutDto(
    @SerializedName("checkoutUrl") val checkoutUrl: String?
)

data class SkipConsultationPaymentDto(
    @SerializedName("appointmentId") val appointmentId: Long,
    @SerializedName("appointmentStatus") val appointmentStatus: String?,
    @SerializedName("reference") val reference: String?
)

data class ClinicalSessionDto(
    @SerializedName("id") val id: Long,
    @SerializedName("status") val status: String?,
    @SerializedName("joinUrl") val joinUrl: String?,
    @SerializedName("startedAt") val startedAt: String? = null,
    @SerializedName("endedAt") val endedAt: String? = null,
    @SerializedName("attendedMinutes") val attendedMinutes: Int? = null
)

data class RespondContactRequestDto(
    @SerializedName("accept") val accept: Boolean
)

data class BookableSlotDto(
    @SerializedName("startsAt") val startsAt: String?,
    @SerializedName("endsAt") val endsAt: String?,
    @SerializedName("availability") val availability: String?
)

data class ProposeScheduleDto(
    @SerializedName("contactRequestId") val contactRequestId: Long,
    @SerializedName("startsAt") val startsAt: String
)

data class CloseSessionDto(
    @SerializedName("outcome") val outcome: String
)

data class ClinicianNameDto(
    @SerializedName("id") val id: Long,
    @SerializedName("displayName") val displayName: String?
)

/**
 * Parses the ISO-8601 instants sent by the backend ("2026-11-08T15:00:00Z", optionally with fractions).
 * java.time is not used because minSdk 24 has no core library desugaring.
 */
internal fun parseInstantMillis(value: String?): Long? {
    if (value.isNullOrBlank() || value.length < 19) return null
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
        val seconds = format.parse(value.substring(0, 19))?.time ?: return null
        val fraction = if (value.length > 19 && value[19] == '.') value.substring(20).takeWhile { it.isDigit() } else ""
        seconds + (fraction.padEnd(3, '0').take(3).toLongOrNull() ?: 0L)
    } catch (e: Exception) {
        null
    }
}
