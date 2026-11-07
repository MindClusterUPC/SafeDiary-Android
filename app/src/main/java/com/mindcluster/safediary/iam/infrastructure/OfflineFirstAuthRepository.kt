package com.mindcluster.safediary.iam.infrastructure

import com.mindcluster.safediary.iam.domain.model.UserSession
import com.mindcluster.safediary.iam.domain.repository.AuthRepository
import com.mindcluster.safediary.iam.infrastructure.remote.RetrofitIamGateway
import com.mindcluster.safediary.profiles.infrastructure.local.ProfilePreferences
import com.mindcluster.safediary.profiles.infrastructure.remote.RetrofitPatientProfileGateway
import com.mindcluster.safediary.shared.infrastructure.network.NetworkMonitor
import com.mindcluster.safediary.shared.infrastructure.security.AuthSessionPreferences

class OfflineFirstAuthRepository(
    private val authSessionPreferences: AuthSessionPreferences,
    private val profilePreferences: ProfilePreferences,
    private val remoteGateway: RetrofitIamGateway,
    private val networkMonitor: NetworkMonitor,
    private val profileGateway: RetrofitPatientProfileGateway = RetrofitPatientProfileGateway()
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): UserSession {
        var accountId = 1L
        var token = "mock_jwt_token_${System.currentTimeMillis()}"
        var resolvedRole = "PATIENT"

        if (networkMonitor.isOnline.value) {
            val response = remoteGateway.signIn(email, password)
            if (response != null) {
                accountId = response.resolvedAccountId
                token = response.resolvedToken
                resolvedRole = response.resolvedRole
            }
        }

        // Save session state
        authSessionPreferences.authToken = token
        authSessionPreferences.patientAccountId = accountId
        authSessionPreferences.userEmail = email
        authSessionPreferences.isLoggedIn = true

        // Also update local profile email if different
        profilePreferences.email = email

        return UserSession(
            accountId = accountId,
            email = email,
            token = token,
            role = resolvedRole
        )
    }

    override suspend fun signUp(
        fullName: String,
        email: String,
        password: String,
        role: String
    ): UserSession {
        var accountId = 1L
        var token = "mock_jwt_token_${System.currentTimeMillis()}"
        var resolvedRole = role

        if (networkMonitor.isOnline.value) {
            val response = remoteGateway.signUp(email, password, role)
            if (response != null) {
                accountId = response.resolvedAccountId
                token = response.resolvedToken
                resolvedRole = response.resolvedRole
            }
        }

        // Save session state
        authSessionPreferences.authToken = token
        authSessionPreferences.patientAccountId = accountId
        authSessionPreferences.userEmail = email
        authSessionPreferences.isLoggedIn = true

        // Parse fullName into firstName and lastName
        val nameParts = fullName.trim().split(Regex("\\s+"), limit = 2)
        val firstName = nameParts.getOrNull(0).orEmpty().ifBlank { "Usuario" }
        val lastName = nameParts.getOrNull(1).orEmpty().ifBlank { "SafeDiary" }

        profilePreferences.firstName = firstName
        profilePreferences.lastName = lastName
        profilePreferences.email = email

        if (networkMonitor.isOnline.value && resolvedRole.equals("PATIENT", ignoreCase = true)) {
            runCatching {
                profileGateway.updatePatientProfile(
                    patientId = accountId,
                    firstName = firstName,
                    lastName = lastName
                )
            }
        }

        return UserSession(
            accountId = accountId,
            email = email,
            token = token,
            role = resolvedRole
        )
    }

    override suspend fun signOut() {
        authSessionPreferences.clear()
        authSessionPreferences.isLoggedIn = false
    }

    override fun isLoggedIn(): Boolean = authSessionPreferences.isLoggedIn

    override fun getStoredSession(): UserSession? {
        if (!authSessionPreferences.isLoggedIn) return null
        val token = authSessionPreferences.authToken ?: return null
        return UserSession(
            accountId = authSessionPreferences.patientAccountId,
            email = authSessionPreferences.userEmail.orEmpty(),
            token = token,
            role = "PATIENT"
        )
    }
}
