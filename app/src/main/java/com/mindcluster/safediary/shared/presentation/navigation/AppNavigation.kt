package com.mindcluster.safediary.shared.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mindcluster.safediary.diary.infrastructure.DiaryModule
import com.mindcluster.safediary.diary.presentation.viewmodels.DiaryViewModel
import com.mindcluster.safediary.diary.presentation.views.DiaryMarkdownEditorScreen
import com.mindcluster.safediary.iam.presentation.views.SignInScreen
import com.mindcluster.safediary.payments.presentation.views.PatientPaymentsScreen
import com.mindcluster.safediary.profiles.presentation.views.EditEmailScreen
import com.mindcluster.safediary.profiles.presentation.views.PatientProfileScreen
import com.mindcluster.safediary.profiles.presentation.views.SafeSupportContactScreen
import com.mindcluster.safediary.profiles.presentation.views.SettingsScreen
import com.mindcluster.safediary.shared.infrastructure.security.AuthSessionPreferences
import com.mindcluster.safediary.shared.infrastructure.security.AuthSessionHolder

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val authSessionPreferences = remember { AuthSessionPreferences(context) }
    val startDestination = remember {
        if (authSessionPreferences.isLoggedIn) NavRoutes.AI_CHAT else NavRoutes.SIGN_IN
    }
    val requiresSignIn by AuthSessionHolder.requiresSignIn.collectAsStateWithLifecycle()
    var activeAccountId by remember { mutableStateOf(authSessionPreferences.patientAccountId) }
    var sessionGeneration by remember { mutableStateOf(0) }
    var showSessionNotice by rememberSaveable {
        mutableStateOf(authSessionPreferences.sessionExpired)
    }

    LaunchedEffect(requiresSignIn) {
        if (requiresSignIn) {
            authSessionPreferences.clear()
            activeAccountId = 0L
            sessionGeneration++
            authSessionPreferences.sessionExpired = true
            showSessionNotice = true
            navController.navigate(NavRoutes.SIGN_IN) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
            AuthSessionHolder.acknowledgeSignInRequired()
        }
    }

    val diaryModule = remember(activeAccountId, sessionGeneration) { DiaryModule.getInstance(context) }
    val diaryViewModel: DiaryViewModel = viewModel(
        key = "diary-account-$activeAccountId-session-$sessionGeneration",
        factory = DiaryViewModel.Factory(diaryModule.diaryRepository, authSessionPreferences)
    )

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 0. IAM Sign In
        composable(NavRoutes.SIGN_IN) {
            SignInScreen(
                onSignInSuccess = {
                    activeAccountId = authSessionPreferences.patientAccountId
                    sessionGeneration++
                    authSessionPreferences.sessionExpired = false
                    showSessionNotice = false
                    navController.navigate(NavRoutes.AI_CHAT) {
                        popUpTo(NavRoutes.SIGN_IN) { inclusive = true }
                    }
                },
                sessionExpired = showSessionNotice
            )
        }

        // Main Tab Pager (Diarito AI Chat, Diary Notes, Home, Psychologist, Scheduling)
        composable(NavRoutes.AI_CHAT) {
            MainTabPagerScreen(
                diaryViewModel = diaryViewModel,
                onNavigateToProfile = {
                    navController.navigate(NavRoutes.PROFILE)
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.SETTINGS)
                },
                onOpenDiaryEditor = { localId ->
                    if (localId != null) {
                        diaryViewModel.openExistingEntry(localId)
                    } else {
                        diaryViewModel.openNewDraft()
                    }
                    navController.navigate(NavRoutes.DIARY_EDITOR)
                }
            )
        }

        // 1. My Profile & Privacy
        composable(
            route = NavRoutes.PROFILE,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            }
        ) {
            PatientProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(NavRoutes.SETTINGS) },
                onNavigateToEditEmail = { navController.navigate(NavRoutes.EDIT_EMAIL) },
                onNavigateToSafeContact = { navController.navigate(NavRoutes.SAFE_CONTACT) },
                onNavigateToPayments = { navController.navigate(NavRoutes.PATIENT_PAYMENTS) },
                onLogout = {
                    activeAccountId = 0L
                    sessionGeneration++
                    navController.navigate(NavRoutes.SIGN_IN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 2. Edit Email Address
        composable(
            route = NavRoutes.EDIT_EMAIL,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            }
        ) {
            EditEmailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 3. Safe Support Contact
        composable(
            route = NavRoutes.SAFE_CONTACT,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            }
        ) {
            SafeSupportContactScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 4. Payments & Digital Wallet
        composable(
            route = NavRoutes.PATIENT_PAYMENTS,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            }
        ) {
            PatientPaymentsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 5. Settings Screen
        composable(
            route = NavRoutes.SETTINGS,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            }
        ) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 6. Diary Markdown Editor
        composable(
            route = NavRoutes.DIARY_EDITOR,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(300)
                )
            }
        ) {
            DiaryMarkdownEditorScreen(
                viewModel = diaryViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
