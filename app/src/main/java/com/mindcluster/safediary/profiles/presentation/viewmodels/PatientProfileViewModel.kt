package com.mindcluster.safediary.profiles.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mindcluster.safediary.profiles.application.commands.*
import com.mindcluster.safediary.profiles.application.queries.GetPatientProfileHandler
import com.mindcluster.safediary.profiles.application.queries.GetPatientProfileQuery
import com.mindcluster.safediary.profiles.domain.model.AvatarConfig
import com.mindcluster.safediary.profiles.domain.model.PatientProfileAggregate
import com.mindcluster.safediary.profiles.domain.model.TherapeuticTopic
import com.mindcluster.safediary.profiles.domain.repository.PatientProfileRepository
import com.mindcluster.safediary.profiles.infrastructure.ProfilesModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PatientProfileUiState(
    val profile: PatientProfileAggregate? = null,
    val isLoading: Boolean = false,
    val isAvatarSheetOpen: Boolean = false,
    val saveMessage: String? = null
)

class PatientProfileViewModel(
    application: Application,
    private val repository: PatientProfileRepository,
    private val getPatientProfileHandler: GetPatientProfileHandler,
    private val updatePatientNameHandler: UpdatePatientNameHandler,
    private val updateAvatarHandler: UpdateAvatarHandler,
    private val updateTherapeuticTopicsHandler: UpdateTherapeuticTopicsHandler,
    private val updatePrivacySettingsHandler: UpdatePrivacySettingsHandler,
    private val patientId: Long = 1L
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PatientProfileUiState(isLoading = true))
    val uiState: StateFlow<PatientProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        viewModelScope.launch {
            repository.observeProfile(patientId).collect { currentProfile ->
                if (currentProfile != null) {
                    _uiState.update { it.copy(profile = currentProfile, isLoading = false) }
                }
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = getPatientProfileHandler.handle(GetPatientProfileQuery(patientId))
            _uiState.update {
                it.copy(
                    profile = result.getOrNull(),
                    isLoading = false
                )
            }
        }
    }

    fun updateName(firstName: String, lastName: String) {
        viewModelScope.launch {
            val result = updatePatientNameHandler.handle(
                UpdatePatientNameCommand(patientId, firstName, lastName)
            )
            result.getOrNull()?.let { updated ->
                _uiState.update { it.copy(profile = updated) }
            }
        }
    }

    fun openAvatarSheet() {
        _uiState.update { it.copy(isAvatarSheetOpen = true) }
    }

    fun closeAvatarSheet() {
        _uiState.update { it.copy(isAvatarSheetOpen = false) }
    }

    fun selectAvatar(config: AvatarConfig) {
        viewModelScope.launch {
            val result = updateAvatarHandler.handle(UpdateAvatarCommand(patientId, config))
            result.getOrNull()?.let { updated ->
                _uiState.update { it.copy(profile = updated, isAvatarSheetOpen = false) }
            }
        }
    }

    fun toggleTopic(topic: TherapeuticTopic) {
        val currentProfile = _uiState.value.profile ?: return
        val currentTopics = currentProfile.selectedTopics
        val newTopics = if (currentTopics.contains(topic)) currentTopics - topic else currentTopics + topic
        viewModelScope.launch {
            val result = updateTherapeuticTopicsHandler.handle(
                UpdateTherapeuticTopicsCommand(patientId, newTopics)
            )
            result.getOrNull()?.let { updated ->
                _uiState.update { it.copy(profile = updated) }
            }
        }
    }

    fun toggleBiometricLock(enabled: Boolean) {
        val currentProfile = _uiState.value.profile ?: return
        viewModelScope.launch {
            val result = updatePrivacySettingsHandler.handle(
                UpdatePrivacySettingsCommand(patientId, enabled, currentProfile.dailyReminderEnabled)
            )
            result.getOrNull()?.let { updated ->
                _uiState.update { it.copy(profile = updated) }
            }
        }
    }

    fun toggleDailyReminder(enabled: Boolean) {
        val currentProfile = _uiState.value.profile ?: return
        viewModelScope.launch {
            val result = updatePrivacySettingsHandler.handle(
                UpdatePrivacySettingsCommand(patientId, currentProfile.biometricLockEnabled, enabled)
            )
            result.getOrNull()?.let { updated ->
                _uiState.update { it.copy(profile = updated) }
            }
        }
    }

    class Factory(
        private val application: Application,
        private val profilesModule: ProfilesModule
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PatientProfileViewModel(
                application = application,
                repository = profilesModule.repository,
                getPatientProfileHandler = profilesModule.getPatientProfileHandler,
                updatePatientNameHandler = profilesModule.updatePatientNameHandler,
                updateAvatarHandler = profilesModule.updateAvatarHandler,
                updateTherapeuticTopicsHandler = profilesModule.updateTherapeuticTopicsHandler,
                updatePrivacySettingsHandler = profilesModule.updatePrivacySettingsHandler,
                patientId = profilesModule.authSessionPreferences.patientProfileId
            ) as T
        }
    }
}
