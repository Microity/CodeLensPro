package com.codelens.pro

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RasterGlyphSamplerTest {
    @Test
    fun `non positive width returns no cells`() {
        val sample = RasterGlyphSampler.sample(0)

        assertEquals(0, sample.cellCount)
        assertEquals(0, sample.cellWidth)
    }

    @Test
    fun `short width still produces visible cells`() {
        val sample = RasterGlyphSampler.sample(5)

        assertTrue(sample.cellCount >= 1)
        assertTrue(sample.cellWidth >= 2)
    }

    @Test
    fun `long width caps cell count`() {
        val sample = RasterGlyphSampler.sample(1_000)

        assertEquals(64, sample.cellCount)
        assertTrue(sample.cellWidth >= 3)
    }
}
