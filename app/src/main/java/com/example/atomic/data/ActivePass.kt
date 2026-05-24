package com.example.atomic.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_passes")
data class ActivePass(
    @PrimaryKey val packageName: String,
    val expiryTime: Long
)
