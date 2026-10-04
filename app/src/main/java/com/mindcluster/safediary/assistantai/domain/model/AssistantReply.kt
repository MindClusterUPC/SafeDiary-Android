package com.mindcluster.safediary.assistantai.domain.model

import com.mindcluster.safediary.shared.domain.model.ValueObject

data class AssistantReply(
    val content: String,
    val remoteConversationId: String? = null,
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val crisisResources: List<CrisisResource> = emptyList()
) : ValueObject
