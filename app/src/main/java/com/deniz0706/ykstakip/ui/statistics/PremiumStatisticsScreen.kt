package com.deniz0706.ykstakip.ui.statistics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.ui.theme.YksTokens
import com.deniz0706.ykstakip.util.Fmt

/** Premium statistics and analytics screen for YKS Takip 4.0. */
@Composable
fun PremiumStatisticsScreen(
    exams: List<Exam>,
    obp: Double?,
    targetTyt: Float?,
    targetAyt: Float?
) {
    val c = MaterialTheme.colorScheme
    val tytExams = exams.filter { it.type.label == "TYT" }.sortedBy { it.date }
    val aytExams = exams.filter { it.type.label == "AYT" }.sortedBy { it.date }

    val tytAverage = remember(tytExams) {
        if (tytExams.isNotEmpty()) tytExams.map { it.totalNet }.average() else 0.0
    }
    val aytAverage = remember(aytExams) {
        if (aytExams.isNotEmpty()) aytExams.map { it.totalNet }.average() else 0.0
    }
    val overallAverage = remember(exams) {
        if (exams.isNotEmpty()) exams.map { it.totalNet }.average() else 0.0
    }
    val tytBest = remember(tytExams) { tytExams.maxOfOrNull { it.totalNet } }
    val aytBest = remember(aytExams) { aytExams.maxOfOrNull { it.totalNet } }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(c.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = YksTokens.Space.pageHorizontal,
            vertical = YksTokens.Space.pageVertical
        ),
        verticalArrangement = Arrangement.spacedBy(YksTokens.Space.section)
    ) {
        // Header
        item {
            PremiumSectionHeader(
                title = "İstatistikler",
                subtitle = "${exams.size} deneme · OBP: ${Fmt.net(obp)}",
                colorScheme = c
            )
        }

        // Overview stats row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(YksTokens.Space.md)
            ) {
                PremiumStatTile(
                    label = "Genel Ortalama",
                    value = Fmt.net(overallAverage),
                    detail = "tüm sınavlar",
                    accent = c.primary,
                    modifier = Modifier.weight(1f)
                )
                PremiumStatTile(
                    label = "TYT Ortalama",
                    value = Fmt.net(tytAverage),
                    detail = "${tytExams.size} deneme",
                    accent = c.secondary,
                    modifier = Modifier.weight(1f)
                )
                PremiumStatTile(
                    label = "AYT Ortalama",
                    value = Fmt.net(aytAverage),
                    detail = "${aytExams.size} deneme",
                    accent = c.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(YksTokens.Space.sm)) }

        // Best scores row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(YksTokens.Space.md)
            ) {
                PremiumStatTile(
                    label = "TYT En İyi",
                    value = Fmt.net(tytBest),
                    detail = "kişisel rekor",
                    accent = c.primary,
                    modifier = Modifier.weight(1f)
                )
                PremiumStatTile(
                    label = "AYT En İyi",
                    value = Fmt.net(aytBest),
                    detail = "kişisel rekor",
                    accent = c.secondary,
                    modifier = Modifier.weight(1f)
                )
                PremiumStatTile(
                    label = "OBP",
                    value = obp?.let { it.toInt().toString() } ?: "—",
                    detail = "0–100",
                    accent = c.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(YksTokens.Space.md)) }

        // Targets section
        item {
            PremiumTargetsCard(
                targetTyt = targetTyt,
                targetAyt = targetAyt,
                obp = obp,
                tytAverage = tytAverage,
                aytAverage = aytAverage
            )
        }

        item { Spacer(modifier = Modifier.height(YksTokens.Space.md)) }

        // Recent exams trend
        item {
            PremiumRecentTrendCard(
                exams = exams.takeLast(10).reversed()
            )
        }

        item { Spacer(modifier = Modifier.height(120.dp)) }
    }
}

@Composable
private fun PremiumSectionHeader(
    title: String,
    subtitle: String,
    colorScheme: ColorScheme
) {
    val c = colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = c.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = c.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PremiumStatTile(
    label: String,
    value: String,
    detail: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme
    Column(
        modifier = modifier
             .clip(RoundedCornerShape(YksTokens.Shape.panel))
            .background(c.surfaceContainer)
            .padding(YksTokens.Space.cardContent)
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = accent,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = detail, style = MaterialTheme.typography.labelSmall, color = c.onSurfaceVariant)
    }
}

@Composable
private fun PremiumTargetsCard(
    targetTyt: Float?,
    targetAyt: Float?,
    obp: Double?,
    tytAverage: Double,
    aytAverage: Double
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
                text = "Hedefler ve İlerleme",
                style = MaterialTheme.typography.labelLarge,
                color = c.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(YksTokens.Space.md)
            ) {
                PremiumProgressTile(
                    label = "TYT Hedef Neti",
                    current = tytAverage,
                    target = targetTyt?.toDouble(),
                    unit = "net",
                    range = "0–120",
                    accent = c.primary,
                    modifier = Modifier.weight(1f)
                )
                PremiumProgressTile(
                    label = "AYT Hedef Neti",
                    current = aytAverage,
                    target = targetAyt?.toDouble(),
                    unit = "net",
                    range = "0–80",
                    accent = c.secondary,
                    modifier = Modifier.weight(1f)
                )
                PremiumProgressTile(
                    label = "OBP",
                    current = obp ?: 0.0,
                    target = null,
                    unit = "",
                    range = "0–100",
                    accent = c.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PremiumProgressTile(
    label: String,
    current: Double,
    target: Double?,
    unit: String,
    range: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme
    val progress = if (target != null && target > 0) {
        (current / target.coerceAtLeast(0.1)).coerceIn(0.0, 1.0)
    } else 0.0

    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = Fmt.net(current) + (if (unit.isNotBlank()) " $unit" else ""),
            style = MaterialTheme.typography.headlineMedium,
            color = accent,
            fontWeight = FontWeight.Bold
        )
        if (target != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Hedef: ${Fmt.net(target)} · ${range}",
                style = MaterialTheme.typography.labelSmall,
                color = c.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxWidth().height(4.dp)
                .clip(RoundedCornerShape(YksTokens.Shape.pill)),
            contentAlignment = Alignment.CenterStart
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .width(1000.dp * progress.toFloat()) // approximate visual width
                    .height(4.dp)
                    .background(accent)
                    .clip(RoundedCornerShape(YksTokens.Shape.pill))
            )
        }
    }
}

@Composable
private fun PremiumRecentTrendCard(exams: List<Exam>) {
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
                text = "Son Denemeler",
                style = MaterialTheme.typography.labelLarge,
                color = c.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (exams.isEmpty()) {
                Text(
                    text = "Henüz deneme kaydı yok",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.onSurfaceVariant
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    exams.forEach { exam ->
                        PremiumTrendRow(
                            exam = exam,
                            colorScheme = c
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumTrendRow(exam: Exam, colorScheme: ColorScheme) {
    val c = colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = exam.date,
            style = MaterialTheme.typography.bodyMedium,
            color = c.onSurfaceVariant,
            modifier = Modifier.width(70.dp)
        )
        Text(
            text = "${exam.type.label} · ${exam.title}",
            style = MaterialTheme.typography.bodyMedium,
            color = c.onSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = Fmt.net(exam.totalNet),
            style = MaterialTheme.typography.headlineMedium,
            color = c.primary,
            fontWeight = FontWeight.Bold
        )
    }
}
