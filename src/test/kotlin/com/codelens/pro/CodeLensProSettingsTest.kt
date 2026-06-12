package com.codelens.pro

import kotlin.test.Test
import kotlin.test.assertEquals

class CodeLensProSettingsTest {
    @Test
    fun `clampWidth keeps values inside configured range`() {
        assertEquals(CodeLensProSettings.MIN_WIDTH, CodeLensProSettings.clampWidth(1))
        assertEquals(90, CodeLensProSettings.clampWidth(90))
        assertEquals(CodeLensProSettings.MAX_WIDTH, CodeLensProSettings.clampWidth(999))
    }

    @Test
    fun `performance values are clamped`() {
        assertEquals(500, CodeLensProSettings.clampLargeFileThreshold(1))
        assertEquals(1_000, CodeLensProSettings.clampHugeFileThreshold(1))
    }
}
