package com.codelens.pro

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.util.EmptyEditorHighlighter
import java.awt.Color
import kotlin.math.min

class TokenColorCollector {
    fun collect(editor: Editor, settings: CodeLensProSettings, lineCount: Int): TokenRenderData? {
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

            val segments = SegmentBuffer()
            val perLineSegments = IntArray(lineCount)

            val iterator = highlighter.createIterator(0)
            while (!iterator.atEnd() && segments.size < MAX_TOTAL_SEGMENTS) {
                val start = iterator.start.coerceIn(0, document.textLength)
                val end = iterator.end.coerceIn(0, document.textLength)
                if (end > start) {
                    val colorIndex = indexFor(iterator.textAttributes?.foregroundColor).coerceIn(0, Byte.MAX_VALUE.toInt())
                    appendToken(
                        document,
                        start,
                        end,
                        colorIndex,
                        perLineSegments,
                        segments,
                    )
                }
                iterator.advance()
            }

            if (segments.size == 0) {
                null
            } else {
                TokenRenderData(
                    segments.lines(),
                    segments.xs(),
                    segments.widths(),
                    segments.colorIndexes(),
                    palette.toIntArray(),
                )
            }
        }.getOrNull()
    }

    private fun appendToken(
        document: Document,
        startOffset: Int,
        endOffset: Int,
        colorIndex: Int,
        perLineSegments: IntArray,
        segments: SegmentBuffer,
    ) {
        var line = document.getLineNumber(startOffset)
        val endLine = document.getLineNumber((endOffset - 1).coerceAtLeast(startOffset))
        while (line <= endLine && line < perLineSegments.size && segments.size < MAX_TOTAL_SEGMENTS) {
            if (perLineSegments[line] < MAX_SEGMENTS_PER_LINE) {
                val lineStart = document.getLineStartOffset(line)
                val lineEnd = document.getLineEndOffset(line)
                val from = startOffset.coerceAtLeast(lineStart)
                val to = endOffset.coerceAtMost(lineEnd)
                val first = firstNonWhitespace(document.charsSequence, from, to)
                val last = lastNonWhitespace(document.charsSequence, from, to)
                if (first <= last) {
                    val added = segments.add(
                        line,
                        (first - lineStart).coerceIn(0, Short.MAX_VALUE.toInt()),
                        (last - first + 1).coerceIn(1, Short.MAX_VALUE.toInt()),
                        colorIndex,
                    )
                    if (added) perLineSegments[line]++
                }
            }
            line++
        }
    }

    private fun firstNonWhitespace(chars: CharSequence, start: Int, end: Int): Int {
        var index = start
        while (index < end && chars[index].isWhitespace()) index++
        return index
    }

    private fun lastNonWhitespace(chars: CharSequence, start: Int, end: Int): Int {
        var index = min(end, chars.length) - 1
        while (index >= start && chars[index].isWhitespace()) index--
        return index
    }

    companion object {
        const val MAX_PALETTE_COLORS = 96
        const val MAX_SEGMENTS_PER_LINE = 24
        const val MAX_TOTAL_SEGMENTS = 80_000
    }

    private class SegmentBuffer(initialCapacity: Int = 512) {
        private var segmentLines = IntArray(initialCapacity)
        private var segmentXs = ShortArray(initialCapacity)
        private var segmentWidths = ShortArray(initialCapacity)
        private var segmentColorIndexes = ByteArray(initialCapacity)
        var size: Int = 0
            private set

        fun add(line: Int, x: Int, width: Int, colorIndex: Int): Boolean {
            val last = size - 1
            if (last >= 0 && segmentLines[last] == line && segmentColorIndexes[last].toInt() == colorIndex) {
                val lastX = segmentXs[last].toInt()
                val lastWidth = segmentWidths[last].toInt()
                if (lastX + lastWidth >= x) {
                    val mergedWidth = (x + width - lastX).coerceIn(1, Short.MAX_VALUE.toInt())
                    segmentWidths[last] = mergedWidth.toShort()
                    return false
                }
            }
            ensureCapacity(size + 1)
            segmentLines[size] = line
            segmentXs[size] = x.toShort()
            segmentWidths[size] = width.toShort()
            segmentColorIndexes[size] = colorIndex.toByte()
            size++
            return true
        }

        fun lines(): IntArray = segmentLines.copyOf(size)
        fun xs(): ShortArray = segmentXs.copyOf(size)
        fun widths(): ShortArray = segmentWidths.copyOf(size)
        fun colorIndexes(): ByteArray = segmentColorIndexes.copyOf(size)

        private fun ensureCapacity(required: Int) {
            if (required <= segmentLines.size) return
            val newCapacity = minOf(MAX_TOTAL_SEGMENTS, segmentLines.size * 2)
            segmentLines = segmentLines.copyOf(newCapacity)
            segmentXs = segmentXs.copyOf(newCapacity)
            segmentWidths = segmentWidths.copyOf(newCapacity)
            segmentColorIndexes = segmentColorIndexes.copyOf(newCapacity)
        }
    }
}
