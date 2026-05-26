package com.example.atomic.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.atomic.data.HabitCompletion
import com.example.atomic.data.HabitCompletionDao
import com.example.atomic.data.ProactiveHabit
import com.example.atomic.domain.repository.ProactiveHabitRepository
import com.example.atomic.util.InstalledAppInfo
import com.example.atomic.util.getInstalledApps
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HabitWithProgress(
    val habit: ProactiveHabit,
    val streak: Int,
    val last7Days: List<Boolean> // De Lunes a Domingo, o simplemente últimos 7 días.
)

data class HabitManagerUiState(
    val habits: List<HabitWithProgress> = emptyList(),
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val isLoading: Boolean = false
)

class HabitManagerViewModel(
    private val repository: ProactiveHabitRepository,
    private val completionDao: HabitCompletionDao,
    context: Context
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    
    val uiState: StateFlow<HabitManagerUiState> = combine(
        repository.getAllHabits(),
        completionDao.getAllCompletions(),
        _isLoading
    ) { habits, completions, isLoading ->
        val progressList = habits.map { habit ->
            val habitCompletions = completions.filter { it.habitId == habit.id }.map { it.dateString }.toSet()
            
            // Calculate streak
            var streak = 0
            var currentDay = LocalDate.now()
            
            // If they haven't done it today, we check if they did it yesterday.
            // If they didn't do it today or yesterday, streak is 0.
            if (habitCompletions.contains(currentDay.toString())) {
                streak++
                currentDay = currentDay.minusDays(1)
                while (habitCompletions.contains(currentDay.toString())) {
                    streak++
                    currentDay = currentDay.minusDays(1)
                }
            } else {
                currentDay = currentDay.minusDays(1)
                while (habitCompletions.contains(currentDay.toString())) {
                    streak++
                    currentDay = currentDay.minusDays(1)
                }
            }

            // Calculate last 7 days (index 0 is 6 days ago, index 6 is today)
            val today = LocalDate.now()
            val last7Days = (6 downTo 0).map { offset ->
                habitCompletions.contains(today.minusDays(offset.toLong()).toString())
            }

            HabitWithProgress(habit, streak, last7Days)
        }

        HabitManagerUiState(
            habits = progressList,
            installedApps = getInstalledApps(context, emptyList()),
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitManagerUiState(isLoading = true)
    )

    fun addHabit(name: String, targetPackage: String?, triggerHour: Int, triggerMinute: Int, isPhysical: Boolean) {
        viewModelScope.launch {
            repository.insertHabit(
                ProactiveHabit(
                    name = name,
                    targetPackage = targetPackage,
                    triggerHour = triggerHour,
                    triggerMinute = triggerMinute,
                    isPhysical = isPhysical
                )
            )
        }
    }

    fun deleteHabit(habit: ProactiveHabit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }
}
