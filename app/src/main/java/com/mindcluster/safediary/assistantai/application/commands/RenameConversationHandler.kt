package com.mindcluster.safediary.assistantai.application.commands

import com.mindcluster.safediary.assistantai.domain.model.ConversationSummary
import com.mindcluster.safediary.assistantai.domain.repository.ConversationHistoryGateway

class RenameConversationHandler(
    private val gateway: ConversationHistoryGateway
) {
    suspend fun handle(command: RenameConversationCommand): Result<ConversationSummary> = runCatching {
        gateway.renameConversation(command.remoteId, command.newTitle)
    }
}
