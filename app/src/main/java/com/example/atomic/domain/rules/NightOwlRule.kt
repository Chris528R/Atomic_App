package com.example.atomic.domain.rules

import com.example.atomic.data.UsageLog
import com.example.atomic.domain.ActionableInsight
import com.example.atomic.domain.InsightRule
import java.time.Instant
import java.time.ZoneId

class NightOwlRule : InsightRule {
    override fun evaluate(logs: List<UsageLog>): ActionableInsight? {
        if (logs.isEmpty()) return null

        val nightLogs = logs.count { log ->
            val hour = Instant.ofEpochMilli(log.timestamp).atZone(ZoneId.systemDefault()).hour
            hour >= 22 || hour < 5 // De 10 PM a 5 AM
        }

        val percentage = (nightLogs.toFloat() / logs.size) * 100

        return if (percentage > 40f) { // Umbral: 40% de aperturas son de noche
            ActionableInsight(
                title = "El Búho Digital",
                description = "El ${percentage.toInt()}% de tus desbloqueos ocurren de noche. Tu deuda de sueño podría estar costándote caro al día siguiente.",
                iconEmoji = "🦉"
            )
        } else null
    }
}
