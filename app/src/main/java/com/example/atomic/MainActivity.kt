package com.example.atomic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.atomic.data.AtomicDatabase
import com.example.atomic.ui.AtomicApp
import com.example.atomic.ui.PermissionsViewModel
import com.example.atomic.ui.UsageViewModel
import com.example.atomic.ui.UsageViewModelFactory
import com.example.atomic.ui.theme.AtomicTheme
import com.example.atomic.util.PermissionChecker

class MainActivity : ComponentActivity() {

    private val permissionsViewModel: PermissionsViewModel by viewModels()
    private val usageViewModel: UsageViewModel by viewModels {
        UsageViewModelFactory(AtomicDatabase.getDatabase(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AtomicTheme {
                val permissionsUiState by permissionsViewModel.uiState.collectAsStateWithLifecycle()

                AtomicApp(
                    permissionsUiState = permissionsUiState,
                    usageViewModel = usageViewModel,
                    onOpenOverlaySettings = {
                        PermissionChecker.openOverlaySettings(this)
                    },
                    onOpenAccessibilitySettings = {
                        PermissionChecker.openAccessibilitySettings(this)
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        permissionsViewModel.refresh()
    }
}
