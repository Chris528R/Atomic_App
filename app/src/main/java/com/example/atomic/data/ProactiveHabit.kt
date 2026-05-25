package com.example.atomic.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proactive_habits")
data class ProactiveHabit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetPackage: String? = null,
    val triggerHour: Int, // 0-23
    val isPhysical: Boolean
)
