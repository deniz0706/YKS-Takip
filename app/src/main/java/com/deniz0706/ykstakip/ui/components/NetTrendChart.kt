package com.deniz0706.ykstakip.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.util.Fmt
import kotlin.math.roundToInt

@Composable
fun NetTrendChart(values: List<Double>, labels: List<String>, modifier: Modifier = Modifier, target: Float? = null) {
    val colors = MaterialTheme.colorScheme
    val line = colors.tertiary
    val guide = colors.outline.copy(alpha = .26f)
    val surface = colors.background
    val animation = remember { Animatable(0f) }
    var selected by remember(values) { mutableIntStateOf(if (values.isEmpty()) -1 else values.lastIndex) }
    LaunchedEffect(values) {
        animation.snapTo(0f)
        animation.animateTo(1f, tween(850))
    }
    Column(modifier) {
        if (values.isNotEmpty() && selected in values.indices) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("SEÇİLEN DENEME", color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    Text("${Fmt.net(values[selected])} net", color = colors.onSurface, fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
                }
                Text(labels.getOrNull(selected).orEmpty(), color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(12.dp))
        }
        if (values.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(240.dp)) {
                Text("Grafik için henüz deneme kaydı yok", color = colors.onSurfaceVariant)
            }
            return@Column
        }
        val low = minOf(values.minOrNull() ?: 0.0, target?.toDouble() ?: Double.POSITIVE_INFINITY)
        val high = maxOf(values.maxOrNull() ?: 1.0, target?.toDouble() ?: Double.NEGATIVE_INFINITY)
        val margin = ((high - low) * .14).coerceAtLeast(3.0)
        val minY = low - margin
        val maxY = high + margin
        fun valueAt(index: Int) = values[index]
        Canvas(
            Modifier.fillMaxWidth().height(250.dp)
                .pointerInput(values) {
                    detectTapGestures { pos ->
                        val left = 44.dp.toPx()
                        val right = size.width - 12.dp.toPx()
                        selected = if (values.size == 1) 0 else
                            (((pos.x - left) / (right - left).coerceAtLeast(1f)) * (values.size - 1)).roundToInt().coerceIn(0, values.lastIndex)
                    }
                }
                .pointerInput(values) {
                    detectDragGestures { change, _ ->
                        val left = 44.dp.toPx()
                        val right = size.width - 12.dp.toPx()
                        selected = if (values.size == 1) 0 else
                            (((change.position.x - left) / (right - left).coerceAtLeast(1f)) * (values.size - 1)).roundToInt().coerceIn(0, values.lastIndex)
                        change.consume()
                    }
                }
        ) {
            val left = 44.dp.toPx()
            val right = size.width - 12.dp.toPx()
            val top = 12.dp.toPx()
            val bottom = size.height - 22.dp.toPx()
            fun y(v: Double): Float = bottom - ((v - minY) / (maxY - minY)).toFloat() * (bottom - top)
            val pts = values.mapIndexed { i, v ->
                Offset(if (values.size == 1) (left + right) / 2 else left + (right - left) * i / (values.size - 1), y(v))
            }
            repeat(4) { i ->
                val v = minY + (maxY - minY) * i / 3.0
                val yy = y(v)
                drawLine(guide, Offset(left, yy), Offset(right, yy), strokeWidth = 1.dp.toPx())
                drawContext.canvas.nativeCanvas.drawText(
                    Fmt.net(v), 0f, yy + 4.dp.toPx(),
                    android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        color = android.graphics.Color.argb(160, 155, 172, 190)
                        textSize = 10.sp.toPx()
                    }
                )
            }
            target?.let {
                val ty = y(it.toDouble())
                drawLine(colors.primary.copy(alpha = .65f), Offset(left, ty), Offset(right, ty), strokeWidth = 1.5.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(9f, 8f)))
            }
            val path = Path().apply {
                moveTo(pts.first().x, pts.first().y)
                for (i in 1 until pts.size) {
                    val prev = pts[i - 1]
                    val next = pts[i]
                    val midX = (prev.x + next.x) / 2
                    cubicTo(midX, prev.y, midX, next.y, next.x, next.y)
                }
            }
            val area = Path().apply {
                addPath(path)
                lineTo(pts.last().x, bottom)
                lineTo(pts.first().x, bottom)
                close()
            }
            drawPath(area, Brush.verticalGradient(listOf(line.copy(alpha = .25f), line.copy(alpha = 0f)), startY = top, endY = bottom), alpha = animation.value)
            drawPath(path, line, style = Stroke(width = 2.7.dp.toPx(), cap = StrokeCap.Round), alpha = animation.value)
            if (selected in pts.indices) {
                val p = pts[selected]
                drawLine(line.copy(alpha = .5f), Offset(p.x, top), Offset(p.x, bottom), 1.dp.toPx())
                drawCircle(surface, 7.dp.toPx(), p)
                drawCircle(line, 5.dp.toPx(), p)
            }
        }
        Row(Modifier.fillMaxWidth().padding(start = 44.dp, end = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(labels.firstOrNull().orEmpty(), color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            if (labels.size > 1) Text(labels.last(), color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        }
    }
}
