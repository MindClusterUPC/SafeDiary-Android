package com.mindcluster.safediary.shared.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mindcluster.safediary.iam.presentation.views.SignInScreen
import com.mindcluster.safediary.payments.presentation.views.PatientPaymentsScreen
import com.mindcluster.safediary.profiles.presentation.views.EditEmailScreen
import com.mindcluster.safediary.profiles.presentation.views.PatientProfileScreen
import com.mindcluster.safediary.profiles.presentation.views.SafeSupportContactScreen
import com.mindcluster.safediary.profiles.presentation.views.SettingsScreen
import com.mindcluster.safediary.shared.infrastructure.security.AuthSessionPreferences

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val authSessionPreferences = remember { AuthSessionPreferences(context) }
    val startDestination = if (authSessionPreferences.isLoggedIn) NavRoutes.AI_CHAT else NavRoutes.SIGN_IN

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 0. IAM Sign In
        composable(NavRoutes.SIGN_IN) {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate(NavRoutes.AI_CHAT) {
                        popUpTo(NavRoutes.SIGN_IN) { inclusive = true }
                    }
                }
            )
        }

        // Main Tab Pager (Diarito AI Chat, Home, Psychologist)
        composable(NavRoutes.AI_CHAT) {
            MainTabPagerScreen(
                onNavigateToProfile = {
                    navController.navigate(NavRoutes.PROFILE)
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.SETTINGS)
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
    }
}
