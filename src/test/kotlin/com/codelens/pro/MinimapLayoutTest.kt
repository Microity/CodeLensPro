package com.codelens.pro

import kotlin.test.Test
import kotlin.test.assertEquals

class MinimapLayoutTest {
    @Test
    fun `short file keeps preferred pixels per line instead of stretching`() {
        val layout = MinimapLayout.compute(visualLineCount = 20, panelHeight = 1000)

        assertEquals(2.0, layout.pixelsPerLine)
        assertEquals(2, layout.renderHeight)
        assertEquals(40, layout.contentHeight)
        assertEquals(40, layout.drawHeight)
    }

    @Test
    fun `long file fits into panel height`() {
        val layout = MinimapLayout.compute(visualLineCount = 2000, panelHeight = 1000)

        assertEquals(0.5, layout.pixelsPerLine)
        assertEquals(1, layout.renderHeight)
        assertEquals(1000, layout.contentHeight)
        assertEquals(1000, layout.drawHeight)
    }

    @Test
    fun `zero panel height is safe`() {
        val layout = MinimapLayout.compute(visualLineCount = 20, panelHeight = 0)

        assertEquals(0.0, layout.pixelsPerLine)
        assertEquals(1, layout.renderHeight)
        assertEquals(0, layout.contentHeight)
        assertEquals(0, layout.drawHeight)
    }
}
