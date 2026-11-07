package com.mindcluster.safediary.diary.infrastructure.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mindcluster.safediary.diary.infrastructure.local.entity.DiaryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryEntryDao {

    @Upsert
    suspend fun upsert(entry: DiaryEntryEntity)

    @Query("SELECT * FROM diary_entries WHERE patientAccountId = :patientAccountId ORDER BY updatedAt DESC")
    fun getAllFlow(patientAccountId: Long): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entries WHERE localId = :localId LIMIT 1")
    suspend fun getByLocalId(localId: String): DiaryEntryEntity?

    @Query("DELETE FROM diary_entries WHERE localId = :localId")
    suspend fun deleteByLocalId(localId: String)

    @Query("SELECT * FROM diary_entries WHERE isSynced = 0 AND patientAccountId = :patientAccountId")
    suspend fun getUnsyncedEntries(patientAccountId: Long): List<DiaryEntryEntity>
}
