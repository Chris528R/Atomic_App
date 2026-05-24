package com.example.atomic.domain

data class WeakPointPattern(
    val appName: String,
    val dayOfWeek: String, // Ej: "Martes"
    val timeWindow: String, // Ej: "11:00 AM - 12:00 PM"
    val occurrences: Int,
    val severity: PatternSeverity
)

enum class PatternSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}
