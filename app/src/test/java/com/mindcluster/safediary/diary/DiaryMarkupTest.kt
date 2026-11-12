package com.mindcluster.safediary.diary

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.mindcluster.safediary.diary.presentation.components.DiaryInlineStyle
import com.mindcluster.safediary.diary.presentation.components.DiaryLineStyle
import com.mindcluster.safediary.diary.presentation.components.DiaryMarkup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DiaryMarkupTest {

    private fun value(text: String, cursor: Int = text.length) = TextFieldValue(text, TextRange(cursor))

    @Test
    fun titleIsAppliedToTheLineOfTheCursorNotAtTheEnd() {
        val result = DiaryMarkup.toggleLineStyle(value("Hoy\nfue un buen dia", cursor = 2), DiaryLineStyle.TITLE)
        assertEquals("# Hoy\nfue un buen dia", result.text)
        assertEquals(TextRange(4), result.selection)
    }

    @Test
    fun applyingTheSameLineStyleAgainRemovesIt() {
        val once = DiaryMarkup.toggleLineStyle(value("Gracias por hoy", cursor = 3), DiaryLineStyle.HIGHLIGHT)
        val twice = DiaryMarkup.toggleLineStyle(once, DiaryLineStyle.HIGHLIGHT)
        assertEquals("> Gracias por hoy", once.text)
        assertEquals("Gracias por hoy", twice.text)
        assertEquals(TextRange(3), twice.selection)
    }

    @Test
    fun aLineStyleReplacesAnotherOne() {
        val result = DiaryMarkup.toggleLineStyle(value("- dormir temprano", cursor = 5), DiaryLineStyle.TITLE)
        assertEquals("# dormir temprano", result.text)
    }

    @Test
    fun listIsAppliedToEverySelectedLine() {
        val source = TextFieldValue("agua\npasear\nleer", TextRange(1, 13))
        val result = DiaryMarkup.toggleLineStyle(source, DiaryLineStyle.LIST)
        assertEquals("- agua\n- pasear\n- leer", result.text)
    }

    @Test
    fun boldWrapsTheSelectionAndUnwrapsIt() {
        val selected = TextFieldValue("me senti tranquila", TextRange(9, 18))
        val bold = DiaryMarkup.toggleInlineStyle(selected, DiaryInlineStyle.BOLD)
        assertEquals("me senti **tranquila**", bold.text)
        assertEquals(TextRange(11, 20), bold.selection)
        val plain = DiaryMarkup.toggleInlineStyle(bold, DiaryInlineStyle.BOLD)
        assertEquals("me senti tranquila", plain.text)
        assertEquals(TextRange(9, 18), plain.selection)
    }

    @Test
    fun inlineStyleWithoutSelectionLeavesTheCursorBetweenTheMarkers() {
        val result = DiaryMarkup.toggleInlineStyle(value("Hoy ", cursor = 4), DiaryInlineStyle.ITALIC)
        assertEquals("Hoy **", result.text)
        assertEquals(TextRange(5), result.selection)
    }

    @Test
    fun editorShowsTextWithoutSymbolsAndMapsTheCursor() {
        val source = "# Mi dia\n- **bien**"
        val transformed = DiaryMarkup.transform(source, Color.Black, Color.White)
        assertEquals("Mi dia\n• bien", transformed.text.text)
        // The cursor after "Mi" in the source (offset 4) is after "Mi" in the visible text (offset 2).
        assertEquals(2, transformed.offsetMapping.originalToTransformed(4))
        assertEquals(source.length, transformed.offsetMapping.transformedToOriginal(transformed.text.length))
        for (i in 0..source.length) {
            val visible = transformed.offsetMapping.originalToTransformed(i)
            assert(visible in 0..transformed.text.length)
        }
    }

    @Test
    fun plainTextRemovesEveryFormattingSymbol() {
        assertEquals("Mi dia\nbien y tranquila\nnota", DiaryMarkup.plainText("# Mi dia\n- **bien** y *tranquila*\n> nota"))
    }

    @Test
    fun lineStyleAtCursorDetectsTheCurrentLine() {
        assertEquals(DiaryLineStyle.LIST, DiaryMarkup.lineStyleAtCursor(value("hola\n- uno", cursor = 8)))
        assertNull(DiaryMarkup.lineStyleAtCursor(value("hola\n- uno", cursor = 2)))
    }
}
