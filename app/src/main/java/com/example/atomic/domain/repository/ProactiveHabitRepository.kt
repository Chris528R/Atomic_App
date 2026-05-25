package com.example.atomic.domain.repository

import com.example.atomic.data.ProactiveHabit
import kotlinx.coroutines.flow.Flow

interface ProactiveHabitRepository {
    fun getAllHabits(): Flow<List<ProactiveHabit>>
    suspend fun getAllHabitsList(): List<ProactiveHabit>
    suspend fun insertHabit(habit: ProactiveHabit)
    suspend fun deleteHabit(habit: ProactiveHabit)
    suspend fun getHabitById(id: Int): ProactiveHabit?
}
