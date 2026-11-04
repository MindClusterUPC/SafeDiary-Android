package com.mindcluster.safediary.home.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class HomeViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    val mood = savedState.getStateFlow<String?>("mood", null)
    fun selectMood(value: String) { savedState["mood"] = value }
}
