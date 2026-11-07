package com.mindcluster.safediary.iam.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName

data class SignInRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class SignInResponseDto(
    @SerializedName("id") val id: Long?,
    @SerializedName("accountId") val accountId: Long?,
    @SerializedName("email") val email: String?,
    @SerializedName("token") val token: String?,
    @SerializedName("accessToken") val accessToken: String?,
    @SerializedName("roles") val roles: List<String>?
) {
    val resolvedAccountId: Long
        get() = id ?: accountId ?: 1L

    val resolvedToken: String
        get() = token ?: accessToken ?: "mock_jwt_token_safe_diary"
}
