package com.codelens.pro

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.util.EmptyEditorHighlighter
import java.awt.Color

class CharGlyphCollector {
    fun collect(editor: Editor, settings: CodeLensProSettings, lineCount: Int): CharGlyphRenderData? {
        if (!settings.useSimplifiedLanguageColors) return null
        if (lineCount > settings.largeFileLineThreshold) return null
        val highlighter = editor.highlighter
        if (highlighter is EmptyEditorHighlighter) return null

        return runCatching {
            val document = editor.document
            val defaultColor = editor.colorsScheme.defaultForeground
            val palette = ArrayList<Int>(32)
            val paletteIndex = HashMap<Int, Int>(32)
            fun indexFor(color: Color?): Int {
                val rgb = (color ?: defaultColor).rgb
                val existing = paletteIndex[rgb]
                if (existing != null) return existing
                if (palette.size >= MAX_PALETTE_COLORS) return 0
                palette.add(rgb)
                val index = palette.lastIndex
                paletteIndex[rgb] = index
                return index
            }
            indexFor(defaultColor)

            val glyphs = GlyphBuffer()
            val perLineGlyphs = IntArray(lineCount)
            val iterator = highlighter.createIterator(0)
            while (!iterator.atEnd() && glyphs.size < MAX_TOTAL_GLYPHS) {
                val start = iterator.start.coerceIn(0, document.textLength)
                val end = iterator.end.coerceIn(0, document.textLength)
                if (end > start) {
                    val colorIndex = indexFor(iterator.textAttributes?.foregroundColor).coerceIn(0, Byte.MAX_VALUE.toInt())
                    appendGlyphs(document, start, end, colorIndex, perLineGlyphs, glyphs)
                }
                iterator.advance()
            }

            if (glyphs.size == 0) null else CharGlyphRenderData(
                glyphs.lines(),
                glyphs.xs(),
                glyphs.codes(),
                glyphs.colorIndexes(),
                palette.toIntArray(),
            )
        }.getOrNull()
    }

    private fun appendGlyphs(
        document: Document,
        startOffset: Int,
        endOffset: Int,
        colorIndex: Int,
        perLineGlyphs: IntArray,
        glyphs: GlyphBuffer,
    ) {
        val chars = document.charsSequence
        var offset = startOffset
        var line = document.getLineNumber(startOffset)
        var lineStart = document.getLineStartOffset(line)
        var column = offset - lineStart
        while (offset < endOffset && offset < chars.length && glyphs.size < MAX_TOTAL_GLYPHS) {
            val char = chars[offset]
            if (char == '\n' || char == '\r') {
                offset++
                if (offset < chars.length) {
                    line = document.getLineNumber(offset)
                    lineStart = document.getLineStartOffset(line)
                    column = offset - lineStart
                }
                continue
            }
            if (line in perLineGlyphs.indices && perLineGlyphs[line] < MAX_GLYPHS_PER_LINE) {
                if (char == '\t') {
                    column += TAB_WIDTH
                } else {
                    val code = glyphCode(char)
                    if (char.code > 32 && GlyphWeights.isRenderable(code)) {
                        glyphs.add(
                            line,
                            column.coerceIn(0, Short.MAX_VALUE.toInt()),
                            code,
                            colorIndex,
                        )
                        perLineGlyphs[line]++
                    }
                    column++
                }
            } else {
                column++
            }
            offset++
        }
    }

    companion object {
        const val MAX_PALETTE_COLORS = 96
        const val MAX_GLYPHS_PER_LINE = 160
        const val MAX_TOTAL_GLYPHS = 120_000
        const val TAB_WIDTH = 4

        fun glyphCode(char: Char): Int = if (char.code in 33..126) char.code else 0
    }

    private class GlyphBuffer(initialCapacity: Int = 1024) {
        private var glyphLines = IntArray(initialCapacity)
        private var glyphXs = ShortArray(initialCapacity)
        private var glyphCodes = ByteArray(initialCapacity)
        private var glyphColorIndexes = ByteArray(initialCapacity)
        var size: Int = 0
            private set

        fun add(line: Int, x: Int, code: Int, colorIndex: Int) {
            ensureCapacity(size + 1)
            glyphLines[size] = line
            glyphXs[size] = x.toShort()
            glyphCodes[size] = code.toByte()
            glyphColorIndexes[size] = colorIndex.toByte()
            size++
        }

        fun lines(): IntArray = glyphLines.copyOf(size)
        fun xs(): ShortArray = glyphXs.copyOf(size)
        fun codes(): ByteArray = glyphCodes.copyOf(size)
        fun colorIndexes(): ByteArray = glyphColorIndexes.copyOf(size)

        private fun ensureCapacity(required: Int) {
            if (required <= glyphLines.size) return
            val newCapacity = minOf(MAX_TOTAL_GLYPHS, glyphLines.size * 2)
            glyphLines = glyphLines.copyOf(newCapacity)
            glyphXs = glyphXs.copyOf(newCapacity)
            glyphCodes = glyphCodes.copyOf(newCapacity)
            glyphColorIndexes = glyphColorIndexes.copyOf(newCapacity)
        }
    }
}
