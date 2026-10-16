package com.deniz0706.ykstakip.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
    val filtered = remember(exams, selected) { exams.filter { it.type == selected }.sortedBy { it.date } }
    val nets = remember(filtered) { filtered.map { it.totalNet } }
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
                Text("SON ${selected.name} DENEMESİ", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(12.dp))
                Text(last?.let(Fmt::net) ?: "—", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    delta?.let { "Önceki denemeye göre ${Fmt.netWithSign(it)} net" } ?: "Henüz karşılaştırma yok",
                    color = if (delta != null && delta >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
                NetSparkline(nets.takeLast(10), Modifier.fillMaxWidth().height(94.dp))
                Spacer(Modifier.height(12.dp))
                Text("Son ${minOf(nets.size, 10)} denemenin net eğilimi", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile("Ortalama", if (nets.isEmpty()) "—" else Fmt.net(nets.average()), Modifier.weight(1f))
                MetricTile("En yüksek", nets.maxOrNull()?.let(Fmt::net) ?: "—", Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile("Son 5 ort.", if (nets.isEmpty()) "—" else Fmt.net(nets.takeLast(5).average()), Modifier.weight(1f))
                MetricTile("Deneme", filtered.size.toString(), Modifier.weight(1f))
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
    Canvas(modifier) {
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
            drawPath(path, lineColor, style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.PathJoin.Round))
        }
        points.lastOrNull()?.let { drawCircle(lineColor, 5.dp.toPx(), it) }
    }
}
