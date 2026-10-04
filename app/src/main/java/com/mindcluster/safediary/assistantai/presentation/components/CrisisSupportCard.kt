package com.mindcluster.safediary.assistantai.presentation.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindcluster.safediary.R
import com.mindcluster.safediary.assistantai.domain.model.CrisisResource
import com.mindcluster.safediary.shared.presentation.theme.OnSurface
import com.mindcluster.safediary.shared.presentation.theme.OnSurfaceVariant
import com.mindcluster.safediary.shared.presentation.theme.PrimaryNavy
import com.mindcluster.safediary.shared.presentation.theme.SecondaryContainer
import com.mindcluster.safediary.shared.presentation.theme.SecondaryTeal
import com.mindcluster.safediary.shared.presentation.theme.SurfaceContainerLow

/**
 * Support card shown when the assistant detects high or critical risk.
 * It only opens the phone dialer; the call is never placed automatically.
 */
@Composable
fun CrisisSupportCard(
    resources: List<CrisisResource>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SecondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = SecondaryTeal,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.chat_crisis_title),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )
                    Text(
                        text = stringResource(R.string.chat_crisis_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.chat_crisis_dismiss),
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            resources.forEach { resource ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = resource.name,
                            style = MaterialTheme.typography.labelLarge,
                            color = OnSurface
                        )
                        if (resource.description.isNotBlank()) {
                            Text(
                                text = resource.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(PrimaryNavy)
                            .clickable {
                                val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${resource.phone}"))
                                try {
                                    context.startActivity(dial)
                                } catch (_: ActivityNotFoundException) {
                                    // No dialer available (e.g. tablet); the number stays visible on screen.
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.chat_crisis_call, resource.phone),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}
