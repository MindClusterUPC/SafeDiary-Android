package com.mindcluster.safediary.assistantai.application.commands

import com.mindcluster.safediary.assistantai.domain.repository.ConversationHistoryGateway
import com.mindcluster.safediary.assistantai.domain.repository.ConversationRepository

class DeleteConversationHandler(
    private val gateway: ConversationHistoryGateway,
    private val repository: ConversationRepository
) {
    suspend fun handle(command: DeleteConversationCommand): Result<Unit> = runCatching {
        gateway.deleteConversation(command.remoteId)
        val active = repository.getActiveConversation()
        if (active.remoteConversationId == command.remoteId) {
            repository.reset()
        }
    }
}
