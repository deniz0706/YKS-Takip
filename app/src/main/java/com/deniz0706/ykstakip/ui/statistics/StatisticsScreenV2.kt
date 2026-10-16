package com.deniz0706.ykstakip.ui.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.model.*
import com.deniz0706.ykstakip.ui.components.YksCard
import com.deniz0706.ykstakip.ui.components.DashboardPanel
import com.deniz0706.ykstakip.ui.components.DashboardMetric
import com.deniz0706.ykstakip.ui.components.DashboardRing
import com.deniz0706.ykstakip.ui.components.DashboardProgress
import com.deniz0706.ykstakip.ui.components.SubjectMomentumPanel
import com.deniz0706.ykstakip.ui.components.ExamComparisonPanel
import com.deniz0706.ykstakip.util.WeakTopicAnalyzer
import com.deniz0706.ykstakip.util.YksRankingCalculator
import com.deniz0706.ykstakip.util.RankingEstimate
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreenV2(exams: List<Exam>, obp: Double?, targetTyt: Float?, targetAyt: Float?) {
    var type by remember { mutableStateOf(ExamType.TYT) }
    var branchMode by remember { mutableStateOf(false) }
    var branch by remember { mutableStateOf("") }
    var metric by remember { mutableStateOf("net") }
    var range by remember { mutableStateOf("10") }
    var section by remember { mutableStateOf("general") }
    val listState = rememberLazyListState()
    LaunchedEffect(section, type) { listState.scrollToItem(0) }
    var field by remember { mutableStateOf(AytField.SAYISAL) }
    var showMetric by remember { mutableStateOf(false) }
    var showBranch by remember { mutableStateOf(false) }
    var showField by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val subjects = remember(type) { SubjectConfigs.subjectsFor(type).map { it.name } }
    val selectedBranch = branch.takeIf { it in subjects } ?: subjects.firstOrNull().orEmpty()
    val metrics = listOf("net" to "Net", "correct" to "Doğru", "wrong" to "Yanlış",
        "blank" to "Boş", "time" to "Süre", "mpq" to "Dakika/Soru")
    val all = remember(exams, type) { exams.filter { it.type == type }.sortedBy { it.date } }
    val series = remember(all, branchMode, selectedBranch, metric, range) {
        val raw = all.mapNotNull { exam ->
            val subject = if (branchMode) exam.subject(selectedBranch) else null
            val value = if (branchMode) {
                when (metric) {
                    "net" -> subject?.net?.toFloat()
                    "correct" -> subject?.correct?.toFloat()
                    "wrong" -> subject?.wrong?.toFloat()
                    "blank" -> subject?.blank?.toFloat()
                    "time" -> subject?.timeMinutes?.toFloat()
                    else -> subject?.minutesPerQuestion?.toFloat()
                }
            } else when (metric) {
                "net" -> exam.totalNet.toFloat()
                "correct" -> exam.totalCorrect.toFloat()
                "wrong" -> exam.totalWrong.toFloat()
                "blank" -> exam.totalBlank.toFloat()
                "time" -> exam.totalTimeMinutes.toFloat()
                else -> exam.overallMinutesPerQuestion?.toFloat()
            }
            value?.let { exam.date to it }
        }
        if (range == "all") raw else raw.takeLast(range.toInt())
    }
    val weak = remember(all, section) {
        if (section == "branches") WeakTopicAnalyzer.findWeakSpots(all, sampleSize = 5, top = 3)
        else emptyList()
    }
    val ranking = remember(exams, type, field, obp, section) {
        if (section != "ranking" || obp == null) emptyList() else if (type == ExamType.TYT)
            YksRankingCalculator.calculateTyt(exams, obp).map { it.estimate }
        else YksRankingCalculator.calculateAyt(exams, obp, field).map { it.estimate }
    }
    val nf = remember { NumberFormat.getIntegerInstance(Locale("tr", "TR")) }
    fun rank(v: Double): String = "~" + nf.format(v.toInt())
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("PERFORMANS ANALİZİ", color = colors.secondary, style = MaterialTheme.typography.labelLarge)
            Text("Deneme Analizi", color = colors.onBackground,
                fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("Grafik, branş gelişimi ve sıralamanı ayrı ayrı keşfet.",
                color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(ExamType.TYT, ExamType.AYT).forEach { t ->
                    FilterChip(selected = type == t, onClick = { type = t; branch = "" },
                        label = { Text(t.name) }, modifier = Modifier.weight(1f))
                }
            }
        }
        item(key = "analytics-categories") {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf("general" to "Genel", "branches" to "Branş", "ranking" to "Sıralama")
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
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DashboardMetric("Deneme", "${all.size}", Modifier.weight(1f), "Toplam kayıt")
                DashboardMetric("Ortalama net",
                    all.takeIf { it.isNotEmpty() }?.map { it.totalNet }?.average()
                        ?.let { "%.1f".format(Locale("tr", "TR"), it) } ?: "—",
                    Modifier.weight(1f), "Tüm denemeler", colors.primary)
            }
        }
        if (section == "general") item {
            DashboardPanel(Modifier.fillMaxWidth(), accent = true) {
                val last = all.lastOrNull()
                val maxQuestions = if (type == ExamType.TYT) 120f else 80f
                val correct = last?.totalCorrect ?: 0
                val wrong = last?.totalWrong ?: 0
                val blank = last?.totalBlank ?: 0
                Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    DashboardRing(correct / maxQuestions,
                        "${(correct / maxQuestions * 100).toInt()}%", caption = "doğru")
                    Column(Modifier.weight(1f)) {
                        Text("SON DENEME DAĞILIMI", color = colors.secondary,
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(if (last == null) "İlk denemeni bekliyor"
                            else "${last.totalNet} net",
                            color = colors.onSurface,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold)
                        Text(if (last == null) "Deneme ekleyince halka dolacak."
                            else "$correct doğru · $wrong yanlış · $blank boş",
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(14.dp))
                DashboardProgress(correct / maxQuestions, Modifier.fillMaxWidth())
                Spacer(Modifier.height(7.dp))
                Text("Son denemedeki doğru oranı · ${maxQuestions.toInt()} soru üzerinden",
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
            }
        }
        if (section == "general") item {
            DashboardPanel(Modifier.fillMaxWidth()) {
                Text("Net ve performans trendi", color = colors.onSurface,
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !branchMode, onClick = { branchMode = false }, label = { Text("Toplam") })
                    FilterChip(selected = branchMode, onClick = { branchMode = true }, label = { Text("Branş") })
                }
                if (branchMode) {
                    Box {
                        OutlinedButton(onClick = { showBranch = true }) { Text("Branş: $selectedBranch") }
                        DropdownMenu(expanded = showBranch, onDismissRequest = { showBranch = false }) {
                            subjects.forEach { name ->
                                DropdownMenuItem(text = { Text(name) }, onClick = { branch = name; showBranch = false })
                            }
                        }
                    }
                }
                Box {
                    OutlinedButton(onClick = { showMetric = true }) {
                        Text("Metrik: ${metrics.first { it.first == metric }.second}")
                    }
                    DropdownMenu(expanded = showMetric, onDismissRequest = { showMetric = false }) {
                        metrics.forEach { (key, label) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = { metric = key; showMetric = false })
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("5", "10", "20", "all").forEach { r ->
                        FilterChip(selected = range == r, onClick = { range = r },
                            label = { Text(if (r == "all") "Tümü" else r) })
                    }
                }
                if (series.isEmpty()) {
                    Text("Bu seçim için deneme kaydı bulunamadı.", color = colors.onSurfaceVariant)
                } else {
                    val target = if (!branchMode && metric == "net")
                        if (type == ExamType.TYT) targetTyt else targetAyt else null
                    StatsTrend(series, target)
                    Spacer(Modifier.height(12.dp))
                    val values = series.map { it.second }
                    val suffix = if (metric == "time") " dk" else if (metric == "mpq") " dk/soru" else ""
                    val decimals = if (metric == "mpq") 2 else 1
                    fun fmt(v: Float) = "%.${decimals}f".format(Locale.US, v) + suffix
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text("Son", color = colors.onSurfaceVariant); Text(fmt(values.last()), fontWeight = FontWeight.Bold) }
                        Column { Text("Ortalama", color = colors.onSurfaceVariant); Text(fmt(values.average().toFloat()), fontWeight = FontWeight.Bold) }
                        Column { Text("En yüksek", color = colors.onSurfaceVariant); Text(fmt(values.max()), fontWeight = FontWeight.Bold) }
                        Column { Text("En düşük", color = colors.onSurfaceVariant); Text(fmt(values.min()), fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
        if (section == "general") item(key = "exam-duel") {
            ExamComparisonPanel(all, Modifier.fillMaxWidth())
        }
        if (section == "branches") item(key = "subject-momentum") {
            SubjectMomentumPanel(all, Modifier.fillMaxWidth())
        }
        if (section == "ranking") item {
            DashboardPanel(Modifier.fillMaxWidth()) {
                Text("Sıralama tahmini", color = colors.onSurface,
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("Tahmini değerlerdir; gerçek YKS sıralaması farklı olabilir.",
                    color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                if (type == ExamType.AYT) {
                    Box {
                        OutlinedButton(onClick = { showField = true }) {
                            Text(when (field) {
                                AytField.SAYISAL -> "Sayısal"
                                AytField.ESIT_AGIRLIK -> "Eşit Ağırlık"
                                AytField.SOZEL -> "Sözel"
                            })
                        }
                        DropdownMenu(expanded = showField, onDismissRequest = { showField = false }) {
                            listOf(AytField.SAYISAL to "Sayısal", AytField.ESIT_AGIRLIK to "Eşit Ağırlık",
                                AytField.SOZEL to "Sözel").forEach { (f, label) ->
                                DropdownMenuItem(text = { Text(label) }, onClick = { field = f; showField = false })
                            }
                        }
                    }
                }
                if (obp == null) {
                    Text("Sıralama için Ayarlar ekranından OBP gir.", color = colors.onSurfaceVariant)
                } else if (ranking.isEmpty()) {
                    Text("Hesaplanabilecek uygun deneme bulunamadı.", color = colors.onSurfaceVariant)
                } else {
                    val numbers = ranking.map { it.center.toFloat() }
                    StatsTrend(numbers.mapIndexed { index, n -> "Deneme ${index + 1}" to -n }, null, rankingMode = true)
                    Spacer(Modifier.height(12.dp))
                    Text("Son tahmin: ${rank(ranking.last().center.toDouble())}", fontWeight = FontWeight.Bold,
                        color = colors.primary, fontSize = 22.sp)
                    Text("Son 7 ortalama: ${rank(ranking.takeLast(7).map { it.center }.average())}", color = colors.onSurface)
                    Text("Son 30 ortalama: ${rank(ranking.takeLast(30).map { it.center }.average())}", color = colors.onSurface)
                    Text("Genel ortalama: ${rank(ranking.map { it.center }.average())}", color = colors.onSurface)
                    Text("En iyi: ${rank(ranking.minOf { it.center }.toDouble())}", color = colors.onSurface)
                    Text("En kötü: ${rank(ranking.maxOf { it.center }.toDouble())}", color = colors.onSurface)
                }
            }
        }
        if (section == "branches" && weak.isNotEmpty()) item {
            DashboardPanel(Modifier.fillMaxWidth()) {
                Text("Tekrar gerektiren konular", color = colors.onSurface,
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                weak.forEach { spot ->
                    Text("${spot.subject} · ${spot.topic}", color = colors.onSurface,
                        fontWeight = FontWeight.Medium)
                    Text("Son ${spot.outOf} denemede ${spot.count} kez",
                        color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun StatsTrend(
    values: List<Pair<String, Float>>,
    target: Float?,
    rankingMode: Boolean = false
) {
    val colors = MaterialTheme.colorScheme
    val plotted = remember(values) {
        if (values.size <= 80) values
        else {
            val step = (values.size + 79) / 80
            values.chunked(step).map { chunk ->
                chunk.last().first to chunk.map { it.second }.average().toFloat()
            }
        }
    }
    if (plotted.isEmpty()) return
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(plotted) {
        reveal.snapTo(0f)
        reveal.animateTo(1f, tween(950))
    }

    var selected by remember(plotted) { mutableIntStateOf(plotted.lastIndex) }
    val min = minOf(plotted.minOf { it.second }, target ?: Float.POSITIVE_INFINITY)
    val max = maxOf(plotted.maxOf { it.second }, target ?: Float.NEGATIVE_INFINITY)
    val spread = (max - min).coerceAtLeast(1f)
    val selectedPoint = plotted[selected.coerceIn(0, plotted.lastIndex)]
    val animatedIndex by animateFloatAsState(
        targetValue = selected.coerceIn(0, plotted.lastIndex).toFloat(),
        animationSpec = spring(stiffness = 500f, dampingRatio = .85f),
        label = "trend-cursor"
    )
    val selectedValue = if (rankingMode) -selectedPoint.second else selectedPoint.second
    val formatted = if (rankingMode) {
        "~" + NumberFormat.getIntegerInstance(Locale("tr", "TR")).format(selectedValue.toLong())
    } else "%.2f".format(Locale("tr", "TR"), selectedValue)

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(selectedPoint.first, style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant)
        Text(formatted, style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold, color = colors.primary)
    }
    Spacer(Modifier.height(8.dp))

    fun indexFor(x: Float, width: Float): Int =
        if (plotted.size == 1 || width <= 0f) 0
        else ((x / width).coerceIn(0f, 1f) * plotted.lastIndex).toInt().coerceIn(0, plotted.lastIndex)

    Box(
        Modifier.fillMaxWidth().height(210.dp)
            .pointerInput(plotted) {
                detectTapGestures { offset ->
                    selected = indexFor(offset.x, size.width.toFloat())
                }
            }
            .pointerInput(plotted) {
                detectDragGestures(
                    onDragStart = { selected = indexFor(it.x, size.width.toFloat()) },
                    onDrag = { change, _ ->
                        selected = indexFor(change.position.x, size.width.toFloat())
                        change.consume()
                    }
                )
            }
            .drawWithCache {
                fun pos(i: Int, v: Float): Offset {
                    val x = if (plotted.size == 1) size.width / 2f
                        else size.width * i / plotted.lastIndex
                    val y = size.height * (1f - (v - min) / spread) * .85f +
                        size.height * .075f
                    return Offset(x, y)
                }
                val line = Path()
                plotted.forEachIndexed { index, pair ->
                    val point = pos(index, pair.second)
                    if (index == 0) line.moveTo(point.x, point.y)
                    else line.lineTo(point.x, point.y)
                }
                val area = Path().apply {
                    addPath(line)
                    lineTo(pos(plotted.lastIndex, plotted.last().second).x, size.height)
                    lineTo(pos(0, plotted.first().second).x, size.height)
                    close()
                }
                val areaBrush = Brush.verticalGradient(
                    listOf(colors.primary.copy(alpha = .23f), colors.primary.copy(alpha = 0f)))
                val targetY = target?.let { pos(0, it).y }
                val stroke = Stroke(width = 3.dp.toPx())
                val guideWidth = 1.dp.toPx()
                val targetWidth = 2.dp.toPx()
                val outerRadius = 6.dp.toPx()
                val innerRadius = 2.5.dp.toPx()
                onDrawBehind {
                    if (targetY != null) {
                        drawLine(colors.secondary.copy(alpha = .65f),
                            Offset(0f, targetY), Offset(size.width, targetY),
                            strokeWidth = targetWidth)
                    }
                    val revealX = size.width * reveal.value
                    clipRect(left = 0f, top = 0f, right = revealX, bottom = size.height) {
                        drawPath(area, areaBrush)
                        drawPath(line, colors.primary, style = stroke)
                    }
                    val left = animatedIndex.toInt().coerceIn(0, plotted.lastIndex)
                    val right = (left + 1).coerceAtMost(plotted.lastIndex)
                    val fraction = (animatedIndex - left).coerceIn(0f, 1f)
                    val a = pos(left, plotted[left].second)
                    val b = pos(right, plotted[right].second)
                    val point = Offset(a.x + (b.x - a.x) * fraction,
                        a.y + (b.y - a.y) * fraction)
                    if (point.x <= revealX + 1f) {
                        drawLine(colors.outline, Offset(point.x, 0f),
                            Offset(point.x, size.height), strokeWidth = guideWidth)
                        drawCircle(colors.primary, radius = outerRadius, center = point)
                        drawCircle(colors.surface, radius = innerRadius, center = point)
                    }
                }
            }
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(plotted.first().first, color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall)
        Text(plotted.last().first, color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall)
    }
}
