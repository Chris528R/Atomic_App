package com.example.atomic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.atomic.data.UsageLog
import com.example.atomic.domain.repository.UsageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.example.atomic.util.resolveAppDisplayName

class UsageViewModel(private val usageRepository: UsageRepository) : ViewModel() {

    private val _logs = MutableStateFlow<List<UsageLog>>(emptyList())
    val logs: StateFlow<List<UsageLog>> = _logs.asStateFlow()

    val chartData: StateFlow<List<ChartBarData>> = _logs.map { logs ->
        val aggregated = logs.groupBy { it.packageName }
            .mapValues { entry ->
                entry.value.sumOf { it.realUsageMillis ?: 0L }
            }

        aggregated.entries
            .sortedByDescending { it.value }
            .map { (pkg, totalMillis) ->
                val minutes = totalMillis / (1000 * 60)
                ChartBarData(
                    label = resolveAppDisplayName(pkg),
                    valueMillis = totalMillis,
                    formattedValue = "${minutes}m"
                )
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            usageRepository.getAllLogs().collect { logs ->
                _logs.value = logs
            }
        }
    }
}
