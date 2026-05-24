package com.example.atomic.domain

import com.example.atomic.data.UsageLog

data class ActionableInsight(
    val title: String,
    val description: String,
    val iconEmoji: String
)

interface InsightRule {
    fun evaluate(logs: List<UsageLog>): ActionableInsight?
}
