package com.example.atomic.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_debt")
data class TimeDebt(
    @PrimaryKey val id: Int = 1, // Siempre será la fila 1
    val debtMinutes: Int
)
