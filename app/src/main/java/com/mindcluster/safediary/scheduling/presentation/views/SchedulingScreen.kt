package com.mindcluster.safediary.scheduling.presentation.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mindcluster.safediary.R
import com.mindcluster.safediary.shared.presentation.components.ComingSoonScreen
import com.mindcluster.safediary.shared.presentation.navigation.NavRoutes

@Composable
fun SchedulingScreen(
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ComingSoonScreen(
        title = stringResource(R.string.scheduling_title),
        icon = Icons.Outlined.CalendarMonth,
        description = stringResource(R.string.scheduling_subtitle),
        plannedFeatures = listOf(
            stringResource(R.string.scheduling_feature_1),
            stringResource(R.string.scheduling_feature_2),
            stringResource(R.string.scheduling_feature_3),
            stringResource(R.string.scheduling_feature_4)
        ),
        currentRoute = NavRoutes.SCHEDULING,
        onNavigateToRoute = onNavigateToRoute,
        modifier = modifier
    )
}
