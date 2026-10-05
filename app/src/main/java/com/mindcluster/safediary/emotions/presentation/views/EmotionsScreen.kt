package com.mindcluster.safediary.emotions.presentation.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mindcluster.safediary.R
import com.mindcluster.safediary.shared.presentation.components.ComingSoonScreen

@Composable
fun EmotionsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    ComingSoonScreen(
        title = stringResource(R.string.nav_emotions),
        icon = Icons.Default.Insights,
        description = stringResource(R.string.emotions_subtitle),
        plannedFeatures = listOf(
            stringResource(R.string.emotions_feature_1),
            stringResource(R.string.emotions_feature_2),
            stringResource(R.string.emotions_feature_3),
            stringResource(R.string.emotions_feature_4),
            stringResource(R.string.emotions_feature_5)
        ),
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}
