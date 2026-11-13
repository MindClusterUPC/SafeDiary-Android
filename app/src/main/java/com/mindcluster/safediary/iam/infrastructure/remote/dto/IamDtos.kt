package com.mindcluster.safediary.iam.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName
import com.mindcluster.safediary.shared.infrastructure.security.SessionTokenPolicy

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
        get() = sequenceOf(accountId, id).filterNotNull().firstOrNull { it > 0 }
            ?: error("El servidor no devolvió una cuenta válida. Vuelve a iniciar sesión.")

    val resolvedToken: String
        get() = sequenceOf(accessToken, token).filterNotNull()
            .map(String::trim).firstOrNull(SessionTokenPolicy::isUsable)
            ?: error("El servidor no devolvió una sesión válida. Vuelve a iniciar sesión.")

    val resolvedRole: String
        get() = role ?: roles?.firstOrNull() ?: "PATIENT"
}
