package com.mindcluster.safediary.scheduling.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mindcluster.safediary.professionals.infrastructure.local.PreviewProfessionals
import com.mindcluster.safediary.scheduling.domain.model.*
import com.mindcluster.safediary.shared.presentation.components.*
import com.mindcluster.safediary.shared.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun appointmentDate(time: Long): String = SimpleDateFormat("EEE d MMM · HH:mm", Locale.forLanguageTag("es-PE")).apply { timeZone = TimeZone.getTimeZone("America/Lima") }.format(Date(time))
fun reservationTime(seconds: Long) = "%02d:%02d".format(seconds / 60, seconds % 60)
fun AppointmentStatus.label() = when (this) {
    AppointmentStatus.RESERVED -> "◷ Reserva temporal"
    AppointmentStatus.CONFIRMED -> "✓ Confirmada"
    AppointmentStatus.COMPLETED -> "✓ Completada"
    AppointmentStatus.CANCELLED -> "Cancelada"
    AppointmentStatus.EXPIRED -> "Reserva vencida"
}

@Composable
fun AppointmentCard(appointment: Appointment, now: Long, onDetail: () -> Unit, onPay: () -> Unit, onJoin: () -> Unit) {
    val professional = PreviewProfessionals.find(appointment.professionalId)
    PatientCard {
        PatientBadge(appointment.status.label(), if (appointment.status == AppointmentStatus.EXPIRED) ErrorContainer else PatientMint, if (appointment.status == AppointmentStatus.EXPIRED) ErrorRed else SecondaryTeal)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            PatientAvatar(professional.initials)
            Column { Text(professional.name, style = MaterialTheme.typography.titleMedium, color = PrimaryNavy); PatientInfo(appointmentDate(appointment.startsAt) + " · 50 min") }
        }
        HorizontalDivider(color = PatientBorder)
        when (appointment.status) {
            AppointmentStatus.RESERVED -> {
                PatientInfo("Aún no confirmada · vence en ${reservationTime(appointment.remainingSeconds(now))}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PatientButton("Ver reserva", onDetail, Modifier.weight(1f), secondary = true)
                    PatientButton("Pagar S/ ${professional.price}", onPay, Modifier.weight(1f))
                }
            }
            AppointmentStatus.CONFIRMED -> {
                PatientInfo("Videollamada · acceso 10 min antes", Icons.Outlined.Videocam)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PatientButton("Ver detalle", onDetail, Modifier.weight(1f), secondary = true)
                    PatientButton("Ingresar", onJoin, Modifier.weight(1f), enabled = appointment.canJoin(now))
                }
            }
            else -> PatientButton(if (appointment.status == AppointmentStatus.COMPLETED && appointment.rating == null) "Calificar sesión" else "Ver detalle", onDetail, Modifier.fillMaxWidth(), secondary = true)
        }
    }
}
