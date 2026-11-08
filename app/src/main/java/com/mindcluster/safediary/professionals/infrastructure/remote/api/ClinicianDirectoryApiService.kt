package com.mindcluster.safediary.professionals.infrastructure.remote.api

import com.mindcluster.safediary.professionals.infrastructure.remote.dto.ClinicianProfileDto
import com.mindcluster.safediary.professionals.infrastructure.remote.dto.RatingSummaryDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Public Clinician Directory endpoints: only verified and published profiles are returned. */
interface ClinicianDirectoryApiService {

    @GET("api/v1/clinicians")
    suspend fun searchClinicians(
        @Query("size") size: Int = 50
    ): Response<List<ClinicianProfileDto>>

    @GET("api/v1/clinicians/{id}/rating")
    suspend fun getRating(
        @Path("id") id: Long
    ): Response<RatingSummaryDto>
}
