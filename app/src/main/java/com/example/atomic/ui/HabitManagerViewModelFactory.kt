package com.example.atomic.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.atomic.domain.repository.ProactiveHabitRepository

class HabitManagerViewModelFactory(
    private val repository: ProactiveHabitRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitManagerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitManagerViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
