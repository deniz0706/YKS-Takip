package com.deniz0706.ykstakip.ui.study

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
    onCompleted: (String, Boolean) -> Unit,
    onWrong: (String) -> Unit,
    onRequestReview: (String) -> Unit,
    onReviewed: (String) -> Unit,
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
    var subjectFilter by remember(startingExamType, startingSubject) {
        mutableStateOf(startingSubject)
    }
    var topicFilter by remember { mutableStateOf("") }
    var subjectMenu by remember { mutableStateOf(false) }
    var createOutcome by remember { mutableStateOf(false) }
    var createQuestion by remember { mutableStateOf(false) }
    var actionFeedback by remember { mutableStateOf<String?>(null) }
    var focusedReviewId by remember { mutableStateOf<String?>(null) }
    var outcomePage by remember(examType, subjectFilter, topicFilter) { mutableIntStateOf(0) }
    var deleteQuestion by remember { mutableStateOf<String?>(null) }

    val scoped = remember(outcomes, examType) { outcomes.filter { it.examType == examType } }
    val subjects = remember(scoped) { scoped.map { it.subject }.distinct() }
    val subject = subjectFilter.takeIf { it in subjects } ?: subjects.firstOrNull().orEmpty()
    val courseOutcomes = remember(scoped, subject) { scoped.filter { it.subject == subject } }
    val topics = remember(courseOutcomes) { courseOutcomes.map { it.topic }.distinct() }
    val topic = topicFilter.takeIf { it in topics } ?: "Tümü"
    val filtered = remember(courseOutcomes, topic) {
        if (topic == "Tümü") courseOutcomes else courseOutcomes.filter { it.topic == topic }
    }
    val available = remember(outcomes) { outcomes.associateBy { it.id } }
    val courseQuestions = remember(questions, available, examType, subject) {
        questions.filter { card ->
            available[card.outcomeId]?.let { it.examType == examType && it.subject == subject } == true
        }
    }
    val completed = scoped.count { it.completed }
    val percent = if (scoped.isEmpty()) 0 else completed * 100 / scoped.size
    val active = questions.count { it.status != QuestionStatus.COZULDU }
    val urgent = signals.count { it.urgent }
    val due = questions.count { it.status != QuestionStatus.COZULDU &&
        it.dueDate.isNotBlank() && it.dueDate <= LocalDate.now().toString() }

    DashboardPanel(Modifier.fillMaxWidth(), accent = true) {
        Text("ÖĞRENME MERKEZİ", color = c.secondary, style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.1.sp)
        Spacer(Modifier.height(5.dp))
        Text("Bilmediğini takip et, öğrendiğini işaretle.",
            color = c.onSurface, style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            DashboardRing(if (scoped.isEmpty()) 0f else completed.toFloat() / scoped.size,
                "$percent%", diameter = 82.dp, caption = examType)
            Column(Modifier.weight(1f)) {
                Text("$completed / ${scoped.size} kazanım tamamlandı",
                    color = c.onSurface, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(5.dp))
                Text("$active açık soru · $urgent öncelikli tekrar · $due günü gelmiş soru",
                    color = c.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                DashboardProgress(if (scoped.isEmpty()) 0f else completed.toFloat() / scoped.size,
                    Modifier.fillMaxWidth())
            }
        }
        Spacer(Modifier.height(9.dp))
        Text("Kazanımlar resmî MEB metinlerinin birebir aktarımı değil, müfredat odaklı takip maddeleridir. Eksiklerini de ekleyebilirsin.",
            color = c.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
    }

    if (actionFeedback != null) {
        Surface(color = c.primaryContainer, shape = RoundedCornerShape(14.dp)) {
            Row(Modifier.fillMaxWidth().padding(11.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text(actionFeedback.orEmpty(), modifier = Modifier.weight(1f),
                    color = c.onPrimaryContainer, style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { actionFeedback = null }) { Text("✕") }
            }
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        listOf("outcomes" to "Kazanımlar", "questions" to "Sorular", "review" to "Tekrar")
            .forEach { (key, label) ->
                FilterChip(selected = pane == key, onClick = { pane = key },
                    label = { Text(label, maxLines = 1) }, modifier = Modifier.weight(1f))
            }
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = examType == "TYT", onClick = {
            examType = "TYT"; topicFilter = ""; subjectFilter = ""
        }, label = { Text("TYT") })
        FilterChip(selected = examType == "AYT", onClick = {
            examType = "AYT"; topicFilter = ""; subjectFilter = ""
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
                        subjectFilter = item; topicFilter = ""; subjectMenu = false
                    })
                }
            }
        }
    }

    when (pane) {
        "outcomes" -> {
            val doneInCourse = courseOutcomes.count { it.completed }
            DashboardPanel(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(subject, color = c.onSurface, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium)
                        Text("$doneInCourse / ${courseOutcomes.size} kazanım",
                            color = c.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(if (courseOutcomes.isEmpty()) "—"
                        else "${doneInCourse * 100 / courseOutcomes.size}%",
                        color = c.primary, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall)
                }
                Spacer(Modifier.height(8.dp))
                DashboardProgress(if (courseOutcomes.isEmpty()) 0f
                    else doneInCourse.toFloat() / courseOutcomes.size,
                    Modifier.fillMaxWidth())
                Spacer(Modifier.height(13.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf("Tümü").plus(topics).forEach { t ->
                        FilterChip(selected = t == topic,
                            onClick = { topicFilter = t },
                            label = { Text(t, maxLines = 1) })
                    }
                }
                Spacer(Modifier.height(12.dp))
                // Cap the composed rows: a full curriculum can contain hundreds of
                // outcomes and rendering them all in one LazyColumn item causes jank.
                val pageSize = 25
                val visibleOutcomes = filtered.take((outcomePage + 1) * pageSize)
                visibleOutcomes.groupBy { it.topic }.forEach { (topicName, outcomesInTopic) ->
                    val groupDone = outcomesInTopic.count { it.completed }
                    Row(Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(topicName, color = c.secondary, fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f))
                        Text("$groupDone/${outcomesInTopic.size}",
                            color = c.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.height(5.dp))
                    DashboardProgress(groupDone.toFloat() / outcomesInTopic.size,
                        Modifier.fillMaxWidth())
                    outcomesInTopic.forEach { outcome ->
                        Row(Modifier.fillMaxWidth().padding(top = 12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = outcome.completed,
                                onCheckedChange = { onCompleted(outcome.id, it) })
                            Column(Modifier.weight(1f)) {
                                Text(outcome.title, color = c.onSurface,
                                    style = MaterialTheme.typography.bodyMedium)
                                if (outcome.wrongCount > 0) {
                                    Text("Yanlış işaretlemesi: ${outcome.wrongCount}",
                                        color = c.onSurfaceVariant,
                                        style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = {
                                onWrong(outcome.id)
                                actionFeedback = "Yanlış işaretlendi: bu kazanım yarın yeniden önerilecek."
                            }) {
                                Text("+ Yanlış", color = c.error,
                                    style = MaterialTheme.typography.labelSmall)
                            }
                            TextButton(onClick = {
                                onRequestReview(outcome.id)
                                focusedReviewId = outcome.id
                                pane = "review"
                                actionFeedback = "Tekrar listesi açıldı. Bitirince 'Tekrar ettim' ile sonraki tarihi oluştur."
                            }) {
                                Text("↻ Tekrar et", style = MaterialTheme.typography.labelSmall)
                            }
                            if (outcome.custom && questions.none { it.outcomeId == outcome.id }) {
                                TextButton(onClick = { onDeleteOutcome(outcome.id) }) {
                                    Text("Sil", color = c.onSurfaceVariant,
                                        style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        HorizontalDivider(color = c.outlineVariant)
                    }
                    Spacer(Modifier.height(12.dp))
                }
                if (filtered.size > visibleOutcomes.size) {
                    OutlinedButton(onClick = { outcomePage++ },
                        modifier = Modifier.fillMaxWidth()) {
                        Text("Diğer ${minOf(pageSize, filtered.size - visibleOutcomes.size)} kazanımı göster · ${visibleOutcomes.size}/${filtered.size}")
                    }
                    Spacer(Modifier.height(9.dp))
                }
                Button(onClick = { createOutcome = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("+  Kendi kazanımını ekle")
                }
            }
        }
        "questions" -> {
            var filter by remember { mutableStateOf("open") }
            var visibleCount by remember { mutableIntStateOf(8) }
            DashboardPanel(Modifier.fillMaxWidth()) {
                Text("SORU DEFTERİ", color = c.secondary,
                    style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
                Text("Çözemediğin soruları kaybetme", color = c.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(9.dp))
                Text("Kamerayla çek ya da galeriden seç. Bir soruya birden fazla fotoğraf ekleyebilirsin.",
                    color = c.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(10.dp))
                Button(onClick = { createQuestion = true }, modifier = Modifier.fillMaxWidth(),
                    enabled = courseOutcomes.isNotEmpty()) {
                    Text("+  Soru ekle")
                }
                Spacer(Modifier.height(7.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    FilterChip(selected = filter == "open", onClick = {
                        filter = "open"; visibleCount = 8
                    }, label = { Text("Bekleyen") })
                    FilterChip(selected = filter == "all", onClick = {
                        filter = "all"; visibleCount = 8
                    }, label = { Text("Tümü") })
                    FilterChip(selected = filter == "done", onClick = {
                        filter = "done"; visibleCount = 8
                    }, label = { Text("Çözüldü") })
                }
                val list = courseQuestions.filter {
                    when (filter) {
                        "open" -> it.status != QuestionStatus.COZULDU
                        "done" -> it.status == QuestionStatus.COZULDU
                        else -> true
                    }
                }
                if (list.isEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text("Burada henüz soru yok.", color = c.onSurfaceVariant)
                }
                list.take(visibleCount).forEach { question ->
                    QuestionJournalCard(question, available[question.outcomeId],
                        onCapture, onGallery, onReviewQuestion, onMarkUnsolved,
                        onRemovePhoto, onDelete = { deleteQuestion = it })
                }
                if (list.size > visibleCount) {
                    TextButton(onClick = { visibleCount += 8 },
                        modifier = Modifier.fillMaxWidth()) {
                        Text("Diğer ${list.size - visibleCount} soruyu göster")
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Fotoğraflar telefonda çevrimdışı saklanır; JSON deneme yedeğine dahil değildir.",
                    color = c.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            }
        }
        else -> {
            DashboardPanel(Modifier.fillMaxWidth()) {
                Text("TEKRAR RADARI", color = c.secondary,
                    style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
                Spacer(Modifier.height(4.dp))
                Text("Neye tekrar bakmalıyım?", color = c.onSurface,
                    fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text("Son 3 denemedeki işaretlediğin konular, kazanım yanlışların ve soru defterin birlikte değerlendirilir.",
                    color = c.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
                val relevant = signals.filter { it.outcome.examType == examType &&
                    it.outcome.subject == subject }
                if (relevant.isEmpty()) {
                    Text("Şu an bu ders için işaretlenmiş tekrar ihtiyacı yok.",
                        color = c.onSurfaceVariant)
                }
                relevant.sortedWith(compareByDescending<com.deniz0706.ykstakip.util.ReviewSignal> {
                    it.outcome.id == focusedReviewId
                }.thenByDescending { it.urgent }.thenByDescending { it.due })
                    .take(30).forEach { alert ->
                    HorizontalDivider(color = c.outlineVariant)
                    Column(Modifier.fillMaxWidth().padding(vertical = 11.dp)) {
                        Row(Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(alert.outcome.topic, color = c.secondary,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.weight(1f))
                            Text(if (alert.urgent) "ÖNCELİKLİ" else "TEKRAR",
                                color = if (alert.urgent) c.error else c.primary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall)
                        }
                        Text(alert.outcome.title, color = c.onSurface,
                            fontWeight = FontWeight.SemiBold)
                        Text(alert.explanation, color = c.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(5.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            TextButton(onClick = {
                                onReviewed(alert.outcome.id)
                                focusedReviewId = null
                                actionFeedback = "Tekrar tamamlandı ✓ Sonraki kontrol tarihi otomatik planlandı."
                            }) {
                                Text("Tekrar ettim ✓", style = MaterialTheme.typography.labelMedium)
                            }
                            TextButton(onClick = {
                                onRequestReview(alert.outcome.id)
                                focusedReviewId = alert.outcome.id
                                actionFeedback = "Bu kazanım bugünün tekrar listesinde öne alındı."
                            }) {
                                Text("Bugün tekrar", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
                val dueQuestions = courseQuestions.filter {
                    it.status != QuestionStatus.COZULDU && it.dueDate.isNotBlank() &&
                        it.dueDate <= LocalDate.now().toString()
                }
                if (dueQuestions.isNotEmpty()) {
                    Spacer(Modifier.height(9.dp))
                    Text("GÜNÜ GELEN SORULAR", color = c.secondary,
                        style = MaterialTheme.typography.labelSmall)
                    dueQuestions.take(5).forEach { q ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(q.title.ifBlank { available[q.outcomeId]?.title.orEmpty() },
                                color = c.onSurface, modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = { onReviewQuestion(q.id, true) }) {
                                Text("Çözdüm")
                            }
                            TextButton(onClick = { onReviewQuestion(q.id, false) }) {
                                Text("Ertele")
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Bu öneriler işaretlediğin verilere dayanır; fotoğrafların içeriği otomatik okunmaz.",
                    color = c.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            }
        }
    }

    if (createOutcome) {
        var topicText by remember { mutableStateOf(if (topic == "Tümü") "" else topic) }
        var outcomeText by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { createOutcome = false },
            title = { Text("Kazanım ekle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("$examType · $subject", color = c.onSurfaceVariant)
                    OutlinedTextField(value = topicText, onValueChange = { topicText = it },
                        label = { Text("Konu") }, singleLine = true)
                    OutlinedTextField(value = outcomeText, onValueChange = { outcomeText = it },
                        label = { Text("Kazanım / yapabilmem gereken şey") },
                        minLines = 2)
                }
            },
            confirmButton = { TextButton(onClick = {
                if (subject.isNotBlank() && topicText.isNotBlank() && outcomeText.isNotBlank()) {
                    onCreateOutcome(examType, subject, topicText, outcomeText)
                    createOutcome = false
                }
            }) { Text("Ekle") } },
            dismissButton = { TextButton(onClick = { createOutcome = false }) { Text("Vazgeç") } })
    }

    if (createQuestion) {
        var selection by remember { mutableStateOf("") }
        var showOutcomes by remember { mutableStateOf(false) }
        var title by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        var status by remember { mutableStateOf(QuestionStatus.COZULEMEDI) }
        val options = filtered.ifEmpty { courseOutcomes }
        val selectedOutcome = options.find { it.id == selection } ?: options.firstOrNull()
        AlertDialog(onDismissRequest = { createQuestion = false },
            title = { Text("Soru defterine ekle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("Kazanımı seç; soruyu doğrudan kamera ya da galeriden fotoğrafıyla ekleyebilirsin.",
                        color = c.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Box {
                        OutlinedButton(onClick = { showOutcomes = true },
                            modifier = Modifier.fillMaxWidth()) {
                            Text(selectedOutcome?.let { it.topic + " · " + it.title }
                                ?: "Kazanım seç", maxLines = 2,
                                overflow = TextOverflow.Ellipsis)
                        }
                        DropdownMenu(expanded = showOutcomes, onDismissRequest = {
                            showOutcomes = false
                        }) {
                            options.forEach { o ->
                                DropdownMenuItem(
                                    text = { Text(o.topic + " · " + o.title, maxLines = 2) },
                                    onClick = { selection = o.id; showOutcomes = false })
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
                                actionFeedback = "Soru eklendi. Kamera açılıyor..."
                                onCapture(id)
                            }
                        }) { Text("📷 Çekerek ekle", style = MaterialTheme.typography.labelSmall) }
                        OutlinedButton(enabled = selectedOutcome != null, onClick = {
                            selectedOutcome?.let {
                                val id = onCreateQuestion(it.id, title, notes, status)
                                createQuestion = false
                                pane = "questions"
                                actionFeedback = "Soru eklendi. Galeriden fotoğraf seç."
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
