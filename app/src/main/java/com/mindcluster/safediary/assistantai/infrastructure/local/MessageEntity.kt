package com.mindcluster.safediary.assistantai.infrastructure.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    indices = [
        Index("conversationRemoteId"),
        Index("pending")
    ]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val remoteId: Long? = null,
    val conversationRemoteId: String,
    val sender: String,
    val content: String,
    val emotionTag: String? = null,
    val sentAt: Long,
    val pending: Boolean = false
)
