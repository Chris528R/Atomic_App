package com.example.atomic.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import com.example.atomic.ui.components.AtomicTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.atomic.domain.PatternSeverity
import com.example.atomic.domain.WeakPointPattern
import com.example.atomic.domain.ActionableInsight
import com.example.atomic.domain.WeeklyProgress
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weakPoints by viewModel.weakPoints.collectAsState()
    val insights by viewModel.insights.collectAsState()
    val weeklyProgress by viewModel.weeklyProgress.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            AtomicTopAppBar(
                title = "Inteligencia y Patrones",
                onSettingsClick = onSettingsClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Atomic detecta automáticamente las concentraciones de uso inusuales.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (weakPoints.isEmpty() && insights.isEmpty() && weeklyProgress.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se detectaron patrones de riesgo aún. ¡Buen trabajo!")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (insights.isNotEmpty()) {
                        item {
                            Text(
                                text = "💡 Recomendaciones",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(insights) { insight ->
                            ActionableInsightCard(insight = insight)
                        }
                    }

                    if (weakPoints.isNotEmpty()) {
                        item {
                            if (insights.isNotEmpty()) Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "🔥 Zonas de Riesgo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(weakPoints) { pattern ->
                            WeakPointCard(pattern = pattern)
                        }
                    }

                    if (weeklyProgress.isNotEmpty()) {
                        item {
                            if (insights.isNotEmpty() || weakPoints.isNotEmpty()) Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "🏆 Metas y Progreso Semanal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(weeklyProgress) { progress ->
                            ProgressCard(progress = progress)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeakPointCard(pattern: WeakPointPattern) {
    val containerColor = when (pattern.severity) {
        PatternSeverity.CRITICAL -> MaterialTheme.colorScheme.errorContainer
        PatternSeverity.HIGH -> MaterialTheme.colorScheme.tertiaryContainer
        PatternSeverity.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
        PatternSeverity.LOW -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🚨 Riesgo en ${pattern.appName}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Detectamos una alta concentración de aperturas los ${pattern.dayOfWeek} entre las ${pattern.timeWindow}.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${pattern.occurrences} veces registradas",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ActionableInsightCard(insight: ActionableInsight) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = insight.iconEmoji,
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = insight.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun ProgressCard(progress: WeeklyProgress) {
    val isPositive = progress.percentChange >= 0
    val color = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val sign = if (isPositive) "↓" else "↑"
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Progreso: ${progress.appName}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Semana pasada: ${progress.baselineMinutes} min", style = MaterialTheme.typography.bodySmall)
                Text("Esta semana: ${progress.currentMinutes} min", style = MaterialTheme.typography.bodySmall)
                
                if(progress.isGoalMet) {
                    Text("¡Lograste tu meta del 15%!", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                }
            }
            
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$sign${kotlin.math.abs(progress.percentChange)}%",
                    color = color,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
