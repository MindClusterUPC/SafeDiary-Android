package com.mindcluster.safediary

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.SavedStateHandle
import com.mindcluster.safediary.home.presentation.viewmodels.HomeViewModel
import com.mindcluster.safediary.home.presentation.views.HomeScreen
import com.mindcluster.safediary.professionals.presentation.viewmodels.ProfessionalsViewModel
import com.mindcluster.safediary.professionals.presentation.views.ProfessionalsScreen
import com.mindcluster.safediary.rutines.presentation.viewmodels.RutinesViewModel
import com.mindcluster.safediary.rutines.presentation.views.RutinesScreen
import com.mindcluster.safediary.scheduling.presentation.viewmodels.SchedulingViewModel
import com.mindcluster.safediary.scheduling.presentation.views.SchedulingScreen
import com.mindcluster.safediary.shared.presentation.components.SafeDiaryBottomNavBar
import com.mindcluster.safediary.shared.presentation.navigation.NavRoutes
import com.mindcluster.safediary.shared.presentation.theme.SafeDiaryTheme

@Composable
private fun PatientPreview(route: String, content: @Composable () -> Unit) {
    SafeDiaryTheme {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) { content() }
            SafeDiaryBottomNavBar(route, {})
        }
    }
}

@Preview(name = "Inicio", widthDp = 393, heightDp = 852, group = "Paciente")
@Composable
private fun HomePreview() = PatientPreview(NavRoutes.HOME) { HomeScreen({}, viewModel = HomeViewModel(SavedStateHandle())) }

@Preview(name = "Rutinas", widthDp = 393, heightDp = 852, group = "Paciente")
@Composable
private fun RoutinesPreview() = PatientPreview(NavRoutes.RUTINES) { RutinesScreen({}, viewModel = RutinesViewModel()) }

@Preview(name = "Psicólogos", widthDp = 393, heightDp = 852, group = "Paciente")
@Composable
private fun ProfessionalsPreview() = PatientPreview(NavRoutes.PROFESSIONALS) { ProfessionalsScreen({}, viewModel = ProfessionalsViewModel()) }

@Preview(name = "Mis citas", widthDp = 393, heightDp = 852, group = "Paciente")
@Composable
private fun SchedulingPreview() = PatientPreview(NavRoutes.SCHEDULING) { SchedulingScreen({}, isActive = false, viewModel = SchedulingViewModel()) }
