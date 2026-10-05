package com.mindcluster.safediary.assistantai.application.commands

import com.mindcluster.safediary.assistantai.domain.repository.ConversationRepository

class DismissCrisisSupportHandler(
    private val repository: ConversationRepository
) {
    suspend fun handle(command: DismissCrisisSupportCommand = DismissCrisisSupportCommand()): Result<Unit> = runCatching {
        val conversation = repository.getActiveConversation()
        conversation.dismissCrisisSupport()
        repository.save(conversation)
    }
}
