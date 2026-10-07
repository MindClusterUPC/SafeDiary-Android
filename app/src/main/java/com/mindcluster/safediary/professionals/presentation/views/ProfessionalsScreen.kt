package com.mindcluster.safediary.professionals.presentation.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mindcluster.safediary.R
import com.mindcluster.safediary.shared.presentation.components.ComingSoonScreen
import com.mindcluster.safediary.shared.presentation.navigation.NavRoutes

@Composable
fun ProfessionalsScreen(
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ComingSoonScreen(
        title = stringResource(R.string.professionals_title),
        icon = Icons.Outlined.Psychology,
        description = stringResource(R.string.professionals_subtitle),
        plannedFeatures = listOf(
            stringResource(R.string.professionals_feature_1),
            stringResource(R.string.professionals_feature_2),
            stringResource(R.string.professionals_feature_3),
            stringResource(R.string.professionals_feature_4)
        ),
        currentRoute = NavRoutes.PROFESSIONALS,
        onNavigateToRoute = onNavigateToRoute,
        modifier = modifier
    )
}
