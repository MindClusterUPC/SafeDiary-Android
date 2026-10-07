package com.mindcluster.safediary.assistantai.infrastructure.local

import android.util.Log
import com.mindcluster.safediary.assistantai.domain.model.AssistantUnavailableException
import com.mindcluster.safediary.assistantai.domain.model.ChatMessage
import com.mindcluster.safediary.assistantai.domain.model.ConversationSnapshot
import com.mindcluster.safediary.assistantai.domain.model.ConversationSummary
import com.mindcluster.safediary.assistantai.domain.model.MessageAuthor
import com.mindcluster.safediary.assistantai.domain.repository.ConversationHistoryGateway
import com.mindcluster.safediary.assistantai.infrastructure.remote.RetrofitConversationHistoryGateway
import com.mindcluster.safediary.shared.infrastructure.network.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant

class CacheFirstConversationHistoryGateway(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val remoteGateway: RetrofitConversationHistoryGateway,
    private val networkMonitor: NetworkMonitor
) : ConversationHistoryGateway {

    private val tag = "CacheFirstGateway"

    override fun observeConversations(): Flow<List<ConversationSummary>> {
        return conversationDao.observeAll().map { list ->
            list.map { entity ->
                ConversationSummary(
                    remoteId = entity.remoteId,
                    title = entity.title,
                    lastActivityAt = Instant.ofEpochMilli(entity.lastActivityAt)
                )
            }
        }
    }

    override suspend fun listConversations(): List<ConversationSummary> = withContext(Dispatchers.IO) {
        val cached = conversationDao.getAll().map { entity ->
            ConversationSummary(
                remoteId = entity.remoteId,
                title = entity.title,
                lastActivityAt = Instant.ofEpochMilli(entity.lastActivityAt)
            )
        }

        if (!networkMonitor.isCurrentlyOnline()) {
            return@withContext cached
        }

        try {
            val remote = remoteGateway.listConversations()
            val entities = remote.map { summary ->
                ConversationEntity(
                    remoteId = summary.remoteId,
                    title = summary.title,
                    lastActivityAt = summary.lastActivityAt.toEpochMilli()
                )
            }
            conversationDao.upsertAll(entities)
            remote
        } catch (e: Exception) {
            Log.w(tag, "Failed to refresh conversations from remote, returning cache", e)
            cached
        }
    }

    override suspend fun openConversation(remoteId: String): ConversationSnapshot = withContext(Dispatchers.IO) {
        val cachedMessages = messageDao.getMessages(remoteId).map { entity ->
            ChatMessage(
                remoteId = entity.remoteId,
                author = if (entity.sender == "user") MessageAuthor.USER else MessageAuthor.ASSISTANT,
                content = entity.content,
                sentAt = Instant.ofEpochMilli(entity.sentAt),
                isPending = entity.pending
            )
        }

        if (!networkMonitor.isCurrentlyOnline()) {
            if (cachedMessages.isNotEmpty()) {
                return@withContext ConversationSnapshot(
                    remoteId = remoteId,
                    messages = cachedMessages,
                    crisisResources = emptyList()
                )
            }
        }

        try {
            val remoteSnapshot = remoteGateway.openConversation(remoteId)
            // Sync remote messages into Room
            val messageEntities = remoteSnapshot.messages.map { msg ->
                MessageEntity(
                    remoteId = msg.remoteId,
                    conversationRemoteId = remoteId,
                    sender = if (msg.author == MessageAuthor.USER) "user" else "assistant",
                    content = msg.content,
                    sentAt = msg.sentAt.toEpochMilli(),
                    pending = msg.isPending
                )
            }
            messageDao.syncRemoteMessages(remoteId, messageEntities)
            remoteSnapshot
        } catch (e: Exception) {
            Log.w(tag, "Failed to open conversation from remote, falling back to cache", e)
            if (cachedMessages.isNotEmpty()) {
                ConversationSnapshot(
                    remoteId = remoteId,
                    messages = cachedMessages,
                    crisisResources = emptyList()
                )
            } else {
                throw AssistantUnavailableException(e)
            }
        }
    }

    override suspend fun renameConversation(remoteId: String, title: String): ConversationSummary = withContext(Dispatchers.IO) {
        val oldEntity = conversationDao.getById(remoteId)
        // Optimistic update in Room immediately
        conversationDao.updateTitle(remoteId, title)

        if (networkMonitor.isCurrentlyOnline()) {
            try {
                remoteGateway.renameConversation(remoteId, title)
            } catch (e: Exception) {
                Log.w(tag, "Failed to rename on remote, reverting local change", e)
                oldEntity?.let { conversationDao.upsert(it) }
                throw AssistantUnavailableException(e)
            }
        } else {
            ConversationSummary(
                remoteId = remoteId,
                title = title,
                lastActivityAt = Instant.ofEpochMilli(oldEntity?.lastActivityAt ?: System.currentTimeMillis())
            )
        }
    }

    override suspend fun deleteConversation(remoteId: String) = withContext(Dispatchers.IO) {
        val oldEntity = conversationDao.getById(remoteId)
        val oldMessages = messageDao.getMessages(remoteId)
        // Optimistic delete in Room immediately
        conversationDao.deleteById(remoteId)
        messageDao.deleteByConversation(remoteId)

        if (networkMonitor.isCurrentlyOnline()) {
            try {
                remoteGateway.deleteConversation(remoteId)
            } catch (e: Exception) {
                Log.w(tag, "Failed to delete on remote, reverting local change", e)
                oldEntity?.let { conversationDao.upsert(it) }
                messageDao.insertAll(oldMessages)
                throw AssistantUnavailableException(e)
            }
        }
    }
}
