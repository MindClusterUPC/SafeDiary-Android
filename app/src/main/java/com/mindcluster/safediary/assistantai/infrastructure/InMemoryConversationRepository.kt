package com.mindcluster.safediary.assistantai.infrastructure

import com.mindcluster.safediary.assistantai.domain.model.AiConversationAggregate
import com.mindcluster.safediary.assistantai.domain.model.ChatMessage
import com.mindcluster.safediary.assistantai.domain.model.CrisisResource
import com.mindcluster.safediary.assistantai.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryConversationRepository : ConversationRepository {
    private var conversation = AiConversationAggregate()
    private val _messagesFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val _crisisResourcesFlow = MutableStateFlow<List<CrisisResource>>(emptyList())

    override fun observeMessages(): Flow<List<ChatMessage>> = _messagesFlow.asStateFlow()

    override fun observeCrisisResources(): Flow<List<CrisisResource>> = _crisisResourcesFlow.asStateFlow()

    override suspend fun getActiveConversation(): AiConversationAggregate = conversation

    override suspend fun save(conversation: AiConversationAggregate) {
        this.conversation = conversation
        _messagesFlow.value = conversation.messages
        _crisisResourcesFlow.value = conversation.crisisResources
    }

    override suspend fun reset() {
        conversation = AiConversationAggregate()
        _messagesFlow.value = emptyList()
        _crisisResourcesFlow.value = emptyList()
    }
}
