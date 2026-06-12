package com.codelens.pro

data class FoldedLineRange(val startLine: Int, val endLine: Int)

data class FoldAwareLineMapping(
    val visualLineCount: Int,
    val documentLineToVisualLine: IntArray,
    val visualLineToDocumentLine: IntArray,
    val foldPlaceholderLines: BooleanArray,
) {
    fun visualLineForDocumentLine(line: Int): Int =
        documentLineToVisualLine[line.coerceIn(0, documentLineToVisualLine.lastIndex)]

    fun documentLineForVisualLine(line: Int): Int =
        visualLineToDocumentLine[line.coerceIn(0, visualLineToDocumentLine.lastIndex)]

    fun isFoldPlaceholderVisualLine(line: Int): Boolean =
        line in foldPlaceholderLines.indices && foldPlaceholderLines[line]
}

object FoldAwareLineMapper {
    fun identity(lineCount: Int): FoldAwareLineMapping = build(lineCount, emptyList())

    fun build(lineCount: Int, ranges: List<FoldedLineRange>): FoldAwareLineMapping {
        val safeLineCount = lineCount.coerceAtLeast(1)
        val normalized = normalizeRanges(safeLineCount, ranges)
        val documentToVisual = IntArray(safeLineCount)
        val visualToDocument = IntArray(safeLineCount)
        val placeholders = BooleanArray(safeLineCount)

        var visualLine = 0
        var rangeIndex = 0
        var documentLine = 0
        while (documentLine < safeLineCount) {
            val range = normalized.getOrNull(rangeIndex)
            if (range != null && documentLine == range.startLine) {
                visualToDocument[visualLine] = documentLine
                placeholders[visualLine] = range.endLine > range.startLine
                for (line in range.startLine..range.endLine) {
                    documentToVisual[line] = visualLine
                }
                documentLine = range.endLine + 1
                visualLine++
                rangeIndex++
            } else {
                documentToVisual[documentLine] = visualLine
                visualToDocument[visualLine] = documentLine
                documentLine++
                visualLine++
            }
        }

        return FoldAwareLineMapping(
            visualLine.coerceAtLeast(1),
            documentToVisual,
            visualToDocument.copyOf(visualLine.coerceAtLeast(1)),
            placeholders.copyOf(visualLine.coerceAtLeast(1)),
        )
    }

    private fun normalizeRanges(lineCount: Int, ranges: List<FoldedLineRange>): List<FoldedLineRange> {
        if (ranges.isEmpty()) return emptyList()
        val sorted = ranges
            .mapNotNull { range ->
                val start = range.startLine.coerceIn(0, lineCount - 1)
                val end = range.endLine.coerceIn(0, lineCount - 1)
                if (end < start) null else FoldedLineRange(start, end)
            }
            .sortedWith(compareBy<FoldedLineRange> { it.startLine }.thenByDescending { it.endLine })
        if (sorted.isEmpty()) return emptyList()

        val result = ArrayList<FoldedLineRange>(sorted.size)
        var current = sorted.first()
        for (index in 1 until sorted.size) {
            val next = sorted[index]
            if (next.startLine <= current.endLine) {
                current = FoldedLineRange(current.startLine, maxOf(current.endLine, next.endLine))
            } else {
                result.add(current)
                current = next
            }
        }
        result.add(current)
        return result
    }
}
