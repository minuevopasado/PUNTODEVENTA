package com.inventarioapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.inventarioapp.ui.viewmodels.SalesData

@Composable
fun SalesChart(
    salesData: List<SalesData>,
    modifier: Modifier = Modifier
) {
    if (salesData.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay datos disponibles",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val width = size.width
            val height = size.height
            val maxValue = salesData.maxOfOrNull { it.amount } ?: 0.0
            val minValue = 0.0
            val valueRange = maxValue - minValue

            if (valueRange > 0) {
                val points = salesData.mapIndexed { index, data ->
                    val x = (index.toFloat() / (salesData.size - 1)) * width
                    val y = height - ((data.amount - minValue) / valueRange * height).toFloat()
                    Offset(x, y)
                }

                // Draw line chart
                val path = Path()
                points.forEachIndexed { index, point ->
                    if (index == 0) {
                        path.moveTo(point.x, point.y)
                    } else {
                        path.lineTo(point.x, point.y)
                    }
                }

                drawPath(
                    path = path,
                    color = MaterialTheme.colorScheme.primary,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Draw points
                points.forEach { point ->
                    drawCircle(
                        color = MaterialTheme.colorScheme.primary,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                }
            }
        }

        // X-axis labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            salesData.forEach { data ->
                Text(
                    text = data.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}