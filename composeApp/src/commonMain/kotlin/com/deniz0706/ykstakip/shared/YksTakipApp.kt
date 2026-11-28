package com.deniz0706.ykstakip.shared

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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deniz0706.ykstakip.domain.Exam
import com.deniz0706.ykstakip.domain.ExamAnalytics
import com.deniz0706.ykstakip.domain.ExamType

private val GraphiteBackground = Color(0xFF0F1115)
private val GraphiteSurface = Color(0xFF181B20)
private val GraphiteAccent = Color(0xFFB8C7E3)

/** Data contract supplied by the platform repository; empty values are genuine empty states. */
data class DashboardUiState(
    val tytLatestNet: Double? = null,
    val tytPreviousNet: Double? = null,
    val tytGoal: Float? = null,
    val tytTrend: List<Double> = emptyList(),
    val aytLatestNet: Double? = null,
    val aytPreviousNet: Double? = null,
    val aytGoal: Float? = null,
    val aytTrend: List<Double> = emptyList(),
    val daysUntilExam: Long? = null,
    val todayQuestionCount: Int = 0,
    val dailyQuestionGoal: Int? = null
) {
    init {
        require(todayQuestionCount >= 0) { "Bugünkü soru sayısı negatif olamaz." }
        require(dailyQuestionGoal == null || dailyQuestionGoal >= 0) { "Günlük hedef negatif olamaz." }
        require(daysUntilExam == null || daysUntilExam >= 0) { "Sınava kalan gün negatif olamaz." }
        require(
            (tytGoal == null || (tytGoal.isFinite() && tytGoal >= 0f)) &&
                (aytGoal == null || (aytGoal.isFinite() && aytGoal >= 0f))
        ) {
            "Net hedefi geçersiz."
        }
        // Negative net values are legitimate when wrong answers exceed correct answers.
        require((tytTrend + aytTrend).all { it.isFinite() }) {
            "Net geçmişi sonlu olmayan değer içeriyor."
        }
    }

    companion object {
        /**
         * Builds the shared dashboard contract from persisted domain records.
         * Platform repositories only need to supply exams and platform-owned settings.
         */
        fun fromExams(
            exams: List<Exam>,
            tytGoal: Float? = null,
            aytGoal: Float? = null,
            daysUntilExam: Long? = null,
            todayQuestionCount: Int = 0,
            dailyQuestionGoal: Int? = null
        ): DashboardUiState {
            val tytExams = exams.filter { it.type == ExamType.TYT }
            val aytExams = exams.filter { it.type == ExamType.AYT }
            val tyt = ExamAnalytics.summarize(tytExams)
            val ayt = ExamAnalytics.summarize(aytExams)
            return DashboardUiState(
                tytLatestNet = tyt.latestNet,
                tytPreviousNet = tyt.previousNet,
                tytGoal = tytGoal,
                tytTrend = ExamAnalytics.trend(tytExams, limit = 10),
                aytLatestNet = ayt.latestNet,
                aytPreviousNet = ayt.previousNet,
                aytGoal = aytGoal,
                aytTrend = ExamAnalytics.trend(aytExams, limit = 10),
                daysUntilExam = daysUntilExam,
                todayQuestionCount = todayQuestionCount,
                dailyQuestionGoal = dailyQuestionGoal
            )
        }
    }
}

@Composable
fun YksTakipApp(
    dashboard: DashboardUiState = DashboardUiState(),
    onAddExam: (String) -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onOpenStudy: () -> Unit = {}
) {
    var selectedExam by remember { mutableStateOf("TYT") }
    var showTarget by remember { mutableStateOf(true) }
    val latest = if (selectedExam == "TYT") dashboard.tytLatestNet else dashboard.aytLatestNet
    val previous = if (selectedExam == "TYT") dashboard.tytPreviousNet else dashboard.aytPreviousNet
    val goal = if (selectedExam == "TYT") dashboard.tytGoal else dashboard.aytGoal
    val trend = if (selectedExam == "TYT") dashboard.tytTrend else dashboard.aytTrend
    val change = if (latest != null && previous != null) latest - previous else null
    val progress = if (latest != null && goal != null && goal > 0f) {
        "${((latest / goal) * 100.0).toInt().coerceIn(0, 999)}%"
    } else null

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = GraphiteAccent,
            background = GraphiteBackground,
            surface = GraphiteSurface,
            onBackground = Color(0xFFE8EAF0),
            onSurface = Color(0xFFE8EAF0),
            onSurfaceVariant = Color(0xFFA5AAB5),
            outline = Color(0xFF343943)
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = GraphiteBackground) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Bugün", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        Text(
                            dashboard.daysUntilExam?.let { "YKS'ye $it gün kaldı" } ?: "Çalışma durumun",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        "${dashboard.todayQuestionCount}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { selectedExam = "TYT" }) { Text(if (selectedExam == "TYT") "● TYT" else "TYT") }
                    TextButton(onClick = { selectedExam = "AYT" }) { Text(if (selectedExam == "AYT") "● AYT" else "AYT") }
                }
                SharedExamTrendCard(
                    examType = selectedExam,
                    latestNet = latest,
                    change = change,
                    goal = goal,
                    goalProgressLabel = progress,
                    trend = trend,
                    trendCount = trend.size,
                    showTarget = showTarget,
                    onShowTargetChange = { showTarget = it }
                )
                if (dashboard.dailyQuestionGoal != null) {
                    Text(
                        "Günlük hedef: ${dashboard.dailyQuestionGoal} soru",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(onClick = { onAddExam(selectedExam) }, modifier = Modifier.fillMaxWidth()) {
                    Text("$selectedExam denemesi ekle")
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onOpenHistory) { Text("Deneme geçmişi") }
                    TextButton(onClick = onOpenStudy) { Text("Çalışma takibi") }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Veriler cihazındaki kayıtlarından alınır. Henüz deneme eklemediysen burada boş durum gösterilir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
