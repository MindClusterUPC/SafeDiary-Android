package com.mindcluster.safediary.professionals.presentation.views

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindcluster.safediary.professionals.infrastructure.local.PreviewProfessionals
import com.mindcluster.safediary.professionals.presentation.components.ProfessionalCard
import com.mindcluster.safediary.professionals.presentation.viewmodels.ProfessionalsViewModel
import com.mindcluster.safediary.shared.presentation.components.*
import com.mindcluster.safediary.shared.presentation.theme.*

@Composable
fun ProfessionalsScreen(
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = {},
    isActive: Boolean = true,
    onContactProfessional: (String, String) -> Unit = { _, _ -> },
    viewModel: ProfessionalsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var page by rememberSaveable { mutableStateOf("directory") }
    var professionalId by rememberSaveable { mutableStateOf("laura") }
    var contactMessage by rememberSaveable { mutableStateOf("") }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    var helpful by rememberSaveable { mutableStateOf(false) }
    var report by rememberSaveable { mutableStateOf(false) }
    var reportReason by rememberSaveable { mutableStateOf("") }
    var reportSent by rememberSaveable { mutableStateOf(false) }
    val professional = PreviewProfessionals.find(professionalId)
    fun back() { page = if (page == "contact" || page == "reviews") "profile" else "directory" }
    BackHandler(isActive && page != "directory") { back() }

    PatientScreen("Psicólogos", modifier, onBack = if (page != "directory") ({ back() }) else null, onSettings = onNavigateToSettings, contentKey = page) {
        when (page) {
            "profile" -> {
                PatientHeading("Perfil profesional", "Información para elegir con confianza.")
                PatientCard {
                    PatientAvatar(professional.initials)
                    Text(professional.name, style = MaterialTheme.typography.headlineSmall, color = PrimaryNavy)
                    PatientInfo(professional.specialty)
                    PatientBadge("✓ Licencia profesional verificada")
                    PatientInfo("★ ${professional.rating} · ${professional.reviewCount} reseñas")
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        professional.tags.forEach { PatientBadge(it, SurfaceContainerLow, PrimaryNavy) }
                    }
                    Text("S/ ${professional.price} por sesión de 50 min", style = MaterialTheme.typography.titleMedium, color = PrimaryNavy)
                }
                Text("Sobre su enfoque", style = MaterialTheme.typography.titleLarge, color = PrimaryNavy)
                PatientCard { Text(professional.approach, color = PrimaryNavy) }
                PatientCard {
                    PatientInfo("Credenciales revisadas antes de publicar la ficha.", Icons.Outlined.VerifiedUser)
                    PatientInfo("Atiende en ${professional.languages.lowercase()}.")
                }
                Text("Valoraciones", style = MaterialTheme.typography.titleLarge, color = PrimaryNavy)
                PatientCard {
                    Text("★ ${professional.rating} de 5 · ${professional.reviewCount} reseñas", color = PrimaryNavy)
                    Text("«Me sentí escuchada y con un plan claro.»", color = PrimaryNavy)
                    PatientInfo("Reseña anónima de una sesión completada")
                    TextButton(onClick = { page = "reviews" }) { Text("Ver todas las reseñas") }
                }
                PatientButton("Solicitar contacto", { page = "contact" }, Modifier.fillMaxWidth())
                PatientInfo("Inicia una coordinación privada; no crea una cita ni un cobro.")
            }
            "contact" -> {
                PatientHeading("Solicitar contacto", "Cuéntale brevemente qué apoyo estás buscando.")
                PatientCard {
                    PatientAvatar(professional.initials)
                    Text(professional.name, color = PrimaryNavy, style = MaterialTheme.typography.titleLarge)
                    PatientBadge("✓ Ficha verificada")
                    OutlinedTextField(contactMessage, { contactMessage = it }, label = { Text("Motivo de consulta") }, placeholder = { Text("Hola, me gustaría coordinar una primera sesión…") }, modifier = Modifier.fillMaxWidth(), minLines = 5)
                    PatientInfo("Comparte solo lo que te resulte cómodo. Tu diario no se adjunta.", Icons.Outlined.Lock)
                }
                PatientBadge("Vista previa · no se envían mensajes reales", SurfaceContainerLow, PrimaryNavy)
                PatientButton("Continuar al chat", {
                    onContactProfessional(professional.id, contactMessage.trim())
                    page = "directory"
                    contactMessage = ""
                }, Modifier.fillMaxWidth(), enabled = contactMessage.isNotBlank())
                PatientButton("Volver al perfil", { page = "profile" }, Modifier.fillMaxWidth(), secondary = true)
            }
            "reviews" -> {
                PatientHeading("Valoraciones y reseñas", professional.name)
                PatientCard {
                    Text("★ ${professional.rating} de 5", style = MaterialTheme.typography.headlineMedium, color = PrimaryNavy)
                    PatientInfo("${professional.reviewCount} reseñas de sesiones completadas")
                    (5 downTo 1).forEach { stars ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("$stars ★", color = PrimaryNavy)
                            LinearProgressIndicator(progress = { if (stars == 5) .84f else if (stars == 4) .12f else .02f }, modifier = Modifier.weight(1f).padding(top = 8.dp), color = SecondaryTeal)
                        }
                    }
                }
                PatientCard {
                    PatientBadge("Sesión verificada")
                    Text("Paciente anónimo · ★★★★★", color = PrimaryNavy)
                    Text("Me sentí escuchada y con un plan claro. Pude expresarme a mi ritmo.", color = PrimaryNavy)
                    Row {
                        TextButton(onClick = { helpful = !helpful }) { Text(if (helpful) "✓ Útil (13)" else "Me resulta útil (12)") }
                        TextButton(onClick = { report = true }) { Text("Denunciar") }
                    }
                }
                PatientInfo("Las reseñas se muestran de forma anónima.")
                if (reportSent) PatientBadge("Denuncia recibida para revisión")
            }
            else -> {
                PatientHeading("Psicólogos y especialistas verificados")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(state.query, viewModel::search, modifier = Modifier.weight(1f), placeholder = { Text("Nombre o especialidad") }, singleLine = true, leadingIcon = { Icon(Icons.Outlined.Search, null) }, shape = MaterialTheme.shapes.extraLarge)
                    IconButton(onClick = { showFilters = true }, modifier = Modifier.padding(top = 4.dp)) { Icon(Icons.Outlined.Tune, "Abrir filtros", tint = PrimaryNavy) }
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Todos", "Ansiedad y estrés", "Atención plena", "Bienestar integral").forEach { category ->
                        FilterChip(state.category == category, { viewModel.category(category) }, label = { Text(category) })
                    }
                }
                PatientInfo("Perfiles de muestra · ${state.results.size} resultados")
                if (state.results.isEmpty()) PatientCard {
                    PatientHeading("Sin resultados", "Prueba otro nombre o cambia los filtros.")
                    PatientButton("Limpiar filtros", viewModel::clearFilters, Modifier.fillMaxWidth(), secondary = true)
                }
                state.results.forEach { item ->
                    ProfessionalCard(item, item.id in state.favorites, { viewModel.toggleFavorite(item.id) }, {
                        professionalId = item.id; page = "profile"
                    }, {
                        professionalId = item.id; page = "contact"
                    })
                }
            }
        }
    }
    if (showFilters) AlertDialog(
        onDismissRequest = { showFilters = false },
        title = { Text("Filtrar especialistas") },
        text = {
            Column {
                Row { Checkbox(state.affordableOnly, viewModel::affordable); Text("Hasta S/ 100 por sesión", Modifier.padding(top = 12.dp)) }
                Row { Checkbox(state.favoritesOnly, viewModel::favoritesOnly); Text("Solo guardados", Modifier.padding(top = 12.dp)) }
            }
        },
        confirmButton = { TextButton(onClick = { showFilters = false }) { Text("Ver resultados") } },
        dismissButton = { TextButton(onClick = { viewModel.clearFilters(); showFilters = false }) { Text("Limpiar") } }
    )
    if (report) AlertDialog(
        onDismissRequest = { report = false },
        title = { Text("Denunciar reseña") },
        text = {
            Column {
                Text("Elige un motivo. La reseña seguirá visible mientras se revisa.")
                listOf("Contenido ofensivo", "Información personal", "Publicidad o contenido falso").forEach { reason ->
                    Row { RadioButton(reportReason == reason, { reportReason = reason }); Text(reason, Modifier.padding(top = 12.dp)) }
                }
                PatientInfo("Vista previa: esta denuncia no se envía.")
            }
        },
        confirmButton = { TextButton(enabled = reportReason.isNotBlank(), onClick = { report = false; reportSent = true }) { Text("Confirmar") } },
        dismissButton = { TextButton(onClick = { report = false }) { Text("Cancelar") } }
    )
}
