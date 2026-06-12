package com.codelens.pro

object FoldedRangeCollector {
    fun collectSafely(collector: () -> List<FoldedLineRange>): List<FoldedLineRange> =
        runCatching { collector() }.getOrDefault(emptyList())
}
