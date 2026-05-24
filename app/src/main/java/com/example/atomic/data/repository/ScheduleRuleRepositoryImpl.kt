package com.example.atomic.data.repository

import com.example.atomic.data.ScheduleRule
import com.example.atomic.data.ScheduleRuleDao
import com.example.atomic.domain.repository.ScheduleRuleRepository
import kotlinx.coroutines.flow.Flow

class ScheduleRuleRepositoryImpl(
    private val dao: ScheduleRuleDao
) : ScheduleRuleRepository {
    override fun getAllRules(): Flow<List<ScheduleRule>> {
        return dao.getAllRules()
    }

    override suspend fun insertRule(rule: ScheduleRule) {
        dao.insertRule(rule)
    }

    override suspend fun updateRule(rule: ScheduleRule) {
        dao.updateRule(rule)
    }

    override suspend fun deleteRule(rule: ScheduleRule) {
        dao.deleteRule(rule)
    }
}
