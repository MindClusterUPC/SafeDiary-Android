package com.mindcluster.safediary.rutines.infrastructure.remote.api

import com.mindcluster.safediary.rutines.infrastructure.remote.dto.CreateDailyRoutineRequestDto
import com.mindcluster.safediary.rutines.infrastructure.remote.dto.CreatePromptReflectionRequestDto
import com.mindcluster.safediary.rutines.infrastructure.remote.dto.DailyRoutineResponseDto
import com.mindcluster.safediary.rutines.infrastructure.remote.dto.PromptReflectionResponseDto
import com.mindcluster.safediary.rutines.infrastructure.remote.dto.SosExerciseResponseDto
import com.mindcluster.safediary.rutines.infrastructure.remote.dto.StartSosExerciseRequestDto
import com.mindcluster.safediary.rutines.infrastructure.remote.dto.SubmitDailyReflectionRequestDto
import com.mindcluster.safediary.rutines.infrastructure.remote.dto.ToggleRoutineNotificationRequestDto
import com.mindcluster.safediary.rutines.infrastructure.remote.dto.UpdateDailyRoutineRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RutinesApiService {

    @POST("api/v1/daily-routines")
    suspend fun createDailyRoutine(
        @Body request: CreateDailyRoutineRequestDto
    ): Response<DailyRoutineResponseDto>

    @GET("api/v1/daily-routines/{id}")
    suspend fun getDailyRoutineById(
        @Path("id") id: Long
    ): Response<DailyRoutineResponseDto>

    @GET("api/v1/daily-routines/patient/{patientId}")
    suspend fun getAllRoutinesByPatientId(
        @Path("patientId") patientId: Long
    ): Response<List<DailyRoutineResponseDto>>

    @PUT("api/v1/daily-routines/{id}")
    suspend fun updateDailyRoutine(
        @Path("id") id: Long,
        @Body request: UpdateDailyRoutineRequestDto
    ): Response<DailyRoutineResponseDto>

    @PATCH("api/v1/daily-routines/{id}/notification")
    suspend fun toggleRoutineNotification(
        @Path("id") id: Long,
        @Body request: ToggleRoutineNotificationRequestDto
    ): Response<DailyRoutineResponseDto>

    @PATCH("api/v1/daily-routines/{id}/toggle-active")
    suspend fun toggleRoutineActive(
        @Path("id") id: Long
    ): Response<DailyRoutineResponseDto>

    @POST("api/v1/prompt-reflections")
    suspend fun createPromptReflection(
        @Body request: CreatePromptReflectionRequestDto
    ): Response<PromptReflectionResponseDto>

    @GET("api/v1/prompt-reflections/{id}")
    suspend fun getPromptReflectionById(
        @Path("id") id: Long
    ): Response<PromptReflectionResponseDto>

    @GET("api/v1/prompt-reflections/patient/{patientId}/pending")
    suspend fun getPendingPromptReflection(
        @Path("patientId") patientId: Long
    ): Response<PromptReflectionResponseDto>

    @PATCH("api/v1/prompt-reflections/{id}/submit")
    suspend fun submitDailyReflection(
        @Path("id") id: Long,
        @Body request: SubmitDailyReflectionRequestDto
    ): Response<PromptReflectionResponseDto>

    @POST("api/v1/sos-exercises")
    suspend fun startSosExercise(
        @Body request: StartSosExerciseRequestDto
    ): Response<SosExerciseResponseDto>

    @GET("api/v1/sos-exercises/{id}")
    suspend fun getSosExerciseById(
        @Path("id") id: Long
    ): Response<SosExerciseResponseDto>

    @GET("api/v1/sos-exercises/patient/{patientId}")
    suspend fun getSosExerciseHistory(
        @Path("patientId") patientId: Long
    ): Response<List<SosExerciseResponseDto>>

    @PATCH("api/v1/sos-exercises/{id}/advance")
    suspend fun advanceSosStep(
        @Path("id") id: Long
    ): Response<SosExerciseResponseDto>

    @PATCH("api/v1/sos-exercises/{id}/complete")
    suspend fun completeSosExercise(
        @Path("id") id: Long
    ): Response<SosExerciseResponseDto>
}
