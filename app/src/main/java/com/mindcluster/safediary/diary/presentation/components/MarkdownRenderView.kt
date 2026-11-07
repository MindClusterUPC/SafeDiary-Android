package com.mindcluster.safediary.diary.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindcluster.safediary.shared.presentation.theme.PrimaryNavy
import com.mindcluster.safediary.shared.presentation.theme.SecondaryTeal
import com.mindcluster.safediary.shared.presentation.theme.SlateSubtle
import com.mindcluster.safediary.shared.presentation.theme.SurfaceContainerLow

@Composable
fun MarkdownRenderView(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (markdown.isBlank()) {
            Text(
                text = "Escribe algo en el editor para ver la vista previa...",
                style = MaterialTheme.typography.bodyMedium,
                color = SlateSubtle,
                fontStyle = FontStyle.Italic
            )
            return@Column
        }

        val lines = markdown.lines()
        for (line in lines) {
            when {
                // Header 1
                line.startsWith("# ") -> {
                    Text(
                        text = line.removePrefix("# ").trim(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                // Header 2
                line.startsWith("## ") -> {
                    Text(
                        text = line.removePrefix("## ").trim(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryNavy,
                        modifier = Modifier.padding(top = 6.dp, bottom = 3.dp)
                    )
                }
                // Header 3
                line.startsWith("### ") -> {
                    Text(
                        text = line.removePrefix("### ").trim(),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = SecondaryTeal,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                // Blockquote
                line.startsWith("> ") -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .background(SecondaryTeal, RoundedCornerShape(2.dp))
                        )
                        Text(
                            text = parseInlineMarkdown(line.removePrefix("> ").trim()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateSubtle,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
                // Bullet item
                line.startsWith("- ") || line.startsWith("* ") -> {
                    Row(
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("•", color = SecondaryTeal, fontWeight = FontWeight.Bold)
                        Text(
                            text = parseInlineMarkdown(line.substring(2).trim()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryNavy
                        )
                    }
                }
                // Code block line
                line.startsWith("`") && line.endsWith("`") && line.length > 2 -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceContainerLow, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = line.trim('`'),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = PrimaryNavy
                        )
                    }
                }
                // Standard paragraph line
                line.isNotBlank() -> {
                    Text(
                        text = parseInlineMarkdown(line),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryNavy,
                        lineHeight = 22.sp
                    )
                }
                else -> {
                    // Empty line space
                    Box(modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        }
    }
}

/**
 * Parses bold (**text**), italic (*text*), and inline code (`code`) into an AnnotatedString.
 */
private fun parseInlineMarkdown(text: String) = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        when {
            // Bold **text**
            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(text.substring(i + 2, end))
                    pop()
                    i = end + 2
                } else {
                    append(text[i])
                    i++
                }
            }
            // Inline code `text`
            text.startsWith("`", i) -> {
                val end = text.indexOf("`", i + 1)
                if (end != -1) {
                    pushStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0xFFECEFF1)))
                    append(" ${text.substring(i + 1, end)} ")
                    pop()
                    i = end + 1
                } else {
                    append(text[i])
                    i++
                }
            }
            // Italic *text*
            text.startsWith("*", i) -> {
                val end = text.indexOf("*", i + 1)
                if (end != -1) {
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(text.substring(i + 1, end))
                    pop()
                    i = end + 1
                } else {
                    append(text[i])
                    i++
                }
            }
            else -> {
                append(text[i])
                i++
            }
        }
    }
}
