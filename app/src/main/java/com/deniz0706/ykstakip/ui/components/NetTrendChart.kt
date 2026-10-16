package com.deniz0706.ykstakip.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.util.Fmt
import kotlin.math.roundToInt

/**
 * Exam-count trend, with a geometric left-to-right reveal.
 * Path, axis labels, gradient and strokes are cached until data/size/theme changes;
 * cursor movement and reveal only invalidate the draw pass.
 */
@Composable
fun NetTrendChart(
    values: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    target: Float? = null
) {
    val colors = MaterialTheme.colorScheme
    val line = colors.tertiary
    val guide = colors.outline.copy(alpha = .28f)
    val progress = remember { Animatable(0f) }
    var selected by remember(values) {
        mutableIntStateOf(if (values.isEmpty()) -1 else values.lastIndex)
    }
    val animatedSelection by animateFloatAsState(
        targetValue = selected.coerceAtLeast(0).toFloat(),
        animationSpec = spring(stiffness = 540f, dampingRatio = .88f),
        label = "exam-trend-cursor"
    )
    LaunchedEffect(values) {
        if (values.isNotEmpty()) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(durationMillis = 1050))
        }
    }

    Column(modifier) {
        if (values.isEmpty()) {
            Text("Henüz deneme kaydı yok. İlk denemeni ekleyerek grafiği oluştur.",
                color = colors.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(vertical = 46.dp))
            return@Column
        }
        val index = selected.coerceIn(0, values.lastIndex)
        val current = values[index]
        val previous = values.getOrNull(index - 1)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("SEÇİLEN DENEME · ${index + 1}/${values.size}",
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
                Text("${Fmt.net(current)} net",
                    color = colors.onSurface, fontSize = 26.sp,
                    fontWeight = FontWeight.Bold)
                if (previous != null) {
                    val delta = current - previous
                    Text(
                        if (delta >= 0) "↗ +${Fmt.net(delta)} önceki denemeye göre"
                        else "↘ ${Fmt.net(delta)} önceki denemeye göre",
                        color = if (delta >= 0) colors.primary else colors.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Text(labels.getOrNull(index).orEmpty(),
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(14.dp))

        val low = minOf(values.minOrNull() ?: 0.0,
            target?.toDouble() ?: Double.POSITIVE_INFINITY)
        val high = maxOf(values.maxOrNull() ?: 1.0,
            target?.toDouble() ?: Double.NEGATIVE_INFINITY)
        val margin = ((high - low) * .14).coerceAtLeast(3.0)
        val minY = low - margin
        val maxY = high + margin
        val axisText = colors.onSurfaceVariant.toArgb()
        Box(
            Modifier.fillMaxWidth().height(240.dp)
                .pointerInput(values) {
                    detectTapGestures { offset ->
                        val left = 44.dp.toPx()
                        val width = (size.width - left - 12.dp.toPx()).coerceAtLeast(1f)
                        selected = if (values.size == 1) 0
                        else (((offset.x - left) / width) * values.lastIndex)
                            .roundToInt().coerceIn(0, values.lastIndex)
                    }
                }
                .pointerInput(values) {
                    detectDragGestures { change, _ ->
                        val left = 44.dp.toPx()
                        val width = (size.width - left - 12.dp.toPx()).coerceAtLeast(1f)
                        selected = if (values.size == 1) 0
                        else (((change.position.x - left) / width) * values.lastIndex)
                            .roundToInt().coerceIn(0, values.lastIndex)
                        change.consume()
                    }
                }
                .drawWithCache {
                    val left = 44.dp.toPx()
                    val right = (size.width - 12.dp.toPx()).coerceAtLeast(left + 1f)
                    val top = 12.dp.toPx()
                    val bottom = size.height - 22.dp.toPx()
                    fun y(v: Double): Float =
                        bottom - ((v - minY) / (maxY - minY)).toFloat() * (bottom - top)
                    val points = values.mapIndexed { i, v ->
                        Offset(
                            if (values.size == 1) (left + right) / 2f
                            else left + (right - left) * i / values.lastIndex,
                            y(v)
                        )
                    }
                    val curve = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            val a = points[i - 1]
                            val b = points[i]
                            val centerX = (a.x + b.x) / 2f
                            cubicTo(centerX, a.y, centerX, b.y, b.x, b.y)
                        }
                    }
                    val area = Path().apply {
                        addPath(curve)
                        lineTo(points.last().x, bottom)
                        lineTo(points.first().x, bottom)
                        close()
                    }
                    val fill = Brush.verticalGradient(
                        listOf(line.copy(alpha = .27f), line.copy(alpha = .015f)),
                        startY = top, endY = bottom
                    )
                    val stroke = Stroke(3.dp.toPx(), cap = StrokeCap.Round)
                    val dash = PathEffect.dashPathEffect(floatArrayOf(9.dp.toPx(), 6.dp.toPx()))
                    val axisPaint = android.graphics.Paint(
                        android.graphics.Paint.ANTI_ALIAS_FLAG
                    ).apply {
                        color = axisText
                        alpha = 200
                        textSize = 10.sp.toPx()
                    }
                    val axisLines = List(4) { i ->
                        val v = minY + (maxY - minY) * i / 3.0
                        y(v) to Fmt.net(v)
                    }
                    val targetLineY = target?.let { y(it.toDouble()) }
                    onDrawBehind {
                        axisLines.forEach { (yy, label) ->
                            drawLine(guide, Offset(left, yy), Offset(right, yy),
                                strokeWidth = 1.dp.toPx())
                            drawContext.canvas.nativeCanvas.drawText(
                                label, 0f, yy + 4.dp.toPx(), axisPaint)
                        }
                        if (targetLineY != null) {
                            drawLine(colors.primary.copy(alpha = .73f),
                                Offset(left, targetLineY),
                                Offset(right, targetLineY),
                                strokeWidth = 1.5.dp.toPx(), pathEffect = dash)
                        }
                        val revealX = left + (right - left) * progress.value
                        clipRect(left = left, top = top, right = revealX, bottom = bottom) {
                            drawPath(area, fill)
                            drawPath(curve, line, style = stroke)
                        }
                        val first = animatedSelection.toInt().coerceIn(0, points.lastIndex)
                        val next = (first + 1).coerceAtMost(points.lastIndex)
                        val t = (animatedSelection - first).coerceIn(0f, 1f)
                        val a = points[first]
                        val b = points[next]
                        val cursor = Offset(a.x + (b.x - a.x) * t,
                            a.y + (b.y - a.y) * t)
                        if (cursor.x <= revealX + 1f) {
                            drawLine(line.copy(alpha = .42f),
                                Offset(cursor.x, top), Offset(cursor.x, bottom),
                                strokeWidth = 1.dp.toPx())
                            drawCircle(colors.surface, radius = 7.dp.toPx(), center = cursor)
                            drawCircle(line, radius = 4.7.dp.toPx(), center = cursor)
                        }
                    }
                }
        )
        Row(
            Modifier.fillMaxWidth().padding(start = 44.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(labels.firstOrNull().orEmpty(), color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall)
            if (labels.size > 1) Text(labels.last(), color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall)
        }
    }
}
