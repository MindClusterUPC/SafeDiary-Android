package com.mindcluster.safediary.assistantai.domain.model

import com.mindcluster.safediary.assistantai.domain.events.CrisisSupportOfferedEvent
import com.mindcluster.safediary.assistantai.domain.events.PromptSentEvent
import com.mindcluster.safediary.shared.domain.model.AggregateRoot
import java.util.UUID

class AiConversationAggregate(
    val id: String = UUID.randomUUID().toString()
) : AggregateRoot() {

    private val _messages = mutableListOf<ChatMessage>()
    val messages: List<ChatMessage> get() = _messages.toList()

    var remoteConversationId: String? = null
        private set

    var riskLevel: RiskLevel = RiskLevel.LOW
        private set

    var crisisResources: List<CrisisResource> = emptyList()
        private set

    fun addUserMessage(content: String): ChatMessage {
        val trimmed = content.trim()
        require(trimmed.isNotBlank()) { "Prompt cannot be empty" }
        val message = ChatMessage(
            author = MessageAuthor.USER,
            content = trimmed
        )
        _messages.add(message)
        raise(
            PromptSentEvent(
                conversationId = id,
                messageId = message.id,
                userPrompt = trimmed
            )
        )
        return message
    }

    fun addAssistantResponse(reply: AssistantReply): ChatMessage {
        val message = ChatMessage(
            author = MessageAuthor.ASSISTANT,
            content = reply.content
        )
        _messages.add(message)
        reply.remoteConversationId?.let { remoteConversationId = it }
        riskLevel = reply.riskLevel
        if (reply.riskLevel.requiresCrisisSupport && reply.crisisResources.isNotEmpty()) {
            crisisResources = reply.crisisResources
            raise(CrisisSupportOfferedEvent(conversationId = id, riskLevel = reply.riskLevel))
        }
        return message
    }

    /** Last user message still waiting for an assistant reply, if any. */
    fun pendingUserMessage(): ChatMessage? =
        _messages.lastOrNull()?.takeIf { it.author == MessageAuthor.USER }

    fun dismissCrisisSupport() {
        crisisResources = emptyList()
    }

    fun clear() {
        _messages.clear()
        remoteConversationId = null
        riskLevel = RiskLevel.LOW
        crisisResources = emptyList()
        clearEvents()
    }
}
