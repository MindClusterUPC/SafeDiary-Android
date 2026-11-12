package com.mindcluster.safediary.scheduling.presentation.views

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindcluster.safediary.scheduling.presentation.components.*
import com.mindcluster.safediary.scheduling.presentation.viewmodels.ClinicianAgendaViewModel
import com.mindcluster.safediary.shared.presentation.components.PatientCard
import com.mindcluster.safediary.shared.presentation.components.PatientMint
import com.mindcluster.safediary.shared.presentation.components.PatientScreen
import com.mindcluster.safediary.shared.presentation.theme.*
import kotlinx.coroutines.delay

/** Psychologist side of the Scheduling tab (designs 03 and 10). */
@Composable
fun ClinicianAgendaScreen(
    clinicianId: Long,
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = {},
    isActive: Boolean = true,
    viewModel: ClinicianAgendaViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var page by rememberSaveable { mutableStateOf("agenda") }
    var chatId by rememberSaveable { mutableLongStateOf(-1L) }
    var appointmentId by rememberSaveable { mutableLongStateOf(-1L) }
    val chat = state.request(chatId)
    var message by rememberSaveable(chatId) { mutableStateOf("") }
    ObserveCoordinationUpdates(isActive, chat?.id?.takeIf { page == "chat" }) {
        if (!state.isWorking && !state.isLoading) { viewModel.loadMessages(chatId, silent = true); viewModel.refresh() }
    }
    val appointment = state.appointment(appointmentId)

    LaunchedEffect(clinicianId) { viewModel.start(clinicianId) }
    LaunchedEffect(isActive) {
        if (isActive) {
            viewModel.refresh()
            while (true) { viewModel.tick(); delay(1000) }
        }
    }
    LaunchedEffect(state.joinUrl) {
        state.joinUrl?.let { url ->
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: ActivityNotFoundException) {
                viewModel.showError("No encontramos una aplicación para abrir la videollamada.")
            }
            viewModel.consumeJoinUrl()
        }
    }
    fun back() { page = if (page == "close") "detail" else "agenda" }
    BackHandler(isActive && page != "agenda") { back() }

    PatientScreen("Scheduling", modifier, onBack = if (page != "agenda") ({ back() }) else null, onSettings = onNavigateToSettings, contentKey = page,
        scrollToBottomKey = if (page == "chat") state.messages[chatId]?.lastOrNull()?.id ?: 0L else null,
        bottomBar = if (page == "chat" && chat?.isOpen == true) ({
            CoordinationComposer(message, { message = it }, state.isWorking) {
                viewModel.sendMessage(chat.id, message); message = ""
            }
        }) else null
    ) {
        state.notice?.let { notice ->
            PatientCard(color = PatientMint) {
                Text(notice, color = PrimaryNavy)
                TextButton(onClick = viewModel::dismissNotice) { Text("Entendido") }
            }
        }
        when {
            page == "chat" && chat != null -> ClinicianChatContent(
                request = chat,
                messages = state.messages[chat.id].orEmpty(),
                proposal = state.proposalFor(chat.id),
                isWorking = state.isWorking,
                onProposeSchedule = { viewModel.selectRequest(chat.id); page = "agenda" }
            )
            page == "detail" && appointment != null -> ClinicianAppointmentContent(
                appointment, state.now, state.isWorking,
                onJoin = { viewModel.join(appointment.id) },
                onClose = { viewModel.loadSession(appointment.id); page = "close" }
            )
            page == "close" && appointment != null -> CloseAttentionContent(
                appointment, state.sessions[appointment.id], state.now, state.isWorking,
                onBackToSession = { viewModel.join(appointment.id) },
                onClose = { outcome -> viewModel.close(appointment.id, outcome) { page = "detail" } }
            )
            else -> ClinicianAgendaContent(
                state = state,
                onReject = viewModel::reject,
                onOpenChat = { id -> chatId = id; page = "chat"; viewModel.loadMessages(id) },
                onSelectRequest = viewModel::selectRequest,
                onShiftWeek = viewModel::shiftWeek,
                onSelectDay = viewModel::selectDay,
                onSelectSlot = viewModel::selectSlot,
                onPropose = viewModel::propose,
                onOpenAppointment = { id -> appointmentId = id; page = "detail"; viewModel.loadSession(id) },
                onRefresh = viewModel::refresh
            )
        }
    }
    state.actionError?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            title = { Text("No se pudo completar") },
            text = { Text(error) },
            confirmButton = { TextButton(onClick = viewModel::dismissError) { Text("Entendido") } }
        )
    }
}
