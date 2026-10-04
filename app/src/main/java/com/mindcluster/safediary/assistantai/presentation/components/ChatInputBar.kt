package com.mindcluster.safediary.assistantai.presentation.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mindcluster.safediary.R
import com.mindcluster.safediary.shared.presentation.theme.MicGradientEnd
import com.mindcluster.safediary.shared.presentation.theme.MicGradientStart
import com.mindcluster.safediary.shared.presentation.theme.OnSurface
import com.mindcluster.safediary.shared.presentation.theme.OnSurfaceVariant
import com.mindcluster.safediary.shared.presentation.theme.PrimaryNavy
import com.mindcluster.safediary.shared.presentation.theme.SecondaryTeal
import com.mindcluster.safediary.shared.presentation.theme.SurfaceContainerHighest
import com.mindcluster.safediary.shared.presentation.theme.SurfaceContainerLow
import com.mindcluster.safediary.shared.presentation.theme.SurfaceContainerLowest

@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isTyping: Boolean,
    modifier: Modifier = Modifier,
    onAddContextClick: () -> Unit = {}
) {
    var isRecordingSimulated by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(56.dp),
        shape = RoundedCornerShape(9999.dp),
        color = SurfaceContainerLowest,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceContainerHighest),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Plus / Add Context Action
            IconButton(
                onClick = onAddContextClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.chat_cd_add_context),
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Text Input
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.chat_input_placeholder_voice),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = OnSurface),
                    cursorBrush = SolidColor(PrimaryNavy),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (value.isNotBlank() && !isTyping) onSend()
                    }),
                    enabled = !isTyping
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Selector Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(SurfaceContainerLow)
                    .clickable { /* selector */ }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Glowing Mic / Send Action Button
            val hasText = value.isNotBlank()
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .scale(if (isRecordingSimulated) pulseScale else 1f)
                    .clip(CircleShape)
                    .background(
                        if (hasText) {
                            Brush.horizontalGradient(listOf(PrimaryNavy, SecondaryTeal))
                        } else {
                            Brush.horizontalGradient(listOf(MicGradientStart, MicGradientEnd))
                        }
                    )
                    .clickable {
                        if (hasText && !isTyping) {
                            onSend()
                        } else {
                            isRecordingSimulated = !isRecordingSimulated
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (hasText) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                    contentDescription = stringResource(if (hasText) R.string.chat_cd_send else R.string.chat_cd_mic),
                    tint = if (hasText) Color.White else Color(0xFF030914),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
