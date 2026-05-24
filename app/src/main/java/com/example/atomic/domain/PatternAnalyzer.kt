package com.example.atomic.domain

import com.example.atomic.data.UsageLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId

class PatternAnalyzer {

    // Una "Cubeta" tridimensional: App + Día + Hora
    private data class TimeBucket(val packageName: String, val dayOfWeek: Int, val hourOfDay: Int)

    suspend fun analyzeWeakPoints(logs: List<UsageLog>): List<WeakPointPattern> = withContext(Dispatchers.Default) {
        if (logs.isEmpty()) return@withContext emptyList()

        // 1. Mapear cada log a su respectiva cubeta de tiempo
        val bucketCounts = logs.groupingBy { log ->
            val date = Instant.ofEpochMilli(log.timestamp).atZone(ZoneId.systemDefault())
            TimeBucket(log.packageName, date.dayOfWeek.value, date.hour)
        }.eachCount()

        // 2. Filtrar solo los patrones relevantes (ruido estadístico)
        val significantPatterns = bucketCounts.filter { it.value >= 5 }

        // 3. Transformar las cubetas matemáticas en datos legibles para humanos
        val weakPoints = significantPatterns.map { (bucket, count) ->
            WeakPointPattern(
                appName = formatAppName(bucket.packageName),
                dayOfWeek = getDayName(bucket.dayOfWeek),
                timeWindow = formatTimeWindow(bucket.hourOfDay),
                occurrences = count,
                severity = calculateSeverity(count)
            )
        }

        // 4. Retornar la lista ordenada de más grave a menos grave
        return@withContext weakPoints.sortedByDescending { it.occurrences }
    }

    // --- Helpers Utilitarios ---

    private fun calculateSeverity(occurrences: Int): PatternSeverity {
        return when {
            occurrences >= 20 -> PatternSeverity.CRITICAL
            occurrences >= 12 -> PatternSeverity.HIGH
            occurrences >= 8 -> PatternSeverity.MEDIUM
            else -> PatternSeverity.LOW
        }
    }

    private fun getDayName(dayValue: Int): String {
        val days = arrayOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        return days[dayValue - 1]
    }

    private fun formatTimeWindow(hour: Int): String {
        val amPmStart = if (hour >= 12) "PM" else "AM"
        val displayStart = if (hour % 12 == 0) 12 else hour % 12
        
        val nextHour = (hour + 1) % 24
        val amPmEnd = if (nextHour >= 12) "PM" else "AM"
        val displayEnd = if (nextHour % 12 == 0) 12 else nextHour % 12

        return "$displayStart:00 $amPmStart - $displayEnd:00 $amPmEnd"
    }

    private fun formatAppName(packageName: String): String {
        return packageName.split(".").lastOrNull()?.replaceFirstChar { it.uppercase() } ?: packageName
    }
}
