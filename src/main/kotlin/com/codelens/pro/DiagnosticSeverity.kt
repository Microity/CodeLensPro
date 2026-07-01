package com.codelens.pro

enum class DiagnosticSeverity { ERROR, WARNING, OTHER }

fun DiagnosticSeverity.toHighlightLane(): HighlightLane? = when (this) {
    DiagnosticSeverity.ERROR -> HighlightLane.ERROR
    DiagnosticSeverity.WARNING -> HighlightLane.WARNING
    DiagnosticSeverity.OTHER -> null
}
