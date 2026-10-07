package com.mindcluster.safediary.assistantai.infrastructure.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationRemoteId = :conversationRemoteId ORDER BY sentAt ASC, localId ASC")
    fun observeMessages(conversationRemoteId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationRemoteId = :conversationRemoteId ORDER BY sentAt ASC, localId ASC")
    suspend fun getMessages(conversationRemoteId: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE pending = 1 ORDER BY sentAt ASC")
    suspend fun getPendingMessages(): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Query("UPDATE messages SET pending = :pending WHERE localId = :localId")
    suspend fun setPending(localId: Long, pending: Boolean)

    @Query("UPDATE messages SET remoteId = :remoteId, pending = 0 WHERE localId = :localId")
    suspend fun markSent(localId: Long, remoteId: Long)

    @Query("DELETE FROM messages WHERE conversationRemoteId = :conversationRemoteId")
    suspend fun deleteByConversation(conversationRemoteId: String)

    @Query("DELETE FROM messages WHERE localId = :localId")
    suspend fun deleteById(localId: Long)

    @Query("DELETE FROM messages WHERE remoteId = :remoteId")
    suspend fun deleteByRemoteId(remoteId: Long)

    @Query("DELETE FROM messages WHERE conversationRemoteId = :conversationRemoteId AND pending = 0")
    suspend fun deleteNonPendingByConversation(conversationRemoteId: String)

    /** Replaces every cached message of a conversation, so repeated saves never duplicate rows. */
    @Transaction
    suspend fun replaceConversationMessages(conversationRemoteId: String, messages: List<MessageEntity>) {
        deleteByConversation(conversationRemoteId)
        insertAll(messages)
    }

    @Transaction
    suspend fun syncRemoteMessages(conversationRemoteId: String, remoteMessages: List<MessageEntity>) {
        deleteNonPendingByConversation(conversationRemoteId)
        insertAll(remoteMessages)
    }
}
