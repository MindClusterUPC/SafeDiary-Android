package com.mindcluster.safediary.rutines.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindcluster.safediary.rutines.domain.model.Routine
import com.mindcluster.safediary.rutines.domain.model.RoutineKind
import com.mindcluster.safediary.rutines.infrastructure.remote.RetrofitRutinesGateway
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class RutinesUiState(
    val routines: List<Routine> = listOf(
        Routine("morning", "Meditación de la mañana", "Comienza con una respiración profunda y un momento de gratitud.", "09:00", 5, setOf(0, 1, 2, 3, 4), kind = RoutineKind.BREATHING),
        Routine("midday", "Pausa del mediodía", "Un espacio breve para volver al presente.", "13:00", 3, setOf(0, 2, 4)),
        Routine("evening", "Reflexión de la noche", "Reconoce lo que sentiste hoy y prepárate para descansar.", "21:30", 5, (0..6).toSet(), reminder = false)
    ),
    val isLoading: Boolean = false
)

class RutinesViewModel @JvmOverloads constructor(
    private val gateway: RetrofitRutinesGateway = RetrofitRutinesGateway(),
    // Set by the screen from the signed-in account before the first load.
    private var currentPatientId: Long = 0L
) : ViewModel() {

    private val _uiState = MutableStateFlow(RutinesUiState())
    val uiState = _uiState.asStateFlow()

    private val dayNames = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")

    fun loadRoutines(patientId: Long = currentPatientId) {
        currentPatientId = patientId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val remoteList = gateway.getRoutinesByPatientId(patientId)
            if (remoteList.isNotEmpty()) {
                val mapped = remoteList.map { dto ->
                    val daysSet = dto.frequencyDays.mapNotNull { name ->
                        dayNames.indexOf(name.uppercase()).takeIf { it >= 0 }
                    }.toSet().ifEmpty { setOf(0, 1, 2, 3, 4) }
                    Routine(
                        id = dto.id.toString(),
                        title = dto.title,
                        description = if (dto.title.contains("respiración", ignoreCase = true) || dto.title.contains("meditación", ignoreCase = true) || dto.title.contains("breathing", ignoreCase = true)) "Ejercicio guiado de respiración." else "Pausa para reconectar y volver al presente.",
                        time = "09:00",
                        minutes = 5,
                        days = daysSet,
                        reminder = dto.notificationStatus == "ENABLED",
                        completed = !dto.isActive,
                        kind = if (dto.title.contains("respiración", ignoreCase = true) || dto.title.contains("meditación", ignoreCase = true) || dto.title.contains("breathing", ignoreCase = true)) RoutineKind.BREATHING else RoutineKind.GROUNDING
                    )
                }
                _uiState.update { it.copy(routines = mapped, isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun toggleReminder(id: String) {
        val current = _uiState.value.routines.firstOrNull { it.id == id } ?: return
        val newReminder = !current.reminder
        _uiState.update { state ->
            state.copy(routines = state.routines.map { if (it.id == id) it.copy(reminder = newReminder) else it })
        }
        val numericId = id.toLongOrNull()
        if (numericId != null) {
            viewModelScope.launch {
                gateway.toggleRoutineNotification(numericId, newReminder)
            }
        }
    }

    fun complete(id: String) {
        _uiState.update { state ->
            state.copy(routines = state.routines.map { if (it.id == id) it.copy(completed = true) else it })
        }
        val numericId = id.toLongOrNull()
        val routine = _uiState.value.routines.firstOrNull { it.id == id }
        viewModelScope.launch {
            if (numericId != null) {
                gateway.toggleRoutineActive(numericId)
            }
            val exerciseType = if (routine?.kind == RoutineKind.BREATHING) "Deep Breathing Exercise" else "5-4-3-2-1 Grounding"
            val totalSteps = if (routine?.kind == RoutineKind.BREATHING) 1 else 5
            val started = gateway.startSosExercise(currentPatientId, exerciseType, totalSteps)
            if (started != null) {
                gateway.completeSosExercise(started.id)
            }
        }
    }

    fun add(
        title: String,
        description: String,
        time: String,
        minutes: Int,
        days: Set<Int>,
        reminder: Boolean,
        kind: RoutineKind = RoutineKind.GROUNDING
    ): Boolean {
        if (title.isBlank() || days.isEmpty() || minutes !in 1..60 || !Regex("([01][0-9]|2[0-3]):[0-5][0-9]").matches(time)) {
            return false
        }
        val tempId = UUID.randomUUID().toString()
        val localRoutine = Routine(tempId, title.trim(), description.trim(), time, minutes, days, reminder, kind = kind)
        _uiState.update { it.copy(routines = it.routines + localRoutine) }

        val frequencyDays = days.mapNotNull { dayNames.getOrNull(it) }.toSet().ifEmpty { setOf("MONDAY") }

        viewModelScope.launch {
            val created = gateway.createDailyRoutine(
                patientId = currentPatientId,
                title = title.trim(),
                frequencyDays = frequencyDays,
                isNotificationActive = reminder
            )
            if (created != null) {
                _uiState.update { state ->
                    state.copy(routines = state.routines.map {
                        if (it.id == tempId) it.copy(id = created.id.toString()) else it
                    })
                }
            }
        }
        return true
    }
}
