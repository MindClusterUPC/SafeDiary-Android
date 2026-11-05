package com.mindcluster.safediary.iam.application.commands

import com.mindcluster.safediary.iam.domain.repository.AuthRepository

class SignOutCommand

class SignOutHandler(
    private val authRepository: AuthRepository
) {
    suspend fun handle(command: SignOutCommand = SignOutCommand()): Result<Unit> = runCatching {
        authRepository.signOut()
    }
}
