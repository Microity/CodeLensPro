package com.codelens.pro

import com.intellij.openapi.editor.EditorKind
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EditorEligibilityTest {
    @Test
    fun `project file editor is eligible`() {
        val context = EditorEligibility.Context(
            hasVirtualFile = true,
            isOneLineMode = false,
            isViewer = false,
            isRendererMode = false,
            isEmbeddedIntoDialogWrapper = false,
            editorKind = EditorKind.MAIN_EDITOR,
        )

        assertTrue(EditorEligibility.isEligible(context))
    }

    @Test
    fun `diff file editor is eligible`() {
        val context = EditorEligibility.Context(
            hasVirtualFile = true,
            isOneLineMode = false,
            isViewer = true,
            isRendererMode = false,
            isEmbeddedIntoDialogWrapper = false,
            editorKind = EditorKind.DIFF,
        )

        assertTrue(EditorEligibility.isEligible(context))
    }

    @Test
    fun `one line editor backed input is not eligible`() {
        val context = EditorEligibility.Context(
            hasVirtualFile = false,
            isOneLineMode = true,
            isViewer = false,
            isRendererMode = false,
            isEmbeddedIntoDialogWrapper = false,
            editorKind = EditorKind.UNTYPED,
        )

        assertFalse(EditorEligibility.isEligible(context))
    }

    @Test
    fun `editor without virtual file is not eligible`() {
        val context = EditorEligibility.Context(
            hasVirtualFile = false,
            isOneLineMode = false,
            isViewer = false,
            isRendererMode = false,
            isEmbeddedIntoDialogWrapper = false,
            editorKind = EditorKind.UNTYPED,
        )

        assertFalse(EditorEligibility.isEligible(context))
    }

    @Test
    fun `renderer editor is not eligible`() {
        val context = EditorEligibility.Context(
            hasVirtualFile = true,
            isOneLineMode = false,
            isViewer = false,
            isRendererMode = true,
            isEmbeddedIntoDialogWrapper = false,
            editorKind = EditorKind.MAIN_EDITOR,
        )

        assertFalse(EditorEligibility.isEligible(context))
    }

    @Test
    fun `dialog backed editor is not eligible even when it has a virtual file`() {
        val context = EditorEligibility.Context(
            hasVirtualFile = true,
            isOneLineMode = false,
            isViewer = false,
            isRendererMode = false,
            isEmbeddedIntoDialogWrapper = true,
            editorKind = EditorKind.MAIN_EDITOR,
        )

        assertFalse(EditorEligibility.isEligible(context))
    }

    @Test
    fun `untyped editor is not eligible even when it has a virtual file`() {
        val context = EditorEligibility.Context(
            hasVirtualFile = true,
            isOneLineMode = false,
            isViewer = false,
            isRendererMode = false,
            isEmbeddedIntoDialogWrapper = false,
            editorKind = EditorKind.UNTYPED,
        )

        assertFalse(EditorEligibility.isEligible(context))
    }

    @Test
    fun `console editor is eligible even without a virtual file`() {
        val context = EditorEligibility.Context(
            hasVirtualFile = false,
            isOneLineMode = false,
            isViewer = false,
            isRendererMode = false,
            isEmbeddedIntoDialogWrapper = false,
            editorKind = EditorKind.CONSOLE,
        )

        assertTrue(EditorEligibility.isEligible(context))
    }

    @Test
    fun `dialog backed console editor is not eligible`() {
        val context = EditorEligibility.Context(
            hasVirtualFile = false,
            isOneLineMode = false,
            isViewer = false,
            isRendererMode = false,
            isEmbeddedIntoDialogWrapper = true,
            editorKind = EditorKind.CONSOLE,
        )

        assertFalse(EditorEligibility.isEligible(context))
    }
}
