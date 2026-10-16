package com.deniz0706.ykstakip.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.ui.components.ExamTypeTabs
import com.deniz0706.ykstakip.ui.components.MetricTile
import com.deniz0706.ykstakip.ui.components.YksCard
import com.deniz0706.ykstakip.ui.components.NetTrendChart
import com.deniz0706.ykstakip.util.Fmt

@Composable
fun HomeScreenV2(
    exams: List<Exam>,
    onNewExam: (ExamType) -> Unit,
    modifier: Modifier = Modifier,
    daysLeft: Long? = null,
    todayQuestions: Int = 0,
    dailyGoal: Int? = null,
    targetTyt: Float? = null,
    targetAyt: Float? = null,
    daysLeftAyt: Long? = null
) {
    var selected by remember { mutableStateOf(ExamType.TYT) }
    var period by remember { mutableStateOf(0) }
    var showTarget by remember { mutableStateOf(true) }
    val filtered = remember(exams, selected) { exams.filter { it.type == selected }.sortedBy { it.date } }
    val visible = remember(filtered, period) {
        val cutoff = when (period) { 0 -> 7L; 1 -> 30L; 2 -> 90L; else -> null }
        if (cutoff == null) filtered else {
            val limit = java.time.LocalDate.now().minusDays(cutoff - 1)
            filtered.filter { runCatching { java.time.LocalDate.parse(it.date).isBefore(limit) }.getOrDefault(false).not() }
        }
    }
    val nets = remember(visible) { visible.map { it.totalNet } }
    val targetNet = if (selected == ExamType.TYT) targetTyt else targetAyt
    val remainingDays = if (selected == ExamType.TYT) daysLeft else daysLeftAyt
    val last = nets.lastOrNull()
    val delta = if (nets.size >= 2) nets.last() - nets[nets.lastIndex - 1] else null
    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 26.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("YKS TAKİP", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelLarge)
                Text("Genel Bakış", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }
        }
        item { ExamTypeTabs(selected.name, { selected = ExamType.valueOf(it) }) }
        item {
            YksCard(highlighted = true) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("SON ${selected.name} DENEMESİ", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(7.dp))
                        AnimatedContent(last?.let(Fmt::net) ?: "—", label = "last-net") { value ->
                            Text(value, fontSize = 42.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    Column {
                        Text("DEĞİŞİM", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        Text(delta?.let { "${if (it >= 0) "↗" else "↘"} ${Fmt.netWithSign(it)}" } ?: "—",
                            color = if (delta != null && delta >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(22.dp))
                Text("Net gelişimi", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    listOf("7G", "30G", "90G", "Tümü").forEachIndexed { index, label ->
                        val active = period == index
                        Text(label,
                            modifier = Modifier.weight(1f).background(
                                if (active) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                                RoundedCornerShape(12.dp)
                            ).clickable { period = index }.padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = if (active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium)
                    }
                }
                Spacer(Modifier.height(18.dp))
                NetTrendChart(nets, visible.map { it.date }, Modifier.fillMaxWidth(), if (showTarget) targetNet else null)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Hedef çizgisi", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                        Text("Grafikte hedef neti göster", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    androidx.compose.material3.Switch(checked = showTarget, onCheckedChange = { showTarget = it }, enabled = targetNet != null)
                }
            }
        }
        item {
            YksCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Performans özeti", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("${filtered.size} deneme", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CompactMetric("Ortalama", if (filtered.isEmpty()) "—" else Fmt.net(filtered.map { it.totalNet }.average()), "◉", Modifier.weight(1f))
                    CompactMetric("En iyi", filtered.maxOfOrNull { it.totalNet }?.let(Fmt::net) ?: "—", "★", Modifier.weight(1f))
                    CompactMetric("Son 5", if (filtered.isEmpty()) "—" else Fmt.net(filtered.takeLast(5).map { it.totalNet }.average()), "↗", Modifier.weight(1f))
                }
            }
        }
        item {
            YksCard {
                Text("HEDEFE İLERLEME", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(10.dp))
                Text(
                    targetNet?.let { "${last?.let(Fmt::net) ?: "—"} / ${Fmt.net(it.toDouble())} net" } ?: "Hedef netini Ayarlar'dan belirleyebilirsin",
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (targetNet != null && targetNet > 0f && last != null) {
                    Spacer(Modifier.height(12.dp))
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { (last / targetNet).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile("Sınava kalan", remainingDays?.let { if (it >= 0) "$it gün" else "Geçti" } ?: "—", Modifier.weight(1f))
                MetricTile("Bugün çözülen", dailyGoal?.let { "$todayQuestions / $it" } ?: "$todayQuestions", Modifier.weight(1f))
            }
        }
        item {
            Button(onClick = { onNewExam(selected) }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)) {
                Text("Yeni ${selected.name} Denemesi")
            }
        }
    }
}

@Composable
private fun NetSparkline(values: List<Double>, modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.secondary
    val guideColor = MaterialTheme.colorScheme.outline
    var selectedIndex by remember(values) { mutableIntStateOf(-1) }
    Column {
    Canvas(modifier.pointerInput(values) {
        detectTapGestures { tap ->
            if (values.isNotEmpty()) {
                val padding = 8.dp.toPx()
                val fraction = ((tap.x - padding) / (size.width - 2 * padding).coerceAtLeast(1f)).coerceIn(0f, 1f)
                selectedIndex = (fraction * (values.size - 1)).toInt().coerceIn(0, values.lastIndex)
            }
        }
    }) {
        drawLine(guideColor, Offset(0f, size.height * 0.8f), Offset(size.width, size.height * 0.8f), 1.dp.toPx())
        if (values.isEmpty()) return@Canvas
        val min = values.minOrNull() ?: return@Canvas
        val max = values.maxOrNull() ?: return@Canvas
        val range = (max - min).coerceAtLeast(1.0)
        val padding = 8.dp.toPx()
        val points = values.mapIndexed { index, value ->
            Offset(
                if (values.size == 1) size.width / 2 else padding + index * (size.width - 2 * padding) / (values.size - 1),
                padding + ((max - value) / range).toFloat() * (size.height - 2 * padding)
            )
        }
        if (points.size > 1) {
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(path, lineColor, style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
        }
        points.forEachIndexed { index, point ->
            if (index == selectedIndex || index == points.lastIndex) {
                drawCircle(lineColor, if (index == selectedIndex) 6.dp.toPx() else 4.dp.toPx(), point)
            }
        }
    }
    if (selectedIndex in values.indices) {
        Text("Seçilen deneme: ${Fmt.net(values[selectedIndex])} net", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
    }
    }
}

@Composable
private fun CompactMetric(label: String, value: String, symbol: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(symbol + "  " + label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 21.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}
