package com.mindcluster.safediary.shared.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mindcluster.safediary.R
import com.mindcluster.safediary.shared.presentation.components.PrimaryButton
import com.mindcluster.safediary.shared.presentation.components.SafeDiaryCard
import com.mindcluster.safediary.shared.presentation.components.SafeDiaryTopBar
import com.mindcluster.safediary.shared.presentation.theme.BackgroundSanctuary
import com.mindcluster.safediary.shared.presentation.theme.PrimaryNavy

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.WELCOME
    ) {
        composable(NavRoutes.WELCOME) {
            WelcomePlaceholderScreen()
        }
    }
}

@Composable
fun WelcomePlaceholderScreen() {
    Scaffold(
        topBar = {
            SafeDiaryTopBar(
                sectionTitle = stringResource(R.string.shared_topbar_section_home)
            )
        },
        containerColor = BackgroundSanctuary
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SafeDiaryCard(
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = stringResource(R.string.shared_welcome_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = PrimaryNavy,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.shared_welcome_desc),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            PrimaryButton(
                text = stringResource(R.string.shared_button_continue),
                onClick = {}
            )
        }
    }
}
