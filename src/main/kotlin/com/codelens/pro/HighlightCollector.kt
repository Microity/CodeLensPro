package com.codelens.pro

import com.intellij.openapi.editor.Editor

class HighlightCollector {
    private val daemonDiagnosticCollector = DaemonDiagnosticCollector()

    fun collect(editor: Editor, settings: CodeLensProSettings, colors: ColorSchemeAdapter): List<HighlightInfo> {
        if (!settings.showErrorsAndWarnings) return emptyList()
        return daemonDiagnosticCollector.collect(editor, settings, colors)
    }
}
