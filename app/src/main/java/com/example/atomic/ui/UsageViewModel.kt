package com.example.atomic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.atomic.data.AtomicDatabase
import com.example.atomic.data.UsageLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UsageViewModel(private val database: AtomicDatabase) : ViewModel() {

    private val _logs = MutableStateFlow<List<UsageLog>>(emptyList())
    val logs: StateFlow<List<UsageLog>> = _logs.asStateFlow()

    init {
        viewModelScope.launch {
            database.usageLogDao().getAllLogs().collect { logs ->
                _logs.value = logs
            }
        }
    }
}
