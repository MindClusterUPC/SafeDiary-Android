package com.mindcluster.safediary.assistantai.presentation.views

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindcluster.safediary.R
import com.mindcluster.safediary.assistantai.domain.model.MessageAuthor
import com.mindcluster.safediary.assistantai.presentation.components.ChatEmptyState
import com.mindcluster.safediary.assistantai.presentation.components.ChatInputBar
import com.mindcluster.safediary.assistantai.presentation.components.ConversationHistoryDrawer
import com.mindcluster.safediary.assistantai.presentation.components.CrisisSupportCard
import com.mindcluster.safediary.assistantai.presentation.components.MessageBubble
import com.mindcluster.safediary.assistantai.presentation.components.ReplyErrorNotice
import com.mindcluster.safediary.assistantai.presentation.components.TypingIndicator
import com.mindcluster.safediary.assistantai.presentation.viewmodels.AssistantViewModel
import com.mindcluster.safediary.shared.presentation.components.SafeDiaryBottomNavBar
import com.mindcluster.safediary.shared.presentation.navigation.NavRoutes
import com.mindcluster.safediary.shared.presentation.theme.BackgroundSanctuary
import com.mindcluster.safediary.shared.presentation.theme.PrimaryNavy
import com.mindcluster.safediary.shared.presentation.theme.SlateBreadcrumbDark
import com.mindcluster.safediary.shared.presentation.theme.SlateMenuIcon
import com.mindcluster.safediary.shared.presentation.theme.SlatePlaceholder
import com.mindcluster.safediary.shared.presentation.theme.SlateSubtle
import com.mindcluster.safediary.shared.presentation.theme.SurfaceContainer
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
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

    // System back closes the history drawer instead of leaving the app.
    BackHandler(enabled = isDrawerOpen) {
        coroutineScope.launch { drawerState.close() }
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
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .displayCutoutPadding(),
                    color = Color.White.copy(alpha = 0.90f),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left side: Hamburger (40dp circle) + Logo (32dp, 8dp corners) + Breadcrumb
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable { coroutineScope.launch { drawerState.open() } },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = stringResource(R.string.shared_cd_menu),
                                    tint = SlateMenuIcon,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Image(
                                painter = painterResource(id = R.drawable.safediary_logo),
                                contentDescription = stringResource(R.string.shared_cd_logo),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.shared_topbar_title_prefix),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryNavy
                                )
                                Text(
                                    text = " / ",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Light,
                                    color = SlatePlaceholder
                                )
                                Text(
                                    text = stringResource(R.string.chat_header_diarito),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SlateBreadcrumbDark
                                )
                            }
                        }

                        // Right side: Profile avatar (36dp circle with person icon on #E7EEFF circle)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainer)
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
            },
            bottomBar = {
                if (!WindowInsets.isImeVisible) {
                    SafeDiaryBottomNavBar(
                        currentRoute = NavRoutes.AI_CHAT,
                        onNavigateToRoute = onNavigateToRoute
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding()
                    )
                    .displayCutoutPadding()
                    .imePadding()
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
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = SlateSubtle,
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
