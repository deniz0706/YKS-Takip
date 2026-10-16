package com.deniz0706.ykstakip.ui.study

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.animation.core.animateFloatAsState
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
    val weekdays = remember { listOf(2 to "Pzt", 3 to "Sal", 4 to "Çar", 5 to "Per", 6 to "Cum", 7 to "Cmt", 1 to "Paz") }
    var selectedDay by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) }
    var metric by remember { mutableStateOf("questions") }
    var range by remember { mutableStateOf("7") }
    var showAllDays by remember { mutableStateOf(false) }
    var section by remember { mutableStateOf("today") }
    val listState = rememberLazyListState()
    LaunchedEffect(section) { listState.scrollToItem(0) }
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
    val chartValues = remember(chartDays, metric) {
        chartDays.map {
            it.date to if (metric == "questions") it.questionCount.toFloat() else it.studyMinutes.toFloat()
        }
    }
    val selectedPlans = remember(plan, selectedDay) {
        plan.filter { it.dayOfWeek == selectedDay }.sortedBy { it.startTime }
    }
    val totalQuestions = remember(sorted) { sorted.sumOf { it.questionCount } }
    val totalMinutes = remember(sorted) { sorted.sumOf { it.studyMinutes } }
    val weeklyComparison = remember(sorted, today) {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -13) }
        val previousStart = formatter.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val currentStart = formatter.format(calendar.time)
        val previous = sorted.filter { it.date >= previousStart && it.date < currentStart }
        val currentWeek = sorted.filter { it.date >= currentStart && it.date <= today }
        Triple(
            currentWeek.sumOf { it.questionCount },
            previous.sumOf { it.questionCount },
            currentWeek.sumOf { it.studyMinutes }
        )
    }
    val activeDates = remember(sorted) {
        sorted.filter { it.questionCount > 0 || it.studyMinutes > 0 }.map { it.date }.toSet()
    }
    val streak = remember(activeDates, today) {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cursor = Calendar.getInstance()
        if (today !in activeDates) cursor.add(Calendar.DAY_OF_YEAR, -1)
        var count = 0
        while (count < activeDates.size && formatter.format(cursor.time) in activeDates) {
            count++
            cursor.add(Calendar.DAY_OF_YEAR, -1)
        }
        count
    }
    val activeLast7 = remember(activeDates, today) {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cursor = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6) }
        List(7) {
            val key = formatter.format(cursor.time)
            cursor.add(Calendar.DAY_OF_YEAR, 1)
            key to (key in activeDates)
        }
    }

    LazyColumn(
        Modifier.fillMaxSize().background(colors.background),
        state = listState,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("ÇALIŞMA GÜNLÜĞÜ", color = colors.secondary, style = MaterialTheme.typography.labelLarge)
            Text("Çalışma", color = colors.onBackground, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("Günün, haftalık planın ve gelişimin artık ayrı bölümlerde.",
                color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        item(key = "study-sections") {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf("today" to "Bugün", "plan" to "Plan", "analysis" to "Analiz")
                    .forEachIndexed { index, (key, label) ->
                        SegmentedButton(
                            selected = section == key,
                            onClick = { section = key },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 3),
                            label = { Text(label, maxLines = 1) }
                        )
                    }
            }
        }
        if (section == "today") {
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
        }
        if (section == "today") {
        item {
            DashboardPanel(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("ÇALIŞMA RİTMİ", color = colors.secondary,
                            style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                        Spacer(Modifier.height(5.dp))
                        Text("$streak günlük seri", color = colors.onSurface,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold)
                        Text("${activeLast7.count { it.second }} / 7 aktif gün",
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    DashboardRing(activeLast7.count { it.second } / 7f,
                        "${activeLast7.count { it.second }}/7", caption = "hafta")
                }
                Spacer(Modifier.height(13.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    activeLast7.forEach { (date, active) ->
                        val tileColor by animateColorAsState(
                            targetValue = if (active) colors.primaryContainer
                                else colors.surfaceVariant,
                            animationSpec = spring(stiffness = 350f),
                            label = "study-day-tile"
                        )
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.fillMaxWidth().height(30.dp)
                                .background(tileColor, RoundedCornerShape(10.dp))
                                .clickable { onEditDay(date) },
                                contentAlignment = Alignment.Center) {
                                Text(if (active) "✓" else "·",
                                    color = if (active) colors.onPrimaryContainer
                                        else colors.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(5.dp))
                            Text(date.takeLast(2), color = colors.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                Spacer(Modifier.height(7.dp))
                Text("Bir güne dokunarak kaydını düzenleyebilirsin.",
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
            }
        }
        }
        if (section == "analysis") {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DashboardMetric("Toplam soru", "%,d".format(Locale("tr", "TR"), totalQuestions),
                    Modifier.weight(1f), "Tüm zamanlar")
                DashboardMetric("Çalışma", "${totalMinutes / 60} sa",
                    Modifier.weight(1f), "Toplam süre", colors.primary)
            }
        }
        }
        if (section == "analysis") {
        item(key = "weekly-comparison") {
            val currentTotal = weeklyComparison.first
            val previousTotal = weeklyComparison.second
            val maximum = maxOf(currentTotal, previousTotal, 1).toFloat()
            val diff = currentTotal - previousTotal
            DashboardPanel(Modifier.fillMaxWidth(), accent = true) {
                Text("HAFTALIK KARŞILAŞTIRMA", color = colors.secondary,
                    letterSpacing = 1.sp, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(7.dp))
                Text("Son 7 gün · Önceki 7 gün",
                    color = colors.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(13.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("BU HAFTA", color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall)
                        Text("%,d".format(Locale("tr", "TR"), currentTotal),
                            color = colors.onSurface,
                            fontWeight = FontWeight.Bold, fontSize = 25.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("ÖNCEKİ HAFTA", color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall)
                        Text("%,d".format(Locale("tr", "TR"), previousTotal),
                            color = colors.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold, fontSize = 25.sp)
                    }
                }
                Spacer(Modifier.height(10.dp))
                DashboardProgress(currentTotal / maximum, Modifier.fillMaxWidth(),
                    foreground = colors.primary)
                Spacer(Modifier.height(7.dp))
                DashboardProgress(previousTotal / maximum, Modifier.fillMaxWidth(),
                    foreground = colors.tertiary)
                Spacer(Modifier.height(10.dp))
                Text(when {
                    currentTotal == 0 && previousTotal == 0 ->
                        "İki haftada da soru kaydı bulunmuyor."
                    diff > 0 -> "↗ Önceki haftaya göre $diff soru daha fazla çözdün."
                    diff < 0 -> "↘ Önceki haftaya göre ${-diff} soru daha az çözdün."
                    else -> "→ Her iki haftada da aynı sayıda soru çözdün."
                }, color = if (diff < 0) colors.error else colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
                Text("Son 7 gündeki çalışma: ${weeklyComparison.third / 60} sa ${weeklyComparison.third % 60} dk",
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
            }
        }
        }
        if (section == "analysis") {
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
                    (if (showAllDays) sorted.asReversed() else sorted.asReversed().take(7)).forEach { day ->
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
                    if (sorted.size > 7) {
                        TextButton(onClick = { showAllDays = !showAllDays },
                            modifier = Modifier.fillMaxWidth()) {
                            Text(if (showAllDays) "Daha az göster"
                                else "Tüm ${sorted.size} günü göster")
                        }
                    }
                }
            }
        }
        }
        if (section == "analysis") {
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
        }
        if (section == "plan") {
        item {
            val completed = remember(plan) { plan.count { it.completed } }
            val total = plan.size
            DashboardPanel(Modifier.fillMaxWidth(), accent = true) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    DashboardRing(if (total == 0) 0f else completed.toFloat() / total,
                        if (total == 0) "—" else "${completed * 100 / total}%",
                        caption = "plan")
                    Column(Modifier.weight(1f)) {
                        Text("HAFTALIK PLAN DURUMU", color = colors.secondary,
                            style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("$completed / $total tamamlandı", color = colors.onSurface,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold)
                        Text(if (total == 0) "Haftaya ders ekleyerek planını oluştur."
                            else if (completed == total) "Bu haftanın planı tamamlandı!"
                            else "${total - completed} görev daha tamamlanmayı bekliyor.",
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(14.dp))
                DashboardProgress(if (total == 0) 0f else completed.toFloat() / total,
                    Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    weekdays.forEach { (day, label) ->
                        val tasks = plan.filter { it.dayOfWeek == day }
                        val done = tasks.count { it.completed }
                        val ratio = if (tasks.isEmpty()) 0f else done.toFloat() / tasks.size
                        Column(Modifier.weight(1f).clickable { selectedDay = day },
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(label, color = if (selectedDay == day) colors.primary
                                else colors.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall)
                            Spacer(Modifier.height(6.dp))
                            Box(Modifier.fillMaxWidth().height(34.dp)
                                .background(colors.surfaceVariant, RoundedCornerShape(9.dp)),
                                contentAlignment = Alignment.BottomCenter) {
                                Box(Modifier.fillMaxWidth().fillMaxHeight(ratio)
                                    .background(colors.primary, RoundedCornerShape(9.dp)))
                                Text(if (tasks.isEmpty()) "·" else "$done/${tasks.size}",
                                    color = if (ratio >= .5f) colors.onPrimary
                                        else colors.onSurface,
                                    fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Bir güne dokunarak o günün derslerini aç.",
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
            }
        }
        }
        if (section == "plan") {
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
        }
        if (section == "plan") {
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
        }
        if (section == "analysis") {
        item {
            DashboardPanel(Modifier.fillMaxWidth()) {
                Text("KİŞİSEL REKORLAR", color = colors.secondary,
                    style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
                Spacer(Modifier.height(6.dp))
                Text("Çalışma kilometre taşları", color = colors.onSurface,
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(14.dp))
                val bestQ = sorted.maxByOrNull { it.questionCount }
                val bestT = sorted.maxByOrNull { it.studyMinutes }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("EN FAZLA SORU", color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall)
                        Text(bestQ?.questionCount?.toString() ?: "—", color = colors.primary,
                            fontSize = 29.sp, fontWeight = FontWeight.Bold)
                        Text(bestQ?.date ?: "Henüz kayıt yok",
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("EN UZUN ÇALIŞMA", color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall)
                        Text(bestT?.studyMinutes?.let { "${it / 60}sa ${it % 60}dk" } ?: "—",
                            color = colors.onSurface,
                            fontSize = 25.sp, fontWeight = FontWeight.Bold)
                        Text(bestT?.date ?: "Henüz kayıt yok",
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(Modifier.height(15.dp))
                HorizontalDivider(color = colors.outline.copy(alpha = .3f))
                Spacer(Modifier.height(13.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Kayıtlı gün", color = colors.onSurfaceVariant)
                    Text("${sorted.size}", color = colors.onSurface, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(7.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Günlük soru ortalaması", color = colors.onSurfaceVariant)
                    Text(if (sorted.isEmpty()) "—" else "${totalQuestions / sorted.size}",
                        color = colors.onSurface, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(7.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Günlük çalışma ortalaması", color = colors.onSurfaceVariant)
                    Text(if (sorted.isEmpty()) "—" else "${totalMinutes / sorted.size} dk",
                        color = colors.onSurface, fontWeight = FontWeight.Bold)
                }
                if (goal != null && goal > 0) {
                    Spacer(Modifier.height(7.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Hedefe ulaşılan gün", color = colors.onSurfaceVariant)
                        Text("${sorted.count { it.questionCount >= goal }}",
                            color = colors.primary, fontWeight = FontWeight.Bold)
                    }
                }
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
    val animatedSelection by animateFloatAsState(
        selected.toFloat(), animationSpec = spring(stiffness = 550f),
        label = "study-chart-selection"
    )
    Box(
        Modifier.fillMaxWidth().height(205.dp)
            .pointerInput(display) {
                detectTapGestures { offset ->
                    selected = ((offset.x / size.width.coerceAtLeast(1) * display.size)
                        .toInt()).coerceIn(0, display.lastIndex)
                }
            }
            .pointerInput(display) {
                detectDragGestures(
                    onDragStart = {
                        selected = ((it.x / size.width.coerceAtLeast(1) * display.size)
                            .toInt()).coerceIn(0, display.lastIndex)
                    },
                    onDrag = { change, _ ->
                        selected = ((change.position.x / size.width.coerceAtLeast(1) * display.size)
                            .toInt()).coerceIn(0, display.lastIndex)
                        change.consume()
                    }
                )
            }
            .drawWithCache {
                val count = display.size
                val slot = size.width / count
                val gap = 3.dp.toPx().coerceAtMost(slot * .3f)
                val barWidth = (slot - gap).coerceAtLeast(1f)
                val rounding = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx())
                val fill = Brush.verticalGradient(
                    listOf(foreground, foreground.copy(alpha = .65f)))
                val base = background.copy(alpha = .23f)
                val bars = display.mapIndexed { index, pair ->
                    val height = (pair.second / max).coerceIn(0f, 1f) * size.height
                    val x = index * slot + gap / 2f
                    androidx.compose.ui.geometry.Rect(x, size.height - height, x + barWidth, size.height)
                }
                onDrawBehind {
                    bars.forEachIndexed { index, rect ->
                        val x = index * slot + gap / 2f
                        drawRoundRect(base, topLeft = Offset(x, 0f),
                            size = androidx.compose.ui.geometry.Size(barWidth, size.height),
                            cornerRadius = rounding)
                        if (rect.height > 0f) {
                            drawRoundRect(fill, topLeft = rect.topLeft,
                                size = rect.size, cornerRadius = rounding)
                        }
                    }
                    val x = (animatedSelection + .5f) * slot
                    drawLine(foreground.copy(alpha = .65f),
                        Offset(x, 0f), Offset(x, size.height),
                        strokeWidth = 1.dp.toPx())
                    val selectedBar = bars[selected.coerceIn(0, bars.lastIndex)]
                    drawCircle(foreground,
                        radius = 4.dp.toPx(),
                        center = Offset(x, selectedBar.top.coerceIn(4.dp.toPx(),
                            size.height - 4.dp.toPx())))
                }
            }
    )
    Spacer(Modifier.height(8.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(display.first().first, color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall)
        Text(display.last().first, color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall)
    }
}
