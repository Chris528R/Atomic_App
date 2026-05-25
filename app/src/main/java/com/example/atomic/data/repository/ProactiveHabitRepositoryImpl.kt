package com.example.atomic.data.repository

import com.example.atomic.data.ProactiveHabit
import com.example.atomic.data.ProactiveHabitDao
import com.example.atomic.domain.repository.ProactiveHabitRepository
import kotlinx.coroutines.flow.Flow

class ProactiveHabitRepositoryImpl(private val proactiveHabitDao: ProactiveHabitDao) : ProactiveHabitRepository {
    override fun getAllHabits(): Flow<List<ProactiveHabit>> = proactiveHabitDao.getAllHabits()
    
    override suspend fun getAllHabitsList(): List<ProactiveHabit> = proactiveHabitDao.getAllHabitsList()

    override suspend fun insertHabit(habit: ProactiveHabit) {
        proactiveHabitDao.insertHabit(habit)
    }

    override suspend fun deleteHabit(habit: ProactiveHabit) {
        proactiveHabitDao.deleteHabit(habit)
    }

    override suspend fun getHabitById(id: Int): ProactiveHabit? = proactiveHabitDao.getHabitById(id)
}
