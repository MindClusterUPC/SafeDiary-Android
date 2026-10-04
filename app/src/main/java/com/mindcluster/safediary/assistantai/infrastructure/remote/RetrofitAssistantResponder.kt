package com.mindcluster.safediary.assistantai.infrastructure.remote

import android.content.Context
import android.util.Log
import com.mindcluster.safediary.assistantai.domain.model.AssistantReply
import com.mindcluster.safediary.assistantai.domain.model.AssistantResponder
import com.mindcluster.safediary.assistantai.domain.model.AssistantUnavailableException
import com.mindcluster.safediary.assistantai.domain.model.ChatMessage
import com.mindcluster.safediary.assistantai.domain.model.CrisisResource
import com.mindcluster.safediary.assistantai.domain.model.RiskLevel
import com.mindcluster.safediary.assistantai.infrastructure.remote.api.AssistantApiService
import com.mindcluster.safediary.assistantai.infrastructure.remote.dto.PromptRequestDto
import com.mindcluster.safediary.shared.infrastructure.locale.AppLanguage
import com.mindcluster.safediary.shared.infrastructure.network.RetrofitClientProvider

class RetrofitAssistantResponder(
    private val context: Context,
    private val apiService: AssistantApiService = RetrofitClientProvider.createService()
) : AssistantResponder {

    private val tag = "RetrofitAssistant"

    override suspend fun respond(
        userPrompt: String,
        history: List<ChatMessage>,
        remoteConversationId: String?
    ): AssistantReply {
        try {
            // Answer in the language the UI is actually displayed in.
            val locale = AppLanguage.fromCode(context.resources.configuration.locales[0].language).code
            val request = PromptRequestDto(
                prompt = userPrompt,
                conversationId = remoteConversationId,
                locale = locale
            )
            val response = apiService.sendPrompt(request)
            return AssistantReply(
                content = response.reply,
                remoteConversationId = response.conversationId,
                riskLevel = RiskLevel.fromBackend(response.riskLevel),
                crisisResources = response.crisisResources.orEmpty().map {
                    CrisisResource(name = it.name, phone = it.phone, description = it.description.orEmpty())
                }
            )
        } catch (e: Exception) {
            Log.w(tag, "Assistant request failed (${e.javaClass.simpleName}).")
            throw AssistantUnavailableException(e)
        }
    }
}
