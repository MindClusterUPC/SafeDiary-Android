package com.mindcluster.safediary.assistantai.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName

data class AssistantResponseDto(
    @SerializedName("reply")
    val reply: String,

    @SerializedName("sentiment")
    val sentiment: String? = null,

    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
