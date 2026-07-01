package com.codelens.pro

import com.intellij.openapi.editor.Editor
import kotlin.math.max

class MinimapSnapshotBuilder(
    private val diagnosticCollector: DaemonDiagnosticCollector = DaemonDiagnosticCollector(),
    private val tokenColorCollector: TokenColorCollector = TokenColorCollector(),
    private val charGlyphCollector: CharGlyphCollector = CharGlyphCollector(),
) {
    fun build(editor: Editor, settings: CodeLensProSettings): MinimapSnapshot {
        val document = editor.document
        val lineCount = max(1, document.lineCount)
        val foldedRanges = FoldedRangeCollector.collectSafely { collectFoldedRanges(editor) }
        val lineMapping = FoldAwareLineMapper.build(lineCount, foldedRanges)
        val foldingStamp = foldingStamp(foldedRanges)
        val mode = PerformanceGuard.modeFor(lineCount, settings)
        val colors = ColorSchemeAdapter(editor, settings)
        val lines = if (mode == RenderMode.MINIMAL) {
            null
        } else {
            val indents = IntArray(document.lineCount)
            val lengths = IntArray(document.lineCount)
            val colorIndexes = ByteArray(document.lineCount)
            val palette = intArrayOf(colors.text.rgb, colors.comment.rgb, colors.keyword.rgb, colors.string.rgb)
            for (line in 0 until document.lineCount) {
                val start = document.getLineStartOffset(line)
                val end = document.getLineEndOffset(line)
                val chars = document.charsSequence
                var firstNonWhitespace = -1
                var lastNonWhitespace = start - 1
                var index = start
                while (index < end) {
                    val char = chars[index]
                    if (!char.isWhitespace()) {
                        if (firstNonWhitespace < 0) firstNonWhitespace = index - start
                        lastNonWhitespace = index
                    }
                    index++
                }
                indents[line] = if (firstNonWhitespace < 0) 0 else firstNonWhitespace
                lengths[line] = if (lastNonWhitespace < start) 0 else lastNonWhitespace - start + 1
                colorIndexes[line] = colors.lineColorIndex(chars.subSequence(start, end)).toByte()
            }
            LineRenderData(indents, lengths, colorIndexes, palette)
        }
        val charGlyphData = if (mode == RenderMode.FULL) {
            charGlyphCollector.collect(editor, settings, lineCount)
        } else {
            null
        }
        val tokenData = if (mode == RenderMode.FULL && charGlyphData == null) {
            tokenColorCollector.collect(editor, settings, lineCount)
        } else {
            null
        }
        val highlights = if (mode == RenderMode.MINIMAL) emptyList() else diagnosticCollector.collect(editor, settings, colors)
        return MinimapSnapshot(
            lineCount,
            lineMapping.visualLineCount,
            lineMapping.documentLineToVisualLine,
            lineMapping.visualLineToDocumentLine,
            lineMapping.foldPlaceholderLines,
            lines,
            tokenData,
            charGlyphData,
            highlights,
            editor.caretModel.logicalPosition.line,
            mode,
            document.modificationStamp,
            foldingStamp,
        )
    }

    fun foldingStamp(editor: Editor): Long = foldingStamp(FoldedRangeCollector.collectSafely { collectFoldedRanges(editor) })

    private fun collectFoldedRanges(editor: Editor): List<FoldedLineRange> {
        val document = editor.document
        val ranges = ArrayList<FoldedLineRange>()
        for (region in editor.foldingModel.allFoldRegions) {
            if (!region.isValid || region.isExpanded) continue
            val start = region.startOffset.coerceIn(0, document.textLength)
            val end = region.endOffset.coerceIn(0, document.textLength)
            if (end <= start) continue
            val startLine = document.getLineNumber(start)
            val endLine = document.getLineNumber((end - 1).coerceAtLeast(start).coerceAtMost(document.textLength))
            ranges.add(FoldedLineRange(startLine, endLine))
        }
        return ranges
    }

    private fun foldingStamp(ranges: List<FoldedLineRange>): Long {
        var stamp = ranges.size.toLong()
        for (range in ranges) {
            stamp = stamp * 31 + range.startLine
            stamp = stamp * 31 + range.endLine
        }
        return stamp
    }
}
