package com.mindcluster.safediary.assistantai.application.commands

import com.mindcluster.safediary.assistantai.domain.model.AssistantResponder
import com.mindcluster.safediary.assistantai.domain.repository.ConversationRepository
import com.mindcluster.safediary.shared.domain.events.DomainEventPublisher

class SendPromptHandler(
    private val repository: ConversationRepository,
    private val responder: AssistantResponder,
    private val eventBus: DomainEventPublisher
) {
    suspend fun handle(command: SendPromptCommand): Result<Unit> = runCatching {
        val conversation = repository.getActiveConversation()
        val userMessage = conversation.addUserMessage(command.prompt)
        repository.save(conversation)

        eventBus.publish(conversation.getDomainEvents())
        conversation.clearEvents()

        val responseContent = responder.respond(userMessage.content, conversation.messages)
        conversation.addAssistantResponse(responseContent)
        repository.save(conversation)
    }
}
