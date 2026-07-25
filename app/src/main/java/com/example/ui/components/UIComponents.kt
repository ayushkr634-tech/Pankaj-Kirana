package com.example.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StarRatingBar(
    rating: Double,
    maxStars: Int = 5,
    onRatingSelected: ((Double) -> Unit)? = null,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        for (i in 1..maxStars) {
            val isFilled = rating >= i
            val isHalf = !isFilled && (rating > (i - 1)) && (rating < i)
            
            IconButton(
                onClick = { onRatingSelected?.invoke(i.toDouble()) },
                enabled = onRatingSelected != null,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (isFilled) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Star $i",
                    tint = if (isFilled || isHalf) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SalesPieChart(
    deliveredCount: Int,
    pendingCount: Int,
    modifier: Modifier = Modifier
) {
    val total = deliveredCount + pendingCount
    if (total == 0) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No Orders Placed", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val deliveredAngle = (deliveredCount.toFloat() / total.toFloat()) * 360f
    val pendingAngle = (pendingCount.toFloat() / total.toFloat()) * 360f

    val colorDelivered = MaterialTheme.colorScheme.primary
    val colorPending = MaterialTheme.colorScheme.secondary

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Canvas(modifier = Modifier.size(100.dp)) {
            drawArc(
                color = colorDelivered,
                startAngle = -90f,
                sweepAngle = deliveredAngle,
                useCenter = true
            )
            drawArc(
                color = colorPending,
                startAngle = -90f + deliveredAngle,
                sweepAngle = pendingAngle,
                useCenter = true
            )
        }

        Column(verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(14.dp).background(colorDelivered, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delivered ($deliveredCount)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(14.dp).background(colorPending, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pending ($pendingCount)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SalesLineChart(
    weeklySales: List<Double>,
    modifier: Modifier = Modifier
) {
    if (weeklySales.isEmpty()) return
    val maxVal = weeklySales.maxOrNull()?.coerceAtLeast(100.0) ?: 100.0
    val linePrimaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Weekly Revenue Trend", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                val width = size.width
                val height = size.height
                val stepX = width / (weeklySales.size - 1).coerceAtLeast(1)
                
                val points = weeklySales.mapIndexed { index, value ->
                    val x = index * stepX
                    val y = height - ((value / maxVal) * (height - 20)).toFloat() - 10f
                    Offset(x, y)
                }

                // Draw helper lines
                for (i in 1..3) {
                    val yLine = height * (i / 4f)
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.4f),
                        start = Offset(0f, yLine),
                        end = Offset(width, yLine),
                        strokeWidth = 1f
                    )
                }

                // Draw line path
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = linePrimaryColor,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                }

                // Draw point circles
                points.forEach { point ->
                    drawCircle(
                        color = linePrimaryColor,
                        radius = 5f,
                        center = point
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                days.take(weeklySales.size).forEach { day ->
                    Text(day, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        }
    }
}
