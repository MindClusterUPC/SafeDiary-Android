package com.mindcluster.safediary.profiles.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mindcluster.safediary.assistantai.domain.model.DiaritoPersonality
import com.mindcluster.safediary.assistantai.infrastructure.local.PersonalityPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val selectedToneIndex: Int = 0,
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

    fun openLanguageSheet() {
        _uiState.update { it.copy(isLanguageSheetOpen = true) }
    }

    fun closeLanguageSheet() {
        _uiState.update { it.copy(isLanguageSheetOpen = false) }
    }
}
