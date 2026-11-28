package com.deniz0706.ykstakip.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
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

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(c.background)) {
        val tabletLayout = maxWidth >= 840.dp
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (tabletLayout) 2 else 1),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = if (tabletLayout) 28.dp else YksTokens.Space.pageHorizontal,
                vertical = YksTokens.Space.pageVertical
            ),
            horizontalArrangement = Arrangement.spacedBy(if (tabletLayout) 20.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(YksTokens.Space.section)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
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
                PremiumTodayCard(
                    todayQuestions = todayQuestions,
                    dailyGoal = dailyGoal,
                    onOpenStudy = onOpenStudy,
                    hasFocusSession = hasFocusSession
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                PremiumActionRow(
                    onNewExam = { onNewExam(type) },
                    onOpenStudy = onOpenStudy,
                    onOpenAiCoach = onOpenAiCoach,
                    hasFocusSession = hasFocusSession
                )
            }
            item {
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
            item {
                PremiumPerformanceCard(
                    allCount = all.size,
                    average = average,
                    best = best,
                    change = change,
                    recentNets = all.takeLast(8).map { it.totalNet.toFloat() }
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
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
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(YksTokens.Shape.panel))
            .background(c.surfaceContainer)
            .padding(YksTokens.Space.cardContent),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DailyGoalRing(
            questions = todayQuestions,
            goal = dailyGoal
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Bugünkü ilerleme",
                style = MaterialTheme.typography.labelLarge,
                color = c.onSurface
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = if (dailyGoal != null && dailyGoal > 0) "$todayQuestions / $dailyGoal soru" else "$todayQuestions soru",
                style = MaterialTheme.typography.bodyMedium,
                color = c.onSurfaceVariant
            )
            if (dailyGoal == null || dailyGoal <= 0) {
                Text(
                    text = "Hedef ayarlanmadı",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.onSurfaceVariant
                )
            }
        }
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(YksTokens.Shape.control))
                .clickable(onClick = onOpenStudy)
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (hasFocusSession) "Devam" else "Başlat",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = c.primary
            )
            Text(
                text = "Çalışma →",
                style = MaterialTheme.typography.labelSmall,
                color = c.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DailyGoalRing(
    questions: Int,
    goal: Int?
) {
    val c = MaterialTheme.colorScheme
    val progress = if (goal != null && goal > 0) {
        (questions.coerceAtLeast(0).toFloat() / goal.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = YksMotion.standardMs),
        label = "daily-goal-ring"
    )
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.width(88.dp).height(88.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 9.dp.toPx()
            drawArc(
                color = c.outlineVariant,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            if (animatedProgress > 0f) {
                drawArc(
                    color = c.primary,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (goal != null && goal > 0) (progress * 100).toInt().toString() + "%" else "—",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = c.onSurface
            )
            Text(
                text = "HEDEF",
                style = MaterialTheme.typography.labelSmall,
                color = c.onSurfaceVariant
            )
        }
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
    change: Double?,
    recentNets: List<Float>
) {
    val c = MaterialTheme.colorScheme
    val chartReveal by animateFloatAsState(
        targetValue = if (recentNets.size >= 2) 1f else 0f,
        animationSpec = tween(durationMillis = YksMotion.standardMs),
        label = "performance-chart-reveal"
    )
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
            Spacer(modifier = Modifier.height(12.dp))
            if (recentNets.size >= 2) {
                SmoothNetTrendChart(
                    values = recentNets,
                    reveal = chartReveal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp)
                        .semantics {
                            contentDescription = "Son ${recentNets.size} denemenin net grafiği. En düşük net: ${recentNets.minOrNull()}, en yüksek net: ${recentNets.maxOrNull()}."
                        }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Son ${recentNets.size} deneme · En düşük ${Fmt.net(recentNets.minOrNull()?.toDouble())} · En yüksek ${Fmt.net(recentNets.maxOrNull()?.toDouble())}",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Grafik için en az 2 deneme kaydı gerekli.",
                    style = MaterialTheme.typography.bodySmall,
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
private fun SmoothNetTrendChart(
    values: List<Float>,
    reveal: Float,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme
    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas
        val low = values.minOrNull() ?: 0f
        val high = values.maxOrNull() ?: 1f
        val spread = (high - low).coerceAtLeast(1f)
        val inset = 8.dp.toPx()
        val chartWidth = (size.width - inset * 2).coerceAtLeast(1f)
        val chartHeight = (size.height - inset * 2).coerceAtLeast(1f)
        val points = values.mapIndexed { index, value ->
            val x = inset + chartWidth * index / values.lastIndex.toFloat()
            val y = inset + chartHeight * (1f - (value - low) / spread)
            androidx.compose.ui.geometry.Offset(x, y)
        }
        for (fraction in listOf(0.25f, 0.5f, 0.75f)) {
            val y = inset + chartHeight * fraction
            drawLine(
                color = c.outlineVariant.copy(alpha = 0.55f),
                start = androidx.compose.ui.geometry.Offset(inset, y),
                end = androidx.compose.ui.geometry.Offset(size.width - inset, y),
                strokeWidth = 1.dp.toPx()
            )
        }
        val curve = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (index in 0 until points.lastIndex) {
                val current = points[index]
                val next = points[index + 1]
                val controlX = (current.x + next.x) / 2f
                cubicTo(controlX, current.y, controlX, next.y, next.x, next.y)
            }
        }
        val area = Path().apply {
            addPath(curve)
            lineTo(points.last().x, size.height - inset)
            lineTo(points.first().x, size.height - inset)
            close()
        }
        // Reveal the filled area and curve from left to right instead of only fading them in.
        clipRect(right = inset + chartWidth * reveal) {
            drawPath(
                path = area,
                brush = Brush.verticalGradient(
                    colors = listOf(c.primary.copy(alpha = 0.20f), c.primary.copy(alpha = 0.01f)),
                    startY = inset,
                    endY = size.height - inset
                )
            )
            drawPath(
                path = curve,
                color = c.primary,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            val last = points.last()
            drawCircle(color = c.primary, radius = 4.dp.toPx(), center = last)
            drawCircle(color = c.surface, radius = 1.8.dp.toPx(), center = last)
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