package com.example.atomic.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.atomic.domain.repository.BlockedAppRepository

class BlockedAppsViewModelFactory(
    private val blockedAppRepository: BlockedAppRepository,
    private val application: Application,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BlockedAppsViewModel::class.java)) {
            return BlockedAppsViewModel(blockedAppRepository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
