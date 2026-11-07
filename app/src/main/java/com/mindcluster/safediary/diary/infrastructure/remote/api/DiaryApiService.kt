package com.mindcluster.safediary.diary.infrastructure.remote.api

import com.mindcluster.safediary.diary.infrastructure.remote.dto.CreateDiaryEntryRequest
import com.mindcluster.safediary.diary.infrastructure.remote.dto.DiaryEntryDto
import com.mindcluster.safediary.diary.infrastructure.remote.dto.UpdateDiaryEntryRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface DiaryApiService {

    @GET("api/v1/diary-entries")
    suspend fun listEntries(
        @Query("patientId") patientId: Long? = null
    ): Response<List<DiaryEntryDto>>

    @GET("api/v1/diary-entries/{id}")
    suspend fun getEntryById(
        @Path("id") id: Long
    ): Response<DiaryEntryDto>

    @POST("api/v1/diary-entries")
    suspend fun createEntry(
        @Body request: CreateDiaryEntryRequest
    ): Response<DiaryEntryDto>

    @PUT("api/v1/diary-entries/{id}")
    suspend fun updateEntry(
        @Path("id") id: Long,
        @Body request: UpdateDiaryEntryRequest
    ): Response<DiaryEntryDto>

    @DELETE("api/v1/diary-entries/{id}")
    suspend fun deleteEntry(
        @Path("id") id: Long
    ): Response<Unit>
}
