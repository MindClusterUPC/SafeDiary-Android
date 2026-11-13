package com.mindcluster.safediary.iam.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mindcluster.safediary.iam.application.commands.SignInCommand
import com.mindcluster.safediary.iam.application.commands.SignInHandler
import com.mindcluster.safediary.iam.application.commands.SignUpCommand
import com.mindcluster.safediary.iam.application.commands.SignUpHandler
import com.mindcluster.safediary.iam.infrastructure.IamModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignInUiState(
    val isSignUpMode: Boolean = false,
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val passwordsMatch: Boolean
        get() = !isSignUpMode || confirmPassword.isEmpty() || password == confirmPassword

    val canSubmit: Boolean
        get() = if (isSignUpMode) {
            fullName.trim().length >= 2 &&
                email.isNotBlank() &&
                password.length >= 8 &&
                password == confirmPassword &&
                !isLoading
        } else {
            email.isNotBlank() && password.length >= 4 && !isLoading
        }
}

class SignInViewModel(
    application: Application,
    private val signInHandler: SignInHandler,
    private val signUpHandler: SignUpHandler
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun setSignUpMode(isSignUp: Boolean) {
        _uiState.update {
            it.copy(
                isSignUpMode = isSignUp,
                errorMessage = null
            )
        }
    }

    fun toggleAuthMode() {
        _uiState.update {
            it.copy(
                isSignUpMode = !it.isSignUpMode,
                errorMessage = null
            )
        }
    }

    fun onFullNameChanged(fullName: String) {
        _uiState.update { it.copy(fullName = fullName, errorMessage = null) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun submit(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!state.canSubmit) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = if (state.isSignUpMode) {
                signUpHandler.handle(
                    SignUpCommand(
                        fullName = state.fullName,
                        email = state.email,
                        password = state.password,
                        role = "PATIENT"
                    )
                )
            } else {
                signInHandler.handle(SignInCommand(state.email, state.password))
            }

            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } else {
                val defaultError = if (state.isSignUpMode) {
                    "Error al crear la cuenta"
                } else {
                    "Error al iniciar sesión"
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: defaultError
                    )
                }
            }
        }
    }

    class Factory(
        private val application: Application,
        private val iamModule: IamModule
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SignInViewModel(
                application = application,
                signInHandler = iamModule.signInHandler,
                signUpHandler = iamModule.signUpHandler
            ) as T
        }
    }
}
