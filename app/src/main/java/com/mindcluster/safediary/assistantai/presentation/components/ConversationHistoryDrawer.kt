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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.sp
import com.mindcluster.safediary.R
import com.mindcluster.safediary.assistantai.domain.model.ConversationSummary
import com.mindcluster.safediary.shared.presentation.theme.DeleteActionRed
import com.mindcluster.safediary.shared.presentation.theme.DrawerBackgroundSlate
import com.mindcluster.safediary.shared.presentation.theme.DrawerBorder
import com.mindcluster.safediary.shared.presentation.theme.DrawerIconSquareBg
import com.mindcluster.safediary.shared.presentation.theme.DrawerNewChatBg
import com.mindcluster.safediary.shared.presentation.theme.DrawerSurfaceSlate
import com.mindcluster.safediary.shared.presentation.theme.DrawerTealAccent
import com.mindcluster.safediary.shared.presentation.theme.DrawerTitleLight
import com.mindcluster.safediary.shared.presentation.theme.SecondaryTeal
import com.mindcluster.safediary.shared.presentation.theme.SlatePlaceholder
import com.mindcluster.safediary.shared.presentation.theme.SlateSubtle
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private enum class HistoryGroup { TODAY, YESTERDAY, OLDER }

/**
 * Conversation history drawer styled to Figma specifications.
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
            else -> HistoryGroup.OLDER
        }
    }

    renamingSummary?.let { target ->
        AlertDialog(
            onDismissRequest = { renamingSummary = null },
            containerColor = DrawerSurfaceSlate,
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
                        unfocusedLabelColor = SlatePlaceholder
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
                    Text(stringResource(R.string.shared_button_cancel), color = SlatePlaceholder)
                }
            }
        )
    }

    deletingSummary?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingSummary = null },
            containerColor = DrawerSurfaceSlate,
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
                    color = SlatePlaceholder,
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
                        color = DeleteActionRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingSummary = null }) {
                    Text(stringResource(R.string.shared_button_cancel), color = SlatePlaceholder)
                }
            }
        )
    }

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(308.dp),
        color = DrawerBackgroundSlate
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .statusBarsPadding()
                .navigationBarsPadding()
                .displayCutoutPadding()
        ) {
            // Top row: 32dp square with corner radius 9, title, close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
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
                            .clip(RoundedCornerShape(9.dp))
                            .background(DrawerIconSquareBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = DrawerTealAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.chat_drawer_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.shared_button_cancel),
                        tint = SlatePlaceholder,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // "Nueva conversación" button: Size 280x43, corner radius 14, background #0E2A2E
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .fillMaxWidth()
                    .height(43.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DrawerNewChatBg)
                    .clickable { onNewChat() }
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = DrawerTealAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.chat_cd_new_chat_button),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search field: background #1E293B, corner radius 14, placeholder #94A3B8
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DrawerSurfaceSlate)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = SlatePlaceholder,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = stringResource(R.string.chat_drawer_search_hint),
                            fontSize = 14.sp,
                            color = SlatePlaceholder
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            color = Color.White
                        ),
                        cursorBrush = SolidColor(DrawerTealAccent),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // "Recientes" label: Medium 12, #94A3B8
            Text(
                text = stringResource(R.string.chat_drawer_recent),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = SlatePlaceholder,
                modifier = Modifier.padding(start = 18.dp, top = 12.dp, bottom = 4.dp)
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    isLoading && conversations.isEmpty() -> CircularProgressIndicator(
                        color = SecondaryTeal,
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    )

                    loadFailed && conversations.isEmpty() -> Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.chat_drawer_error),
                            fontSize = 13.sp,
                            color = SlatePlaceholder,
                            textAlign = TextAlign.Center
                        )
                        TextButton(onClick = onRetry) {
                            Text(text = stringResource(R.string.chat_retry), color = DrawerTealAccent)
                        }
                    }

                    filtered.isEmpty() -> Text(
                        text = stringResource(R.string.chat_drawer_empty),
                        fontSize = 13.sp,
                        color = SlatePlaceholder,
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
                                        timeLabel = timeLabel(summary, zone),
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
        }
    }
}

@Composable
private fun GroupHeader(group: HistoryGroup) {
    val label = when (group) {
        HistoryGroup.TODAY -> R.string.chat_drawer_group_today
        HistoryGroup.YESTERDAY -> R.string.chat_drawer_group_yesterday
        HistoryGroup.OLDER -> R.string.chat_drawer_group_older
    }
    Text(
        text = stringResource(label),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = SlateSubtle,
        modifier = Modifier.padding(start = 18.dp, top = 10.dp, bottom = 4.dp)
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
            .padding(horizontal = 14.dp, vertical = 2.dp)
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) DrawerSurfaceSlate else Color.Transparent)
            .clickable { onClick() }
            .padding(start = 10.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ChatBubbleOutline,
            contentDescription = null,
            tint = SlatePlaceholder,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = summary.title ?: stringResource(R.string.chat_drawer_untitled),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = DrawerTitleLight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = timeLabel,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = SlatePlaceholder
        )
        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.chat_cd_more_options),
                    tint = SlatePlaceholder,
                    modifier = Modifier.size(16.dp)
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                shape = RoundedCornerShape(12.dp),
                containerColor = DrawerSurfaceSlate
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.chat_conversation_rename),
                            color = Color.White
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = SlatePlaceholder,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    onClick = {
                        showMenu = false
                        onRename()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.chat_conversation_delete),
                            color = DeleteActionRed
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = DeleteActionRed,
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

private fun timeLabel(summary: ConversationSummary, zone: ZoneId): String {
    val dateTime = summary.lastActivityAt.atZone(zone)
    return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
}
