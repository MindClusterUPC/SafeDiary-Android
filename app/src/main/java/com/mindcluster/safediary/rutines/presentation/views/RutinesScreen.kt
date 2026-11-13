package com.mindcluster.safediary.rutines.presentation.views

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindcluster.safediary.rutines.domain.model.Routine
import com.mindcluster.safediary.rutines.domain.model.RoutineKind
import com.mindcluster.safediary.rutines.presentation.components.BreathingExercise
import com.mindcluster.safediary.rutines.presentation.viewmodels.RutinesViewModel
import com.mindcluster.safediary.shared.infrastructure.security.AuthSessionPreferences
import com.mindcluster.safediary.shared.presentation.components.*
import com.mindcluster.safediary.shared.presentation.navigation.NavRoutes
import com.mindcluster.safediary.shared.presentation.theme.*

private val dayLabels = listOf("L", "M", "X", "J", "V", "S", "D")
private val dayNames = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

@Composable
fun RutinesScreen(
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = {},
    isActive: Boolean = true,
    viewModel: RutinesViewModel = viewModel()
) {
    val context = LocalContext.current
    val authSessionPreferences = remember { AuthSessionPreferences(context) }
    val patientId = authSessionPreferences.patientAccountId

    LaunchedEffect(isActive, patientId) {
        if (isActive) {
            viewModel.loadRoutines(patientId)
        }
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var page by rememberSaveable { mutableStateOf("list") }
    var routineId by rememberSaveable { mutableStateOf("") }
    var deleteId by rememberSaveable { mutableStateOf<String?>(null) }
    var step by rememberSaveable { mutableIntStateOf(0) }
    var confirmExit by rememberSaveable { mutableStateOf(false) }
    val active = state.routines.firstOrNull { it.id == routineId }
    fun back() { if (state.isSaving) return; if (page == "exercise") confirmExit = true else page = "list" }
    BackHandler(isActive && page != "list") { back() }
    PatientScreen("Rutinas", modifier, onBack = if (page != "list") ({ back() }) else null, onSettings = onNavigateToSettings, contentKey = page) {
        state.error?.let { Text(it, color = ErrorRed, style = MaterialTheme.typography.bodyMedium) }
        when (page) {
            "create", "edit" -> key(if (page == "edit") routineId else "new") {
                RoutineForm(initial = if (page == "edit") active else null, isSaving = state.isSaving,
                    onSave = { title, description, time, minutes, days, reminder, kind ->
                        viewModel.save(if (page == "edit") routineId else null, title, description, time, minutes, days, reminder, kind) { page = "list" }
                    }, onCancel = { page = "list" })
            }
            "exercise" -> {
                if (active?.kind == RoutineKind.BREATHING) {
                    BreathingExercise(active.id, active.minutes, isActive && !confirmExit && !state.isSaving, onComplete = { viewModel.complete(routineId) { page = "complete" } }, onExit = { confirmExit = true })
                    if (state.error != null) PatientButton("Guardar actividad completada", { viewModel.complete(routineId) { page = "complete" } }, Modifier.fillMaxWidth(), enabled = !state.isSaving)
                } else {
                val prompts = listOf(
                    "5 cosas que puedes ver" to "Mira a tu alrededor. Observa cinco objetos y nota sus colores, formas y detalles.",
                    "4 cosas que puedes sentir" to "Siente cuatro texturas: la tela de tu ropa, el suelo o el apoyo de la silla.",
                    "3 cosas que puedes oír" to "Escucha con calma. Identifica tres sonidos a tu alrededor.",
                    "2 cosas que puedes oler" to "Reconoce dos aromas. Si no los encuentras, recuerda dos que te resulten agradables.",
                    "1 cosa que puedes saborear" to "Nota el sabor en tu boca o piensa en uno que disfrutes. Respira a tu ritmo."
                )
                PatientHeading("Vuelve al presente", "Una pausa para conectar con tus sentidos.")
                PatientCard {
                    Column(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        PatientBadge("PASO ${step + 1} DE 5")
                        Icon(Icons.Outlined.SelfImprovement, null, Modifier.size(80.dp), tint = SecondaryTeal)
                        Text(prompts[step].first, style = MaterialTheme.typography.headlineMedium, color = PrimaryNavy, textAlign = TextAlign.Center)
                        Text(prompts[step].second, color = SlateSubtle, textAlign = TextAlign.Center)
                        LinearProgressIndicator(progress = { (step + 1) / 5f }, modifier = Modifier.fillMaxWidth(), color = SecondaryTeal)
                        PatientButton(if (step < 4) "Siguiente paso" else "Finalizar ejercicio", {
                            if (step < 4) step++ else viewModel.complete(routineId) { page = "complete" }
                        }, Modifier.fillMaxWidth(), enabled = !state.isSaving)
                    }
                }
                PatientButton("Salir del ejercicio", { confirmExit = true }, Modifier.fillMaxWidth(), secondary = true)
                }
            }
            "complete" -> {
                Spacer(Modifier.height(16.dp))
                PatientCard {
                    Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        Box(Modifier.size(156.dp).background(PatientMint, CircleShape), contentAlignment = Alignment.Center) {
                            Box(Modifier.size(110.dp).background(SecondaryTeal, CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Check, null, Modifier.size(60.dp), tint = Color.White)
                            }
                        }
                        Text("¡Rutina completada!", style = MaterialTheme.typography.headlineMedium, color = PrimaryNavy, textAlign = TextAlign.Center)
                        Text("Te regalaste un momento de calma. Cada pequeño paso cuenta.", color = SlateSubtle, textAlign = TextAlign.Center)
                        PatientBadge("✓ Actividades completadas hoy: ${state.routines.count { it.completed }}")
                        PatientButton("Registrar cómo me siento", { onNavigateToRoute(NavRoutes.HOME) }, Modifier.fillMaxWidth())
                        PatientButton("Volver a mis rutinas", { page = "list" }, Modifier.fillMaxWidth(), secondary = true)
                    }
                }
            }
            else -> {
                PatientHeading("Mis rutinas", "Pequeños hábitos para acompañar tu día.")
                PatientBadge("${state.routines.count { it.completed }} de ${state.routines.size} completadas hoy")
                if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = SecondaryTeal)
                if (state.error != null) PatientButton("Volver a cargar", { viewModel.loadRoutines(patientId) }, Modifier.fillMaxWidth(), secondary = true, enabled = !state.isSaving)
                if (!state.isLoading && state.error == null && state.routines.isEmpty()) PatientCard {
                    Text("Aún no tienes rutinas", style = MaterialTheme.typography.titleLarge, color = PrimaryNavy)
                    PatientInfo("Crea tu primera actividad. Solo aparecerá en tu cuenta.")
                }
                state.routines.forEach { routine ->
                    RoutineCard(routine, enabled = !state.isSaving && !state.isLoading,
                        onEdit = { routineId = routine.id; page = "edit" }, onDelete = { deleteId = routine.id },
                        onToggle = { viewModel.toggleReminder(routine.id) }, onStart = {
                        routineId = routine.id
                        step = 0
                        page = if (routine.completed) "complete" else "exercise"
                    })
                }
                PatientButton("+ Crear nueva rutina", { page = "create" }, Modifier.fillMaxWidth(), enabled = !state.isSaving && !state.isLoading)
                PatientCard(color = PatientMint) {
                    PatientInfo("No necesitas hacerlo perfecto. Una pausa breve también cuenta.", Icons.Outlined.Spa)
                    PatientInfo("Los recordatorios se muestran aquí; todavía no se envían notificaciones.")
                }
            }
        }
    }
    deleteId?.let { id ->
        val routine = state.routines.firstOrNull { it.id == id }
        AlertDialog(
            onDismissRequest = { if (!state.isSaving) deleteId = null },
            title = { Text("¿Eliminar rutina?") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Se eliminará «${routine?.title.orEmpty()}» de tu cuenta. Esta acción no se puede deshacer.")
                state.error?.let { Text(it, color = ErrorRed) }
            } },
            confirmButton = { TextButton(onClick = { viewModel.delete(id) { deleteId = null } }, enabled = !state.isSaving) { Text(if (state.isSaving) "Eliminando…" else "Eliminar", color = ErrorRed) } },
            dismissButton = { TextButton(onClick = { deleteId = null }, enabled = !state.isSaving) { Text("Cancelar") } }
        )
    }
    if (confirmExit) AlertDialog(
        onDismissRequest = { confirmExit = false },
        title = { Text("¿Salir del ejercicio?") },
        text = { Text("El progreso de esta actividad no se guardará como completado.") },
        confirmButton = { TextButton(onClick = { confirmExit = false; page = "list" }) { Text("Salir") } },
        dismissButton = { TextButton(onClick = { confirmExit = false }) { Text("Continuar") } }
    )
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun RoutineCard(routine: Routine, enabled: Boolean, onEdit: () -> Unit, onDelete: () -> Unit, onToggle: () -> Unit, onStart: () -> Unit) {
    PatientCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(routine.title, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, color = PrimaryNavy)
            Switch(checked = routine.reminder, onCheckedChange = { onToggle() }, enabled = enabled, modifier = Modifier.minimumInteractiveComponentSize().semantics { contentDescription = "Recordatorio de ${routine.title}" })
        }
        if (routine.description.isNotBlank()) PatientInfo(routine.description)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PatientBadge(routine.time)
            PatientBadge("${routine.minutes} min", SurfaceContainerLow, PrimaryNavy)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            dayLabels.forEachIndexed { index, label ->
                Surface(shape = CircleShape, color = if (index in routine.days) SecondaryTeal else PatientBorder) {
                    Text(label, Modifier.padding(horizontal = 9.dp, vertical = 5.dp), color = if (index in routine.days) Color.White else SlateSubtle, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        PatientButton(if (routine.completed) "✓ Completada" else "Comenzar", onStart, Modifier.fillMaxWidth(), secondary = routine.completed, enabled = enabled)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onEdit, enabled = enabled) { Icon(Icons.Outlined.Edit, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Editar") }
            TextButton(onClick = onDelete, enabled = enabled) { Icon(Icons.Outlined.DeleteOutline, null, Modifier.size(18.dp), tint = ErrorRed); Spacer(Modifier.width(8.dp)); Text("Eliminar", color = ErrorRed) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RoutineForm(initial: Routine?, isSaving: Boolean, onSave: (String, String, String, Int, Set<Int>, Boolean, RoutineKind) -> Unit, onCancel: () -> Unit) {
    var title by rememberSaveable { mutableStateOf(initial?.title.orEmpty()) }
    var description by rememberSaveable { mutableStateOf(initial?.description.orEmpty()) }
    var time by rememberSaveable { mutableStateOf(initial?.time ?: "09:00") }
    var minutes by rememberSaveable { mutableStateOf((initial?.minutes ?: 5).toString()) }
    var days by rememberSaveable { mutableStateOf(initial?.days?.toList() ?: listOf(0, 1, 2, 3, 4)) }
    var reminder by rememberSaveable { mutableStateOf(initial?.reminder ?: true) }
    var kind by rememberSaveable { mutableStateOf(initial?.kind ?: RoutineKind.GROUNDING) }
    var attempted by rememberSaveable { mutableStateOf(false) }
    val validTime = Regex("([01][0-9]|2[0-3]):[0-5][0-9]").matches(time)
    val validMinutes = (minutes.toIntOrNull() ?: 0) in 1..60
    PatientHeading(if (initial == null) "Nueva rutina" else "Editar rutina", "Haz espacio para algo que te haga bien.")
    PatientCard {
        OutlinedTextField(title, { if (it.length <= 60) title = it }, label = { Text("Nombre de la actividad") }, modifier = Modifier.fillMaxWidth(), enabled = !isSaving, singleLine = true, isError = attempted && title.isBlank(), supportingText = { if (attempted && title.isBlank()) Text("Escribe un nombre para tu rutina.") })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(time, { time = it }, label = { Text("Hora (24 h)") }, modifier = Modifier.weight(1f), enabled = !isSaving, singleLine = true, isError = attempted && !validTime, supportingText = { if (attempted && !validTime) Text("Usa HH:mm") })
            OutlinedTextField(minutes, { minutes = it }, label = { Text("Minutos") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), enabled = !isSaving, singleLine = true, isError = attempted && !validMinutes, supportingText = { if (attempted && !validMinutes) Text("Entre 1 y 60") })
        }
        PatientInfo("Hora de Lima · UTC−5")
        OutlinedTextField(description, { if (it.length <= 1000) description = it }, label = { Text("Descripción (opcional)") }, modifier = Modifier.fillMaxWidth(), enabled = !isSaving, minLines = 3)
        Text("Tipo de ejercicio", color = PrimaryNavy, fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(kind == RoutineKind.GROUNDING, { kind = RoutineKind.GROUNDING }, label = { Text("Conectar con mis sentidos") }, enabled = !isSaving)
            FilterChip(kind == RoutineKind.BREATHING, { kind = RoutineKind.BREATHING }, label = { Text("Respiración guiada") }, enabled = !isSaving)
        }
        Text("Repetir los días", color = PrimaryNavy, fontWeight = FontWeight.SemiBold)
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            dayLabels.forEachIndexed { index, label ->
                FilledIconToggleButton(checked = index in days, onCheckedChange = { days = if (index in days) days - index else days + index }, modifier = Modifier.semantics { contentDescription = dayNames[index] }, enabled = !isSaving) {
                    Text(label, modifier = Modifier, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        if (attempted && days.isEmpty()) Text("Selecciona al menos un día.", color = ErrorRed, style = MaterialTheme.typography.bodySmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Recordarme", Modifier.weight(1f), color = PrimaryNavy)
            Switch(reminder, { reminder = it }, enabled = !isSaving)
        }
        PatientInfo("Puedes cambiar esta preferencia desde la lista de rutinas.")
    }
    PatientButton(if (isSaving) "Guardando…" else "Guardar rutina", {
        attempted = true
        if (title.isNotBlank() && validTime && validMinutes && days.isNotEmpty()) onSave(title, description, time, minutes.toInt(), days.toSet(), reminder, kind)
    }, Modifier.fillMaxWidth(), enabled = !isSaving)
    PatientButton("Cancelar", onCancel, Modifier.fillMaxWidth(), secondary = true, enabled = !isSaving)
}
