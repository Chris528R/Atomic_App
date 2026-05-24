package com.example.atomic.domain.rules

import com.example.atomic.data.UsageLog
import com.example.atomic.domain.ActionableInsight
import com.example.atomic.domain.InsightRule

class ReflexActionRule : InsightRule {
    override fun evaluate(logs: List<UsageLog>): ActionableInsight? {
        val validLogs = logs.filter { it.realUsageMillis != null }
        if (validLogs.isEmpty()) return null

        val shortSessions = validLogs.count { it.realUsageMillis!! < 60_000 } // Menos de 1 minuto
        val percentage = (shortSessions.toFloat() / validLogs.size) * 100

        return if (percentage > 50f) { // Umbral: Más de la mitad duran nada
            ActionableInsight(
                title = "Aperturas Fantasma",
                description = "El ${percentage.toInt()}% de tus sesiones duran menos de 1 minuto. Son actos reflejos, no decisiones conscientes.",
                iconEmoji = "👻"
            )
        } else null
    }
}
