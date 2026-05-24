package com.example.atomic.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usage_logs")
data class UsageLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val reason: String,
    val timestamp: Long,
    val durationMinutes: Int,
    val closeTimestamp: Long? = null,
    val realUsageMillis: Long? = null
)
