package com.mindcluster.safediary.assistantai.infrastructure.remote.api

import com.mindcluster.safediary.assistantai.infrastructure.remote.dto.AssistantResponseDto
import com.mindcluster.safediary.assistantai.infrastructure.remote.dto.PromptRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AssistantApiService {

    @POST("api/v1/assistant/chat")
    suspend fun sendPrompt(@Body request: PromptRequestDto): AssistantResponseDto
}
