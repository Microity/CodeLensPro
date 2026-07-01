package com.codelens.pro

import java.awt.Color

data class LineRenderData(
    val indents: IntArray,
    val lengths: IntArray,
    val colorIndexes: ByteArray,
    val palette: IntArray,
)

data class TokenRenderData(
    val segmentLines: IntArray,
    val segmentXs: ShortArray,
    val segmentWidths: ShortArray,
    val segmentColorIndexes: ByteArray,
    val palette: IntArray,
)

data class CharGlyphRenderData(
    val glyphLines: IntArray,
    val glyphXs: ShortArray,
    val glyphCodes: ByteArray,
    val glyphColorIndexes: ByteArray,
    val palette: IntArray,
)

data class HighlightInfo(
    val startLine: Int,
    val endLine: Int,
    val color: Color,
    val lane: HighlightLane,
)

enum class HighlightLane { ERROR, WARNING }

data class MinimapSnapshot(
    val lineCount: Int,
    val visualLineCount: Int,
    val documentLineToVisualLine: IntArray,
    val visualLineToDocumentLine: IntArray,
    val foldPlaceholderLines: BooleanArray,
    val lines: LineRenderData?,
    val tokenData: TokenRenderData?,
    val charGlyphData: CharGlyphRenderData?,
    val highlights: List<HighlightInfo>,
    val caretLine: Int,
    val renderMode: RenderMode,
    val documentStamp: Long,
    val foldingStamp: Long,
) {
    fun visualLineForDocumentLine(line: Int): Int =
        documentLineToVisualLine[line.coerceIn(0, documentLineToVisualLine.lastIndex)]

    fun documentLineForVisualLine(line: Int): Int =
        visualLineToDocumentLine[line.coerceIn(0, visualLineToDocumentLine.lastIndex)]

    fun isFoldPlaceholderVisualLine(line: Int): Boolean =
        line in foldPlaceholderLines.indices && foldPlaceholderLines[line]
}
