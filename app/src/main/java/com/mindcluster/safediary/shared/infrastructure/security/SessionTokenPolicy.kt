package com.mindcluster.safediary.shared.infrastructure.security

/** IAM returns signed JWTs. This checks the local format; the server verifies the signature. */
object SessionTokenPolicy {
    fun isUsable(token: String?): Boolean {
        if (token.isNullOrBlank() || token.startsWith("mock_jwt_token")) return false
        val segments = token.split('.')
        return segments.size == 3 && segments.all { it.isNotBlank() }
    }
}
