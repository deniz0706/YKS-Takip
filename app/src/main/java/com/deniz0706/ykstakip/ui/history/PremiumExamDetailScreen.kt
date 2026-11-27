package com.deniz0706.ykstakip.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.ui.theme.YksTokens
import com.deniz0706.ykstakip.util.Fmt
import java.text.SimpleDateFormat
import java.util.Locale

/** Premium exam detail screen for YKS Takip 4.0. */
@Composable
fun PremiumExamDetailScreen(
    exam: Exam,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenLearning: (String, String, String) -> Unit
) {
    val c = MaterialTheme.colorScheme
    var confirmDelete by remember(exam.id) { mutableStateOf(false) }
    val displayDate = remember(exam.date) {
        runCatching {
            SimpleDateFormat("d MMMM yyyy", Locale("tr", "TR"))
                .format(SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(exam.date)!!)
        }.getOrDefault(exam.date)
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = YksTokens.Space.pageHorizontal, vertical = YksTokens.Space.pageVertical),
        verticalArrangement = Arrangement.spacedBy(YksTokens.Space.md)
    ) {
        // Header actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(onClick = onBack) { Text("← Geri") }
            Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(onClick = onEdit) { Text("Düzenle") }
            TextButton(onClick = { confirmDelete = true }) {
                Text("Sil", color = c.error)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Exam title
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = exam.type.label.uppercase(),
                color = c.primary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = exam.title,
                color = c.onSurface,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = displayDate + (exam.publisher.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""),
                color = c.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Net summary card
        PremiumMetricCard(
            label = "Toplam Net",
            value = Fmt.net(exam.totalNet),
            detail = "${exam.totalCorrect} doğru · ${exam.totalWrong} yanlış · ${exam.totalBlank} boş",
            accent = c.primary,
            color = c
        )

        Spacer(modifier = Modifier.height(YksTokens.Space.sm))

        // Timing info
        if (exam.totalTimeMinutes > 0) {
            PremiumMetricCard(
                label = "Süre",
                value = "${exam.totalTimeMinutes} dakika",
                detail = exam.overallMinutesPerQuestion?.let { "· ${Fmt.minutesPerQuestion(it)} dk/soru" } ?: "",
                accent = c.secondary,
                color = c
            )
            Spacer(modifier = Modifier.height(YksTokens.Space.sm))
        }

        // Notes
        if (exam.notes.isNotBlank()) {
            PremiumNotesCard(
                title = "Deneme Notu",
                content = exam.notes,
                color = c
            )
            Spacer(modifier = Modifier.height(YksTokens.Space.sm))
        }

        // Subject breakdown
        Text(
            text = "Ders Kırılımı (${exam.subjects.size} ders)",
            style = MaterialTheme.typography.titleMedium,
            color = c.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        exam.subjects.forEachIndexed { index, subject ->
            PremiumSubjectRow(
                subject = subject,
                color = c,
                onTopicClick = { topic -> onOpenLearning(exam.type.name, subject.subject, topic) }
            )
            if (index < exam.subjects.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }

    if (confirmDelete) {
        PremiumDeleteDialog(
            onDismiss = { confirmDelete = false },
            onConfirm = {
                confirmDelete = false
                onDelete()
            },
            examTitle = exam.title,
            color = c
        )
    }
}

@Composable
private fun PremiumMetricCard(
    label: String,
    value: String,
    detail: String,
    accent: Color,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(YksTokens.Shape.panel),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = color.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(YksTokens.Space.cardContent),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = accent,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = color.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PremiumNotesCard(
    title: String,
    content: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(YksTokens.Shape.panel),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = color.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(YksTokens.Space.cardContent),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = color.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = color.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PremiumSubjectRow(
    subject: com.deniz0706.ykstakip.domain.model.SubjectResult,
    color: Color,
    onTopicClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(YksTokens.Shape.card),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = color.surface)
    ) {
        Column(
            modifier = Modifier.padding(YksTokens.Space.cardContent),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = subject.subject,
                        color = color.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${subject.questionCount} soru · ${subject.timeMinutes} dk",
                        color = color.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Text(
                    text = "${Fmt.net(subject.net)} net",
                    color = color.primary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "${subject.correct} doğru · ${subject.wrong} yanlış · ${subject.blank} boş" +
                    (subject.minutesPerQuestion?.let { " · ${Fmt.minutesPerQuestion(it)} dk/soru" } ?: ""),
                color = color.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )

            if (subject.weakTopics.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Zayıf Konular",
                    color = color.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    subject.weakTopics.forEach { topic ->
                        AssistChip(
                            onClick = { onTopicClick(topic) },
                            label = { Text(topic, maxLines = 1) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bir konuya dokunarak öğrenme merkezinde aç.",
                    color = color.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun PremiumDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    examTitle: String,
    color: Color
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Deneme Silinsin mi?") },
        text = { Text("“$examTitle” denemesi kalıcı olarak silinecek. Bu işlem geri alınamaz.") },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) { Text("Sil", color = color.error) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Vazgeç") }
        }
    )
}

private object Fmt {
    fun net(value: Double?): String = value?.let { "%.2f".format(it) } ?: "—"
    fun minutesPerQuestion(value: Double): String = "%.1f".format(value)
}