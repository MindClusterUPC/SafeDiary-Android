package com.mindcluster.safediary.rutines.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.mindcluster.safediary.rutines.domain.model.Routine
import com.mindcluster.safediary.rutines.domain.model.RoutineKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class RutinesUiState(val routines: List<Routine> = listOf(
    Routine("morning", "Meditación de la mañana", "Comienza con una respiración profunda y un momento de gratitud.", "09:00", 5, setOf(0, 1, 2, 3, 4), kind = RoutineKind.BREATHING),
    Routine("midday", "Pausa del mediodía", "Un espacio breve para volver al presente.", "13:00", 3, setOf(0, 2, 4)),
    Routine("evening", "Reflexión de la noche", "Reconoce lo que sentiste hoy y prepárate para descansar.", "21:30", 5, (0..6).toSet(), reminder = false)
))

class RutinesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RutinesUiState())
    val uiState = _uiState.asStateFlow()

    fun toggleReminder(id: String) { _uiState.update { state -> state.copy(routines = state.routines.map { if (it.id == id) it.copy(reminder = !it.reminder) else it }) } }
    fun complete(id: String) { _uiState.update { state -> state.copy(routines = state.routines.map { if (it.id == id) it.copy(completed = true) else it }) } }
    fun add(title: String, description: String, time: String, minutes: Int, days: Set<Int>, reminder: Boolean, kind: RoutineKind = RoutineKind.GROUNDING): Boolean {
        if (title.isBlank() || days.isEmpty() || minutes !in 1..60 || !Regex("([01][0-9]|2[0-3]):[0-5][0-9]").matches(time)) return false
        val routine = Routine(UUID.randomUUID().toString(), title.trim(), description.trim(), time, minutes, days, reminder, kind = kind)
        _uiState.update { it.copy(routines = it.routines + routine) }
        return true
    }
}
