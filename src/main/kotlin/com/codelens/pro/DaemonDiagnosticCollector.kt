package com.codelens.pro

import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerEx
import com.intellij.codeInsight.daemon.impl.HighlightInfo as DaemonHighlightInfo
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Editor

class DaemonDiagnosticCollector {
    fun collect(editor: Editor, settings: CodeLensProSettings, colors: ColorSchemeAdapter): List<HighlightInfo> {
        if (!settings.showErrorsAndWarnings) return emptyList()
        val project = editor.project ?: return emptyList()
        val document = editor.document
        return ReadAction.compute<List<HighlightInfo>, RuntimeException> {
            val result = ArrayList<HighlightInfo>(256)
            val textLength = document.textLength
            DaemonCodeAnalyzerEx.processHighlights(
                document,
                project,
                HighlightSeverity.WARNING,
                0,
                textLength,
            ) { info ->
                if (result.size >= MAX_DIAGNOSTICS) return@processHighlights false
                val lane = severityOf(info).toHighlightLane() ?: return@processHighlights true
                val lineRange = HighlightLineRange.fromOffsets(
                    startOffset = info.startOffset,
                    endOffset = info.endOffset,
                    textLength = textLength,
                    lineForOffset = document::getLineNumber,
                )
                val color = info.highlighter?.getErrorStripeMarkColor(editor.colorsScheme) ?: defaultColor(lane, colors)
                result.add(HighlightInfo(lineRange.startLine, lineRange.endLine, color, lane))
                true
            }
            result
        }
    }

    private fun severityOf(info: DaemonHighlightInfo): DiagnosticSeverity = when (info.severity) {
        HighlightSeverity.ERROR -> DiagnosticSeverity.ERROR
        HighlightSeverity.WARNING -> DiagnosticSeverity.WARNING
        else -> DiagnosticSeverity.OTHER
    }

    private fun defaultColor(lane: HighlightLane, colors: ColorSchemeAdapter) = when (lane) {
        HighlightLane.ERROR -> colors.error
        HighlightLane.WARNING -> colors.warning
    }

    companion object {
        private const val MAX_DIAGNOSTICS = 2_000
    }
}
