package com.mindcluster.safediary.shared.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.mindcluster.safediary.R
import com.mindcluster.safediary.shared.presentation.theme.BackgroundSanctuary
import com.mindcluster.safediary.shared.presentation.theme.PrimaryNavy
import com.mindcluster.safediary.shared.presentation.theme.SecondaryTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeDiaryTopBar(
    sectionTitle: String,
    modifier: Modifier = Modifier,
    canNavigateBack: Boolean = false,
    onNavigateBack: () -> Unit = {},
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = BackgroundSanctuary,
            titleContentColor = PrimaryNavy,
            navigationIconContentColor = PrimaryNavy,
            actionIconContentColor = PrimaryNavy
        ),
        navigationIcon = {
            if (navigationIcon != null) {
                navigationIcon()
            } else if (canNavigateBack) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.shared_cd_back),
                        tint = PrimaryNavy
                    )
                }
            } else {
                // Brand logo badge
                Box(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 4.dp)
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrimaryNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = stringResource(R.string.shared_cd_logo),
                        tint = SecondaryTeal,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (canNavigateBack) {
                    Text(
                        text = sectionTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryNavy
                    )
                } else {
                    val prefix = stringResource(R.string.shared_topbar_title_prefix)
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryNavy
                                )
                            ) {
                                append(prefix)
                            }
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF73777F)
                                )
                            ) {
                                append(" / ")
                            }
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Medium,
                                    color = PrimaryNavy
                                )
                            ) {
                                append(sectionTitle)
                            }
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        actions = actions
    )
}
