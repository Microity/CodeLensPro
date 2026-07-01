package com.codelens.pro

data class HighlightLineRange(val startLine: Int, val endLine: Int) {
    companion object {
        fun fromOffsets(
            startOffset: Int,
            endOffset: Int,
            textLength: Int,
            lineForOffset: (Int) -> Int,
        ): HighlightLineRange {
            val safeStart = startOffset.coerceIn(0, textLength)
            val endForLine = when {
                endOffset <= startOffset -> safeStart
                else -> (endOffset - 1).coerceIn(0, textLength)
            }
            val startLine = lineForOffset(safeStart)
            val endLine = lineForOffset(endForLine).coerceAtLeast(startLine)
            return HighlightLineRange(startLine, endLine)
        }
    }
}
