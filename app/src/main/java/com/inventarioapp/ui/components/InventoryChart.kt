package com.inventarioapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.inventarioapp.ui.viewmodels.CategoryData
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun InventoryChart(
    categoryData: List<CategoryData>,
    modifier: Modifier = Modifier
) {
    if (categoryData.isEmpty()) {
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

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pie Chart
        Canvas(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterVertically)
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = minOf(size.width, size.height) / 2 - 10.dp.toPx()
            var startAngle = 0f

            val colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.tertiary,
                Color(0xFF4CAF50),
                Color(0xFFFF9800)
            )

            categoryData.forEachIndexed { index, data ->
                val sweepAngle = (data.percentage / 100f) * 360f
                
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )
                
                startAngle += sweepAngle
            }
        }

        // Legend
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoryData.forEachIndexed { index, data ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.tertiary,
                        Color(0xFF4CAF50),
                        Color(0xFFFF9800)
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = colors[index % colors.size],
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                    
                    Column {
                        Text(
                            text = data.category,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${data.count} productos (${data.percentage}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}