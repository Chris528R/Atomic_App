package com.example.atomic.data.repository

import com.example.atomic.data.UsageLog
import com.example.atomic.data.UsageLogDao
import com.example.atomic.domain.repository.UsageRepository
import kotlinx.coroutines.flow.Flow

class UsageRepositoryImpl(
    private val dao: UsageLogDao
) : UsageRepository {
    override fun getAllLogs(): Flow<List<UsageLog>> {
        return dao.getAllLogs()
    }

    override suspend fun insertLog(log: UsageLog): Long {
        return dao.insertLog(log)
    }

    override suspend fun updateUsageEnd(logId: Long, closeTime: Long, usage: Long) {
        dao.updateUsageEnd(logId, closeTime, usage)
    }
}
