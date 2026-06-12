package com.codelens.pro

import com.intellij.openapi.options.Configurable
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class CodeLensProConfigurable : Configurable {
    private val settings: CodeLensProSettings = CodeLensProSettings.getInstance()
    private val checkBoxes = mutableMapOf<String, JCheckBox>()
    private val spinners = mutableMapOf<String, JSpinner>()

    override fun getDisplayName(): String = "CodeLens Pro"

    override fun createComponent(): JComponent {
        val panel = JPanel(GridBagLayout())
        var row = 0

        fun constraints(y: Int, x: Int = 0): GridBagConstraints = GridBagConstraints().apply {
            gridx = x
            gridy = y
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            weightx = if (x == 1) 1.0 else 0.0
        }

        fun section(title: String) {
            panel.add(JLabel("<html><b>$title</b></html>"), constraints(row++))
        }

        fun checkbox(key: String, text: String, value: Boolean) {
            val box = JCheckBox(text, value)
            checkBoxes[key] = box
            panel.add(box, constraints(row++))
        }

        fun spinner(key: String, label: String, value: Int, min: Int, max: Int, step: Int) {
            panel.add(JLabel(label), constraints(row, 0))
            val spinner = JSpinner(SpinnerNumberModel(value, min, max, step))
            spinners[key] = spinner
            panel.add(spinner, constraints(row++, 1))
        }

        section("General")
        checkbox("enabled", "Enable CodeLens Pro", settings.enabled)
        checkbox("autoWidth", "Auto width in split editors", settings.autoWidth)
        checkbox("hideOriginalScrollbar", "Hide original editor scrollbar", settings.hideOriginalScrollbar)
        spinner("width", "Minimap width:", settings.width, CodeLensProSettings.MIN_WIDTH, CodeLensProSettings.MAX_WIDTH, 5)

        section("Rendering")
        checkbox("showViewport", "Show viewport", settings.showViewport)
        checkbox("showCaretLine", "Show caret line", settings.showCaretLine)
        checkbox("showErrorsAndWarnings", "Show errors and warnings", settings.showErrorsAndWarnings)
        checkbox("showMarkupHighlights", "Show markup/search/inspection highlights", settings.showMarkupHighlights)
        checkbox("useEditorColorScheme", "Use editor color scheme", settings.useEditorColorScheme)
        checkbox("useSimplifiedLanguageColors", "Use simplified language colors", settings.useSimplifiedLanguageColors)

        section("Performance")
        spinner("largeFileLineThreshold", "Large file threshold:", settings.largeFileLineThreshold, 500, 100_000, 500)
        spinner("hugeFileLineThreshold", "Huge file threshold:", settings.hugeFileLineThreshold, 1_000, 300_000, 1_000)

        section("Diagnostics")
        checkbox("debugLogs", "Enable debug logs", settings.debugLogs)

        return JPanel(BorderLayout()).apply {
            add(JScrollPane(panel), BorderLayout.CENTER)
        }
    }

    override fun isModified(): Boolean =
        bool("enabled") != settings.enabled ||
            bool("autoWidth") != settings.autoWidth ||
            bool("hideOriginalScrollbar") != settings.hideOriginalScrollbar ||
            int("width") != settings.width ||
            bool("showViewport") != settings.showViewport ||
            bool("showCaretLine") != settings.showCaretLine ||
            bool("showErrorsAndWarnings") != settings.showErrorsAndWarnings ||
            bool("showMarkupHighlights") != settings.showMarkupHighlights ||
            bool("useEditorColorScheme") != settings.useEditorColorScheme ||
            bool("useSimplifiedLanguageColors") != settings.useSimplifiedLanguageColors ||
            int("largeFileLineThreshold") != settings.largeFileLineThreshold ||
            int("hugeFileLineThreshold") != settings.hugeFileLineThreshold ||
            bool("debugLogs") != settings.debugLogs

    override fun apply() {
        settings.enabled = bool("enabled")
        settings.autoWidth = bool("autoWidth")
        settings.hideOriginalScrollbar = bool("hideOriginalScrollbar")
        settings.width = int("width")
        settings.showViewport = bool("showViewport")
        settings.showCaretLine = bool("showCaretLine")
        settings.showErrorsAndWarnings = bool("showErrorsAndWarnings")
        settings.showMarkupHighlights = bool("showMarkupHighlights")
        settings.useEditorColorScheme = bool("useEditorColorScheme")
        settings.useSimplifiedLanguageColors = bool("useSimplifiedLanguageColors")
        settings.largeFileLineThreshold = int("largeFileLineThreshold")
        settings.hugeFileLineThreshold = int("hugeFileLineThreshold")
        settings.debugLogs = bool("debugLogs")
        EditorLensManager.getInstance().refreshAllEditors()
    }

    override fun reset() {
        checkBoxes["enabled"]?.isSelected = settings.enabled
        checkBoxes["autoWidth"]?.isSelected = settings.autoWidth
        checkBoxes["hideOriginalScrollbar"]?.isSelected = settings.hideOriginalScrollbar
        spinners["width"]?.value = settings.width
        checkBoxes["showViewport"]?.isSelected = settings.showViewport
        checkBoxes["showCaretLine"]?.isSelected = settings.showCaretLine
        checkBoxes["showErrorsAndWarnings"]?.isSelected = settings.showErrorsAndWarnings
        checkBoxes["showMarkupHighlights"]?.isSelected = settings.showMarkupHighlights
        checkBoxes["useEditorColorScheme"]?.isSelected = settings.useEditorColorScheme
        checkBoxes["useSimplifiedLanguageColors"]?.isSelected = settings.useSimplifiedLanguageColors
        spinners["largeFileLineThreshold"]?.value = settings.largeFileLineThreshold
        spinners["hugeFileLineThreshold"]?.value = settings.hugeFileLineThreshold
        checkBoxes["debugLogs"]?.isSelected = settings.debugLogs
    }

    override fun disposeUIResources() {
        checkBoxes.clear()
        spinners.clear()
    }

    private fun bool(key: String): Boolean = checkBoxes[key]?.isSelected ?: false
    private fun int(key: String): Int = spinners[key]?.value as? Int ?: 0
}
