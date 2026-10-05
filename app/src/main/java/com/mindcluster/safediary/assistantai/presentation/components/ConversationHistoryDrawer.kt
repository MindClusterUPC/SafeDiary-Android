package com.mindcluster.safediary.assistantai.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.vector.ImageVector
import com.mindcluster.safediary.shared.presentation.navigation.NavRoutes
import com.mindcluster.safediary.shared.presentation.theme.DrawerSurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mindcluster.safediary.R
import com.mindcluster.safediary.assistantai.domain.model.ConversationSummary
import com.mindcluster.safediary.shared.presentation.theme.DrawerBackground
import com.mindcluster.safediary.shared.presentation.theme.DrawerBorder
import com.mindcluster.safediary.shared.presentation.theme.PrimaryNavy
import com.mindcluster.safediary.shared.presentation.theme.SecondaryTeal
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private enum class HistoryGroup { TODAY, YESTERDAY, THIS_WEEK, OLDER }

/**
 * Conversation history drawer.
 */
@Composable
fun ConversationHistoryDrawer(
    conversations: List<ConversationSummary>,
    activeConversationId: String?,
    isLoading: Boolean,
    loadFailed: Boolean,
    onRetry: () -> Unit,
    onNewChat: () -> Unit,
    onConversationClick: (ConversationSummary) -> Unit,
    onClose: () -> Unit,
    onRenameConversation: (remoteId: String, newTitle: String) -> Unit = { _, _ -> },
    onDeleteConversation: (remoteId: String) -> Unit = {},
    onNavigateToRoute: (String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var renamingSummary by remember { mutableStateOf<ConversationSummary?>(null) }
    var renameTitleInput by remember { mutableStateOf("") }
    var deletingSummary by remember { mutableStateOf<ConversationSummary?>(null) }

    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val filtered = conversations.filter {
        searchQuery.isBlank() || (it.title ?: "").contains(searchQuery.trim(), ignoreCase = true)
    }
    val grouped = filtered.groupBy { summary ->
        val day = summary.lastActivityAt.atZone(zone).toLocalDate()
        when {
            day == today -> HistoryGroup.TODAY
            day == today.minusDays(1) -> HistoryGroup.YESTERDAY
            day.isAfter(today.minusDays(7)) -> HistoryGroup.THIS_WEEK
            else -> HistoryGroup.OLDER
        }
    }

    renamingSummary?.let { target ->
        AlertDialog(
            onDismissRequest = { renamingSummary = null },
            containerColor = DrawerSurface,
            title = {
                Text(
                    text = stringResource(R.string.chat_rename_dialog_title),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            },
            text = {
                OutlinedTextField(
                    value = renameTitleInput,
                    onValueChange = { if (it.length <= 80) renameTitleInput = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.chat_rename_dialog_hint)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SecondaryTeal,
                        unfocusedBorderColor = DrawerBorder,
                        focusedLabelColor = SecondaryTeal,
                        unfocusedLabelColor = Color(0xFF94A3B8)
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val text = renameTitleInput.trim()
                        if (text.isNotBlank()) {
                            onRenameConversation(target.remoteId, text)
                        }
                        renamingSummary = null
                    },
                    enabled = renameTitleInput.trim().isNotBlank()
                ) {
                    Text(stringResource(R.string.shared_button_save), color = SecondaryTeal)
                }
            },
            dismissButton = {
                TextButton(onClick = { renamingSummary = null }) {
                    Text(stringResource(R.string.shared_button_cancel), color = Color(0xFF94A3B8))
                }
            }
        )
    }

    deletingSummary?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingSummary = null },
            containerColor = DrawerSurface,
            title = {
                Text(
                    text = stringResource(R.string.chat_delete_dialog_title),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.chat_delete_dialog_body),
                    color = Color(0xFF94A3B8),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteConversation(target.remoteId)
                        deletingSummary = null
                    }
                ) {
                    Text(
                        text = stringResource(R.string.chat_delete_dialog_confirm),
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingSummary = null }) {
                    Text(stringResource(R.string.shared_button_cancel), color = Color(0xFF94A3B8))
                }
            }
        )
    }

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp),
        color = DrawerBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .statusBarsPadding()
                .navigationBarsPadding()
                .displayCutoutPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
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
                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.shared_button_cancel),
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(color = DrawerBorder, thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SecondaryTeal.copy(alpha = 0.18f))
                    .clickable { onNewChat() }
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EditNote,
                    contentDescription = null,
                    tint = Color(0xFF5EEAD4),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.chat_cd_new_chat_button),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            ) {
                DrawerNavigationItem(
                    label = stringResource(R.string.nav_home),
                    icon = Icons.Default.Home,
                    onClick = {
                        onClose()
                        onNavigateToRoute(NavRoutes.HOME)
                    }
                )
                DrawerNavigationItem(
                    label = stringResource(R.string.nav_emotions),
                    icon = Icons.Default.Insights,
                    onClick = {
                        onClose()
                        onNavigateToRoute(NavRoutes.EMOTIONS)
                    }
                )
                DrawerNavigationItem(
                    label = stringResource(R.string.nav_professionals),
                    icon = Icons.Default.MedicalServices,
                    onClick = {
                        onClose()
                        onNavigateToRoute(NavRoutes.PROFESSIONALS)
                    }
                )
                DrawerNavigationItem(
                    label = stringResource(R.string.nav_community),
                    icon = Icons.Default.Groups,
                    onClick = {
                        onClose()
                        onNavigateToRoute(NavRoutes.COMMUNITY)
                    }
                )
            }

            HorizontalDivider(color = DrawerBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = stringResource(R.string.chat_drawer_search_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B)
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        cursorBrush = SolidColor(SecondaryTeal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Text(
                text = stringResource(R.string.chat_drawer_recent),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFF64748B),
                modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 4.dp)
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    isLoading && conversations.isEmpty() -> CircularProgressIndicator(
                        color = SecondaryTeal,
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.TopCenter)
                            .padding(top = 4.dp)
                    )

                    loadFailed && conversations.isEmpty() -> Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.chat_drawer_error),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                        TextButton(onClick = onRetry) {
                            Text(text = stringResource(R.string.chat_retry), color = Color(0xFF5EEAD4))
                        }
                    }

                    filtered.isEmpty() -> Text(
                        text = stringResource(R.string.chat_drawer_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    )

                    else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                        HistoryGroup.entries.forEach { group ->
                            val items = grouped[group].orEmpty()
                            if (items.isNotEmpty()) {
                                item(key = "header_${group.name}") { GroupHeader(group) }
                                items(items, key = { it.remoteId }) { summary ->
                                    ConversationRow(
                                        summary = summary,
                                        isActive = summary.remoteId == activeConversationId,
                                        timeLabel = timeLabel(summary, group, zone),
                                        onClick = { onConversationClick(summary) },
                                        onRename = {
                                            renamingSummary = summary
                                            renameTitleInput = summary.title ?: ""
                                        },
                                        onDelete = {
                                            deletingSummary = summary
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = DrawerBorder, thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onClose()
                        onNavigateToSettings()
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SecondaryTeal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = stringResource(R.string.shared_cd_user_avatar),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.nav_settings),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.shared_cd_settings),
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun GroupHeader(group: HistoryGroup) {
    val label = when (group) {
        HistoryGroup.TODAY -> R.string.chat_drawer_group_today
        HistoryGroup.YESTERDAY -> R.string.chat_drawer_group_yesterday
        HistoryGroup.THIS_WEEK -> R.string.chat_drawer_group_this_week
        HistoryGroup.OLDER -> R.string.chat_drawer_group_older
    }
    Text(
        text = stringResource(label),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        color = Color(0xFF64748B),
        modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 6.dp)
    )
}

@Composable
private fun ConversationRow(
    summary: ConversationSummary,
    isActive: Boolean,
    timeLabel: String,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isActive) Color.White.copy(alpha = 0.10f) else Color.Transparent)
            .clickable { onClick() }
            .padding(start = 12.dp, top = 6.dp, bottom = 6.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ChatBubbleOutline,
            contentDescription = null,
            tint = Color(0xFF64748B),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = summary.title ?: stringResource(R.string.chat_drawer_untitled),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFE2E8F0),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = timeLabel,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF64748B)
        )
        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.chat_cd_more_options),
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(DrawerSurface)
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.chat_conversation_rename), color = Color.White) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    onClick = {
                        showMenu = false
                        onRename()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.chat_conversation_delete), color = Color(0xFFEF4444)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    onClick = {
                        showMenu = false
                        onDelete()
                    }
                )
            }
        }
    }
}

private fun timeLabel(summary: ConversationSummary, group: HistoryGroup, zone: ZoneId): String {
    val dateTime = summary.lastActivityAt.atZone(zone)
    val pattern = if (group == HistoryGroup.TODAY || group == HistoryGroup.YESTERDAY) "HH:mm" else "d MMM"
    return dateTime.format(DateTimeFormatter.ofPattern(pattern))
}

@Composable
private fun DrawerNavigationItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = Color(0xFFE2E8F0)
        )
    }
}
