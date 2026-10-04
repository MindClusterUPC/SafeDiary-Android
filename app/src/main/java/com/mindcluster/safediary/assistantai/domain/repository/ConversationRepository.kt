package com.mindcluster.safediary.assistantai.domain.repository

import com.mindcluster.safediary.assistantai.domain.model.AiConversationAggregate
import com.mindcluster.safediary.assistantai.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun observeMessages(): Flow<List<ChatMessage>>
    suspend fun getActiveConversation(): AiConversationAggregate
    suspend fun save(conversation: AiConversationAggregate)
    suspend fun reset()
}
