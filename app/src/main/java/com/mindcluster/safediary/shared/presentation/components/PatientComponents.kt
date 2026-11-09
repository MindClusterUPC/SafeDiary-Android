package com.mindcluster.safediary.shared.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindcluster.safediary.R
import com.mindcluster.safediary.shared.presentation.theme.*

val PatientMint = Color(0xFFDFF5EF)
val PatientBorder = Color(0xFFE3E8F0)

/** The pager owns bottom navigation; each patient tab owns its content and header. */
@Composable
fun PatientScreen(section: String, modifier: Modifier = Modifier, onBack: (() -> Unit)? = null, onSettings: () -> Unit = {}, contentKey: Any = section, bottomBar: (@Composable () -> Unit)? = null, scrollToBottomKey: Any? = null, content: @Composable ColumnScope.() -> Unit) {
    val scrollState = key(contentKey) { rememberScrollState() }
    LaunchedEffect(scrollToBottomKey, scrollState.maxValue) {
        if (scrollToBottomKey != null) scrollState.scrollTo(scrollState.maxValue)
    }
    Column(modifier.fillMaxSize().background(BackgroundSanctuary).statusBarsPadding().displayCutoutPadding().imePadding()) {
        Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Volver", tint = PrimaryNavy) }
            Image(painterResource(R.drawable.safediary_logo), "SafeDiary", Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)))
            Spacer(Modifier.width(8.dp))
            Text("SafeDiary", color = PrimaryNavy, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(" / $section", Modifier.weight(1f), color = SlateSubtle, style = MaterialTheme.typography.bodySmall)
            IconButton(onClick = onSettings) { Icon(Icons.Outlined.Person, "Perfil y configuración", Modifier.size(28.dp), tint = PrimaryNavy) }
        }
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(scrollState).padding(horizontal = 16.dp).padding(top = 16.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
        bottomBar?.invoke()
    }
}

@Composable
fun PatientHeading(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, color = PrimaryNavy, fontWeight = FontWeight.Bold)
        if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = SlateSubtle)
    }
}

@Composable
fun PatientCard(modifier: Modifier = Modifier, color: Color = Color.White, content: @Composable ColumnScope.() -> Unit) {
    SafeDiaryCard(modifier.fillMaxWidth(), cornerRadius = 20.dp, containerColor = color, borderColor = PatientBorder.copy(alpha = .5f)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
fun PatientButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, secondary: Boolean = false, enabled: Boolean = true) {
    Button(onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = 48.dp), shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = if (secondary) SurfaceContainerLow else PrimaryNavy, contentColor = if (secondary) PrimaryNavy else Color.White)
    ) { Text(text, style = MaterialTheme.typography.labelLarge) }
}

@Composable
fun PatientBadge(text: String, color: Color = PatientMint, foreground: Color = SecondaryTeal) {
    Surface(color = color, shape = CircleShape) { Text(text, Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelMedium, color = foreground) }
}

@Composable
fun PatientAvatar(initials: String, modifier: Modifier = Modifier) {
    Box(modifier.size(56.dp).background(SurfaceContainerHigh, CircleShape), contentAlignment = Alignment.Center) { Text(initials, color = PrimaryNavy, fontWeight = FontWeight.Bold) }
}

@Composable
fun PatientInfo(text: String, icon: ImageVector? = null) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) Icon(icon, null, Modifier.size(18.dp), tint = SecondaryTeal)
        Text(text, style = MaterialTheme.typography.bodySmall, color = SlateSubtle)
    }
}
