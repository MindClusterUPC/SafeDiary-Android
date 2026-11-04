package com.mindcluster.safediary.home.presentation.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mindcluster.safediary.R
import com.mindcluster.safediary.shared.presentation.components.ComingSoonScreen
import com.mindcluster.safediary.shared.presentation.navigation.NavRoutes

@Composable
fun HomeScreen(
    onNavigateToRoute: (String) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ComingSoonScreen(
        title = stringResource(R.string.home_title),
        icon = Icons.Outlined.Home,
        description = stringResource(R.string.home_subtitle),
        plannedFeatures = listOf(
            stringResource(R.string.home_feature_1),
            stringResource(R.string.home_feature_2),
            stringResource(R.string.home_feature_3),
            stringResource(R.string.home_feature_4),
            stringResource(R.string.home_feature_5)
        ),
        currentRoute = NavRoutes.HOME,
        onNavigateToRoute = onNavigateToRoute,
        onNavigateToProfile = onNavigateToProfile,
        modifier = modifier
    )
}
