package com.mindcluster.safediary.iam.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName

data class SignInRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class SignUpRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("role") val role: String = "PATIENT"
)

data class SignInResponseDto(
    @SerializedName("id") val id: Long?,
    @SerializedName("accountId") val accountId: Long?,
    @SerializedName("email") val email: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("token") val token: String?,
    @SerializedName("accessToken") val accessToken: String?,
    @SerializedName("roles") val roles: List<String>?
) {
    val resolvedAccountId: Long
        get() = accountId ?: id ?: 1L

    val resolvedToken: String
        get() = accessToken ?: token ?: "mock_jwt_token_safe_diary"

    val resolvedRole: String
        get() = role ?: roles?.firstOrNull() ?: "PATIENT"
}
