package com.mindcluster.safediary.assistantai.domain.model

import com.mindcluster.safediary.shared.domain.model.Entity
import java.time.Instant
import java.util.UUID

data class ChatMessage(
    override val id: String = UUID.randomUUID().toString(),
    val author: MessageAuthor,
    val content: String,
    val sentAt: Instant = Instant.now(),
    val remoteId: Long? = null,
    val isPending: Boolean = false
) : Entity<String>()
