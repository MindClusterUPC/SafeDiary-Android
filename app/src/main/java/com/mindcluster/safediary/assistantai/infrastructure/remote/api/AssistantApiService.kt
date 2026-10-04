package com.mindcluster.safediary.assistantai.infrastructure.remote.api

import com.mindcluster.safediary.assistantai.infrastructure.remote.dto.AssistantResponseDto
import com.mindcluster.safediary.assistantai.infrastructure.remote.dto.PromptRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface AssistantApiService {

    @POST("api/v1/assistant/chat")
    suspend fun sendPrompt(@Body request: PromptRequestDto): AssistantResponseDto

    @PATCH("api/v1/conversation-sessions/{sessionId}/close")
    suspend fun closeSession(@Path("sessionId") sessionId: String): Response<Unit>
}
