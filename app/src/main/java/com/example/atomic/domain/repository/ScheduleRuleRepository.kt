package com.example.atomic.domain.repository

import com.example.atomic.data.ScheduleRule
import kotlinx.coroutines.flow.Flow

interface ScheduleRuleRepository {
    fun getAllRules(): Flow<List<ScheduleRule>>
    suspend fun insertRule(rule: ScheduleRule)
    suspend fun updateRule(rule: ScheduleRule)
    suspend fun deleteRule(rule: ScheduleRule)
}
