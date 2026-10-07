package com.mindcluster.safediary.rutines.presentation.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mindcluster.safediary.R
import com.mindcluster.safediary.shared.presentation.components.ComingSoonScreen
import com.mindcluster.safediary.shared.presentation.navigation.NavRoutes

@Composable
fun RutinesScreen(
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ComingSoonScreen(
        title = stringResource(R.string.rutines_title),
        icon = Icons.Outlined.Checklist,
        description = stringResource(R.string.rutines_subtitle),
        plannedFeatures = listOf(
            stringResource(R.string.rutines_feature_1),
            stringResource(R.string.rutines_feature_2),
            stringResource(R.string.rutines_feature_3),
            stringResource(R.string.rutines_feature_4)
        ),
        currentRoute = NavRoutes.RUTINES,
        onNavigateToRoute = onNavigateToRoute,
        modifier = modifier
    )
}
