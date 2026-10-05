package com.mindcluster.safediary.assistantai.infrastructure.remote

import android.content.Context
import android.util.Log
import com.mindcluster.safediary.assistantai.domain.model.AssistantReply
import com.mindcluster.safediary.assistantai.domain.model.AssistantResponder
import com.mindcluster.safediary.assistantai.domain.model.AssistantUnavailableException
import com.mindcluster.safediary.assistantai.domain.model.ChatMessage
import com.mindcluster.safediary.assistantai.domain.model.CrisisResource
import com.mindcluster.safediary.assistantai.domain.model.RiskLevel
import com.mindcluster.safediary.assistantai.infrastructure.local.PersonalityPreferences
import com.mindcluster.safediary.assistantai.infrastructure.remote.api.AssistantApiService
import com.mindcluster.safediary.assistantai.infrastructure.remote.dto.AssistantResponseDto
import com.mindcluster.safediary.assistantai.infrastructure.remote.dto.EditMessageRequestDto
import com.mindcluster.safediary.assistantai.infrastructure.remote.dto.RegenerateRequestDto
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
            val locale = currentLocale()
            val personality = currentPersonality()
            val request = PromptRequestDto(
                prompt = userPrompt,
                conversationId = remoteConversationId,
                locale = locale,
                personality = personality
            )
            val response = apiService.sendPrompt(request)
            return mapResponse(response)
        } catch (e: Exception) {
            Log.w(tag, "Assistant request failed (${e.javaClass.simpleName}).")
            throw AssistantUnavailableException(e)
        }
    }

    override suspend fun editMessage(
        remoteConversationId: String,
        messageRemoteId: Long,
        newPrompt: String
    ): AssistantReply {
        try {
            val locale = currentLocale()
            val personality = currentPersonality()
            val request = EditMessageRequestDto(
                prompt = newPrompt,
                locale = locale,
                personality = personality
            )
            val response = apiService.editMessage(remoteConversationId, messageRemoteId, request)
            return mapResponse(response)
        } catch (e: Exception) {
            Log.w(tag, "Edit message request failed (${e.javaClass.simpleName}).")
            throw AssistantUnavailableException(e)
        }
    }

    override suspend fun regenerate(
        remoteConversationId: String
    ): AssistantReply {
        try {
            val locale = currentLocale()
            val personality = currentPersonality()
            val request = RegenerateRequestDto(
                locale = locale,
                personality = personality
            )
            val response = apiService.regenerateReply(remoteConversationId, request)
            return mapResponse(response)
        } catch (e: Exception) {
            Log.w(tag, "Regenerate request failed (${e.javaClass.simpleName}).")
            throw AssistantUnavailableException(e)
        }
    }

    private fun currentLocale(): String =
        AppLanguage.fromCode(context.resources.configuration.locales[0].language).code

    private fun currentPersonality(): String =
        PersonalityPreferences(context).get().apiName

    private fun mapResponse(response: AssistantResponseDto): AssistantReply =
        AssistantReply(
            content = response.reply,
            remoteConversationId = response.conversationId,
            riskLevel = RiskLevel.fromBackend(response.riskLevel),
            crisisResources = response.crisisResources.orEmpty().map {
                CrisisResource(name = it.name, phone = it.phone, description = it.description.orEmpty())
            }
        )
}
