package com.example.atomic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.atomic.data.AtomicDatabase

class InsightsViewModelFactory(
    private val database: AtomicDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InsightsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InsightsViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
