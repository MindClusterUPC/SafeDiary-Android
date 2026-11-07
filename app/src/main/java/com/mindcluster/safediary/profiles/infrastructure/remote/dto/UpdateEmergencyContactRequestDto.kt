package com.mindcluster.safediary.profiles.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName

data class UpdateEmergencyContactRequestDto(
    @SerializedName("name") val name: String,
    @SerializedName("relationship") val relationship: String,
    @SerializedName("phoneNumber") val phoneNumber: String?,
    @SerializedName("email") val email: String?
)
