package com.mindcluster.safediary.rutines.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindcluster.safediary.rutines.domain.model.Routine
import com.mindcluster.safediary.rutines.domain.model.RoutineKind
import com.mindcluster.safediary.rutines.infrastructure.remote.RetrofitRutinesGateway
import com.mindcluster.safediary.rutines.infrastructure.remote.dto.DailyRoutineResponseDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class RutinesUiState(
    val routines: List<Routine> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

private val dayNames = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")
internal fun DailyRoutineResponseDto.toRoutine(today: String = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { timeZone = TimeZone.getTimeZone("America/Lima") }.format(Date())) = Routine(
    id.toString(), title, description.orEmpty(), time ?: "09:00", minutes ?: 5,
    frequencyDays.mapNotNull { dayNames.indexOf(it.uppercase()).takeIf { index -> index >= 0 } }.toSet(),
    notificationStatus == "ENABLED", completedOn == today,
    RoutineKind.entries.firstOrNull { it.name == kind } ?: RoutineKind.GROUNDING
)

class RutinesViewModel @JvmOverloads constructor(
    private val gateway: RetrofitRutinesGateway = RetrofitRutinesGateway(),
    private var currentPatientId: Long = 0L
) : ViewModel() {
    private val _uiState = MutableStateFlow(RutinesUiState())
    val uiState = _uiState.asStateFlow()
    private var loading: Job? = null

    fun loadRoutines(patientId: Long = currentPatientId) {
        if (patientId <= 0 || _uiState.value.isSaving) return
        loading?.cancel()
        if (currentPatientId != patientId) _uiState.value = RutinesUiState()
        currentPatientId = patientId
        loading = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val remote = gateway.getRoutinesByPatientId(patientId)
            _uiState.update {
                if (remote == null) it.copy(isLoading = false, error = "No pudimos cargar tus rutinas. Revisa tu conexión e inténtalo de nuevo.")
                else it.copy(routines = remote.filter { row -> row.patientId == patientId }.map { row -> row.toRoutine() }, isLoading = false)
            }
        }
    }

    private fun change(action: suspend () -> Boolean, onSuccess: () -> Unit = {}) {
        if (_uiState.value.isSaving) return
        loading?.cancel()
        _uiState.update { it.copy(isSaving = true, isLoading = false, error = null) }
        viewModelScope.launch {
            val success = action()
            _uiState.update { it.copy(isSaving = false, error = if (success) null else "No se guardó el cambio. Revisa tu conexión y vuelve a intentarlo.") }
            if (success) onSuccess()
        }
    }

    private fun replace(dto: DailyRoutineResponseDto) {
        _uiState.update { state -> state.copy(routines = state.routines.map { if (it.id == dto.id.toString()) dto.toRoutine() else it }) }
    }

    fun toggleReminder(id: String) {
        val routine = _uiState.value.routines.find { it.id == id } ?: return
        change({ gateway.toggleRoutineNotification(id.toLong(), !routine.reminder)?.also(::replace) != null })
    }

    fun complete(id: String, onSuccess: () -> Unit = {}) {
        val routine = _uiState.value.routines.find { it.id == id } ?: return
        if (routine.completed) { onSuccess(); return }
        change({ gateway.completeRoutine(id.toLong())?.also(::replace) != null }, onSuccess)
    }

    fun delete(id: String, onSuccess: () -> Unit = {}) = change({
        if (gateway.deleteRoutine(id.toLong())) {
            _uiState.update { it.copy(routines = it.routines.filterNot { routine -> routine.id == id }) }
            true
        } else false
    }, onSuccess)

    fun add(title: String, description: String, time: String, minutes: Int, days: Set<Int>, reminder: Boolean,
            kind: RoutineKind = RoutineKind.GROUNDING, onSuccess: () -> Unit = {}): Boolean =
        save(null, title, description, time, minutes, days, reminder, kind, onSuccess)

    fun save(id: String?, title: String, description: String, time: String, minutes: Int, days: Set<Int>, reminder: Boolean,
             kind: RoutineKind, onSuccess: () -> Unit): Boolean {
        if (currentPatientId <= 0 || title.isBlank() || title.trim().length > 60 || description.length > 1000 || days.isEmpty()
            || days.any { it !in 0..6 } || minutes !in 1..60 || !Regex("([01][0-9]|2[0-3]):[0-5][0-9]").matches(time)) return false
        val frequency = days.map { dayNames[it] }.toSet()
        change({
            val dto = if (id == null) gateway.createDailyRoutine(currentPatientId, title.trim(), frequency, reminder, description.trim(), time, minutes, kind.name)
            else gateway.updateDailyRoutine(id.toLong(), title.trim(), reminder, frequency, description.trim(), time, minutes, kind.name)
            if (dto == null) false else {
                if (id == null) _uiState.update { it.copy(routines = it.routines + dto.toRoutine()) } else replace(dto)
                true
            }
        }, onSuccess)
        return true
    }
}
