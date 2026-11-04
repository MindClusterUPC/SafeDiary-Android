package com.mindcluster.safediary.profiles.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName

data class PatientResponseDto(
    @SerializedName("id") val id: Long,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("email") val email: String,
    @SerializedName("emergencyContact") val emergencyContact: EmergencyContactDto?,
    @SerializedName("planStatus") val planStatus: PlanStatusDto?
)

data class EmergencyContactDto(
    @SerializedName("name") val name: String,
    @SerializedName("relationship") val relationship: String,
    @SerializedName("phoneNumber") val phoneNumber: String?,
    @SerializedName("email") val email: String?
)

data class PlanStatusDto(
    @SerializedName("plan") val plan: String,
    @SerializedName("status") val status: String,
    @SerializedName("active") val active: Boolean
)
