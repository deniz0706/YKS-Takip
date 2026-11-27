package com.deniz0706.ykstakip.ui.study

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import com.deniz0706.ykstakip.domain.model.AytField
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.LearningOutcome
import com.deniz0706.ykstakip.domain.model.QuestionCard
import com.deniz0706.ykstakip.domain.model.QuestionStatus
import com.deniz0706.ykstakip.domain.model.StudyDay
import com.deniz0706.ykstakip.model.StudyPlanItem
import com.deniz0706.ykstakip.ui.components.YksSection
import com.deniz0706.ykstakip.ui.components.YksStatTile
import com.deniz0706.ykstakip.ui.theme.YksShape
import com.deniz0706.ykstakip.ui.theme.YksSpacing

@Composable
fun NewStudyScreen(
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
    onEditToday: () -> Unit,
    onFocusSaved: () -> Unit,
    onEditDay: (String) -> Unit,
    onPickDay: () -> Unit,
    onAddPlan: (Int) -> Unit,
    onAddSuggestedPlan: (Int, String, String) -> Unit,
    onEditPlan: (StudyPlanItem) -> Unit,
    onTogglePlan: (Long) -> Unit,
    onOpenLearningFromPlan: (StudyPlanItem) -> Unit,
    onDeletePlan: (StudyPlanItem) -> Unit
) {
    val c = MaterialTheme.colorScheme
    var section by remember { mutableStateOf("today") }

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
                    text = "Çalışma Günlüğü",
                    style = MaterialTheme.typography.displayMedium,
                    color = c.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Günlük plan ve ilerleme",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.onSurfaceVariant
                )
            }
        }

        item {
            SingleChoiceSegmentedButtonRow(
                selected = section,
                onClick = { section = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("today" to "Bugün", "plan" to "Plan", "learning" to "Öğrenme", "analysis" to "Analiz")
                    .forEachIndexed { index, (key, label) ->
                        SegmentedButton(
                            selected = section == key,
                            onClick = { section = key },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 4),
                            label = { Text(label, maxLines = 1) }
                        )
                    }
            }
        }

        if (section == "today") {
            TodaySection(
                days = days,
                goal = goal,
                onEditToday = onEditToday,
                onPickDay = onPickDay,
                onEditDay = onEditDay
            )
        } else if (section == "plan") {
            PlanSection(
                plan = plan,
                onAddPlan = onAddPlan,
                onEditPlan = onEditPlan,
                onTogglePlan = onTogglePlan,
                onDeletePlan = onDeletePlan,
                onOpenLearningFromPlan = onOpenLearningFromPlan
            )
        } else if (section == "learning") {
            LearningSection(
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
            AnalysisSection(
                days = days,
                goal = goal,
                exams = exams
            )
        }

        item { Spacer(Modifier.height(96.dp)) }
    }
}

@Composable
private fun TodaySection(
    days: List<StudyDay>,
    goal: Int?,
    onEditToday: () -> Unit,
    onPickDay: () -> Unit,
    onEditDay: (String) -> Unit
) {
    val c = MaterialTheme.colorScheme
    val today = java.time.LocalDate.now().toString()
    val current = days.firstOrNull { it.date == today }
    val questions = current?.questionCount ?: 0
    val minutes = current?.studyMinutes ?: 0

    YksSection(title = "Bugün", detail = java.time.LocalDate.now().toString())
    Spacer(Modifier.height(4.dp))

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(YksSpacing.md)) {
        YksStatTile(
            label = "Çözülen soru",
            value = questions.toString(),
            detail = "bugün",
            modifier = Modifier.weight(1f)
        )
        YksStatTile(
            label = "Çalışma süresi",
            value = "$minutes dk",
            detail = "toplam",
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(Modifier.height(14.dp))
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(YksSpacing.sm)
    ) {
        androidx.compose.foundation.layout.Column(
            Modifier.weight(1f).fillMaxHeight()
                .clip(RoundedCornerShape(YksShape.card))
                .background(c.surfaceContainer)
                .padding(YksSpacing.cardContent)
                .clickable(onClick = onEditToday)
        ) {
            Text("Düzenle", style = MaterialTheme.typography.labelMedium, color = c.primary)
        }
        androidx.compose.foundation.layout.Box(
            Modifier.weight(1f).fillMaxHeight()
                .clip(RoundedCornerShape(YksShape.card))
                .background(c.surfaceContainer)
                .clickable(onClick = onPickDay)
        ) {
            Text("Gün seç", style = MaterialTheme.typography.labelMedium, color = c.primary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun PlanSection(
    plan: List<StudyPlanItem>,
    onAddPlan: (Int) -> Unit,
    onEditPlan: (StudyPlanItem) -> Unit,
    onTogglePlan: (Long) -> Unit,
    onDeletePlan: (StudyPlanItem) -> Unit,
    onOpenLearningFromPlan: (StudyPlanItem) -> Unit
) {
    val c = MaterialTheme.colorScheme
    val weekdays = listOf(2 to "Pazartesi", 3 to "Salı", 4 to "Çarşamba", 5 to "Perşembe", 6 to "Cuma", 7 to "Cumartesi", 1 to "Pazar")

    YksSection(title = "Haftalık Plan", detail = "Derslerinizin tekrarlayan haftalık programı")
    Spacer(Modifier.height(4.dp))

    weekdays.forEachIndexed { index, (day, label) ->
        val dayItems = plan.filter { it.dayOfWeek == day }
        if (dayItems.isNotEmpty()) {
            androidx.compose.foundation.layout.Column(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(YksShape.card))
                    .background(c.surfaceContainer)
                    .padding(YksSpacing.cardContent)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = c.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                dayItems.forEach { item ->
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(YksSpacing.sm)
                    ) {
                        Text(
                            text = "${item.startTime} - ${item.endTime}",
                            style = MaterialTheme.typography.labelMedium,
                            color = c.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = item.subject,
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.onSurface
                        )
                        if (item.topic.isNotBlank()) {
                            Text(
                                text = "· ${item.topic}",
                                style = MaterialTheme.typography.bodySmall,
                                color = c.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
            Spacer(Modifier.height(YksSpacing.sm))
        }
    }
}

@Composable
private fun LearningSection(
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
    YksSection(title = "Öğrenme Kazanımları", detail = "Müfredat ilerlemeniz ve soru defteriniz")
    Spacer(Modifier.height(4.dp))
    Text(
        text = "Alan: ${field.label}",
        style = MaterialTheme.typography.bodyMedium,
        color = c.onSurfaceVariant
    )
    Spacer(Modifier.height(8.dp))

    outcomes.take(8).forEach { outcome ->
        Row(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(YksShape.control))
                .background(c.surfaceContainer)
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(YksSpacing.md)
        ) {
            Text(
                text = if (outcome.completed) "✓" else "○",
                color = if (outcome.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium
            )
            Column(Modifier.weight(1f)) {
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
        }
    }
}

@Composable
private fun AnalysisSection(
    days: List<StudyDay>,
    goal: Int?,
    exams: List<Exam>
) {
    val c = MaterialTheme.colorScheme
    YksSection(title = "Analiz", detail = "Çalışma alışkanlıklarınızın gözden geçirilməsi")
    Spacer(Modifier.height(4.dp))

    val totalDays = days.size
    val totalMinutes = days.sumOf { it.studyMinutes }
    val totalQuestions = days.sumOf { it.questionCount }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(YksSpacing.md)) {
        YksStatTile(
            label = "Kayıtlı gün",
            value = totalDays.toString(),
            modifier = Modifier.weight(1f)
        )
        YksStatTile(
            label = "Toplam süre",
            value = "$totalMinutes dk",
            modifier = Modifier.weight(1f)
        )
        YksStatTile(
            label = "Toplam soru",
            value = totalQuestions.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}
