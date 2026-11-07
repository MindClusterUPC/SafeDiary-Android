package com.mindcluster.safediary.profiles.infrastructure.remote.api

import com.mindcluster.safediary.profiles.infrastructure.remote.dto.PatientResponseDto
import com.mindcluster.safediary.profiles.infrastructure.remote.dto.UpdateEmergencyContactRequestDto
import com.mindcluster.safediary.profiles.infrastructure.remote.dto.UpdatePatientEmailRequestDto
import com.mindcluster.safediary.profiles.infrastructure.remote.dto.UpdatePatientProfileRequestDto
import retrofit2.Response
import retrofit2.http.*

interface ProfilesApiService {
    @GET
    suspend fun getPatientById(
        @Url url: String
    ): Response<PatientResponseDto>

    @PUT
    suspend fun updateEmergencyContact(
        @Url url: String,
        @Body request: UpdateEmergencyContactRequestDto
    ): Response<PatientResponseDto>

    @DELETE
    suspend fun deleteEmergencyContact(
        @Url url: String
    ): Response<PatientResponseDto>

    @PUT
    suspend fun updatePatientProfile(
        @Url url: String,
        @Body request: UpdatePatientProfileRequestDto
    ): Response<PatientResponseDto>

    @PUT
    suspend fun updatePatientEmail(
        @Url url: String,
        @Body request: UpdatePatientEmailRequestDto
    ): Response<PatientResponseDto>
}
