package com.mindcluster.safediary.shared.infrastructure.network

import com.mindcluster.safediary.shared.infrastructure.security.AuthSessionHolder
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class SessionBoundaryInterceptor(private val token: String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (token.isNullOrBlank() || token != AuthSessionHolder.token) throw IOException("Account session changed")
        val request = chain.request().newBuilder().header("Authorization", "Bearer $token").build()
        val response = chain.proceed(request)
        if (token != AuthSessionHolder.token) {
            response.close()
            throw IOException("Account session changed")
        }
        return response
    }
}
