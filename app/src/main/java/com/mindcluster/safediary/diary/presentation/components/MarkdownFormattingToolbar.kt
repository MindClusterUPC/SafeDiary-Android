package com.mindcluster.safediary.diary.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindcluster.safediary.shared.presentation.theme.PrimaryNavy
import com.mindcluster.safediary.shared.presentation.theme.SurfaceContainerLow

@Composable
fun MarkdownFormattingToolbar(
    onInsertSymbol: (prefix: String, suffix: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceContainerLow)
            .horizontalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ToolbarButton(label = "H1", onClick = { onInsertSymbol("# ", "") })
        ToolbarButton(label = "H2", onClick = { onInsertSymbol("## ", "") })
        ToolbarButton(label = "B", fontWeight = FontWeight.Bold, onClick = { onInsertSymbol("**", "**") })
        ToolbarButton(label = "I", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, onClick = { onInsertSymbol("*", "*") })
        ToolbarButton(label = "Quote", onClick = { onInsertSymbol("> ", "") })
        ToolbarButton(label = "List", onClick = { onInsertSymbol("- ", "") })
        ToolbarButton(label = "Code", fontFamily = FontFamily.Monospace, onClick = { onInsertSymbol("`", "`") })
    }
}

@Composable
private fun ToolbarButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: androidx.compose.ui.text.font.FontStyle? = null,
    fontFamily: FontFamily? = null
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            fontFamily = fontFamily,
            color = PrimaryNavy,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
