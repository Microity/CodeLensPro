package com.codelens.pro

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.VisibleAreaEvent
import com.intellij.openapi.editor.event.VisibleAreaListener
import com.intellij.openapi.editor.ex.MarkupModelEx
import com.intellij.openapi.editor.ex.RangeHighlighterEx
import com.intellij.openapi.editor.impl.DocumentMarkupModel
import com.intellij.openapi.editor.impl.event.MarkupModelListener
import com.intellij.openapi.editor.ex.FoldingListener
import com.intellij.openapi.editor.ex.FoldingModelEx
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.util.Disposer
import com.intellij.util.messages.MessageBusConnection
import java.awt.Dimension
import java.awt.Cursor
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import javax.swing.JPanel
import javax.swing.Timer
import kotlin.math.max

class CodeLensProPanel(
    private val editor: Editor,
    private val settings: CodeLensProSettings,
    width: Int = settings.width,
) : JPanel(), Disposable {
    private val painter = CodeLensProPainter()
    private val snapshotBuilder = MinimapSnapshotBuilder()
    private val imageRenderer = MinimapImageRenderer()
    private val popupMenu = CodeLensProPopupMenu()
    private var snapshot: MinimapSnapshot = snapshotBuilder.build(editor, settings)
    private var minimapImage: BufferedImage? = null
    private var minimapImageWidth = -1
    private var minimapImageHeight = -1
    private var rebuildTimer: Timer? = null
    private val diagnosticsScheduler = DiagnosticsRefreshScheduler()
    private var diagnosticsRefreshTimer: Timer? = null
    private var daemonConnection: MessageBusConnection? = null
    private var lastDragScrollNanos: Long = 0
    private var lastRepaintNanos: Long = 0
    private var dragViewportAnchorRatio: Double? = null
    private var pressedInsideViewport = false
    private var resizing = false
    private var resizeStartXOnScreen = 0
    private var resizeStartWidth = width
    private var currentResizeWidth = width
    private val documentListener = object : DocumentListener {
        override fun documentChanged(event: DocumentEvent) {
            if (event.document !== editor.document) return
            scheduleRebuild()
        }
    }
    private val caretListener = object : CaretListener {
        override fun caretPositionChanged(event: CaretEvent) {
            snapshot = snapshot.copy(caretLine = editor.caretModel.logicalPosition.line)
            repaintThrottled()
        }
    }
    private val visibleAreaListener = object : VisibleAreaListener {
        override fun visibleAreaChanged(event: VisibleAreaEvent) {
            repaintThrottled()
        }
    }
    private val foldingListener = object : FoldingListener {
        override fun onFoldProcessingEnd() {
            scheduleRebuild()
        }
    }
    private val markupModelListener = object : MarkupModelListener {
        override fun afterAdded(highlighter: RangeHighlighterEx) {
            scheduleDiagnosticsRefresh()
        }

        override fun afterRemoved(highlighter: RangeHighlighterEx) {
            scheduleDiagnosticsRefresh()
        }

        override fun attributesChanged(highlighter: RangeHighlighterEx, renderersChanged: Boolean, fontStyleOrColorChanged: Boolean) {
            scheduleDiagnosticsRefresh()
        }

        override fun attributesChanged(
            highlighter: RangeHighlighterEx,
            renderersChanged: Boolean,
            fontStyleOrColorChanged: Boolean,
            gutterIconRendererChanged: Boolean,
        ) {
            scheduleDiagnosticsRefresh()
        }
    }

    init {
        preferredSize = Dimension(width, 1)
        minimumSize = Dimension(width, 1)
        maximumSize = Dimension(width, Int.MAX_VALUE)
        isOpaque = true

        val mouseHandler = object : MouseAdapter() {
            override fun mousePressed(event: MouseEvent) {
                if (event.isPopupTrigger || event.button == MouseEvent.BUTTON3) {
                    popupMenu.show(event)
                    return
                }
                if (isResizeArea(event.x)) {
                    resizing = true
                    resizeStartXOnScreen = event.xOnScreen
                    resizeStartWidth = this@CodeLensProPanel.width
                    currentResizeWidth = resizeStartWidth
                    return
                }
                pressedInsideViewport = isInsideViewportOnMap(event.y)
                dragViewportAnchorRatio = viewportAnchorRatio(event.y)
                if (!pressedInsideViewport) jumpToEvent(event)
            }

            override fun mouseReleased(event: MouseEvent) {
                if (resizing) {
                    resizing = false
                    settings.width = currentResizeWidth.coerceIn(CodeLensProSettings.MIN_WIDTH, CodeLensProSettings.MAX_WIDTH)
                    clearMinimapImage()
                    revalidate()
                    rebuildAndRepaint()
                }
                dragViewportAnchorRatio = null
                pressedInsideViewport = false
                if (event.isPopupTrigger) popupMenu.show(event)
            }

            override fun mouseDragged(event: MouseEvent) {
                if (resizing) {
                    resizeTo(event.xOnScreen)
                    return
                }
                scrollSmoothlyToEvent(event)
            }

            override fun mouseWheelMoved(event: MouseWheelEvent) {
                scrollByWheel(event)
            }

            override fun mouseMoved(event: MouseEvent) {
                cursor = if (isResizeArea(event.x)) Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR) else Cursor.getDefaultCursor()
            }

            override fun mouseExited(event: MouseEvent) {
                cursor = Cursor.getDefaultCursor()
            }
        }
        addMouseListener(mouseHandler)
        addMouseMotionListener(mouseHandler)
        addMouseWheelListener(mouseHandler)

        EditorFactory.getInstance().eventMulticaster.addDocumentListener(documentListener, this)
        editor.caretModel.addCaretListener(caretListener)
        editor.scrollingModel.addVisibleAreaListener(visibleAreaListener)
        runCatching {
            (editor.foldingModel as? FoldingModelEx)?.addListener(foldingListener, this)
        }
        registerMarkupModelListeners()
        registerDaemonListener()
    }

    override fun paintComponent(graphics: Graphics) {
        super.paintComponent(graphics)
        val layout = currentLayout()
        ensureMinimapImage(layout)
        val g = graphics as Graphics2D
        painter.paint(g, editor, settings, snapshot, Rectangle(0, 0, width, height), layout, minimapImage)
    }

    private fun jumpToEvent(event: MouseEvent) {
        val targetLine = lineForY(event.y)
        val offset = editor.document.getLineStartOffset(targetLine)
        val position = editor.offsetToLogicalPosition(offset)
        editor.scrollingModel.scrollTo(position, ScrollType.CENTER)
        editor.caretModel.moveToLogicalPosition(position)
    }

    private fun scrollSmoothlyToEvent(event: MouseEvent) {
        val now = System.nanoTime()
        if (now - lastDragScrollNanos < DRAG_THROTTLE_NANOS) return
        lastDragScrollNanos = now

        val layout = currentLayout()
        val documentHeight = scrollableDocumentHeight()
        val visibleHeight = editor.scrollingModel.visibleArea.height
        val maxOffset = (documentHeight - visibleHeight).coerceAtLeast(0)
        val viewportHeightOnMap = viewportHeightOnMap(documentHeight, visibleHeight, layout)
        val anchorRatio = dragViewportAnchorRatio ?: viewportAnchorRatio(event.y)
        val targetTopOnMap = event.y - (viewportHeightOnMap * anchorRatio)
        val scrollableMapHeight = (layout.drawHeight - viewportHeightOnMap).coerceAtLeast(1.0)
        val ratio = targetTopOnMap.coerceIn(0.0, scrollableMapHeight) / scrollableMapHeight
        val targetOffset = (ratio * maxOffset).toInt().coerceIn(0, maxOffset)
        editor.scrollingModel.disableAnimation()
        editor.scrollingModel.scrollVertically(targetOffset)
    }

    private fun scrollByWheel(event: MouseWheelEvent) {
        val visibleArea = editor.scrollingModel.visibleArea
        val targetOffset = MinimapWheelScroll.targetOffset(
            currentOffset = visibleArea.y,
            wheelRotation = event.wheelRotation,
            scrollAmount = event.scrollAmount,
            lineHeight = editor.lineHeight,
            visibleHeight = visibleArea.height,
            documentHeight = scrollableDocumentHeight(),
        )
        editor.scrollingModel.disableAnimation()
        editor.scrollingModel.scrollVertically(targetOffset)
        event.consume()
    }

    private fun viewportAnchorRatio(mouseY: Int): Double {
        val layout = currentLayout()
        val documentHeight = scrollableDocumentHeight()
        val visibleArea = editor.scrollingModel.visibleArea
        val viewportHeightOnMap = viewportHeightOnMap(documentHeight, visibleArea.height, layout)
        if (viewportHeightOnMap <= 0.0) return 0.5
        val maxOffset = (documentHeight - visibleArea.height).coerceAtLeast(0)
        val scrollableMapHeight = (layout.drawHeight - viewportHeightOnMap).coerceAtLeast(1.0)
        val viewportTopOnMap = if (maxOffset == 0) {
            0.0
        } else {
            (visibleArea.y.toDouble() / maxOffset.toDouble()) * scrollableMapHeight
        }
        return ((mouseY - viewportTopOnMap) / viewportHeightOnMap).coerceIn(0.0, 1.0)
    }

    private fun viewportHeightOnMap(documentHeight: Int, visibleHeight: Int, layout: MinimapLayout = currentLayout()): Double {
        if (layout.drawHeight <= 0 || documentHeight <= 0) return 0.0
        return max(8.0, (visibleHeight.toDouble() / documentHeight.toDouble()) * layout.drawHeight.toDouble()).coerceAtMost(layout.drawHeight.toDouble())
    }

    private fun isInsideViewportOnMap(mouseY: Int): Boolean {
        val layout = currentLayout()
        val documentHeight = scrollableDocumentHeight()
        val visibleArea = editor.scrollingModel.visibleArea
        val viewportHeightOnMap = viewportHeightOnMap(documentHeight, visibleArea.height, layout)
        val maxOffset = (documentHeight - visibleArea.height).coerceAtLeast(0)
        val scrollableMapHeight = (layout.drawHeight - viewportHeightOnMap).coerceAtLeast(1.0)
        val viewportTopOnMap = if (maxOffset == 0) {
            0.0
        } else {
            (visibleArea.y.toDouble() / maxOffset.toDouble()) * scrollableMapHeight
        }
        return mouseY.toDouble() in viewportTopOnMap..(viewportTopOnMap + viewportHeightOnMap)
    }

    private fun isResizeArea(x: Int): Boolean = x in 0 until RESIZE_HIT_WIDTH

    private fun resizeTo(currentXOnScreen: Int) {
        val delta = resizeStartXOnScreen - currentXOnScreen
        val newWidth = (resizeStartWidth + delta).coerceIn(CodeLensProSettings.MIN_WIDTH, CodeLensProSettings.MAX_WIDTH)
        currentResizeWidth = newWidth
        preferredSize = Dimension(newWidth, 1)
        minimumSize = Dimension(newWidth, 1)
        maximumSize = Dimension(newWidth, Int.MAX_VALUE)
        revalidate()
        clearMinimapImage()
        repaintThrottled()
    }

    override fun dispose() {
        rebuildTimer?.stop()
        diagnosticsRefreshTimer?.stop()
        diagnosticsScheduler.dispose()
        daemonConnection?.disconnect()
        daemonConnection = null
        clearMinimapImage()
        editor.caretModel.removeCaretListener(caretListener)
        editor.scrollingModel.removeVisibleAreaListener(visibleAreaListener)
    }

    fun register(parentDisposable: Disposable) {
        Disposer.register(parentDisposable, this)
    }

    private fun registerMarkupModelListeners() {
        (editor.markupModel as? MarkupModelEx)?.addMarkupModelListener(this, markupModelListener)
        val project = editor.project ?: return
        val documentMarkupModel = runCatching {
            DocumentMarkupModel.forDocument(editor.document, project, false)
        }.getOrNull()
        (documentMarkupModel as? MarkupModelEx)?.addMarkupModelListener(this, markupModelListener)
    }

    private fun registerDaemonListener() {
        val project = editor.project ?: return
        daemonConnection = project.messageBus.connect(this)
        daemonConnection?.subscribe(DaemonCodeAnalyzer.DAEMON_EVENT_TOPIC, object : DaemonCodeAnalyzer.DaemonListener {
            override fun daemonFinished(fileEditors: Collection<FileEditor>) {
                if (fileEditors.isEmpty() || fileEditors.any { fileEditorMatchesCurrentDocument(it) }) {
                    scheduleDiagnosticsRefresh()
                }
            }
        })
    }

    private fun fileEditorMatchesCurrentDocument(fileEditor: FileEditor): Boolean {
        val textEditor = fileEditor as? TextEditor ?: return false
        return textEditor.editor.document === editor.document
    }

    private fun scheduleDiagnosticsRefresh() {
        ApplicationManager.getApplication().invokeLater {
            if (!diagnosticsScheduler.markupChanged()) return@invokeLater
            diagnosticsRefreshTimer?.stop()
            diagnosticsRefreshTimer = Timer(DIAGNOSTICS_REFRESH_DEBOUNCE_MS) {
                diagnosticsScheduler.refreshStarted()
                refreshHighlightsAndRepaint()
            }.apply {
                isRepeats = false
                start()
            }
        }
    }

    private fun refreshHighlightsAndRepaint() {
        if (!settings.showErrorsAndWarnings) {
            if (snapshot.highlights.isNotEmpty()) {
                snapshot = snapshot.copy(highlights = emptyList())
                repaint()
            }
            return
        }
        val colors = ColorSchemeAdapter(editor, settings)
        val highlights = HighlightCollector().collect(editor, settings, colors)
        if (snapshot.highlights != highlights) {
            snapshot = snapshot.copy(highlights = highlights)
            repaint()
        }
    }

    fun rebuildAndRepaint() {
        val currentFoldingStamp = snapshotBuilder.foldingStamp(editor)
        if (snapshot.documentStamp == editor.document.modificationStamp && snapshot.foldingStamp == currentFoldingStamp) {
            refreshHighlightsAndRepaint()
            return
        }
        snapshot = snapshotBuilder.build(editor, settings)
        clearMinimapImage()
        repaint()
    }

    private fun forceRebuildAndRepaint() {
        snapshot = snapshotBuilder.build(editor, settings)
        clearMinimapImage()
        repaint()
    }

    private fun scheduleRebuild() {
        rebuildTimer?.stop()
        rebuildTimer = Timer(REBUILD_DEBOUNCE_MS) {
            forceRebuildAndRepaint()
        }.apply {
            isRepeats = false
            start()
        }
    }

    private fun repaintThrottled() {
        val now = System.nanoTime()
        if (now - lastRepaintNanos < REPAINT_THROTTLE_NANOS) return
        lastRepaintNanos = now
        repaint()
    }

    private fun lineForY(y: Int): Int {
        val lineCount = max(1, snapshot.visualLineCount)
        val visualLine = currentLayout().visualLineForY(y, lineCount)
        return snapshot.documentLineForVisualLine(visualLine)
    }

    private fun scrollableDocumentHeight(): Int {
        val preferredHeight = editor.component.preferredSize?.height ?: 0
        if (preferredHeight > 0) return preferredHeight
        return snapshot.visualLineCount.coerceAtLeast(1) * editor.lineHeight
    }

    private fun ensureMinimapImage(layout: MinimapLayout) {
        if (minimapImage != null && minimapImageWidth == width && minimapImageHeight == height) return
        minimapImage?.flush()
        minimapImage = imageRenderer.render(snapshot, width, layout)
        minimapImageWidth = width
        minimapImageHeight = height
    }

    private fun clearMinimapImage() {
        minimapImage?.flush()
        minimapImage = null
        minimapImageWidth = -1
        minimapImageHeight = -1
    }

    private fun currentLayout(): MinimapLayout = MinimapLayout.compute(snapshot.visualLineCount, height)

    companion object {
        private const val DRAG_THROTTLE_NANOS = 16_000_000L
        private const val REPAINT_THROTTLE_NANOS = 16_000_000L
        private const val REBUILD_DEBOUNCE_MS = 120
        private const val DIAGNOSTICS_REFRESH_DEBOUNCE_MS = 40
        private const val RESIZE_HIT_WIDTH = 6
    }
}
