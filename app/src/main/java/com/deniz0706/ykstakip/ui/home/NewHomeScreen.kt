package com.deniz0706.ykstakip.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import com.deniz0706.ykstakip.ui.components.YksButton
import com.deniz0706.ykstakip.ui.components.YksSection
import com.deniz0706.ykstakip.ui.components.YksStatTile
import com.deniz0706.ykstakip.ui.theme.YksMotion
import com.deniz0706.ykstakip.ui.theme.YksShape
import com.deniz0706.ykstakip.ui.theme.YksSpacing
import com.deniz0706.ykstakip.ui.theme.YksText
import com.deniz0706.ykstakip.util.Fmt

@Composable
fun NewHomeScreen(
    exams: List<Exam>,
    onNewExam: (ExamType) -> Unit,
    onOpenStudy: () -> Unit,
    onOpenAiCoach: () -> Unit,
    onOpenLearning: (String, String) -> Unit,
    aiCoachAdvice: String = "",
    aiCoachDate: String = "",
    learningAlertTitle: String? = null,
    learningAlertType: String = "TYT",
    learningAlertSubject: String = "",
    reviewAlertCount: Int = 0,
    learningDoneCount: Int = 0,
    learningTotalCount: Int = 0,
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
    val nets = remember(shown) { shown.map { it.totalNet } }
    val latest = all.lastOrNull()?.totalNet
    val previous = all.getOrNull(all.lastIndex - 1)?.totalNet
    val change = if (latest != null && previous != null) latest - previous else null
    val goal = if (type == ExamType.TYT) targetTyt else targetAyt
    val countdown = if (type == ExamType.TYT) daysLeft else daysLeftAyt
    val average = remember(all) { all.map { it.totalNet }.takeIf { it.isNotEmpty() }?.average() }
    val best = remember(all) { all.maxOfOrNull { it.totalNet } }
    val recent = remember(all) { all.takeLast(5) }
    val older = remember(all) { all.dropLast(5).takeLast(5) }
    val recentAvg = remember(recent) { recent.map { it.totalNet }.takeIf { it.isNotEmpty() }?.average() }
    val olderAvg = remember(older) { older.takeIf { it.size == 5 }?.map { it.totalNet }?.average() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(c.background),
        contentPadding = PaddingValues(horizontal = YksSpacing.pageHorizontal, vertical = YksSpacing.pageVertical),
        verticalArrangement = Arrangement.spacedBy(YksSpacing.section)
    ) {
        // 1. Status header
        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Bugün",
                        style = YksText.pageTitle,
                        color = c.onSurface
                    )
                    Text(
                        text = "$todayQuestions soru · ${dailyGoal?.let { "hedef: $it" } ?: "hedef belirlenmedi"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.onSurfaceVariant
                    )
                }
                if (countdown != null && countdown >= 0) {
                    androidx.compose.foundation.layout.Box(
                        Modifier
                            .background(c.surfaceContainer, RoundedCornerShape(YksShape.input))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$countdown",
                            style = YksText.smallNumber,
                            color = c.onSurface
                        )
                        Text(
                            text = "gün kaldı",
                            style = YksText.caption,
                            color = c.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("TYT", "AYT").forEach { t ->
                    val active = type.label == t
                    androidx.compose.foundation.layout.Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(YksShape.control))
                            .background(if (active) c.primary else c.surfaceContainer)
                            .clickable { type = ExamType.valueOf(t) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = t,
                            color = if (active) c.onPrimary else c.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }

        // 2. Evidence: latest net + trend
        item {
            androidx.compose.foundation.layout.Column(Modifier.fillMaxWidth().padding(horizontal = YksSpacing.md)) {
                Text(
                    text = "Son ${type.name} Neti",
                    style = MaterialTheme.typography.labelLarge,
                    color = c.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                AnimatedContent(
                    targetState = Fmt.net(latest),
                    transitionSpec = { fadeIn(tween(YksMotion.standardMs)) togetherWith fadeOut(tween(YksMotion.quickMs)) },
                    label = "hero-net"
                ) { value ->
                    Text(
                        text = value,
                        style = YksText.heroNumber,
                        color = c.onSurface
                    )
                }
                Spacer(Modifier.height(8.dp))
                if (change != null) {
                    Text(
                        text = if (change >= 0) " ${Fmt.netWithSign(change)} net" else " ${Fmt.netWithSign(change)} net",
                        color = if (change >= 0) c.primary else c.error,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3. Action
        item {
            YksButton(
                text = "+ Yeni ${type.name} denemesi ekle",
                onClick = { onNewExam(type) }
            )
        }

        // 4. Today row
        item(key = "today-row") {
            Row(
                Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(YksSpacing.sm)
            ) {
                androidx.compose.foundation.layout.Column(
                    Modifier.weight(1f).fillMaxHeight()
                        .clip(RoundedCornerShape(YksShape.panel))
                        .background(c.surfaceContainer)
                        .padding(YksSpacing.cardContent)
                ) {
                    Text(
                        text = "Bugünkü soru",
                        style = YksText.caption,
                        color = c.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "$todayQuestions",
                        style = YksText.statNumber,
                        color = c.onSurface
                    )
                    Spacer(Modifier.height(10.dp))
                    if (dailyGoal != null && dailyGoal > 0) {
                        Text(
                            text = "Hedef: $dailyGoal soru",
                            style = YksText.caption,
                            color = c.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Hedefini Ayarlar'dan belirle",
                            style = YksText.caption,
                            color = c.onSurfaceVariant
                        )
                    }
                }
                androidx.compose.foundation.layout.Box(
                    Modifier.weight(1f).fillMaxHeight()
                        .clip(RoundedCornerShape(YksShape.panel))
                        .background(c.surfaceContainer)
                        .clickable(onClick = onOpenStudy)
                ) {
                    androidx.compose.foundation.layout.Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (hasFocusSession) "Devam ediyor" else "Hazır",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (hasFocusSession) c.primary else c.onSurface
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = if (hasFocusSession) "Seansa dön →" else "Başlat →",
                            style = YksText.caption,
                            color = c.primary
                        )
                    }
                }
            }
        }

        // 5. Next steps
        item(key = "next-steps") {
            androidx.compose.foundation.layout.Column(Modifier.fillMaxWidth().padding(horizontal = YksSpacing.md)) {
                YksSection(
                    title = "Sıradaki adımlar",
                    detail = "Öncelikli işlemleriniz"
                )
                Spacer(Modifier.height(4.dp))
                if (learningAlertTitle != null) {
                    YksActionRow(
                        glyph = "↻",
                        title = "Tekrar: $learningAlertTitle",
                        subtitle = "$reviewAlertCount tekrar önerisi · $learningDoneCount/$learningTotalCount kazanım tamamlandı",
                        accent = c.secondary,
                        onClick = { onOpenLearning(learningAlertType, learningAlertSubject) }
                    )
                    HorizontalDivider(color = c.outlineVariant.copy(alpha = 0.5f))
                }
                YksActionRow(
                    glyph = "✦",
                    title = "AI koçuyla konuş",
                    subtitle = if (aiCoachAdvice.isBlank()) "Denemelerinize ve çalışma düzeninize göre kişisel öneri al." else aiCoachAdvice,
                    accent = c.primary,
                    onClick = onOpenAiCoach
                )
                if (aiCoachAdvice.isNotBlank() && aiCoachDate.isNotBlank()) {
                    Text(
                        text = "Son değerlendirme · $aiCoachDate",
                        style = YksText.caption,
                        color = c.onSurfaceVariant
                    )
                }
            }
        }

        // 6. Performance
        item(key = "performance") {
            androidx.compose.foundation.layout.Column(Modifier.fillMaxWidth().padding(horizontal = YksSpacing.md)) {
                YksSection(title = "Performans", trailing = "${all.size} deneme")
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(YksSpacing.md)) {
                    YksStatTile(
                        label = "Genel ortalama",
                        value = Fmt.net(average),
                        detail = "tüm denemeler",
                        modifier = Modifier.weight(1f)
                    )
                    YksStatTile(
                        label = "Kişisel rekor",
                        value = Fmt.net(best),
                        detail = "en yüksek net",
                        valueColor = c.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(YksSpacing.md)) {
                    YksStatTile(
                        label = "Son 5 ortalama",
                        value = Fmt.net(recentAvg),
                        detail = "güncel seviye",
                        valueColor = c.secondary,
                        modifier = Modifier.weight(1f)
                    )
                    YksStatTile(
                        label = "Önceki 5",
                        value = Fmt.net(olderAvg),
                        detail = "karşılaştırma",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = c.outlineVariant.copy(alpha = 0.55f))
                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (recentAvg != null && olderAvg != null) {
                        val trend = recentAvg - olderAvg
                        if (trend >= 0) " ${Fmt.netWithSign(trend)} net gelişim" else " ${Fmt.netWithSign(trend)} net düşüş"
                    } else "Yeterli veri yok",
                    color = c.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item { Spacer(Modifier.height(96.dp)) }
    }
}

@Composable
private fun YksActionRow(
    glyph: String,
    title: String,
    subtitle: String,
    accent: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    val c = MaterialTheme.colorScheme
    androidx.compose.foundation.layout.Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(YksShape.control))
            .background(c.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(YksSpacing.md)
    ) {
        Text(
            text = glyph,
            style = MaterialTheme.typography.titleLarge,
            color = accent
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = c.onSurface,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = c.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
