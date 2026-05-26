package com.example.atomic.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.res.stringResource
import com.example.atomic.R

private enum class MainTab {
    BlockedApps,
    Stats,
    Insights,
    Habits,
    Schedule,
}

@Composable
fun AtomicApp(
    usageViewModel: UsageViewModel,
    blockedAppsViewModel: BlockedAppsViewModel,
    scheduleSettingsViewModel: ScheduleSettingsViewModel,
    insightsViewModel: InsightsViewModel,
    habitReplacementViewModel: HabitReplacementViewModel,
    habitManagerViewModel: HabitManagerViewModel,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.BlockedApps) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    if (showSettings) {
        SettingsScreen(onBack = { showSettings = false })
        return
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {

                NavigationBarItem(
                    selected = selectedTab == MainTab.BlockedApps,
                    onClick = { selectedTab = MainTab.BlockedApps },
                    icon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_blocked_apps)) },
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.Stats,
                    onClick = { selectedTab = MainTab.Stats },
                    icon = { Icon(Icons.Filled.List, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_stats)) },
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.Insights,
                    onClick = { selectedTab = MainTab.Insights },
                    icon = { Icon(Icons.Filled.Star, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_insights)) },
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.Habits,
                    onClick = { selectedTab = MainTab.Habits },
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
                    label = { Text("Hábitos") },
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.Schedule,
                    onClick = { selectedTab = MainTab.Schedule },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_schedule)) },
                )
            }
        },
    ) { innerPadding ->
        when (selectedTab) {
            MainTab.BlockedApps -> BlockedAppsScreen(
                viewModel = blockedAppsViewModel,
                onSettingsClick = { showSettings = true },
                modifier = Modifier.padding(innerPadding),
            )

            MainTab.Stats -> StatsScreen(
                viewModel = usageViewModel,
                onSettingsClick = { showSettings = true },
                modifier = Modifier.padding(innerPadding),
            )
            
            MainTab.Insights -> InsightsScreen(
                viewModel = insightsViewModel,
                onSettingsClick = { showSettings = true },
                modifier = Modifier.padding(innerPadding),
            )
            
            MainTab.Habits -> {
                val uiState by blockedAppsViewModel.uiState.collectAsState()
                val installedApps = uiState.installedApps
                val blockedApps = installedApps.filter { it.isBlocked }.map { it.packageName }
                HabitsScreen(
                    replacementViewModel = habitReplacementViewModel,
                    managerViewModel = habitManagerViewModel,
                    blockedApps = blockedApps,
                    installedApps = installedApps,
                    onSettingsClick = { showSettings = true },
                    modifier = Modifier.padding(innerPadding),
                )
            }

            MainTab.Schedule -> {
                val rules by scheduleSettingsViewModel.rules.collectAsState()
                Box(modifier = Modifier.padding(innerPadding)) {
                    ScheduleSettingsScreen(
                        rules = rules,
                        onToggleRule = { rule, isEnabled -> scheduleSettingsViewModel.toggleRule(rule, isEnabled) },
                        onAddNewRule = { rule -> scheduleSettingsViewModel.addRule(rule) },
                        onDeleteRule = { rule -> scheduleSettingsViewModel.deleteRule(rule) },
                        onSettingsClick = { showSettings = true }
                    )
                }
            }
        }
    }
}
