package com.example.atomic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.atomic.domain.repository.UsageRepository

class UsageViewModelFactory(
    private val usageRepository: UsageRepository,
    private val timeDebtDao: com.example.atomic.data.TimeDebtDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UsageViewModel::class.java)) {
            return UsageViewModel(usageRepository, timeDebtDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
