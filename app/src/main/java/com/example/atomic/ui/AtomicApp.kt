package com.example.atomic.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.atomic.R

private enum class MainTab {
    BlockedApps,
    Stats,
}

@Composable
fun AtomicApp(
    usageViewModel: UsageViewModel,
    blockedAppsViewModel: BlockedAppsViewModel,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.BlockedApps) }

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
            }
        },
    ) { innerPadding ->
        when (selectedTab) {
            MainTab.BlockedApps -> BlockedAppsScreen(
                viewModel = blockedAppsViewModel,
                modifier = Modifier.padding(innerPadding),
            )

            MainTab.Stats -> StatsScreen(
                viewModel = usageViewModel,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}
