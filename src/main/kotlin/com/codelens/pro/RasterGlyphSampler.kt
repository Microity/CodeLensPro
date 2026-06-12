package com.codelens.pro

data class RasterGlyphSample(val cellCount: Int, val cellWidth: Int)

object RasterGlyphSampler {
    fun sample(width: Int, maxCells: Int = 64, preferredCellWidth: Int = 3): RasterGlyphSample {
        if (width <= 0) return RasterGlyphSample(0, 0)
        val safePreferred = preferredCellWidth.coerceAtLeast(2)
        val naturalCells = (width + safePreferred - 1) / safePreferred
        val cellCount = naturalCells.coerceIn(1, maxCells.coerceAtLeast(1))
        val cellWidth = (width / cellCount).coerceAtLeast(2)
        return RasterGlyphSample(cellCount, cellWidth)
    }
}
