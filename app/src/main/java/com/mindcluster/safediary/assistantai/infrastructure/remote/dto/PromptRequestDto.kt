package com.mindcluster.safediary.assistantai.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName

data class PromptRequestDto(
    @SerializedName("prompt")
    val prompt: String,

    @SerializedName("conversationId")
    val conversationId: String? = null,

    @SerializedName("locale")
    val locale: String? = null
)
