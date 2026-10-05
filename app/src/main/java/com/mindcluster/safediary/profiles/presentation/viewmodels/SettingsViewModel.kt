package com.mindcluster.safediary.profiles.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mindcluster.safediary.assistantai.domain.model.DiaritoPersonality
import com.mindcluster.safediary.assistantai.infrastructure.local.PersonalityPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val selectedToneIndex: Int = 0,
    val isPlayingVoiceSample: Boolean = false,
    val selectedThemeIndex: Int = 0,
    val vaultE2eeEnabled: Boolean = true,
    val trainModelsEnabled: Boolean = false,
    val biometricLockEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val isLanguageSheetOpen: Boolean = false
)

class SettingsViewModel @JvmOverloads constructor(
    application: Application,
    private val personalityPreferences: PersonalityPreferences = PersonalityPreferences(application)
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(
        SettingsUiState(
            selectedToneIndex = DiaritoPersonality.entries.indexOf(personalityPreferences.get()).coerceAtLeast(0)
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun selectTone(index: Int) {
        val personality = DiaritoPersonality.entries.getOrNull(index) ?: DiaritoPersonality.SOL
        personalityPreferences.set(personality)
        _uiState.update { it.copy(selectedToneIndex = index) }
    }

    fun selectTheme(index: Int) {
        _uiState.update { it.copy(selectedThemeIndex = index) }
    }

    fun playVoiceSample() {
        if (_uiState.value.isPlayingVoiceSample) return
        viewModelScope.launch {
            _uiState.update { it.copy(isPlayingVoiceSample = true) }
            delay(2200)
            _uiState.update { it.copy(isPlayingVoiceSample = false) }
        }
    }

    fun toggleVaultE2ee(enabled: Boolean) {
        _uiState.update { it.copy(vaultE2eeEnabled = enabled) }
    }

    fun toggleTrainModels(enabled: Boolean) {
        _uiState.update { it.copy(trainModelsEnabled = enabled) }
    }

    fun toggleBiometricLock(enabled: Boolean) {
        _uiState.update { it.copy(biometricLockEnabled = enabled) }
    }

    fun toggleHaptics(enabled: Boolean) {
        _uiState.update { it.copy(hapticsEnabled = enabled) }
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
    }

    fun openLanguageSheet() {
        _uiState.update { it.copy(isLanguageSheetOpen = true) }
    }

    fun closeLanguageSheet() {
        _uiState.update { it.copy(isLanguageSheetOpen = false) }
    }
}
