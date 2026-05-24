package com.example.atomic.domain

import com.example.atomic.data.UsageLogDao
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

data class WeeklyProgress(
    val appName: String,
    val baselineMinutes: Int, 
    val currentMinutes: Int,  
    val percentChange: Int,   
    val isGoalMet: Boolean    
)

class ProgressAnalyzer(private val dao: UsageLogDao) {

    suspend fun calculateProgress(packageName: String): WeeklyProgress? {
        val now = Instant.now()
        val startOfCurrentWeek = now.minus(7, ChronoUnit.DAYS)
        val startOfBaselineWeek = now.minus(14, ChronoUnit.DAYS)

        val currentUsageMillis = dao.getUsageBetween(
            pkg = packageName,
            startTime = startOfCurrentWeek.toEpochMilli(),
            endTime = now.toEpochMilli()
        ) ?: 0L

        val baselineUsageMillis = dao.getUsageBetween(
            pkg = packageName,
            startTime = startOfBaselineWeek.toEpochMilli(),
            endTime = startOfCurrentWeek.toEpochMilli()
        ) ?: 0L

        if (baselineUsageMillis == 0L) return null

        val baselineMin = (baselineUsageMillis / 60000).toInt()
        val currentMin = (currentUsageMillis / 60000).toInt()

        if (baselineMin == 0) return null

        val change = ((baselineMin.toFloat() - currentMin.toFloat()) / baselineMin.toFloat()) * 100
        val isGoalMet = change >= 15f

        return WeeklyProgress(
            appName = packageName.split(".").last().replaceFirstChar { it.uppercase() },
            baselineMinutes = baselineMin,
            currentMinutes = currentMin,
            percentChange = change.roundToInt(),
            isGoalMet = isGoalMet
        )
    }
}
