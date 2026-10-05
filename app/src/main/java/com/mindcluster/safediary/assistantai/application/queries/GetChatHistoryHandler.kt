package com.mindcluster.safediary.assistantai.application.queries

import com.mindcluster.safediary.assistantai.domain.model.ChatMessage
import com.mindcluster.safediary.assistantai.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow

class GetChatHistoryQuery

class GetChatHistoryHandler(
    private val repository: ConversationRepository
) {
    fun handle(query: GetChatHistoryQuery = GetChatHistoryQuery()): Flow<List<ChatMessage>> {
        return repository.observeMessages()
    }
}
