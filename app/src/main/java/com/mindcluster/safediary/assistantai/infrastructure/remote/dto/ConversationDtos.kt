package com.mindcluster.safediary.assistantai.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName

data class ConversationSummaryDto(
    @SerializedName("conversationId")
    val conversationId: String,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("startedAt")
    val startedAt: String? = null,

    @SerializedName("lastMessageAt")
    val lastMessageAt: String? = null
)

data class ConversationDto(
    @SerializedName("conversationId")
    val conversationId: String,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("messages")
    val messages: List<ConversationMessageDto> = emptyList(),

    @SerializedName("crisisResources")
    val crisisResources: List<CrisisResourceDto>? = null
)

data class ConversationMessageDto(
    @SerializedName("role")
    val role: String,

    @SerializedName("content")
    val content: String,

    @SerializedName("sentAt")
    val sentAt: String? = null
)
