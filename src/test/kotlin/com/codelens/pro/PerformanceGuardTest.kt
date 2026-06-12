package com.codelens.pro

import kotlin.test.Test
import kotlin.test.assertEquals

class PerformanceGuardTest {
    @Test
    fun `render mode follows thresholds`() {
        val settings = CodeLensProSettings().apply {
            largeFileLineThreshold = 3_000
            hugeFileLineThreshold = 10_000
        }

        assertEquals(RenderMode.FULL, PerformanceGuard.modeFor(100, settings))
        assertEquals(RenderMode.SIMPLIFIED, PerformanceGuard.modeFor(5_000, settings))
        assertEquals(RenderMode.MINIMAL, PerformanceGuard.modeFor(20_000, settings))
    }
}
