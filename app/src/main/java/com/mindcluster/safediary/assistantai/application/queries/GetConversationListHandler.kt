package com.mindcluster.safediary.assistantai.application.queries

import com.mindcluster.safediary.assistantai.domain.model.ConversationSummary
import com.mindcluster.safediary.assistantai.domain.repository.ConversationHistoryGateway

class GetConversationListQuery

class GetConversationListHandler(
    private val gateway: ConversationHistoryGateway
) {
    suspend fun handle(query: GetConversationListQuery = GetConversationListQuery()): Result<List<ConversationSummary>> =
        runCatching { gateway.listConversations() }
}
