package com.mindcluster.safediary.shared.presentation.navigation

import android.app.Activity
import androidx.compose.ui.geometry.Offset
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindcluster.safediary.assistantai.infrastructure.AssistantAiModule
import com.mindcluster.safediary.assistantai.presentation.components.ConversationHistoryDrawer
import com.mindcluster.safediary.assistantai.presentation.viewmodels.AssistantViewModel
import com.mindcluster.safediary.assistantai.presentation.views.AiChatScreen
import com.mindcluster.safediary.home.presentation.views.HomeScreen
import com.mindcluster.safediary.professionals.presentation.views.ProfessionalsScreen
import com.mindcluster.safediary.rutines.presentation.views.RutinesScreen
import com.mindcluster.safediary.scheduling.presentation.views.SchedulingScreen
import com.mindcluster.safediary.shared.presentation.components.SafeDiaryBottomNavBar
import com.mindcluster.safediary.shared.presentation.theme.BackgroundSanctuary
import kotlinx.coroutines.launch

import androidx.compose.foundation.layout.ExperimentalLayoutApi

private val tabRoutes = listOf(
    NavRoutes.AI_CHAT,
    NavRoutes.RUTINES,
    NavRoutes.HOME,
    NavRoutes.PROFESSIONALS,
    NavRoutes.SCHEDULING
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainTabPagerScreen(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val assistantModule = remember { AssistantAiModule.getInstance(context) }
    val assistantViewModel: AssistantViewModel = viewModel(
        factory = AssistantViewModel.Factory(
            assistantModule.sendPromptHandler,
            assistantModule.retryLastPromptHandler,
            assistantModule.getChatHistoryHandler,
            assistantModule.startNewChatHandler,
            assistantModule.getCrisisSupportHandler,
            assistantModule.dismissCrisisSupportHandler,
            assistantModule.getConversationListHandler,
            assistantModule.openConversationHandler,
            assistantModule.editMessageHandler,
            assistantModule.regenerateReplyHandler,
            assistantModule.renameConversationHandler,
            assistantModule.deleteConversationHandler,
            assistantModule.networkMonitor,
            assistantModule.chatPreferences
        )
    )

    val uiState by assistantViewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 5 })
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val isImeVisible = WindowInsets.isImeVisible || imeBottom > 0

    val view = LocalView.current
    val isDrawerOpen = drawerState.isOpen || drawerState.targetValue == DrawerValue.Open

    // Status bar icon colors: light over dark drawer, dark over sanctuary background
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
        if (drawerState.isOpen) assistantViewModel.refreshHistory()
    }

    BackHandler(enabled = isDrawerOpen) {
        coroutineScope.launch { drawerState.close() }
    }

    fun navigateToTab(route: String) {
        val targetIndex = tabRoutes.indexOf(route)
        if (targetIndex >= 0) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(targetIndex)
            }
        }
    }

    // On the first page (Diarito) a swipe to the right opens the history drawer. The gesture is
    // read before the pager sees it, and only claimed once it is clearly a rightward drag, so
    // taps, vertical scrolling and swipes to the next tab keep working.
    val openDrawerThresholdPx = with(density) { 48.dp.toPx() }
    val openDrawerOnSwipe = Modifier.pointerInput(pagerState, drawerState, isImeVisible) {
        if (isImeVisible) return@pointerInput
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            if (pagerState.currentPage != 0 || drawerState.isOpen) return@awaitEachGesture
            var totalX = 0f
            var totalY = 0f
            var claimed = false
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                if (!change.pressed) break
                val delta = change.positionChange()
                totalX += delta.x
                totalY += delta.y
                if (!claimed) {
                    if (totalX < -viewConfiguration.touchSlop || abs(totalY) > viewConfiguration.touchSlop) break
                    if (totalX > viewConfiguration.touchSlop && totalX > abs(totalY)) claimed = true
                }
                if (claimed) {
                    change.consume()
                    if (totalX > openDrawerThresholdPx) {
                        coroutineScope.launch { drawerState.open() }
                        break
                    }
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isDrawerOpen || (pagerState.currentPage == 0 && !isImeVisible),
        drawerContent = {
            ConversationHistoryDrawer(
                conversations = uiState.conversations,
                activeConversationId = uiState.activeConversationId,
                isLoading = uiState.isHistoryLoading,
                loadFailed = uiState.historyFailed,
                onRetry = { assistantViewModel.refreshHistory() },
                onNewChat = {
                    assistantViewModel.newChat()
                    coroutineScope.launch { drawerState.close() }
                },
                onConversationClick = { summary ->
                    assistantViewModel.openConversation(summary.remoteId)
                    coroutineScope.launch { drawerState.close() }
                },
                onRenameConversation = { remoteId, newTitle ->
                    assistantViewModel.renameConversation(remoteId, newTitle)
                },
                onDeleteConversation = { remoteId ->
                    assistantViewModel.deleteConversation(remoteId)
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
            bottomBar = {
                // Bottom nav animated in and out based on real IME state
                AnimatedVisibility(
                    visible = !isImeVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(250)
                    ) + fadeIn(animationSpec = tween(250)),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(200)
                    ) + fadeOut(animationSpec = tween(200))
                ) {
                    SafeDiaryBottomNavBar(
                        currentRoute = tabRoutes[pagerState.currentPage],
                        onNavigateToRoute = { route -> navigateToTab(route) }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .then(openDrawerOnSwipe)
            ) {
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = !isImeVisible,
                    beyondViewportPageCount = 4,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> AiChatScreen(
                            viewModel = assistantViewModel,
                            onNavigateToSettings = onNavigateToSettings,
                            onOpenDrawer = {
                                coroutineScope.launch { drawerState.open() }
                            },
                            onNavigateToRoute = { navigateToTab(it) },
                            showBottomBar = false
                        )
                        1 -> RutinesScreen(
                            onNavigateToRoute = { navigateToTab(it) }
                        )
                        2 -> HomeScreen(
                            onNavigateToRoute = { navigateToTab(it) }
                        )
                        3 -> ProfessionalsScreen(
                            onNavigateToRoute = { navigateToTab(it) }
                        )
                        4 -> SchedulingScreen(
                            onNavigateToRoute = { navigateToTab(it) }
                        )
                    }
                }
            }
        }
    }
}
