package com.deniz0706.ykstakip.ui.study

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.ykstakip.model.LearningOutcome
import com.deniz0706.ykstakip.data.LearningSubtopics
import com.deniz0706.ykstakip.model.AytField
import com.deniz0706.ykstakip.util.LearningFields
import com.deniz0706.ykstakip.model.QuestionCard
import com.deniz0706.ykstakip.model.QuestionStatus
import com.deniz0706.ykstakip.ui.components.DashboardPanel
import com.deniz0706.ykstakip.ui.components.DashboardProgress
import com.deniz0706.ykstakip.ui.components.DashboardRing
import com.deniz0706.ykstakip.util.ReviewSignal
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

@Composable
fun LearningHubPanel(
    outcomes: List<LearningOutcome>,
    questions: List<QuestionCard>,
    signals: List<ReviewSignal>,
    startingExamType: String = "TYT",
    startingSubject: String = "",
    learningField: AytField,
    onLearningFieldChange: (AytField) -> Unit,
    modifier: Modifier = Modifier,
    onCompleted: (String, Boolean) -> Unit,
    onSubtopicCompleted: (String, String, Boolean) -> Unit,
    onWrong: (String) -> Unit,
    onRequestReview: (String) -> Unit,
    onReviewed: (String) -> Unit,
    onPostponeReview: (String) -> Unit,
    onCreateOutcome: (String, String, String, String) -> Unit,
    onDeleteOutcome: (String) -> Unit,
    onCreateQuestion: (String, String, String, QuestionStatus) -> String,
    onDeleteQuestion: (String) -> Unit,
    onReviewQuestion: (String, Boolean) -> Unit,
    onMarkUnsolved: (String) -> Unit,
    onCapture: (String) -> Unit,
    onGallery: (String) -> Unit,
    onRemovePhoto: (String, String) -> Unit
) {
    val c = MaterialTheme.colorScheme
    var pane by remember { mutableStateOf("outcomes") }
    var examType by remember(startingExamType) { mutableStateOf(startingExamType) }
    var subjectFilter by remember(startingExamType, startingSubject) { mutableStateOf(startingSubject) }
    var topicFilter by remember { mutableStateOf("") }
    var subjectMenu by remember { mutableStateOf(false) }
    var createOutcome by remember { mutableStateOf(false) }
    var createQuestion by remember { mutableStateOf(false) }
    var actionFeedback by remember { mutableStateOf<String?>(null) }
    var focusedReviewId by remember { mutableStateOf<String?>(null) }
    var deleteQuestion by remember { mutableStateOf<String?>(null) }
    var outcomeSearch by remember { mutableStateOf("") }
    var outcomeStatus by remember { mutableStateOf("all") }
    var expandedOutcomeId by remember { mutableStateOf<String?>(null) }
    var questionSearch by remember { mutableStateOf("") }
    var questionStatus by remember { mutableStateOf("open") }

    val scoped = remember(outcomes, examType, learningField) {
        outcomes.filter { it.examType == examType &&
            LearningFields.includes(examType, it.subject, learningField) }
    }
    val subjects = remember(scoped) { scoped.map { it.subject }.distinct() }
    val subject = subjectFilter.takeIf { it in subjects } ?: subjects.firstOrNull().orEmpty()
    val courseOutcomes = remember(scoped, subject) { scoped.filter { it.subject == subject } }
    val topics = remember(courseOutcomes) { courseOutcomes.map { it.topic }.distinct() }
    val topic = topicFilter.takeIf { it in topics } ?: "Tümü"
    val available = remember(outcomes) { outcomes.associateBy { it.id } }
    val linkedQuestionIds = remember(questions) { questions.map { it.outcomeId }.toSet() }
    val courseQuestions = remember(questions, available, examType, subject) {
        questions.filter { card ->
            available[card.outcomeId]?.let { it.examType == examType && it.subject == subject } == true
        }
    }
    val topicStats = remember(courseOutcomes) {
        courseOutcomes.groupBy { it.topic }.mapValues { (_, items) ->
            items.count { it.completed } to items.size
        }
    }
    val filtered = remember(courseOutcomes, topic, outcomeSearch, outcomeStatus) {
        courseOutcomes.filter { outcome ->
            (topic == "Tümü" || outcome.topic == topic) &&
                (outcomeSearch.isBlank() ||
                    outcome.topic.contains(outcomeSearch.trim(), ignoreCase = true) ||
                    outcome.title.contains(outcomeSearch.trim(), ignoreCase = true)) &&
                when (outcomeStatus) {
                    "pending" -> !outcome.completed
                    "done" -> outcome.completed
                    "wrong" -> outcome.wrongCount > 0
                    else -> true
                }
        }.groupBy { it.topic }.values.flatten()
    }
    val visibleQuestions = remember(courseQuestions, available, questionSearch, questionStatus) {
        courseQuestions.filter { card ->
            val linked = available[card.outcomeId]
            (questionSearch.isBlank() || listOf(
                card.title, card.notes, linked?.title.orEmpty(), linked?.topic.orEmpty()
            ).any { it.contains(questionSearch.trim(), ignoreCase = true) }) &&
                when (questionStatus) {
                    "open" -> card.status != QuestionStatus.COZULDU
                    "done" -> card.status == QuestionStatus.COZULDU
                    "due" -> card.status != QuestionStatus.COZULDU &&
                        card.dueDate.isNotBlank() && card.dueDate <= LocalDate.now().toString()
                    else -> true
                }
        }.sortedWith(compareByDescending<QuestionCard> { it.createdAt }.thenBy { it.id })
    }
    val activeReview = focusedReviewId?.let { available[it] }?.takeIf {
        it.examType == examType && it.subject == subject
    }
    val relevantSignals = remember(signals, examType, subject) {
        signals.filter { it.outcome.examType == examType && it.outcome.subject == subject }
    }
    val dueQuestions = remember(courseQuestions) {
        courseQuestions.filter {
            it.status != QuestionStatus.COZULDU && it.dueDate.isNotBlank() &&
                it.dueDate <= LocalDate.now().toString()
        }
    }
    val doneInCourse = courseOutcomes.count { it.completed }
    val completed = scoped.count { it.completed }
    val percent = if (scoped.isEmpty()) 0 else completed * 100 / scoped.size
    val activeCount = questions.count { it.status != QuestionStatus.COZULDU }
    val urgentCount = signals.count { it.urgent }
    val dueCount = questions.count {
        it.status != QuestionStatus.COZULDU &&
            it.dueDate.isNotBlank() && it.dueDate <= LocalDate.now().toString()
    }
    val listState = rememberLazyListState()
    LaunchedEffect(pane, examType, subject, topic, focusedReviewId) {
        listState.scrollToItem(0)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "learning-overview") {
            DashboardPanel(Modifier.fillMaxWidth(), accent = true) {
                Text("ÖĞRENME MERKEZİ", color = c.secondary,
                    style = MaterialTheme.typography.labelSmall, letterSpacing = 1.1.sp)
                Text("Bilmediğini takip et, öğrendiğini işaretle.",
                    color = c.onSurface, style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DashboardRing(
                        if (scoped.isEmpty()) 0f else completed.toFloat() / scoped.size,
                        "$percent%", diameter = 82.dp, caption = examType
                    )
                    Column(Modifier.weight(1f)) {
                        Text("$completed / ${scoped.size} konu tamamlandı", color = c.onSurface,
                            style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("$activeCount açık soru · $urgentCount öncelikli · $dueCount günü gelmiş",
                            color = c.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(7.dp))
                        DashboardProgress(
                            if (scoped.isEmpty()) 0f else completed.toFloat() / scoped.size,
                            Modifier.fillMaxWidth()
                        )
                    }
                }
                Text("Konu başlıkları müfredat odaklıdır; eksik gördüğün konuyu ekleyebilirsin.",
                    color = c.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            }
        }
        if (actionFeedback != null) {
            item(key = "feedback") {
                Surface(color = c.primaryContainer, shape = RoundedCornerShape(14.dp)) {
                    Row(Modifier.fillMaxWidth().padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(actionFeedback.orEmpty(), modifier = Modifier.weight(1f),
                            color = c.onPrimaryContainer, style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { actionFeedback = null }) { Text("✕") }
                    }
                }
            }
        }
        item(key = "learning-navigation") {
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf("outcomes" to "Konular", "questions" to "Sorular", "review" to "Tekrar")
                        .forEach { (key, label) ->
                            FilterChip(selected = pane == key, onClick = {
                                pane = key
                                if (key != "review") focusedReviewId = null
                            }, label = { Text(label, maxLines = 1) }, modifier = Modifier.weight(1f))
                        }
                }
                if (examType == "AYT") {
                    Text("AYT alanı", color = c.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        listOf(AytField.SAYISAL to "Sayısal",
                            AytField.ESIT_AGIRLIK to "Eşit Ağırlık",
                            AytField.SOZEL to "Sözel").forEach { (field, label) ->
                            FilterChip(
                                selected = learningField == field,
                                onClick = {
                                    onLearningFieldChange(field)
                                    subjectFilter = ""
                                    topicFilter = ""
                                    focusedReviewId = null
                                },
                                label = { Text(label) }
                            )
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = examType == "TYT", onClick = {
                        examType = "TYT"; subjectFilter = ""; topicFilter = ""; focusedReviewId = null
                    }, label = { Text("TYT") })
                    FilterChip(selected = examType == "AYT", onClick = {
                        examType = "AYT"; subjectFilter = ""; topicFilter = ""; focusedReviewId = null
                    }, label = { Text("AYT") })
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(onClick = { subjectMenu = true }, modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)) {
                            Text(subject.ifBlank { "Ders" } + " ▾", maxLines = 1,
                                overflow = TextOverflow.Ellipsis)
                        }
                        DropdownMenu(expanded = subjectMenu, onDismissRequest = { subjectMenu = false }) {
                            subjects.forEach { item ->
                                DropdownMenuItem(text = { Text(item) }, onClick = {
                                    subjectFilter = item; topicFilter = ""
                                    focusedReviewId = null; subjectMenu = false
                                })
                            }
                        }
                    }
                }
            }
        }
        when (pane) {
            "outcomes" -> {
                item(key = "outcome-controls") {
                    DashboardPanel(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(subject, color = c.onSurface, fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium)
                                Text("$doneInCourse / ${courseOutcomes.size} konu",
                                    color = c.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(if (courseOutcomes.isEmpty()) "—" else
                                "${doneInCourse * 100 / courseOutcomes.size}%",
                                color = c.primary, fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineSmall)
                        }
                        Spacer(Modifier.height(8.dp))
                        DashboardProgress(if (courseOutcomes.isEmpty()) 0f
                            else doneInCourse.toFloat() / courseOutcomes.size,
                            Modifier.fillMaxWidth())
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(value = outcomeSearch, onValueChange = { outcomeSearch = it },
                            label = { Text("Konu ara") },
                            singleLine = true, modifier = Modifier.fillMaxWidth())
                        Row(Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            listOf("all" to "Tümü", "pending" to "Eksikler",
                                "done" to "Tamamlanan", "wrong" to "Yanlışlar").forEach { (key,label) ->
                                FilterChip(selected = outcomeStatus == key,
                                    onClick = { outcomeStatus = key }, label = { Text(label) })
                            }
                        }
                        if (topics.size > 1) {
                            Row(Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                (listOf("Tümü") + topics).forEach { t ->
                                    FilterChip(selected = t == topic, onClick = { topicFilter = t },
                                        label = { Text(t, maxLines = 1) })
                                }
                            }
                        }
                        Text("${filtered.size} sonuç · Konular ekranda göründükçe yüklenir",
                            color = c.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    }
                }
                if (filtered.isEmpty()) {
                    item(key = "no-outcomes") {
                        Text("Bu filtrelerle eşleşen konu yok.", color = c.onSurfaceVariant)
                    }
                }
                itemsIndexed(filtered, key = { _, outcome -> "outcome:" + outcome.id }) { index, outcome ->
                    val subtopics = remember(outcome.examType, outcome.subject, outcome.title) {
                        LearningSubtopics.forOutcome(outcome)
                    }
                    val finishedSubtopics = if (outcome.completed && outcome.completedSubtopics.isEmpty())
                        subtopics.toSet() else outcome.completedSubtopics.toSet()
                    val finishedCount = subtopics.count { it in finishedSubtopics }
                    val expanded = expandedOutcomeId == outcome.id && subtopics.isNotEmpty()
                    if (index == 0 || filtered[index - 1].topic != outcome.topic) {
                        val stats = topicStats[outcome.topic] ?: (0 to 0)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(outcome.topic, color = c.secondary, fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Text("${stats.first}/${stats.second}", color = c.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Surface(Modifier.fillMaxWidth(), color = c.surface,
                        shape = RoundedCornerShape(13.dp)) {
                        Column(Modifier.padding(horizontal = 9.dp, vertical = 6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = outcome.completed,
                                    onCheckedChange = { onCompleted(outcome.id, it) })
                                Column(
                                    Modifier.weight(1f).then(
                                        if (subtopics.isNotEmpty()) Modifier.clickable {
                                            expandedOutcomeId = if (expanded) null else outcome.id
                                        } else Modifier
                                    )
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            outcome.title,
                                            color = c.onSurface,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (subtopics.isNotEmpty()) {
                                            Text(if (expanded) "▾" else "▸",
                                                color = c.primary,
                                                style = MaterialTheme.typography.titleMedium)
                                        }
                                    }
                                    if (subtopics.isNotEmpty()) {
                                        Text("$finishedCount / ${subtopics.size} alt konu tamamlandı",
                                            color = c.onSurfaceVariant,
                                            style = MaterialTheme.typography.labelSmall)
                                        Spacer(Modifier.height(4.dp))
                                        DashboardProgress(
                                            finishedCount.toFloat() / subtopics.size,
                                            Modifier.fillMaxWidth()
                                        )
                                    }
                                    if (outcome.wrongCount > 0) {
                                        Text("Yanlış işaretlemesi: ${outcome.wrongCount}",
                                            color = c.onSurfaceVariant,
                                            style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                            if (expanded) {
                                Spacer(Modifier.height(6.dp))
                                HorizontalDivider(color = c.outlineVariant)
                                Column(Modifier.padding(start = 23.dp, top = 4.dp, bottom = 4.dp)) {
                                    subtopics.forEach { subtopic ->
                                        Row(verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()) {
                                            Checkbox(
                                                checked = subtopic in finishedSubtopics,
                                                onCheckedChange = {
                                                    onSubtopicCompleted(outcome.id, subtopic, it)
                                                }
                                            )
                                            Text(
                                                subtopic,
                                                modifier = Modifier.weight(1f).clickable {
                                                    onSubtopicCompleted(
                                                        outcome.id, subtopic, subtopic !in finishedSubtopics
                                                    )
                                                },
                                                color = c.onSurface,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = {
                                    onWrong(outcome.id)
                                    actionFeedback = "Yanlış işaretlendi. Bu konu yarın tekrar önerilecek."
                                }) {
                                    Text("+ Yanlış", color = c.error,
                                        style = MaterialTheme.typography.labelSmall)
                                }
                                TextButton(onClick = {
                                    onRequestReview(outcome.id)
                                    focusedReviewId = outcome.id
                                    pane = "review"
                                    actionFeedback = null
                                }) {
                                    Text("↻ Tekrar et", style = MaterialTheme.typography.labelSmall)
                                }
                                if (outcome.custom && outcome.id !in linkedQuestionIds) {
                                    TextButton(onClick = { onDeleteOutcome(outcome.id) }) {
                                        Text("Sil", color = c.onSurfaceVariant,
                                            style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
                item(key = "new-outcome") {
                    Button(onClick = { createOutcome = true },
                        modifier = Modifier.fillMaxWidth()) { Text("+ Kendi konunu ekle") }
                }
            }
            "questions" -> {
                item(key = "question-controls") {
                    DashboardPanel(Modifier.fillMaxWidth()) {
                        Text("SORU DEFTERİ", color = c.secondary,
                            style = MaterialTheme.typography.labelSmall)
                        Text("Çözemediğin soruları kaybetme", color = c.onSurface,
                            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Fotoğraflar cihazda saklanır; ZIP ile ayrıca yedekleyebilirsin.",
                            color = c.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = { createQuestion = true },
                            modifier = Modifier.fillMaxWidth(), enabled = courseOutcomes.isNotEmpty()) {
                            Text("+ Soru ekle")
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = questionSearch, onValueChange = { questionSearch = it },
                            label = { Text("Soru, kaynak veya konu ara") }, singleLine = true,
                            modifier = Modifier.fillMaxWidth())
                        Row(Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            listOf("open" to "Bekleyen", "due" to "Bugün",
                                "all" to "Tümü", "done" to "Çözüldü").forEach { (key,label) ->
                                FilterChip(selected = questionStatus == key,
                                    onClick = { questionStatus = key }, label = { Text(label) })
                            }
                        }
                        Text("${visibleQuestions.size} soru bulundu",
                            color = c.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    }
                }
                if (visibleQuestions.isEmpty()) {
                    item(key = "no-questions") {
                        Text("Bu filtrelerde soru bulunamadı.", color = c.onSurfaceVariant)
                    }
                }
                items(visibleQuestions, key = { "question:" + it.id }) { card ->
                    DashboardPanel(Modifier.fillMaxWidth()) {
                        QuestionJournalCard(card, available[card.outcomeId], onCapture,
                            onGallery, onReviewQuestion, onMarkUnsolved, onRemovePhoto,
                            onDelete = { deleteQuestion = it })
                    }
                }
            }
            else -> {
                if (activeReview != null) {
                    item(key = "active-review:" + activeReview.id) {
                        ReviewSessionPanel(
                            outcome = activeReview,
                            linkedQuestions = questions.filter { it.outcomeId == activeReview.id },
                            onBack = { focusedReviewId = null },
                            onLearned = {
                                onReviewed(activeReview.id)
                                focusedReviewId = null
                                actionFeedback = "Konu öğrenildi; sonraki tekrar planlandı. Açık soruların kayıtlı kaldı."
                            },
                            onNotSolved = {
                                onWrong(activeReview.id)
                                focusedReviewId = null
                                actionFeedback = "Zorlandığın kaydedildi; yarın tekrar önerilecek."
                            },
                            onPostpone = {
                                onPostponeReview(activeReview.id)
                                focusedReviewId = null
                                actionFeedback = "Konu yarına ertelendi."
                            },
                            onCapture = onCapture, onGallery = onGallery,
                            onReviewQuestion = onReviewQuestion,
                            onMarkUnsolved = onMarkUnsolved,
                            onRemovePhoto = onRemovePhoto,
                            onDeleteQuestion = { deleteQuestion = it }
                        )
                    }
                } else {
                    item(key = "review-intro") {
                        DashboardPanel(Modifier.fillMaxWidth()) {
                            Text("TEKRAR RADARI", color = c.secondary,
                                style = MaterialTheme.typography.labelSmall)
                            Text("Neye tekrar bakmalıyım?", color = c.onSurface,
                                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Denemelerde işaretlediğin konular, yanlışların ve açık soruların değerlendirilir.",
                                color = c.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(8.dp))
                            Text("${relevantSignals.size} öneri",
                                color = c.secondary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (relevantSignals.isEmpty()) {
                        item(key = "no-review") {
                            Text("Bu ders için şu an tekrar önerisi yok.", color = c.onSurfaceVariant)
                        }
                    }
                    items(relevantSignals, key = { "review:" + it.outcome.id }) { alert ->
                        DashboardPanel(Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(alert.outcome.topic, color = c.secondary,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.labelSmall)
                                Text(if (alert.urgent) "ÖNCELİKLİ" else "TEKRAR",
                                    color = if (alert.urgent) c.error else c.primary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold)
                            }
                            Text(alert.outcome.title, color = c.onSurface,
                                fontWeight = FontWeight.SemiBold)
                            Text(alert.explanation, color = c.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall)
                            TextButton(onClick = {
                                focusedReviewId = alert.outcome.id
                                actionFeedback = null
                            }) { Text("Çalışmaya başla →") }
                        }
                    }
                    if (dueQuestions.isNotEmpty()) {
                        item(key = "due-question-title") {
                            Text("GÜNÜ GELEN SORULAR", color = c.secondary,
                                style = MaterialTheme.typography.labelSmall)
                        }
                        items(dueQuestions, key = { "due:" + it.id }) { card ->
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(card.title.ifBlank { available[card.outcomeId]?.title.orEmpty() },
                                    modifier = Modifier.weight(1f), color = c.onSurface,
                                    style = MaterialTheme.typography.bodyMedium)
                                TextButton(onClick = {
                                    focusedReviewId = card.outcomeId
                                    actionFeedback = null
                                }) { Text("Soruyu aç →") }
                            }
                        }
                    }
                    item(key = "review-caveat") {
                        Text("Öneriler kayıtlı verilere dayanır; fotoğrafların içeriği otomatik okunmaz.",
                            color = c.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }

    if (createOutcome) {
        var outcomeText by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { createOutcome = false },
            title = { Text("Konu ekle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("$examType · $subject", color = c.onSurfaceVariant)
                    OutlinedTextField(value = outcomeText, onValueChange = { outcomeText = it },
                        label = { Text("Ünite / konu başlığı") },
                        singleLine = true)
                }
            },
            confirmButton = { TextButton(onClick = {
                if (subject.isNotBlank() && outcomeText.isNotBlank()) {
                    onCreateOutcome(examType, subject, subject, outcomeText)
                    createOutcome = false
                }
            }) { Text("Ekle") } },
            dismissButton = { TextButton(onClick = { createOutcome = false }) { Text("Vazgeç") } })
    }

    if (createQuestion) {
        var selection by remember { mutableStateOf("") }
        var showOutcomes by remember { mutableStateOf(false) }
        var outcomeSearch by remember { mutableStateOf("") }
        var title by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        var status by remember { mutableStateOf(QuestionStatus.COZULEMEDI) }
        val options = courseOutcomes
        // Never silently attach a question to the first outcome in a subject.
        val selectedOutcome = options.find { it.id == selection }
        AlertDialog(onDismissRequest = { createQuestion = false },
            title = { Text("Soru defterine ekle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("Konuyu seç; soruyu kamera veya galeriden fotoğrafıyla ekleyebilirsin.",
                        color = c.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Box {
                        OutlinedButton(onClick = { showOutcomes = true },
                            modifier = Modifier.fillMaxWidth()) {
                            Text(selectedOutcome?.let { it.topic + " · " + it.title }
                                ?: "Konu seç", maxLines = 2,
                                overflow = TextOverflow.Ellipsis)
                        }
                        DropdownMenu(expanded = showOutcomes, onDismissRequest = {
                            showOutcomes = false
                        }) {
                            OutlinedTextField(
                                value = outcomeSearch,
                                onValueChange = { outcomeSearch = it },
                                label = { Text("Konu ara") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                            )
                            val matching = options.filter {
                                outcomeSearch.isBlank() ||
                                    it.topic.contains(outcomeSearch, ignoreCase = true) ||
                                    it.title.contains(outcomeSearch, ignoreCase = true)
                            }
                            matching.take(30).forEach { o ->
                                DropdownMenuItem(
                                    text = { Text(o.topic + " · " + o.title, maxLines = 2) },
                                    onClick = {
                                        selection = o.id
                                        showOutcomes = false
                                        outcomeSearch = ""
                                    })
                            }
                            if (matching.isEmpty()) {
                                Text("Eşleşen konu yok",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall)
                            } else if (matching.size > 30) {
                                Text("Daha fazla sonuç için aramayı daralt",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    OutlinedTextField(title, { title = it }, label = { Text("Soru adı / kaynak (isteğe bağlı)") },
                        singleLine = true)
                    OutlinedTextField(notes, { notes = it },
                        label = { Text("Neden çözemedim? / not") }, minLines = 2)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(QuestionStatus.COZULEMEDI to "Çözemediğim",
                            QuestionStatus.GERI_DON to "Geri dön").forEach { (value,label) ->
                            FilterChip(selected = status == value, onClick = { status = value },
                                label = { Text(label) })
                        }
                    }
                }
            },
            confirmButton = {
                Column(horizontalAlignment = Alignment.End) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        OutlinedButton(enabled = selectedOutcome != null, onClick = {
                            selectedOutcome?.let {
                                val id = onCreateQuestion(it.id, title, notes, status)
                                createQuestion = false
                                pane = "questions"
                                actionFeedback = "Soru oluşturuldu. Fotoğraf çekimini iptal etsen de soru defterinde kalacak."
                                onCapture(id)
                            }
                        }) { Text("📷 Çekerek ekle", style = MaterialTheme.typography.labelSmall) }
                        OutlinedButton(enabled = selectedOutcome != null, onClick = {
                            selectedOutcome?.let {
                                val id = onCreateQuestion(it.id, title, notes, status)
                                createQuestion = false
                                pane = "questions"
                                actionFeedback = "Soru oluşturuldu. Galeriden fotoğraf seçmezsen daha sonra ekleyebilirsin."
                                onGallery(id)
                            }
                        }) { Text("＋ Galeri", style = MaterialTheme.typography.labelSmall) }
                    }
                    TextButton(enabled = selectedOutcome != null, onClick = {
                        selectedOutcome?.let {
                            onCreateQuestion(it.id, title, notes, status)
                            createQuestion = false
                            pane = "questions"
                            actionFeedback = "Soru defterine eklendi; istersen sonra fotoğraf iliştirebilirsin."
                        }
                    }) { Text("Fotoğrafsız kaydet") }
                }
            },
            dismissButton = { TextButton(onClick = { createQuestion = false }) { Text("Vazgeç") } })
    }

    if (deleteQuestion != null) {
        val id = deleteQuestion.orEmpty()
        AlertDialog(onDismissRequest = { deleteQuestion = null },
            title = { Text("Soruyu sil?") },
            text = { Text("Bu sorunun fotoğrafları da telefondan silinecek.") },
            confirmButton = { TextButton(onClick = {
                onDeleteQuestion(id); deleteQuestion = null
            }) { Text("Sil", color = c.error) } },
            dismissButton = { TextButton(onClick = { deleteQuestion = null }) { Text("Vazgeç") } })
    }
}


@Composable
private fun ReviewSessionPanel(
    outcome: LearningOutcome,
    linkedQuestions: List<QuestionCard>,
    onBack: () -> Unit,
    onLearned: () -> Unit,
    onNotSolved: () -> Unit,
    onPostpone: () -> Unit,
    onCapture: (String) -> Unit,
    onGallery: (String) -> Unit,
    onReviewQuestion: (String, Boolean) -> Unit,
    onMarkUnsolved: (String) -> Unit,
    onRemovePhoto: (String, String) -> Unit,
    onDeleteQuestion: (String) -> Unit
) {
    val c = MaterialTheme.colorScheme
    val ordered = remember(linkedQuestions) {
        linkedQuestions.sortedWith(
            compareBy<QuestionCard> { it.status == QuestionStatus.COZULDU }
                .thenBy { it.dueDate }
                .thenBy { it.createdAt }
        )
    }
    var showCount by remember(outcome.id) { mutableIntStateOf(4) }
    val openCount = linkedQuestions.count { it.status != QuestionStatus.COZULDU }
    DashboardPanel(Modifier.fillMaxWidth(), accent = true) {
        TextButton(onClick = onBack) {
            Text("← Tekrar listesine dön")
        }
        Text("TEKRAR OTURUMU", color = c.secondary,
            style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
        Text(outcome.topic, color = c.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium)
        Text(outcome.title, color = c.onSurface,
            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Önce konuyu hatırla ve aşağıdaki soruları yeniden çöz. Sonra gerçekten nasıl geçtiğini işaretle.",
            color = c.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        if (outcome.lastReviewedDate.isNotBlank()) {
            Text("Son tekrar: " + outcome.lastReviewedDate,
                color = c.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.height(10.dp))
        Text("$openCount açık soru · ${linkedQuestions.size} bağlantılı soru",
            color = c.secondary, fontWeight = FontWeight.SemiBold)
        if (ordered.isEmpty()) {
            Text("Bu konua eklenmiş soru yok. Konuyu kaynaklarından çalışıp aşağıda sonucu işaretleyebilirsin.",
                color = c.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        } else {
            ordered.take(showCount).forEach { card ->
                QuestionJournalCard(
                    card = card, outcome = outcome,
                    onCapture = onCapture, onGallery = onGallery,
                    onReviewQuestion = onReviewQuestion,
                    onMarkUnsolved = onMarkUnsolved,
                    onRemovePhoto = onRemovePhoto,
                    onDelete = onDeleteQuestion
                )
            }
            if (ordered.size > showCount) {
                TextButton(onClick = { showCount += 4 }, modifier = Modifier.fillMaxWidth()) {
                    Text("Diğer ${ordered.size - showCount} soruyu göster")
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = c.outlineVariant)
        Spacer(Modifier.height(12.dp))
        Text("Tekrar nasıl geçti?", color = c.onSurface, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(5.dp))
        Button(onClick = onLearned, modifier = Modifier.fillMaxWidth()) {
            Text("✓ Konuyu öğrendim")
        }
        OutlinedButton(onClick = onNotSolved, modifier = Modifier.fillMaxWidth()) {
            Text("✕ Yine çözemedim")
        }
        OutlinedButton(onClick = onPostpone, modifier = Modifier.fillMaxWidth()) {
            Text("↻ Daha fazla çalışmam gerek")
        }
        Text("Öğrendim: sonraki tekrar 1 / 3 / 7 / 14 / 30 gün aralıklarıyla planlanır. " +
            "Zorlandım veya daha fazla çalışmam gerek: yarına alınır. " +
            "Konu değerlendirmesi açık soru kayıtlarını otomatik kapatmaz.",
            color = c.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun QuestionJournalCard(
    card: QuestionCard,
    outcome: LearningOutcome?,
    onCapture: (String) -> Unit,
    onGallery: (String) -> Unit,
    onReviewQuestion: (String, Boolean) -> Unit,
    onMarkUnsolved: (String) -> Unit,
    onRemovePhoto: (String, String) -> Unit,
    onDelete: (String) -> Unit
) {
    val c = MaterialTheme.colorScheme
    val context = LocalContext.current
    var fullPhoto by remember(card.id) { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxWidth().padding(top = 12.dp)) {
        HorizontalDivider(color = c.outlineVariant)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(card.title.ifBlank { outcome?.title ?: "Soru" },
                    color = c.onSurface, fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium)
                Text((outcome?.topic ?: "Konu") + " · " +
                    when(card.status) {
                        QuestionStatus.COZULEMEDI -> "Çözülemedi"
                        QuestionStatus.GERI_DON -> "Geri dönülecek"
                        QuestionStatus.COZULDU -> "Çözüldü"
                    }, color = c.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                if (card.status != QuestionStatus.COZULDU) {
                    Text("Tekrar: " + card.dueDate, color = c.secondary,
                        style = MaterialTheme.typography.labelSmall)
                }
            }
            TextButton(onClick = { onDelete(card.id) }) {
                Text("Sil", color = c.error, style = MaterialTheme.typography.labelSmall)
            }
        }
        if (card.notes.isNotBlank()) {
            Text(card.notes, color = c.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall)
        }
        if (card.photos.isNotEmpty()) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                card.photos.forEach { filename ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val bitmap by produceState<android.graphics.Bitmap?>(
                            initialValue = null, key1 = filename
                        ) {
                            val file = File(context.filesDir, "review_photos/" + File(filename).name)
                            value = withContext(Dispatchers.IO) { decodePhoto(file, 360) }
                        }
                        val decoded = bitmap
                        if (decoded != null) {
                            Image(bitmap = decoded.asImageBitmap(),
                                contentDescription = "Soru fotoğrafını büyüt",
                                modifier = Modifier.size(100.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .clickable { fullPhoto = filename },
                                contentScale = ContentScale.Crop)
                        } else {
                            Box(Modifier.size(100.dp).background(c.surfaceVariant,
                                RoundedCornerShape(11.dp)),
                                contentAlignment = Alignment.Center) {
                                Text("Görsel açılamadı", color = c.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        TextButton(onClick = { onRemovePhoto(card.id, filename) }) {
                            Text("Fotoğrafı sil", color = c.error,
                                style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            OutlinedButton(onClick = { onCapture(card.id) },
                contentPadding = PaddingValues(horizontal = 9.dp),
                modifier = Modifier.weight(1f)) { Text("📷 Kamera", maxLines = 1,
                    style = MaterialTheme.typography.labelMedium) }
            OutlinedButton(onClick = { onGallery(card.id) },
                contentPadding = PaddingValues(horizontal = 9.dp),
                modifier = Modifier.weight(1f)) { Text("+ Galeri", maxLines = 1,
                    style = MaterialTheme.typography.labelMedium) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TextButton(onClick = { onReviewQuestion(card.id, true) }) {
                Text("✓ Çözdüm", style = MaterialTheme.typography.labelMedium)
            }
            TextButton(onClick = { onReviewQuestion(card.id, false) }) {
                Text("↻ Tekrar sonra", style = MaterialTheme.typography.labelMedium)
            }
            if (card.status == QuestionStatus.COZULDU) {
                TextButton(onClick = { onMarkUnsolved(card.id) }) {
                    Text("Geri aç", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
    fullPhoto?.let { filename ->
        val bitmap by produceState<android.graphics.Bitmap?>(
            initialValue = null, key1 = filename
        ) {
            value = withContext(Dispatchers.IO) {
                decodePhoto(File(context.filesDir, "review_photos/" + File(filename).name), 1400)
            }
        }
        AlertDialog(onDismissRequest = { fullPhoto = null },
            title = { Text("Soru fotoğrafı") },
            text = {
                val decoded = bitmap
                if (decoded != null) Image(bitmap = decoded.asImageBitmap(),
                    contentDescription = "Büyütülmüş soru",
                    modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
                    contentScale = ContentScale.Fit)
                else Text("Fotoğraf açılamadı.")
            },
            confirmButton = { TextButton(onClick = { fullPhoto = null }) { Text("Kapat") } })
    }
}

private fun decodePhoto(file: File, maxSide: Int): android.graphics.Bitmap? {
    if (!file.isFile) return null
    return try {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, opts)
        var sample = 1
        while (opts.outWidth / sample > maxSide * 2 || opts.outHeight / sample > maxSide * 2) {
            sample *= 2
        }
        BitmapFactory.decodeFile(file.absolutePath,
            BitmapFactory.Options().apply { inSampleSize = sample })
    } catch (_: Exception) { null }
}
