package com.mindcluster.safediary.assistantai.infrastructure.remote

import android.util.Log
import com.mindcluster.safediary.assistantai.domain.model.AssistantUnavailableException
import com.mindcluster.safediary.assistantai.domain.model.ChatMessage
import com.mindcluster.safediary.assistantai.domain.model.ConversationSnapshot
import com.mindcluster.safediary.assistantai.domain.model.ConversationSummary
import com.mindcluster.safediary.assistantai.domain.model.CrisisResource
import com.mindcluster.safediary.assistantai.domain.model.MessageAuthor
import com.mindcluster.safediary.assistantai.domain.repository.ConversationHistoryGateway
import com.mindcluster.safediary.assistantai.infrastructure.remote.api.AssistantApiService
import com.mindcluster.safediary.assistantai.infrastructure.remote.dto.RenameConversationRequestDto
import com.mindcluster.safediary.shared.infrastructure.network.RetrofitClientProvider
import java.time.Instant

class RetrofitConversationHistoryGateway(
    private val apiService: AssistantApiService = RetrofitClientProvider.createService()
) : ConversationHistoryGateway {

    private val tag = "ConversationHistory"

    override suspend fun listConversations(): List<ConversationSummary> = remote {
        apiService.listConversations().map {
            ConversationSummary(
                remoteId = it.conversationId,
                title = it.title,
                lastActivityAt = parseInstant(it.lastMessageAt ?: it.startedAt)
            )
        }
    }

    override suspend fun openConversation(remoteId: String): ConversationSnapshot = remote {
        val conversation = apiService.getConversation(remoteId)
        ConversationSnapshot(
            remoteId = conversation.conversationId,
            messages = conversation.messages.map {
                ChatMessage(
                    remoteId = it.id,
                    author = if (it.role == "user") MessageAuthor.USER else MessageAuthor.ASSISTANT,
                    content = it.content,
                    sentAt = parseInstant(it.sentAt)
                )
            },
            crisisResources = conversation.crisisResources.orEmpty().map {
                CrisisResource(name = it.name, phone = it.phone, description = it.description.orEmpty())
            }
        )
    }

    override suspend fun renameConversation(remoteId: String, title: String): ConversationSummary = remote {
        val dto = apiService.renameConversation(remoteId, RenameConversationRequestDto(title))
        ConversationSummary(
            remoteId = dto.conversationId,
            title = dto.title,
            lastActivityAt = parseInstant(dto.lastMessageAt ?: dto.startedAt)
        )
    }

    override suspend fun deleteConversation(remoteId: String): Unit = remote {
        apiService.deleteConversation(remoteId)
    }

    private suspend fun <T> remote(block: suspend () -> T): T = try {
        block()
    } catch (e: Exception) {
        Log.w(tag, "Conversation history request failed (${e.javaClass.simpleName}).")
        throw AssistantUnavailableException(e)
    }

    private fun parseInstant(value: String?): Instant =
        value?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: Instant.now()
}
