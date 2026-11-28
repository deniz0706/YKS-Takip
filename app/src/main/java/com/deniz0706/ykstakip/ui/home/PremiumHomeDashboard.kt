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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import com.deniz0706.ykstakip.ui.theme.YksMotion
import com.deniz0706.ykstakip.ui.theme.YksTokens
import com.deniz0706.ykstakip.util.Fmt

/** Premium home dashboard for YKS Takip 4.0. */
@Composable
fun PremiumHomeDashboard(
    exams: List<Exam>,
    onNewExam: (ExamType) -> Unit,
    onOpenStudy: () -> Unit,
    onOpenAiCoach: () -> Unit,
    onOpenLearning: (String, String) -> Unit,
    aiCoachAdvice: String = "",
    aiCoachDate: String = "",
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

    val all = remember(exams, type) {
        exams.filter { it.type == type }.sortedBy { it.date }
    }
    val latest = all.lastOrNull()?.totalNet
    val previous = all.getOrNull(all.lastIndex - 1)?.totalNet
    val change = if (latest != null && previous != null) latest - previous else null
    val goal = if (type == ExamType.TYT) targetTyt else targetAyt
    val countdown = if (type == ExamType.TYT) daysLeft else daysLeftAyt
    val average = remember(all) { all.map { it.totalNet }.takeIf { it.isNotEmpty() }?.average() }
    val best = remember(all) { all.maxOfOrNull { it.totalNet } }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(c.background),
        contentPadding = PaddingValues(
            horizontal = YksTokens.Space.pageHorizontal,
            vertical = YksTokens.Space.pageVertical
        ),
        verticalArrangement = Arrangement.spacedBy(YksTokens.Space.section)
    ) {
        item {
            PremiumHeader(
                todayQuestions = todayQuestions,
                dailyGoal = dailyGoal,
                countdown = countdown,
                type = type,
                onTypeChange = { type = ExamType.valueOf(it) }
            )
        }
        item {
            PremiumNetCard(
                latest = latest,
                change = change,
                goal = goal,
                type = type
            )
        }
        item {
            PremiumActionRow(
                onNewExam = { onNewExam(type) },
                onOpenStudy = onOpenStudy,
                onOpenAiCoach = onOpenAiCoach,
                hasFocusSession = hasFocusSession
            )
        }
        item(key = "today-focus") {
            PremiumTodayCard(
                todayQuestions = todayQuestions,
                dailyGoal = dailyGoal,
                onOpenStudy = onOpenStudy,
                hasFocusSession = hasFocusSession
            )
        }
        item(key = "next-steps") {
            PremiumNextStepsCard(
                reviewAlertCount = reviewAlertCount,
                learningDoneCount = learningDoneCount,
                learningTotalCount = learningTotalCount,
                aiCoachAdvice = aiCoachAdvice,
                aiCoachDate = aiCoachDate,
                onOpenLearning = onOpenLearning,
                onOpenAiCoach = onOpenAiCoach
            )
        }
        item(key = "performance") {
            PremiumPerformanceCard(
                allCount = all.size,
                average = average,
                best = best,
                change = change
            )
        }
        item { Spacer(modifier = Modifier.height(120.dp)) }
    }
}

@Composable
private fun PremiumHeader(
    todayQuestions: Int,
    dailyGoal: Int?,
    countdown: Long?,
    type: ExamType,
    onTypeChange: (String) -> Unit
) {
    val c = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Bugün",
                style = MaterialTheme.typography.headlineMedium,
                color = c.onSurface
            )
            val goalText = dailyGoal?.let { "Günlük hedef: $it soru" } ?: "Hedefi ayarlayın"
            Text(
                text = "$todayQuestions soru · $goalText",
                style = MaterialTheme.typography.bodyMedium,
                color = c.onSurfaceVariant
            )
        }
        if (countdown != null && countdown >= 0) {
            CountdownBox(countdown = countdown)
        }
        ExamTypeToggle(
            type = type,
            onTypeChange = onTypeChange
        )
    }
}

@Composable
private fun CountdownBox(countdown: Long) {
    val c = MaterialTheme.colorScheme
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .background(c.surfaceContainer, RoundedCornerShape(YksTokens.Shape.input))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$countdown",
                style = MaterialTheme.typography.headlineMedium,
                color = c.onSurface
            )
            Text(
                text = "gün kaldı",
                style = MaterialTheme.typography.bodySmall,
                color = c.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExamTypeToggle(
    type: ExamType,
    onTypeChange: (String) -> Unit
) {
    val c = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("TYT", "AYT").forEach { t ->
            val active = type.label == t
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(YksTokens.Shape.control))
                    .background(if (active) c.primary else c.surfaceContainer)
                    .clickable { onTypeChange(t) }
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

@Composable
private fun PremiumNetCard(
    latest: Double?,
    change: Double?,
    goal: Float?,
    type: ExamType
) {
    val c = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(YksTokens.Shape.panel),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = c.surface)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(YksTokens.Space.cardContent)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${type.name} Net Özeti",
                    style = MaterialTheme.typography.labelLarge,
                    color = c.onSurfaceVariant
                )
                goal?.let { g ->
                    Text(
                        text = "Hedef: $g",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedContent(
                targetState = latest ?: 0.0,
                transitionSpec = { fadeIn(tween(YksMotion.standardMs)) togetherWith fadeOut(tween(YksMotion.quickMs)) },
                label = "hero-net"
            ) { value ->
                Text(
                    text = Fmt.net(value),
                    style = MaterialTheme.typography.displayLarge,
                    color = c.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (change != null) {
                Text(
                    text = if (change >= 0) "↗ ${Fmt.netWithSign(change)} net" else "↘ ${Fmt.netWithSign(change)} net",
                    color = if (change >= 0) c.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PremiumActionRow(
    onNewExam: () -> Unit,
    onOpenStudy: () -> Unit,
    onOpenAiCoach: () -> Unit,
    hasFocusSession: Boolean
) {
    val c = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PremiumActionButton(
            text = "+ Yeni Deneme",
            icon = "📝",
            accent = c.primary,
            onClick = onNewExam,
            modifier = Modifier.weight(1f)
        )
        PremiumActionButton(
            text = if (hasFocusSession) "Seans Aktif" else "Çalışmaya Başla",
            icon = "▶️",
            accent = if (hasFocusSession) c.secondary else c.primary,
            onClick = onOpenStudy,
            modifier = Modifier.weight(1f)
        )
        PremiumActionButton(
            text = "AI Koç",
            icon = "🤖",
            accent = c.tertiary,
            onClick = onOpenAiCoach,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PremiumActionButton(
    text: String,
    icon: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .clip(RoundedCornerShape(YksTokens.Shape.input))
            .background(c.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, style = MaterialTheme.typography.titleLarge, color = accent)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = c.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PremiumTodayCard(
    todayQuestions: Int,
    dailyGoal: Int?,
    onOpenStudy: () -> Unit,
    hasFocusSession: Boolean
) {
    val c = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PremiumStatColumn(
            label = "Bugünkü Soru",
            value = todayQuestions.toString(),
            detail = if (dailyGoal != null && dailyGoal > 0) "Hedef: $dailyGoal" else "Hedef ayarlanmadı",
            modifier = Modifier.fillMaxHeight().weight(1f)
        )
        PremiumActionColumn(
            label = if (hasFocusSession) "Devam Ediyor" else "Hazır",
            actionText = if (hasFocusSession) "Seansa Dön →" else "Başlat →",
            accent = if (hasFocusSession) c.primary else c.onSurface,
            onClick = onOpenStudy,
            modifier = Modifier.fillMaxHeight().weight(1f)
        )
    }
}

@Composable
private fun PremiumStatColumn(
    label: String,
    value: String,
    detail: String,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(YksTokens.Shape.panel))
            .background(c.surfaceContainer)
            .padding(YksTokens.Space.cardContent)
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = value, style = MaterialTheme.typography.headlineMedium, color = c.onSurface)
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = detail, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
    }
}

@Composable
private fun PremiumActionColumn(
    label: String,
    actionText: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(YksTokens.Shape.panel))
            .background(c.surfaceContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = accent
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = actionText,
                style = MaterialTheme.typography.bodySmall,
                color = accent
            )
        }
    }
}

@Composable
private fun PremiumNextStepsCard(
    reviewAlertCount: Int,
    learningDoneCount: Int,
    learningTotalCount: Int,
    aiCoachAdvice: String,
    aiCoachDate: String,
    onOpenLearning: (String, String) -> Unit,
    onOpenAiCoach: () -> Unit
) {
    val c = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(YksTokens.Shape.panel),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = c.surface)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(YksTokens.Space.cardContent)
        ) {
            Text(
                text = "Sıradaki Adımlar",
                style = MaterialTheme.typography.labelLarge,
                color = c.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (reviewAlertCount > 0) {
                PremiumActionRowItem(
                    glyph = "↻",
                    title = "Tekrar önerileri ($reviewAlertCount)",
                    subtitle = "$learningDoneCount/$learningTotalCount kazanım tamamlandı",
                    accent = c.secondary,
                    onClick = { onOpenLearning("TYT", "") }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            PremiumActionRowItem(
                glyph = "✦",
                title = "AI Koç ile konuş",
                subtitle = if (aiCoachAdvice.isBlank()) "Denemelerinize göre kişisel öneri al." else aiCoachAdvice,
                accent = c.primary,
                onClick = onOpenAiCoach
            )
            if (aiCoachAdvice.isNotBlank() && aiCoachDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Son değerlendirme · $aiCoachDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PremiumActionRowItem(
    glyph: String,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit
) {
    val c = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(YksTokens.Shape.control))
            .background(c.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = glyph, style = MaterialTheme.typography.titleLarge, color = accent)
        Column(modifier = Modifier.weight(1f)) {
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

@Composable
private fun PremiumPerformanceCard(
    allCount: Int,
    average: Double?,
    best: Double?,
    change: Double?
) {
    val c = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(YksTokens.Shape.panel),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = c.surface)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(YksTokens.Space.cardContent)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Performans",
                    style = MaterialTheme.typography.labelLarge,
                    color = c.onSurfaceVariant
                )
                Text(
                    text = "$allCount deneme",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PremiumStatTile(
                    label = "Genel Ortalama",
                    value = Fmt.net(average),
                    detail = "tüm denemeler",
                    modifier = Modifier.weight(1f)
                )
                PremiumStatTile(
                    label = "Kişisel Rekor",
                    value = Fmt.net(best),
                    detail = "en yüksek net",
                    valueColor = c.primary,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (change != null) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (change >= 0) "↗ ${Fmt.netWithSign(change)} net gelişim" else "↘ ${Fmt.netWithSign(change)} net düşüş",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (change >= 0) c.primary else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumStatTile(
    label: String,
    value: String?,
    detail: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value ?: "—",
            style = MaterialTheme.typography.headlineMedium,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = detail, style = MaterialTheme.typography.labelSmall, color = c.onSurfaceVariant)
    }
}