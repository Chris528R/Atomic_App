package com.example.atomic.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class HabitManagerUiState(
    val habits: List<ProactiveHabit> = emptyList(),
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val isLoading: Boolean = false
)

class HabitManagerViewModel(
    private val repository: ProactiveHabitRepository,
    context: Context
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    
    val uiState: StateFlow<HabitManagerUiState> = combine(
        repository.getAllHabits(),
        _isLoading
    ) { habits, isLoading ->
        HabitManagerUiState(
            habits = habits,
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
