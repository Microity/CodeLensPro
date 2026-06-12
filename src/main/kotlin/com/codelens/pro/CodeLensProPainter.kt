package com.codelens.pro

import com.intellij.openapi.editor.Editor
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.image.BufferedImage
import kotlin.math.max
import kotlin.math.min

class CodeLensProPainter {
    fun paint(g: Graphics2D, editor: Editor, settings: CodeLensProSettings, snapshot: MinimapSnapshot, bounds: Rectangle, layout: MinimapLayout, minimapImage: BufferedImage? = null) {
        val colors = ColorSchemeAdapter(editor, settings)
        val heightPerLine = layout.pixelsPerLine

        g.color = colors.background
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height)

        if (minimapImage != null) {
            g.drawImage(minimapImage, bounds.x, bounds.y, null)
        } else if (snapshot.charGlyphData != null) {
            if (snapshot.tokenData != null) {
                paintTokenSegments(g, colors, snapshot, snapshot.tokenData, bounds, heightPerLine)
            } else {
                paintFallbackLines(g, colors, snapshot, bounds, heightPerLine)
            }
        } else if (snapshot.tokenData != null) {
            paintTokenSegments(g, colors, snapshot, snapshot.tokenData, bounds, heightPerLine)
        } else {
            paintFallbackLines(g, colors, snapshot, bounds, heightPerLine)
        }

        paintFoldPlaceholders(g, colors, snapshot, bounds, layout)
        paintHighlights(g, colors, snapshot, bounds, layout)
        if (settings.showViewport) paintViewport(g, editor, colors, snapshot, bounds, layout)
        if (settings.showCaretLine) paintCaret(g, colors, snapshot, bounds, layout)
    }

    private fun paintFallbackLines(g: Graphics2D, colors: ColorSchemeAdapter, snapshot: MinimapSnapshot, bounds: Rectangle, heightPerLine: Double) {
        snapshot.lines?.let { lines ->
            val strokeHeight = max(1, heightPerLine.toInt())
            val palette = lines.palette
            for (line in lines.indents.indices) {
                val visualLine = snapshot.visualLineForDocumentLine(line)
                if (snapshot.documentLineForVisualLine(visualLine) != line) continue
                val y = bounds.y + (visualLine * heightPerLine).toInt()
                val x = bounds.x + min(bounds.width / 2, lines.indents[line] * 2)
                val lineWidth = min(bounds.width - (x - bounds.x) - 4, max(4, lines.lengths[line] * 2))
                if (lineWidth > 0) {
                    g.color = colors.linePaint(palette[lines.colorIndexes[line].toInt().coerceIn(0, palette.lastIndex)])
                    paintCodeTexture(g, x, y, lineWidth, strokeHeight, line xor lineWidth)
                }
            }
        }
    }

    private fun paintTokenSegments(g: Graphics2D, colors: ColorSchemeAdapter, snapshot: MinimapSnapshot, tokenData: TokenRenderData, bounds: Rectangle, heightPerLine: Double) {
        val strokeHeight = max(1, heightPerLine.toInt())
        val palette = tokenData.palette
        for (index in tokenData.segmentLines.indices) {
            val line = tokenData.segmentLines[index]
            val visualLine = snapshot.visualLineForDocumentLine(line)
            if (snapshot.documentLineForVisualLine(visualLine) != line) continue
            val y = bounds.y + (visualLine * heightPerLine).toInt()
            val x = bounds.x + min(bounds.width - 2, tokenData.segmentXs[index].toInt() * 2)
            val width = min(bounds.width - (x - bounds.x) - 2, max(1, tokenData.segmentWidths[index].toInt() * 2))
            if (width > 0) {
                g.color = colors.linePaint(palette[tokenData.segmentColorIndexes[index].toInt().coerceIn(0, palette.lastIndex)])
                paintCodeTexture(g, x, y, width, strokeHeight, line xor tokenData.segmentXs[index].toInt() xor width)
            }
        }
    }

    private fun paintCodeTexture(g: Graphics2D, x: Int, y: Int, width: Int, height: Int, seed: Int) {
        if (width <= 0 || height <= 0) return
        val sample = RasterGlyphSampler.sample(width, MAX_GLYPH_CELLS_PER_SEGMENT, preferredCellWidth = 3)
        if (sample.cellCount <= 0) return
        val topY = y
        val middleY = y + when {
            height <= 1 -> 0
            height == 2 -> 1
            else -> height / 2
        }
        val bottomY = y + when {
            height <= 1 -> 0
            else -> height - 1
        }
        val canDrawTop = height >= 3
        val canDrawStem = height >= 2
        val cell = sample.cellWidth
        var cursor = x
        var state = seed * 1103515245 + 12345
        val end = x + width
        var drawn = 0
        while (cursor < end && drawn < sample.cellCount) {
            val gap = if ((state and 0x7) == 0) 1 else 0
            val cellStart = cursor + gap
            if (cellStart < end) {
                val available = end - cellStart
                val bodyWidth = min(max(2, cell - 1), available)
                val variant = (state ushr 8) and 0x7
                val primaryY = if ((variant and 0x1) == 0) middleY else bottomY
                g.fillRect(cellStart, primaryY, bodyWidth, 1)
                if (canDrawTop && (variant and 0x2) != 0) {
                    g.fillRect(cellStart, topY, max(1, bodyWidth - 1), 1)
                }
                if (canDrawStem && (variant and 0x4) != 0) {
                    val stemX = min(cellStart + bodyWidth - 1, end - 1)
                    val stemHeight = if (height >= 3) 2 else 1
                    g.fillRect(stemX, topY, 1, stemHeight)
                }
            }
            cursor += cell
            state = state * 1103515245 + 12345
            drawn++
        }
    }

    private fun paintHighlights(g: Graphics2D, colors: ColorSchemeAdapter, snapshot: MinimapSnapshot, bounds: Rectangle, layout: MinimapLayout) {
        snapshot.highlights.forEach { highlight ->
            g.color = highlight.color
            val x = when (highlight.lane) {
                HighlightLane.ERROR, HighlightLane.WARNING -> bounds.x + bounds.width - 5
                HighlightLane.VCS_ADDED, HighlightLane.VCS_MODIFIED, HighlightLane.VCS_DELETED -> bounds.x
                else -> bounds.x + bounds.width - 10
            }
            val startVisualLine = snapshot.visualLineForDocumentLine(highlight.startLine)
            val endVisualLine = snapshot.visualLineForDocumentLine(highlight.endLine)
            val y = bounds.y + layout.yForVisualLine(startVisualLine)
            val h = max(2, ((endVisualLine - startVisualLine + 1) * layout.pixelsPerLine).toInt()).coerceAtMost((bounds.y + layout.drawHeight - y).coerceAtLeast(0))
            g.fillRect(x, y, 4, h)
        }
    }

    private fun paintFoldPlaceholders(g: Graphics2D, colors: ColorSchemeAdapter, snapshot: MinimapSnapshot, bounds: Rectangle, layout: MinimapLayout) {
        val strokeHeight = layout.renderHeight
        g.color = colors.linePaint(colors.text.rgb)
        for (visualLine in 0 until snapshot.visualLineCount) {
            if (!snapshot.isFoldPlaceholderVisualLine(visualLine)) continue
            val y = bounds.y + layout.yForVisualLine(visualLine) + max(0, strokeHeight / 2)
            if (y >= bounds.y + layout.drawHeight) continue
            val markerWidth = max(8, bounds.width / 4)
            val x = bounds.x + bounds.width - markerWidth - 2
            g.fillRect(x, y, markerWidth, 1)
        }
    }

    private fun paintViewport(g: Graphics2D, editor: Editor, colors: ColorSchemeAdapter, snapshot: MinimapSnapshot, bounds: Rectangle, layout: MinimapLayout) {
        val lineCount = max(1, snapshot.visualLineCount)
        val visibleArea = editor.scrollingModel.visibleArea
        val startLogical = editor.xyToLogicalPosition(visibleArea.location).line.coerceIn(0, snapshot.lineCount - 1)
        val endLogical = editor.xyToLogicalPosition(java.awt.Point(visibleArea.x, visibleArea.y + visibleArea.height)).line.coerceIn(0, snapshot.lineCount - 1)
        val startVisual = snapshot.visualLineForDocumentLine(startLogical)
        val endVisual = snapshot.visualLineForDocumentLine(endLogical)
        val y1 = bounds.y + layout.yForVisualLine(startVisual)
        val y2 = bounds.y + layout.yForVisualLine(endVisual.coerceIn(0, lineCount - 1))
        val viewportHeight = max(8, y2 - y1)
        g.color = colors.viewport
        g.fillRect(bounds.x, y1, bounds.width, viewportHeight.coerceAtMost((bounds.y + layout.drawHeight - y1).coerceAtLeast(0)))
        g.color = colors.viewportBorder
        g.drawRect(bounds.x, y1, bounds.width - 1, viewportHeight.coerceAtMost((bounds.y + layout.drawHeight - y1).coerceAtLeast(1)) - 1)
    }

    private fun paintCaret(g: Graphics2D, colors: ColorSchemeAdapter, snapshot: MinimapSnapshot, bounds: Rectangle, layout: MinimapLayout) {
        val visualLine = snapshot.visualLineForDocumentLine(snapshot.caretLine)
        val y = bounds.y + layout.yForVisualLine(visualLine)
        if (y >= bounds.y + layout.drawHeight) return
        g.color = colors.caret
        g.fillRect(bounds.x, y, bounds.width, 2)
    }

    companion object {
        private const val MAX_GLYPH_CELLS_PER_SEGMENT = 64
    }
}
