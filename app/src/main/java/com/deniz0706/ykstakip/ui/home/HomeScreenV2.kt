package com.deniz0706.ykstakip.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.domain.ExamAnalytics
import com.deniz0706.ykstakip.ui.components.DashboardPanel
import com.deniz0706.ykstakip.ui.components.DashboardProgress
import com.deniz0706.ykstakip.ui.components.DashboardRing
import com.deniz0706.ykstakip.ui.components.ExamRhythmPanel
import com.deniz0706.ykstakip.ui.components.ExamTypeTabs
import com.deniz0706.ykstakip.ui.components.NetTrendChart
import com.deniz0706.ykstakip.ui.components.YksActionRow
import com.deniz0706.ykstakip.ui.components.YksDeltaChip
import com.deniz0706.ykstakip.ui.components.YksSectionTitle
import com.deniz0706.ykstakip.ui.components.YksStatCell
import com.deniz0706.ykstakip.ui.theme.YksMotion
import com.deniz0706.ykstakip.ui.theme.YksShape
import com.deniz0706.ykstakip.ui.theme.YksSpacing
import com.deniz0706.ykstakip.ui.theme.YksText
import com.deniz0706.ykstakip.util.Fmt
import com.deniz0706.ykstakip.shared.SharedExamTrendCard

/**
 * Home, organised around one question: "what should I do today?"
 * Order: status (header, exam type) -> evidence (latest net + trend) -> action (add exam, today,
 * next steps) -> depth (performance, rhythm). Equal-weight stacked panels were replaced by this hierarchy.
 */
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
    val analytics = remember(all) { ExamAnalytics.summarize(all) }
    val nets = remember(shown) { ExamAnalytics.trend(shown) }
    val latest = analytics.latestNet
    val change = analytics.change
    val goal = if (type == ExamType.TYT) targetTyt else targetAyt
    val countdown = if (type == ExamType.TYT) daysLeft else daysLeftAyt
    val average = analytics.averageNet
    val best = analytics.bestNet
    val recentAvg = analytics.recentFiveAverage
    val olderAvg = analytics.previousFiveAverage

    LazyColumn(
        modifier.fillMaxSize().background(c.background),
        contentPadding = PaddingValues(horizontal = YksSpacing.pageHorizontal, vertical = YksSpacing.pageVertical),
        verticalArrangement = Arrangement.spacedBy(YksSpacing.section)
    ) {
        // 1. Status
        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Bugün", style = YksText.pageTitle, color = c.onSurface)
                    Text(
                        HomeInsights.subtitle(todayQuestions, dailyGoal),
                        style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant
                    )
                }
                if (countdown != null && countdown >= 0) {
                    Column(
                        Modifier.background(c.surfaceContainerHigh, RoundedCornerShape(YksShape.input))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("$countdown", style = YksText.smallNumber, color = c.onSurface)
                        Text("gün kaldı", style = YksText.caption, color = c.onSurfaceVariant)
                    }
                }
            }
        }
        item { ExamTypeTabs(type.name, { type = ExamType.valueOf(it) }) }

        // 2. Evidence: shared Compose Multiplatform exam trend component.
        item {
            SharedExamTrendCard(
                examType = type.name,
                latestNet = latest,
                change = change,
                goal = goal,
                goalProgressLabel = HomeInsights.goalPercent(latest, goal)?.let { "$it%" },
                trend = nets,
                trendCount = shown.size,
                showTarget = showTarget,
                onShowTargetChange = { showTarget = it }
            )
        }

        // 3. Action: the most frequent task first
        item {
            Button(
                onClick = { onNewExam(type) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(YksShape.card)
            ) {
                Text("+  Yeni ${type.name} denemesi ekle", fontWeight = FontWeight.SemiBold)
            }
        }
        item(key = "today-row") {
            Row(
                Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(YksSpacing.sm)
            ) {
                DashboardPanel(Modifier.weight(1f).fillMaxHeight()) {
                    Text("Bugünkü soru", style = YksText.caption, color = c.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Text("$todayQuestions", style = YksText.statNumber, color = c.onSurface)
                    Spacer(Modifier.height(10.dp))
                    if (dailyGoal != null && dailyGoal > 0) {
                        DashboardProgress(todayQuestions.toFloat() / dailyGoal, Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        Text("Hedef: $dailyGoal soru", style = YksText.caption, color = c.onSurfaceVariant)
                    } else {
                        Text("Hedefini Ayarlar'dan belirle", style = YksText.caption, color = c.onSurfaceVariant)
                    }
                }
                Box(
                    Modifier.weight(1f).fillMaxHeight()
                        .clip(RoundedCornerShape(YksShape.panel))
                        .clickable(onClick = onOpenStudy)
                ) {
                    DashboardPanel(Modifier.fillMaxSize()) {
                        Text("Odak seansı", style = YksText.caption, color = c.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (hasFocusSession) "Devam ediyor" else "Hazır",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (hasFocusSession) c.primary else c.onSurface
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            if (hasFocusSession) "Seansa dön  →" else "Başlat  →",
                            style = YksText.caption, color = c.primary
                        )
                    }
                }
            }
        }
        item(key = "next-steps") {
            DashboardPanel(Modifier.fillMaxWidth()) {
                YksSectionTitle("Sıradaki adımlar")
                Spacer(Modifier.height(4.dp))
                if (learningAlertTitle != null) {
                    YksActionRow(
                        glyph = "↻",
                        title = "Tekrar: $learningAlertTitle",
                        subtitle = "$reviewAlertCount tekrar önerisi · $learningDoneCount/$learningTotalCount kazanım tamamlandı",
                        accent = c.secondary,
                        onClick = { onOpenLearning(learningAlertType, learningAlertSubject) }
                    )
                    HorizontalDivider(color = c.outlineVariant.copy(alpha = .5f))
                }
                YksActionRow(
                    glyph = "✦",
                    title = "AI koçuyla konuş",
                    subtitle = if (aiCoachAdvice.isBlank()) "Denemelerine ve çalışma düzenine göre kişisel öneri al."
                        else aiCoachAdvice,
                    accent = c.primary,
                    onClick = onOpenAiCoach
                )
                if (aiCoachAdvice.isNotBlank() && aiCoachDate.isNotBlank()) {
                    Text("Son değerlendirme · $aiCoachDate", style = YksText.caption, color = c.onSurfaceVariant)
                }
            }
        }

        // 4. Depth
        item(key = "performance") {
            DashboardPanel(Modifier.fillMaxWidth()) {
                YksSectionTitle("Performans", trailing = "${all.size} deneme")
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(YksSpacing.md)) {
                    YksStatCell("Genel ortalama", Fmt.net(average), "tüm denemeler", Modifier.weight(1f))
                    YksStatCell("Kişisel rekor", Fmt.net(best), "en yüksek net", Modifier.weight(1f), valueColor = c.primary)
                }
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(YksSpacing.md)) {
                    YksStatCell("Son 5 ortalama", Fmt.net(recentAvg), "güncel seviye", Modifier.weight(1f), valueColor = c.secondary)
                    YksStatCell("Önceki 5", Fmt.net(olderAvg), "karşılaştırma", Modifier.weight(1f))
                }
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = c.outlineVariant.copy(alpha = .55f))
                Spacer(Modifier.height(12.dp))
                Text(
                    HomeInsights.trendSentence(recentAvg, olderAvg, analytics.recentCount),
                    color = c.onSurface, style = MaterialTheme.typography.bodyMedium
                )
                if (recentAvg != null && olderAvg != null) {
                    val trend = recentAvg - olderAvg
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (trend >= 0) "↗ ${Fmt.netWithSign(trend)} net" else "↘ ${Fmt.netWithSign(trend)} net",
                        color = if (trend >= 0) c.primary else c.error,
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(8.dp))
                    DashboardProgress(
                        (recentAvg / if (type == ExamType.TYT) 120f else 80f).toFloat(),
                        Modifier.fillMaxWidth(),
                        foreground = if (trend >= 0) c.primary else c.tertiary
                    )
                }
            }
        }
        item(key = "exam-rhythm") {
            ExamRhythmPanel(all, Modifier.fillMaxWidth())
        }
        item { Spacer(Modifier.height(96.dp)) }
    }
}
