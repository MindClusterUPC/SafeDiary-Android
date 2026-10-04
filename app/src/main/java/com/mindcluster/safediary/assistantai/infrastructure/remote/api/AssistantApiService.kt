package com.mindcluster.safediary.assistantai.infrastructure.remote.api

import com.mindcluster.safediary.assistantai.infrastructure.remote.dto.AssistantResponseDto
import com.mindcluster.safediary.assistantai.infrastructure.remote.dto.ConversationDto
import com.mindcluster.safediary.assistantai.infrastructure.remote.dto.ConversationSummaryDto
import com.mindcluster.safediary.assistantai.infrastructure.remote.dto.PromptRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AssistantApiService {

    @POST("api/v1/assistant/chat")
    suspend fun sendPrompt(@Body request: PromptRequestDto): AssistantResponseDto

    @GET("api/v1/assistant/conversations")
    suspend fun listConversations(): List<ConversationSummaryDto>

    @GET("api/v1/assistant/conversations/{conversationId}")
    suspend fun getConversation(@Path("conversationId") conversationId: String): ConversationDto
}
