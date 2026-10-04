package com.mindcluster.safediary.assistantai.domain.repository

import com.mindcluster.safediary.assistantai.domain.model.ConversationSnapshot
import com.mindcluster.safediary.assistantai.domain.model.ConversationSummary

/**
 * Access to the conversations stored by the AssistantAI backend.
 */
interface ConversationHistoryGateway {
    suspend fun listConversations(): List<ConversationSummary>
    suspend fun openConversation(remoteId: String): ConversationSnapshot
}
