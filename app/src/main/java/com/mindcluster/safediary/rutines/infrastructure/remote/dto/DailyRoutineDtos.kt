package com.mindcluster.safediary.rutines.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateDailyRoutineRequestDto(
    @SerializedName("patientId") val patientId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("frequencyDays") val frequencyDays: Set<String>,
    @SerializedName("isNotificationActive") val isNotificationActive: Boolean,
    val description: String = "",
    val time: String = "09:00",
    val minutes: Int = 5,
    val kind: String = "GROUNDING"
)

data class UpdateDailyRoutineRequestDto(
    @SerializedName("title") val title: String,
    @SerializedName("isNotificationActive") val isNotificationActive: Boolean,
    val frequencyDays: Set<String>? = null,
    val description: String? = null,
    val time: String? = null,
    val minutes: Int? = null,
    val kind: String? = null
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
    @SerializedName("isActive") val isActive: Boolean,
    val description: String? = null,
    val time: String? = null,
    val minutes: Int? = null,
    val kind: String? = null,
    val completedOn: String? = null
)
