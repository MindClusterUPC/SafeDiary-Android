package com.mindcluster.safediary.rutines.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateDailyRoutineRequestDto(
    @SerializedName("patientId") val patientId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("frequencyDays") val frequencyDays: Set<String>,
    @SerializedName("isNotificationActive") val isNotificationActive: Boolean
)

data class UpdateDailyRoutineRequestDto(
    @SerializedName("title") val title: String,
    @SerializedName("isNotificationActive") val isNotificationActive: Boolean
)

data class ToggleRoutineNotificationRequestDto(
    @SerializedName("isEnabled") val isEnabled: Boolean
)

data class DailyRoutineResponseDto(
    @SerializedName("id") val id: Long,
    @SerializedName("patientId") val patientId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("frequencyDays") val frequencyDays: Set<String>,
    @SerializedName("notificationStatus") val notificationStatus: String,
    @SerializedName("isActive") val isActive: Boolean
)
