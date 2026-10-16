package com.deniz0706.ykstakip.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.util.Fmt
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/**
 * Measured exam cadence. Dates are historical; no claim that a future exam is booked.
 */
@Composable
fun ExamRhythmPanel(exams: List<Exam>, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val sorted = remember(exams) {
        exams.mapNotNull { exam ->
            runCatching { LocalDate.parse(exam.date) }.getOrNull()?.let { it to exam }
        }.sortedBy { it.first }
    }
    val gaps = remember(sorted) {
        sorted.zipWithNext { a, b ->
            ChronoUnit.DAYS.between(a.first, b.first).toInt()
        }.filter { it > 0 }
    }
    val median = remember(gaps) {
        if (gaps.isEmpty()) null else gaps.sorted()[gaps.size / 2]
    }
    val daysSince = sorted.lastOrNull()?.first?.let {
        ChronoUnit.DAYS.between(it, LocalDate.now()).toInt().coerceAtLeast(0)
    }
    DashboardPanel(modifier, accent = false) {
        Row(Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("DENEME RİTMİ", color = colors.secondary,
                    style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                Spacer(Modifier.height(5.dp))
                Text("Deneme alışkanlığın", color = colors.onSurface,
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Text("◷", color = colors.primary, fontSize = 29.sp)
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f)
                .background(colors.surfaceVariant.copy(alpha = .55f),
                    RoundedCornerShape(16.dp))
                .padding(13.dp)) {
                Text("MEDYAN ARALIK", color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
                Text(median?.let { "$it gün" } ?: "—",
                    color = colors.onSurface, fontSize = 23.sp, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f)
                .background(colors.surfaceVariant.copy(alpha = .55f),
                    RoundedCornerShape(16.dp))
                .padding(13.dp)) {
                Text("SON DENEMEDEN", color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
                Text(daysSince?.let { "$it gün" } ?: "—",
                    color = colors.onSurface, fontSize = 23.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(11.dp))
        if (gaps.isNotEmpty()) {
            Text("DENEMELER ARASI GÜNLER", color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall, letterSpacing = .8.sp)
            Spacer(Modifier.height(7.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.Bottom) {
                val recent = gaps.takeLast(7)
                val longest = recent.maxOrNull()?.coerceAtLeast(1) ?: 1
                recent.forEach { gap ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.fillMaxWidth().height(30.dp)
                            .background(colors.surfaceVariant, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.BottomCenter) {
                            Box(Modifier.fillMaxWidth()
                                .fillMaxHeight(gap.toFloat() / longest)
                                .background(colors.tertiary, RoundedCornerShape(8.dp)))
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("$gap", color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Spacer(Modifier.height(7.dp))
            Text(
                if (median != null && median in 5..9)
                    "Son denemelerinin aralığı yaklaşık haftalık bir düzende."
                else "Bu aralıklar geçmiş denemelerinden hesaplandı; planlanmış sınav tarihi değildir.",
                color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall
            )
        } else {
            Text("Aralık analizi için farklı tarihlerde en az iki deneme ekle.",
                color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private data class SubjectPulse(
    val subject: String,
    val recentNet: Double,
    val previousNet: Double?,
    val maxQuestions: Int,
    val sampleSize: Int,
    val latestNet: Double
)

/** Relative branch strength from recent exam records, with comparison to previous attempts. */
@Composable
fun SubjectMomentumPanel(exams: List<Exam>, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val scores = remember(exams) {
        val recent = exams.takeLast(5)
        val older = exams.dropLast(5).takeLast(5)
        recent.flatMap { it.subjects }.map { it.subject }.distinct().mapNotNull { subject ->
            val last = recent.mapNotNull { it.subject(subject) }
            if (last.isEmpty()) null else {
                val previous = older.mapNotNull { it.subject(subject) }
                SubjectPulse(
                    subject = subject,
                    recentNet = last.map { it.net }.average(),
                    previousNet = previous.takeIf { it.isNotEmpty() }
                        ?.map { it.net }?.average(),
                    maxQuestions = last.maxOf { it.questionCount }.coerceAtLeast(1),
                    sampleSize = last.size,
                    latestNet = last.last().net
                )
            }
        }.sortedBy { it.recentNet / it.maxQuestions }
    }
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    if (scores.isEmpty()) return
    DashboardPanel(modifier) {
        Text("BRANŞ PUSULASI", color = colors.secondary,
            style = MaterialTheme.typography.labelSmall, letterSpacing = 1.3.sp)
        Spacer(Modifier.height(5.dp))
        Text("Güçlü ve gelişime açık alanların", color = colors.onSurface,
            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Son 5 denemenin net / soru oranı. Branşa dokunarak ayrıntıyı gör.",
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(14.dp))
        scores.forEach { score ->
            val active = selectedSubject == score.subject
            Row(Modifier.fillMaxWidth().clickable {
                selectedSubject = if (active) null else score.subject
            }.padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(score.subject, color = if (active) colors.primary else colors.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(7.dp))
                    DashboardProgress((score.recentNet / score.maxQuestions).toFloat(),
                        Modifier.fillMaxWidth(),
                        foreground = if (active) colors.secondary else colors.primary)
                }
                Spacer(Modifier.width(12.dp))
                Text(Fmt.net(score.recentNet), color = colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(4.dp))
                Text("›", color = colors.onSurfaceVariant)
            }
            if (active) {
                val difference = score.previousNet?.let { score.recentNet - it }
                Column(Modifier.fillMaxWidth()
                    .background(colors.surfaceVariant.copy(alpha = .55f),
                        RoundedCornerShape(14.dp))
                    .padding(12.dp)) {
                    Text("Son ${score.sampleSize} denemenin ortalaması: ${Fmt.net(score.recentNet)} net",
                        color = colors.onSurface, style = MaterialTheme.typography.bodySmall)
                    Text("Son deneme: ${Fmt.net(score.latestNet)} net",
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall)
                    Text(if (difference == null) "Önceki dönem verisi henüz yok."
                        else "Önceki 5 denemeye göre: ${if (difference >= 0) "+" else ""}${Fmt.net(difference)} net",
                        color = if (difference != null && difference < 0) colors.error
                            else colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
