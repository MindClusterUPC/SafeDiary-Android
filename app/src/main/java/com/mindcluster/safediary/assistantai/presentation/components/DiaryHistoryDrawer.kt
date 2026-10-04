package com.mindcluster.safediary.assistantai.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindcluster.safediary.R
import com.mindcluster.safediary.shared.presentation.theme.DrawerBackground
import com.mindcluster.safediary.shared.presentation.theme.DrawerBorder
import com.mindcluster.safediary.shared.presentation.theme.DrawerSurface
import com.mindcluster.safediary.shared.presentation.theme.PrimaryNavy
import com.mindcluster.safediary.shared.presentation.theme.SecondaryTeal

data class DiaryHistoryEntry(
    val id: String,
    val title: String,
    val timeLabel: String,
    val tag: String,
    val tagColor: Color,
    val duration: String
)

@Composable
fun DiaryHistoryDrawer(
    onCloseClick: () -> Unit,
    onNewDiaryClick: () -> Unit,
    onEntryClick: (DiaryHistoryEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val todayEntries = listOf(
        DiaryHistoryEntry(
            id = "1",
            title = stringResource(R.string.chat_suggestion_vent),
            timeLabel = "21:30",
            tag = stringResource(R.string.chat_tag_calm),
            tagColor = Color(0xFF14B8A6),
            duration = "4m 12s"
        ),
        DiaryHistoryEntry(
            id = "2",
            title = stringResource(R.string.chat_suggestion_summary),
            timeLabel = "08:15",
            tag = stringResource(R.string.chat_tag_positive),
            tagColor = Color(0xFF10B981),
            duration = "2m 45s"
        )
    )

    val yesterdayEntries = listOf(
        DiaryHistoryEntry(
            id = "3",
            title = "Sesión guiada CBT: Ansiedad",
            timeLabel = "19:40",
            tag = stringResource(R.string.chat_tag_vent),
            tagColor = Color(0xFF0EA5E9),
            duration = "6m 08s"
        )
    )

    val thisWeekEntries = listOf(
        DiaryHistoryEntry(
            id = "4",
            title = stringResource(R.string.chat_suggestion_sleep),
            timeLabel = "14 Oct",
            tag = stringResource(R.string.chat_tag_creative),
            tagColor = Color(0xFFA855F7),
            duration = "5m 20s"
        ),
        DiaryHistoryEntry(
            id = "5",
            title = "Victoria personal en equipo",
            timeLabel = "12 Oct",
            tag = stringResource(R.string.chat_tag_pride),
            tagColor = Color(0xFFF59E0B),
            duration = "3m 50s"
        )
    )

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp),
        color = DrawerBackground
    ) {
        Column(
            modifier = Modifier.fillMaxHeight()
        ) {
            // Drawer Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SecondaryTeal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.chat_drawer_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                }

                IconButton(
                    onClick = onCloseClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.shared_button_cancel),
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(color = DrawerBorder, thickness = 1.dp)

            // New Voice Diary Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .displayCutoutPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(Color(0xFF14B8A6).copy(alpha = 0.15f))
                    .clickable { onNewDiaryClick() }
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddComment,
                        contentDescription = null,
                        tint = Color(0xFF5EEAD4),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.chat_drawer_new_diary),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF5EEAD4)
                        )
                    )
                }
            }

            // Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .displayCutoutPadding()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(9999.dp),
                color = DrawerSurface,
                border = BorderStroke(1.dp, DrawerBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.chat_drawer_search_hint),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable Grouped History Lists
            Column(
                modifier = Modifier
                    .weight(1f)
                    .displayCutoutPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DiaryGroupSection(
                    title = stringResource(R.string.chat_drawer_group_today),
                    entries = todayEntries,
                    onEntryClick = onEntryClick
                )

                DiaryGroupSection(
                    title = stringResource(R.string.chat_drawer_group_yesterday),
                    entries = yesterdayEntries,
                    onEntryClick = onEntryClick
                )

                DiaryGroupSection(
                    title = stringResource(R.string.chat_drawer_group_this_week),
                    entries = thisWeekEntries,
                    onEntryClick = onEntryClick
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Drawer Footer
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF09101D),
                border = BorderStroke(1.dp, DrawerBorder)
            ) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .displayCutoutPadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.chat_drawer_security_notice),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8)
                            )
                        )
                        Text(
                            text = stringResource(R.string.chat_drawer_private_badge),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF2DD4BF),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DrawerSurface)
                            .clickable { /* decorative export */ }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color(0xFFCBD5E1),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.chat_drawer_export_btn),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFCBD5E1)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaryGroupSection(
    title: String,
    entries: List<DiaryHistoryEntry>,
    onEntryClick: (DiaryHistoryEntry) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            ),
            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
        )

        entries.forEach { entry ->
            DiaryEntryCard(entry = entry, onClick = { onEntryClick(entry) })
        }
    }
}

@Composable
private fun DiaryEntryCard(
    entry: DiaryHistoryEntry,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E293B).copy(alpha = 0.45f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE2E8F0)
                    ),
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = entry.timeLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF64748B)
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Emotion Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(entry.tagColor.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = entry.tag,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = entry.tagColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        )
                    )
                }

                // Mic duration
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = entry.duration,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}
