package com.deniz0706.ykstakip.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.util.Fmt

/**
 * Compare any two recent exams and their subject-level net changes.
 * Uses existing stored results: no inferred rankings or invented targets.
 */
@Composable
fun ExamComparisonPanel(exams: List<Exam>, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    var olderId by remember(exams) {
        mutableLongStateOf(exams.getOrNull(exams.lastIndex - 1)?.id ?: -1L)
    }
    var newerId by remember(exams) {
        mutableLongStateOf(exams.lastOrNull()?.id ?: -1L)
    }
    var olderMenu by remember { mutableStateOf(false) }
    var newerMenu by remember { mutableStateOf(false) }
    val older = exams.find { it.id == olderId }
    val newer = exams.find { it.id == newerId }

    DashboardPanel(modifier, accent = true) {
        Text("DENEME DÜELLOSU", color = colors.secondary,
            style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
        Spacer(Modifier.height(5.dp))
        Text("İki denemeyi karşılaştır", color = colors.onSurface,
            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("İki kayıt seç; netlerini, doğrularını ve branşlarını yan yana incele.",
            color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(13.dp))
        if (exams.size < 2) {
            Text("Karşılaştırma için aynı türde en az iki deneme gerekli.",
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium)
            return@DashboardPanel
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Column(Modifier.weight(1f)) {
                Text("İLK DENEME", color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
                Box {
                    OutlinedButton(onClick = { olderMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 9.dp, vertical = 8.dp)) {
                        Text(older?.let { "${it.date}  ·  ${it.title}" } ?: "Deneme seç",
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall)
                    }
                    DropdownMenu(expanded = olderMenu, onDismissRequest = { olderMenu = false }) {
                        exams.asReversed().forEach { exam ->
                            DropdownMenuItem(
                                text = { Text("${exam.date} · ${exam.title}",
                                    maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                enabled = exam.id != newerId,
                                onClick = { olderId = exam.id; olderMenu = false }
                            )
                        }
                    }
                }
            }
            Column(Modifier.weight(1f)) {
                Text("İKİNCİ DENEME", color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
                Box {
                    OutlinedButton(onClick = { newerMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 9.dp, vertical = 8.dp)) {
                        Text(newer?.let { "${it.date}  ·  ${it.title}" } ?: "Deneme seç",
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall)
                    }
                    DropdownMenu(expanded = newerMenu, onDismissRequest = { newerMenu = false }) {
                        exams.asReversed().forEach { exam ->
                            DropdownMenuItem(
                                text = { Text("${exam.date} · ${exam.title}",
                                    maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                enabled = exam.id != olderId,
                                onClick = { newerId = exam.id; newerMenu = false }
                            )
                        }
                    }
                }
            }
        }

        if (older == null || newer == null) return@DashboardPanel
        val netDelta = newer.totalNet - older.totalNet
        Spacer(Modifier.height(13.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("İLK", style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant)
                Text(Fmt.net(older.totalNet),
                    fontSize = 27.sp, fontWeight = FontWeight.Bold, color = colors.onSurface)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("NET FARKI", style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant)
                Text("${if (netDelta >= 0) "+" else ""}${Fmt.net(netDelta)}",
                    fontSize = 22.sp, fontWeight = FontWeight.Bold,
                    color = if (netDelta >= 0) colors.primary else colors.error)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("İKİNCİ", style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant)
                Text(Fmt.net(newer.totalNet),
                    fontSize = 27.sp, fontWeight = FontWeight.Bold, color = colors.onSurface)
            }
        }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = colors.outline.copy(alpha = .35f))
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val correctDiff = newer.totalCorrect - older.totalCorrect
            val wrongDiff = newer.totalWrong - older.totalWrong
            Column {
                Text("DOĞRU FARKI", style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant)
                Text("${if (correctDiff > 0) "+" else ""}$correctDiff",
                    color = if (correctDiff >= 0) colors.primary else colors.error,
                    fontWeight = FontWeight.Bold)
            }
            Column {
                Text("YANLIŞ FARKI", style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant)
                Text("${if (wrongDiff > 0) "+" else ""}$wrongDiff",
                    color = if (wrongDiff <= 0) colors.primary else colors.error,
                    fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("SÜRE FARKI", style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant)
                val minutes = newer.totalTimeMinutes - older.totalTimeMinutes
                Text(if (older.totalTimeMinutes == 0 || newer.totalTimeMinutes == 0) "—"
                    else "${if (minutes > 0) "+" else ""}$minutes dk",
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("BRANŞ NET DEĞİŞİMİ", color = colors.secondary,
            style = MaterialTheme.typography.labelSmall, letterSpacing = .8.sp)
        Spacer(Modifier.height(7.dp))
        (older.subjects.map { it.subject } + newer.subjects.map { it.subject })
            .distinct().forEach { subject ->
                val a = older.subject(subject)
                val b = newer.subject(subject)
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(subject, color = colors.onSurface,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(6.dp))
                        if (a != null && b != null) {
                            val denom = maxOf(a.questionCount, b.questionCount, 1)
                            DashboardProgress((a.net / denom).toFloat(),
                                Modifier.fillMaxWidth(),
                                foreground = colors.tertiary)
                            Spacer(Modifier.height(4.dp))
                            DashboardProgress((b.net / denom).toFloat(),
                                Modifier.fillMaxWidth(),
                                foreground = colors.primary)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        if (a == null || b == null) "Karşılaştırılamaz"
                        else {
                            val diff = b.net - a.net
                            "${if (diff > 0) "+" else ""}${Fmt.net(diff)}"
                        },
                        color = if (a != null && b != null && b.net - a.net < 0)
                            colors.error else colors.onSurface,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        Spacer(Modifier.height(7.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp)
                .background(colors.tertiary, RoundedCornerShape(4.dp)))
            Spacer(Modifier.width(5.dp))
            Text("İlk", color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.width(15.dp))
            Box(Modifier.size(8.dp)
                .background(colors.primary, RoundedCornerShape(4.dp)))
            Spacer(Modifier.width(5.dp))
            Text("İkinci", color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall)
        }
    }
}
