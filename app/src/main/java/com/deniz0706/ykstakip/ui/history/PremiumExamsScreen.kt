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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import com.deniz0706.ykstakip.ui.theme.YksTokens
import com.deniz0706.ykstakip.util.Fmt

/** Premium exam history screen for YKS Takip 4.0. */
@Composable
fun PremiumExamsScreen(
    exams: List<Exam>,
    onExamClick: (Long) -> Unit,
    onNewExam: () -> Unit,
    colorScheme: ColorScheme
) {
    val c = colorScheme
    var selectedFilter by remember { mutableStateOf("Tümü") }
    var searchQuery by remember { mutableStateOf("") }
    val filteredExams = remember(exams, selectedFilter, searchQuery) {
        exams.filter { exam ->
            val matchesType = when (selectedFilter) {
                "TYT" -> exam.type == ExamType.TYT
                "AYT" -> exam.type == ExamType.AYT
                else -> true
            }
            val query = searchQuery.trim()
            val matchesQuery = query.isEmpty() ||
                exam.title.contains(query, ignoreCase = true) ||
                exam.date.contains(query, ignoreCase = true) ||
                exam.type.label.contains(query, ignoreCase = true) ||
                exam.publisher.contains(query, ignoreCase = true)
            matchesType && matchesQuery
        }.sortedByDescending { it.date }
    }

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
                colorScheme = c
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
                colorScheme = c,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Deneme adı, yayıncı veya tarih ara") },
                shape = RoundedCornerShape(YksTokens.Shape.panel)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Tümü", "TYT", "AYT").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${filteredExams.size} sonuç",
                style = MaterialTheme.typography.labelSmall,
                color = c.onSurfaceVariant
            )
        }

        item { Spacer(modifier = Modifier.height(YksTokens.Space.sm)) }

        if (exams.isEmpty()) {
            item {
                PremiumEmptyState(
                    title = "Henüz deneme kaydı yok",
                    subtitle = "İlk denemenizi ekleyerek performansınızı takip etmeye başlayın.",
                    colorScheme = c
                )
            }
        } else if (filteredExams.isEmpty()) {
            item {
                PremiumEmptyState(
                    title = "Eşleşen deneme bulunamadı",
                    subtitle = "Arama metnini veya deneme türü filtresini değiştirin.",
                    colorScheme = c
                )
            }
        } else {
            items(
                items = filteredExams,
                key = { it.id }
            ) { exam ->
                PremiumExamCard(
                    exam = exam,
                    onClick = { onExamClick(exam.id) },
                    colorScheme = c
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
private fun PremiumActionCard(
    label: String,
    subtitle: String,
    icon: String,
    accent: Color,
    onClick: () -> Unit,
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier
) {
    val c = colorScheme
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(YksTokens.Shape.panel))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(YksTokens.Shape.panel),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = c.surfaceContainer)
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
                    color = c.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.onSurfaceVariant
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
    colorScheme: ColorScheme
) {
    val c = colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(YksTokens.Shape.panel),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = c.surfaceContainer)
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
                color = c.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = c.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun PremiumExamCard(
    exam: Exam,
    onClick: () -> Unit,
    colorScheme: ColorScheme
) {
    val c = colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(YksTokens.Shape.card))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(YksTokens.Shape.card),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = c.surface)
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
                    color = c.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = exam.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Net: ${Fmt.net(exam.totalNet)}  •  ${exam.totalQuestions} soru",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "→",
                style = MaterialTheme.typography.labelMedium,
                color = c.onSurfaceVariant
            )
        }
    }
}
