package com.mindcluster.safediary.shared.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindcluster.safediary.R
import com.mindcluster.safediary.shared.presentation.navigation.NavRoutes
import com.mindcluster.safediary.shared.presentation.theme.SecondaryTeal
import com.mindcluster.safediary.shared.presentation.theme.SlateBorder
import com.mindcluster.safediary.shared.presentation.theme.SlateSubtle

data class BottomNavItem(
    val route: String,
    val labelResId: Int,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = NavRoutes.AI_CHAT,
        labelResId = R.string.nav_tab_diarito,
        icon = Icons.Default.AutoAwesome
    ),
    BottomNavItem(
        route = NavRoutes.RUTINES,
        labelResId = R.string.nav_tab_rutines,
        icon = Icons.Outlined.Checklist
    ),
    BottomNavItem(
        route = NavRoutes.HOME,
        labelResId = R.string.nav_tab_home,
        icon = Icons.Outlined.Home
    ),
    BottomNavItem(
        route = NavRoutes.PROFESSIONALS,
        labelResId = R.string.nav_tab_psychologist,
        icon = Icons.Outlined.Psychology
    ),
    BottomNavItem(
        route = NavRoutes.SCHEDULING,
        labelResId = R.string.nav_tab_scheduling,
        icon = Icons.Outlined.CalendarMonth
    )
)

/**
 * Bottom navigation bar matching the Figma Serene Sanctuary specifications.
 * Height 64, white at 95% alpha, top shadow.
 * 5 equal items: Diarito, Rutines, Home, Psychologist, Scheduling.
 * Active: #006B5F with 6dp dot below label; Inactive: #64748B.
 */
@Composable
fun SafeDiaryBottomNavBar(
    currentRoute: String,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            HorizontalDivider(color = SlateBorder.copy(alpha = 0.6f), thickness = 0.5.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    val itemColor = if (isSelected) SecondaryTeal else SlateSubtle

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                if (!isSelected) {
                                    onNavigateToRoute(item.route)
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = stringResource(item.labelResId),
                            tint = itemColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(item.labelResId),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = itemColor,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(SecondaryTeal)
                            )
                        } else {
                            Spacer(modifier = Modifier.size(6.dp))
                        }
                    }
                }
            }
        }
    }
}
