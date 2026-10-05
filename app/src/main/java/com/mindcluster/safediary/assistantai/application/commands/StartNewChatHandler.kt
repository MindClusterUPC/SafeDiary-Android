package com.mindcluster.safediary.assistantai.application.commands

import com.mindcluster.safediary.assistantai.domain.repository.ConversationRepository

/**
 * Starts a blank chat. The previous conversation stays stored in the backend and listed in the history.
 */
class StartNewChatHandler(
    private val repository: ConversationRepository
) {
    suspend fun handle(command: StartNewChatCommand = StartNewChatCommand()): Result<Unit> = runCatching {
        repository.reset()
    }
}
