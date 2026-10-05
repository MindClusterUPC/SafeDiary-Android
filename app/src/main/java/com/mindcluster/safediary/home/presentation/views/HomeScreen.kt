package com.mindcluster.safediary.home.presentation.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mindcluster.safediary.R
import com.mindcluster.safediary.shared.presentation.components.ComingSoonScreen

@Composable
fun HomeScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    ComingSoonScreen(
        title = stringResource(R.string.nav_home),
        icon = Icons.Default.Home,
        description = stringResource(R.string.home_subtitle),
        plannedFeatures = listOf(
            stringResource(R.string.home_feature_1),
            stringResource(R.string.home_feature_2),
            stringResource(R.string.home_feature_3),
            stringResource(R.string.home_feature_4),
            stringResource(R.string.home_feature_5)
        ),
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}
