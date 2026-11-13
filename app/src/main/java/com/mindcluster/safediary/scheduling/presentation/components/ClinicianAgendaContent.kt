package com.mindcluster.safediary.scheduling.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindcluster.safediary.scheduling.domain.model.*
import com.mindcluster.safediary.scheduling.presentation.viewmodels.AGENDA_ZONE
import com.mindcluster.safediary.scheduling.presentation.viewmodels.ClinicianAgendaUiState
import com.mindcluster.safediary.scheduling.presentation.viewmodels.weekDays
import com.mindcluster.safediary.shared.presentation.components.*
import com.mindcluster.safediary.shared.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val spanishPeru = Locale.forLanguageTag("es-PE")
private fun format(pattern: String, time: Long) =
    SimpleDateFormat(pattern, spanishPeru).apply { timeZone = AGENDA_ZONE }.format(Date(time))
internal fun timeLabel(time: Long) = format("HH:mm", time)
private fun String.capitalized() = replaceFirstChar { it.titlecase(spanishPeru) }
private fun relativeTime(time: Long, now: Long): String {
    val minutes = ((now - time) / 60_000).coerceAtLeast(0)
    return when {
        minutes < 1 -> "ahora"
        minutes < 60 -> "hace $minutes min"
        minutes < 1_440 -> "hace ${minutes / 60} h"
        else -> "hace ${minutes / 1_440} d"
    }
}

private val HeldSlot = Color(0xFFDDF3F8)
private val ConfirmedSlot = SurfaceContainerHigh
private val BusySlot = Color(0xFFE6E9EF)

/** Design 03 · "Mi agenda": incoming requests, week selector, slot grid and the proposal button (US-043). */
@Composable
fun ClinicianAgendaContent(
    state: ClinicianAgendaUiState,
    onReject: (Long) -> Unit,
    onOpenChat: (Long) -> Unit,
    onSelectRequest: (Long) -> Unit,
    onShiftWeek: (Int) -> Unit,
    onSelectDay: (Long) -> Unit,
    onSelectSlot: (BookableSlot) -> Unit,
    onPropose: () -> Unit,
    onOpenAppointment: (Long) -> Unit,
    onRefresh: () -> Unit
) {
    var rejectId by rememberSaveable { mutableStateOf<Long?>(null) }
    PatientHeading("Mi agenda", "Solicitudes de contacto y horarios disponibles")
    if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = SecondaryTeal)
    state.loadError?.let {
        PatientCard(color = ErrorContainer) {
            Text(it, color = ErrorRed)
            PatientButton("Reintentar", onRefresh, Modifier.fillMaxWidth(), secondary = true)
        }
    }
    if (state.openRequests.isEmpty()) PatientCard {
        PatientInfo("No tienes solicitudes abiertas. Cuando un paciente te contacte, aparecerá aquí.", Icons.Outlined.Inbox)
    }
    state.openRequests.forEach { request ->
        val selected = request.id == state.selectedRequestId
        PatientCard(Modifier.then(if (selected && state.openRequests.size > 1) Modifier.border(2.dp, SecondaryTeal, RoundedCornerShape(20.dp)) else Modifier)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PatientBadge(if (request.status == ContactRequestStatus.PENDING) "Nueva solicitud" else "En coordinación", SurfaceContainerHigh, PrimaryNavy)
                request.lastMessage?.let { PatientInfo(relativeTime(it.sentAt, state.now)) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                PatientAvatar("P")
                Column {
                    Text(patientLabel(request.patientAccountId), style = MaterialTheme.typography.titleMedium, color = PrimaryNavy, fontWeight = FontWeight.Bold)
                    PatientInfo("Paciente · Solicitud #${request.id}")
                }
            }
            request.lastMessage?.text?.takeIf { it.isNotBlank() }?.let {
                Text("“$it”", style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic, color = SlateSubtle)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (request.status == ContactRequestStatus.PENDING) OutlinedButton(
                    onClick = { rejectId = request.id }, enabled = !state.isWorking, modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    border = BorderStroke(1.dp, PatientBorder)
                ) {
                    Icon(Icons.Outlined.Close, null, Modifier.size(18.dp), tint = PrimaryNavy)
                    Spacer(Modifier.width(6.dp)); Text("Rechazar", color = PrimaryNavy)
                }
                PatientButton("Abrir chat", { onOpenChat(request.id) }, Modifier.weight(1f), secondary = true)
            }
            if (state.openRequests.size > 1 && !selected) TextButton(onClick = { onSelectRequest(request.id) }) { Text("Proponer horario a este paciente") }
        }
    }

    val days = weekDays(state.selectedDay)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(format("MMMM yyyy", state.selectedDay).capitalized(), Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, color = PrimaryNavy, fontWeight = FontWeight.Bold)
        IconButton(onClick = { onShiftWeek(-1) }) { Icon(Icons.Outlined.ChevronLeft, "Semana anterior", tint = PrimaryNavy) }
        IconButton(onClick = { onShiftWeek(1) }) { Icon(Icons.Outlined.ChevronRight, "Semana siguiente", tint = PrimaryNavy) }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        days.forEach { day ->
            val selected = day == state.selectedDay
            Surface(
                onClick = { onSelectDay(day) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp),
                color = if (selected) PrimaryNavy else Color.White, border = if (selected) null else BorderStroke(1.dp, PatientBorder.copy(alpha = .5f))
            ) {
                Column(Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(format("EEE", day).take(3).capitalized(), style = MaterialTheme.typography.labelSmall, color = if (selected) Color.White else SlateSubtle)
                    Text(format("d", day), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (selected) Color.White else PrimaryNavy)
                }
            }
        }
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(format("EEEE d 'de' MMMM", state.selectedDay).capitalized(), Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, color = PrimaryNavy, fontWeight = FontWeight.Bold)
        if (!state.slotsLoading && state.slotsError == null) PatientInfo("${state.freeSlots} horarios libres")
    }
    when {
        state.slotsLoading -> LinearProgressIndicator(Modifier.fillMaxWidth(), color = SecondaryTeal)
        state.slotsError != null -> PatientCard(color = ErrorContainer) { Text(state.slotsError, color = ErrorRed) }
        state.slots.isEmpty() -> PatientInfo("No hay horarios este día. Solo se muestran los horarios dentro de tu disponibilidad semanal.")
        else -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.slots.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { slot -> SlotCell(slot, slot == state.selectedSlot, Modifier.weight(1f)) { onSelectSlot(slot) } }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf("Libre" to Color.White, "Retenido" to HeldSlot, "Confirmada" to ConfirmedSlot, "Ocupado" to BusySlot).forEach { (label, color) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(Modifier.size(12.dp).background(color, RoundedCornerShape(3.dp)).border(1.dp, PatientBorder, RoundedCornerShape(3.dp)))
                Text(label, style = MaterialTheme.typography.labelSmall, color = SlateSubtle)
            }
        }
    }
    val request = state.selectedRequest
    val slot = state.selectedSlot
    PatientButton(
        if (slot != null) "Proponer ${format("EEE d", slot.startsAt)} · ${timeLabel(slot.startsAt)}" else "Proponer horario",
        onPropose, Modifier.fillMaxWidth(), enabled = request != null && slot != null && !state.isWorking
    )
    PatientInfo(
        if (request == null) "Necesitas una solicitud abierta para proponer un horario."
        else "Para ${patientLabel(request.patientAccountId)}. Proponer no crea una reserva ni un cobro."
    )

    val appointments = state.agenda.filter { it.status in listOf(AppointmentStatus.HELD, AppointmentStatus.CONFIRMED, AppointmentStatus.COMPLETED) }
    Text("Citas de los próximos 7 días", style = MaterialTheme.typography.titleLarge, color = PrimaryNavy)
    if (appointments.isEmpty()) PatientInfo("Aún no tienes citas reservadas o confirmadas.")
    appointments.forEach { item ->
        PatientCard {
            PatientBadge(item.status.clinicianLabel())
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                PatientAvatar("P")
                Column {
                    Text(patientLabel(item.patientAccountId), style = MaterialTheme.typography.titleMedium, color = PrimaryNavy)
                    PatientInfo(appointmentDate(item.startsAt) + " · ${item.durationMinutes} min")
                }
            }
            PatientButton("Ver cita", { onOpenAppointment(item.id) }, Modifier.fillMaxWidth(), secondary = true)
        }
    }
    TextButton(onClick = onRefresh) { Text("Actualizar agenda") }

    rejectId?.let { id ->
        AlertDialog(
            onDismissRequest = { rejectId = null },
            title = { Text("¿Rechazar la solicitud?") },
            text = { Text("Se cerrará la solicitud y se informará al paciente. No se genera ningún cobro.") },
            confirmButton = { TextButton(onClick = { rejectId = null; onReject(id) }) { Text("Rechazar", color = ErrorRed) } },
            dismissButton = { TextButton(onClick = { rejectId = null }) { Text("Volver") } }
        )
    }
}

@Composable
private fun SlotCell(slot: BookableSlot, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val (background, label) = when {
        selected -> SecondaryTeal to "Seleccionado ✓"
        slot.availability == SlotAvailability.FREE -> Color.White to "Libre"
        slot.availability == SlotAvailability.HELD -> HeldSlot to "Retenido"
        slot.availability == SlotAvailability.CONFIRMED -> ConfirmedSlot to "Confirmada"
        else -> BusySlot to "Ocupado"
    }
    val foreground = if (selected) Color.White else PrimaryNavy
    Surface(
        modifier = modifier.selectable(selected, enabled = slot.availability == SlotAvailability.FREE, onClick = onClick),
        shape = RoundedCornerShape(12.dp), color = background,
        border = if (slot.availability == SlotAvailability.FREE && !selected) BorderStroke(1.dp, PatientBorder) else null
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(timeLabel(slot.startsAt), fontWeight = FontWeight.Bold, color = foreground)
            Text(label, style = MaterialTheme.typography.labelSmall, color = if (selected) Color.White else SlateSubtle)
        }
    }
}

fun AppointmentStatus.clinicianLabel() = when (this) {
    AppointmentStatus.REQUESTED -> "◷ Propuesta enviada"
    AppointmentStatus.HELD -> "◷ Reservada · esperando pago"
    AppointmentStatus.CONFIRMED -> "✓ Confirmada"
    AppointmentStatus.COMPLETED -> "✓ Completada"
    AppointmentStatus.CANCELLED -> "Cancelada"
    AppointmentStatus.EXPIRED -> "Reserva vencida"
}

/** Coordination chat seen by the psychologist; replying opens the chat for the patient (US-043). */
@Composable
fun ClinicianChatContent(
    request: ContactRequest,
    messages: List<CoordinationMessage>,
    proposal: Appointment?,
    isWorking: Boolean,
    onProposeSchedule: () -> Unit
) {
    PatientHeading("Chat de coordinación", patientLabel(request.patientAccountId))
    PatientInfo("Solo tú y el paciente ven esta conversación. No da acceso a su diario.", Icons.Outlined.Lock)
    when (request.status) {
        ContactRequestStatus.PENDING -> PatientCard(color = PatientMint) { PatientInfo("Al responder o proponer un horario aceptas el contacto.") }
        ContactRequestStatus.REJECTED -> PatientCard { PatientInfo("Rechazaste esta solicitud.") }
        ContactRequestStatus.CLOSED -> PatientCard { PatientInfo("Esta conversación está cerrada.") }
        ContactRequestStatus.ACCEPTED -> Unit
    }
    messages.forEach { item ->
        val mine = !item.fromPatient
        Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
            Surface(color = if (mine) PrimaryNavy else Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth(.88f)) {
                Text(item.text, Modifier.padding(16.dp), color = if (mine) Color.White else PrimaryNavy, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
    if (proposal != null) PatientCard {
        PatientBadge("◷ Propuesta enviada")
        Text(appointmentDate(proposal.startsAt), style = MaterialTheme.typography.titleLarge, color = PrimaryNavy)
        PatientInfo("Esperando que el paciente la acepte. Aún no hay reserva ni cobro.")
    }
    if (request.isOpen) PatientButton(if (proposal != null) "Proponer otro horario" else "Proponer horario", onProposeSchedule, Modifier.fillMaxWidth())

}

/** Appointment seen by the psychologist: access to the call and entry to close the attention (US-044). */
@Composable
fun ClinicianAppointmentContent(appointment: Appointment, now: Long, isWorking: Boolean, onJoin: () -> Unit, onClose: () -> Unit) {
    PatientHeading("Detalle de la cita")
    PatientCard {
        PatientBadge(appointment.status.clinicianLabel())
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            PatientAvatar("P")
            Column {
                Text(patientLabel(appointment.patientAccountId), style = MaterialTheme.typography.titleLarge, color = PrimaryNavy)
                PatientInfo("Cita #${appointment.id}")
            }
        }
        PatientInfo(appointmentDate(appointment.startsAt), Icons.Outlined.CalendarMonth)
        PatientInfo("Videollamada · ${appointment.durationMinutes} minutos", Icons.Outlined.Videocam)
        PatientInfo("Tarifa acordada · ${appointment.amountLabel}")
    }
    when (appointment.status) {
        AppointmentStatus.CONFIRMED -> {
            val accessOpen = appointment.canJoin(now)
            PatientCard(color = PatientMint) {
                PatientInfo(appointment.accessOpensAt?.let { "Acceso desde ${appointmentDate(it)}." } ?: "El acceso se habilita cerca del horario de la cita.", Icons.Outlined.Lock)
                PatientInfo("Solo tú y el paciente de esta cita pueden ingresar.")
            }
            PatientButton("Unirse a la videollamada", onJoin, Modifier.fillMaxWidth(), enabled = accessOpen && !isWorking)
            PatientButton("Cerrar la atención", onClose, Modifier.fillMaxWidth(), secondary = true,
                enabled = appointment.accessOpensAt != null && now >= appointment.accessOpensAt)
            if (!accessOpen) PatientInfo("Los botones se activan cuando se abre la ventana de acceso de la cita.")
        }
        AppointmentStatus.HELD -> PatientInfo("El horario está reservado. La cita se confirmará cuando se apruebe el pago del paciente.")
        AppointmentStatus.COMPLETED -> PatientCard(color = PatientMint) { PatientInfo("Atención completada.") }
        else -> Unit
    }
}

/** Design 10 · "Cerrar la atención": operational outcome only, no clinical information (US-044). */
@Composable
fun CloseAttentionContent(
    appointment: Appointment,
    session: ClinicalSession?,
    now: Long,
    isWorking: Boolean,
    onBackToSession: () -> Unit,
    onClose: (SessionOutcome) -> Unit
) {
    var outcome by rememberSaveable { mutableStateOf(SessionOutcome.COMPLETED) }
    var confirm by rememberSaveable { mutableStateOf(false) }
    PatientHeading("Cerrar la atención", "Registra el resultado operativo de la cita")
    PatientCard {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            PatientAvatar("P")
            Column(Modifier.weight(1f)) {
                Text(patientLabel(appointment.patientAccountId), style = MaterialTheme.typography.titleMedium, color = PrimaryNavy, fontWeight = FontWeight.Bold)
                PatientInfo("Paciente · CITA-${appointment.id}")
            }
            PatientBadge(if (session?.startedAt != null) "Sesión iniciada" else "Sin iniciar", if (session?.startedAt != null) PatientMint else SurfaceContainerLow,
                if (session?.startedAt != null) SecondaryTeal else PrimaryNavy)
        }
        val start = session?.startedAt
        val end = session?.endedAt ?: now
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DataTile("Inicio", start?.let { timeLabel(it) } ?: "—", Modifier.weight(1f))
            DataTile("Fin", if (start != null) timeLabel(end) else "—", Modifier.weight(1f))
            DataTile("Duración", if (start != null) "${((end - start) / 60_000).coerceAtLeast(0)} min" else "—", Modifier.weight(1f))
        }
    }
    Text("Resultado de la cita", style = MaterialTheme.typography.titleMedium, color = PrimaryNavy, fontWeight = FontWeight.Bold)
    listOf(
        Triple(SessionOutcome.COMPLETED, "Atención completada", "La sesión se realizó con normalidad"),
        Triple(SessionOutcome.NO_SHOW, "Paciente no asistió", "Se registrará como inasistencia"),
        Triple(SessionOutcome.NOT_HELD, "Sesión no realizada", "Cancelación o problema técnico")
    ).forEach { (value, title, subtitle) ->
        val selected = outcome == value
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { outcome = value }, shape = RoundedCornerShape(16.dp),
            color = if (selected) PatientMint.copy(alpha = .5f) else Color.White,
            border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) SecondaryTeal else PatientBorder.copy(alpha = .5f))
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RadioButton(selected = selected, onClick = { outcome = value }, colors = RadioButtonDefaults.colors(selectedColor = SecondaryTeal))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, color = PrimaryNavy)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SlateSubtle)
                }
            }
        }
    }
    PatientCard(color = SurfaceContainerLow) {
        PatientInfo("No registramos información clínica aquí. Si la atención se completó, el paciente podrá reseñar la cita.", Icons.Outlined.Shield)
    }
    OutlinedButton(onClick = onBackToSession, enabled = !isWorking && appointment.canJoin(now), modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), border = BorderStroke(1.dp, PatientBorder)) {
        Icon(Icons.Outlined.Videocam, null, Modifier.size(18.dp), tint = PrimaryNavy)
        Spacer(Modifier.width(8.dp)); Text("Volver a la sesión", color = PrimaryNavy)
    }
    PatientButton("Cerrar la atención", { confirm = true }, Modifier.fillMaxWidth(), enabled = !isWorking)
    if (confirm) AlertDialog(
        onDismissRequest = { confirm = false },
        title = { Text("¿Cerrar la atención?") },
        text = {
            Text(if (outcome == SessionOutcome.COMPLETED) "La cita quedará como completada. Esta acción no se puede deshacer."
            else "La cita no se marcará como completada y Pagos aplicará la política de devolución. Esta acción no se puede deshacer.")
        },
        confirmButton = { TextButton(onClick = { confirm = false; onClose(outcome) }) { Text("Cerrar la atención") } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text("Volver") } }
    )
}

@Composable
private fun DataTile(label: String, value: String, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(12.dp), color = SurfaceContainerLow) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = SlateSubtle)
            Text(value, fontWeight = FontWeight.Bold, color = PrimaryNavy)
        }
    }
}
