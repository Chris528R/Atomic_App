package com.example.atomic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.atomic.data.AtomicDatabase
import com.example.atomic.data.HabitReplacement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HabitReplacementViewModel(
    private val database: AtomicDatabase
) : ViewModel() {

    private val _replacements = MutableStateFlow<List<HabitReplacement>>(emptyList())
    val replacements: StateFlow<List<HabitReplacement>> = _replacements.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            database.habitReplacementDao().getAllReplacements().collect {
                _replacements.value = it
            }
        }
    }

    fun saveMapping(blockedPkg: String, replacementPkg: String, replacementName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val mapping = HabitReplacement(
                blockedPackageName = blockedPkg,
                replacementPackageName = replacementPkg,
                replacementAppName = replacementName
            )
            database.habitReplacementDao().insertReplacement(mapping)
        }
    }

    fun removeMapping(blockedPkg: String) {
        viewModelScope.launch(Dispatchers.IO) {
            database.habitReplacementDao().deleteReplacement(blockedPkg)
        }
    }
}
