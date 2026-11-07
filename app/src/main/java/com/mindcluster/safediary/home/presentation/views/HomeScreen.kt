package com.mindcluster.safediary.home.presentation.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindcluster.safediary.home.presentation.viewmodels.HomeViewModel
import com.mindcluster.safediary.shared.presentation.components.*
import com.mindcluster.safediary.shared.presentation.navigation.NavRoutes
import com.mindcluster.safediary.shared.presentation.theme.*

private data class MoodOption(val title: String, val caption: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: Color)
private val moods = listOf(
    MoodOption("En paz", "Serenidad", Icons.Outlined.Spa, Color(0xFFE0F3ED)),
    MoodOption("Feliz", "Alegría", Icons.Outlined.SentimentSatisfiedAlt, Color(0xFFFFF5D9)),
    MoodOption("En calma", "Tranquilidad", Icons.Outlined.Air, Color(0xFFE5F4FF)),
    MoodOption("Con cariño", "Gratitud", Icons.Outlined.FavoriteBorder, Color(0xFFFFE9ED)),
    MoodOption("Con ansiedad", "Inquietud", Icons.Outlined.Psychology, Color(0xFFF0E9FF)),
    MoodOption("Triste", "Reflexión", Icons.Outlined.NightsStay, Color(0xFFE9EEFF))
)

@Composable
fun HomeScreen(
    onNavigateToRoute: (String) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onOpenDiary: () -> Unit = { onNavigateToRoute(NavRoutes.AI_CHAT) },
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val selected by viewModel.mood.collectAsStateWithLifecycle()
    PatientScreen("Inicio", modifier, onSettings = onNavigateToProfile) {
        PatientHeading("Un momento para ti", "¿Cómo se siente tu corazón hoy?")
        PatientCard {
            Text("Tu estado de ánimo", style = MaterialTheme.typography.titleLarge, color = PrimaryNavy)
            PatientInfo("Elige cómo te sientes en este momento")
            moods.chunked(3).forEach { options ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { option ->
                        val active = selected == option.title
                        Surface(
                            onClick = { viewModel.selectMood(option.title) }, selected = active,
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp),
                            color = if (active) SecondaryTeal else option.color,
                            contentColor = if (active) Color.White else PrimaryNavy,
                            border = BorderStroke(1.dp, if (active) SecondaryTeal else option.color)
                        ) {
                            Column(Modifier.padding(horizontal = 4.dp, vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(option.icon, null, Modifier.size(28.dp))
                                Text(option.title, style = MaterialTheme.typography.labelMedium)
                                Text(option.caption, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            if (selected != null) Surface(color = SurfaceContainerLow, shape = RoundedCornerShape(14.dp)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.CheckCircle, null, tint = SecondaryTeal)
                    Text("Ánimo registrado: $selected. Este espacio es solo tuyo.", color = PrimaryNavy, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        PatientCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.EditNote, null, Modifier.size(40.dp), tint = SecondaryTeal)
                Column(Modifier.weight(1f)) {
                    Text("Escribe en tu diario", style = MaterialTheme.typography.titleLarge, color = PrimaryNavy)
                    PatientInfo("Dale espacio a lo que sientes, a tu ritmo.")
                }
            }
            PatientButton("Abrir Diario", onOpenDiary, Modifier.fillMaxWidth())
        }
        PatientCard(color = SurfaceContainerLow) {
            PatientBadge("✧ INSPIRACIÓN DEL DÍA", SurfaceContainerHigh, PrimaryNavy)
            Text("Cada pequeña pausa también es una forma de cuidarte.", style = MaterialTheme.typography.titleLarge, color = PrimaryNavy)
            Text("No tienes que resolverlo todo hoy.", style = MaterialTheme.typography.bodySmall, color = SlateSubtle)
        }
        Text("Continúa cuidándote", style = MaterialTheme.typography.titleLarge, color = PrimaryNavy)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PatientButton("Mis rutinas", { onNavigateToRoute(NavRoutes.RUTINES) }, Modifier.weight(1f), secondary = true)
            PatientButton("Mis citas", { onNavigateToRoute(NavRoutes.SCHEDULING) }, Modifier.weight(1f), secondary = true)
        }
    }
}
