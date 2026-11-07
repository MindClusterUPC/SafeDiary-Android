package com.mindcluster.safediary.iam.infrastructure.remote

import android.util.Log
import com.google.gson.JsonParser
import com.mindcluster.safediary.iam.infrastructure.remote.api.IamApiService
import com.mindcluster.safediary.iam.infrastructure.remote.dto.SignInRequestDto
import com.mindcluster.safediary.iam.infrastructure.remote.dto.SignInResponseDto
import com.mindcluster.safediary.iam.infrastructure.remote.dto.SignUpRequestDto
import com.mindcluster.safediary.shared.infrastructure.network.ApiEndpoints
import com.mindcluster.safediary.shared.infrastructure.network.RetrofitClientProvider
import java.io.IOException

class RetrofitIamGateway(
    private val apiService: IamApiService = RetrofitClientProvider.createService()
) {
    private val tag = "RetrofitIamGateway"

    suspend fun signIn(email: String, password: String): SignInResponseDto? = try {
        val response = apiService.signIn(ApiEndpoints.authSignIn, SignInRequestDto(email, password))
        if (response.isSuccessful) {
            response.body()
        } else if (response.code() in 400..499) {
            val errorMsg = extractErrorMessage(response.errorBody()?.string(), response.code())
            throw IllegalArgumentException(errorMsg)
        } else {
            Log.w(tag, "signIn returned HTTP ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: IllegalArgumentException) {
        throw e
    } catch (e: IOException) {
        Log.w(tag, "signIn network exception: ${e.message}")
        null
    } catch (e: Exception) {
        Log.w(tag, "signIn unexpected exception: ${e.message}")
        null
    }

    suspend fun signUp(email: String, password: String, role: String): SignInResponseDto? = try {
        val response = apiService.signUp(ApiEndpoints.authSignUp, SignUpRequestDto(email, password, role))
        if (response.isSuccessful) {
            response.body()
        } else if (response.code() in 400..499) {
            val errorMsg = extractErrorMessage(response.errorBody()?.string(), response.code())
            throw IllegalArgumentException(errorMsg)
        } else {
            Log.w(tag, "signUp returned HTTP ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: IllegalArgumentException) {
        throw e
    } catch (e: IOException) {
        Log.w(tag, "signUp network exception: ${e.message}")
        null
    } catch (e: Exception) {
        Log.w(tag, "signUp unexpected exception: ${e.message}")
        null
    }

    private fun extractErrorMessage(rawErrorBody: String?, statusCode: Int): String {
        if (!rawErrorBody.isNullOrBlank()) {
            try {
                val json = JsonParser.parseString(rawErrorBody).asJsonObject
                val msg = json.get("message")?.asString ?: json.get("detail")?.asString
                if (!msg.isNullOrBlank()) return msg
            } catch (_: Exception) {
                // Ignore JSON parse failure
            }
        }
        return when (statusCode) {
            401 -> "Correo o contraseña incorrectos"
            409 -> "Ya existe una cuenta registrada con este correo electrónico"
            else -> "Error en la solicitud (HTTP $statusCode)"
        }
    }
}
