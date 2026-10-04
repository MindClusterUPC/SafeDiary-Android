package com.mindcluster.safediary.assistantai.domain.model

interface AssistantResponder {
    suspend fun respond(
        userPrompt: String,
        history: List<ChatMessage>,
        remoteConversationId: String?
    ): AssistantReply
}
