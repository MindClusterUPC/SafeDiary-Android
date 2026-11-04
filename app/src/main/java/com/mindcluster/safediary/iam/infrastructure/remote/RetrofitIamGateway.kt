package com.mindcluster.safediary.iam.infrastructure.remote

import android.util.Log
import com.mindcluster.safediary.iam.infrastructure.remote.api.IamApiService
import com.mindcluster.safediary.iam.infrastructure.remote.dto.SignInRequestDto
import com.mindcluster.safediary.iam.infrastructure.remote.dto.SignInResponseDto
import com.mindcluster.safediary.shared.infrastructure.network.ApiEndpoints
import com.mindcluster.safediary.shared.infrastructure.network.RetrofitClientProvider

class RetrofitIamGateway(
    private val apiService: IamApiService = RetrofitClientProvider.createService()
) {
    private val tag = "RetrofitIamGateway"

    suspend fun signIn(email: String, password: String): SignInResponseDto? = try {
        val response = apiService.signIn(ApiEndpoints.authSignIn, SignInRequestDto(email, password))
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.w(tag, "signIn returned HTTP ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: Exception) {
        Log.w(tag, "signIn network exception: ${e.message}")
        null
    }
}
