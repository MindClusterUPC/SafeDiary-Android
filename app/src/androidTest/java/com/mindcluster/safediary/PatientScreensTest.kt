package com.mindcluster.safediary

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.mindcluster.safediary.home.presentation.views.HomeScreen
import com.mindcluster.safediary.professionals.presentation.views.ProfessionalsScreen
import com.mindcluster.safediary.rutines.presentation.views.RutinesScreen
import com.mindcluster.safediary.scheduling.presentation.views.SchedulingScreen
import com.mindcluster.safediary.shared.presentation.theme.SafeDiaryTheme
import com.mindcluster.safediary.shared.presentation.components.SafeDiaryBottomNavBar
import com.mindcluster.safediary.shared.presentation.navigation.NavRoutes
import org.junit.Rule
import org.junit.Test
import java.io.File

class PatientScreensTest {
    @get:Rule val compose = createComposeRule()

    @Test fun homeRecordsMood() {
        compose.setContent { TestPatientShell(NavRoutes.HOME) { HomeScreen(onNavigateToRoute = {}) } }
        compose.onNodeWithText("En paz").performClick()
        compose.onNodeWithText("Ánimo registrado:", substring = true).assertExists()
        screenshot("home")
    }

    @Test fun directorySearchAndProfile() {
        compose.setContent { TestPatientShell(NavRoutes.PROFESSIONALS) { ProfessionalsScreen(onNavigateToRoute = {}) } }
        screenshot("psychologists")
        compose.onNode(hasSetTextAction()).performTextInput("gomez")
        compose.onNodeWithText("Dra. Laura Gómez").assertExists()
        compose.onNodeWithText("Ver perfil").performScrollTo().performClick()
        compose.onNodeWithText("Perfil profesional").assertExists()
        screenshot("professional-profile")
    }

    @Test fun routineValidationAndGuidedExercise() {
        compose.setContent { TestPatientShell(NavRoutes.RUTINES) { RutinesScreen(onNavigateToRoute = {}) } }
        screenshot("routines")
        compose.onAllNodesWithText("Comenzar")[1].performScrollTo().performClick()
        repeat(4) { compose.onNodeWithText("Siguiente paso").performScrollTo().performClick() }
        compose.onNodeWithText("Finalizar ejercicio").performScrollTo().performClick()
        compose.onNodeWithText("¡Rutina completada!").assertExists()
        screenshot("routine-completed")
        compose.onNodeWithText("Volver a mis rutinas").performScrollTo().performClick()
        compose.onNodeWithText("+ Crear nueva rutina").performScrollTo().performClick()
        compose.onNodeWithText("Guardar rutina").performScrollTo().performClick()
        compose.onNodeWithText("Escribe un nombre para tu rutina.").assertExists()
        screenshot("routine-form")
    }

    @Test fun paymentPreviewConfirmsReservationAndConsentIsOptional() {
        compose.setContent { TestPatientShell(NavRoutes.SCHEDULING) { SchedulingScreen(onNavigateToRoute = {}) } }
        screenshot("scheduling")
        compose.onNodeWithText("Pagar S/ 120").performScrollTo().performClick()
        compose.onNodeWithText("Simular pago aprobado").performScrollTo().performClick()
        compose.onNodeWithText("Cita confirmada").assertExists()
        screenshot("appointment-confirmed")
        compose.onNodeWithText("Configurar consentimiento").performScrollTo().performClick()
        compose.onNodeWithText("Tú decides qué compartir").assertExists()
        screenshot("consent")
    }

    private fun screenshot(name: String) {
        compose.waitForIdle()
        val directory = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), "patient-screens").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use { compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}

@Composable
private fun TestPatientShell(route: String, content: @Composable () -> Unit) {
    SafeDiaryTheme {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) { content() }
            SafeDiaryBottomNavBar(route, {})
        }
    }
}
