package com.mindcluster.safediary.diary.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mindcluster.safediary.diary.domain.model.DiaryEntryAggregate
import com.mindcluster.safediary.diary.domain.repository.DiaryEntryRepository
import com.mindcluster.safediary.shared.infrastructure.security.AuthSessionPreferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DiaryViewModel(
    private val repository: DiaryEntryRepository,
    private val authSessionPreferences: AuthSessionPreferences
) : ViewModel() {

    val currentPatientId: Long
        get() = authSessionPreferences.patientAccountId

    val entries: StateFlow<List<DiaryEntryAggregate>> = repository
        .getEntriesFlow(currentPatientId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentDraft = MutableStateFlow<DiaryEntryAggregate?>(null)
    val currentDraft: StateFlow<DiaryEntryAggregate?> = _currentDraft.asStateFlow()

    private var autoSaveJob: Job? = null

    fun openNewDraft() {
        val draft = DiaryEntryAggregate.createDraft(currentPatientId)
        _currentDraft.value = draft
    }

    fun openExistingEntry(localId: String) {
        viewModelScope.launch {
            val entry = repository.getEntryByLocalId(localId)
            _currentDraft.value = entry
        }
    }

    fun onTitleChanged(newTitle: String) {
        val current = _currentDraft.value ?: DiaryEntryAggregate.createDraft(currentPatientId)
        _currentDraft.value = current.copy(title = newTitle)
        scheduleAutoSave()
    }

    fun onContentChanged(newContent: String) {
        val current = _currentDraft.value ?: DiaryEntryAggregate.createDraft(currentPatientId)
        _currentDraft.value = current.copy(content = newContent)
        scheduleAutoSave()
    }

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(1500)
            persistCurrentDraft()
        }
    }

    fun persistCurrentDraft() {
        val draft = _currentDraft.value ?: return
        // Don't save if both title and content are completely blank
        if (draft.title.isBlank() && draft.content.isBlank()) return

        val titleToSave = if (draft.title.isBlank()) DiaryEntryAggregate.defaultTitle() else draft.title
        val toSave = draft.copy(title = titleToSave)

        viewModelScope.launch {
            val saved = repository.saveEntry(toSave)
            _currentDraft.value?.let { current ->
                if (current.localId == saved.localId) {
                    _currentDraft.value = current.copy(
                        remoteId = saved.remoteId,
                        isSynced = saved.isSynced,
                        updatedAt = saved.updatedAt
                    )
                }
            }
        }
    }

    fun deleteEntry(localId: String) {
        viewModelScope.launch {
            repository.deleteEntry(localId)
            if (_currentDraft.value?.localId == localId) {
                _currentDraft.value = null
            }
        }
    }

    fun closeDraft() {
        persistCurrentDraft()
        _currentDraft.value = null
    }

    class Factory(
        private val repository: DiaryEntryRepository,
        private val authSessionPreferences: AuthSessionPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DiaryViewModel(repository, authSessionPreferences) as T
        }
    }
}
