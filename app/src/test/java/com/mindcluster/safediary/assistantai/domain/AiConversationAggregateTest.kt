package com.mindcluster.safediary.assistantai.domain

import com.mindcluster.safediary.assistantai.domain.events.CrisisSupportOfferedEvent
import com.mindcluster.safediary.assistantai.domain.model.AiConversationAggregate
import com.mindcluster.safediary.assistantai.domain.model.AssistantReply
import com.mindcluster.safediary.assistantai.domain.model.ChatMessage
import com.mindcluster.safediary.assistantai.domain.model.ConversationSnapshot
import com.mindcluster.safediary.assistantai.domain.model.MessageAuthor
import com.mindcluster.safediary.assistantai.domain.model.CrisisResource
import com.mindcluster.safediary.assistantai.domain.model.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiConversationAggregateTest {

    private val hotline = CrisisResource("Línea 113 – opción 5 (MINSA)", "113", "Salud mental, 24 horas")

    @Test
    fun keepsRemoteConversationIdFromReplies() {
        val conversation = AiConversationAggregate()
        conversation.addUserMessage("hola")

        conversation.addAssistantResponse(AssistantReply(content = "Te escucho", remoteConversationId = "12"))

        assertEquals("12", conversation.remoteConversationId)
        assertEquals(2, conversation.messages.size)
    }

    @Test
    fun offersCrisisSupportOnlyForHighOrCriticalRisk() {
        val conversation = AiConversationAggregate()
        conversation.addUserMessage("me siento mal")
        conversation.clearEvents()

        conversation.addAssistantResponse(
            AssistantReply(content = "Estoy aquí", riskLevel = RiskLevel.MODERATE, crisisResources = listOf(hotline))
        )
        assertTrue(conversation.crisisResources.isEmpty())

        conversation.addAssistantResponse(
            AssistantReply(content = "Busca ayuda", riskLevel = RiskLevel.CRITICAL, crisisResources = listOf(hotline))
        )
        assertEquals(listOf(hotline), conversation.crisisResources)
        assertTrue(conversation.getDomainEvents().any { it is CrisisSupportOfferedEvent })
    }

    @Test
    fun clearResetsRemoteStateAndCrisisSupport() {
        val conversation = AiConversationAggregate()
        conversation.addUserMessage("hola")
        conversation.addAssistantResponse(
            AssistantReply("Busca ayuda", "7", RiskLevel.HIGH, listOf(hotline))
        )

        conversation.clear()

        assertNull(conversation.remoteConversationId)
        assertEquals(RiskLevel.LOW, conversation.riskLevel)
        assertTrue(conversation.crisisResources.isEmpty())
        assertTrue(conversation.messages.isEmpty())
    }

    @Test
    fun pendingUserMessageIsTheLastUnansweredPrompt() {
        val conversation = AiConversationAggregate()
        val prompt = conversation.addUserMessage("hola")
        assertEquals(prompt, conversation.pendingUserMessage())

        conversation.addAssistantResponse(AssistantReply(content = "Te escucho"))
        assertNull(conversation.pendingUserMessage())
    }

    @Test
    fun restoreRebuildsAStoredConversationToContinueIt() {
        val snapshot = ConversationSnapshot(
            remoteId = "9",
            messages = listOf(
                ChatMessage(author = MessageAuthor.USER, content = "hola"),
                ChatMessage(author = MessageAuthor.ASSISTANT, content = "Te escucho")
            )
        )

        val conversation = AiConversationAggregate.restore(snapshot)

        assertEquals("9", conversation.remoteConversationId)
        assertEquals(2, conversation.messages.size)
        assertNull(conversation.pendingUserMessage())
        assertTrue(conversation.crisisResources.isEmpty())
    }

    @Test
    fun unknownBackendRiskLevelFallsBackToLow() {
        assertEquals(RiskLevel.LOW, RiskLevel.fromBackend("UNKNOWN"))
        assertEquals(RiskLevel.HIGH, RiskLevel.fromBackend("HIGH"))
    }

    @Test
    fun truncateFromRemovesTargetMessageAndSubsequentMessages() {
        val snapshot = ConversationSnapshot(
            remoteId = "10",
            messages = listOf(
                ChatMessage(author = MessageAuthor.USER, content = "m1", remoteId = 101L),
                ChatMessage(author = MessageAuthor.ASSISTANT, content = "m2", remoteId = 102L),
                ChatMessage(author = MessageAuthor.USER, content = "m3", remoteId = 103L),
                ChatMessage(author = MessageAuthor.ASSISTANT, content = "m4", remoteId = 104L)
            )
        )
        val conversation = AiConversationAggregate.restore(snapshot)

        val result = conversation.truncateFrom(103L)
        assertTrue(result)
        assertEquals(2, conversation.messages.size)
        assertEquals(listOf(101L, 102L), conversation.messages.map { it.remoteId })
    }

    @Test
    fun truncateFromReturnsFalseWhenRemoteIdNotFound() {
        val snapshot = ConversationSnapshot(
            remoteId = "10",
            messages = listOf(
                ChatMessage(author = MessageAuthor.USER, content = "m1", remoteId = 101L)
            )
        )
        val conversation = AiConversationAggregate.restore(snapshot)

        val result = conversation.truncateFrom(999L)
        org.junit.Assert.assertFalse(result)
        assertEquals(1, conversation.messages.size)
    }

    @Test
    fun removeLastAssistantMessageRemovesOnlyIfAssistant() {
        val conversation = AiConversationAggregate()
        conversation.addUserMessage("hola")
        val reply = conversation.addAssistantResponse(AssistantReply(content = "hola respuesta"))

        val removed = conversation.removeLastAssistantMessage()
        assertEquals(reply.content, removed?.content)
        assertEquals(1, conversation.messages.size)

        val removedAgain = conversation.removeLastAssistantMessage()
        assertNull(removedAgain)
        assertEquals(1, conversation.messages.size)
    }

    @Test
    fun syncWithReplacesMessagesAndUpdatesState() {
        val conversation = AiConversationAggregate()
        conversation.addUserMessage("old message")

        val snapshot = ConversationSnapshot(
            remoteId = "new-id",
            messages = listOf(
                ChatMessage(author = MessageAuthor.USER, content = "synced message", remoteId = 50L)
            ),
            crisisResources = listOf(hotline)
        )

        conversation.syncWith(snapshot)
        assertEquals("new-id", conversation.remoteConversationId)
        assertEquals(1, conversation.messages.size)
        assertEquals(50L, conversation.messages[0].remoteId)
        assertEquals(listOf(hotline), conversation.crisisResources)
        assertEquals(RiskLevel.HIGH, conversation.riskLevel)
    }
}
