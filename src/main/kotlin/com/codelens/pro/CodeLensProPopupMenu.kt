package com.codelens.pro

import com.intellij.openapi.options.ShowSettingsUtil
import java.awt.event.MouseEvent
import javax.swing.JCheckBoxMenuItem
import javax.swing.JMenuItem
import javax.swing.JPopupMenu

class CodeLensProPopupMenu {
    fun show(event: MouseEvent) {
        val settings = CodeLensProSettings.getInstance()
        val menu = JPopupMenu()

        fun check(label: String, selected: Boolean, action: (Boolean) -> Unit) {
            menu.add(JCheckBoxMenuItem(label, selected).apply {
                addActionListener {
                    action(isSelected)
                    EditorLensManager.getInstance().refreshAllEditors()
                }
            })
        }

        check("Enable CodeLens Pro", settings.enabled) { settings.enabled = it }
        check("Hide Original Scrollbar", settings.hideOriginalScrollbar) { settings.hideOriginalScrollbar = it }
        check("Show Caret Line", settings.showCaretLine) { settings.showCaretLine = it }
        check("Show Errors / Warnings", settings.showErrorsAndWarnings) { settings.showErrorsAndWarnings = it }
        check("Show Markup Highlights", settings.showMarkupHighlights) { settings.showMarkupHighlights = it }
        menu.addSeparator()
        menu.add(JMenuItem("Increase Width").apply {
            addActionListener {
                settings.width += 5
                EditorLensManager.getInstance().refreshAllEditors()
            }
        })
        menu.add(JMenuItem("Decrease Width").apply {
            addActionListener {
                settings.width -= 5
                EditorLensManager.getInstance().refreshAllEditors()
            }
        })
        menu.addSeparator()
        menu.add(JMenuItem("Open Settings").apply {
            addActionListener { ShowSettingsUtil.getInstance().showSettingsDialog(null, CodeLensProConfigurable::class.java) }
        })
        menu.show(event.component, event.x, event.y)
    }
}
