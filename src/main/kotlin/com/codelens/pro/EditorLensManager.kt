package com.codelens.pro

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.util.Disposer
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

class EditorLensManager private constructor() {
    private val panels = mutableMapOf<EditorEx, JPanel>()
    private val lensPanels = mutableMapOf<EditorEx, CodeLensProPanel>()
    private val disposables = mutableMapOf<EditorEx, Disposable>()
    private val scrollbarPolicies = mutableMapOf<EditorEx, Int>()

    fun attachExistingEditors() {
        EditorFactory.getInstance().allEditors.forEach { attach(it as? EditorEx ?: return@forEach) }
    }

    fun attach(editor: EditorEx) {
        val settings = CodeLensProSettings.getInstance()
        if (!settings.enabled) return
        if (panels.containsKey(editor)) return
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
        val scrollPane = editor.scrollPane
        if (!scrollbarPolicies.containsKey(editor)) {
            scrollbarPolicies[editor] = scrollPane.verticalScrollBarPolicy
        }
        if (settings.hideOriginalScrollbar) {
            scrollPane.verticalScrollBarPolicy = javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER
        }
    }

    private fun restoreScrollbar(editor: EditorEx) {
        val oldPolicy = scrollbarPolicies.remove(editor) ?: return
        editor.scrollPane.verticalScrollBarPolicy = oldPolicy
    }

    private fun log(message: String) {
        if (CodeLensProSettings.getInstance().debugLogs) LOG.info(message)
    }

    companion object {
        private val LOG = Logger.getInstance(EditorLensManager::class.java)
        private val INSTANCE = EditorLensManager()
        fun getInstance(): EditorLensManager = INSTANCE
    }
}
