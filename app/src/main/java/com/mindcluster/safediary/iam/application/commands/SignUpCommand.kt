package com.mindcluster.safediary.iam.application.commands

import com.mindcluster.safediary.iam.domain.model.UserSession
import com.mindcluster.safediary.iam.domain.repository.AuthRepository

data class SignUpCommand(
    val fullName: String,
    val email: String,
    val password: String,
    val role: String = "PATIENT"
)

class SignUpHandler(
    private val authRepository: AuthRepository
) {
    suspend fun handle(command: SignUpCommand): Result<UserSession> = runCatching {
        val trimmedName = command.fullName.trim()
        val trimmedEmail = command.email.trim()
        val password = command.password
        val role = command.role.trim().uppercase().ifBlank { "PATIENT" }

        require(trimmedName.length >= 2) {
            "Ingresa tu nombre completo"
        }
        require(trimmedEmail.contains("@") && trimmedEmail.contains(".")) {
            "Ingresa un correo electrónico válido"
        }
        require(password.length in 8..72) {
            "La contraseña debe tener entre 8 y 72 caracteres"
        }
        require(password.any { !it.isLetter() }) {
            "La contraseña debe incluir al menos un número o símbolo"
        }

        authRepository.signUp(
            fullName = trimmedName,
            email = trimmedEmail,
            password = password,
            role = role
        )
    }
}
