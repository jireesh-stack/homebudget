package com.homebudget.monthly.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

data class ChartSegment(val label: String, val value: Float, val color: Color)

// ---- Donut / Pie Chart ----
@Composable
fun DonutChart(
    segments: List<ChartSegment>,
    centerLabel: String = "",
    centerSubLabel: String = "",
    modifier: Modifier = Modifier
) {
    var animProgress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animProgress,
        animationSpec = tween(durationMillis = 1000),
        label = "donut"
    )
    LaunchedEffect(segments) { animProgress = 1f }

    val total = segments.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(180.dp)) {
                val strokeWidth = 40f
                val radius = size.minDimension / 2f - strokeWidth / 2f
                var startAngle = -90f
                segments.forEach { seg ->
                    val sweep = (seg.value / total) * 360f * animatedProgress
                    drawArc(
                        color = seg.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )
                    startAngle += sweep + 2f
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (centerLabel.isNotEmpty()) {
                    Text(
                        centerLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (centerSubLabel.isNotEmpty()) {
                    Text(
                        centerSubLabel,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(Modifier.width(16.dp))

        // Legend
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            segments.take(6).forEach { seg ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(seg.color)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            seg.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val pct = if (total > 0) (seg.value / total * 100).toInt() else 0
                        Text(
                            "$pct%",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

// ---- Bar Chart ----
@Composable
fun BarChart(
    bars: List<Pair<String, Float>>,
    barColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val maxVal = bars.maxOfOrNull { it.second }?.coerceAtLeast(1f) ?: 1f
    var animProgress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animProgress,
        animationSpec = tween(durationMillis = 800),
        label = "bar"
    )
    LaunchedEffect(bars) { animProgress = 1f }

    Row(
        modifier = modifier.height(140.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        bars.forEach { (label, value) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(((value / maxVal) * 100 * animatedProgress).dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(barColor)
                )
                Spacer(Modifier.height(4.dp))
                Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}

// ---- Line Chart ----
@Composable
fun LineChart(
    points: List<Pair<String, Float>>,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    if (points.size < 2) return
    val maxVal = points.maxOfOrNull { it.second }?.coerceAtLeast(1f) ?: 1f
    var animProgress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animProgress,
        animationSpec = tween(durationMillis = 1000),
        label = "line"
    )
    LaunchedEffect(points) { animProgress = 1f }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val step = w / (points.size - 1).coerceAtLeast(1)
        val pts = points.mapIndexed { i, (_, v) ->
            Offset(i * step, h - (v / maxVal) * h)
        }
        val visibleCount = (pts.size * animatedProgress).toInt().coerceAtLeast(1)
        for (i in 0 until (visibleCount - 1).coerceAtLeast(0)) {
            drawLine(
                color = lineColor,
                start = pts[i],
                end = pts[i + 1],
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
        }
        pts.take(visibleCount).forEach { pt ->
            drawCircle(color = lineColor, radius = 6f, center = pt)
            drawCircle(color = Color.White, radius = 3f, center = pt)
        }
    }
}

// ---- Progress Bar ----
@Composable
fun BudgetProgressBar(
    label: String,
    spent: Double,
    budget: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f
    val animProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800),
        label = "progress"
    )
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = if (progress > 0.9f) Color(0xFFE74C3C) else color
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}
