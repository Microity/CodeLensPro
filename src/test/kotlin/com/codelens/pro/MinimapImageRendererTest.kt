package com.codelens.pro

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MinimapImageRendererTest {
    @Test
    fun `renders glyph color into alpha raster image`() {
        val snapshot = MinimapSnapshot(
            lineCount = 1,
            visualLineCount = 1,
            documentLineToVisualLine = intArrayOf(0),
            visualLineToDocumentLine = intArrayOf(0),
            foldPlaceholderLines = booleanArrayOf(false),
            lines = null,
            tokenData = null,
            charGlyphData = CharGlyphRenderData(
                glyphLines = intArrayOf(0),
                glyphXs = shortArrayOf(1),
                glyphCodes = byteArrayOf('a'.code.toByte()),
                glyphColorIndexes = byteArrayOf(0),
                palette = intArrayOf(0x112233),
            ),
            highlights = emptyList(),
            caretLine = 0,
            renderMode = RenderMode.FULL,
            documentStamp = 1L,
            foldingStamp = 1L,
        )

        val image = assertNotNull(MinimapImageRenderer().render(snapshot, width = 8, layout = MinimapLayout.compute(visualLineCount = 1, panelHeight = 4)))
        val pixel = IntArray(4)
        image.raster.getPixel(1, 1, pixel)

        assertEquals(0x11, pixel[0])
        assertEquals(0x22, pixel[1])
        assertEquals(0x33, pixel[2])
        assertTrue(pixel[3] > 0)
    }
}
