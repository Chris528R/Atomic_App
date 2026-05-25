package com.example.atomic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.atomic.data.AtomicDatabase
import com.example.atomic.data.repository.BlockedAppRepositoryImpl
import com.example.atomic.data.repository.UsageRepositoryImpl
import com.example.atomic.ui.AtomicApp
import com.example.atomic.ui.BlockedAppsViewModel
import com.example.atomic.ui.BlockedAppsViewModelFactory
import com.example.atomic.ui.PermissionsViewModel
import com.example.atomic.ui.UsageViewModel
import com.example.atomic.ui.UsageViewModelFactory
import com.example.atomic.ui.ScheduleSettingsViewModel
import com.example.atomic.ui.ScheduleSettingsViewModelFactory
import com.example.atomic.ui.InsightsViewModel
import com.example.atomic.ui.InsightsViewModelFactory
import com.example.atomic.ui.HabitReplacementViewModel
import com.example.atomic.ui.HabitReplacementViewModelFactory
import com.example.atomic.ui.HabitManagerViewModel
import com.example.atomic.ui.HabitManagerViewModelFactory
import com.example.atomic.data.repository.ProactiveHabitRepositoryImpl
import com.example.atomic.data.repository.ScheduleRuleRepositoryImpl
import com.example.atomic.ui.theme.AtomicTheme
import com.example.atomic.util.PermissionChecker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import com.example.atomic.service.HabitReminderWorker
import java.util.concurrent.TimeUnit
import java.util.Calendar

class MainActivity : ComponentActivity() {

    private val database by lazy { AtomicDatabase.getDatabase(applicationContext) }

    private val permissionsViewModel: PermissionsViewModel by viewModels()
    private val usageViewModel: UsageViewModel by viewModels {
        UsageViewModelFactory(UsageRepositoryImpl(database.usageLogDao()), database.timeDebtDao())
    }
    private val blockedAppsViewModel: BlockedAppsViewModel by viewModels {
        BlockedAppsViewModelFactory(BlockedAppRepositoryImpl(database.blockedAppDao()), application)
    }
    private val scheduleSettingsViewModel: ScheduleSettingsViewModel by viewModels {
        ScheduleSettingsViewModelFactory(ScheduleRuleRepositoryImpl(database.scheduleRuleDao()))
    }
    private val insightsViewModel: InsightsViewModel by viewModels {
        InsightsViewModelFactory(database)
    }
    private val habitReplacementViewModel: HabitReplacementViewModel by viewModels {
        HabitReplacementViewModelFactory(database)
    }
    private val habitManagerViewModel: HabitManagerViewModel by viewModels {
        HabitManagerViewModelFactory(ProactiveHabitRepositoryImpl(database.proactiveHabitDao()), applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        scheduleHabitReminder()

        setContent {
            AtomicTheme {
                val permissionsUiState by permissionsViewModel.uiState.collectAsStateWithLifecycle()

                if (!permissionsUiState.allGranted) {
                    com.example.atomic.ui.OnboardingScreen(
                        uiState = permissionsUiState,
                        context = this,
                        onOnboardingComplete = { }
                    )
                } else {
                    AtomicApp(
                        usageViewModel = usageViewModel,
                        blockedAppsViewModel = blockedAppsViewModel,
                        scheduleSettingsViewModel = scheduleSettingsViewModel,
                        insightsViewModel = insightsViewModel,
                        habitReplacementViewModel = habitReplacementViewModel,
                        habitManagerViewModel = habitManagerViewModel,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        permissionsViewModel.refresh()
    }

    private fun scheduleHabitReminder() {
        // Ejecutamos cada hora para revisar los hábitos programados
        val reminderWorkRequest = PeriodicWorkRequestBuilder<HabitReminderWorker>(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "DynamicHabitReminder",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderWorkRequest
        )
    }
}
