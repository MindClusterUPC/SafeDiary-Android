package com.mindcluster.safediary.profiles.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mindcluster.safediary.profiles.application.commands.*
import com.mindcluster.safediary.profiles.application.queries.GetPatientProfileHandler
import com.mindcluster.safediary.profiles.application.queries.GetPatientProfileQuery
import com.mindcluster.safediary.profiles.domain.model.EmergencyContact
import com.mindcluster.safediary.profiles.domain.model.RelationshipType
import com.mindcluster.safediary.profiles.infrastructure.ProfilesModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SafeContactUiState(
    val name: String = "",
    val relationship: RelationshipType = RelationshipType.SIBLING,
    val phoneNumber: String = "",
    val email: String = "",
    val notifyOnInactivity: Boolean = true,
    val hasExistingContact: Boolean = false,
    val isSaving: Boolean = false,
    val isRemoved: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val errorMessage: String? = null
) {
    val canSave: Boolean
        get() = name.isNotBlank() && (phoneNumber.isNotBlank() || email.isNotBlank()) && !isSaving
}

class SafeContactViewModel(
    application: Application,
    private val getPatientProfileHandler: GetPatientProfileHandler,
    private val updateEmergencyContactHandler: UpdateEmergencyContactHandler,
    private val removeEmergencyContactHandler: RemoveEmergencyContactHandler,
    private val patientId: Long = 1L
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SafeContactUiState())
    val uiState: StateFlow<SafeContactUiState> = _uiState.asStateFlow()

    init {
        loadContact()
    }

    private fun loadContact() {
        viewModelScope.launch {
            val result = getPatientProfileHandler.handle(GetPatientProfileQuery(patientId))
            val profile = result.getOrNull()
            val contact = profile?.emergencyContact
            if (contact != null) {
                _uiState.update {
                    it.copy(
                        name = contact.name,
                        relationship = contact.relationship,
                        phoneNumber = contact.phoneNumber ?: "",
                        email = contact.email ?: "",
                        notifyOnInactivity = contact.notifyOnExtendedInactivity,
                        hasExistingContact = true
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        name = "",
                        relationship = RelationshipType.SIBLING,
                        phoneNumber = "",
                        email = "",
                        notifyOnInactivity = true,
                        hasExistingContact = false
                    )
                }
            }
        }
    }

    fun onNameChanged(name: String) = _uiState.update { it.copy(name = name) }
    fun onRelationshipChanged(rel: RelationshipType) = _uiState.update { it.copy(relationship = rel) }
    fun onPhoneChanged(phone: String) = _uiState.update { it.copy(phoneNumber = phone) }
    fun onEmailChanged(email: String) = _uiState.update { it.copy(email = email) }
    fun onNotifyChanged(notify: Boolean) = _uiState.update { it.copy(notifyOnInactivity = notify) }

    fun requestDeleteConfirmation() = _uiState.update { it.copy(showDeleteConfirmDialog = true) }
    fun dismissDeleteConfirmation() = _uiState.update { it.copy(showDeleteConfirmDialog = false) }

    fun saveContact(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!state.canSave) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val contact = EmergencyContact(
                name = state.name.trim(),
                relationship = state.relationship,
                phoneNumber = state.phoneNumber.trim().ifEmpty { null },
                email = state.email.trim().ifEmpty { null },
                notifyOnExtendedInactivity = state.notifyOnInactivity
            )
            val result = updateEmergencyContactHandler.handle(
                UpdateEmergencyContactCommand(patientId, contact)
            )
            if (result.isSuccess) {
                _uiState.update { it.copy(isSaving = false, hasExistingContact = true) }
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Error al guardar contacto"
                    )
                }
            }
        }
    }

    fun removeContact(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, showDeleteConfirmDialog = false) }
            val result = removeEmergencyContactHandler.handle(RemoveEmergencyContactCommand(patientId))
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        name = "",
                        phoneNumber = "",
                        email = "",
                        hasExistingContact = false,
                        isRemoved = true,
                        isSaving = false
                    )
                }
                onSuccess()
            } else {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    class Factory(
        private val application: Application,
        private val profilesModule: ProfilesModule
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SafeContactViewModel(
                application = application,
                getPatientProfileHandler = profilesModule.getPatientProfileHandler,
                updateEmergencyContactHandler = profilesModule.updateEmergencyContactHandler,
                removeEmergencyContactHandler = profilesModule.removeEmergencyContactHandler,
                patientId = profilesModule.authSessionPreferences.patientAccountId
            ) as T
        }
    }
}
