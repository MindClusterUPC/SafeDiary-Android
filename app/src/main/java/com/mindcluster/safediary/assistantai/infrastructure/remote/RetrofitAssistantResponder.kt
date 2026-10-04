package com.mindcluster.safediary.assistantai.infrastructure.remote

import android.content.Context
import android.util.Log
import com.mindcluster.safediary.assistantai.domain.model.AssistantReply
import com.mindcluster.safediary.assistantai.domain.model.AssistantResponder
import com.mindcluster.safediary.assistantai.domain.model.ChatMessage
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
            val request = PromptRequestDto(prompt = userPrompt, locale = locale)
            val response = apiService.sendPrompt(request)
            AssistantReply(content = response.reply)
        } catch (e: Exception) {
            Log.w(tag, "Remote API call failed or server unreachable (${e.javaClass.simpleName}). Resilient fallback activated.")
            fallbackResponder.respond(userPrompt, history, remoteConversationId)
        }
    }
}
