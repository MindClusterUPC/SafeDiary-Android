package com.mindcluster.safediary.assistantai.application.commands

import com.mindcluster.safediary.assistantai.domain.model.AssistantResponder
import com.mindcluster.safediary.assistantai.domain.repository.ConversationHistoryGateway
import com.mindcluster.safediary.assistantai.domain.repository.ConversationRepository
import com.mindcluster.safediary.shared.domain.events.DomainEventPublisher

class RegenerateReplyHandler(
    private val repository: ConversationRepository,
    private val responder: AssistantResponder,
    private val gateway: ConversationHistoryGateway,
    private val eventBus: DomainEventPublisher
) {
    suspend fun handle(command: RegenerateReplyCommand = RegenerateReplyCommand()): Result<String?> = runCatching {
        val conversation = repository.getActiveConversation()
        val remoteConversationId = checkNotNull(conversation.remoteConversationId) {
            "Cannot regenerate reply in an unsaved conversation"
        }

        conversation.removeLastAssistantMessage()
        repository.save(conversation)

        val reply = responder.regenerate(remoteConversationId)
        conversation.addAssistantResponse(reply)
        repository.save(conversation)

        eventBus.publish(conversation.getDomainEvents())
        conversation.clearEvents()

        runCatching {
            val snapshot = gateway.openConversation(remoteConversationId)
            conversation.syncWith(snapshot)
            repository.save(conversation)
        }

        conversation.remoteConversationId
    }
}
