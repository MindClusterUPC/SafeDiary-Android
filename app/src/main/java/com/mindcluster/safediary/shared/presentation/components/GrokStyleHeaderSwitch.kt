package com.mindcluster.safediary.shared.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.mindcluster.safediary.shared.presentation.theme.PrimaryNavy
import com.mindcluster.safediary.shared.presentation.theme.SecondaryTeal
import com.mindcluster.safediary.shared.presentation.theme.SlateSubtle

enum class MainViewMode {
    DIARITO,
    DIARIO
}

@Composable
fun GrokStyleHeaderSwitch(
    currentMode: MainViewMode,
    onModeSelected: (MainViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0xFFEDEFEF))
            .padding(3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            SwitchTab(
                text = stringResource(R.string.switch_diarito_mode),
                icon = Icons.Outlined.ChatBubbleOutline,
                isSelected = currentMode == MainViewMode.DIARITO,
                onClick = { onModeSelected(MainViewMode.DIARITO) }
            )
            SwitchTab(
                text = stringResource(R.string.switch_diary_mode),
                icon = Icons.AutoMirrored.Outlined.MenuBook,
                isSelected = currentMode == MainViewMode.DIARIO,
                onClick = { onModeSelected(MainViewMode.DIARIO) }
            )
        }
    }
}

@Composable
private fun SwitchTab(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.Transparent,
        animationSpec = tween(200),
        label = "switchBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryNavy else SlateSubtle,
        animationSpec = tween(200),
        label = "switchText"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) SecondaryTeal else SlateSubtle,
            modifier = Modifier
                .size(16.dp)
                .padding(end = 4.dp)
        )
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor
        )
    }
}
