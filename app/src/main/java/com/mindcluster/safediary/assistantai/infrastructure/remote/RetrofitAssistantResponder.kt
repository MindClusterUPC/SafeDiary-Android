package com.mindcluster.safediary.assistantai.infrastructure.remote

import android.content.Context
import android.util.Log
import com.mindcluster.safediary.assistantai.domain.model.AssistantReply
import com.mindcluster.safediary.assistantai.domain.model.AssistantResponder
import com.mindcluster.safediary.assistantai.domain.model.ChatMessage
import com.mindcluster.safediary.assistantai.domain.model.CrisisResource
import com.mindcluster.safediary.assistantai.domain.model.RiskLevel
import com.mindcluster.safediary.assistantai.infrastructure.MockAssistantResponder
import com.mindcluster.safediary.assistantai.infrastructure.remote.api.AssistantApiService
import com.mindcluster.safediary.assistantai.infrastructure.remote.dto.PromptRequestDto
import com.mindcluster.safediary.shared.infrastructure.locale.AppLocaleManager
import com.mindcluster.safediary.shared.infrastructure.network.RetrofitClientProvider

class RetrofitAssistantResponder(
    private val context: Context,
    private val apiService: AssistantApiService = RetrofitClientProvider.createService(),
    private val fallbackResponder: AssistantResponder = MockAssistantResponder(context)
) : AssistantResponder {

    private val tag = "RetrofitAssistant"

    override suspend fun respond(
        userPrompt: String,
        history: List<ChatMessage>,
        remoteConversationId: String?
    ): AssistantReply {
        return try {
            val locale = AppLocaleManager.current(context).code
            val request = PromptRequestDto(
                prompt = userPrompt,
                conversationId = remoteConversationId,
                locale = locale
            )
            val response = apiService.sendPrompt(request)
            AssistantReply(
                content = response.reply,
                remoteConversationId = response.conversationId,
                riskLevel = RiskLevel.fromBackend(response.riskLevel),
                crisisResources = response.crisisResources.orEmpty().map {
                    CrisisResource(name = it.name, phone = it.phone, description = it.description.orEmpty())
                }
            )
        } catch (e: Exception) {
            Log.w(tag, "Remote API call failed or server unreachable (${e.javaClass.simpleName}). Resilient fallback activated.")
            fallbackResponder.respond(userPrompt, history, remoteConversationId)
        }
    }

    override suspend fun endConversation(remoteConversationId: String) {
        try {
            apiService.closeSession(remoteConversationId)
        } catch (e: Exception) {
            Log.w(tag, "Could not close remote conversation (${e.javaClass.simpleName}).")
        }
    }
}
