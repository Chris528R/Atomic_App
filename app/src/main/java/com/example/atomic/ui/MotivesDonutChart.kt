package com.example.atomic.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.atomic.data.UsageLog

data class MotiveSliceData(
    val reason: String,
    val count: Int,
    val percentage: Float,
    val sweepAngle: Float,
    val color: Color
)

fun prepareDonutData(logs: List<UsageLog>, colors: List<Color>): List<MotiveSliceData> {
    if (logs.isEmpty()) return emptyList()

    val totalLogs = logs.size.toFloat()
    
    val grouped = logs.groupingBy { it.reason }.eachCount()
    
    return grouped.entries
        .sortedByDescending { it.value }
        .mapIndexed { index, entry ->
            val percentage = (entry.value / totalLogs) * 100f
            val sweepAngle = (entry.value / totalLogs) * 360f
            
            MotiveSliceData(
                reason = entry.key,
                count = entry.value,
                percentage = percentage,
                sweepAngle = sweepAngle,
                color = colors[index % colors.size] 
            )
        }
}

@Composable
fun MotivesDonutChart(
    data: List<MotiveSliceData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val strokeWidth = 40.dp 

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidthPx = strokeWidth.toPx()
                val innerRadius = (size.minDimension - strokeWidthPx) / 2
                val halfSize = size / 2.0f
                
                val topLeft = Offset(
                    halfSize.width - innerRadius,
                    halfSize.height - innerRadius
                )
                val arcSize = Size(innerRadius * 2, innerRadius * 2)

                var currentStartAngle = -90f 

                data.forEach { slice ->
                    drawArc(
                        color = slice.color,
                        startAngle = currentStartAngle,
                        sweepAngle = slice.sweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
                    )
                    currentStartAngle += slice.sweepAngle
                }
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${data.sumOf { it.count }}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Aperturas",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            data.forEach { slice ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(modifier = Modifier.size(12.dp)) {
                        drawRect(color = slice.color)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = slice.reason,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${String.format("%.1f", slice.percentage)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}
