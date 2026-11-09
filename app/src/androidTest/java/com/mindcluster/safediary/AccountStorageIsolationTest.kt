package com.mindcluster.safediary

import androidx.test.platform.app.InstrumentationRegistry
import com.mindcluster.safediary.assistantai.infrastructure.local.ChatPreferences
import com.mindcluster.safediary.assistantai.infrastructure.local.ConversationEntity
import com.mindcluster.safediary.assistantai.infrastructure.local.SafeDiaryDatabase
import com.mindcluster.safediary.shared.infrastructure.security.AccountStorage
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class AccountStorageIsolationTest {
    @Test fun twoAccountsKeepSeparateCachedHistoryAndLastConversation() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val a = 9_000_000_000_000L + System.currentTimeMillis()
        val b = a + 1
        val dbA = SafeDiaryDatabase.getInstance(context, a)
        val dbB = SafeDiaryDatabase.getInstance(context, b)
        val prefsA = ChatPreferences(context, a)
        val prefsB = ChatPreferences(context, b)
        try {
            dbA.conversationDao().upsert(ConversationEntity(remoteId = "same-id", title = "Only A", lastActivityAt = 1L))
            prefsA.setLastOpenedConversationId("A-conversation")
            assertTrue(dbB.conversationDao().getAll().isEmpty())
            assertNull(prefsB.getLastOpenedConversationId())
            dbB.conversationDao().upsert(ConversationEntity(remoteId = "same-id", title = "Only B", lastActivityAt = 2L))
            prefsB.setLastOpenedConversationId("B-conversation")
            assertEquals("Only A", dbA.conversationDao().getAll().single().title)
            assertEquals("Only B", dbB.conversationDao().getAll().single().title)
            assertEquals("A-conversation", prefsA.getLastOpenedConversationId())
            assertEquals("B-conversation", prefsB.getLastOpenedConversationId())
        } finally {
            dbA.close(); dbB.close()
            context.deleteDatabase(AccountStorage.databaseName(a))
            context.deleteDatabase(AccountStorage.databaseName(b))
            context.getSharedPreferences(AccountStorage.chatPreferencesName(a), 0).edit().clear().commit()
            context.getSharedPreferences(AccountStorage.chatPreferencesName(b), 0).edit().clear().commit()
        }
    }
}
