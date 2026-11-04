package com.mindcluster.safediary.scheduling.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mindcluster.safediary.professionals.infrastructure.local.PreviewProfessionals
import com.mindcluster.safediary.scheduling.domain.model.*
import com.mindcluster.safediary.shared.presentation.components.*
import com.mindcluster.safediary.shared.presentation.theme.*

@Composable
fun CoordinationContent(chat: CoordinationConversation, onSend: (String) -> Unit, onAccept: () -> Unit) {
    val professional = PreviewProfessionals.find(chat.professionalId)
    var message by rememberSaveable(chat.id) { mutableStateOf("") }
    var confirm by rememberSaveable(chat.id) { mutableStateOf(false) }
    PatientHeading("Chat de coordinación", professional.name)
    PatientBadge("Vista previa · conversación de ejemplo", SurfaceContainerLow, PrimaryNavy)
    chat.messages.forEach { item ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = if (item.fromPatient) Arrangement.End else Arrangement.Start) {
            Surface(color = if (item.fromPatient) PrimaryNavy else Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth(.88f)) {
                Text(item.text, Modifier.padding(16.dp), color = if (item.fromPatient) Color.White else PrimaryNavy, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
    PatientCard {
        PatientBadge("◷ Horario propuesto")
        Text(appointmentDate(chat.proposedAt), style = MaterialTheme.typography.titleLarge, color = PrimaryNavy)
        PatientInfo("Videollamada · 50 minutos")
        HorizontalDivider(color = PatientBorder)
        Text("Tarifa acordada: S/ ${professional.price}", fontWeight = FontWeight.Bold, color = PrimaryNavy)
        PatientButton("Aceptar horario", { confirm = true }, Modifier.fillMaxWidth())
        PatientButton("Proponer otro horario", { message = "¿Podríamos coordinar otro horario? Tengo disponibilidad " }, Modifier.fillMaxWidth(), secondary = true)
        PatientInfo("Aceptar crea una reserva temporal de una hora. La cita se confirma después del pago aprobado.")
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(message, { message = it }, modifier = Modifier.weight(1f), placeholder = { Text("Escribe un mensaje…") }, maxLines = 4)
        IconButton(onClick = { onSend(message); message = "" }, enabled = message.isNotBlank()) {
            Icon(Icons.AutoMirrored.Outlined.Send, "Añadir mensaje a la vista previa", tint = SecondaryTeal)
        }
    }
    if (confirm) AlertDialog(
        onDismissRequest = { confirm = false },
        title = { Text("¿Aceptar este horario?") },
        text = { Text("${appointmentDate(chat.proposedAt)} · S/ ${professional.price}. Se reservará durante una hora mientras completas el pago. Esta es una vista de ejemplo.") },
        confirmButton = { TextButton(onClick = { confirm = false; onAccept() }) { Text("Aceptar horario") } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text("Volver al chat") } }
    )
}

@Composable
fun ReservationContent(appointment: Appointment, now: Long, onPay: () -> Unit, onCancel: () -> Unit, onChat: () -> Unit) {
    val professional = PreviewProfessionals.find(appointment.professionalId)
    val expired = appointment.status == AppointmentStatus.EXPIRED
    PatientHeading(if (expired) "La reserva venció" else "Tu horario está reservado", if (expired) "Puedes acordar otro horario desde el chat." else "Completa el pago para confirmar la cita.")
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
        PatientAvatar(professional.initials)
        Text(professional.name, color = PrimaryNavy, style = MaterialTheme.typography.titleLarge)
        PatientInfo(appointmentDate(appointment.startsAt), Icons.Outlined.CalendarMonth)
        PatientInfo("Videollamada en SafeDiary · 50 minutos", Icons.Outlined.Videocam)
    }
    PatientCard {
        PatientInfo("Total a pagar")
        Text("S/ ${professional.price}.00", style = MaterialTheme.typography.headlineMedium, color = PrimaryNavy)
    }
    if (expired) PatientButton("Volver a coordinar", onChat, Modifier.fillMaxWidth())
    else {
        PatientButton("Ir a pagar S/ ${professional.price}", onPay, Modifier.fillMaxWidth())
        PatientButton("Cancelar reserva", onCancel, Modifier.fillMaxWidth(), secondary = true)
    }
}

@Composable
fun PaymentContent(appointment: Appointment, failed: Boolean, onSuccess: () -> Unit, onFailure: () -> Unit, onBack: () -> Unit) {
    val professional = PreviewProfessionals.find(appointment.professionalId)
    PatientHeading(if (failed) "Pago no completado" else "Pago de sesión", if (failed) "Tu reserva se mantiene mientras siga vigente." else "Revisa los datos antes de continuar.")
    PatientCard {
        PatientBadge("Vista previa · no se realizará ningún cobro", SurfaceContainerLow, PrimaryNavy)
        Text(professional.name, style = MaterialTheme.typography.titleLarge, color = PrimaryNavy)
        PatientInfo(appointmentDate(appointment.startsAt))
        PatientInfo("Sesión por videollamada · 50 minutos")
        HorizontalDivider(color = PatientBorder)
        Text("Método de pago de ejemplo", color = PrimaryNavy, fontWeight = FontWeight.SemiBold)
        PatientInfo("Tarjeta de demostración · •••• 4242", Icons.Outlined.CreditCard)
        Text("Total: S/ ${professional.price}.00", style = MaterialTheme.typography.headlineMedium, color = PrimaryNavy)
    }
    if (failed) PatientCard(color = ErrorContainer) { Text("No se pudo completar el pago de ejemplo. Puedes volver a intentarlo o regresar a la reserva.", color = ErrorRed) }
    PatientButton(if (failed) "Reintentar pago de ejemplo" else "Simular pago aprobado", onSuccess, Modifier.fillMaxWidth())
    if (!failed) PatientButton("Ver estado de pago fallido", onFailure, Modifier.fillMaxWidth(), secondary = true)
    TextButton(onClick = onBack) { Text("Volver a la reserva") }
}

@Composable
fun AppointmentDetailContent(appointment: Appointment, now: Long, onJoin: () -> Unit, onConsent: () -> Unit, onCancel: () -> Unit, onRate: () -> Unit, onReceipt: () -> Unit) {
    val professional = PreviewProfessionals.find(appointment.professionalId)
    PatientHeading(when (appointment.status) {
        AppointmentStatus.CONFIRMED -> "Cita confirmada"
        AppointmentStatus.COMPLETED -> "Sesión completada"
        AppointmentStatus.CANCELLED -> "Horario liberado"
        else -> "Detalle de la cita"
    })
    PatientCard {
        PatientBadge(appointment.status.label())
        PatientAvatar(professional.initials)
        Text(professional.name, style = MaterialTheme.typography.titleLarge, color = PrimaryNavy)
        PatientInfo(appointmentDate(appointment.startsAt), Icons.Outlined.CalendarMonth)
        PatientInfo("Videollamada · 50 minutos", Icons.Outlined.Videocam)
        if (appointment.status == AppointmentStatus.CONFIRMED || appointment.status == AppointmentStatus.COMPLETED) {
            PatientInfo("Pago de ejemplo aprobado · S/ ${professional.price}")
            TextButton(onClick = onReceipt) { Text("Ver comprobante de ejemplo") }
        }
    }
    when (appointment.status) {
        AppointmentStatus.CONFIRMED -> {
            PatientCard(color = PatientMint) {
                PatientInfo("Puedes ingresar desde 10 minutos antes del inicio.", Icons.Outlined.Lock)
                PatientInfo(if (appointment.shareContext) "Elegiste compartir contexto en esta vista previa." else "Tu diario no se comparte de forma automática.")
            }
            PatientButton("Ingresar a sala de espera", onJoin, Modifier.fillMaxWidth(), enabled = appointment.canJoin(now))
            PatientButton("Configurar consentimiento", onConsent, Modifier.fillMaxWidth(), secondary = true)
            TextButton(onClick = onCancel) { Text("Cancelar cita", color = ErrorRed) }
        }
        AppointmentStatus.COMPLETED -> {
            PatientCard(color = PatientMint) {
                Text("Gracias por darte este espacio.", color = PrimaryNavy, style = MaterialTheme.typography.titleLarge)
                PatientInfo("Puedes valorar tu experiencia de forma anónima.")
            }
            if (appointment.rating == null) PatientButton("Calificar sesión", onRate, Modifier.fillMaxWidth())
            else PatientCard { Text("Tu calificación: ${"★".repeat(appointment.rating)}", color = SecondaryTeal); Text(appointment.review, color = PrimaryNavy) }
        }
        AppointmentStatus.CANCELLED -> PatientInfo("La cita de ejemplo se canceló y el horario se liberó. No se realizó ningún cobro real.")
        else -> Unit
    }
}

@Composable
fun WaitingRoomContent(appointment: Appointment, onJoin: () -> Unit) {
    val professional = PreviewProfessionals.find(appointment.professionalId)
    var mic by rememberSaveable { mutableStateOf(true) }
    var camera by rememberSaveable { mutableStateOf(true) }
    PatientHeading("Sala de espera", "Tu sesión con ${professional.name}")
    PatientBadge("Vista previa · cámara y micrófono sin conexión", SurfaceContainerLow, PrimaryNavy)
    Surface(color = Color(0xFF0B192A), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().height(240.dp)) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            PatientAvatar("TÚ", Modifier.size(88.dp))
            Spacer(Modifier.height(16.dp))
            Text(if (camera) "Vista previa de cámara" else "Cámara desactivada", color = Color.White)
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PatientButton(if (mic) "Micrófono: sí" else "Micrófono: no", { mic = !mic }, Modifier.weight(1f), secondary = true)
        PatientButton(if (camera) "Cámara: sí" else "Cámara: no", { camera = !camera }, Modifier.weight(1f), secondary = true)
    }
    PatientCard {
        PatientBadge("✓ Ventana de acceso abierta")
        PatientInfo(appointmentDate(appointment.startsAt))
        PatientInfo("Participantes: tú y ${professional.name}")
    }
    PatientCard(color = PatientMint) { PatientInfo("Solo los participantes de esta cita pueden ingresar.", Icons.Outlined.Lock) }
    PatientButton("Ver reunión de ejemplo", onJoin, Modifier.fillMaxWidth())
}

@Composable
fun CallPreviewContent(appointment: Appointment, onEnd: () -> Unit) {
    val professional = PreviewProfessionals.find(appointment.professionalId)
    var mic by rememberSaveable { mutableStateOf(true) }
    var camera by rememberSaveable { mutableStateOf(true) }
    PatientHeading("Sesión por videollamada", "Vista de ejemplo · sin conexión de audio ni video")
    Surface(color = Color(0xFF0B192A), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().height(360.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            PatientAvatar(professional.initials, Modifier.size(96.dp))
            Spacer(Modifier.height(16.dp))
            Text(professional.name, color = Color.White, style = MaterialTheme.typography.titleLarge)
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        IconToggleButton(mic, { mic = it }) { Icon(if (mic) Icons.Outlined.Mic else Icons.Outlined.MicOff, "Activar o desactivar micrófono de ejemplo") }
        IconToggleButton(camera, { camera = it }) { Icon(if (camera) Icons.Outlined.Videocam else Icons.Outlined.VideocamOff, "Activar o desactivar cámara de ejemplo") }
    }
    PatientButton("Finalizar vista previa", onEnd, Modifier.fillMaxWidth())
}

@Composable
fun RatingContent(onSubmit: (Int, String) -> Unit) {
    var rating by rememberSaveable { mutableIntStateOf(0) }
    var review by rememberSaveable { mutableStateOf("") }
    PatientHeading("¿Cómo fue tu experiencia?", "Tu opinión ayuda a otras personas a elegir.")
    PatientCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            (1..5).forEach { star ->
                IconToggleButton(rating >= star, { rating = star }) { Icon(if (rating >= star) Icons.Outlined.Star else Icons.Outlined.StarBorder, "$star estrellas", tint = if (rating >= star) Color(0xFF986500) else SlateSubtle) }
            }
        }
        OutlinedTextField(review, { review = it }, label = { Text("Reseña opcional") }, minLines = 4, modifier = Modifier.fillMaxWidth())
        PatientInfo("Tu reseña se presenta como anónima y se vincula a esta sesión.")
        PatientBadge("Vista previa · no se publica una reseña real", SurfaceContainerLow, PrimaryNavy)
    }
    PatientButton("Guardar calificación de ejemplo", { onSubmit(rating, review) }, Modifier.fillMaxWidth(), enabled = rating > 0)
}
