package com.mindcluster.safediary.assistantai.domain.model

import com.mindcluster.safediary.shared.domain.model.ValueObject

data class ConversationSnapshot(
    val remoteId: String,
    val messages: List<ChatMessage>,
    val crisisResources: List<CrisisResource> = emptyList()
) : ValueObject
