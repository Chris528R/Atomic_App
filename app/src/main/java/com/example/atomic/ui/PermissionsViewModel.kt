package com.example.atomic.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.atomic.util.PermissionChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PermissionsUiState(
    val isOverlayGranted: Boolean = false,
    val isAccessibilityGranted: Boolean = false,
    val isBatteryOptimized: Boolean = false,
) {
    val allGranted: Boolean
        get() = isOverlayGranted && isAccessibilityGranted && isBatteryOptimized
}

class PermissionsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PermissionsUiState())
    val uiState: StateFlow<PermissionsUiState> = _uiState.asStateFlow()

    fun refresh() {
        val context = getApplication<Application>()
        _uiState.update {
            PermissionsUiState(
                isOverlayGranted = PermissionChecker.hasOverlayPermission(context),
                isAccessibilityGranted = PermissionChecker.isAccessibilityServiceEnabled(context),
                isBatteryOptimized = PermissionChecker.isIgnoringBatteryOptimizations(context),
            )
        }
    }
}
