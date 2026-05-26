package com.example.atomic.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "habit_completions")
data class HabitCompletion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val habitId: Int,
    val dateString: String // Format: YYYY-MM-DD
) {
    companion object {
        fun createForToday(habitId: Int): HabitCompletion {
            return HabitCompletion(
                habitId = habitId,
                dateString = LocalDate.now().toString()
            )
        }
    }
}
