package com.mindcluster.safediary.professionals.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.mindcluster.safediary.professionals.infrastructure.local.PreviewProfessionals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.Normalizer

data class ProfessionalsUiState(val query: String = "", val category: String = "Todos", val affordableOnly: Boolean = false, val favoritesOnly: Boolean = false, val favorites: Set<String> = emptySet()) {
    val results get() = PreviewProfessionals.all.filter {
        (query.isBlank() || (it.name + " " + it.specialty + " " + it.tags.joinToString()).normalized().contains(query.normalized())) &&
            (category == "Todos" || category in it.tags) && (!affordableOnly || it.price <= 100) && (!favoritesOnly || it.id in favorites)
    }
}
private fun String.normalized() = Normalizer.normalize(trim().lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")

class ProfessionalsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfessionalsUiState())
    val uiState = _uiState.asStateFlow()
    fun search(value: String) { _uiState.update { it.copy(query = value) } }
    fun category(value: String) { _uiState.update { it.copy(category = value) } }
    fun affordable(value: Boolean) { _uiState.update { it.copy(affordableOnly = value) } }
    fun favoritesOnly(value: Boolean) { _uiState.update { it.copy(favoritesOnly = value) } }
    fun toggleFavorite(id: String) { _uiState.update { it.copy(favorites = if (id in it.favorites) it.favorites - id else it.favorites + id) } }
    fun clearFilters() { _uiState.update { ProfessionalsUiState(favorites = it.favorites) } }
}
