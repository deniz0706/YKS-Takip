package com.deniz0706.ykstakip.ui.study

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.model.StudyDay
import com.deniz0706.ykstakip.model.StudyPlanItem
import com.deniz0706.ykstakip.ui.components.YksCard
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun StudyScreenV2(
    days: List<StudyDay>,
    plan: List<StudyPlanItem>,
    goal: Int?,
    onEditToday: () -> Unit,
    onAddPlan: (Int) -> Unit,
    onTogglePlan: (Long) -> Unit,
    onDeletePlan: (StudyPlanItem) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(java.util.Date())
    val current = days.firstOrNull { it.date == today }
    val questions = current?.questionCount ?: 0
    val minutes = current?.studyMinutes ?: 0
    val weekdays = listOf(2 to "Pzt", 3 to "Sal", 4 to "Çar", 5 to "Per", 6 to "Cum", 7 to "Cmt", 1 to "Paz")
    var selectedDay by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) }
    var metric by remember { mutableStateOf("questions") }
    var range by remember { mutableStateOf("7") }
    val sorted = remember(days) { days.sortedBy { it.date } }
    val filtered = remember(sorted, range) {
        if (range == "all") sorted else {
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -(range.toInt() - 1)) }
            val start = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
            sorted.filter { it.date >= start }
        }
    }
    val chartValues = filtered.map { if (metric == "questions") it.questionCount.toFloat() else it.studyMinutes.toFloat() }
    val selectedPlans = plan.filter { it.dayOfWeek == selectedDay }.sortedBy { it.startTime }
    val totalQuestions = sorted.sumOf { it.questionCount }
    val totalMinutes = sorted.sumOf { it.studyMinutes }

    LazyColumn(
        Modifier.fillMaxSize().background(colors.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("ÇALIŞMA GÜNLÜĞÜ", color = colors.secondary, style = MaterialTheme.typography.labelLarge)
            Text("Çalışma", color = colors.onBackground, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("Günlük ilerlemeni ve ders planını tek yerden takip et.",
                color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        item {
            YksCard(Modifier.fillMaxWidth(), highlighted = true) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Bugün", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = onEditToday) { Text("Düzenle") }
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("$questions", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                        Text("Çözülen soru", color = colors.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${minutes / 60} sa ${minutes % 60} dk", fontSize = 23.sp, fontWeight = FontWeight.Bold)
                        Text("Çalışma süresi", color = colors.onSurfaceVariant)
                    }
                }
                if (goal != null && goal > 0) {
                    Spacer(Modifier.height(18.dp))
                    Text("Günlük hedef · $questions / $goal", color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (questions.toFloat() / goal).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = colors.primary, trackColor = colors.surfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(if (questions >= goal) "Hedef tamamlandı ✓" else "%${questions * 100L / goal} tamamlandı",
                        color = colors.primary, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        item {
            YksCard(Modifier.fillMaxWidth()) {
                Text("Çalışma grafiği", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("questions" to "Soru", "time" to "Süre").forEach { (key, label) ->
                        FilterChip(selected = metric == key, onClick = { metric = key }, label = { Text(label) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("7" to "7G", "30" to "30G", "all" to "Tümü").forEach { (key, label) ->
                        FilterChip(selected = range == key, onClick = { range = key }, label = { Text(label) })
                    }
                }
                if (chartValues.isEmpty()) {
                    Text("Henüz çalışma kaydı yok.", color = colors.onSurfaceVariant)
                } else {
                    StudyBars(chartValues, colors.primary, colors.surfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("${filtered.size} kayıt · En yüksek: ${chartValues.maxOrNull()?.toInt() ?: 0}",
                        color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            YksCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Haftalık ders planı", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = { onAddPlan(selectedDay) }) { Text("+ Ders") }
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    weekdays.forEach { (day, label) ->
                        val active = selectedDay == day
                        Box(Modifier.weight(1f).background(
                            if (active) colors.secondary else colors.surfaceVariant, RoundedCornerShape(10.dp)
                        ).clickable { selectedDay = day }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium,
                                color = if (active) colors.onSecondary else colors.onSurfaceVariant)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("${selectedPlans.count { it.completed }} / ${selectedPlans.size} tamamlandı",
                    color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                if (selectedPlans.isEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text("Bu güne henüz ders eklenmedi.", color = colors.onSurfaceVariant)
                }
            }
        }
        items(selectedPlans, key = { it.id }) { item ->
            YksCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = item.completed, onCheckedChange = { onTogglePlan(item.id) })
                    Column(Modifier.weight(1f)) {
                        Text(item.subject, fontWeight = FontWeight.SemiBold,
                            color = if (item.completed) colors.onSurfaceVariant else colors.onSurface)
                        if (item.topic.isNotBlank()) Text(item.topic, color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall)
                        Text("${item.startTime} – ${item.endTime}", color = colors.secondary,
                            style = MaterialTheme.typography.labelMedium)
                    }
                    TextButton(onClick = { onDeletePlan(item) }) { Text("Sil", color = colors.error) }
                }
            }
        }
        item {
            YksCard(Modifier.fillMaxWidth()) {
                Text("Genel özet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                Text("Toplam soru: $totalQuestions")
                Text("Toplam çalışma: ${totalMinutes / 60} saat ${totalMinutes % 60} dakika")
                Text("Kayıtlı gün: ${sorted.size}")
                if (sorted.isNotEmpty()) {
                    Text("Günlük ortalama: ${totalQuestions / sorted.size} soru")
                    Text("Ortalama çalışma: ${totalMinutes / sorted.size} dakika")
                    val bestQ = sorted.maxByOrNull { it.questionCount }
                    val bestT = sorted.maxByOrNull { it.studyMinutes }
                    Text("En çok soru: ${bestQ?.questionCount} · ${bestQ?.date}")
                    Text("En uzun çalışma: ${bestT?.studyMinutes} dk · ${bestT?.date}")
                    if (goal != null && goal > 0) Text("Hedefe ulaşılan gün: ${sorted.count { it.questionCount >= goal }}")
                }
            }
        }
    }
}

@Composable
private fun StudyBars(
    values: List<Float>,
    foreground: androidx.compose.ui.graphics.Color,
    background: androidx.compose.ui.graphics.Color
) {
    val display = remember(values) {
        if (values.size <= 42) values else values.chunked((values.size + 41) / 42).map { it.average().toFloat() }
    }
    val max = (display.maxOrNull() ?: 1f).coerceAtLeast(1f)
    Row(Modifier.fillMaxWidth().height(172.dp), horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom) {
        display.forEach { value ->
            Box(Modifier.weight(1f).fillMaxHeight().background(background.copy(alpha = .25f), RoundedCornerShape(5.dp)),
                contentAlignment = Alignment.BottomCenter) {
                Box(Modifier.fillMaxWidth().fillMaxHeight((value / max).coerceIn(.015f, 1f))
                    .background(foreground, RoundedCornerShape(5.dp)))
            }
        }
    }
}
