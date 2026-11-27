package com.deniz0706.ykstakip.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.ui.theme.YksTokens
import com.deniz0706.ykstakip.util.Fmt

/** Premium exam history screen for YKS Takip 4.0. */
@Composable
fun PremiumExamsScreen(
    exams: List<Exam>,
    onExamClick: (Long) -> Unit,
    onNewExam: () -> Unit
) {
    val c = MaterialTheme.colorScheme

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
                title = "Denemeler",
                subtitle = "${exams.size} kayıt",
                color = c.primary
            )
        }

        // New exam action card
        item {
            PremiumActionCard(
                label = "+ Yeni Deneme Ekle",
                subtitle = "Performans takibi için ilk denemenizi kaydedin.",
                icon = "📝",
                accent = c.primary,
                onClick = onNewExam,
                color = c,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(YksTokens.Space.sm))

        if (exams.isEmpty()) {
            item {
                PremiumEmptyState(
                    title = "Henüz deneme kaydı yok",
                    subtitle = "İlk denemenizi ekleyerek performansınızı takip etmeye başlayın.",
                    color = c
                )
            }
        } else {
            items(
                items = exams.sortedByDescending { it.date },
                key = { it.id }
            ) { exam ->
                PremiumExamCard(
                    exam = exam,
                    onClick = { onExamClick(exam.id) },
                    color = c
                )
            }
        }

        item { Spacer(modifier = Modifier.height(120.dp)) }
    }
}

@Composable
private fun PremiumSectionHeader(
    title: String,
    subtitle: String,
    color: Color
) {
    val c = MaterialTheme.colorScheme
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
private fun PremiumActionCard(
    label: String,
    subtitle: String,
    icon: String,
    accent: Color,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(YksTokens.Shape.panel))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(YksTokens.Shape.panel),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = color.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.padding(YksTokens.Space.cardContent),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(YksTokens.Space.md)
        ) {
            Text(text = icon, style = MaterialTheme.typography.titleLarge, color = accent)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = color.onSurfaceVariant
                )
            }
            Text(
                text = "→",
                style = MaterialTheme.typography.labelMedium,
                color = accent
            )
        }
    }
}

@Composable
private fun PremiumEmptyState(
    title: String,
    subtitle: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(YksTokens.Shape.panel),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = color.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(YksTokens.Space.cardContent)
                .heightIn(min = 200.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = color.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = color.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun PremiumExamCard(
    exam: Exam,
    onClick: () -> Unit,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(YksTokens.Shape.card))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(YksTokens.Shape.card),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = color.surface)
    ) {
        Row(
            modifier = Modifier.padding(YksTokens.Space.cardContent),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(YksTokens.Space.md)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${exam.type.label} · ${exam.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = color.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = exam.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = color.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Net: ${Fmt.net(exam.totalNet)}  •  ${exam.totalQuestions} soru",
                    style = MaterialTheme.typography.labelMedium,
                    color = color.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "→",
                style = MaterialTheme.typography.labelMedium,
                color = color.onSurfaceVariant
            )
        }
    }
}

private object Fmt {
    fun net(value: Double?): String = value?.let { "%.2f".format(it) } ?: "—"
}