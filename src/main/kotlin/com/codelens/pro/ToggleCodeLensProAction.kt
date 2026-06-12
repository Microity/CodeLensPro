package com.codelens.pro

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class ToggleCodeLensProAction : AnAction("Toggle CodeLens Pro") {
    override fun actionPerformed(event: AnActionEvent) {
        val settings = CodeLensProSettings.getInstance()
        settings.enabled = !settings.enabled
        EditorLensManager.getInstance().refreshAllEditors()
    }
}
