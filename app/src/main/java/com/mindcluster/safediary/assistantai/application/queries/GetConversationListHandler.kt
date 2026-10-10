package com.mindcluster.safediary.assistantai.application.queries

import com.mindcluster.safediary.assistantai.domain.model.ConversationSummary
import com.mindcluster.safediary.assistantai.domain.repository.ConversationHistoryGateway
import kotlinx.coroutines.flow.Flow

class GetConversationListQuery

class GetConversationListHandler(
    private val gateway: ConversationHistoryGateway
) {
    fun observe(): Flow<List<ConversationSummary>> = gateway.observeConversations()

    suspend fun handle(query: GetConversationListQuery = GetConversationListQuery()): Result<List<ConversationSummary>> =
        runCatching { gateway.listConversations() }
}
