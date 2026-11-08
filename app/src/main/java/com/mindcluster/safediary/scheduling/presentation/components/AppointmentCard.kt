package com.mindcluster.safediary.scheduling.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    AppointmentStatus.REQUESTED -> "◷ Horario propuesto"
    AppointmentStatus.HELD -> "◷ Reserva temporal"
    AppointmentStatus.CONFIRMED -> "✓ Confirmada"
    AppointmentStatus.COMPLETED -> "✓ Completada"
    AppointmentStatus.CANCELLED -> "Cancelada"
    AppointmentStatus.EXPIRED -> "Reserva vencida"
}

@Composable
fun AppointmentCard(appointment: Appointment, clinician: ClinicianSummary, now: Long, onDetail: () -> Unit, onJoin: () -> Unit) {
    PatientCard {
        PatientBadge(appointment.status.label(), if (appointment.status == AppointmentStatus.EXPIRED) ErrorContainer else PatientMint, if (appointment.status == AppointmentStatus.EXPIRED) ErrorRed else SecondaryTeal)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            PatientAvatar(clinician.initials)
            Column { Text(clinician.name, style = MaterialTheme.typography.titleMedium, color = PrimaryNavy); PatientInfo(appointmentDate(appointment.startsAt) + " · ${appointment.durationMinutes} min") }
        }
        HorizontalDivider(color = PatientBorder)
        when (appointment.status) {
            AppointmentStatus.HELD -> {
                PatientInfo("Aún no confirmada · vence en ${reservationTime(appointment.remainingSeconds(now))}")
                PatientButton("Ver reserva", onDetail, Modifier.fillMaxWidth())
            }
            AppointmentStatus.CONFIRMED -> {
                PatientInfo("Videollamada en SafeDiary", Icons.Outlined.Videocam)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PatientButton("Ver detalle", onDetail, Modifier.weight(1f), secondary = true)
                    PatientButton("Ingresar", onJoin, Modifier.weight(1f), enabled = appointment.canJoin(now))
                }
            }
            else -> PatientButton("Ver detalle", onDetail, Modifier.fillMaxWidth(), secondary = true)
        }
    }
}
