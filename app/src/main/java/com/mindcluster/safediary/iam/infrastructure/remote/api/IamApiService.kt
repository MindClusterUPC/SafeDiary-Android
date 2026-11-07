package com.mindcluster.safediary.iam.infrastructure.remote.api

import com.mindcluster.safediary.iam.infrastructure.remote.dto.SignInRequestDto
import com.mindcluster.safediary.iam.infrastructure.remote.dto.SignInResponseDto
import com.mindcluster.safediary.iam.infrastructure.remote.dto.SignUpRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface IamApiService {
    @POST
    suspend fun signIn(
        @Url url: String,
        @Body request: SignInRequestDto
    ): Response<SignInResponseDto>

    @POST
    suspend fun signUp(
        @Url url: String,
        @Body request: SignUpRequestDto
    ): Response<SignInResponseDto>
}
