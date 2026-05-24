package com.example.atomic.domain.repository

import com.example.atomic.data.UsageLog
import kotlinx.coroutines.flow.Flow

interface UsageRepository {
    fun getAllLogs(): Flow<List<UsageLog>>
    suspend fun insertLog(log: UsageLog): Long
    suspend fun updateUsageEnd(logId: Long, closeTime: Long, usage: Long)
}
