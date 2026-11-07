package com.mindcluster.safediary.diary.domain.repository

import com.mindcluster.safediary.diary.domain.model.DiaryEntryAggregate
import kotlinx.coroutines.flow.Flow

interface DiaryEntryRepository {
    fun getEntriesFlow(patientAccountId: Long): Flow<List<DiaryEntryAggregate>>
    suspend fun getEntryByLocalId(localId: String): DiaryEntryAggregate?
    suspend fun saveEntry(entry: DiaryEntryAggregate): DiaryEntryAggregate
    suspend fun deleteEntry(localId: String)
    suspend fun syncPendingWithRemote(patientAccountId: Long)
}
