package com.mindcluster.safediary.profiles.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName

data class UpdatePatientProfileRequestDto(
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String
)

data class UpdatePatientEmailRequestDto(
    @SerializedName("newEmail") val newEmail: String,
    @SerializedName("currentPassword") val currentPassword: String
)

data class ApiErrorResponseDto(
    @SerializedName("code") val code: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("details") val details: String?
)
