package com.codelens.pro

object MinimapWheelScroll {
    fun targetOffset(
        currentOffset: Int,
        wheelRotation: Int,
        scrollAmount: Int,
        lineHeight: Int,
        visibleHeight: Int,
        documentHeight: Int,
    ): Int {
        val maxOffset = (documentHeight - visibleHeight).coerceAtLeast(0)
        if (maxOffset == 0) return 0
        val step = lineHeight.coerceAtLeast(1) * scrollAmount.coerceAtLeast(1)
        return (currentOffset + wheelRotation * step).coerceIn(0, maxOffset)
    }
}
