package com.mindcluster.safediary.assistantai.infrastructure.remote.api

import com.mindcluster.safediary.assistantai.infrastructure.remote.dto.AssistantResponseDto
import com.mindcluster.safediary.assistantai.infrastructure.remote.dto.ConversationDto
import com.mindcluster.safediary.assistantai.infrastructure.remote.dto.ConversationSummaryDto
import com.mindcluster.safediary.assistantai.infrastructure.remote.dto.PromptRequestDto
import com.mindcluster.safediary.assistantai.infrastructure.remote.dto.EditMessageRequestDto
import com.mindcluster.safediary.assistantai.infrastructure.remote.dto.RegenerateRequestDto
import com.mindcluster.safediary.assistantai.infrastructure.remote.dto.RenameConversationRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AssistantApiService {

    @POST("api/v1/assistant/chat")
    suspend fun sendPrompt(@Body request: PromptRequestDto): AssistantResponseDto

    @GET("api/v1/assistant/conversations")
    suspend fun listConversations(): List<ConversationSummaryDto>

    @GET("api/v1/assistant/conversations/{conversationId}")
    suspend fun getConversation(@Path("conversationId") conversationId: String): ConversationDto

    @PATCH("api/v1/assistant/conversations/{conversationId}")
    suspend fun renameConversation(
        @Path("conversationId") conversationId: String,
        @Body request: RenameConversationRequestDto
    ): ConversationSummaryDto

    @DELETE("api/v1/assistant/conversations/{conversationId}")
    suspend fun deleteConversation(@Path("conversationId") conversationId: String)

    @PUT("api/v1/assistant/conversations/{conversationId}/messages/{messageId}")
    suspend fun editMessage(
        @Path("conversationId") conversationId: String,
        @Path("messageId") messageId: Long,
        @Body request: EditMessageRequestDto
    ): AssistantResponseDto

    @POST("api/v1/assistant/conversations/{conversationId}/regenerate")
    suspend fun regenerateReply(
        @Path("conversationId") conversationId: String,
        @Body request: RegenerateRequestDto
    ): AssistantResponseDto
}
