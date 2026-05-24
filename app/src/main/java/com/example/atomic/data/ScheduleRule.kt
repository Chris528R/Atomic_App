package com.example.atomic.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_rules")
data class ScheduleRule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isEnabled: Boolean = true,
    val activeDays: String, // e.g. "MONDAY,TUESDAY"
    val startMinute: Int,
    val endMinute: Int
)
