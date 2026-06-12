package com.codelens.pro

import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.editor.ex.EditorEx

class CodeLensProEditorListener : EditorFactoryListener {
    override fun editorCreated(event: EditorFactoryEvent) {
        EditorLensManager.getInstance().attach(event.editor as? EditorEx ?: return)
    }

    override fun editorReleased(event: EditorFactoryEvent) {
        val editor = event.editor as? EditorEx ?: return
        EditorLensManager.getInstance().detach(editor)
    }
}
