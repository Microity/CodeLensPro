package com.codelens.pro

import kotlin.math.max
import kotlin.math.roundToInt

data class MinimapLayout(
    val pixelsPerLine: Double,
    val renderHeight: Int,
    val contentHeight: Int,
    val drawHeight: Int,
) {
    fun yForVisualLine(visualLine: Int): Int = (visualLine.coerceAtLeast(0) * pixelsPerLine).toInt()

    fun visualLineForY(y: Int, visualLineCount: Int): Int {
        if (visualLineCount <= 0 || drawHeight <= 0 || pixelsPerLine <= 0.0) return 0
        if (y >= drawHeight) return visualLineCount - 1
        return (y.coerceAtLeast(0) / pixelsPerLine).toInt().coerceIn(0, visualLineCount - 1)
    }

    companion object {
        const val DEFAULT_PIXELS_PER_LINE = 2.0

        fun compute(
            visualLineCount: Int,
            panelHeight: Int,
            preferredPixelsPerLine: Double = DEFAULT_PIXELS_PER_LINE,
        ): MinimapLayout {
            val lineCount = visualLineCount.coerceAtLeast(1)
            if (panelHeight <= 0) {
                return MinimapLayout(0.0, 1, 0, 0)
            }
            val naturalHeight = (lineCount * preferredPixelsPerLine).roundToInt().coerceAtLeast(1)
            val pixelsPerLine = if (naturalHeight <= panelHeight) {
                preferredPixelsPerLine
            } else {
                panelHeight.toDouble() / lineCount.toDouble()
            }
            val contentHeight = if (naturalHeight <= panelHeight) {
                naturalHeight
            } else {
                panelHeight
            }
            return MinimapLayout(
                pixelsPerLine = pixelsPerLine,
                renderHeight = max(1, pixelsPerLine.toInt()),
                contentHeight = contentHeight,
                drawHeight = contentHeight.coerceIn(0, panelHeight),
            )
        }
    }
}
