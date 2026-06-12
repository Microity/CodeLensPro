package com.codelens.pro

object GlyphWeights {
    fun isRenderable(code: Int): Boolean = code == 0 || code > 32

    fun cleanWeights(code: Int, height: Int): FloatArray {
        val weight = if (code in 33..126) 0.8f else 0.4f
        return when (height.coerceAtLeast(1).coerceAtMost(4)) {
            1 -> floatArrayOf(weight * 0.6f)
            2 -> floatArrayOf(weight * 0.3f, weight * 0.6f)
            3 -> floatArrayOf(weight * 0.1f, weight * 0.6f, weight * 0.6f)
            else -> floatArrayOf(0f, weight * 0.6f, weight * 0.6f, weight * 0.6f)
        }
    }
}
