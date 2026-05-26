package com.example.atomic.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.atomic.ui.components.AtomicTopAppBar
import com.example.atomic.util.InstalledAppInfo

@Composable
fun HabitsScreen(
    replacementViewModel: HabitReplacementViewModel,
    managerViewModel: HabitManagerViewModel,
    blockedApps: List<String>,
    installedApps: List<InstalledAppInfo>,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Sustitución", "Recordatorios")

    Scaffold(
        modifier = modifier,
        topBar = { AtomicTopAppBar(title = "Hábitos", onSettingsClick = onSettingsClick) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTabIndex) {
                    0 -> HabitReplacementScreen(
                        viewModel = replacementViewModel,
                        blockedApps = blockedApps,
                        installedApps = installedApps
                    )
                    1 -> HabitManagerScreen(
                        viewModel = managerViewModel
                    )
                }
            }
        }
    }
}
