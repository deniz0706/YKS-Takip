package com.deniz0706.ykstakip.ui.history

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.ui.components.ExamTypeTabs
import com.deniz0706.ykstakip.ui.components.YksCard
import com.deniz0706.ykstakip.ui.components.DashboardPanel
import com.deniz0706.ykstakip.ui.components.DashboardPageHeader
import com.deniz0706.ykstakip.ui.components.DashboardMetric
import com.deniz0706.ykstakip.ui.components.DashboardRing
import com.deniz0706.ykstakip.ui.components.DashboardProgress
import com.deniz0706.ykstakip.util.Fmt

@Composable
fun HistoryScreenV2(
    exams: List<Exam>,
    onExamClick: (Long) -> Unit,
    onPickDate: (Boolean) -> Unit,
    fromDate: String?,
    toDate: String?,
    onClearDates: () -> Unit,
    modifier: Modifier = Modifier
) {
    var type by remember { mutableStateOf(ExamType.TYT) }
    var query by remember { mutableStateOf("") }
    var newestFirst by remember { mutableStateOf(true) }
    val filtered = remember(exams, type, query, fromDate, toDate, newestFirst) {
        val matches = exams.filter { exam ->
            exam.type == type &&
                (query.isBlank() || exam.title.contains(query.trim(), ignoreCase = true) ||
                    exam.publisher.contains(query.trim(), ignoreCase = true)) &&
                (fromDate == null || exam.date >= fromDate) &&
                (toDate == null || exam.date <= toDate)
        }
        if (newestFirst) matches.sortedByDescending { it.date }
        else matches.sortedBy { it.date }
    }
    val average = remember(filtered) { filtered.takeIf { it.isNotEmpty() }?.map { it.totalNet }?.average() }
    val best = remember(filtered) { filtered.maxOfOrNull { it.totalNet } }
    val latest = filtered.firstOrNull()?.totalNet
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 42.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            DashboardPageHeader(
                "DENEME ARŞİVİ",
                "Geçmiş",
                "Denemelerini filtrele, karşılaştır ve ayrıntılarına dön."
            )
        }
        item { ExamTypeTabs(type.name, { type = ExamType.valueOf(it) }) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DashboardMetric("Ortalama", average?.let(Fmt::net) ?: "—", Modifier.weight(1f))
                DashboardMetric("En iyi", best?.let(Fmt::net) ?: "—", Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.primary)
            }
        }
        item {
            val c = MaterialTheme.colorScheme
            val recent = filtered.take(5)
            val previous = filtered.drop(5).take(5)
            val recentAverage = recent.takeIf { it.isNotEmpty() }
                ?.map { it.totalNet }?.average()
            val previousAverage = previous.takeIf { it.isNotEmpty() }
                ?.map { it.totalNet }?.average()
            val delta = if (recentAverage != null && previousAverage != null)
                recentAverage - previousAverage else null
            val maximum = if (type == ExamType.TYT) 120f else 80f
            DashboardPanel(Modifier.fillMaxWidth(), accent = true) {
                Text("ARŞİVİN NABZI", color = c.secondary,
                    style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    DashboardRing(((latest ?: 0.0) / maximum).toFloat(),
                        latest?.let(Fmt::net) ?: "—", caption = "son net")
                    Column(Modifier.weight(1f)) {
                        Text("SON 5 DENEME", color = c.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall)
                        Text(recentAverage?.let(Fmt::net) ?: "—",
                            color = c.onSurface, fontSize = 28.sp,
                            fontWeight = FontWeight.Bold)
                        Text(when {
                            delta == null -> "Karşılaştırma için 6+ deneme gerekli"
                            delta > 0.5 -> "↗ +${Fmt.net(delta)} net yükseliş"
                            delta < -0.5 -> "↘ ${Fmt.net(delta)} net gerileme"
                            else -> "→ Ortalama dengeli"
                        }, color = if (delta != null && delta < -.5) c.error
                            else c.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(12.dp))
                DashboardProgress(((latest ?: 0.0) / maximum).toFloat(),
                    Modifier.fillMaxWidth())
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Deneme veya yayınevi ara") },
                leadingIcon = { Text("⌕", fontSize = 24.sp) },
                shape = RoundedCornerShape(18.dp)
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { onPickDate(true) }, modifier = Modifier.weight(1f)) {
                    Text(fromDate ?: "Başlangıç", maxLines = 1, fontSize = 12.sp)
                }
                OutlinedButton(onClick = { onPickDate(false) }, modifier = Modifier.weight(1f)) {
                    Text(toDate ?: "Bitiş", maxLines = 1, fontSize = 12.sp)
                }
                if (fromDate != null || toDate != null) {
                    TextButton(onClick = onClearDates) { Text("Sil") }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Kayıtlar", fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium)
                    Text("${filtered.size} deneme · ${if (newestFirst) "yeniden eskiye" else "eskiden yeniye"}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall)
                }
                TextButton(onClick = { newestFirst = !newestFirst }) {
                    Text(if (newestFirst) "En eski önce ↑" else "En yeni önce ↓")
                }
            }
        }
        if (filtered.isEmpty()) {
            item {
                YksCard {
                    Text("Burada henüz deneme görünmüyor", fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(6.dp))
                    Text("Arama ve tarih filtrelerini kontrol edebilir veya yeni bir deneme ekleyebilirsin.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        itemsIndexed(filtered, key = { _, exam -> exam.id }) { index, exam ->
            val c = MaterialTheme.colorScheme
            val comparisonIndex = if (newestFirst) index + 1 else index - 1
            val previousExam = filtered.getOrNull(comparisonIndex)
            val netChange = previousExam?.let { exam.totalNet - it.totalNet }
            val shape = RoundedCornerShape(22.dp)
            Column(
                Modifier.fillMaxWidth()
                    .background(c.surfaceContainerLow, shape)
                    .clickable { onExamClick(exam.id) }
                    .padding(horizontal = 16.dp, vertical = 15.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(exam.title, fontWeight = FontWeight.SemiBold, color = c.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(5.dp))
                        Text(exam.date + if (exam.publisher.isBlank()) "" else " · " + exam.publisher,
                            color = c.onSurfaceVariant, style = MaterialTheme.typography.bodySmall,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(Fmt.net(exam.totalNet), fontSize = 29.sp,
                            fontWeight = FontWeight.Bold, color = c.primary)
                        Text("NET", color = c.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
                    }
                }
                Spacer(Modifier.height(13.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    HistoryPill("${exam.totalCorrect} doğru", c.primary.copy(alpha = .12f), c.primary, Modifier.weight(1f))
                    HistoryPill("${exam.totalWrong} yanlış", c.error.copy(alpha = .10f), c.error, Modifier.weight(1f))
                    HistoryPill("${exam.totalBlank} boş", c.surfaceVariant, c.onSurfaceVariant, Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { (exam.totalNet / if (exam.type == ExamType.TYT) 120.0 else 80.0)
                        .toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(5.dp),
                    color = c.primary, trackColor = c.surfaceVariant
                )
                Spacer(Modifier.height(9.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("${exam.totalTimeMinutes} dakika", style = MaterialTheme.typography.labelSmall,
                            color = c.onSurfaceVariant)
                        if (netChange != null) {
                            val changeColor = when {
                                netChange > 0.0001 -> c.primary
                                netChange < -0.0001 -> c.error
                                else -> c.onSurfaceVariant
                            }
                            Text(when {
                                netChange > 0.0001 -> "Önceki denemeye göre +${Fmt.net(netChange)} net"
                                netChange < -0.0001 -> "Önceki denemeye göre ${Fmt.net(netChange)} net"
                                else -> "Önceki denemeyle aynı net"
                            }, color = changeColor, style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Text("Detayları gör  ↗", style = MaterialTheme.typography.labelSmall,
                        color = c.secondary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        item { Spacer(Modifier.height(90.dp)) }
    }
}

@Composable
private fun HistoryPill(
    label: String,
    background: androidx.compose.ui.graphics.Color,
    foreground: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(modifier.background(background, RoundedCornerShape(11.dp))
        .padding(horizontal = 5.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center) {
        Text(label, color = foreground, style = MaterialTheme.typography.labelSmall,
            maxLines = 1)
    }
}
