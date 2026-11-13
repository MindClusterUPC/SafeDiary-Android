package com.mindcluster.safediary.rutines.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName

data class StartSosExerciseRequestDto(
    @SerializedName("patientId") val patientId: Long,
    @SerializedName("exerciseType") val exerciseType: String,
    @SerializedName("totalSteps") val totalSteps: Int
)

data class SosExerciseResponseDto(
    @SerializedName("id") val id: Long,
    @SerializedName("patientId") val patientId: Long,
    @SerializedName("exerciseType") val exerciseType: String,
    @SerializedName("currentStep") val currentStep: Int,
    @SerializedName("totalSteps") val totalSteps: Int,
    @SerializedName("completedAt") val completedAt: String?
)
