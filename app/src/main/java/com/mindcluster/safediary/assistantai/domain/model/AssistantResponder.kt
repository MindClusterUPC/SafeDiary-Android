package com.mindcluster.safediary.assistantai.domain.model

interface AssistantResponder {
    suspend fun respond(
        userPrompt: String,
        history: List<ChatMessage>,
        remoteConversationId: String?
    ): AssistantReply

    suspend fun editMessage(
        remoteConversationId: String,
        messageRemoteId: Long,
        newPrompt: String
    ): AssistantReply

    suspend fun regenerate(
        remoteConversationId: String
    ): AssistantReply
}
