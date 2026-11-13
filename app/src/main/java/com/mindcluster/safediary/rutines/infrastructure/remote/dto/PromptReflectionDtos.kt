package com.mindcluster.safediary.rutines.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName

data class CreatePromptReflectionRequestDto(
    @SerializedName("patientId") val patientId: Long,
    @SerializedName("promptText") val promptText: String
)

data class SubmitDailyReflectionRequestDto(
    @SerializedName("answer") val answer: String
)

data class PromptReflectionResponseDto(
    @SerializedName("id") val id: Long,
    @SerializedName("patientId") val patientId: Long,
    @SerializedName("promptText") val promptText: String,
    @SerializedName("answer") val answer: String?,
    @SerializedName("status") val status: String
)
