package com.example.atomic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.atomic.data.AtomicDatabase
import com.example.atomic.domain.InsightGenerator
import com.example.atomic.domain.PatternAnalyzer
import com.example.atomic.domain.ProgressAnalyzer
import com.example.atomic.domain.WeakPointPattern
import com.example.atomic.domain.ActionableInsight
import com.example.atomic.domain.WeeklyProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InsightsViewModel(private val database: AtomicDatabase) : ViewModel() {

    private val patternAnalyzer = PatternAnalyzer()
    private val insightGenerator = InsightGenerator()
    private val progressAnalyzer = ProgressAnalyzer(database.usageLogDao())

    private val _weakPoints = MutableStateFlow<List<WeakPointPattern>>(emptyList())
    val weakPoints: StateFlow<List<WeakPointPattern>> = _weakPoints.asStateFlow()

    private val _insights = MutableStateFlow<List<ActionableInsight>>(emptyList())
    val insights: StateFlow<List<ActionableInsight>> = _insights.asStateFlow()

    private val _weeklyProgress = MutableStateFlow<List<WeeklyProgress>>(emptyList())
    val weeklyProgress: StateFlow<List<WeeklyProgress>> = _weeklyProgress.asStateFlow()

    init {
        loadPatterns()
    }

    private fun loadPatterns() {
        viewModelScope.launch {
            val logs = database.usageLogDao().getAllLogsSnapshot()
            _weakPoints.value = patternAnalyzer.analyzeWeakPoints(logs)
            _insights.value = insightGenerator.generateInsights(logs)

            val packages = database.usageLogDao().getDistinctPackages()
            val progressList = packages.mapNotNull { pkg ->
                progressAnalyzer.calculateProgress(pkg)
            }.sortedByDescending { it.percentChange }
            _weeklyProgress.value = progressList
        }
    }
}
