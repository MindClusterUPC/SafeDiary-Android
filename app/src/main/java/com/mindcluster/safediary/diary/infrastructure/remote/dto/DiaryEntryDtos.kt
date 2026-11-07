package com.mindcluster.safediary.diary.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName

data class DiaryEntryDto(
    @SerializedName("id") val id: Long,
    @SerializedName("patientId") val patientId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String?,
    @SerializedName("vaulted") val vaulted: Boolean,
    @SerializedName("entryDate") val entryDate: String?
)

data class CreateDiaryEntryRequest(
    @SerializedName("patientId") val patientId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("vaulted") val vaulted: Boolean
)

data class UpdateDiaryEntryRequest(
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String
)
