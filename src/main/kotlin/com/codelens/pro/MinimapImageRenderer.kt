package com.codelens.pro

import java.awt.image.BufferedImage

class MinimapImageRenderer {
    fun render(snapshot: MinimapSnapshot, width: Int, layout: MinimapLayout): BufferedImage? {
        val glyphData = snapshot.charGlyphData ?: return null
        if (width <= 0 || layout.drawHeight <= 0) return null
        val image = BufferedImage(width, layout.drawHeight, BufferedImage.TYPE_INT_ARGB)
        val raster = image.raster
        val renderHeight = layout.renderHeight
        val pixel = IntArray(4)
        val palette = glyphData.palette

        for (index in glyphData.glyphLines.indices) {
            val documentLine = glyphData.glyphLines[index]
            val visualLine = snapshot.visualLineForDocumentLine(documentLine)
            if (snapshot.documentLineForVisualLine(visualLine) != documentLine) continue
            val xStart = glyphData.glyphXs[index].toInt().coerceIn(0, width - 1)
            val xEnd = (xStart + 1).coerceAtMost(width - 1)
            val yStart = layout.yForVisualLine(visualLine).coerceIn(0, layout.drawHeight - 1)
            val yEnd = (yStart + renderHeight).coerceAtMost(layout.drawHeight)
            if (yEnd <= yStart) continue

            val rgb = palette[glyphData.glyphColorIndexes[index].toInt().coerceIn(0, palette.lastIndex)]
            pixel[0] = (rgb shr 16) and 0xFF
            pixel[1] = (rgb shr 8) and 0xFF
            pixel[2] = rgb and 0xFF
            val code = glyphData.glyphCodes[index].toInt() and 0xFF
            if (!GlyphWeights.isRenderable(code)) continue
            val weights = GlyphWeights.cleanWeights(code, renderHeight)
            val rasterHeight = yEnd - yStart
            for (row in 0 until rasterHeight) {
                val weightIndex = (((row + 0.5) * weights.size) / rasterHeight).toInt().coerceIn(0, weights.lastIndex)
                val alpha = weights[weightIndex]
                if (alpha <= 0f) continue
                pixel[3] = (alpha * 255).toInt().coerceIn(0, 255)
                raster.setPixel(xStart, yStart + row, pixel)
            }
        }
        return image
    }
}
