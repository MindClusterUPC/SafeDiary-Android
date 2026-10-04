package com.mindcluster.safediary.profiles.presentation.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val darkThemeEnabled: Boolean = false,
    val pinLockEnabled: Boolean = false,
    val biometricEnabled: Boolean = true,
    val isLanguageSheetOpen: Boolean = false
)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun toggleNotifications(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
    }

    fun toggleDarkTheme(enabled: Boolean) {
        _uiState.update { it.copy(darkThemeEnabled = enabled) }
    }

    fun togglePinLock(enabled: Boolean) {
        _uiState.update { it.copy(pinLockEnabled = enabled) }
    }

    fun toggleBiometric(enabled: Boolean) {
        _uiState.update { it.copy(biometricEnabled = enabled) }
    }

    fun openLanguageSheet() {
        _uiState.update { it.copy(isLanguageSheetOpen = true) }
    }

    fun closeLanguageSheet() {
        _uiState.update { it.copy(isLanguageSheetOpen = false) }
    }
}
