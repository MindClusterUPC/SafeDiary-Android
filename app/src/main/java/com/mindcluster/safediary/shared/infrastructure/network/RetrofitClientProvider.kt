package com.mindcluster.safediary.shared.infrastructure.network

import com.mindcluster.safediary.BuildConfig
import com.mindcluster.safediary.shared.infrastructure.security.AuthSessionHolder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClientProvider {

    // Debug builds point to the local backend (10.0.2.2:8080 from the emulator); release builds to the cloud API.
    private val baseUrl: String = BuildConfig.API_BASE_URL

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            // Credentials, tokens and emotional messages must never appear in network logs.
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            // AI replies can take a while when the model is busy, and the free Render instance
            // needs up to ~2.5 minutes to wake up after being idle.
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("User-Agent", "SafeDiary-Android/1.0.0")

                val token = AuthSessionHolder.token
                if (!token.isNullOrBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }

                val response = chain.proceed(requestBuilder.build())
                if (response.code == 401 &&
                    !response.request.url.encodedPath.startsWith("/api/v1/authentication/")
                ) {
                    AuthSessionHolder.rejectToken(token)
                }
                response
            }
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val retrofitInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun <T> createService(serviceClass: Class<T>): T {
        return retrofitInstance.create(serviceClass)
    }

    inline fun <reified T> createService(): T {
        return createService(T::class.java)
    }

    fun <T> createSessionService(token: String?, serviceClass: Class<T>): T = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient.newBuilder().addInterceptor(SessionBoundaryInterceptor(token)).build())
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(serviceClass)

    inline fun <reified T> createSessionService(token: String?): T = createSessionService(token, T::class.java)
}
