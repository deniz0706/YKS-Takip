package com.deniz0706.ykstakip.ui.study

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.model.StudyDay
import com.deniz0706.ykstakip.ui.components.DashboardPanel
import java.time.DayOfWeek
import java.time.LocalDate

private data class DayCell(val day: LocalDate, val questionCount: Int)

/**
 * Five full Monday-to-Sunday weeks. One Canvas rather than 35 composable boxes,
 * with a tap target for editing any past day.
 */
@Composable
fun StudyActivityHeatmap(
    days: List<StudyDay>,
    onEditDay: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val today = LocalDate.now()
    val cells = remember(days, today) {
        val records = days.associateBy { it.date }
        val start = today.minusWeeks(4).with(DayOfWeek.MONDAY)
        List(35) { index ->
            val date = start.plusDays(index.toLong())
            DayCell(date, records[date.toString()]?.questionCount ?: 0)
        }
    }
    val maximum = cells.maxOfOrNull { it.questionCount }?.coerceAtLeast(1) ?: 1
    val eligible = cells.filter { it.day <= today }
    val total = eligible.sumOf { it.questionCount }
    val active = eligible.count { it.questionCount > 0 }
    val paint = remember(colors.onSurface, colors.onPrimary) {
        android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
    }
    val normalText = colors.onSurface.toArgb()
    val activeText = colors.onPrimary.toArgb()
    val futureText = colors.onSurfaceVariant.toArgb()
    DashboardPanel(modifier) {
        Text("AKTİVİTE TAKVİMİ", color = colors.secondary,
            style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
        Spacer(Modifier.height(6.dp))
        Text("Son 5 hafta", color = colors.onSurface,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold)
        Text("Her kare bir gün. Geçmiş bir güne dokunarak kaydını aç.",
            color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(15.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz").forEach { day ->
                Text(day, modifier = Modifier.weight(1f),
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
        Spacer(Modifier.height(6.dp))
        Canvas(Modifier.fillMaxWidth().height(190.dp)
            .pointerInput(cells) {
                detectTapGestures { tap ->
                    if (size.width <= 0 || size.height <= 0) return@detectTapGestures
                    val x = (tap.x / size.width * 7f).toInt().coerceIn(0, 6)
                    val y = (tap.y / size.height * 5f).toInt().coerceIn(0, 4)
                    val day = cells[y * 7 + x].day
                    if (!day.isAfter(LocalDate.now())) onEditDay(day.toString())
                }
            }
        ) {
            val slotW = size.width / 7f
            val slotH = size.height / 5f
            val padding = 3.dp.toPx()
            val round = CornerRadius(10.dp.toPx())
            paint.textSize = 11.sp.toPx()
            cells.forEachIndexed { index, cell ->
                val isFuture = cell.day.isAfter(today)
                val intensity = (cell.questionCount.toFloat() / maximum)
                    .coerceIn(0f, 1f)
                val x = (index % 7) * slotW + padding
                val y = (index / 7) * slotH + padding
                val w = (slotW - padding * 2f).coerceAtLeast(1f)
                val h = (slotH - padding * 2f).coerceAtLeast(1f)
                val background = when {
                    isFuture -> colors.surfaceVariant.copy(alpha = .23f)
                    cell.questionCount > 0 -> colors.primary.copy(alpha = .25f + .72f * intensity)
                    else -> colors.surfaceVariant
                }
                drawRoundRect(background, topLeft = Offset(x, y),
                    size = Size(w, h), cornerRadius = round)
                if (cell.day == today) {
                    drawRoundRect(colors.secondary, topLeft = Offset(x, y),
                        size = Size(w, h), cornerRadius = round,
                        style = Stroke(1.8.dp.toPx()))
                }
                paint.color = when {
                    isFuture -> futureText
                    cell.questionCount > 0 && intensity > .65f -> activeText
                    else -> normalText
                }
                paint.alpha = if (isFuture) 110 else 255
                drawContext.canvas.nativeCanvas.drawText(
                    cell.day.dayOfMonth.toString(),
                    x + w / 2f,
                    y + h / 2f - (paint.ascent() + paint.descent()) / 2f,
                    paint
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("$active aktif gün", color = colors.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
                Text("$total soru · gösterilen dönem", color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
            }
            Text("Az  ▪ ▪ ▪ ▪  Çok",
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall)
        }
    }
}
