package com.mindcluster.safediary.diary.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindcluster.safediary.R
import com.mindcluster.safediary.shared.presentation.theme.OutlineVariantBorder
import com.mindcluster.safediary.shared.presentation.theme.PrimaryNavy
import com.mindcluster.safediary.shared.presentation.theme.SecondaryContainer
import com.mindcluster.safediary.shared.presentation.theme.SecondaryTeal
import com.mindcluster.safediary.shared.presentation.theme.SurfaceContainerLow

/**
 * Formatting bar of the diary editor. It speaks the language of a journal (title, highlight a thought, list) instead
 * of Markdown, and marks the line format the cursor is on.
 */
@Composable
fun DiaryFormattingToolbar(
    activeLineStyle: DiaryLineStyle?,
    onLineStyle: (DiaryLineStyle) -> Unit,
    onInlineStyle: (DiaryInlineStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceContainerLow)
            // Keeps the bar above the gesture or three-button navigation bar; the keyboard inset is handled by the screen.
            .navigationBarsPadding()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FormatChip(
            label = stringResource(R.string.diary_format_title),
            icon = Icons.Filled.Title,
            selected = activeLineStyle == DiaryLineStyle.TITLE,
            onClick = { onLineStyle(DiaryLineStyle.TITLE) }
        )
        FormatChip(
            label = stringResource(R.string.diary_format_bold),
            icon = Icons.Filled.FormatBold,
            selected = false,
            onClick = { onInlineStyle(DiaryInlineStyle.BOLD) }
        )
        FormatChip(
            label = stringResource(R.string.diary_format_italic),
            icon = Icons.Filled.FormatItalic,
            selected = false,
            onClick = { onInlineStyle(DiaryInlineStyle.ITALIC) }
        )
        FormatChip(
            label = stringResource(R.string.diary_format_highlight),
            icon = Icons.Filled.FormatQuote,
            selected = activeLineStyle == DiaryLineStyle.HIGHLIGHT,
            onClick = { onLineStyle(DiaryLineStyle.HIGHLIGHT) }
        )
        FormatChip(
            label = stringResource(R.string.diary_format_list),
            icon = Icons.AutoMirrored.Filled.FormatListBulleted,
            selected = activeLineStyle == DiaryLineStyle.LIST,
            onClick = { onLineStyle(DiaryLineStyle.LIST) }
        )
    }
}

@Composable
private fun FormatChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) SecondaryContainer else Color.White,
        border = BorderStroke(1.dp, if (selected) SecondaryTeal else OutlineVariantBorder),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) SecondaryTeal else PrimaryNavy,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = PrimaryNavy
            )
        }
    }
}
