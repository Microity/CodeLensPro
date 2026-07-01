package com.codelens.pro

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.APP)
@State(name = "CodeLensProSettings", storages = [Storage("CodeLens Pro.xml")])
class CodeLensProSettings : PersistentStateComponent<CodeLensProSettings.State> {
    data class State(
        var enabled: Boolean = true,
        var width: Int = DEFAULT_WIDTH,
        var autoWidth: Boolean = true,
        var hideOriginalScrollbar: Boolean = false,
        var showViewport: Boolean = true,
        var showCaretLine: Boolean = true,
        var showErrorsAndWarnings: Boolean = true,
        var useEditorColorScheme: Boolean = true,
        var useSimplifiedLanguageColors: Boolean = true,
        var largeFileLineThreshold: Int = DEFAULT_LARGE_FILE_LINE_THRESHOLD,
        var hugeFileLineThreshold: Int = DEFAULT_HUGE_FILE_LINE_THRESHOLD,
        var debugLogs: Boolean = false,
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state.copy(
            width = clampWidth(state.width),
            largeFileLineThreshold = clampLargeFileThreshold(state.largeFileLineThreshold),
            hugeFileLineThreshold = clampHugeFileThreshold(state.hugeFileLineThreshold),
        )
    }

    var enabled: Boolean
        get() = state.enabled
        set(value) {
            state.enabled = value
        }

    var width: Int
        get() = state.width
        set(value) {
            state.width = clampWidth(value)
        }

    var autoWidth: Boolean
        get() = state.autoWidth
        set(value) { state.autoWidth = value }

    var hideOriginalScrollbar: Boolean
        get() = state.hideOriginalScrollbar
        set(value) { state.hideOriginalScrollbar = value }

    var showViewport: Boolean
        get() = state.showViewport
        set(value) { state.showViewport = value }

    var showCaretLine: Boolean
        get() = state.showCaretLine
        set(value) { state.showCaretLine = value }

    var showErrorsAndWarnings: Boolean
        get() = state.showErrorsAndWarnings
        set(value) { state.showErrorsAndWarnings = value }

    var useEditorColorScheme: Boolean
        get() = state.useEditorColorScheme
        set(value) { state.useEditorColorScheme = value }

    var useSimplifiedLanguageColors: Boolean
        get() = state.useSimplifiedLanguageColors
        set(value) { state.useSimplifiedLanguageColors = value }

    var largeFileLineThreshold: Int
        get() = state.largeFileLineThreshold
        set(value) { state.largeFileLineThreshold = clampLargeFileThreshold(value) }

    var hugeFileLineThreshold: Int
        get() = state.hugeFileLineThreshold
        set(value) { state.hugeFileLineThreshold = clampHugeFileThreshold(value) }

    var debugLogs: Boolean
        get() = state.debugLogs
        set(value) { state.debugLogs = value }

    companion object {
        const val MIN_WIDTH = 40
        const val MAX_WIDTH = 180
        const val DEFAULT_WIDTH = 90
        const val DEFAULT_LARGE_FILE_LINE_THRESHOLD = 3_000
        const val DEFAULT_HUGE_FILE_LINE_THRESHOLD = 10_000

        fun getInstance(): CodeLensProSettings =
            ApplicationManager.getApplication().getService(CodeLensProSettings::class.java)

        fun clampWidth(value: Int): Int = value.coerceIn(MIN_WIDTH, MAX_WIDTH)
        fun clampLargeFileThreshold(value: Int): Int = value.coerceIn(500, 100_000)
        fun clampHugeFileThreshold(value: Int): Int = value.coerceIn(1_000, 300_000)
    }
}
