package com.deniz0706.ykstakip.ui.newexam

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.model.*
import com.deniz0706.ykstakip.data.ExamValidator
import com.deniz0706.ykstakip.ui.components.DashboardPanel
import com.deniz0706.ykstakip.ui.components.DashboardMetric
import com.deniz0706.ykstakip.util.Fmt
import java.time.LocalDate

private class SubjectDraft(
    val config: SubjectConfig,
    correct: Int = 0,
    wrong: Int = 0,
    minutes: Int = 0,
    topics: String = ""
) {
    var correct by mutableStateOf(correct.toString())
    var wrong by mutableStateOf(wrong.toString())
    var minutes by mutableStateOf(minutes.toString())
    var topics by mutableStateOf(topics)
    val c get() = correct.toIntOrNull() ?: 0
    val w get() = wrong.toIntOrNull() ?: 0
    val time get() = minutes.toIntOrNull() ?: 0
    val blank get() = (config.questionCount - c - w).coerceAtLeast(0)
    val net get() = c - w / 4.0
    val error get() = (ExamValidator.validatePair(c, w, config) as?
        ExamValidator.FieldResult.Invalid)?.message
}

@Composable
fun NewExamScreenV2(
    initialType: ExamType,
    existing: Exam?,
    learningOutcomes: List<LearningOutcome>,
    onBack: () -> Unit,
    onPickDate: (LocalDate, (LocalDate) -> Unit) -> Unit,
    onSave: (Exam) -> String?
) {
    val colors = MaterialTheme.colorScheme
    var type by remember { mutableStateOf(initialType) }
    var field by remember { mutableStateOf(existing?.aytField ?: AytField.SAYISAL) }
    var title by remember { mutableStateOf(existing?.title.orEmpty()) }
    var publisher by remember { mutableStateOf(existing?.publisher.orEmpty()) }
    var notes by remember { mutableStateOf(existing?.notes.orEmpty()) }
    var date by remember { mutableStateOf(
        existing?.date?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: LocalDate.now()) }
    var error by remember { mutableStateOf<String?>(null) }
    val configs = remember(type, field) {
        if (type == ExamType.AYT) SubjectConfigs.subjectsFor(field)
        else SubjectConfigs.subjectsFor(type)
    }
    // Preserve unsaved inputs when moving between TYT, AYT and AYT fields.
    val drafts = remember(existing) { mutableStateMapOf<String, List<SubjectDraft>>() }
    val draftKey = if (type == ExamType.TYT) "TYT" else "AYT_${field.name}"
    val rows = drafts.getOrPut(draftKey) {
        configs.map { config ->
            val saved = existing?.takeIf {
                it.type == type && (type != ExamType.AYT || it.aytField == field)
            }?.subject(config.name)
            SubjectDraft(config, saved?.correct ?: 0, saved?.wrong ?: 0,
                saved?.timeMinutes ?: 0, saved?.weakTopics?.joinToString(", ").orEmpty())
        }
    }
    val totalCorrect = rows.sumOf { it.c }
    val totalWrong = rows.sumOf { it.w }
    val totalTime = rows.sumOf { it.time }
    val totalNet = rows.sumOf { it.net }
    val valid = title.isNotBlank() && rows.all { it.error == null && it.time >= 0 } &&
        totalTime <= type.totalTimeMinutes
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(colors.background),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 130.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("DENEME / EDİTÖR", color = colors.secondary,
                        style = MaterialTheme.typography.labelSmall, letterSpacing = 1.4.sp)
                    Text(if (existing == null) "Yeni Deneme" else "Denemeyi Düzenle",
                        fontSize = 29.sp, fontWeight = FontWeight.Bold, color = colors.onBackground)
                }
                TextButton(onClick = onBack) { Text("Vazgeç") }
            }
        }
        item {
            DashboardPanel(Modifier.fillMaxWidth(), accent = true) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    listOf(ExamType.TYT, ExamType.AYT).forEach { candidate ->
                        FilterChip(selected = type == candidate,
                            onClick = { type = candidate; error = null },
                            label = { Text(candidate.name) }, modifier = Modifier.weight(1f))
                    }
                }
                AnimatedVisibility(type == ExamType.AYT) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        listOf(AytField.SAYISAL to "SAY",
                            AytField.ESIT_AGIRLIK to "EA",
                            AytField.SOZEL to "SÖZ").forEach { (f, label) ->
                            FilterChip(selected = field == f, onClick = { field = f; error = null },
                                label = { Text(label) })
                        }
                    }
                }
                Spacer(Modifier.height(9.dp))
                OutlinedTextField(title, { title = it; error = null },
                    label = { Text("Deneme adı") }, modifier = Modifier.fillMaxWidth(),
                    singleLine = true, shape = RoundedCornerShape(15.dp))
                Spacer(Modifier.height(9.dp))
                OutlinedTextField(publisher, { publisher = it },
                    label = { Text("Yayınevi") }, modifier = Modifier.fillMaxWidth(),
                    singleLine = true, shape = RoundedCornerShape(15.dp))
                Spacer(Modifier.height(9.dp))
                OutlinedButton(onClick = { onPickDate(date) { date = it } },
                    modifier = Modifier.fillMaxWidth()) { Text("Tarih  ·  $date") }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                DashboardMetric("TOPLAM NET", Fmt.net(totalNet), Modifier.weight(1f),
                    "Canlı hesaplama", colors.primary)
                DashboardMetric("SÜRE", "$totalTime dk", Modifier.weight(1f),
                    "En fazla ${type.totalTimeMinutes} dk")
            }
        }
        item {
            DashboardPanel(Modifier.fillMaxWidth()) {
                Text("SÜRE DAĞILIMI", color = colors.secondary,
                    style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = {
                        (totalTime.toFloat() / type.totalTimeMinutes.coerceAtLeast(1))
                            .coerceIn(0f, 1f)
                    },
                    modifier = Modifier.fillMaxWidth().height(9.dp),
                    color = if (totalTime > type.totalTimeMinutes) colors.error else colors.primary,
                    trackColor = colors.surfaceVariant
                )
                Spacer(Modifier.height(7.dp))
                Text("$totalTime / ${type.totalTimeMinutes} dakika",
                    color = if (totalTime > type.totalTimeMinutes) colors.error
                        else colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            DashboardPanel(Modifier.fillMaxWidth()) {
                Text("BRANŞLAR", color = colors.secondary,
                    style = MaterialTheme.typography.labelSmall, letterSpacing = 1.4.sp)
                Spacer(Modifier.height(5.dp))
                Text("$totalCorrect doğru  ·  $totalWrong yanlış  ·  " +
                    "${rows.sumOf { it.blank }} boş",
                    color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
        items(rows, key = { it.config.name }) { row ->
            DashboardPanel(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(row.config.name, fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                        Text("${row.config.questionCount} soru  ·  ${row.blank} boş",
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall)
                    }
                    Text("${Fmt.net(row.net)} net", color = colors.primary,
                        fontSize = 21.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = {
                        ((row.c + row.w).toFloat() / row.config.questionCount.coerceAtLeast(1))
                            .coerceIn(0f, 1f)
                    },
                    modifier = Modifier.fillMaxWidth().height(5.dp),
                    color = colors.primary,
                    trackColor = colors.surfaceVariant
                )
                Spacer(Modifier.height(5.dp))
                Text("${row.c + row.w} / ${row.config.questionCount} soru girildi",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant)
                Spacer(Modifier.height(11.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumericEntry("Doğru", row.correct, { row.correct = it }, Modifier.weight(1f))
                    NumericEntry("Yanlış", row.wrong, { row.wrong = it }, Modifier.weight(1f))
                    NumericEntry("Dakika", row.minutes, { row.minutes = it }, Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(row.topics, { row.topics = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    label = { Text("Tekrar edilecek konular / kazanımlar (virgülle)") },
                    shape = RoundedCornerShape(13.dp),
                    textStyle = MaterialTheme.typography.bodySmall)
                val choices = remember(learningOutcomes, type, row.config.name) {
                    learningOutcomes.filter {
                        it.examType == type.name && it.subject == row.config.name
                    }
                }
                if (choices.isNotEmpty()) {
                    var menuOpen by remember(type, row.config.name) { mutableStateOf(false) }
                    Box {
                        TextButton(onClick = { menuOpen = true }) {
                            Text("+ Kazanım listesinden işaretle",
                                style = MaterialTheme.typography.labelMedium)
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = {
                            menuOpen = false
                        }) {
                            choices.forEach { choice ->
                                val tagged = row.topics.split(",").map { it.trim() }
                                val checked = choice.title in tagged
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(checked = checked,
                                                onCheckedChange = null)
                                            Column {
                                                Text(choice.topic,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = colors.onSurfaceVariant)
                                                Text(choice.title, maxLines = 2,
                                                    style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    },
                                    onClick = {
                                        val current = row.topics.split(",").map { it.trim() }
                                            .filter { it.isNotBlank() }
                                        row.topics = (if (checked) current.filterNot {
                                            it == choice.title
                                        } else current + choice.title).distinct().joinToString(", ")
                                    }
                                )
                            }
                        }
                    }
                    Text("Burada işaretlediğin kazanımlar tekrar radarında değerlendirilir.",
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall)
                }
                if (row.error != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(row.error.orEmpty(), color = colors.error,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            DashboardPanel(Modifier.fillMaxWidth()) {
                Text("NOTLAR", color = colors.secondary,
                    style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(notes, { notes = it },
                    modifier = Modifier.fillMaxWidth(), minLines = 3,
                    label = { Text("Deneme notları") },
                    shape = RoundedCornerShape(14.dp))
            }
        }
        item {
            if (totalTime > type.totalTimeMinutes)
                Text("Toplam süre sınırı aşıldı.", color = colors.error)
            if (error != null) Text(error.orEmpty(), color = colors.error)
            Button(onClick = {
                if (title.isBlank()) {
                    error = "Deneme adı gir."
                } else {
                    val draft = Exam(
                        id = existing?.id ?: 0L, title = title.trim(), date = date.toString(),
                        type = type, aytField = if (type == ExamType.AYT) field else null,
                        subjects = rows.map { row ->
                            SubjectResult(subject = row.config.name,
                                questionCount = row.config.questionCount,
                                correct = row.c, wrong = row.w, timeMinutes = row.time,
                                weakTopics = row.topics.split(",").map { it.trim() }
                                    .filter { it.isNotEmpty() })
                        }, notes = notes.trim(), publisher = publisher.trim()
                    )
                    error = onSave(draft)
                }
            }, enabled = valid,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(17.dp)) {
                Text(if (existing == null) "Denemeyi Kaydet" else "Değişiklikleri Kaydet")
            }
        }
    }
}

@Composable
private fun NumericEntry(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(value, { input ->
        if (input.isEmpty() || (input.all { it.isDigit() } && input.length <= 4))
            onChange(input)
    }, modifier = modifier, label = { Text(label) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(13.dp),
        textStyle = MaterialTheme.typography.bodyMedium)
}
