package com.mindcluster.safediary.scheduling.presentation.views

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindcluster.safediary.payments.presentation.components.StripeCheckoutWebViewDialog
import com.mindcluster.safediary.scheduling.domain.model.*
import com.mindcluster.safediary.scheduling.presentation.components.*
import com.mindcluster.safediary.scheduling.presentation.viewmodels.AgendaMode
import com.mindcluster.safediary.scheduling.presentation.viewmodels.SchedulingViewModel
import com.mindcluster.safediary.shared.presentation.components.*
import com.mindcluster.safediary.shared.presentation.navigation.NavRoutes
import com.mindcluster.safediary.shared.presentation.theme.*
import kotlinx.coroutines.delay

@Composable
fun SchedulingScreen(
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = {},
    isActive: Boolean = true,
    viewModel: SchedulingViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var page by rememberSaveable { mutableStateOf("list") }
    var appointmentId by rememberSaveable { mutableLongStateOf(-1L) }
    var chatId by rememberSaveable { mutableLongStateOf(-1L) }
    var tab by rememberSaveable { mutableStateOf("Próximas") }
    var cancelDialog by rememberSaveable { mutableStateOf(false) }
    var draftConsent by rememberSaveable { mutableStateOf(false) }
    val appointment = state.appointments.find { it.id == appointmentId }
    val chat = state.contactRequest(chatId)
    var message by rememberSaveable(chatId) { mutableStateOf("") }
    ObserveCoordinationUpdates(isActive, chat?.id?.takeIf { page == "chat" }) {
        if (!state.isWorking && !state.isLoading) viewModel.refreshConversation(chatId, silent = true)
    }

    LaunchedEffect(isActive) {
        if (isActive) {
            viewModel.refresh()
            while (true) { viewModel.tick(); delay(1000) }
        }
    }
    LaunchedEffect(state.activeConversationId) {
        state.activeConversationId?.let { chatId = it; page = "chat" }
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
    fun openChat(id: Long) { chatId = id; page = "chat"; viewModel.openConversation(id) }
    fun back() {
        page = if (page == "consent") "detail" else "list"
        viewModel.closeConversation()
    }
    val ownClinicianId = state.ownClinicianId
    if (state.mode == AgendaMode.PSYCHOLOGIST && ownClinicianId != null) {
        ClinicianAgendaScreen(ownClinicianId, modifier, onNavigateToSettings, isActive)
        return
    }
    BackHandler(isActive && page != "list") { back() }
    fun openAppointment(item: Appointment) {
        appointmentId = item.id
        page = if (item.status == AppointmentStatus.HELD || item.status == AppointmentStatus.EXPIRED) "reservation" else "detail"
    }
    PatientScreen("Agenda", modifier, onBack = if (page != "list") ({ back() }) else null, onSettings = onNavigateToSettings, contentKey = page,
        scrollToBottomKey = if (page == "chat") state.messages[chatId]?.lastOrNull()?.id ?: 0L else null,
        bottomBar = if (page == "chat" && chat?.isOpen == true) ({
            CoordinationComposer(message, { message = it }, state.isWorking) {
                viewModel.sendMessage(chat.id, message); message = ""
            }
        }) else null
    ) {
        when {
            page == "chat" && chat != null -> CoordinationContent(
                request = chat,
                clinician = state.clinician(chat.clinicianId),
                messages = state.messages[chat.id].orEmpty(),
                proposal = state.proposalFor(chat.id),
                held = state.heldFor(chat.id),
                isWorking = state.isWorking,
                onMessageChange = { message = it },
                onAccept = { proposalId -> viewModel.accept(proposalId) { heldId -> appointmentId = heldId; page = "reservation"; viewModel.closeConversation() } },
                onOpenReservation = { heldId -> appointmentId = heldId; page = "reservation"; viewModel.closeConversation() }
            )
            page == "reservation" && appointment != null &&
                (appointment.status == AppointmentStatus.HELD || appointment.status == AppointmentStatus.EXPIRED) ->
                ReservationContent(appointment, state.clinician(appointment.clinicianId), state.now,
                    isWorking = state.isWorking,
                    canSimulatePayment = viewModel.canSimulatePayment,
                    onSimulatePayment = { viewModel.simulatePayment(appointment.id) },
                    onPay = { viewModel.pay(appointment.id) },
                    onCancel = { cancelDialog = true },
                    onChat = { openChat(appointment.contactRequestId) }
                )
            page == "consent" && appointment != null -> {
                PatientHeading("Tú decides qué compartir", "El consentimiento es opcional.")
                PatientCard {
                    Text("Contexto para ${state.clinician(appointment.clinicianId).name}", style = MaterialTheme.typography.titleLarge, color = PrimaryNavy)
                    PatientInfo("Compartir un resumen emocional para preparar esta sesión. No incluye las conversaciones completas ni grabaciones.", Icons.Outlined.Lock)
                    Row {
                        Checkbox(draftConsent, { draftConsent = it })
                        Text("Permitir acceso al resumen", Modifier.weight(1f).padding(top = 12.dp), color = PrimaryNavy)
                    }
                    PatientInfo("Puedes revocarlo desde esta misma pantalla. Al desactivarlo, el profesional deja de tener acceso.")
                    PatientBadge("Vista previa · no se comparte ningún dato", SurfaceContainerLow, PrimaryNavy)
                }
                PatientButton("Guardar preferencia", { viewModel.shareContext(appointment.id, draftConsent); page = "detail" }, Modifier.fillMaxWidth())
                PatientButton("Continuar sin cambios", { page = "detail" }, Modifier.fillMaxWidth(), secondary = true)
            }
            (page == "detail" || page == "reservation") && appointment != null -> AppointmentDetailContent(
                appointment, state.clinician(appointment.clinicianId), state.now,
                sharedContext = appointment.id in state.sharedContext,
                isWorking = state.isWorking,
                onJoin = { viewModel.join(appointment.id) },
                onConsent = { draftConsent = appointment.id in state.sharedContext; page = "consent" },
                onCancel = { cancelDialog = true },
                onChat = { openChat(appointment.contactRequestId) }
            )
            state.mode == AgendaMode.PSYCHOLOGIST_WITHOUT_PROFILE -> {
                PatientHeading("Mi agenda")
                PatientCard {
                    PatientHeading("Completa tu ficha profesional", "Para recibir solicitudes y proponer horarios necesitas una ficha en el directorio de psicólogos.")
                    PatientInfo("Tu ficha debe estar verificada y publicada para que los pacientes puedan contactarte.", Icons.Outlined.Info)
                    PatientButton("Actualizar", viewModel::refresh, Modifier.fillMaxWidth(), secondary = true)
                }
            }
            state.mode != AgendaMode.PATIENT -> {
                PatientHeading("Agenda")
                PatientCard {
                    PatientHeading("Agenda no disponible para esta cuenta", "La agenda es para pacientes y psicólogos.")
                    PatientButton("Actualizar", viewModel::refresh, Modifier.fillMaxWidth(), secondary = true)
                }
            }
            else -> {
                PatientHeading("Mis citas", "Coordina, reserva y asiste a tus sesiones.")
                if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = SecondaryTeal)
                state.loadError?.let { error ->
                    PatientCard(color = ErrorContainer) {
                        Text(error, color = ErrorRed)
                        PatientButton("Reintentar", viewModel::refresh, Modifier.fillMaxWidth(), secondary = true)
                    }
                }
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    listOf("Próximas", "Pendientes", "Historial").forEachIndexed { index, label ->
                        SegmentedButton(selected = tab == label, onClick = { tab = label }, shape = SegmentedButtonDefaults.itemShape(index, 3)) { Text(label, style = MaterialTheme.typography.labelMedium) }
                    }
                }
                val filtered = state.appointments.filter { item ->
                    when (tab) {
                        "Próximas" -> item.status == AppointmentStatus.HELD || item.status == AppointmentStatus.CONFIRMED
                        "Pendientes" -> item.status == AppointmentStatus.HELD
                        else -> item.status in listOf(AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED, AppointmentStatus.EXPIRED)
                    }
                }.sortedBy { it.startsAt }
                if (filtered.isEmpty()) PatientCard {
                    PatientHeading(if (tab == "Historial") "Aún no hay sesiones anteriores" else "No tienes citas aquí", "Encuentra un profesional y coordina a tu ritmo.")
                    PatientButton("Explorar psicólogos", { onNavigateToRoute(NavRoutes.PROFESSIONALS) }, Modifier.fillMaxWidth())
                }
                filtered.forEach { item ->
                    AppointmentCard(item, state.clinician(item.clinicianId), state.now, onDetail = { openAppointment(item) }, onJoin = { viewModel.join(item.id) })
                }
                Text("Conversaciones", style = MaterialTheme.typography.titleLarge, color = PrimaryNavy)
                if (state.contactRequests.isEmpty()) PatientInfo("Cuando solicites contacto con un especialista, la conversación aparecerá aquí.")
                state.contactRequests.forEach { conversation ->
                    val clinician = state.clinician(conversation.clinicianId)
                    PatientCard {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PatientAvatar(clinician.initials)
                            Column(Modifier.weight(1f)) {
                                Text(clinician.name, color = PrimaryNavy, style = MaterialTheme.typography.titleMedium)
                                PatientInfo(conversation.lastMessage?.text ?: "Coordina tu próxima sesión")
                            }
                        }
                        when {
                            conversation.isOpen && state.proposalFor(conversation.id) != null -> PatientBadge("◷ Nueva propuesta de horario")
                            conversation.status == ContactRequestStatus.PENDING -> PatientBadge("Esperando respuesta", SurfaceContainerLow, PrimaryNavy)
                            conversation.status == ContactRequestStatus.REJECTED -> PatientBadge("Solicitud no aceptada", ErrorContainer, ErrorRed)
                            conversation.status == ContactRequestStatus.CLOSED -> PatientBadge("Conversación cerrada", SurfaceContainerLow, PrimaryNavy)
                        }
                        PatientButton("Abrir conversación", { openChat(conversation.id) }, Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
    if (cancelDialog && appointment != null) AlertDialog(
        onDismissRequest = { cancelDialog = false },
        title = { Text(if (appointment.status == AppointmentStatus.HELD) "¿Cancelar la reserva?" else "¿Cancelar la cita?") },
        text = {
            Text(
                if (appointment.status == AppointmentStatus.HELD) "Se liberará el horario. La cita aún no está confirmada y no se registrará ningún cobro."
                else "Se liberará el horario. El estado del cobro y la devolución que corresponda se reflejarán en tus pagos."
            )
        },
        confirmButton = { TextButton(onClick = { viewModel.cancel(appointment.id); cancelDialog = false; page = "detail" }) { Text("Sí, cancelar", color = ErrorRed) } },
        dismissButton = { TextButton(onClick = { cancelDialog = false }) { Text("Conservar") } }
    )
    state.checkoutUrl?.let { url ->
        StripeCheckoutWebViewDialog(
            checkoutUrl = url,
            onSuccess = { viewModel.closeCheckout(completed = true) },
            onCancelled = { viewModel.closeCheckout(completed = false) },
            onDismissRequest = { viewModel.closeCheckout(completed = false) }
        )
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
