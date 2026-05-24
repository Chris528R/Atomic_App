package com.example.atomic.domain

import java.time.DayOfWeek
import java.time.LocalDateTime

import com.example.atomic.data.ScheduleRule

object TimeRuleEngine {
    
    fun isFreeTime(rules: List<ScheduleRule>, now: LocalDateTime = LocalDateTime.now()): Boolean {
        val currentDayName = now.dayOfWeek.name
        val currentMinute = now.hour * 60 + now.minute

        for (rule in rules) {
            if (!rule.isEnabled) continue
            
            // Check if rule applies today
            val activeDays = rule.activeDays.split(",")
            if (!activeDays.contains(currentDayName)) continue

            // Evaluate time bounds
            if (rule.startMinute < rule.endMinute) {
                // Normal schedule: e.g. 08:00 to 20:00
                if (currentMinute in rule.startMinute..rule.endMinute) {
                    return true
                }
            } else {
                // Midnight crossover: e.g. 22:00 to 06:00
                if (currentMinute >= rule.startMinute || currentMinute <= rule.endMinute) {
                    return true
                }
            }
        }
        return false
    }
}
