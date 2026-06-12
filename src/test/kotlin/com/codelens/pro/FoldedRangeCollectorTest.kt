package com.codelens.pro

import kotlin.test.Test
import kotlin.test.assertTrue

class FoldedRangeCollectorTest {
    @Test
    fun `returns empty ranges when folding api throws`() {
        val ranges = FoldedRangeCollector.collectSafely {
            error("folding model not ready")
        }

        assertTrue(ranges.isEmpty())
    }
}
