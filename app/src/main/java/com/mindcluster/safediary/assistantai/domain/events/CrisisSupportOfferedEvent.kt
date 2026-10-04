package com.mindcluster.safediary.assistantai.domain.events

import com.mindcluster.safediary.assistantai.domain.model.RiskLevel
import com.mindcluster.safediary.shared.domain.events.DomainEvent
import java.time.Instant

data class CrisisSupportOfferedEvent(
    val conversationId: String,
    val riskLevel: RiskLevel,
    override val occurredOn: Instant = Instant.now()
) : DomainEvent
