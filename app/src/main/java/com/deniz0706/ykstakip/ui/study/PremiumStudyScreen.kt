package com.deniz0706.ykstakip.ui.study

import com.deniz0706.ykstakip.ui.theme.clayCanvas
import com.deniz0706.ykstakip.ui.theme.clayRaised
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deniz0706.ykstakip.domain.model.AytField
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.LearningOutcome
import com.deniz0706.ykstakip.domain.model.QuestionCard
import com.deniz0706.ykstakip.domain.model.QuestionStatus
import com.deniz0706.ykstakip.domain.model.StudyDay
import com.deniz0706.ykstakip.domain.model.StudyPlanItem
import com.deniz0706.ykstakip.ui.theme.YksTokens
import com.deniz0706.ykstakip.util.Fmt

/** Premium study planning and tracking screen for YKS Takip 4.0. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumStudyScreen(
    days: List<StudyDay>,
    plan: List<StudyPlanItem>,
    goal: Int?,
    exams: List<Exam>,
    learningOutcomes: List<LearningOutcome>,
    learningQuestions: List<QuestionCard>,
    learningField: AytField,
    onLearningFieldChange: (AytField) -> Unit,
    onLearningComplete: (String, Boolean) -> Unit,
    onLearningSubtopicComplete: (String, String, Boolean) -> Unit,
    onLearningWrong: (String) -> Unit,
    onLearningRequestReview: (String) -> Unit,
    onLearningReviewed: (String) -> Unit,
    onLearningPostpone: (String) -> Unit,
    onLearningCreate: (String, String, String, String) -> Unit,
    onLearningDelete: (String) -> Unit,
    onQuestionCreate: (String, String, String, QuestionStatus) -> String,
    onQuestionDelete: (String) -> Unit,
    onQuestionReview: (String, Boolean) -> Unit,
    onQuestionUnsolved: (String) -> Unit,
    onQuestionCapture: (String) -> Unit,
    onQuestionGallery: (String) -> Unit,
    onQuestionPhotoRemove: (String, String) -> Unit,
    onRequestReviewNotifications: () -> Unit,
    onSaveDay: (String, Int, Int) -> Unit,
    onAddPlan: (Int) -> Unit,
    onAddSuggestedPlan: (Int, String, String) -> Unit,
    onEditPlan: (StudyPlanItem) -> Unit,
    onTogglePlan: (Long) -> Unit,
    onOpenLearningFromPlan: (StudyPlanItem) -> Unit,
    onDeletePlan: (StudyPlanItem) -> Unit,
    initialSection: String = "today"
) {
    val c = MaterialTheme.colorScheme
    var section by remember(initialSection) { mutableStateOf(initialSection) }
    var selectedDate by remember { mutableStateOf(java.time.LocalDate.now().toString()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDayEditor by remember { mutableStateOf(false) }
    val selectedDay = days.firstOrNull { it.date == selectedDate }

    LazyColumn(
        modifier = Modifier.clayCanvas(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = YksTokens.Space.pageHorizontal,
            vertical = YksTokens.Space.pageVertical
        ),
        verticalArrangement = Arrangement.spacedBy(YksTokens.Space.section)
    ) {
        // Header
        item {
            PremiumSectionHeader(
                title = "Çalışma Günlüğü",
                subtitle = "Günlük plan ve ilerleme",
                colorScheme = c
            )
        }

        // Tab navigation
        item {
            PremiumStudyTabs(
                sections = listOf(
                    "today" to "Bugün",
                    "plan" to "Haftalık Plan",
                    "learning" to "Öğrenme",
                    "analysis" to "Analiz"
                ),
                selected = section,
                onSelect = { section = it }
            )
        }

        item { Spacer(modifier = Modifier.height(YksTokens.Space.md)) }

        // Content
        item {
            if (section == "today") {
                PremiumTodaySection(
                    days = days,
                    goal = goal,
                    date = selectedDate,
                    onEditToday = { showDayEditor = true },
                    onPickDay = { showDatePicker = true }
                )
            } else if (section == "plan") {
                PremiumPlanSection(
                    plan = plan,
                    onAddPlan = onAddPlan,
                    onEditPlan = onEditPlan,
                    onTogglePlan = onTogglePlan,
                    onDeletePlan = onDeletePlan,
                    onOpenLearningFromPlan = onOpenLearningFromPlan
                )
            } else if (section == "learning") {
                PremiumLearningSection(
                    outcomes = learningOutcomes,
                    questions = learningQuestions,
                    field = learningField,
                    onFieldChange = onLearningFieldChange,
                    onComplete = onLearningComplete,
                    onSubtopicComplete = onLearningSubtopicComplete,
                    onWrong = onLearningWrong,
                    onRequestReview = onLearningRequestReview,
                    onReviewed = onLearningReviewed,
                    onPostpone = onLearningPostpone,
                    onCreate = onLearningCreate,
                    onDelete = onLearningDelete,
                    onQuestionCreate = onQuestionCreate,
                    onQuestionDelete = onQuestionDelete,
                    onQuestionReview = onQuestionReview,
                    onQuestionUnsolved = onQuestionUnsolved,
                    onQuestionCapture = onQuestionCapture,
                    onQuestionGallery = onQuestionGallery,
                    onQuestionPhotoRemove = onQuestionPhotoRemove,
                    onRequestReviewNotifications = onRequestReviewNotifications
                )
            } else {
                PremiumAnalysisSection(
                    days = days,
                    goal = goal,
                    exams = exams
                )
            }
        }

        item { Spacer(modifier = Modifier.height(120.dp)) }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = runCatching {
                java.time.LocalDate.parse(selectedDate).atStartOfDay(java.time.ZoneOffset.UTC)
                    .toInstant().toEpochMilli()
            }.getOrNull()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        selectedDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneOffset.UTC).toLocalDate().toString()
                    }
                    showDatePicker = false
                }) { Text("Günü seç") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Vazgeç") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showDayEditor) {
        var questionDraft by remember(selectedDate, selectedDay) {
            mutableStateOf(selectedDay?.questionCount?.toString() ?: "0")
        }
        var minuteDraft by remember(selectedDate, selectedDay) {
            mutableStateOf(selectedDay?.studyMinutes?.toString() ?: "0")
        }
        val validDayValues = (questionDraft.toIntOrNull() ?: -1) >= 0 &&
            (minuteDraft.toIntOrNull() ?: -1) >= 0
        AlertDialog(
            onDismissRequest = { showDayEditor = false },
            title = { Text(selectedDate + " kaydını düzenle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = questionDraft,
                        onValueChange = { questionDraft = it.filter(Char::isDigit).take(5) },
                        label = { Text("Çözülen soru") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minuteDraft,
                        onValueChange = { minuteDraft = it.filter(Char::isDigit).take(4) },
                        label = { Text("Çalışma süresi (dakika)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = validDayValues,
                    onClick = {
                        onSaveDay(selectedDate, questionDraft.toInt(), minuteDraft.toInt())
                        showDayEditor = false
                    }
                ) { Text("Kaydet") }
            },
            dismissButton = {
                TextButton(onClick = { showDayEditor = false }) { Text("Vazgeç") }
            }
        )
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
private fun PremiumStudyTabs(
    sections: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit
) {
    val c = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        sections.forEachIndexed { index, (key, label) ->
            val isSelected = selected == key
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(YksTokens.Shape.input))
                    .background(if (isSelected) c.primaryContainer else c.surfaceContainer)
                    .clickable { onSelect(key) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = if (isSelected) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) c.onPrimaryContainer else c.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PremiumTodaySection(
    days: List<StudyDay>,
    goal: Int?,
    date: String,
    onEditToday: () -> Unit,
    onPickDay: () -> Unit
) {
    val c = MaterialTheme.colorScheme
    val current = days.firstOrNull { it.date == date }
    val questions = current?.questionCount ?: 0
    val minutes = current?.studyMinutes ?: 0

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(YksTokens.Space.md)
    ) {
        Text(
            text = if (date == java.time.LocalDate.now().toString()) "Bugün" else date,
            style = MaterialTheme.typography.titleMedium,
            color = c.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(YksTokens.Space.md)
        ) {
            PremiumStatTile(
                label = "Çözülen Soru",
                value = questions.toString(),
                detail = "bugün",
                accent = c.primary,
                modifier = Modifier.weight(1f)
            )
            PremiumStatTile(
                label = "Çalışma Süresi",
                value = "$minutes dk",
                detail = "toplam",
                accent = c.secondary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(YksTokens.Space.sm)
        ) {
            PremiumActionTile(
                label = "Düzenle",
                icon = "",
                accent = c.primary,
                onClick = onEditToday,
                modifier = Modifier.weight(1f)
            )
            PremiumActionTile(
                label = "Gün Seç",
                icon = "Takvim",
                accent = c.secondary,
                onClick = onPickDay,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PremiumPlanSection(
    plan: List<StudyPlanItem>,
    onAddPlan: (Int) -> Unit,
    onEditPlan: (StudyPlanItem) -> Unit,
    onTogglePlan: (Long) -> Unit,
    onDeletePlan: (StudyPlanItem) -> Unit,
    onOpenLearningFromPlan: (StudyPlanItem) -> Unit
) {
    val c = MaterialTheme.colorScheme
    val weekdays = listOf(2 to "Pazartesi", 3 to "Salı", 4 to "Çarşamba", 5 to "Perşembe", 6 to "Cuma", 7 to "Cumartesi", 1 to "Pazar")

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(YksTokens.Space.md)
    ) {
        Text(
            text = "Haftalık Plan",
            style = MaterialTheme.typography.titleMedium,
            color = c.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Derslerinizin tekrarlayan haftalık programı",
            style = MaterialTheme.typography.bodySmall,
            color = c.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        weekdays.forEachIndexed { index, (day, label) ->
            val dayItems = plan.filter { it.dayOfWeek == day }
            if (dayItems.isNotEmpty()) {
                PremiumDayCard(
                    label = label,
                    items = dayItems,
                    colorScheme = c
                )
                Spacer(modifier = Modifier.height(YksTokens.Space.sm))
            }
        }
    }
}

@Composable
private fun PremiumDayCard(
    label: String,
    items: List<StudyPlanItem>,
    colorScheme: ColorScheme
) {
    val c = colorScheme
    Card(
        modifier = Modifier.fillMaxWidth().clayRaised(shape = RoundedCornerShape(YksTokens.Shape.panel), depth = 8.dp),
        shape = RoundedCornerShape(YksTokens.Shape.panel),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 6.dp),

        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = c.surface)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(YksTokens.Space.cardContent)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = c.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            items.forEach { item ->
                PremiumPlanItemRow(
                    item = item,
                    colorScheme = c
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PremiumPlanItemRow(
    item: StudyPlanItem,
    colorScheme: ColorScheme
) {
    val c = colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(YksTokens.Space.sm)
    ) {
        Text(
            text = "${item.startTime} - ${item.endTime}",
            style = MaterialTheme.typography.labelMedium,
            color = c.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = item.subject,
            style = MaterialTheme.typography.bodyMedium,
            color = c.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (item.topic.isNotBlank()) {
            Text(
                text = "· ${item.topic}",
                style = MaterialTheme.typography.bodySmall,
                color = c.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PremiumLearningSection(
    outcomes: List<LearningOutcome>,
    questions: List<QuestionCard>,
    field: AytField,
    onFieldChange: (AytField) -> Unit,
    onComplete: (String, Boolean) -> Unit,
    onSubtopicComplete: (String, String, Boolean) -> Unit,
    onWrong: (String) -> Unit,
    onRequestReview: (String) -> Unit,
    onReviewed: (String) -> Unit,
    onPostpone: (String) -> Unit,
    onCreate: (String, String, String, String) -> Unit,
    onDelete: (String) -> Unit,
    onQuestionCreate: (String, String, String, QuestionStatus) -> String,
    onQuestionDelete: (String) -> Unit,
    onQuestionReview: (String, Boolean) -> Unit,
    onQuestionUnsolved: (String) -> Unit,
    onQuestionCapture: (String) -> Unit,
    onQuestionGallery: (String) -> Unit,
    onQuestionPhotoRemove: (String, String) -> Unit,
    onRequestReviewNotifications: () -> Unit
) {
    val c = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(YksTokens.Space.md)
    ) {
        Text(
            text = "Öğrenme Kazanımları",
            style = MaterialTheme.typography.titleMedium,
            color = c.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Alan: ${field.label}",
            style = MaterialTheme.typography.bodyMedium,
            color = c.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        outcomes.take(12).forEachIndexed { index, outcome ->
            PremiumLearningRow(
                outcome = outcome,
                colorScheme = c,
                onComplete = { onComplete(outcome.id, !outcome.completed) }
            )
            if (index < outcomes.take(12).lastIndex) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun PremiumLearningRow(
    outcome: LearningOutcome,
    colorScheme: ColorScheme,
    onComplete: () -> Unit
) {
    val c = colorScheme
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(YksTokens.Shape.control))
            .background(c.surfaceContainer)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(YksTokens.Space.md)
    ) {
        Text(
            text = if (outcome.completed) "✓" else "○",
            color = if (outcome.completed) c.primary else c.onSurfaceVariant,
            style = MaterialTheme.typography.titleMedium
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${outcome.subject} · ${outcome.topic}",
                style = MaterialTheme.typography.bodyMedium,
                color = c.onSurface,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = outcome.title,
                style = MaterialTheme.typography.bodySmall,
                color = c.onSurfaceVariant
            )
        }
        androidx.compose.material3.IconButton(
            onClick = onComplete,
        ) {
            Text(
                text = if (outcome.completed) "✓" else "□",
                color = if (outcome.completed) c.primary else c.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun PremiumAnalysisSection(
    days: List<StudyDay>,
    goal: Int?,
    exams: List<Exam>
) {
    val c = MaterialTheme.colorScheme
    val totalDays = days.size
    val totalMinutes = days.sumOf { it.studyMinutes }
    val totalQuestions = days.sumOf { it.questionCount }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(YksTokens.Space.md)
    ) {
        Text(
            text = "Analiz",
            style = MaterialTheme.typography.titleMedium,
            color = c.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Çalışma alışkanlıklarınızın gözden geçirilmesi",
            style = MaterialTheme.typography.bodySmall,
            color = c.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(YksTokens.Space.md)
        ) {
            PremiumStatTile(
                label = "Kayıtlı Gün",
                value = totalDays.toString(),
                accent = c.primary,
                modifier = Modifier.weight(1f)
            )
            PremiumStatTile(
                label = "Toplam Süre",
                value = "$totalMinutes dk",
                accent = c.secondary,
                modifier = Modifier.weight(1f)
            )
            PremiumStatTile(
                label = "Toplam Soru",
                value = totalQuestions.toString(),
                accent = c.tertiary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PremiumStatTile(
    label: String,
    value: String,
    detail: String = "",
    accent: Color,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme
    Column(
        modifier = modifier
             .clayRaised(shape = RoundedCornerShape(YksTokens.Shape.panel), depth = 8.dp)
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
        if (detail.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = detail, style = MaterialTheme.typography.labelSmall, color = c.onSurfaceVariant)
        }
    }
}

@Composable
private fun PremiumActionTile(
    label: String,
    icon: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme
    androidx.compose.foundation.layout.Box(
        modifier = modifier
             .clayRaised(shape = RoundedCornerShape(YksTokens.Shape.panel), depth = 8.dp)
            .clip(RoundedCornerShape(YksTokens.Shape.panel))
            .background(c.surfaceContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = icon, style = MaterialTheme.typography.titleLarge, color = accent)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = c.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}