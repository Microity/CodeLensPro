package com.codelens.pro

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.editor.EditorKind
import com.intellij.openapi.editor.ex.EditorEx

object EditorEligibility {
    data class Context(
        val hasVirtualFile: Boolean,
        val isOneLineMode: Boolean,
        val isViewer: Boolean,
        val isRendererMode: Boolean,
        val isEmbeddedIntoDialogWrapper: Boolean,
        val editorKind: EditorKind,
    )

    fun isEligible(context: Context): Boolean {
        if (context.isOneLineMode) return false
        if (context.isRendererMode) return false
        if (context.isEmbeddedIntoDialogWrapper) return false
        if (context.editorKind == EditorKind.CONSOLE) return true
        if (context.editorKind != EditorKind.MAIN_EDITOR && context.editorKind != EditorKind.DIFF) return false
        return context.hasVirtualFile
    }

    fun isEligible(editor: EditorEx): Boolean = isEligible(
        Context(
            hasVirtualFile = FileDocumentManager.getInstance().getFile(editor.document) != null,
            isOneLineMode = editor.isOneLineMode,
            isViewer = editor.isViewer,
            isRendererMode = editor.isRendererMode,
            isEmbeddedIntoDialogWrapper = editor.isEmbeddedIntoDialogWrapper,
            editorKind = editor.editorKind,
        )
    )
}
