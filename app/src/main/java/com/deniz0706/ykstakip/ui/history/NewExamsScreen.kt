package com.deniz0706.ykstakip.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.ui.components.YksSection
import com.deniz0706.ykstakip.ui.components.YksStatTile
import com.deniz0706.ykstakip.ui.theme.YksShape
import com.deniz0706.ykstakip.ui.theme.YksSpacing

@Composable
fun NewExamsScreen(
    exams: List<Exam>,
    onExamClick: (Long) -> Unit,
    onNewExam: () -> Unit
) {
    val c = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(c.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = YksSpacing.pageHorizontal,
            vertical = YksSpacing.pageVertical
        ),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(YksSpacing.section)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Denemeler",
                    style = MaterialTheme.typography.displayMedium,
                    color = c.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${exams.size} kayıt",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.onSurfaceVariant
                )
            }
        }

        item {
            androidx.compose.foundation.layout.Column(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(YksShape.card))
                    .background(c.surfaceContainer)
                    .padding(YksSpacing.cardContent)
                    .clickable(onClick = onNewExam)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "+ Yeni deneme ekle",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "→",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.primary
                    )
                }
            }
        }

        if (exams.isEmpty()) {
            item {
                EmptyExamsState()
            }
        } else {
            items(
                items = exams,
                key = { it.id }
            ) { exam ->
                ExamCard(
                    exam = exam,
                    onClick = { onExamClick(exam.id) }
                )
            }
        }

        item { Spacer(Modifier.height(96.dp)) }
    }
}

@Composable
private fun EmptyExamsState() {
    val c = MaterialTheme.colorScheme
    androidx.compose.foundation.layout.Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(YksShape.card))
            .background(c.surfaceContainer)
            .padding(YksSpacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Henüz deneme kaydı yok",
            style = MaterialTheme.typography.titleMedium,
            color = c.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "İlk denemenizi ekleyerek performansınızı takip etmeye başlayın.",
            style = MaterialTheme.typography.bodySmall,
            color = c.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun ExamCard(
    exam: Exam,
    onClick: () -> Unit
) {
    val c = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(YksShape.card))
            .background(c.surfaceContainer)
            .padding(YksSpacing.cardContent)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(YksSpacing.md)
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "${exam.type.label} · ${exam.title}",
                style = MaterialTheme.typography.bodyMedium,
                color = c.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = exam.date,
                style = MaterialTheme.typography.bodySmall,
                color = c.onSurfaceVariant
            )
            Text(
                text = "Net: ${com.deniz0706.ykstakip.util.Fmt.net(exam.totalNet)}  •  ${exam.totalQuestions} soru",
                style = MaterialTheme.typography.bodySmall,
                color = c.onSurfaceVariant
            )
        }
        Text(
            text = "→",
            style = MaterialTheme.typography.labelMedium,
            color = c.onSurfaceVariant
        )
    }
}
