package com.deniz0706.ykstakip.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.ui.components.*
import com.deniz0706.ykstakip.util.Fmt

@Composable
fun HomeScreenV2(
    exams: List<Exam>,
    onNewExam: (ExamType) -> Unit,
    onOpenStudy: () -> Unit,
    onOpenLearning: (String, String) -> Unit,
    learningAlertTitle: String? = null,
    learningAlertType: String = "TYT",
    learningAlertSubject: String = "",
    reviewAlertCount: Int = 0,
    learningDoneCount: Int = 0,
    learningTotalCount: Int = 0,
    modifier: Modifier = Modifier,
    hasFocusSession: Boolean = false,
    daysLeft: Long? = null,
    todayQuestions: Int = 0,
    dailyGoal: Int? = null,
    targetTyt: Float? = null,
    targetAyt: Float? = null,
    daysLeftAyt: Long? = null
) {
    val c = MaterialTheme.colorScheme
    var type by remember { mutableStateOf(ExamType.TYT) }
    // The timeline follows exam attempts, not calendar days; weekly mock exams
    // should remain meaningful even when only one exam happened this week.
    var period by remember { mutableIntStateOf(1) }
    var showTarget by remember { mutableStateOf(true) }
    val all = remember(exams, type) {
        exams.filter { it.type == type }.sortedBy { it.date }
    }
    val shown = remember(all, period) {
        when (period) {
            0 -> all.takeLast(5)
            1 -> all.takeLast(10)
            2 -> all.takeLast(20)
            else -> all
        }
    }
    val shownDates = remember(shown) { shown.map { it.date } }
    val nets = remember(shown) { shown.map { it.totalNet } }
    val latest = all.lastOrNull()?.totalNet
    val previous = all.getOrNull(all.lastIndex - 1)?.totalNet
    val change = if (latest != null && previous != null) latest - previous else null
    val goal = if (type == ExamType.TYT) targetTyt else targetAyt
    val countdown = if (type == ExamType.TYT) daysLeft else daysLeftAyt
    val average = remember(all) { all.map { it.totalNet }.takeIf { it.isNotEmpty() }?.average() }
    val best = remember(all) { all.maxOfOrNull { it.totalNet } }
    val lastFive = remember(all) { all.takeLast(5).map { it.totalNet }.takeIf { it.isNotEmpty() }?.average() }

    LazyColumn(
        modifier.fillMaxSize().background(c.background),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("YKS / KONTROL MERKEZİ", style = MaterialTheme.typography.labelSmall,
                        color = c.secondary, letterSpacing = 1.8.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Genel Bakış", fontSize = 31.sp, fontWeight = FontWeight.Bold,
                        color = c.onBackground)
                }
                Surface(shape = RoundedCornerShape(15.dp), color = c.surfaceVariant) {
                    Text("${all.size} deneme", Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        color = c.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        item { ExamTypeTabs(type.name, { type = ExamType.valueOf(it) }) }
        if (learningAlertTitle != null) {
            item(key = "learning-alert") {
                DashboardPanel(Modifier.fillMaxWidth(), accent = true) {
                    Text("TEKRAR RADARI", color = c.secondary,
                        style = MaterialTheme.typography.labelSmall, letterSpacing = 1.1.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("Şu kazanıma dikkat et:", color = c.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium)
                    Text(learningAlertTitle, color = c.onSurface,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(5.dp))
                    Text("$reviewAlertCount tekrar önerisi · $learningDoneCount/$learningTotalCount kazanım tamamlandı",
                        color = c.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(onClick = {
                        onOpenLearning(learningAlertType, learningAlertSubject)
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Öğrenme Merkezinde incele →")
                    }
                }
            }
        }
        item {
            DashboardPanel(Modifier.fillMaxWidth(), accent = true) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("SON ${type.name} NETİ", color = c.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall, letterSpacing = 1.3.sp)
                        Spacer(Modifier.height(7.dp))
                        AnimatedContent(latest?.let(Fmt::net) ?: "—",
                            transitionSpec = { androidx.compose.animation.fadeIn(tween(220)) togetherWith androidx.compose.animation.fadeOut(tween(150)) },
                            label = "hero-net") { value ->
                            Text(value, fontSize = 52.sp, lineHeight = 60.sp,
                                fontWeight = FontWeight.Bold, color = c.onSurface)
                        }
                        Text(if (change == null) "Önceki deneme yok"
                            else "${if (change >= 0) "↗ +" else "↘ "}${Fmt.net(change)} net / önceki",
                            color = if (change != null && change >= 0) c.primary else c.secondary,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    if (goal != null && goal > 0f && latest != null) {
                        DashboardRing((latest / goal).toFloat(),
                            "${((latest / goal) * 100).toInt()}%",
                            caption = "hedef")
                    }
                }
                Spacer(Modifier.height(18.dp))
                HorizontalDivider(color = c.outline.copy(alpha = .4f))
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("NET TRENDİ", color = c.onSurface,
                        fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp,
                        style = MaterialTheme.typography.labelMedium)
                    Text("${shown.size} kayıt", color = c.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    listOf("Son 5", "Son 10", "Son 20", "Tümü").forEachIndexed { index, label ->
                        val active = period == index
                        Text(label, Modifier.weight(1f)
                            .background(if (active) c.secondary.copy(alpha = .17f)
                                else c.surfaceVariant.copy(alpha = .35f), RoundedCornerShape(12.dp))
                            .clickable { period = index }
                            .padding(vertical = 10.dp),
                            textAlign = TextAlign.Center,
                            color = if (active) c.secondary else c.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium)
                    }
                }
                Spacer(Modifier.height(14.dp))
                NetTrendChart(nets, shownDates, Modifier.fillMaxWidth(),
                    if (showTarget) goal else null)
                if (goal != null) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Hedef çizgisini göster", color = c.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall)
                        Switch(checked = showTarget, onCheckedChange = { showTarget = it })
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DashboardMetric("Ortalama", average?.let(Fmt::net) ?: "—",
                    Modifier.weight(1f), "Tüm denemeler", c.onSurface)
                DashboardMetric("En iyi", best?.let(Fmt::net) ?: "—",
                    Modifier.weight(1f), "Kişisel rekor", c.primary)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DashboardMetric("Son 5", lastFive?.let(Fmt::net) ?: "—",
                    Modifier.weight(1f), "Güncel ortalama")
                DashboardMetric("Sınava kalan", countdown?.let {
                    if (it >= 0) "$it" else "—"
                } ?: "—", Modifier.weight(1f), "gün")
            }
        }
        item(key = "exam-rhythm") {
            ExamRhythmPanel(all, Modifier.fillMaxWidth())
        }
        item {
            DashboardPanel(Modifier.fillMaxWidth()) {
                Text("PERFORMANS RADARI", color = c.secondary,
                    style = MaterialTheme.typography.labelSmall, letterSpacing = 1.3.sp)
                Spacer(Modifier.height(9.dp))
                val recent = all.takeLast(5)
                val older = all.dropLast(5).takeLast(5)
                val recentAvg = recent.takeIf { it.isNotEmpty() }?.map { it.totalNet }?.average()
                val olderAvg = older.takeIf { it.isNotEmpty() }?.map { it.totalNet }?.average()
                val trend = if (recentAvg != null && olderAvg != null) recentAvg - olderAvg else null
                Text(when {
                    recentAvg == null -> "İlk denemeni eklediğinde gelişim analizin burada görünecek."
                    trend == null -> "İlk ${recent.size} denemen kaydedildi. Birkaç deneme daha ekleyince dönemleri karşılaştırabileceksin."
                    trend > 0.5 -> "Son 5 denemede yükseliş var. Önceki 5 denemeye göre ortalaman arttı."
                    trend < -0.5 -> "Son 5 denemede ortalama geriledi. Zayıf branşlarını inceleyebilirsin."
                    else -> "Son 5 denemede performansın dengeli seyrediyor."
                }, color = c.onSurface, style = MaterialTheme.typography.bodyMedium)
                if (trend != null) {
                    Spacer(Modifier.height(13.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("ÖNCEKİ 5", color = c.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall)
                            Text(Fmt.net(olderAvg!!), color = c.onSurface,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold)
                        }
                        Text(if (trend >= 0) "↗ +${Fmt.net(trend)}" else "↘ ${Fmt.net(trend)}",
                            color = if (trend >= 0) c.primary else c.error,
                            fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Column(horizontalAlignment = Alignment.End) {
                            Text("SON 5", color = c.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall)
                            Text(Fmt.net(recentAvg!!), color = c.onSurface,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    DashboardProgress(
                        (recentAvg!! / if (type == ExamType.TYT) 120f else 80f).toFloat(),
                        Modifier.fillMaxWidth(),
                        foreground = if (trend >= 0) c.primary else c.tertiary
                    )
                }
            }
        }
        item {
            DashboardPanel(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("BUGÜNÜN HEDEFİ", style = MaterialTheme.typography.labelSmall,
                            color = c.secondary, letterSpacing = 1.sp)
                        Spacer(Modifier.height(7.dp))
                        Text("$todayQuestions soru", fontSize = 24.sp,
                            fontWeight = FontWeight.Bold, color = c.onSurface)
                        Text(dailyGoal?.let { "$it soru hedefi" } ?: "Hedefini Ayarlar'dan belirle",
                            color = c.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    if (dailyGoal != null && dailyGoal > 0)
                        DashboardRing(todayQuestions.toFloat() / dailyGoal,
                            "${(todayQuestions * 100L / dailyGoal)}%", caption = "bugün")
                }
                if (dailyGoal != null && dailyGoal > 0) {
                    Spacer(Modifier.height(14.dp))
                    DashboardProgress(todayQuestions.toFloat() / dailyGoal,
                        Modifier.fillMaxWidth())
                }
                Spacer(Modifier.height(13.dp))
                OutlinedButton(onClick = onOpenStudy,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)) {
                    Text(if (hasFocusSession) "◷  Odak seansına dön  ↗"
                        else "◷  Odak seansı aç  ↗")
                }
            }
        }
        item {
            Button(onClick = { onNewExam(type) },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(19.dp)) {
                Text("+  Yeni ${type.name} Denemesi", fontWeight = FontWeight.SemiBold)
            }
        }
        item { Spacer(Modifier.height(96.dp)) }
    }
}
