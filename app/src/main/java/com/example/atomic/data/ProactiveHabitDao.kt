package com.example.atomic.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProactiveHabitDao {
    @Query("SELECT * FROM proactive_habits")
    fun getAllHabits(): Flow<List<ProactiveHabit>>

    @Query("SELECT * FROM proactive_habits")
    suspend fun getAllHabitsList(): List<ProactiveHabit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: ProactiveHabit)

    @Delete
    suspend fun deleteHabit(habit: ProactiveHabit)

    @Query("SELECT * FROM proactive_habits WHERE id = :id")
    suspend fun getHabitById(id: Int): ProactiveHabit?
}
