package com.mindcluster.safediary.assistantai.infrastructure.local

import com.mindcluster.safediary.assistantai.domain.model.AiConversationAggregate
import com.mindcluster.safediary.assistantai.domain.model.ChatMessage
import com.mindcluster.safediary.assistantai.domain.model.ConversationSnapshot
import com.mindcluster.safediary.assistantai.domain.model.CrisisResource
import com.mindcluster.safediary.assistantai.domain.model.MessageAuthor
import com.mindcluster.safediary.assistantai.domain.repository.ConversationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant

class OfflineFirstConversationRepository(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val chatPreferences: ChatPreferences,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : ConversationRepository {

    private var conversation = AiConversationAggregate()
    private val _messagesFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val _crisisResourcesFlow = MutableStateFlow<List<CrisisResource>>(emptyList())

    init {
        // Reopen the last conversation from cache instantly on launch
        scope.launch {
            val lastConversationId = chatPreferences.getLastOpenedConversationId()
            if (lastConversationId != null) {
                val cachedMessages = messageDao.getMessages(lastConversationId)
                if (cachedMessages.isNotEmpty()) {
                    val domainMessages = cachedMessages.map { entity ->
                        ChatMessage(
                            remoteId = entity.remoteId,
                            author = if (entity.sender == "user") MessageAuthor.USER else MessageAuthor.ASSISTANT,
                            content = entity.content,
                            sentAt = Instant.ofEpochMilli(entity.sentAt),
                            isPending = entity.pending
                        )
                    }
                    val snapshot = ConversationSnapshot(
                        remoteId = lastConversationId,
                        messages = domainMessages,
                        crisisResources = emptyList()
                    )
                    conversation.syncWith(snapshot)
                    _messagesFlow.value = domainMessages
                }
            }
        }
    }

    override fun observeMessages(): Flow<List<ChatMessage>> = _messagesFlow.asStateFlow()

    override fun observeCrisisResources(): Flow<List<CrisisResource>> = _crisisResourcesFlow.asStateFlow()

    override suspend fun getActiveConversation(): AiConversationAggregate = conversation

    override suspend fun save(conversation: AiConversationAggregate) = withContext(Dispatchers.IO) {
        this@OfflineFirstConversationRepository.conversation = conversation
        _messagesFlow.value = conversation.messages
        _crisisResourcesFlow.value = conversation.crisisResources

        val remoteId = conversation.remoteConversationId
        if (remoteId != null) {
            chatPreferences.setLastOpenedConversationId(remoteId)
            val lastMsg = conversation.messages.lastOrNull()
            val existing = conversationDao.getById(remoteId)
            val title = existing?.title
                ?: conversation.messages.firstOrNull()?.content?.take(40)
                ?: "Conversación"

            conversationDao.upsert(
                ConversationEntity(
                    remoteId = remoteId,
                    title = title,
                    currentTone = null,
                    status = null,
                    lastActivityAt = lastMsg?.sentAt?.toEpochMilli() ?: System.currentTimeMillis(),
                    lastMessageSnippet = lastMsg?.content
                )
            )

            // Persist messages into Room
            val entities = conversation.messages.map { msg ->
                MessageEntity(
                    remoteId = msg.remoteId,
                    conversationRemoteId = remoteId,
                    sender = if (msg.author == MessageAuthor.USER) "user" else "assistant",
                    content = msg.content,
                    sentAt = msg.sentAt.toEpochMilli(),
                    pending = msg.isPending
                )
            }
            messageDao.replaceConversationMessages(remoteId, entities)
        }
    }

    override suspend fun reset() = withContext(Dispatchers.IO) {
        conversation = AiConversationAggregate()
        chatPreferences.setLastOpenedConversationId(null)
        _messagesFlow.value = emptyList()
        _crisisResourcesFlow.value = emptyList()
    }
}
