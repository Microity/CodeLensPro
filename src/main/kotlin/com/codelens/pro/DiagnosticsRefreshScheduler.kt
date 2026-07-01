package com.codelens.pro

class DiagnosticsRefreshScheduler {
    var pendingRequests: Int = 0
        private set
    private var disposed = false

    fun markupChanged(): Boolean {
        if (disposed) return false
        if (pendingRequests > 0) return false
        pendingRequests = 1
        return true
    }

    fun refreshStarted() {
        pendingRequests = 0
    }

    fun dispose() {
        disposed = true
        pendingRequests = 0
    }
}
