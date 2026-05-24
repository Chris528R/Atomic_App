package com.example.atomic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.atomic.data.ScheduleRule
import com.example.atomic.domain.repository.ScheduleRuleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScheduleSettingsViewModel(
    private val repository: ScheduleRuleRepository
) : ViewModel() {

    val rules = repository.getAllRules().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun toggleRule(rule: ScheduleRule, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.updateRule(rule.copy(isEnabled = isEnabled))
        }
    }

    fun addRule(rule: ScheduleRule) {
        viewModelScope.launch {
            repository.insertRule(rule)
        }
    }

    fun deleteRule(rule: ScheduleRule) {
        viewModelScope.launch {
            repository.deleteRule(rule)
        }
    }
}
