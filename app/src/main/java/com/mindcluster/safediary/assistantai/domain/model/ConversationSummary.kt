package com.mindcluster.safediary.assistantai.domain.model

import com.mindcluster.safediary.shared.domain.model.ValueObject
import java.time.Instant

data class ConversationSummary(
    val remoteId: String,
    val title: String?,
    val lastActivityAt: Instant
) : ValueObject
