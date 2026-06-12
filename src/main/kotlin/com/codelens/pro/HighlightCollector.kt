package com.codelens.pro

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter

class HighlightCollector {
    fun collect(editor: Editor, settings: CodeLensProSettings, colors: ColorSchemeAdapter): List<HighlightInfo> {
        if (!settings.showMarkupHighlights && !settings.showErrorsAndWarnings) return emptyList()
        val document = editor.document
        val result = ArrayList<HighlightInfo>(minOf(MAX_HIGHLIGHTS, 256))
        for (highlighter in editor.markupModel.allHighlighters) {
            if (result.size >= MAX_HIGHLIGHTS) break
            if (!highlighter.isValid) continue
            if (highlighter.targetArea != HighlighterTargetArea.LINES_IN_RANGE && highlighter.endOffset <= highlighter.startOffset) continue
            val startLine = document.getLineNumber(highlighter.startOffset.coerceIn(0, document.textLength))
            val endLine = document.getLineNumber(highlighter.endOffset.coerceIn(0, document.textLength))
            val attributes = highlighter.textAttributesKey?.let { editor.colorsScheme.getAttributes(it) }
            val color = highlighter.getErrorStripeMarkColor(editor.colorsScheme) ?: attributes?.errorStripeColor ?: attributes?.backgroundColor
            val lane = laneFor(highlighter, settings) ?: continue
            result.add(HighlightInfo(startLine, endLine, color ?: defaultColor(lane, colors), lane))
        }
        return result
    }

    private fun laneFor(highlighter: RangeHighlighter, settings: CodeLensProSettings): HighlightLane? {
        val tooltip = highlighter.errorStripeTooltip?.toString()?.lowercase().orEmpty()
        val color = highlighter.getErrorStripeMarkColor(null)
        if (settings.showErrorsAndWarnings && (tooltip.contains("error") || color != null && color.red > 180 && color.green < 120)) return HighlightLane.ERROR
        if (settings.showErrorsAndWarnings && (tooltip.contains("warning") || color != null && color.red > 180 && color.green > 120)) return HighlightLane.WARNING
        return if (settings.showMarkupHighlights) HighlightLane.MARKUP else null
    }

    private fun defaultColor(lane: HighlightLane, colors: ColorSchemeAdapter) = when (lane) {
        HighlightLane.ERROR -> colors.error
        HighlightLane.WARNING -> colors.warning
        else -> colors.markup
    }

    companion object {
        private const val MAX_HIGHLIGHTS = 2_000
    }
}
