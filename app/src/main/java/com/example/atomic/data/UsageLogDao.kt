package com.example.atomic.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageLogDao {
    @Insert
    suspend fun insertLog(log: UsageLog): Long

    @Query("UPDATE usage_logs SET closeTimestamp = :closeTime, realUsageMillis = :usage WHERE id = :logId")
    suspend fun updateUsageEnd(logId: Long, closeTime: Long, usage: Long)

    @Query("SELECT * FROM usage_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<UsageLog>>

    @Query("SELECT * FROM usage_logs ORDER BY timestamp DESC")
    suspend fun getAllLogsSnapshot(): List<UsageLog>

    @Query("SELECT COUNT(*) FROM usage_logs WHERE packageName = :pkg AND timestamp >= :startOfDay")
    suspend fun getTodayOpenCount(pkg: String, startOfDay: Long): Int

    @Query("""
        SELECT SUM(realUsageMillis) 
        FROM usage_logs 
        WHERE packageName = :pkg 
        AND timestamp BETWEEN :startTime AND :endTime
    """)
    suspend fun getUsageBetween(pkg: String, startTime: Long, endTime: Long): Long?

    @Query("SELECT DISTINCT packageName FROM usage_logs")
    suspend fun getDistinctPackages(): List<String>
}
