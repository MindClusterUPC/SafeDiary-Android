package com.mindcluster.safediary.professionals.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindcluster.safediary.professionals.domain.model.Professional
import com.mindcluster.safediary.shared.presentation.components.*
import com.mindcluster.safediary.shared.presentation.theme.*

@Composable
fun ProfessionalCard(professional: Professional, favorite: Boolean, onFavorite: () -> Unit, onProfile: () -> Unit, onContact: () -> Unit) {
    PatientCard {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            PatientAvatar(professional.initials)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(professional.name, style = MaterialTheme.typography.titleLarge, color = PrimaryNavy)
                PatientInfo(professional.specialty)
                PatientBadge("✓ Licencia verificada")
            }
            IconToggleButton(checked = favorite, onCheckedChange = { onFavorite() }) {
                Icon(if (favorite) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder, if (favorite) "Quitar de guardados" else "Guardar especialista", tint = SecondaryTeal)
            }
        }
        PatientInfo("★ ${professional.rating} · ${professional.reviewCount} reseñas · ${professional.languages}")
        Surface(color = SurfaceContainerLow, shape = MaterialTheme.shapes.medium) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { PatientInfo("Disponibilidad", Icons.Outlined.CalendarMonth); Text(professional.availability, color = PrimaryNavy, style = MaterialTheme.typography.bodySmall) }
                Column(horizontalAlignment = Alignment.End) { PatientInfo("Tarifa / 50 min"); Text("S/ ${professional.price}", fontWeight = FontWeight.Bold, color = PrimaryNavy) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PatientButton("Ver perfil", onProfile, Modifier.weight(1f))
            PatientButton("Contactar", onContact, Modifier.weight(1f), secondary = true)
        }
    }
}
