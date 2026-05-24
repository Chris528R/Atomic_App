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
}
