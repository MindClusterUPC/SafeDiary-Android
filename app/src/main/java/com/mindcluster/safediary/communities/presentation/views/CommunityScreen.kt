package com.mindcluster.safediary.communities.presentation.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mindcluster.safediary.R
import com.mindcluster.safediary.shared.presentation.components.ComingSoonScreen

@Composable
fun CommunityScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    ComingSoonScreen(
        title = stringResource(R.string.nav_community),
        icon = Icons.Default.Groups,
        description = stringResource(R.string.community_subtitle),
        plannedFeatures = listOf(
            stringResource(R.string.community_feature_1),
            stringResource(R.string.community_feature_2),
            stringResource(R.string.community_feature_3),
            stringResource(R.string.community_feature_4)
        ),
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}
