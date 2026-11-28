package com.deniz0706.ykstakip.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.deniz0706.ykstakip.data.StudyRepository
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import com.deniz0706.ykstakip.ui.theme.*
import com.deniz0706.ykstakip.util.Fmt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val GainColor = Color(0xFF3DDC97)

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
    val context = LocalContext.current
    val todayMinutes by produceState(0, todayQuestions) {
        value = withContext(Dispatchers.IO) {
            runCatching { StudyRepository(context).get(java.time.LocalDate.now().toString())?.studyMinutes ?: 0 }.getOrDefault(0)
        }
    }
    val tytExams = remember(exams) { exams.filter { it.type == ExamType.TYT }.sortedBy { it.date } }
    val aytExams = remember(exams) { exams.filter { it.type == ExamType.AYT }.sortedBy { it.date } }

    BoxWithConstraints(Modifier.fillMaxSize().background(c.background)) {
        val tablet = maxWidth >= 840.dp
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (tablet) 2 else 1),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = if (tablet) 28.dp else YksTokens.Space.pageHorizontal, vertical = YksTokens.Space.pageVertical),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(YksTokens.Space.section + 4.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) { ClayCountdownPill(daysLeft) }
            item { ClayGaugeCard(todayQuestions, dailyGoal, todayMinutes, hasFocusSession, onOpenStudy) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    ClayNetCard("TYT Netleri", tytExams, targetTyt, c.primary, Modifier.weight(1f))
                    ClayNetCard("AYT Netleri", aytExams, targetAyt, c.tertiary, Modifier.weight(1f))
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ClayPill("+ Soru Ekle", onOpenStudy, Modifier.weight(1f))
                    ClayPill("+ Deneme Gir", { onNewExam(ExamType.TYT) }, Modifier.weight(1f))
                    ClayPill("+ Hata Kaydet", { onOpenLearning("TYT", "") }, Modifier.weight(1f))
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                ClayNextStepsCard(reviewAlertCount, learningDoneCount, learningTotalCount, aiCoachAdvice, aiCoachDate, onOpenLearning, onOpenAiCoach)
            }
            item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ClayCountdownPill(daysLeft: Long?) {
    val c = MaterialTheme.colorScheme
    val progress by animateFloatAsState(ClayHomeMath.countdownFraction(daysLeft), tween(YksMotion.progressMs), label = "countdown")
    val title = if (daysLeft != null && daysLeft >= 0L) "YKS'ye $daysLeft Gün" else "YKS Takip"
    Column(Modifier.fillMaxWidth().clayRaised(RoundedCornerShape(YksTokens.Shape.pill), 8.dp).padding(horizontal = 24.dp, vertical = 14.dp)
        .semantics(mergeDescendants = true) { contentDescription = title },
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, Modifier.align(Alignment.CenterHorizontally), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = c.onSurface)
        ClayProgressTrack(progress, c.primary)
    }
}

@Composable
private fun ClayProgressTrack(progress: Float, color: Color) {
    Box(Modifier.fillMaxWidth().height(10.dp).clayInset()) {
        if (progress > 0f) Box(Modifier.fillMaxWidth(progress.coerceIn(0.04f, 1f)).height(10.dp).clip(RoundedCornerShape(YksTokens.Shape.pill))
            .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.7f), color))))
    }
}

@Composable
private fun ClayGaugeCard(todayQuestions: Int, dailyGoal: Int?, todayMinutes: Int, hasFocusSession: Boolean, onOpenStudy: () -> Unit) {
    val c = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(36.dp)
    val questions by animateFloatAsState(ClayHomeMath.fraction(todayQuestions, dailyGoal), tween(YksMotion.progressMs), label = "questions")
    val study by animateFloatAsState(ClayHomeMath.studyFraction(todayMinutes), tween(YksMotion.progressMs), label = "study")
    val questionLabel = if (dailyGoal != null && dailyGoal > 0) "$todayQuestions/$dailyGoal Soru" else "$todayQuestions Soru"
    val timeLabel = ClayHomeMath.formatStudyTime(todayMinutes)
    Column(Modifier.fillMaxWidth().clayRaised(shape, 12.dp).clip(shape).clickable(onClick = onOpenStudy).padding(20.dp)
        .semantics(mergeDescendants = true) { contentDescription = "Bugün $questionLabel çözüldü, $timeLabel çalışıldı." },
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.fillMaxWidth(0.86f).aspectRatio(1f), contentAlignment = Alignment.Center) {
            ClayRing(questions, study, c.primary, c.secondary, Modifier.fillMaxSize())
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(questionLabel, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = c.primary, textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                Text("⏱ $timeLabel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = c.secondary)
            }
        }
        Text(if (hasFocusSession) "Odak seansına devam et →" else "Çalışmaya başla →", style = MaterialTheme.typography.labelLarge, color = c.onSurfaceVariant)
    }
}

@Composable
private fun ClayRing(outer: Float, inner: Float, outerColor: Color, innerColor: Color, modifier: Modifier = Modifier) {
    val track = MaterialTheme.colorScheme.surfaceDim
    Canvas(modifier) {
        val dim = size.minDimension
        val outerStroke = dim * 0.115f
        val innerStroke = dim * 0.085f
        val gap = dim * 0.05f
        fun ring(inset: Float, stroke: Float, progress: Float, color: Color) {
            val diameter = dim - inset * 2f
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)
            drawArc(track, -90f, 360f, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            if (progress > 0f) {
                drawArc(color.copy(alpha = 0.18f), -90f, 360f * progress, false, topLeft, arcSize, style = Stroke(stroke * 1.5f, cap = StrokeCap.Round))
                drawArc(color, -90f, 360f * progress, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
                drawArc(Color.White.copy(alpha = 0.22f), -90f, 360f * progress, false, topLeft, arcSize, style = Stroke(stroke * 0.3f, cap = StrokeCap.Round))
            }
        }
        ring(outerStroke / 2f, outerStroke, outer, outerColor)
        ring(outerStroke + gap + innerStroke / 2f, innerStroke, inner, innerColor)
    }
}

@Composable
private fun ClayNetCard(title: String, exams: List<Exam>, goal: Float?, accent: Color, modifier: Modifier = Modifier) {
    val c = MaterialTheme.colorScheme
    val latest = exams.lastOrNull()?.totalNet
    val previous = exams.getOrNull(exams.lastIndex - 1)?.totalNet
    val change = if (latest != null && previous != null) latest - previous else null
    val recent = remember(exams) { exams.takeLast(8).map { it.totalNet.toFloat() } }
    val average = remember(exams) { exams.map { it.totalNet }.takeIf { it.isNotEmpty() }?.average() }
    val best = remember(exams) { exams.maxOfOrNull { it.totalNet } }
    val reveal by animateFloatAsState(if (recent.size >= 2) 1f else 0f, tween(YksMotion.progressMs), label = "sparkline")
    Column(modifier.clayRaised(RoundedCornerShape(YksTokens.Shape.panel), 10.dp).padding(16.dp)
        .semantics(mergeDescendants = true) { contentDescription = "$title: ${Fmt.net(latest)}" + (change?.let { ", değişim ${Fmt.netWithSign(it)}" } ?: "") },
        verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = c.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(Fmt.net(latest), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = c.onSurface)
            if (change != null) Text(if (change >= 0) "↗" else "↘", color = if (change >= 0) GainColor else c.error, fontWeight = FontWeight.Bold)
        }
        if (recent.size >= 2) ClaySparkline(recent, reveal, accent, Modifier.fillMaxWidth().height(72.dp))
        else Text("Grafik için en az 2 deneme gerekli.", style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, modifier = Modifier.height(72.dp))
        if (exams.isNotEmpty()) Text("Ort. ${Fmt.net(average)} · Rekor ${Fmt.net(best)}", style = MaterialTheme.typography.labelSmall, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        goal?.let { Text("Hedef: $it", style = MaterialTheme.typography.labelSmall, color = accent) }
    }
}

@Composable
private fun ClaySparkline(values: List<Float>, reveal: Float, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        if (values.size < 2) return@Canvas
        val low = values.minOrNull() ?: 0f
        val high = values.maxOrNull() ?: 1f
        val spread = (high - low).coerceAtLeast(1f)
        val inset = 6.dp.toPx()
        val width = (size.width - inset * 2).coerceAtLeast(1f)
        val height = (size.height - inset * 2).coerceAtLeast(1f)
        val points = values.mapIndexed { i, value -> Offset(inset + width * i / values.lastIndex.toFloat(), inset + height * (1f - (value - low) / spread)) }
        val curve = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 0 until points.lastIndex) {
                val a = points[i]; val b = points[i + 1]; val mid = (a.x + b.x) / 2f
                cubicTo(mid, a.y, mid, b.y, b.x, b.y)
            }
        }
        val area = Path().apply { addPath(curve); lineTo(points.last().x, size.height); lineTo(points.first().x, size.height); close() }
        clipRect(right = inset + width * reveal) {
            drawPath(area, Brush.verticalGradient(listOf(color.copy(alpha = 0.35f), color.copy(alpha = 0.02f)), endY = size.height))
            drawPath(curve, color, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
            drawCircle(color, 4.dp.toPx(), points.last())
        }
    }
}

@Composable
private fun ClayPill(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(YksTokens.Shape.pill)
    Box(modifier.clayRaised(shape, 6.dp).clip(shape).clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 14.dp), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = c.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ClayNextStepsCard(reviewAlertCount: Int, learningDoneCount: Int, learningTotalCount: Int, aiCoachAdvice: String, aiCoachDate: String,
    onOpenLearning: (String, String) -> Unit, onOpenAiCoach: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().clayRaised(RoundedCornerShape(YksTokens.Shape.panel), 10.dp).padding(YksTokens.Space.cardContent),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Sıradaki Adımlar", style = MaterialTheme.typography.labelLarge, color = c.onSurfaceVariant)
        if (reviewAlertCount > 0) ClayActionRow("↻", "Tekrar önerileri ($reviewAlertCount)", "$learningDoneCount/$learningTotalCount kazanım tamamlandı", c.secondary) { onOpenLearning("TYT", "") }
        ClayActionRow("✦", "AI Koç ile konuş", if (aiCoachAdvice.isBlank()) "Denemelerinize göre kişisel öneri al." else aiCoachAdvice, c.primary, onOpenAiCoach)
        if (aiCoachAdvice.isNotBlank() && aiCoachDate.isNotBlank()) Text("Son değerlendirme · $aiCoachDate", style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
    }
}

@Composable
private fun ClayActionRow(glyph: String, title: String, subtitle: String, accent: Color, onClick: () -> Unit) {
    val c = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(YksTokens.Shape.card)
    Row(Modifier.fillMaxWidth().clayInset(shape).clip(shape).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(glyph, style = MaterialTheme.typography.titleLarge, color = accent)
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = c.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}
