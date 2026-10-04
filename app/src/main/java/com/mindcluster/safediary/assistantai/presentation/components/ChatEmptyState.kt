package com.mindcluster.safediary.assistantai.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mindcluster.safediary.R
import com.mindcluster.safediary.shared.presentation.components.SafeDiaryCard
import com.mindcluster.safediary.shared.presentation.theme.OnSurface
import com.mindcluster.safediary.shared.presentation.theme.OnSurfaceVariant
import com.mindcluster.safediary.shared.presentation.theme.OutlineVariantBorder
import com.mindcluster.safediary.shared.presentation.theme.PillShape
import com.mindcluster.safediary.shared.presentation.theme.PrimaryNavy
import com.mindcluster.safediary.shared.presentation.theme.SecondaryTeal
import com.mindcluster.safediary.shared.presentation.theme.SurfaceContainerHigh
import com.mindcluster.safediary.shared.presentation.theme.SurfaceContainerLow
import com.mindcluster.safediary.shared.presentation.theme.SurfaceContainerLowest

@Composable
fun ChatEmptyState(
    suggestions: List<String>,
    onSuggestionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // E2EE Privacy Shield Badge (From Serene Sanctuary & Mockups)
        Surface(
            shape = PillShape,
            color = SurfaceContainerHigh,
            border = BorderStroke(1.dp, OutlineVariantBorder.copy(alpha = 0.3f)),
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = SecondaryTeal,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.chat_e2ee_badge),
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryNavy,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "• " + stringResource(R.string.chat_e2ee_protocol),
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryTeal,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // AI Welcoming Orb / Avatar
        Surface(
            shape = CircleShape,
            color = SecondaryTeal,
            modifier = Modifier.size(54.dp),
            shadowElevation = 2.dp
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .padding(14.dp)
                    .size(26.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = stringResource(R.string.chat_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            color = PrimaryNavy,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.chat_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Suggestion Cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            suggestions.take(3).forEach { suggestion ->
                SafeDiaryCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSuggestionSelected(suggestion) },
                    cornerRadius = 14.dp,
                    containerColor = SurfaceContainerLowest,
                    contentPadding = 14.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = SecondaryTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
