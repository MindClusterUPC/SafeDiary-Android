package com.mindcluster.safediary.assistantai.presentation.views

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindcluster.safediary.R
import com.mindcluster.safediary.assistantai.infrastructure.local.PersonalityPreferences
import com.mindcluster.safediary.assistantai.presentation.components.ChatEmptyState
import com.mindcluster.safediary.assistantai.presentation.components.ChatInputBar
import com.mindcluster.safediary.assistantai.presentation.components.CrisisSupportCard
import com.mindcluster.safediary.assistantai.presentation.components.ConversationHistoryDrawer
import com.mindcluster.safediary.assistantai.presentation.components.MessageBubble
import com.mindcluster.safediary.assistantai.presentation.components.ReplyErrorNotice
import com.mindcluster.safediary.assistantai.presentation.components.TypingIndicator
import com.mindcluster.safediary.assistantai.domain.model.MessageAuthor
import com.mindcluster.safediary.assistantai.presentation.viewmodels.AssistantViewModel
import com.mindcluster.safediary.shared.presentation.theme.BackgroundSanctuary
import com.mindcluster.safediary.shared.presentation.theme.OnSurface
import com.mindcluster.safediary.shared.presentation.theme.OnSurfaceVariant
import com.mindcluster.safediary.shared.presentation.theme.PrimaryNavy
import com.mindcluster.safediary.shared.presentation.theme.SecondaryContainer
import com.mindcluster.safediary.shared.presentation.theme.SecondaryTeal
import com.mindcluster.safediary.shared.presentation.theme.SurfaceContainer
import com.mindcluster.safediary.shared.presentation.theme.SurfaceContainerLowest
import kotlinx.coroutines.launch

@Composable
fun AiChatScreen(
    viewModel: AssistantViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToRoute: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    var currentPersonality by remember {
        mutableStateOf(PersonalityPreferences(context).get())
    }

    LifecycleResumeEffect(Unit) {
        currentPersonality = PersonalityPreferences(context).get()
        onPauseOrDispose { }
    }

    val view = LocalView.current
    val isDrawerOpen = drawerState.isOpen || drawerState.targetValue == DrawerValue.Open
    DisposableEffect(isDrawerOpen) {
        val window = (view.context as? Activity)?.window
        window?.let {
            val controller = WindowCompat.getInsetsController(it, view)
            controller.isAppearanceLightStatusBars = !isDrawerOpen
            controller.isAppearanceLightNavigationBars = !isDrawerOpen
        }
        onDispose {
            val window = (view.context as? Activity)?.window
            window?.let {
                val controller = WindowCompat.getInsetsController(it, view)
                controller.isAppearanceLightStatusBars = true
                controller.isAppearanceLightNavigationBars = true
            }
        }
    }

    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen) viewModel.refreshHistory()
    }

    LaunchedEffect(uiState.messages.size, uiState.isTyping, uiState.replyFailed) {
        if (uiState.messages.isNotEmpty()) {
            val extraItems = (if (uiState.isTyping) 1 else 0) + (if (uiState.replyFailed) 1 else 0)
            val totalCount = uiState.messages.size + extraItems
            listState.animateScrollToItem(totalCount - 1)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ConversationHistoryDrawer(
                conversations = uiState.conversations,
                activeConversationId = uiState.activeConversationId,
                isLoading = uiState.isHistoryLoading,
                loadFailed = uiState.historyFailed,
                onRetry = { viewModel.refreshHistory() },
                onNewChat = {
                    viewModel.newChat()
                    coroutineScope.launch { drawerState.close() }
                },
                onConversationClick = { summary ->
                    viewModel.openConversation(summary.remoteId)
                    coroutineScope.launch { drawerState.close() }
                },
                onRenameConversation = { remoteId, newTitle ->
                    viewModel.renameConversation(remoteId, newTitle)
                },
                onDeleteConversation = { remoteId ->
                    viewModel.deleteConversation(remoteId)
                },
                onNavigateToRoute = onNavigateToRoute,
                onNavigateToSettings = onNavigateToSettings,
                onClose = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = BackgroundSanctuary,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceContainerLowest)
                        .statusBarsPadding()
                        .displayCutoutPadding()
                ) {
                    // Header Bar (Matching HTML 1:1)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Hamburger + Logo + Brand
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { coroutineScope.launch { drawerState.open() } },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = stringResource(R.string.shared_cd_menu),
                                    tint = PrimaryNavy,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

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

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.shared_topbar_title_prefix),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = PrimaryNavy
                                    )
                                )
                                Text(
                                    text = "/",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF94A3B8)
                                    )
                                )
                                Text(
                                    text = stringResource(R.string.chat_header_subtitle, currentPersonality.apiName),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = SecondaryTeal
                                    )
                                )
                            }
                        }

                        // Right: New Chat Pencil + Profile Avatar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.newChat() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = stringResource(R.string.chat_cd_new_chat_button),
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SecondaryContainer)
                                    .clickable { onNavigateToSettings() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = stringResource(R.string.shared_cd_user_avatar),
                                    tint = PrimaryNavy,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = SurfaceContainer, thickness = 1.dp)
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .displayCutoutPadding()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (uiState.messages.isEmpty()) {
                        ChatEmptyState(
                            onSuggestionSelected = { suggestion ->
                                viewModel.onSuggestionSelected(suggestion)
                            },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val lastAssistantIndex = uiState.messages.indexOfLast { it.author == MessageAuthor.ASSISTANT }
                            items(uiState.messages, key = { it.id }) { message ->
                                val isLastAssistant = message.author == MessageAuthor.ASSISTANT &&
                                    uiState.messages.indexOf(message) == lastAssistantIndex
                                MessageBubble(
                                    message = message,
                                    isLastAssistantReply = isLastAssistant,
                                    isTyping = uiState.isTyping,
                                    onEditMessage = { msg -> viewModel.startEditing(msg) },
                                    onRegenerate = { viewModel.regenerateLastReply() }
                                )
                            }
                            if (uiState.isTyping) {
                                item(key = "typing_indicator") {
                                    TypingIndicator()
                                }
                            }
                            if (uiState.replyFailed && !uiState.isTyping) {
                                item(key = "reply_error") {
                                    ReplyErrorNotice(onRetry = { viewModel.retry() })
                                }
                            }
                        }
                    }
                }

                if (uiState.crisisResources.isNotEmpty()) {
                    CrisisSupportCard(
                        resources = uiState.crisisResources,
                        onDismiss = { viewModel.dismissCrisisSupport() }
                    )
                }

                Text(
                    text = stringResource(R.string.chat_disclaimer),
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 2.dp)
                )

                ChatInputBar(
                    value = uiState.input,
                    onValueChange = { viewModel.onInputChanged(it) },
                    onSend = { viewModel.send() },
                    isTyping = uiState.isTyping,
                    isEditing = uiState.editingMessageRemoteId != null,
                    onCancelEdit = { viewModel.cancelEditing() }
                )
            }
        }
    }
}
