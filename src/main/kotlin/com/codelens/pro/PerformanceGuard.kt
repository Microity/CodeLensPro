package com.codelens.pro

enum class RenderMode { FULL, SIMPLIFIED, MINIMAL }

object PerformanceGuard {
    fun modeFor(lineCount: Int, settings: CodeLensProSettings): RenderMode = when {
        lineCount > settings.hugeFileLineThreshold -> RenderMode.MINIMAL
        lineCount > settings.largeFileLineThreshold -> RenderMode.SIMPLIFIED
        else -> RenderMode.FULL
    }
}
