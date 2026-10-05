package com.mindcluster.safediary.assistantai.application.commands

import com.mindcluster.safediary.assistantai.domain.model.AssistantResponder
import com.mindcluster.safediary.assistantai.domain.repository.ConversationHistoryGateway
import com.mindcluster.safediary.assistantai.domain.repository.ConversationRepository
import com.mindcluster.safediary.shared.domain.events.DomainEventPublisher

class EditMessageHandler(
    private val repository: ConversationRepository,
    private val responder: AssistantResponder,
    private val gateway: ConversationHistoryGateway,
    private val eventBus: DomainEventPublisher
) {
    suspend fun handle(command: EditMessageCommand): Result<String?> = runCatching {
        val conversation = repository.getActiveConversation()
        val remoteConversationId = checkNotNull(conversation.remoteConversationId) {
            "Cannot edit message in an unsaved conversation"
        }

        conversation.truncateFrom(command.messageRemoteId)
        val userMessage = conversation.addUserMessage(command.newPrompt)
        repository.save(conversation)

        eventBus.publish(conversation.getDomainEvents())
        conversation.clearEvents()

        val reply = responder.editMessage(
            remoteConversationId = remoteConversationId,
            messageRemoteId = command.messageRemoteId,
            newPrompt = userMessage.content
        )
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
