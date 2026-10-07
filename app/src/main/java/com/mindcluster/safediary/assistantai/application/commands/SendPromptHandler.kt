package com.mindcluster.safediary.assistantai.application.commands

import com.mindcluster.safediary.assistantai.domain.model.AssistantResponder
import com.mindcluster.safediary.assistantai.domain.repository.ConversationHistoryGateway
import com.mindcluster.safediary.assistantai.domain.repository.ConversationRepository
import com.mindcluster.safediary.shared.domain.events.DomainEventPublisher
import com.mindcluster.safediary.shared.infrastructure.network.NetworkMonitor

class OfflineException(message: String = "Device is offline") : Exception(message)

class SendPromptHandler(
    private val repository: ConversationRepository,
    private val responder: AssistantResponder,
    private val eventBus: DomainEventPublisher,
    private val gateway: ConversationHistoryGateway? = null,
    private val networkMonitor: NetworkMonitor? = null
) {
    /** Returns the backend conversation id, so the UI can track the current chat. */
    suspend fun handle(command: SendPromptCommand): Result<String?> = runCatching {
        val isOffline = networkMonitor?.isCurrentlyOnline() == false
        val conversation = repository.getActiveConversation()
        val userMessage = conversation.addUserMessage(command.prompt, isPending = isOffline)
        repository.save(conversation)

        if (isOffline) {
            throw OfflineException()
        }

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

    /** Retries sending any pending messages once back online */
    suspend fun sendPending(): Result<Unit> = runCatching {
        val conversation = repository.getActiveConversation()
        val pendingMessages = conversation.messages.filter { it.isPending }
        for (msg in pendingMessages) {
            val reply = responder.respond(
                userPrompt = msg.content,
                history = conversation.messages.filter { !it.isPending },
                remoteConversationId = conversation.remoteConversationId
            )
            conversation.markPendingSent(msg.id)
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
        }
    }
}
