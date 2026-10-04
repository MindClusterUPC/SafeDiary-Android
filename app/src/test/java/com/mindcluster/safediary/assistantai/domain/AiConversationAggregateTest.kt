package com.mindcluster.safediary.assistantai.domain

import com.mindcluster.safediary.assistantai.domain.events.CrisisSupportOfferedEvent
import com.mindcluster.safediary.assistantai.domain.model.AiConversationAggregate
import com.mindcluster.safediary.assistantai.domain.model.AssistantReply
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
    fun unknownBackendRiskLevelFallsBackToLow() {
        assertEquals(RiskLevel.LOW, RiskLevel.fromBackend("UNKNOWN"))
        assertEquals(RiskLevel.HIGH, RiskLevel.fromBackend("HIGH"))
    }
}
