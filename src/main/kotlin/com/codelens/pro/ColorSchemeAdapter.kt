package com.codelens.pro

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.ui.JBColor
import java.awt.Color

class ColorSchemeAdapter(private val editor: Editor, private val settings: CodeLensProSettings) {
    private val scheme = editor.colorsScheme ?: EditorColorsManager.getInstance().globalScheme
    private val paintCache = HashMap<Int, Color>(8)

    val background: Color = scheme.defaultBackground

    val text: Color = dim(scheme.defaultForeground, 0.62f)

    val comment: Color = scheme.getAttributes(EditorColors.FOLDED_TEXT_ATTRIBUTES)?.foregroundColor ?: JBColor.GRAY
    val keyword: Color = scheme.getFont(EditorFontType.BOLD).let { text.brighter() }
    val string: Color = JBColor(Color(0x2E7D32), Color(0x7CB342))
    val viewport: Color = JBColor(Color(0x4487CEFA, true), Color(0x4462AEEF, true))
    val viewportBorder: Color = JBColor(Color(0x8887CEFA.toInt(), true), Color(0x8862AEEF.toInt(), true))
    val caret: Color = JBColor(Color(0xD84315), Color(0xFFAB40))
    val error: Color = JBColor(Color(0xD32F2F), Color(0xEF5350))
    val warning: Color = JBColor(Color(0xF9A825), Color(0xFFCA28))
    val markup: Color = JBColor(Color(0x7E57C2), Color(0xB39DDB))
    val vcsAdded: Color = JBColor(Color(0x2E7D32), Color(0x66BB6A))
    val vcsModified: Color = JBColor(Color(0x1565C0), Color(0x42A5F5))
    val vcsDeleted: Color = JBColor(Color(0xC62828), Color(0xEF5350))

    fun lineColor(textLine: CharSequence): Color {
        return when (lineColorIndex(textLine)) {
            LINE_COMMENT -> comment
            LINE_KEYWORD -> keyword
            LINE_STRING -> string
            else -> text
        }
    }

    fun lineColorIndex(textLine: CharSequence): Int {
        if (!settings.useSimplifiedLanguageColors) return LINE_TEXT
        val start = firstNonWhitespace(textLine)
        return when {
            startsWith(textLine, start, "//") || startsWith(textLine, start, "/*") || startsWith(textLine, start, "*") || startsWith(textLine, start, "#") -> LINE_COMMENT
            contains(textLine, '"') || contains(textLine, '\'') -> LINE_STRING
            KEYWORDS.any { startsWithWord(textLine, start, it) } -> LINE_KEYWORD
            else -> LINE_TEXT
        }
    }

    fun linePaint(rgb: Int): Color = paintCache.getOrPut(rgb) { Color(rgb, true) }

    private fun firstNonWhitespace(textLine: CharSequence): Int {
        var start = 0
        while (start < textLine.length && textLine[start].isWhitespace()) start++
        return start
    }

    @Suppress("unused")
    private fun legacyLineColor(textLine: CharSequence): Color {
        if (!settings.useSimplifiedLanguageColors) return text
        val start = firstNonWhitespace(textLine)
        return when {
            startsWith(textLine, start, "//") || startsWith(textLine, start, "/*") || startsWith(textLine, start, "*") || startsWith(textLine, start, "#") -> comment
            contains(textLine, '"') || contains(textLine, '\'') -> string
            KEYWORDS.any { startsWithWord(textLine, start, it) } -> keyword
            else -> text
        }
    }

    private fun startsWith(text: CharSequence, start: Int, prefix: String): Boolean {
        if (start + prefix.length > text.length) return false
        for (index in prefix.indices) {
            if (text[start + index] != prefix[index]) return false
        }
        return true
    }

    private fun startsWithWord(text: CharSequence, start: Int, word: String): Boolean {
        if (!startsWith(text, start, word)) return false
        val next = start + word.length
        return next == text.length || text[next].isWhitespace()
    }

    private fun contains(text: CharSequence, needle: Char): Boolean {
        for (index in 0 until text.length) {
            if (text[index] == needle) return true
        }
        return false
    }

    private fun dim(color: Color, factor: Float): Color = Color(
        (color.red * factor).toInt().coerceIn(0, 255),
        (color.green * factor).toInt().coerceIn(0, 255),
        (color.blue * factor).toInt().coerceIn(0, 255),
        color.alpha,
    )

    companion object {
        private const val LINE_TEXT = 0
        private const val LINE_COMMENT = 1
        private const val LINE_KEYWORD = 2
        private const val LINE_STRING = 3
        private val KEYWORDS = setOf("class", "fun", "val", "var", "if", "else", "for", "while", "return", "public", "private", "protected", "import", "package")
    }
}
