package com.mindcluster.safediary.assistantai.presentation.views

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CloudOff
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
    onOpenDrawer: (() -> Unit)? = null,
    onNavigateToRoute: (String) -> Unit = {},
    showBottomBar: Boolean = false,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val internalDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val useInternalDrawer = onOpenDrawer == null
    val drawerState = internalDrawerState

    if (useInternalDrawer) {
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

        BackHandler(enabled = isDrawerOpen) {
            coroutineScope.launch { drawerState.close() }
        }
    }

    // Scrolling the message list hides the keyboard
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    LaunchedEffect(uiState.messages.size, uiState.isTyping, uiState.replyFailed) {
        if (uiState.messages.isNotEmpty()) {
            val extraItems = (if (uiState.isTyping) 1 else 0) + (if (uiState.replyFailed) 1 else 0)
            val totalCount = uiState.messages.size + extraItems
            listState.animateScrollToItem(totalCount - 1)
        }
    }

    val content: @Composable () -> Unit = {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = BackgroundSanctuary,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .displayCutoutPadding()
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            })
                        },
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
                        // Left side: Hamburger + Logo + Breadcrumb
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        if (onOpenDrawer != null) {
                                            onOpenDrawer()
                                        } else {
                                            coroutineScope.launch { drawerState.open() }
                                        }
                                    },
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

                        // Right side: Offline indicator + Profile avatar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!uiState.isOnline) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFEF3C7))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.chat_offline_indicator),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceContainer)
                                    .clickable {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        onNavigateToSettings()
                                    },
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
                }
            },
            bottomBar = {
                if (showBottomBar) {
                    val density = LocalDensity.current
                    val isIme = WindowInsets.isImeVisible || WindowInsets.ime.getBottom(density) > 0
                    if (!isIme) {
                        SafeDiaryBottomNavBar(
                            currentRoute = NavRoutes.AI_CHAT,
                            onNavigateToRoute = onNavigateToRoute
                        )
                    }
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
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            })
                        }
                ) {
                    if (uiState.messages.isEmpty()) {
                        ChatEmptyState(
                            onSuggestionSelected = { suggestion ->
                                focusManager.clearFocus()
                                keyboardController?.hide()
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

                if (uiState.showOfflinePendingNotice || uiState.messages.any { it.isPending }) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CloudOff,
                                contentDescription = null,
                                tint = Color(0xFF92400E),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.chat_offline_pending_notice),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF92400E),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
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
                    onSend = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        viewModel.send()
                    },
                    isTyping = uiState.isTyping,
                    isEditing = uiState.editingMessageRemoteId != null,
                    onCancelEdit = { viewModel.cancelEditing() }
                )

                // 12dp spacing between input bar and bottom navigation bar
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (useInternalDrawer) {
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
            content()
        }
    } else {
        content()
    }
}
