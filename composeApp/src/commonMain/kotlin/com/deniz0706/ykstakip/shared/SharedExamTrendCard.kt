package com.deniz0706.ykstakip.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Cross-platform evidence card for the latest exam net and recent trajectory.
 * All values come from the host repository; this component never invents sample data.
 */
@Composable
fun SharedExamTrendCard(
    examType: String,
    latestNet: Double?,
    change: Double?,
    goal: Float?,
    goalProgressLabel: String?,
    trend: List<Double>,
    trendCount: Int,
    showTarget: Boolean,
    onShowTargetChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.55f))
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Son $examType neti", style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
                    Spacer(Modifier.height(5.dp))
                    Text(
                        latestNet?.let(::formatNet) ?: "—",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Text(
                        when {
                            change == null -> "Karşılaştırma için ikinci deneme gerekli"
                            change >= 0.0 -> "+${formatNet(change)} net"
                            else -> "${formatNet(change)} net"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = when {
                            change == null -> colors.onSurfaceVariant
                            change >= 0.0 -> colors.primary
                            else -> colors.error
                        }
                    )
                }
                if (goal != null && goal > 0f && latestNet != null && goalProgressLabel != null) {
                    GoalRing(
                        progress = (latestNet / goal.toDouble()).toFloat(),
                        label = goalProgressLabel,
                        caption = "hedef"
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = colors.outline.copy(alpha = 0.35f))
            Spacer(Modifier.height(14.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Net trendi", style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                Text("$trendCount kayıt", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            }
            Spacer(Modifier.height(10.dp))

            if (trend.isEmpty()) {
                Text(
                    "Trend burada, ilk denemeni eklediğinde oluşacak.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 22.dp)
                )
            } else {
                TrendLine(
                    values = trend,
                    target = goal?.toDouble(),
                    showTarget = showTarget,
                    modifier = Modifier.fillMaxWidth().height(112.dp)
                )
            }

            if (goal != null && goal > 0f) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hedef çizgisini göster", color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Switch(checked = showTarget, onCheckedChange = onShowTargetChange)
                }
            }
        }
    }
}

@Composable
private fun GoalRing(progress: Float, label: String, caption: String) {
    val colors = MaterialTheme.colorScheme
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(76.dp)) {
        Canvas(Modifier.size(76.dp)) {
            val stroke = 7.dp.toPx()
            drawArc(
                color = colors.outline.copy(alpha = 0.35f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = colors.primary,
                startAngle = -90f,
                sweepAngle = progress.coerceIn(0f, 1f) * 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = colors.onSurface)
            Text(caption, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun TrendLine(
    values: List<Double>,
    target: Double?,
    showTarget: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Canvas(modifier) {
        val finite = values.filter { it.isFinite() }
        if (finite.isEmpty()) return@Canvas
        val minValue = minOf(finite.minOrNull() ?: 0.0, if (showTarget) target ?: Double.POSITIVE_INFINITY else Double.POSITIVE_INFINITY)
        val maxValue = maxOf(finite.maxOrNull() ?: 1.0, if (showTarget) target ?: Double.NEGATIVE_INFINITY else Double.NEGATIVE_INFINITY)
        val padding = ((maxValue - minValue).coerceAtLeast(1.0)) * 0.12
        val low = minValue - padding
        val high = maxValue + padding
        val range = (high - low).coerceAtLeast(1.0)
        fun point(index: Int, value: Double): Offset = Offset(
            x = if (finite.size == 1) size.width / 2f else index * size.width / (finite.size - 1),
            y = size.height - (((value - low) / range).toFloat() * size.height)
        )
        if (showTarget && target != null) {
            val y = size.height - (((target - low) / range).toFloat() * size.height)
            drawLine(
                color = colors.tertiary.copy(alpha = 0.75f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(7.dp.toPx(), 5.dp.toPx()))
            )
        }
        val path = Path()
        finite.forEachIndexed { index, value ->
            val p = point(index, value)
            if (index == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
        }
        drawPath(path, colors.primary, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        finite.forEachIndexed { index, value ->
            val p = point(index, value)
            drawCircle(colors.surface, radius = 5.dp.toPx(), center = p)
            drawCircle(colors.primary, radius = 3.dp.toPx(), center = p)
        }
    }
}

internal fun formatNet(value: Double): String =
    ((value * 100.0).roundToInt() / 100.0).toString().replace('.', ',')
