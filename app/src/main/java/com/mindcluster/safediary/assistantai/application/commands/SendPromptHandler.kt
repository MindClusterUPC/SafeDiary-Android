package com.mindcluster.safediary.assistantai.application.commands

import com.mindcluster.safediary.assistantai.domain.model.AssistantResponder
import com.mindcluster.safediary.assistantai.domain.repository.ConversationHistoryGateway
import com.mindcluster.safediary.assistantai.domain.repository.ConversationRepository
import com.mindcluster.safediary.shared.domain.events.DomainEventPublisher

class SendPromptHandler(
    private val repository: ConversationRepository,
    private val responder: AssistantResponder,
    private val eventBus: DomainEventPublisher,
    private val gateway: ConversationHistoryGateway? = null
) {
    /** Returns the backend conversation id, so the UI can track the current chat. */
    suspend fun handle(command: SendPromptCommand): Result<String?> = runCatching {
        val conversation = repository.getActiveConversation()
        val userMessage = conversation.addUserMessage(command.prompt)
        repository.save(conversation)

        eventBus.publish(conversation.getDomainEvents())
        conversation.clearEvents()

        val reply = responder.respond(
            userPrompt = userMessage.content,
            history = conversation.messages,
            remoteConversationId = conversation.remoteConversationId
        )
        conversation.addAssistantResponse(reply)
        repository.save(conversation)

        eventBus.publish(conversation.getDomainEvents())
        conversation.clearEvents()

        conversation.remoteConversationId?.let { remoteId ->
            gateway?.let { gw ->
                runCatching {
                    val snapshot = gw.openConversation(remoteId)
                    conversation.syncWith(snapshot)
                    repository.save(conversation)
                }
            }
        }

        conversation.remoteConversationId
    }
}
