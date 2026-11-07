package com.mindcluster.safediary.diary.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class DiaryEntryAggregate(
    val localId: String = UUID.randomUUID().toString(),
    val remoteId: Long? = null,
    val patientAccountId: Long,
    val title: String,
    val content: String,
    val isVaulted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
) {
    companion object {
        fun defaultTitle(date: Date = Date()): String {
            return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
        }

        fun createDraft(patientAccountId: Long): DiaryEntryAggregate {
            val now = System.currentTimeMillis()
            return DiaryEntryAggregate(
                patientAccountId = patientAccountId,
                title = defaultTitle(Date(now)),
                content = "",
                createdAt = now,
                updatedAt = now,
                isSynced = false
            )
        }
    }
}
