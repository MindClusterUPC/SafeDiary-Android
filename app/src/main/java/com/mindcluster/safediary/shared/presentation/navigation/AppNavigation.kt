package com.mindcluster.safediary.shared.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mindcluster.safediary.assistantai.infrastructure.AssistantAiModule
import com.mindcluster.safediary.assistantai.presentation.viewmodels.AssistantViewModel
import com.mindcluster.safediary.assistantai.presentation.views.AiChatScreen
import com.mindcluster.safediary.profiles.presentation.views.SettingsScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val assistantModule = AssistantAiModule.getInstance(context)

    NavHost(
        navController = navController,
        startDestination = NavRoutes.AI_CHAT
    ) {
        composable(NavRoutes.AI_CHAT) {
            val assistantViewModel: AssistantViewModel = viewModel(
                factory = AssistantViewModel.Factory(
                    assistantModule.sendPromptHandler,
                    assistantModule.getChatHistoryHandler,
                    assistantModule.startNewChatHandler
                )
            )
            AiChatScreen(
                viewModel = assistantViewModel,
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.SETTINGS)
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
