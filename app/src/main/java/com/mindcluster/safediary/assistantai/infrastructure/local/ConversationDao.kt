package com.mindcluster.safediary.assistantai.infrastructure.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY lastActivityAt DESC")
    fun observeAll(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations ORDER BY lastActivityAt DESC")
    suspend fun getAll(): List<ConversationEntity>

    @Query("SELECT * FROM conversations WHERE remoteId = :remoteId LIMIT 1")
    fun observeById(remoteId: String): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversations WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getById(remoteId: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(conversations: List<ConversationEntity>)

    @Query("UPDATE conversations SET title = :title WHERE remoteId = :remoteId")
    suspend fun updateTitle(remoteId: String, title: String)

    @Query("DELETE FROM conversations WHERE remoteId = :remoteId")
    suspend fun deleteById(remoteId: String)

    @Query("DELETE FROM conversations")
    suspend fun deleteAll()
}
