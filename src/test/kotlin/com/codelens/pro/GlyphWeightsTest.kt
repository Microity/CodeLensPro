package com.codelens.pro

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GlyphWeightsTest {
    @Test
    fun `whitespace is not renderable`() {
        assertFalse(GlyphWeights.isRenderable(' '.code))
        assertFalse(GlyphWeights.isRenderable('\n'.code))
    }

    @Test
    fun `visible characters are renderable`() {
        assertTrue(GlyphWeights.isRenderable('a'.code))
        assertTrue(GlyphWeights.isRenderable('{'.code))
        assertTrue(GlyphWeights.isRenderable(0))
    }

    @Test
    fun `clean weights follow render height`() {
        assertEquals(1, GlyphWeights.cleanWeights('a'.code, 1).size)
        assertEquals(2, GlyphWeights.cleanWeights('a'.code, 2).size)
        assertEquals(3, GlyphWeights.cleanWeights('a'.code, 3).size)
        assertEquals(4, GlyphWeights.cleanWeights('a'.code, 8).size)
    }

    @Test
    fun `ascii visible characters are stronger than fallback glyphs`() {
        val ascii = GlyphWeights.cleanWeights('a'.code, 1).first()
        val fallback = GlyphWeights.cleanWeights(0, 1).first()

        assertTrue(ascii > fallback)
    }
}
