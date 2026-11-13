package com.mindcluster.safediary.scheduling.presentation.components

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindcluster.safediary.scheduling.domain.model.*
import com.mindcluster.safediary.shared.presentation.components.*
import com.mindcluster.safediary.shared.presentation.theme.*

/** US-048 / US-049: private coordination chat and the clinician's proposal. */
@Composable
fun CoordinationContent(
    request: ContactRequest,
    clinician: ClinicianSummary,
    messages: List<CoordinationMessage>,
    proposal: Appointment?,
    held: Appointment?,
    isWorking: Boolean,
    onMessageChange: (String) -> Unit,
    onAccept: (Long) -> Unit,
    onOpenReservation: (Long) -> Unit
) {
    var confirm by rememberSaveable(request.id) { mutableStateOf(false) }
    PatientHeading("Chat de coordinación", clinician.name)
    PatientInfo("Solo tú y el especialista ven esta conversación. Tu diario no se comparte.", Icons.Outlined.Lock)
    when (request.status) {
        ContactRequestStatus.PENDING -> if (proposal == null) PatientCard(color = PatientMint) {
            PatientInfo("Solicitud enviada. El especialista revisará tu mensaje y te propondrá un horario por aquí.")
        }
        ContactRequestStatus.REJECTED -> PatientCard(color = ErrorContainer) {
            Text("El especialista no puede atender esta solicitud. No se generó ninguna cita ni cobro.", color = ErrorRed)
        }
        ContactRequestStatus.CLOSED -> PatientCard { PatientInfo("Esta conversación está cerrada.") }
        ContactRequestStatus.ACCEPTED -> Unit
    }
    messages.forEach { item ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = if (item.fromPatient) Arrangement.End else Arrangement.Start) {
            Surface(color = if (item.fromPatient) PrimaryNavy else Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth(.88f)) {
                Text(item.text, Modifier.padding(16.dp), color = if (item.fromPatient) Color.White else PrimaryNavy, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
    if (held != null) PatientCard(color = PatientMint) {
        PatientBadge(AppointmentStatus.HELD.label())
        Text(appointmentDate(held.startsAt), style = MaterialTheme.typography.titleLarge, color = PrimaryNavy)
        PatientButton("Ver reserva", { onOpenReservation(held.id) }, Modifier.fillMaxWidth())
    } else if (proposal != null && request.isOpen) PatientCard {
        PatientBadge("◷ Horario propuesto")
        Text(appointmentDate(proposal.startsAt), style = MaterialTheme.typography.titleLarge, color = PrimaryNavy)
        PatientInfo("Videollamada · ${proposal.durationMinutes} minutos")
        HorizontalDivider(color = PatientBorder)
        Text("Tarifa acordada: ${proposal.amountLabel}", fontWeight = FontWeight.Bold, color = PrimaryNavy)
        PatientButton("Aceptar horario", { confirm = true }, Modifier.fillMaxWidth(), enabled = !isWorking)
        PatientButton("Proponer otro horario", { onMessageChange("¿Podríamos coordinar otro horario? Tengo disponibilidad ") }, Modifier.fillMaxWidth(), secondary = true)
        PatientInfo("Aceptar crea una reserva temporal de una hora. La cita se confirma después del pago aprobado.")
    }
    if (confirm && proposal != null) AlertDialog(
        onDismissRequest = { confirm = false },
        title = { Text("¿Aceptar este horario?") },
        text = { Text("${appointmentDate(proposal.startsAt)} · ${proposal.amountLabel}. Se reservará durante una hora mientras completas el pago. La cita aún no quedará confirmada.") },
        confirmButton = { TextButton(onClick = { confirm = false; onAccept(proposal.id) }) { Text("Aceptar horario") } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text("Volver al chat") } }
    )
}

/** US-014: one-hour hold with the remaining time and the amount, never shown as a confirmed appointment. */
@Composable
fun ReservationContent(
    appointment: Appointment, clinician: ClinicianSummary, now: Long,
    isWorking: Boolean, canSimulatePayment: Boolean,
    onSimulatePayment: () -> Unit, onPay: () -> Unit, onSkipPayment: () -> Unit, onCancel: () -> Unit, onChat: () -> Unit
) {
    var confirmTestPayment by rememberSaveable(appointment.id) { mutableStateOf(false) }
    var confirmSkipPayment by rememberSaveable(appointment.id) { mutableStateOf(false) }
    val expired = appointment.status == AppointmentStatus.EXPIRED
    PatientHeading(if (expired) "La reserva venció" else "Tu horario está reservado", if (expired) "Acuerda un nuevo horario desde el chat." else "Completa el pago para confirmar la cita.")
    PatientCard {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { (appointment.remainingSeconds(now) / 3600f).coerceIn(0f, 1f) }, modifier = Modifier.size(160.dp), color = SecondaryTeal, trackColor = PatientMint, strokeWidth = 9.dp)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(reservationTime(appointment.remainingSeconds(now)), style = MaterialTheme.typography.displayLarge, color = PrimaryNavy)
                    PatientInfo("minutos restantes")
                }
            }
            PatientBadge(if (expired) "Reserva vencida · horario liberado" else "Reserva temporal · aún no confirmada")
        }
    }
    PatientCard {
        PatientAvatar(clinician.initials)
        Text(clinician.name, color = PrimaryNavy, style = MaterialTheme.typography.titleLarge)
        PatientInfo(appointmentDate(appointment.startsAt), Icons.Outlined.CalendarMonth)
        PatientInfo("Videollamada en SafeDiary · ${appointment.durationMinutes} minutos", Icons.Outlined.Videocam)
    }
    PatientCard {
        PatientInfo("Total a pagar")
        Text(appointment.amountLabel, style = MaterialTheme.typography.headlineMedium, color = PrimaryNavy)
    }
    if (expired) PatientButton("Volver a coordinar", onChat, Modifier.fillMaxWidth())
    else {
        if (canSimulatePayment) {
            PatientInfo("Pago de prueba local. Confirma esta cita sin cobrar dinero ni solicitar una tarjeta.")
            PatientButton(if (isWorking) "Procesando pago de prueba…" else "Simular pago · ${appointment.amountLabel}",
                { confirmTestPayment = true }, Modifier.fillMaxWidth(), enabled = !isWorking && appointment.isHoldActive(now))
        } else {
            PatientButton(if (isWorking) "Preparando el pago…" else "Pagar · ${appointment.amountLabel}",
                onPay, Modifier.fillMaxWidth(), enabled = !isWorking && appointment.isHoldActive(now))
            PatientInfo("El pago se procesa de forma segura con Stripe. La cita se confirmará automáticamente cuando se apruebe.", Icons.Outlined.Lock)
            PatientButton("Saltar pago solo por esta vez",
                { confirmSkipPayment = true }, Modifier.fillMaxWidth(), secondary = true, enabled = !isWorking && appointment.isHoldActive(now))
        }
        PatientButton("Cancelar reserva", onCancel, Modifier.fillMaxWidth(), secondary = true, enabled = !isWorking)
    }
    if (confirmTestPayment && !expired) AlertDialog(
        onDismissRequest = { confirmTestPayment = false },
        title = { Text("¿Simular este pago?") },
        text = { Text("Se confirmará la cita con ${clinician.name} por ${appointment.amountLabel}. Es una prueba local: no se cobrará dinero.") },
        confirmButton = { TextButton(onClick = { confirmTestPayment = false; onSimulatePayment() },
            enabled = !isWorking && appointment.isHoldActive(now)) { Text("Confirmar pago de prueba") } },
        dismissButton = { TextButton(onClick = { confirmTestPayment = false }) { Text("Volver") } }
    )
    if (confirmSkipPayment && !expired) AlertDialog(
        onDismissRequest = { confirmSkipPayment = false },
        title = { Text("¿Saltar pago solo por esta vez?") },
        text = { Text("Se confirmará tu cita con ${clinician.name} sin procesar cobro en Stripe para que puedas probar la videollamada. Quedará registrada como «Pago omitido (Demo)» en tu historial.") },
        confirmButton = { TextButton(onClick = { confirmSkipPayment = false; onSkipPayment() },
            enabled = !isWorking && appointment.isHoldActive(now)) { Text("Sí, saltar pago") } },
        dismissButton = { TextButton(onClick = { confirmSkipPayment = false }) { Text("Volver") } }
    )
}

/** US-038 / US-050: confirmed, completed, cancelled or expired appointment. */
@Composable
fun AppointmentDetailContent(
    appointment: Appointment,
    clinician: ClinicianSummary,
    now: Long,
    sharedContext: Boolean,
    isWorking: Boolean,
    onJoin: () -> Unit,
    onConsent: () -> Unit,
    onCancel: () -> Unit,
    onChat: () -> Unit
) {
    PatientHeading(when (appointment.status) {
        AppointmentStatus.CONFIRMED -> "Cita confirmada"
        AppointmentStatus.COMPLETED -> "Sesión completada"
        AppointmentStatus.CANCELLED, AppointmentStatus.EXPIRED -> "Horario liberado"
        else -> "Detalle de la cita"
    })
    PatientCard {
        PatientBadge(appointment.status.label())
        PatientAvatar(clinician.initials)
        Text(clinician.name, style = MaterialTheme.typography.titleLarge, color = PrimaryNavy)
        PatientInfo(appointmentDate(appointment.startsAt), Icons.Outlined.CalendarMonth)
        PatientInfo("Videollamada · ${appointment.durationMinutes} minutos", Icons.Outlined.Videocam)
        if (appointment.status == AppointmentStatus.CONFIRMED || appointment.status == AppointmentStatus.COMPLETED) {
            PatientInfo("Pago aprobado · ${appointment.amountLabel}")
        }
    }
    when (appointment.status) {
        AppointmentStatus.CONFIRMED -> {
            val canJoin = appointment.canJoin(now)
            PatientCard(color = PatientMint) {
                PatientInfo(
                    appointment.accessOpensAt?.let { "Puedes ingresar desde ${appointmentDate(it)}." } ?: "El acceso se habilita cerca del horario de la cita.",
                    Icons.Outlined.Lock
                )
                PatientInfo("Solo los participantes de esta cita pueden ingresar.")
                PatientInfo(if (sharedContext) "Elegiste compartir un resumen emocional (vista previa)." else "Tu diario no se comparte de forma automática.")
            }
            PatientButton("Unirse a la videollamada", onJoin, Modifier.fillMaxWidth(), enabled = canJoin && !isWorking)
            if (!canJoin) PatientInfo("El botón se activa dentro de la ventana de acceso de la cita.")
            PatientButton("Escribir al especialista", onChat, Modifier.fillMaxWidth(), secondary = true)
            PatientButton("Configurar consentimiento", onConsent, Modifier.fillMaxWidth(), secondary = true)
            TextButton(onClick = onCancel, enabled = !isWorking) { Text("Cancelar cita", color = ErrorRed) }
        }
        AppointmentStatus.COMPLETED -> PatientCard(color = PatientMint) {
            Text("Gracias por darte este espacio.", color = PrimaryNavy, style = MaterialTheme.typography.titleLarge)
        }
        AppointmentStatus.CANCELLED -> PatientInfo("La cita se canceló y el horario se liberó. Si hubo un cobro, su estado y la devolución que corresponda se reflejarán en tus pagos.")
        AppointmentStatus.EXPIRED -> PatientInfo("La reserva venció sin un pago aprobado y el horario se liberó. No se confirmó ninguna cita.")
        else -> Unit
    }
}
