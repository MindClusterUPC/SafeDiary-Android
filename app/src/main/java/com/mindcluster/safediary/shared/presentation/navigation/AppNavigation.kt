package com.mindcluster.safediary.shared.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mindcluster.safediary.assistantai.infrastructure.AssistantAiModule
import com.mindcluster.safediary.assistantai.presentation.viewmodels.AssistantViewModel
import com.mindcluster.safediary.assistantai.presentation.views.AiChatScreen
import com.mindcluster.safediary.home.presentation.views.HomeScreen
import com.mindcluster.safediary.professionals.presentation.views.ProfessionalsScreen
import com.mindcluster.safediary.profiles.presentation.views.SettingsScreen
import com.mindcluster.safediary.rutines.presentation.views.RutinesScreen
import com.mindcluster.safediary.scheduling.presentation.views.SchedulingScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val assistantModule = AssistantAiModule.getInstance(context)

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.AI_CHAT
    ) {
        composable(NavRoutes.AI_CHAT) {
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
                    assistantModule.deleteConversationHandler
                )
            )
            AiChatScreen(
                viewModel = assistantViewModel,
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.SETTINGS)
                },
                onNavigateToRoute = { route ->
                    navigateToTab(route)
                }
            )
        }

        composable(NavRoutes.RUTINES) {
            RutinesScreen(
                onNavigateToRoute = { route ->
                    navigateToTab(route)
                }
            )
        }

        composable(NavRoutes.HOME) {
            HomeScreen(
                onNavigateToRoute = { route ->
                    navigateToTab(route)
                }
            )
        }

        composable(NavRoutes.PROFESSIONALS) {
            ProfessionalsScreen(
                onNavigateToRoute = { route ->
                    navigateToTab(route)
                }
            )
        }

        composable(NavRoutes.SCHEDULING) {
            SchedulingScreen(
                onNavigateToRoute = { route ->
                    navigateToTab(route)
                }
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
