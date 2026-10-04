package com.mindcluster.safediary.assistantai.application.commands

import com.mindcluster.safediary.assistantai.domain.model.AssistantResponder
import com.mindcluster.safediary.assistantai.domain.repository.ConversationRepository

class StartNewChatHandler(
    private val repository: ConversationRepository,
    private val responder: AssistantResponder
) {
    suspend fun handle(command: StartNewChatCommand = StartNewChatCommand()): Result<Unit> = runCatching {
        val current = repository.getActiveConversation()
        current.remoteConversationId?.let { responder.endConversation(it) }
        repository.reset()
    }
}
