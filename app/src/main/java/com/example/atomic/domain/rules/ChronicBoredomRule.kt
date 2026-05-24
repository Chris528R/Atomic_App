package com.example.atomic.domain.rules

import com.example.atomic.data.UsageLog
import com.example.atomic.domain.ActionableInsight
import com.example.atomic.domain.InsightRule

class ChronicBoredomRule : InsightRule {
    override fun evaluate(logs: List<UsageLog>): ActionableInsight? {
        if (logs.isEmpty()) return null

        val boredLogs = logs.count { it.reason == "Estoy aburrido" }
        val percentage = (boredLogs.toFloat() / logs.size) * 100

        return if (percentage > 35f) { // Umbral: 35% por aburrimiento
            ActionableInsight(
                title = "Tolerancia Cero al Aburrimiento",
                description = "Usas las apps como 'chupón emocional'. Un ${percentage.toInt()}% de tus aperturas son por puro aburrimiento. Intenta sentarte con la incomodidad.",
                iconEmoji = "🧘"
            )
        } else null
    }
}
