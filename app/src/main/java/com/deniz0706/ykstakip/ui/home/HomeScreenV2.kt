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
import com.deniz0706.ykstakip.ui.theme.YksSpacing
import com.deniz0706.ykstakip.ui.theme.YksShape
import com.deniz0706.ykstakip.ui.theme.YksMotion

@Composable
fun HomeScreenV2(
    exams: List<Exam>,
    onNewExam: (ExamType) -> Unit,
    onOpenStudy: () -> Unit,
    onOpenAiCoach: () -> Unit,
    aiCoachAdvice: String = "",
    aiCoachDate: String = "",
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
        contentPadding = PaddingValues(horizontal = YksSpacing.pageHorizontal, vertical = YksSpacing.pageVertical),
        verticalArrangement = Arrangement.spacedBy(YksSpacing.section)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text("YKS 2027  /  KONTROL MERKEZİ",
                        style = MaterialTheme.typography.labelSmall, color = c.primary,
                        letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(5.dp))
                    Text("Genel Bakış", fontSize = 31.sp, lineHeight = 36.sp,
                        fontWeight = FontWeight.Bold, color = c.onSurface, letterSpacing = (-0.6).sp)
                    Text("Hazırlığını verilerle takip et.",
                        style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant)
                }
                Spacer(Modifier.width(12.dp))
                Column(
                    Modifier.background(c.surfaceContainerHigh, RoundedCornerShape(18.dp))
                        .padding(horizontal = 15.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(all.size.toString(), fontSize = 22.sp,
                        fontWeight = FontWeight.Bold, color = c.onSurface)
                    Text("DENEME", style = MaterialTheme.typography.labelSmall,
                        color = c.onSurfaceVariant, letterSpacing = .8.sp)
                }
            }
        }
        item {
            DashboardPanel(Modifier.fillMaxWidth(), accent = true) {
                Text("YAPAY ZEKÂ KOÇU", color = c.secondary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                Spacer(Modifier.height(5.dp))
                Text("Verilerini birlikte değerlendirelim.", color = c.onSurface, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                Text("Denemelerin, konu ilerlemen ve çalışma düzenin üzerinden kişisel öneriler al.", color = c.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                if (aiCoachAdvice.isNotBlank()) {
                    Spacer(Modifier.height(9.dp))
                    Text("SON KOÇ DEĞERLENDİRMESİ · $aiCoachDate", color = c.secondary, style = MaterialTheme.typography.labelSmall)
                    Text(aiCoachAdvice, color = c.onSurface, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(9.dp))
                OutlinedButton(onClick = onOpenAiCoach, modifier = Modifier.fillMaxWidth()) { Text("AI koçuyla konuş  ↗") }
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
                            transitionSpec = { androidx.compose.animation.fadeIn(tween(YksMotion.standardMs)) togetherWith androidx.compose.animation.fadeOut(tween(YksMotion.quickMs)) },
                            label = "hero-net") { value ->
                            Text(value, fontSize = 52.sp, lineHeight = 60.sp,
                                fontWeight = FontWeight.Bold, color = c.onSurface)
                        }
                        Surface(
                            shape = RoundedCornerShape(YksShape.pill),
                            color = when {
                                change == null -> c.surfaceVariant
                                change >= 0 -> c.primary.copy(alpha = .13f)
                                else -> c.error.copy(alpha = .13f)
                            }
                        ) {
                            Text(
                                if (change == null) "İlk deneme"
                                else "${if (change >= 0) "↗ +" else "↘ "}${Fmt.net(change)} net",
                                Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                color = when {
                                    change == null -> c.onSurfaceVariant
                                    change >= 0 -> c.primary
                                    else -> c.error
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
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
                                else c.surfaceVariant.copy(alpha = .35f), RoundedCornerShape(YksShape.control))
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
        item(key = "performance-snapshot") {
            DashboardPanel(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("GENEL ORTALAMA", color = c.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall, letterSpacing = .8.sp)
                        Spacer(Modifier.height(5.dp))
                        Text(average?.let(Fmt::net) ?: "—", color = c.onSurface,
                            fontSize = 25.sp, fontWeight = FontWeight.Bold)
                        Text("Tüm denemeler", color = c.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("KİŞİSEL REKOR", color = c.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall, letterSpacing = .8.sp)
                        Spacer(Modifier.height(5.dp))
                        Text(best?.let(Fmt::net) ?: "—", color = c.primary,
                            fontSize = 25.sp, fontWeight = FontWeight.Bold)
                        Text("En yüksek net", color = c.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(Modifier.height(13.dp))
                HorizontalDivider(color = c.outlineVariant.copy(alpha = .55f))
                Spacer(Modifier.height(13.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("SON 5 DENEME", color = c.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall, letterSpacing = .8.sp)
                        Spacer(Modifier.height(5.dp))
                        Text(lastFive?.let(Fmt::net) ?: "—", color = c.secondary,
                            fontSize = 23.sp, fontWeight = FontWeight.Bold)
                        Text("Güncel ortalama", color = c.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("SINAVA KALAN", color = c.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall, letterSpacing = .8.sp)
                        Spacer(Modifier.height(5.dp))
                        Text(countdown?.let { if (it >= 0) "$it" else "—" } ?: "—",
                            color = c.onSurface, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                        Text("gün", color = c.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
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
                shape = RoundedCornerShape(YksShape.card)) {
                Text("+  Yeni ${type.name} Denemesi", fontWeight = FontWeight.SemiBold)
            }
        }
        item { Spacer(Modifier.height(96.dp)) }
    }
}
