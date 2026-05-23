package com.example.atomic.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageLogDao {
    @Insert
    suspend fun insertLog(log: UsageLog)

    @Query("SELECT * FROM usage_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<UsageLog>>
}
