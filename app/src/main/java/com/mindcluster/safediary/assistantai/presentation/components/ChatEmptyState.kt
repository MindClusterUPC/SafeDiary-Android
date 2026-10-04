package com.mindcluster.safediary.assistantai.presentation.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindcluster.safediary.R
import com.mindcluster.safediary.shared.presentation.theme.OnSurface
import com.mindcluster.safediary.shared.presentation.theme.OnSurfaceVariant
import com.mindcluster.safediary.shared.presentation.theme.OutlineVariantBorder
import com.mindcluster.safediary.shared.presentation.theme.PrimaryNavy
import com.mindcluster.safediary.shared.presentation.theme.SecondaryTeal
import com.mindcluster.safediary.shared.presentation.theme.SurfaceContainerLow
import com.mindcluster.safediary.shared.presentation.theme.SurfaceContainerLowest

data class SuggestionItem(
    val emoji: String,
    val text: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatEmptyState(
    onSuggestionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_dot")
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_scale"
    )

    val items = listOf(
        SuggestionItem("✨", stringResource(R.string.chat_suggestion_summary)),
        SuggestionItem("🧘", stringResource(R.string.chat_suggestion_vent)),
        SuggestionItem("💭", stringResource(R.string.chat_suggestion_sleep)),
        SuggestionItem("🎯", stringResource(R.string.chat_suggestion_reflection))
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Top Encryption Badge Subtle Pill
        Surface(
            shape = RoundedCornerShape(9999.dp),
            color = SurfaceContainerLow,
            border = BorderStroke(1.dp, OutlineVariantBorder.copy(alpha = 0.35f)),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .scale(dotScale)
                        .clip(CircleShape)
                        .background(SecondaryTeal)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = SecondaryTeal,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.chat_voice_e2ee_badge),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        letterSpacing = 0.3.sp
                    ),
                    color = OnSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Gemini-style Warm Greeting Heading in Deep Primary Navy
        Text(
            text = stringResource(R.string.chat_greeting_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 26.sp,
                letterSpacing = (-0.5).sp
            ),
            color = PrimaryNavy,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Suggestion Action Pills Wrapped
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEach { item ->
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = SurfaceContainerLowest,
                    border = BorderStroke(1.dp, OutlineVariantBorder.copy(alpha = 0.4f)),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .clickable { onSuggestionSelected(item.text) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = item.emoji, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.text,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp
                            ),
                            color = OnSurface
                        )
                    }
                }
            }
        }
    }
}
