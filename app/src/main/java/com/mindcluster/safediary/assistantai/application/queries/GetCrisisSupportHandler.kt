package com.mindcluster.safediary.assistantai.application.queries

import com.mindcluster.safediary.assistantai.domain.model.CrisisResource
import com.mindcluster.safediary.assistantai.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow

class GetCrisisSupportQuery

class GetCrisisSupportHandler(
    private val repository: ConversationRepository
) {
    fun handle(query: GetCrisisSupportQuery = GetCrisisSupportQuery()): Flow<List<CrisisResource>> {
        return repository.observeCrisisResources()
    }
}
