package com.deniz0706.ykstakip.ui.study

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.model.StudyDay
import com.deniz0706.ykstakip.model.StudyPlanItem
import com.deniz0706.ykstakip.ui.components.YksCard
import com.deniz0706.ykstakip.ui.components.DashboardPanel
import com.deniz0706.ykstakip.ui.components.DashboardMetric
import com.deniz0706.ykstakip.ui.components.DashboardRing
import com.deniz0706.ykstakip.ui.components.DashboardProgress
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun StudyScreenV2(
    days: List<StudyDay>,
    plan: List<StudyPlanItem>,
    goal: Int?,
    onEditToday: () -> Unit,
    onEditDay: (String) -> Unit,
    onPickDay: () -> Unit,
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
    val chartDays = remember(sorted, range, today) {
        if (range == "all") sorted
        else {
            val count = range.toInt()
            val lookup = sorted.associateBy { it.date }
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -(count - 1)) }
            List(count) {
                val key = formatter.format(calendar.time)
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                lookup[key] ?: StudyDay(key, 0, 0)
            }
        }
    }
    val chartValues = chartDays.map {
        it.date to if (metric == "questions") it.questionCount.toFloat() else it.studyMinutes.toFloat()
    }
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
            DashboardPanel(Modifier.fillMaxWidth(), accent = true) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("BUGÜNÜN PERFORMANSI", color = colors.secondary,
                            style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("$questions", fontSize = 52.sp, lineHeight = 58.sp,
                            fontWeight = FontWeight.Bold, color = colors.onSurface)
                        Text("çözülen soru", color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    if (goal != null && goal > 0)
                        DashboardRing(questions.toFloat() / goal,
                            "${questions * 100L / goal}%", caption = "hedef")
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("${minutes / 60} sa ${minutes % 60} dk",
                            fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("çalışma süresi", color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedButton(onClick = onEditToday,
                        shape = RoundedCornerShape(14.dp)) { Text("Düzenle") }
                }
                if (goal != null && goal > 0) {
                    Spacer(Modifier.height(17.dp))
                    DashboardProgress(questions.toFloat() / goal, Modifier.fillMaxWidth())
                    Spacer(Modifier.height(7.dp))
                    Text(if (questions >= goal) "Günlük hedef tamamlandı ✓"
                        else "$questions / $goal soru · hedefe ilerleme",
                        color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DashboardMetric("Toplam soru", "%,d".format(Locale("tr", "TR"), totalQuestions),
                    Modifier.weight(1f), "Tüm zamanlar")
                DashboardMetric("Çalışma", "${totalMinutes / 60} sa",
                    Modifier.weight(1f), "Toplam süre", colors.primary)
            }
        }
        item {
            DashboardPanel(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("Çalışma arşivi", style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                        Text("Önceki günlerini seç, incele ve düzenle.",
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = onPickDay, shape = RoundedCornerShape(14.dp)) {
                        Text("+ Gün")
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (sorted.isEmpty()) {
                    Text("Henüz kayıtlı çalışma günü yok.", color = colors.onSurfaceVariant)
                } else {
                    sorted.asReversed().take(7).forEach { day ->
                        Row(Modifier.fillMaxWidth()
                            .clickable { onEditDay(day.date) }
                            .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(day.date, fontWeight = FontWeight.SemiBold,
                                    color = colors.onSurface)
                                Text("${day.studyMinutes / 60} sa ${day.studyMinutes % 60} dk",
                                    color = colors.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            Text("${day.questionCount} soru", color = colors.primary,
                                fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(10.dp))
                            Text("Düzenle ›", color = colors.secondary,
                                style = MaterialTheme.typography.labelMedium)
                        }
                        HorizontalDivider(color = colors.outline.copy(alpha = .25f))
                    }
                }
            }
        }
        item {
            DashboardPanel(Modifier.fillMaxWidth()) {
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
                    StudyBars(chartValues, colors.primary, colors.surfaceVariant, if (metric == "questions") "soru" else "dk")
                    Spacer(Modifier.height(8.dp))
                    Text("${chartDays.count { it.questionCount > 0 || it.studyMinutes > 0 }} kayıtlı gün · En yüksek: ${chartValues.maxOfOrNull { it.second }?.toInt() ?: 0}",
                        color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            DashboardPanel(Modifier.fillMaxWidth()) {
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
            DashboardPanel(Modifier.fillMaxWidth()) {
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
    values: List<Pair<String, Float>>,
    foreground: androidx.compose.ui.graphics.Color,
    background: androidx.compose.ui.graphics.Color,
    unit: String
) {
    val display = remember(values) {
        if (values.size <= 42) values
        else values.chunked((values.size + 41) / 42).map { chunk ->
            chunk.last().first to chunk.map { it.second }.average().toFloat()
        }
    }
    if (display.isEmpty()) return
    var selected by remember(display) { mutableIntStateOf(display.lastIndex) }
    val max = (display.maxOfOrNull { it.second } ?: 1f).coerceAtLeast(1f)
    val chosen = display[selected.coerceIn(0, display.lastIndex)]
    Text("${chosen.first} · ${chosen.second.toInt()} $unit",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary)
    Spacer(Modifier.height(8.dp))
    var widthPx by remember { mutableFloatStateOf(1f) }
    Row(
        Modifier.fillMaxWidth().height(185.dp)
            .onSizeChanged { widthPx = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(display) {
                detectTapGestures { offset ->
                    selected = ((offset.x / widthPx) * display.size).toInt().coerceIn(0, display.lastIndex)
                }
            }
            .pointerInput(display) {
                detectDragGestures(
                    onDragStart = { selected = ((it.x / widthPx) * display.size).toInt().coerceIn(0, display.lastIndex) },
                    onDrag = { change, _ ->
                        selected = ((change.position.x / widthPx) * display.size).toInt().coerceIn(0, display.lastIndex)
                        change.consume()
                    }
                )
            },
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        display.forEachIndexed { index, entry ->
            val value = entry.second
            Box(Modifier.weight(1f).fillMaxHeight()
                .background(background.copy(alpha = .25f), RoundedCornerShape(5.dp)),
                contentAlignment = Alignment.BottomCenter) {
                Box(Modifier.fillMaxWidth()
                    .fillMaxHeight((value / max).coerceIn(0f, 1f))
                    .background(if (index == selected) MaterialTheme.colorScheme.secondary else foreground,
                        RoundedCornerShape(5.dp)))
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(display.first().first, color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall)
        Text(display.last().first, color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall)
    }
}
