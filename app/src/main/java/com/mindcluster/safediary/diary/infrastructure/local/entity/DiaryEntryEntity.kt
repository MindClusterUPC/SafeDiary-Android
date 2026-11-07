package com.mindcluster.safediary.diary.infrastructure.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mindcluster.safediary.diary.domain.model.DiaryEntryAggregate

@Entity(tableName = "diary_entries")
data class DiaryEntryEntity(
    @PrimaryKey
    val localId: String,
    val remoteId: Long? = null,
    val patientAccountId: Long,
    val title: String,
    val content: String,
    val isVaulted: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val isSynced: Boolean = false
) {
    fun toDomain(): DiaryEntryAggregate = DiaryEntryAggregate(
        localId = localId,
        remoteId = remoteId,
        patientAccountId = patientAccountId,
        title = title,
        content = content,
        isVaulted = isVaulted,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSynced = isSynced
    )

    companion object {
        fun fromDomain(domain: DiaryEntryAggregate): DiaryEntryEntity = DiaryEntryEntity(
            localId = domain.localId,
            remoteId = domain.remoteId,
            patientAccountId = domain.patientAccountId,
            title = domain.title,
            content = domain.content,
            isVaulted = domain.isVaulted,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            isSynced = domain.isSynced
        )
    }
}
