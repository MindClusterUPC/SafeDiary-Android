package com.mindcluster.safediary.scheduling.presentation.views

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
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
import com.mindcluster.safediary.scheduling.domain.model.*
import com.mindcluster.safediary.scheduling.presentation.components.*
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
    var page by rememberSaveable { mutableStateOf("list") }
    var appointmentId by rememberSaveable { mutableStateOf("") }
    var chatId by rememberSaveable { mutableStateOf("") }
    var tab by rememberSaveable { mutableStateOf("Próximas") }
    var cancelDialog by rememberSaveable { mutableStateOf(false) }
    var receiptDialog by rememberSaveable { mutableStateOf(false) }
    var draftConsent by rememberSaveable { mutableStateOf(false) }
    val appointment = state.appointments.find { it.id == appointmentId }
    val chat = state.conversations.find { it.id == chatId }

    LaunchedEffect(isActive) {
        if (isActive) while (true) { viewModel.tick(); delay(1000) }
    }
    LaunchedEffect(state.activeConversationId) {
        state.activeConversationId?.let { chatId = it; page = "chat" }
    }
    LaunchedEffect(appointment?.status) {
        if (appointment?.status == AppointmentStatus.EXPIRED && page in listOf("payment", "failed")) page = "reservation"
    }
    fun back() {
        page = when (page) {
            "payment", "failed" -> "reservation"
            "waiting", "rating", "consent", "call" -> "detail"
            else -> "list"
        }
        viewModel.closeConversation()
    }
    BackHandler(isActive && page != "list") { back() }
    fun openAppointment(item: Appointment) {
        appointmentId = item.id
        page = when (item.status) {
            AppointmentStatus.RESERVED, AppointmentStatus.EXPIRED -> "reservation"
            AppointmentStatus.COMPLETED -> if (item.rating == null) "rating" else "detail"
            else -> "detail"
        }
    }
    PatientScreen("Agenda", modifier, onBack = if (page != "list") ({ back() }) else null, onSettings = onNavigateToSettings, contentKey = page) {
        when {
            page == "chat" && chat != null -> CoordinationContent(chat, onSend = { viewModel.sendMessage(chat.id, it) }, onAccept = {
                viewModel.reserve(chat.id)?.let { appointmentId = it; page = "reservation" }
            })
            page == "reservation" && appointment != null -> ReservationContent(appointment, state.now,
                onPay = { viewModel.tick(); page = "payment" },
                onCancel = { cancelDialog = true },
                onChat = {
                    appointment.conversationId?.let { chatId = it; page = "chat" } ?: run { page = "list" }
                }
            )
            page in listOf("payment", "failed") && appointment != null -> PaymentContent(appointment, page == "failed", onSuccess = {
                page = if (viewModel.simulatePayment(appointment.id)) "detail" else "reservation"
            }, onFailure = { page = "failed" }, onBack = { page = "reservation" })
            page == "detail" && appointment != null -> AppointmentDetailContent(appointment, state.now,
                onJoin = { page = "waiting" },
                onConsent = { draftConsent = appointment.shareContext; page = "consent" },
                onCancel = { cancelDialog = true },
                onRate = { page = "rating" },
                onReceipt = { receiptDialog = true }
            )
            page == "waiting" && appointment != null -> {
                if (appointment.canJoin(state.now)) WaitingRoomContent(appointment, onJoin = { page = "call" })
                else {
                    PatientHeading("Acceso fuera de horario", "Puedes entrar desde diez minutos antes y durante la sesión.")
                    PatientButton("Volver a la cita", { page = "detail" }, Modifier.fillMaxWidth())
                }
            }
            page == "call" && appointment != null -> CallPreviewContent(appointment) { viewModel.completePreview(appointment.id); page = "detail" }
            page == "rating" && appointment != null -> RatingContent { rating, review -> viewModel.rate(appointment.id, rating, review); page = "detail" }
            page == "consent" && appointment != null -> {
                val professional = PreviewProfessionals.find(appointment.professionalId)
                PatientHeading("Tú decides qué compartir", "El consentimiento es opcional.")
                PatientCard {
                    Text("Contexto para ${professional.name}", style = MaterialTheme.typography.titleLarge, color = PrimaryNavy)
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
            else -> {
                PatientHeading("Mis citas", "Coordina, reserva y asiste a tus sesiones.")
                PatientBadge("Vista previa · datos de ejemplo", SurfaceContainerLow, PrimaryNavy)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    listOf("Próximas", "Pendientes", "Historial").forEachIndexed { index, label ->
                        SegmentedButton(selected = tab == label, onClick = { tab = label }, shape = SegmentedButtonDefaults.itemShape(index, 3)) { Text(label, style = MaterialTheme.typography.labelMedium) }
                    }
                }
                val filtered = state.appointments.filter { item ->
                    when (tab) {
                        "Próximas" -> item.status == AppointmentStatus.RESERVED || item.status == AppointmentStatus.CONFIRMED
                        "Pendientes" -> item.status == AppointmentStatus.RESERVED
                        else -> item.status in listOf(AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED, AppointmentStatus.EXPIRED)
                    }
                }.sortedBy { it.startsAt }
                if (filtered.isEmpty()) PatientCard {
                    PatientHeading(if (tab == "Historial") "Aún no hay sesiones anteriores" else "No tienes citas aquí", "Encuentra un profesional y coordina a tu ritmo.")
                    PatientButton("Explorar psicólogos", { onNavigateToRoute(NavRoutes.PROFESSIONALS) }, Modifier.fillMaxWidth())
                }
                filtered.forEach { item ->
                    AppointmentCard(item, state.now, onDetail = { openAppointment(item) }, onPay = { appointmentId = item.id; page = "payment" }, onJoin = { appointmentId = item.id; page = "waiting" })
                }
                Text("Conversaciones activas", style = MaterialTheme.typography.titleLarge, color = PrimaryNavy)
                state.conversations.forEach { conversation ->
                    val professional = PreviewProfessionals.find(conversation.professionalId)
                    PatientCard {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PatientAvatar(professional.initials)
                            Column(Modifier.weight(1f)) {
                                Text(professional.name, color = PrimaryNavy, style = MaterialTheme.typography.titleMedium)
                                PatientInfo(conversation.messages.lastOrNull()?.text ?: "Coordina tu próxima sesión")
                            }
                        }
                        TextButton(onClick = { chatId = conversation.id; page = "chat" }) { Text("Abrir conversación") }
                    }
                }
            }
        }
    }
    if (cancelDialog && appointment != null) AlertDialog(
        onDismissRequest = { cancelDialog = false },
        title = { Text(if (appointment.status == AppointmentStatus.RESERVED) "¿Cancelar la reserva?" else "¿Cancelar la cita?") },
        text = { Text(if (appointment.status == AppointmentStatus.RESERVED) "Se liberará el horario. La cita aún no está confirmada y no se ha realizado ningún cargo." else "Se liberará el horario de esta cita de ejemplo. No se procesan cargos ni devoluciones reales.") },
        confirmButton = { TextButton(onClick = { viewModel.cancel(appointment.id); cancelDialog = false; page = "detail" }) { Text("Sí, cancelar", color = ErrorRed) } },
        dismissButton = { TextButton(onClick = { cancelDialog = false }) { Text("Conservar") } }
    )
    if (receiptDialog && appointment != null) AlertDialog(
        onDismissRequest = { receiptDialog = false },
        title = { Text("Comprobante de ejemplo") },
        text = { Text("${PreviewProfessionals.find(appointment.professionalId).name}\n${appointmentDate(appointment.startsAt)}\nS/ ${PreviewProfessionals.find(appointment.professionalId).price}.00\n\nEsta vista no representa una transacción real.") },
        confirmButton = { TextButton(onClick = { receiptDialog = false }) { Text("Cerrar") } }
    )
}
