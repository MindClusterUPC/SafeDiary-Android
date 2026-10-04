package com.mindcluster.safediary.assistantai.infrastructure

import android.content.Context
import com.mindcluster.safediary.R
import com.mindcluster.safediary.assistantai.domain.model.AssistantReply
import com.mindcluster.safediary.assistantai.domain.model.AssistantResponder
import com.mindcluster.safediary.assistantai.domain.model.ChatMessage
import kotlinx.coroutines.delay

class MockAssistantResponder(
    private val context: Context,
    private val delayMillis: Long = 1200L
) : AssistantResponder {

    private var anxietyIndex = 0
    private var sadnessIndex = 0
    private var sleepIndex = 0
    private var genericIndex = 0

    override suspend fun respond(
        userPrompt: String,
        history: List<ChatMessage>,
        remoteConversationId: String?
    ): AssistantReply {
        delay(delayMillis)
        val normalized = userPrompt.lowercase()

        val resId = when {
            normalized.contains("ansiedad") || normalized.contains("ansios") || normalized.contains("estrés") ||
                    normalized.contains("estres") || normalized.contains("anxiety") || normalized.contains("anxious") ||
                    normalized.contains("stress") || normalized.contains("overwhelm") || normalized.contains("pánico") -> {
                R.array.ai_responses_anxiety
            }
            normalized.contains("triste") || normalized.contains("tristeza") || normalized.contains("llor") ||
                    normalized.contains("deprim") || normalized.contains("sad") || normalized.contains("sadness") ||
                    normalized.contains("depressed") || normalized.contains("lonely") || normalized.contains("solo") -> {
                R.array.ai_responses_sadness
            }
            normalized.contains("dormir") || normalized.contains("sueño") || normalized.contains("insomnio") ||
                    normalized.contains("cansad") || normalized.contains("sleep") || normalized.contains("insomnia") ||
                    normalized.contains("tired") || normalized.contains("exhaust") -> {
                R.array.ai_responses_sleep
            }
            else -> {
                R.array.ai_responses_generic
            }
        }

        val responses = context.resources.getStringArray(resId)
        if (responses.isEmpty()) return AssistantReply(content = "")

        val index = when (resId) {
            R.array.ai_responses_anxiety -> (anxietyIndex++ % responses.size)
            R.array.ai_responses_sadness -> (sadnessIndex++ % responses.size)
            R.array.ai_responses_sleep -> (sleepIndex++ % responses.size)
            else -> (genericIndex++ % responses.size)
        }
        return AssistantReply(content = responses[index])
    }
}
