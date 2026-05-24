package com.example.atomic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.atomic.domain.repository.ScheduleRuleRepository

class ScheduleSettingsViewModelFactory(
    private val repository: ScheduleRuleRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduleSettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
