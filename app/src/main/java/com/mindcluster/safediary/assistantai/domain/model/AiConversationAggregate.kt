package com.mindcluster.safediary.assistantai.domain.model

import com.mindcluster.safediary.assistantai.domain.events.PromptSentEvent
import com.mindcluster.safediary.shared.domain.model.AggregateRoot
import java.util.UUID

class AiConversationAggregate(
    val id: String = UUID.randomUUID().toString()
) : AggregateRoot() {

    private val _messages = mutableListOf<ChatMessage>()
    val messages: List<ChatMessage> get() = _messages.toList()

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

    fun addAssistantResponse(content: String): ChatMessage {
        val message = ChatMessage(
            author = MessageAuthor.ASSISTANT,
            content = content
        )
        _messages.add(message)
        return message
    }

    fun clear() {
        _messages.clear()
        clearEvents()
    }
}
