package com.mindcluster.safediary.diary.infrastructure.repositories

import com.mindcluster.safediary.diary.domain.model.DiaryEntryAggregate
import com.mindcluster.safediary.diary.domain.repository.DiaryEntryRepository
import com.mindcluster.safediary.diary.infrastructure.local.dao.DiaryEntryDao
import com.mindcluster.safediary.diary.infrastructure.local.entity.DiaryEntryEntity
import com.mindcluster.safediary.diary.infrastructure.remote.RetrofitDiaryGateway
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OfflineFirstDiaryRepository(
    private val dao: DiaryEntryDao,
    private val remoteGateway: RetrofitDiaryGateway,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : DiaryEntryRepository {

    override fun getEntriesFlow(patientAccountId: Long): Flow<List<DiaryEntryAggregate>> {
        // Trigger a background remote sync whenever entries are requested
        coroutineScope.launch {
            syncPendingWithRemote(patientAccountId)
        }
        return dao.getAllFlow(patientAccountId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getEntryByLocalId(localId: String): DiaryEntryAggregate? = withContext(Dispatchers.IO) {
        dao.getByLocalId(localId)?.toDomain()
    }

    override suspend fun saveEntry(entry: DiaryEntryAggregate): DiaryEntryAggregate = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val updated = entry.copy(updatedAt = now)
        // 1. Save locally to SQLite first
        dao.upsert(DiaryEntryEntity.fromDomain(updated))

        // 2. Sync to remote in background
        coroutineScope.launch {
            try {
                if (updated.remoteId != null) {
                    val res = remoteGateway.updateEntry(updated.remoteId, updated.title, updated.content)
                    if (res.isSuccess) {
                        dao.upsert(DiaryEntryEntity.fromDomain(updated.copy(isSynced = true)))
                    }
                } else {
                    val res = remoteGateway.createEntry(
                        updated.patientAccountId,
                        updated.title,
                        updated.content,
                        updated.isVaulted
                    )
                    res.getOrNull()?.let { dto ->
                        dao.upsert(DiaryEntryEntity.fromDomain(updated.copy(remoteId = dto.id, isSynced = true)))
                    }
                }
            } catch (ignored: Exception) {}
        }
        updated
    }

    override suspend fun deleteEntry(localId: String) = withContext(Dispatchers.IO) {
        val existing = dao.getByLocalId(localId)
        dao.deleteByLocalId(localId)
        if (existing?.remoteId != null) {
            coroutineScope.launch {
                try {
                    remoteGateway.deleteEntry(existing.remoteId)
                } catch (ignored: Exception) {}
            }
        }
    }

    override suspend fun syncPendingWithRemote(patientAccountId: Long) = withContext(Dispatchers.IO) {
        try {
            val remoteResult = remoteGateway.listEntries(patientAccountId)
            val remoteEntries = remoteResult.getOrNull() ?: return@withContext

            for (dto in remoteEntries) {
                val existingLocal = dao.getAllFlow(patientAccountId)
                // If not in local, add it
                val entity = DiaryEntryEntity(
                    localId = "remote_${dto.id}",
                    remoteId = dto.id,
                    patientAccountId = dto.patientId,
                    title = dto.title,
                    content = dto.content.orEmpty(),
                    isVaulted = dto.vaulted,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    isSynced = true
                )
                dao.upsert(entity)
            }
        } catch (ignored: Exception) {}
    }
}
