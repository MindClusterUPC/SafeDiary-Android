package com.mindcluster.safediary.assistantai.application.commands

import com.mindcluster.safediary.assistantai.domain.model.AssistantResponder
import com.mindcluster.safediary.assistantai.domain.repository.ConversationHistoryGateway
import com.mindcluster.safediary.assistantai.domain.repository.ConversationRepository
import com.mindcluster.safediary.shared.domain.events.DomainEventPublisher

/**
 * Asks the assistant again for the last user message that did not get a reply.
 */
class RetryLastPromptHandler(
    private val repository: ConversationRepository,
    private val responder: AssistantResponder,
    private val eventBus: DomainEventPublisher,
    private val gateway: ConversationHistoryGateway? = null
) {
    suspend fun handle(command: RetryLastPromptCommand = RetryLastPromptCommand()): Result<String?> = runCatching {
        val conversation = repository.getActiveConversation()
        val pending = conversation.pendingUserMessage() ?: return@runCatching conversation.remoteConversationId

        val reply = responder.respond(
            userPrompt = pending.content,
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
