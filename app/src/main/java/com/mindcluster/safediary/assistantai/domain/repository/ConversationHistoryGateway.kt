package com.mindcluster.safediary.assistantai.domain.repository

import com.mindcluster.safediary.assistantai.domain.model.ConversationSnapshot
import com.mindcluster.safediary.assistantai.domain.model.ConversationSummary

import kotlinx.coroutines.flow.Flow

/**
 * Access to the conversations stored by the AssistantAI backend.
 */
interface ConversationHistoryGateway {
    fun observeConversations(): Flow<List<ConversationSummary>>
    suspend fun listConversations(): List<ConversationSummary>
    suspend fun openConversation(remoteId: String): ConversationSnapshot
    suspend fun renameConversation(remoteId: String, title: String): ConversationSummary
    suspend fun deleteConversation(remoteId: String)
}
