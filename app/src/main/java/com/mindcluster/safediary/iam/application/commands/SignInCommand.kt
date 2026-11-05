package com.mindcluster.safediary.iam.application.commands

import com.mindcluster.safediary.iam.domain.model.UserSession
import com.mindcluster.safediary.iam.domain.repository.AuthRepository

data class SignInCommand(
    val email: String,
    val password: String
)

class SignInHandler(
    private val authRepository: AuthRepository
) {
    suspend fun handle(command: SignInCommand): Result<UserSession> = runCatching {
        val trimmedEmail = command.email.trim()
        val password = command.password
        require(trimmedEmail.contains("@") && trimmedEmail.contains(".")) {
            "Invalid email address"
        }
        require(password.length >= 4) {
            "Password must have at least 4 characters"
        }
        authRepository.signIn(trimmedEmail, password)
    }
}
