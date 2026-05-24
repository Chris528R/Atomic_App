package com.example.atomic.domain

import com.example.atomic.data.UsageLog
import com.example.atomic.domain.rules.ChronicBoredomRule
import com.example.atomic.domain.rules.NightOwlRule
import com.example.atomic.domain.rules.ReflexActionRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InsightGenerator {

    // Registramos todas las reglas activas
    private val activeRules: List<InsightRule> = listOf(
        NightOwlRule(),
        ReflexActionRule(),
        ChronicBoredomRule()
    )

    suspend fun generateInsights(logs: List<UsageLog>): List<ActionableInsight> = withContext(Dispatchers.Default) {
        val results = mutableListOf<ActionableInsight>()

        // Pasamos los datos por cada regla
        for (rule in activeRules) {
            val insight = rule.evaluate(logs)
            if (insight != null) {
                results.add(insight)
            }
        }

        return@withContext results
    }
}
