package com.example.atomic.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.atomic.data.BlockedApp
import com.example.atomic.data.DefaultBlockedApps
import com.example.atomic.domain.repository.BlockedAppRepository
import com.example.atomic.util.InstalledAppInfo
import com.example.atomic.util.getInstalledApps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BlockedAppsUiState(
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val blockedCount: Int = 0,
    val isLoading: Boolean = true,
    val searchQuery: String = "",
) {
    val filteredApps: List<InstalledAppInfo>
        get() = if (searchQuery.isBlank()) {
            installedApps
        } else {
            val query = searchQuery.lowercase()
            installedApps.filter {
                it.appName.lowercase().contains(query) ||
                    it.packageName.lowercase().contains(query)
            }
        }
}

class BlockedAppsViewModel(
    private val blockedAppRepository: BlockedAppRepository,
    application: Application,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BlockedAppsUiState())
    val uiState: StateFlow<BlockedAppsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            seedDefaultsIfEmpty()
        }
        viewModelScope.launch {
            blockedAppRepository.getBlockedPackages().collect { blockedPackages ->
                refreshInstalledApps(blockedPackages)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setAppBlocked(app: InstalledAppInfo, blocked: Boolean) {
        viewModelScope.launch {
            if (blocked) {
                blockedAppRepository.addBlockedApp(
                    BlockedApp(
                        packageName = app.packageName,
                        appName = app.appName,
                    ),
                )
            } else {
                blockedAppRepository.removeBlockedApp(app.packageName)
            }
        }
    }

    private suspend fun seedDefaultsIfEmpty() {
        if (blockedAppRepository.count() == 0) {
            DefaultBlockedApps.seed.forEach { blockedAppRepository.addBlockedApp(it) }
        }
    }

    private suspend fun refreshInstalledApps(blockedPackages: List<String>) {
        val apps = withContext(Dispatchers.Default) {
            getInstalledApps(getApplication(), blockedPackages)
                .sortedWith(compareByDescending<InstalledAppInfo> { it.isBlocked }
                    .thenBy { it.appName.lowercase() })
        }
        _uiState.update {
            it.copy(
                installedApps = apps,
                blockedCount = blockedPackages.size,
                isLoading = false,
            )
        }
    }
}
