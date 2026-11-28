package com.deniz0706.ykstakip.ui.history

import com.deniz0706.ykstakip.ui.theme.clayCanvas
import com.deniz0706.ykstakip.ui.theme.clayRaised
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deniz0706.ykstakip.domain.model.AytField
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import com.deniz0706.ykstakip.domain.model.SubjectConfig
import com.deniz0706.ykstakip.domain.model.SubjectConfigs
import com.deniz0706.ykstakip.domain.model.SubjectResult
import com.deniz0706.ykstakip.ui.theme.YksTokens
import java.time.LocalDate

@Composable
fun ExamEntryScreen(
    onBack: () -> Unit,
    onSave: (Exam) -> Unit,
    initialExam: Exam? = null
) {
    val colors = MaterialTheme.colorScheme
    var type by remember(initialExam) { mutableStateOf(initialExam?.type ?: ExamType.TYT) }
    var aytField by remember(initialExam) { mutableStateOf(initialExam?.aytField ?: AytField.SAYISAL) }
    var title by remember(initialExam) { mutableStateOf(initialExam?.title.orEmpty()) }
    var publisher by remember(initialExam) { mutableStateOf(initialExam?.publisher.orEmpty()) }
    var date by remember(initialExam) { mutableStateOf(initialExam?.date ?: LocalDate.now().toString()) }
    var notes by remember(initialExam) { mutableStateOf(initialExam?.notes.orEmpty()) }
    val answers = remember(initialExam) {
        mutableStateMapOf<String, String>().apply {
            initialExam?.subjects?.forEach { subject ->
                put(subject.subject + ":correct", subject.correct.toString())
                put(subject.subject + ":wrong", subject.wrong.toString())
            }
        }
    }
    val times = remember(initialExam) {
        mutableStateMapOf<String, String>().apply {
            initialExam?.subjects?.forEach { subject ->
                put(subject.subject, subject.timeMinutes.toString())
            }
        }
    }

    val configs = if (type == ExamType.TYT) SubjectConfigs.subjectsFor(ExamType.TYT)
        else SubjectConfigs.subjectsFor(aytField)
    val results = configs.map { config ->
        val correct = answers["${config.name}:correct"]?.toIntOrNull() ?: 0
        val wrong = answers["${config.name}:wrong"]?.toIntOrNull() ?: 0
        val time = times[config.name]?.toIntOrNull() ?: 0
        SubjectResult(
            subject = config.name,
            correct = correct,
            wrong = wrong,
            blank = (config.questionCount - correct - wrong).coerceAtLeast(0),
            timeMinutes = time
        )
    }
    val validScores = configs.all { config ->
        val correct = answers["${config.name}:correct"]?.toIntOrNull() ?: 0
        val wrong = answers["${config.name}:wrong"]?.toIntOrNull() ?: 0
        correct >= 0 && wrong >= 0 && correct + wrong <= config.questionCount
    }
    val validDate = runCatching { LocalDate.parse(date); true }.getOrDefault(false)
    val canSave = title.isNotBlank() && validDate && validScores

    Column(
        modifier = Modifier.clayCanvas()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = YksTokens.Space.pageHorizontal, vertical = YksTokens.Space.pageVertical),
        verticalArrangement = Arrangement.spacedBy(YksTokens.Space.md)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Geri") }
            Text(if (initialExam == null) "Yeni Deneme" else "Denemeyi Düzenle", style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
        }

        Card(
        modifier = Modifier.fillMaxWidth().clayRaised(shape = RoundedCornerShape(YksTokens.Shape.panel), depth = 8.dp),
            shape = RoundedCornerShape(YksTokens.Shape.panel),
            elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 6.dp),

            colors = CardDefaults.cardColors(containerColor = colors.surfaceContainer)
        ) {
            Column(Modifier.padding(YksTokens.Space.cardContent), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Sınav bilgileri", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExamType.values().forEach { option ->
                        FilterChip(
                            selected = type == option,
                            onClick = { type = option },
                            label = { Text(option.label) }
                        )
                    }
                }
                if (type == ExamType.AYT) {
                    Text("Alan", style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        AytField.values().forEach { field ->
                            FilterChip(
                                selected = aytField == field,
                                onClick = { aytField = field },
                                label = { Text(field.label) }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Deneme adı *") }
                )
                OutlinedTextField(
                    value = publisher,
                    onValueChange = { publisher = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Yayıncı (isteğe bağlı)") }
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Tarih (YYYY-AA-GG)") },
                    isError = date.isNotBlank() && !validDate
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    label = { Text("Deneme notu (isteğe bağlı)") }
                )
            }
        }

        Text("Ders sonuçları", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
        Text(
            "Doğru ve yanlış sayılarını gir. Boş sayısı soru sayısından otomatik hesaplanır.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )

        configs.forEach { config ->
            SubjectEntryCard(
                config = config,
                correct = answers["${config.name}:correct"].orEmpty(),
                wrong = answers["${config.name}:wrong"].orEmpty(),
                time = times[config.name].orEmpty(),
                onCorrectChange = { answers["${config.name}:correct"] = it },
                onWrongChange = { answers["${config.name}:wrong"] = it },
                onTimeChange = { times[config.name] = it }
            )
        }

        Card(
        modifier = Modifier.fillMaxWidth().clayRaised(shape = RoundedCornerShape(YksTokens.Shape.panel), depth = 8.dp),
            shape = RoundedCornerShape(YksTokens.Shape.panel),
            elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 6.dp),

            colors = CardDefaults.cardColors(containerColor = colors.surfaceContainer)
        ) {
            Column(Modifier.padding(YksTokens.Space.cardContent)) {
                Text("Toplam net", style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
                Text(
                    text = String.format(java.util.Locale("tr", "TR"), "%.2f", results.sumOf { it.net }),
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.primary
                )
                Text(
                    text = "${results.sumOf { it.correct }} doğru · ${results.sumOf { it.wrong }} yanlış · ${results.sumOf { it.blank }} boş",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }

        Button(
            onClick = {
                if (canSave) {
                    onSave(
                        Exam(
                            id = initialExam?.id ?: System.currentTimeMillis(),
                            title = title.trim(),
                            date = date,
                            type = type,
                            aytField = if (type == ExamType.AYT) aytField else null,
                            subjects = results,
                            notes = notes.trim(),
                            publisher = publisher.trim()
                        )
                    )
                }
            },
            enabled = canSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (initialExam == null) "Denemeyi Kaydet" else "Değişiklikleri Kaydet")
        }
        Spacer(Modifier.height(72.dp))
    }
}

@Composable
private fun SubjectEntryCard(
    config: SubjectConfig,
    correct: String,
    wrong: String,
    time: String,
    onCorrectChange: (String) -> Unit,
    onWrongChange: (String) -> Unit,
    onTimeChange: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val correctValue = correct.toIntOrNull() ?: 0
    val wrongValue = wrong.toIntOrNull() ?: 0
    val remaining = (config.questionCount - correctValue - wrongValue).coerceAtLeast(0)
    Card(
        modifier = Modifier.fillMaxWidth().clayRaised(shape = RoundedCornerShape(YksTokens.Shape.panel), depth = 8.dp),
        shape = RoundedCornerShape(YksTokens.Shape.card),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 6.dp),

        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow)
    ) {
        Column(Modifier.padding(YksTokens.Space.cardContent), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(config.name, style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                Text("${config.questionCount} soru", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                ScoreInput("Doğru", correct, onCorrectChange, Modifier.weight(1f))
                ScoreInput("Yanlış", wrong, onWrongChange, Modifier.weight(1f))
                Column(Modifier.weight(1f)) {
                    Text("Boş", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                    Text(remaining.toString(), style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                }
            }
            OutlinedTextField(
                value = time,
                onValueChange = { onTimeChange(it.filter(Char::isDigit).take(3)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Süre (dakika, isteğe bağlı)") }
            )
            if (correctValue + wrongValue > config.questionCount) {
                Text("Doğru + yanlış ${config.questionCount} soruyu aşamaz.", color = colors.error, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun ScoreInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter(Char::isDigit).take(2)) },
        modifier = modifier,
        singleLine = true,
        label = { Text(label) }
    )
}
