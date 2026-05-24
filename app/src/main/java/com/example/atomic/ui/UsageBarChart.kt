package com.example.atomic.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp

data class ChartBarData(
    val label: String,
    val valueMillis: Long,
    val formattedValue: String
)

@OptIn(ExperimentalTextApi::class)
@Composable
fun UsageBarChart(
    data: List<ChartBarData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOf { it.valueMillis }.coerceAtLeast(1L)
    
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
    val valueStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    val barHeight = 24.dp
    val spacing = 20.dp
    val totalHeight = (barHeight + spacing) * data.size

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight)
    ) {
        val widthPx = size.width
        val barHeightPx = barHeight.toPx()
        val spacingPx = spacing.toPx()
        
        val labelColumnWidth = widthPx * 0.30f 
        val barAreaWidth = widthPx - labelColumnWidth

        data.forEachIndexed { index, barData ->
            val yOffset = index * (barHeightPx + spacingPx)

            drawText(
                textMeasurer = textMeasurer,
                text = barData.label,
                style = labelStyle,
                topLeft = Offset(x = 0f, y = yOffset + (barHeightPx / 4))
            )

            val barWidthPx = (barData.valueMillis.toFloat() / maxValue.toFloat()) * barAreaWidth

            drawRoundRect(
                color = trackColor,
                topLeft = Offset(x = labelColumnWidth, y = yOffset),
                size = Size(width = barAreaWidth, height = barHeightPx),
                cornerRadius = CornerRadius(barHeightPx / 2)
            )

            if (barWidthPx > 0) {
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(x = labelColumnWidth, y = yOffset),
                    size = Size(width = barWidthPx, height = barHeightPx),
                    cornerRadius = CornerRadius(barHeightPx / 2)
                )
            }

            drawText(
                textMeasurer = textMeasurer,
                text = barData.formattedValue,
                style = valueStyle,
                topLeft = Offset(x = labelColumnWidth, y = yOffset - 40f) 
            )
        }
    }
}
