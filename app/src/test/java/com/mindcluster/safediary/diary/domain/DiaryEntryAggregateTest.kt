package com.mindcluster.safediary.diary.domain

import com.mindcluster.safediary.diary.domain.model.DiaryEntryAggregate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiaryEntryAggregateTest {

    @Test
    fun createDraftGeneratesValidDefaults() {
        val draft = DiaryEntryAggregate.createDraft(patientAccountId = 1L)

        assertNotNull(draft.localId)
        assertTrue(draft.localId.isNotBlank())
        assertNull(draft.remoteId)
        assertEquals(1L, draft.patientAccountId)
        assertEquals("", draft.content)
        assertFalse(draft.isSynced)
        assertFalse(draft.isVaulted)

        // Title should follow dd/MM/yyyy format matching today's date
        val expectedTitle = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        assertEquals(expectedTitle, draft.title)
    }

    @Test
    fun defaultTitleFormatsGivenDateCorrectly() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val testDate = sdf.parse("2026-11-06")!!
        val formattedTitle = DiaryEntryAggregate.defaultTitle(testDate)
        assertEquals("06/11/2026", formattedTitle)
    }

    @Test
    fun copyAllowsContentUpdateAndModifiesTimestamp() {
        val entry = DiaryEntryAggregate.createDraft(patientAccountId = 1L)
        val updated = entry.copy(
            title = "Mi día en el parque",
            content = "## Tranquilidad\nHoy caminé un rato al aire libre.",
            updatedAt = System.currentTimeMillis() + 1000L,
            isSynced = false
        )

        assertEquals("Mi día en el parque", updated.title)
        assertEquals("## Tranquilidad\nHoy caminé un rato al aire libre.", updated.content)
        assertFalse(updated.isSynced)
        assertEquals(entry.localId, updated.localId)
    }

    @Test
    fun syncStatusUpdatesCorrectly() {
        val entry = DiaryEntryAggregate.createDraft(patientAccountId = 1L)
        assertFalse(entry.isSynced)

        val synced = entry.copy(remoteId = 88L, isSynced = true)
        assertTrue(synced.isSynced)
        assertEquals(88L, synced.remoteId)
    }
}
