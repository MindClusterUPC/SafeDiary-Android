package com.mindcluster.safediary.diary.presentation.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp

/** Line formats a diary note supports; each one is stored as a Markdown line prefix. */
enum class DiaryLineStyle(val prefix: String) {
    TITLE("# "),
    HIGHLIGHT("> "),
    LIST("- ")
}

/** Inline formats a diary note supports; each one is stored as a pair of Markdown markers. */
enum class DiaryInlineStyle(val marker: String) {
    BOLD("**"),
    ITALIC("*")
}

/**
 * Formatting of diary notes. Notes are stored as a small subset of Markdown so the backend and older notes keep
 * working, but the writer never sees the symbols: the editor hides them and shows the formatted text instead.
 */
object DiaryMarkup {

    private val headingPrefix = Regex("^#{1,3} ")
    private val inlinePattern = Regex("""\*\*(.+?)\*\*|\*(.+?)\*""")
    private val strayMarkers = Regex("""[*`]+""")

    /** Plain text of a note, without any formatting symbol, for previews such as the gallery cards. */
    fun plainText(markdown: String): String = markdown.lines().joinToString("\n") { line ->
        val body = stripLinePrefix(line).second
        inlinePattern.replace(body) { it.groupValues[1].ifEmpty { it.groupValues[2] } }
            .replace(strayMarkers, "")
    }.lines().filter { it.isNotBlank() }.joinToString("\n").trim()

    /** Applies or removes a line format on every line touched by the selection, keeping the selection in place. */
    fun toggleLineStyle(value: TextFieldValue, style: DiaryLineStyle): TextFieldValue {
        val text = value.text
        val min = value.selection.min
        val max = value.selection.max
        val start = text.lastIndexOf('\n', min - 1).let { if (it < 0) 0 else it + 1 }
        val end = text.indexOf('\n', max).let { if (it < 0) text.length else it }
        val lines = text.substring(start, end).split('\n')
        val removing = lines.all { lineStyleOf(it) == style }

        val rebuilt = lines.map { line ->
            val body = stripLinePrefix(line).second
            if (removing) body else style.prefix + body
        }
        val newBlock = rebuilt.joinToString("\n")
        val firstLineShift = rebuilt.first().length - lines.first().length
        val totalShift = newBlock.length - (end - start)
        val newMin = (min + firstLineShift).coerceIn(start, start + newBlock.length)
        val newMax = if (min == max) newMin else (max + totalShift).coerceIn(newMin, start + newBlock.length)
        return value.copy(
            text = text.substring(0, start) + newBlock + text.substring(end),
            selection = TextRange(newMin, newMax)
        )
    }

    /**
     * Wraps the selection in an inline format, or removes it when the selection is already wrapped. Without a
     * selection it inserts an empty pair and leaves the cursor between the markers, ready to type.
     */
    fun toggleInlineStyle(value: TextFieldValue, style: DiaryInlineStyle): TextFieldValue {
        val text = value.text
        val marker = style.marker
        val min = value.selection.min
        val max = value.selection.max

        val wrappedOutside = min >= marker.length && max + marker.length <= text.length &&
            text.substring(min - marker.length, min) == marker && text.substring(max, max + marker.length) == marker &&
            !(style == DiaryInlineStyle.ITALIC && isBoldMarkerAt(text, min - 1, max))
        if (wrappedOutside) {
            val newText = text.removeRange(max, max + marker.length).removeRange(min - marker.length, min)
            return value.copy(text = newText, selection = TextRange(min - marker.length, max - marker.length))
        }
        val newText = text.substring(0, min) + marker + text.substring(min, max) + marker + text.substring(max)
        return value.copy(text = newText, selection = TextRange(min + marker.length, max + marker.length))
    }

    /** The line format of the line where the cursor is, to highlight the matching toolbar button. */
    fun lineStyleAtCursor(value: TextFieldValue): DiaryLineStyle? {
        val text = value.text
        val cursor = value.selection.min.coerceIn(0, text.length)
        val start = text.lastIndexOf('\n', cursor - 1).let { if (it < 0) 0 else it + 1 }
        val end = text.indexOf('\n', cursor).let { if (it < 0) text.length else it }
        return lineStyleOf(text.substring(start, end))
    }

    /** Shows the note formatted while it is edited: symbols are hidden and their format is applied. */
    fun visualTransformation(highlightColor: Color, highlightBackground: Color): VisualTransformation =
        VisualTransformation { text -> transform(text.text, highlightColor, highlightBackground) }

    internal fun transform(source: String, highlightColor: Color, highlightBackground: Color): TransformedText {
        val originalToTransformed = IntArray(source.length + 1)
        val transformedToOriginal = ArrayList<Int>(source.length + 1)

        val annotated = buildAnnotatedString {
            var offset = 0
            val lines = source.split('\n')
            lines.forEachIndexed { lineIndex, line ->
                val style = lineStyleOf(line)
                val prefixLength = if (style == null) 0 else headingPrefix.find(line)?.value?.length ?: 2

                // Hidden line prefix: every hidden character maps to the start of the visible line.
                for (i in 0 until prefixLength) originalToTransformed[offset + i] = length
                if (style == DiaryLineStyle.LIST) {
                    // The list marker is replaced, not hidden, so the bullet stays aligned with the source.
                    append("• ")
                    originalToTransformed[offset] = length - 2
                    originalToTransformed[offset + 1] = length - 1
                    transformedToOriginal.add(offset)
                    transformedToOriginal.add(offset + 1)
                }

                val bodyStart = length
                val body = line.substring(prefixLength)
                appendInline(body, offset + prefixLength, originalToTransformed, transformedToOriginal)
                when (style) {
                    DiaryLineStyle.TITLE -> addStyle(SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold), bodyStart, length)
                    DiaryLineStyle.HIGHLIGHT -> addStyle(
                        SpanStyle(color = highlightColor, fontStyle = FontStyle.Italic, background = highlightBackground),
                        bodyStart,
                        length
                    )
                    else -> Unit
                }

                offset += line.length
                if (lineIndex < lines.lastIndex) {
                    originalToTransformed[offset] = length
                    transformedToOriginal.add(offset)
                    append('\n')
                    offset += 1
                }
            }
            originalToTransformed[source.length] = length
            transformedToOriginal.add(source.length)
        }

        val toOriginal = transformedToOriginal.toIntArray()
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                originalToTransformed[offset.coerceIn(0, source.length)]

            override fun transformedToOriginal(offset: Int): Int =
                toOriginal[offset.coerceIn(0, toOriginal.lastIndex)]
        }
        return TransformedText(annotated, mapping)
    }

    /** Appends a line body, hiding inline markers and recording where every source character lands. */
    private fun AnnotatedString.Builder.appendInline(
        body: String,
        bodyOffset: Int,
        originalToTransformed: IntArray,
        transformedToOriginal: MutableList<Int>
    ) {
        var cursor = 0
        fun appendVisible(from: Int, to: Int) {
            for (i in from until to) {
                originalToTransformed[bodyOffset + i] = length
                transformedToOriginal.add(bodyOffset + i)
                append(body[i])
            }
        }
        fun hide(from: Int, to: Int) {
            for (i in from until to) originalToTransformed[bodyOffset + i] = length
        }
        for (match in inlinePattern.findAll(body)) {
            appendVisible(cursor, match.range.first)
            val bold = match.groupValues[1].isNotEmpty()
            val markerLength = if (bold) 2 else 1
            hide(match.range.first, match.range.first + markerLength)
            val start = length
            appendVisible(match.range.first + markerLength, match.range.last + 1 - markerLength)
            addStyle(
                if (bold) SpanStyle(fontWeight = FontWeight.Bold) else SpanStyle(fontStyle = FontStyle.Italic),
                start,
                length
            )
            hide(match.range.last + 1 - markerLength, match.range.last + 1)
            cursor = match.range.last + 1
        }
        appendVisible(cursor, body.length)
    }

    private fun lineStyleOf(line: String): DiaryLineStyle? = when {
        headingPrefix.containsMatchIn(line) -> DiaryLineStyle.TITLE
        line.startsWith(DiaryLineStyle.HIGHLIGHT.prefix) -> DiaryLineStyle.HIGHLIGHT
        line.startsWith(DiaryLineStyle.LIST.prefix) -> DiaryLineStyle.LIST
        else -> null
    }

    private fun stripLinePrefix(line: String): Pair<DiaryLineStyle?, String> {
        val style = lineStyleOf(line) ?: return null to line
        val length = if (style == DiaryLineStyle.TITLE) headingPrefix.find(line)!!.value.length else style.prefix.length
        return style to line.substring(length)
    }

    /** True when the asterisk before the selection belongs to a bold marker, so italic must not unwrap it. */
    private fun isBoldMarkerAt(text: String, index: Int, max: Int): Boolean =
        index > 0 && text[index - 1] == '*' && max + 1 < text.length && text[max + 1] == '*'
}
