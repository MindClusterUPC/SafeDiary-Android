package com.mindcluster.safediary.assistantai.application.commands

import com.mindcluster.safediary.assistantai.domain.model.AiConversationAggregate
import com.mindcluster.safediary.assistantai.domain.repository.ConversationHistoryGateway
import com.mindcluster.safediary.assistantai.domain.repository.ConversationRepository

/**
 * Loads a stored conversation from the backend and makes it the active one, so the user can continue it.
 */
class OpenConversationHandler(
    private val gateway: ConversationHistoryGateway,
    private val repository: ConversationRepository
) {
    suspend fun handle(command: OpenConversationCommand): Result<Unit> = runCatching {
        val snapshot = gateway.openConversation(command.remoteId)
        repository.save(AiConversationAggregate.restore(snapshot))
    }
}
