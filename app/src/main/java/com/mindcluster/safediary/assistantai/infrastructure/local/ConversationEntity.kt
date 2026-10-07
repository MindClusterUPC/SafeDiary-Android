package com.mindcluster.safediary.assistantai.infrastructure.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey
    val remoteId: String,
    val title: String?,
    val currentTone: String? = null,
    val status: String? = null,
    val lastActivityAt: Long,
    val lastMessageSnippet: String? = null
)
