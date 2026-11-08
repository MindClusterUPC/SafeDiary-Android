package com.mindcluster.safediary.professionals.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName

data class ClinicianProfileDto(
    @SerializedName("id") val id: Long,
    @SerializedName("displayName") val displayName: String?,
    @SerializedName("professionalTitle") val professionalTitle: String?,
    @SerializedName("bio") val bio: String?,
    @SerializedName("specialties") val specialties: List<String>?,
    @SerializedName("rate") val rate: ConsultationRateDto?
)

data class ConsultationRateDto(
    @SerializedName("amount") val amount: Double?,
    @SerializedName("currency") val currency: String?,
    @SerializedName("durationMinutes") val durationMinutes: Int?
)

data class RatingSummaryDto(
    @SerializedName("average") val average: Double?,
    @SerializedName("reviewCount") val reviewCount: Int?
)
