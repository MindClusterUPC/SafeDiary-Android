package com.mindcluster.safediary.assistantai

import com.mindcluster.safediary.assistantai.application.commands.OfflineException
import com.mindcluster.safediary.assistantai.application.commands.SendPromptCommand
import com.mindcluster.safediary.assistantai.application.commands.SendPromptHandler
import com.mindcluster.safediary.assistantai.domain.model.AiConversationAggregate
import com.mindcluster.safediary.assistantai.domain.model.AssistantReply
import com.mindcluster.safediary.assistantai.domain.model.AssistantResponder
import com.mindcluster.safediary.assistantai.domain.model.ChatMessage
import com.mindcluster.safediary.assistantai.domain.model.CrisisResource
import com.mindcluster.safediary.assistantai.domain.model.MessageAuthor
import com.mindcluster.safediary.assistantai.domain.repository.ConversationRepository
import com.mindcluster.safediary.shared.domain.events.DomainEvent
import com.mindcluster.safediary.shared.domain.events.DomainEventPublisher
import com.mindcluster.safediary.shared.infrastructure.eventbus.InMemoryDomainEventBus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeConversationRepository : ConversationRepository {
    private var conversation = AiConversationAggregate()
    private val messagesFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val crisisFlow = MutableStateFlow<List<CrisisResource>>(emptyList())

    override fun observeMessages(): Flow<List<ChatMessage>> = messagesFlow
    override fun observeCrisisResources(): Flow<List<CrisisResource>> = crisisFlow
    override suspend fun getActiveConversation(): AiConversationAggregate = conversation

    override suspend fun save(conversation: AiConversationAggregate) {
        this.conversation = conversation
        messagesFlow.value = conversation.messages
        crisisFlow.value = conversation.crisisResources
    }

    override suspend fun reset() {
        conversation = AiConversationAggregate()
        messagesFlow.value = emptyList()
        crisisFlow.value = emptyList()
    }
}

class FakeAssistantResponder : AssistantResponder {
    var respondCallCount = 0

    override suspend fun respond(
        userPrompt: String,
        history: List<ChatMessage>,
        remoteConversationId: String?
    ): AssistantReply {
        respondCallCount++
        return AssistantReply(
            content = "Respuesta a $userPrompt",
            remoteConversationId = "remote-100"
        )
    }

    override suspend fun editMessage(
        remoteConversationId: String,
        messageRemoteId: Long,
        newPrompt: String
    ): AssistantReply = AssistantReply("Editado")

    override suspend fun regenerate(remoteConversationId: String): AssistantReply =
        AssistantReply("Regenerado")
}

class OfflineChatTest {

    @Test
    fun messageAddedWithPendingFlagWhenOffline() = runBlocking {
        val repo = FakeConversationRepository()
        val conversation = repo.getActiveConversation()

        val msg = conversation.addUserMessage("Mensaje sin internet", isPending = true)
        repo.save(conversation)

        assertTrue(msg.isPending)
        val active = repo.getActiveConversation()
        assertEquals(1, active.messages.size)
        assertTrue(active.messages[0].isPending)
        assertEquals("Mensaje sin internet", active.messages[0].content)
    }

    @Test
    fun markingPendingSentUpdatesMessageState() = runBlocking {
        val repo = FakeConversationRepository()
        val conversation = repo.getActiveConversation()

        val msg = conversation.addUserMessage("Mensaje que se enviará", isPending = true)
        repo.save(conversation)
        assertTrue(repo.getActiveConversation().messages[0].isPending)

        conversation.markPendingSent(msg.id, remoteId = 555L)
        repo.save(conversation)

        val updated = repo.getActiveConversation()
        assertFalse(updated.messages[0].isPending)
        assertEquals(555L, updated.messages[0].remoteId)
    }

    @Test
    fun sendPromptHandlerOfflineFailsFastAndCanSendPendingLater() = runBlocking {
        val repo = FakeConversationRepository()
        val responder = FakeAssistantResponder()
        val eventBus = InMemoryDomainEventBus()

        // Create a handler without network monitor (simulating offline via aggregate pending)
        val handler = SendPromptHandler(repo, responder, eventBus)
        val conversation = repo.getActiveConversation()
        conversation.addUserMessage("Pendiente", isPending = true)
        repo.save(conversation)

        assertEquals(0, responder.respondCallCount)

        // Now trigger sendPending (simulating network return)
        handler.sendPending()

        assertEquals(1, responder.respondCallCount)
        val updated = repo.getActiveConversation()
        assertEquals(2, updated.messages.size)
        assertFalse(updated.messages[0].isPending)
        assertEquals(MessageAuthor.ASSISTANT, updated.messages[1].author)
    }
}
