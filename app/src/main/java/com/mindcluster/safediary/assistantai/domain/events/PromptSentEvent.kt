package com.mindcluster.safediary.assistantai.domain.events

import com.mindcluster.safediary.shared.domain.events.DomainEvent
import java.time.Instant

data class PromptSentEvent(
    val conversationId: String,
    val messageId: String,
    val userPrompt: String,
    override val occurredOn: Instant = Instant.now()
) : DomainEvent
