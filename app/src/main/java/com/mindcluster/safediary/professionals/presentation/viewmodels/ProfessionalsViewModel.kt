package com.mindcluster.safediary.professionals.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindcluster.safediary.professionals.domain.model.Professional
import com.mindcluster.safediary.professionals.infrastructure.remote.RetrofitClinicianDirectoryGateway
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Normalizer

data class ProfessionalsUiState(
    val professionals: List<Professional> = emptyList(),
    val isLoading: Boolean = false,
    val loadFailed: Boolean = false,
    val query: String = "",
    val category: String = "Todos",
    val affordableOnly: Boolean = false,
    val favoritesOnly: Boolean = false,
    val favorites: Set<String> = emptySet()
) {
    val categories get() = listOf("Todos") + professionals.flatMap { it.tags }.distinct()
    val results get() = professionals.filter {
        (query.isBlank() || (it.name + " " + it.specialty + " " + it.tags.joinToString()).normalized().contains(query.normalized())) &&
            (category == "Todos" || category in it.tags) && (!affordableOnly || it.price <= 100) && (!favoritesOnly || it.id in favorites)
    }
    fun find(id: String) = professionals.firstOrNull { it.id == id }
}
private fun String.normalized() = Normalizer.normalize(trim().lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")

class ProfessionalsViewModel(
    private val gateway: RetrofitClinicianDirectoryGateway = RetrofitClinicianDirectoryGateway()
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfessionalsUiState())
    val uiState = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadFailed = false) }
            val professionals = gateway.searchClinicians()
            _uiState.update {
                it.copy(professionals = professionals ?: it.professionals, isLoading = false, loadFailed = professionals == null)
            }
        }
    }
    fun search(value: String) { _uiState.update { it.copy(query = value) } }
    fun category(value: String) { _uiState.update { it.copy(category = value) } }
    fun affordable(value: Boolean) { _uiState.update { it.copy(affordableOnly = value) } }
    fun favoritesOnly(value: Boolean) { _uiState.update { it.copy(favoritesOnly = value) } }
    fun toggleFavorite(id: String) { _uiState.update { it.copy(favorites = if (id in it.favorites) it.favorites - id else it.favorites + id) } }
    fun clearFilters() {
        _uiState.update { ProfessionalsUiState(professionals = it.professionals, isLoading = it.isLoading, loadFailed = it.loadFailed, favorites = it.favorites) }
    }
}
