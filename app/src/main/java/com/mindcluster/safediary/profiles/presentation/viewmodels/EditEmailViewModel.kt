package com.mindcluster.safediary.profiles.presentation.viewmodels

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mindcluster.safediary.profiles.application.commands.UpdatePatientEmailCommand
import com.mindcluster.safediary.profiles.application.commands.UpdatePatientEmailHandler
import com.mindcluster.safediary.profiles.infrastructure.ProfilesModule
import com.mindcluster.safediary.shared.infrastructure.security.AuthSessionPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditEmailUiState(
    val currentEmail: String = "",
    val newEmail: String = "",
    val masterPassword: String = "",
    val isEmailValid: Boolean = true,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val canSubmit: Boolean
        get() = newEmail.isNotBlank() && isEmailValid && masterPassword.length >= 6 && !isSubmitting
}

class EditEmailViewModel(
    application: Application,
    private val updatePatientEmailHandler: UpdatePatientEmailHandler,
    private val authSessionPreferences: AuthSessionPreferences,
    private val patientId: Long = 1L,
    initialEmail: String = ""
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(EditEmailUiState(currentEmail = initialEmail))
    val uiState: StateFlow<EditEmailUiState> = _uiState.asStateFlow()

    fun onNewEmailChanged(email: String) {
        val isValid = email.isBlank() || Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
        _uiState.update {
            it.copy(
                newEmail = email,
                isEmailValid = isValid,
                errorMessage = null
            )
        }
    }

    fun onMasterPasswordChanged(password: String) {
        _uiState.update {
            it.copy(
                masterPassword = password,
                errorMessage = null
            )
        }
    }

    fun onPasswordChanged(password: String) {
        onMasterPasswordChanged(password)
    }

    fun sendVerificationCode(onSuccess: (() -> Unit)? = null) {
        submitNewEmail(onSuccess)
    }

    fun submitNewEmail(onSuccess: (() -> Unit)? = null) {
        val state = _uiState.value
        if (!state.canSubmit) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val result = updatePatientEmailHandler.handle(
                UpdatePatientEmailCommand(
                    patientId = patientId,
                    newEmail = state.newEmail.trim(),
                    currentPassword = state.masterPassword
                )
            )

            if (result.isSuccess) {
                // Update local auth session preferences with new email
                authSessionPreferences.userEmail = state.newEmail.trim()
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        isSuccess = true,
                        currentEmail = state.newEmail.trim(),
                        newEmail = "",
                        masterPassword = "",
                        errorMessage = null
                    )
                }
                onSuccess?.invoke()
            } else {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Error al actualizar el correo electrónico"
                    )
                }
            }
        }
    }

    class Factory(
        private val application: Application,
        private val profilesModule: ProfilesModule
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val email = profilesModule.profilePreferences.email
            val masked = if (email.contains("@")) {
                val prefix = email.substringBefore("@").take(5)
                val domain = email.substringAfter("@")
                "$prefix***@$domain"
            } else email

            return EditEmailViewModel(
                application = application,
                updatePatientEmailHandler = profilesModule.updatePatientEmailHandler,
                authSessionPreferences = profilesModule.authSessionPreferences,
                patientId = profilesModule.authSessionPreferences.patientProfileId,
                initialEmail = masked
            ) as T
        }
    }
}
