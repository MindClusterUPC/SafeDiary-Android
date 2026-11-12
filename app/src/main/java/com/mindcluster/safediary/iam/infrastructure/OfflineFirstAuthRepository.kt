package com.mindcluster.safediary.iam.infrastructure

import com.mindcluster.safediary.iam.domain.model.UserSession
import com.mindcluster.safediary.iam.domain.repository.AuthRepository
import com.mindcluster.safediary.iam.infrastructure.remote.RetrofitIamGateway
import com.mindcluster.safediary.iam.infrastructure.remote.dto.SignInResponseDto
import com.mindcluster.safediary.profiles.infrastructure.local.ProfilePreferences
import com.mindcluster.safediary.profiles.infrastructure.remote.RetrofitPatientProfileGateway
import com.mindcluster.safediary.shared.infrastructure.network.NetworkMonitor
import com.mindcluster.safediary.shared.infrastructure.security.AuthSessionPreferences

/**
 * Signs in against IAM. A session only exists with a real access token: without connection or with a server error
 * the attempt fails, so the app never falls back to another account's data.
 */
class OfflineFirstAuthRepository(
    private val authSessionPreferences: AuthSessionPreferences,
    private val profilePreferences: ProfilePreferences,
    private val remoteGateway: RetrofitIamGateway,
    private val networkMonitor: NetworkMonitor,
    private val profileGateway: RetrofitPatientProfileGateway = RetrofitPatientProfileGateway(),
    private val clearLocalData: suspend () -> Unit = {}
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): UserSession {
        check(networkMonitor.isOnline.value) { NO_CONNECTION_MESSAGE }
        val response = remoteGateway.signIn(email, password) ?: error(SERVER_UNAVAILABLE_MESSAGE)
        startSession(response, email)

        if (profilePreferences.firstName.isBlank()) {
            profilePreferences.firstName = email.substringBefore('@')
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
        return response.toSession(email)
    }

    override suspend fun signUp(
        fullName: String,
        email: String,
        password: String,
        role: String
    ): UserSession {
        check(networkMonitor.isOnline.value) { NO_CONNECTION_MESSAGE }
        val response = remoteGateway.signUp(email, password, role) ?: error(SERVER_UNAVAILABLE_MESSAGE)
        startSession(response, email)

        val nameParts = fullName.trim().split(Regex("\\s+"), limit = 2)
        val firstName = nameParts.getOrNull(0).orEmpty().ifBlank { "Usuario" }
        val lastName = nameParts.getOrNull(1).orEmpty().ifBlank { "SafeDiary" }
        profilePreferences.firstName = firstName
        profilePreferences.lastName = lastName

        val profileId = authSessionPreferences.patientProfileId
        if (profileId != AuthSessionPreferences.NO_ID) {
            runCatching {
                profileGateway.updatePatientProfile(patientId = profileId, firstName = firstName, lastName = lastName)
            }
        }
        return response.toSession(email)
    }

    /** Switches session and shared profile preferences; chat and diary storage are scoped per account. */
    private suspend fun startSession(response: SignInResponseDto, email: String) {
        val accountId = response.resolvedAccountId
        val accessToken = response.resolvedToken
        if (accountId != authSessionPreferences.lastAccountId) {
            clearLocalData()
            profilePreferences.clear()
        }
        authSessionPreferences.authToken = accessToken
        authSessionPreferences.patientAccountId = accountId
        authSessionPreferences.patientProfileId = AuthSessionPreferences.NO_ID
        authSessionPreferences.userEmail = email
        authSessionPreferences.isLoggedIn = true
        profilePreferences.email = email

        if (response.resolvedRole.equals("PATIENT", ignoreCase = true)) {
            profileGateway.fetchCurrentPatientProfile()?.let { authSessionPreferences.patientProfileId = it.id }
        }
    }

    private fun SignInResponseDto.toSession(email: String) = UserSession(
        accountId = resolvedAccountId,
        email = email,
        token = resolvedToken,
        role = resolvedRole
    )

    override suspend fun signOut() {
        authSessionPreferences.clear()
        authSessionPreferences.isLoggedIn = false
        profilePreferences.clear()
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

    private companion object {
        const val NO_CONNECTION_MESSAGE = "Sin conexión a internet. Conéctate para iniciar sesión."
        const val SERVER_UNAVAILABLE_MESSAGE = "No se pudo conectar con el servidor. Inténtalo de nuevo en un momento."
    }
}
