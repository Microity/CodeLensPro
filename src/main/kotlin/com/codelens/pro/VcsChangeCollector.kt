package com.codelens.pro

import com.intellij.openapi.editor.Editor

class VcsChangeCollector {
    fun collect(editor: Editor, settings: CodeLensProSettings, colors: ColorSchemeAdapter): List<HighlightInfo> {
        if (!settings.showVcsChanges) return emptyList()
        // IntelliJ line status APIs are not stable across EAP builds. Keep a safe fallback
        // so CodeLens Pro remains installable. VCS rendering can be upgraded with a verified
        // public API for the exact target platform.
        return emptyList()
    }
}
