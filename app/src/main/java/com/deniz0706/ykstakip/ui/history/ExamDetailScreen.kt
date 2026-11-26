package com.deniz0706.ykstakip.ui.history

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.ui.components.DashboardPanel
import com.deniz0706.ykstakip.ui.components.DashboardSectionHeading
import com.deniz0706.ykstakip.util.Fmt
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ExamDetailScreen(
    exam: Exam,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenLearning: (String, String, String) -> Unit
) {
    var confirmDelete by remember(exam.id) { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val displayDate = remember(exam.date) {
        runCatching {
            SimpleDateFormat("d MMMM yyyy", Locale("tr", "TR"))
                .format(SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(exam.date)!!)
        }.getOrDefault(exam.date)
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onBack) { Text("← Geri") }
            Spacer(Modifier.weight(1f))
            OutlinedButton(onClick = onEdit) { Text("Düzenle") }
            TextButton(onClick = { confirmDelete = true }) {
                Text("Sil", color = colors.error)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(exam.type.label.uppercase(), color = colors.primary,
                style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(exam.title, color = colors.onSurface,
                style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(displayDate + (exam.publisher.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""),
                color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }

        DashboardPanel(accent = true) {
            Text("TOPLAM NET", color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            Text(Fmt.net(exam.totalNet), color = colors.onSurface,
                style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
            Text(
                "${exam.totalCorrect} doğru · ${exam.totalWrong} yanlış · ${exam.totalBlank} boş",
                color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "${exam.totalTimeMinutes} dakika" +
                    (exam.overallMinutesPerQuestion?.let { " · ${Fmt.minutesPerQuestion(it)} dk/soru" } ?: ""),
                color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelMedium
            )
        }

        if (exam.notes.isNotBlank()) {
            DashboardPanel {
                DashboardSectionHeading("Deneme notu")
                Text(exam.notes, color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium)
            }
        }

        DashboardSectionHeading("Ders kırılımı", detail = "${exam.subjects.size} ders")
        exam.subjects.forEach { subject ->
            DashboardPanel {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(subject.subject, color = colors.onSurface,
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("${subject.questionCount} soru · ${subject.timeMinutes} dk",
                            color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    }
                    Text("${Fmt.net(subject.net)} net", color = colors.primary,
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Text(
                    "${subject.correct} doğru · ${subject.wrong} yanlış · ${subject.blank} boş" +
                        (subject.minutesPerQuestion?.let { " · ${Fmt.minutesPerQuestion(it)} dk/soru" } ?: ""),
                    color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall
                )
                if (subject.weakTopics.isNotEmpty()) {
                    Text("Zayıf konular", color = colors.onSurface,
                        style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        subject.weakTopics.forEach { topic ->
                            AssistChip(
                                onClick = { onOpenLearning(exam.type.name, subject.subject, topic) },
                                label = { Text(topic, maxLines = 1) }
                            )
                        }
                    }
                    Text("Bir konuya dokunarak öğrenme merkezinde aç.",
                        color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Deneme silinsin mi?") },
            text = { Text("“${exam.title}” denemesi kalıcı olarak silinecek. Bu işlem geri alınamaz.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text("Sil", color = colors.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Vazgeç") }
            }
        )
    }
}
