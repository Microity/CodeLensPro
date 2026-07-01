package com.codelens.pro

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.util.Disposer
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel

class EditorLensManager private constructor() {
    private val panels = mutableMapOf<EditorEx, JPanel>()
    private val lensPanels = mutableMapOf<EditorEx, CodeLensProPanel>()
    private val disposables = mutableMapOf<EditorEx, Disposable>()
    private val scrollbarState = mutableMapOf<EditorEx, ScrollbarState>()

    fun attachExistingEditors() {
        EditorFactory.getInstance().allEditors.forEach { attach(it as? EditorEx ?: return@forEach) }
    }

    fun attach(editor: EditorEx) {
        val settings = CodeLensProSettings.getInstance()
        if (!settings.enabled) return
        if (panels.containsKey(editor)) return
        if (!EditorEligibility.isEligible(editor)) return
        val parent = editor.component as? JComponent ?: return
        if (parent.layout !is BorderLayout) return
        val width = widthFor(editor, settings)
        val lensPanel = CodeLensProPanel(editor, settings, width)
        val wrapper = JPanel(BorderLayout()).apply {
            isOpaque = false
            add(lensPanel, BorderLayout.CENTER)
        }
        parent.add(wrapper, BorderLayout.EAST)
        parent.revalidate()
        parent.repaint()
        panels[editor] = wrapper
        lensPanels[editor] = lensPanel
        applyScrollbarPolicy(editor, settings)
        val disposable = Disposable { detach(editor) }
        disposables[editor] = disposable
        lensPanel.register(disposable)
        log("Attached CodeLens Pro minimap")
    }

    fun detach(editor: EditorEx) {
        restoreScrollbar(editor)
        val wrapper = panels.remove(editor)
        val parent = editor.component as? JComponent
        if (wrapper != null && parent != null) {
            parent.remove(wrapper)
            parent.revalidate()
            parent.repaint()
        }
        lensPanels.remove(editor)
        disposables.remove(editor)
    }

    fun refreshAllEditors() {
        ApplicationManager.getApplication().invokeLater {
            EditorFactory.getInstance().allEditors.forEach { editor ->
                val editorEx = editor as? EditorEx ?: return@forEach
                detach(editorEx)
                attach(editorEx)
            }
        }
    }

    fun repaintAll() {
        lensPanels.values.forEach { it.rebuildAndRepaint() }
    }

    private fun widthFor(editor: EditorEx, settings: CodeLensProSettings): Int {
        if (!settings.autoWidth) return settings.width
        val editorWidth = editor.component.width.takeIf { it > 0 } ?: return settings.width
        return (editorWidth / 12).coerceIn(CodeLensProSettings.MIN_WIDTH, settings.width)
    }

    private fun applyScrollbarPolicy(editor: EditorEx, settings: CodeLensProSettings) {
        val scrollbar = editor.scrollPane.verticalScrollBar
        if (!scrollbarState.containsKey(editor)) {
            scrollbarState[editor] = ScrollbarState(
                visible = scrollbar.isVisible,
                preferredSize = scrollbar.preferredSize,
                minimumSize = scrollbar.minimumSize,
                maximumSize = scrollbar.maximumSize,
            )
        }
        if (settings.hideOriginalScrollbar) {
            editor.setVerticalScrollbarVisible(true)
            val hiddenSize = Dimension(0, 0)
            scrollbar.preferredSize = hiddenSize
            scrollbar.minimumSize = hiddenSize
            scrollbar.maximumSize = hiddenSize
            scrollbar.isOpaque = false
        } else {
            restoreScrollbar(editor, keepState = true)
        }
    }

    private fun restoreScrollbar(editor: EditorEx, keepState: Boolean = false) {
        val oldState = (if (keepState) scrollbarState[editor] else scrollbarState.remove(editor)) ?: return
        val scrollbar = editor.scrollPane.verticalScrollBar
        scrollbar.preferredSize = oldState.preferredSize
        scrollbar.minimumSize = oldState.minimumSize
        scrollbar.maximumSize = oldState.maximumSize
        editor.setVerticalScrollbarVisible(oldState.visible)
    }

    private fun log(message: String) {
        if (CodeLensProSettings.getInstance().debugLogs) LOG.info(message)
    }

    companion object {
        private val LOG = Logger.getInstance(EditorLensManager::class.java)
        private val INSTANCE = EditorLensManager()
        fun getInstance(): EditorLensManager = INSTANCE
    }

    private data class ScrollbarState(
        val visible: Boolean,
        val preferredSize: Dimension,
        val minimumSize: Dimension,
        val maximumSize: Dimension,
    )
}
