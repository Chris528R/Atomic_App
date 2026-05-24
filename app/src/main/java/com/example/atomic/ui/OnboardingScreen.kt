package com.example.atomic.ui

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.atomic.util.PermissionChecker
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    uiState: PermissionsUiState,
    context: Context,
    onOnboardingComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                    
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(10.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            userScrollEnabled = false
        ) { page ->
            when (page) {
                0 -> OnboardingPage(
                    title = "Superposición de Pantalla",
                    description = "Atomic necesita mostrarse por encima de otras aplicaciones para poder bloquearlas con la pantalla de fricción.",
                    isGranted = uiState.isOverlayGranted,
                    onRequest = { PermissionChecker.openOverlaySettings(context) },
                    onNext = { 
                        coroutineScope.launch { pagerState.animateScrollToPage(1) } 
                    }
                )
                1 -> OnboardingPage(
                    title = "Servicio de Accesibilidad",
                    description = "Este permiso es el motor principal. Nos permite saber exactamente en qué milisegundo abres Instagram o Facebook.",
                    isGranted = uiState.isAccessibilityGranted,
                    onRequest = { PermissionChecker.openAccessibilitySettings(context) },
                    onNext = { 
                        coroutineScope.launch { pagerState.animateScrollToPage(2) } 
                    }
                )
                2 -> OnboardingPage(
                    title = "Batería sin Restricciones",
                    description = "Android es agresivo cerrando apps. Si no das este permiso, el sistema apagará a Atomic en un par de horas.",
                    isGranted = uiState.isBatteryOptimized,
                    onRequest = { PermissionChecker.requestIgnoreBatteryOptimizations(context) },
                    onNext = onOnboardingComplete
                )
            }
        }
    }
}

@Composable
fun OnboardingPage(
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(if (isGranted) "✅" else "🔒", style = MaterialTheme.typography.displayLarge)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (isGranted) {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Siguiente", fontWeight = FontWeight.Bold)
            }
        } else {
            FilledTonalButton(
                onClick = onRequest,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Conceder Permiso", fontWeight = FontWeight.Bold)
            }
        }
    }
}
