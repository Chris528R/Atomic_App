package com.example.atomic.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCompletionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletion)

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY dateString DESC")
    fun getCompletionsForHabit(habitId: Int): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions")
    fun getAllCompletions(): Flow<List<HabitCompletion>>

    @Query("SELECT COUNT(*) FROM habit_completions WHERE habitId = :habitId AND dateString = :dateString")
    suspend fun isCompleted(habitId: Int, dateString: String): Int
}
