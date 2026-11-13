package com.mindcluster.safediary.scheduling.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mindcluster.safediary.shared.presentation.components.PatientBorder
import com.mindcluster.safediary.shared.presentation.theme.PrimaryNavy
import com.mindcluster.safediary.shared.presentation.theme.SecondaryTeal

@Composable
internal fun CoordinationMessageInput(message: String, onChange: (String) -> Unit, isWorking: Boolean, onSend: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = message, onValueChange = { if (it.length <= 500) onChange(it) },
            modifier = Modifier.weight(1f).heightIn(min = 56.dp),
            placeholder = { Text("Escribe un mensaje…") },
            shape = RoundedCornerShape(18.dp), maxLines = 4, enabled = !isWorking,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SecondaryTeal, unfocusedBorderColor = PatientBorder,
                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                cursorColor = SecondaryTeal, focusedTextColor = PrimaryNavy, unfocusedTextColor = PrimaryNavy
            )
        )
        FilledIconButton(
            onClick = onSend, enabled = message.isNotBlank() && !isWorking,
            modifier = Modifier.padding(bottom = 4.dp).size(48.dp), shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = SecondaryTeal, contentColor = Color.White)
        ) { Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Enviar mensaje") }
    }
}

@Composable
internal fun CoordinationComposer(message: String, onChange: (String) -> Unit, isWorking: Boolean, onSend: () -> Unit) {
    Surface(color = com.mindcluster.safediary.shared.presentation.theme.BackgroundSanctuary) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
            HorizontalDivider(color = PatientBorder)
            Box(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                CoordinationMessageInput(message, onChange, isWorking, onSend)
            }
        }
    }
}

@Composable
internal fun ObserveCoordinationUpdates(isActive: Boolean, conversationId: Long?, refresh: () -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val latestRefresh by rememberUpdatedState(refresh)
    LaunchedEffect(isActive, conversationId, lifecycle) {
        if (isActive && conversationId != null) lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) { delay(8000); latestRefresh() }
        }
    }
}
