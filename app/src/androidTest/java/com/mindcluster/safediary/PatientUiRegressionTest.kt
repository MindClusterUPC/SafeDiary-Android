package com.mindcluster.safediary

import android.graphics.Bitmap
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.mindcluster.safediary.rutines.domain.model.Routine
import com.mindcluster.safediary.rutines.domain.model.RoutineKind
import com.mindcluster.safediary.rutines.presentation.views.RoutineCard
import com.mindcluster.safediary.rutines.presentation.views.RoutineForm
import com.mindcluster.safediary.scheduling.presentation.components.CoordinationMessageInput
import com.mindcluster.safediary.scheduling.presentation.components.CoordinationComposer
import com.mindcluster.safediary.scheduling.presentation.components.CoordinationContent
import com.mindcluster.safediary.scheduling.domain.model.*
import com.mindcluster.safediary.shared.presentation.components.PatientScreen
import com.mindcluster.safediary.shared.presentation.theme.BackgroundSanctuary
import com.mindcluster.safediary.shared.presentation.theme.SafeDiaryTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class PatientUiRegressionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private fun screenshot(tag: String, name: String) {
        val bitmap = compose.onNodeWithTag(tag).captureToImage().asAndroidBitmap()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        File(context.getExternalFilesDir(null), name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun fullChatKeepsComposerBelowMessagesWithKeyboardAndHasNoRefreshButton() {
        compose.activityRule.scenario.onActivity {
            it.enableEdgeToEdge()
            it.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
        val messages = mutableStateOf(listOf(CoordinationMessage(1, "Hola, quisiera coordinar una cita.", true, 0)))
        compose.setContent {
            var draft by remember { mutableStateOf("") }
            SafeDiaryTheme(darkTheme = false) {
                PatientScreen("Agenda", Modifier.testTag("full-chat"), onBack = {},
                    bottomBar = { CoordinationComposer(draft, { draft = it }, false) {} },
                    scrollToBottomKey = messages.value.last().id
                ) {
                    CoordinationContent(ContactRequest(1, 2, ContactRequestStatus.PENDING),
                        ClinicianSummary(2, "Demo - Ana Torres", "AT"), messages.value, null, null, false, { draft = it }, {}, {})
                }
            }
        }
        compose.onNodeWithText("Actualizar conversación").assertDoesNotExist()
        val field = compose.onNode(hasSetTextAction()).assertIsDisplayed()
        screenshot("full-chat", "qa-full-coordination-chat.png")
        field.performClick().performTextInput("Disponible por la tarde")
        compose.onNodeWithContentDescription("Enviar mensaje").assertIsDisplayed().assertIsEnabled()
        compose.runOnIdle {
            messages.value = (1L..20L).map { CoordinationMessage(it, "Mensaje de prueba $it", true, 0) }
        }
        compose.onNodeWithText("Mensaje de prueba 20").assertIsDisplayed()
        val last = compose.onNodeWithText("Mensaje de prueba 20").fetchSemanticsNode().boundsInRoot
        val input = field.assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue(last.bottom <= input.top)
        screenshot("full-chat", "qa-full-coordination-chat-keyboard.png")
    }

    @Test fun coordinationInputFitsNarrowScreenAndSendsEditedMessage() {
        var sent = ""
        compose.setContent {
            var message by remember { mutableStateOf("Quisiera coordinar una cita") }
            SafeDiaryTheme(darkTheme = false) {
                Column(Modifier.width(320.dp).background(BackgroundSanctuary).padding(16.dp).testTag("composer")) {
                    CoordinationMessageInput(message, { message = it }, false) { sent = message }
                }
            }
        }
        val field = compose.onNode(hasSetTextAction()).assertIsDisplayed()
        val send = compose.onNodeWithContentDescription("Enviar mensaje").assertIsDisplayed().assertIsEnabled()
        assertTrue(send.fetchSemanticsNode().boundsInRoot.left >= field.fetchSemanticsNode().boundsInRoot.right)
        screenshot("composer", "qa-coordination-input.png")
        field.performTextReplacement("Mi disponibilidad es por la tarde")
        send.performClick()
        compose.runOnIdle { assertEquals("Mi disponibilidad es por la tarde", sent) }
    }

    @Test fun routineCardShowsEditAndDeleteWithoutOverflow() {
        var edits = 0; var deletes = 0
        compose.setContent {
            SafeDiaryTheme(darkTheme = false) {
                Column(Modifier.width(320.dp).background(BackgroundSanctuary).padding(12.dp).testTag("routine-card")) {
                    RoutineCard(Routine("1", "Mi pausa para respirar con calma", "Una pausa al terminar el día.", "18:30", 7, (0..6).toSet()),
                        true, { edits++ }, { deletes++ }, {}, {})
                }
            }
        }
        compose.onNodeWithText("Editar").assertIsDisplayed().performClick()
        compose.onNodeWithText("Eliminar").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, edits); assertEquals(1, deletes) }
        screenshot("routine-card", "qa-routine-card.png")
    }

    @Test fun editFormKeepsExistingDetailsWhenOnlyTitleChanges() {
        val original = Routine("1", "Mi pausa", "Respirar despacio", "18:30", 7, setOf(0, 4), false, kind = RoutineKind.BREATHING)
        var saved: Routine? = null
        compose.setContent {
            SafeDiaryTheme(darkTheme = false) {
                PatientScreen("Rutinas") {
                    RoutineForm(original, false, { title, description, time, minutes, days, reminder, kind ->
                        saved = Routine("1", title, description, time, minutes, days, reminder, kind = kind)
                    }, {})
                }
            }
        }
        compose.onNodeWithText("Editar rutina").assertIsDisplayed()
        compose.onNode(hasSetTextAction() and hasText("Mi pausa")).performTextReplacement("Mi pausa editada")
        compose.onNodeWithText("Guardar rutina").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(original.copy(title = "Mi pausa editada"), saved) }
    }
}
