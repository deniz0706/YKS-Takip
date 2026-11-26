package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.model.StudyDay
import java.time.LocalDate

data class StudyWeekSummary(
    val currentQuestions: Int,
    val previousQuestions: Int,
    val currentMinutes: Int,
    val activeDays: Int,
    val goalDays: Int,
    val estimatedDays: Int,
    val averageQuestionsPerActiveDay: Int
)

/** Builds two fixed seven-day windows ending at the supplied local date. */
object StudyActivityAnalytics {
    fun summarize(days: List<StudyDay>, today: String, dailyGoal: Int?): StudyWeekSummary {
        val end = runCatching { LocalDate.parse(today) }.getOrNull()
            ?: return StudyWeekSummary(0, 0, 0, 0, 0, 0, 0)
        val currentStart = end.minusDays(6)
        val previousStart = end.minusDays(13)
        val previousEnd = currentStart.minusDays(1)
        val parsed = days.mapNotNull { day ->
            runCatching { LocalDate.parse(day.date) }.getOrNull()?.let { it to day }
        }.groupBy { it.first }.map { (date, entries) ->
            date to StudyDay(
                date = date.toString(),
                questionCount = entries.sumOf { it.second.questionCount.coerceAtLeast(0) },
                studyMinutes = entries.sumOf { it.second.studyMinutes.coerceAtLeast(0) },
                estimated = entries.any { it.second.estimated }
            )
        }
        val current = parsed.filter { (date, _) -> !date.isBefore(currentStart) && !date.isAfter(end) }
            .map { it.second }
        val previous = parsed.filter { (date, _) -> !date.isBefore(previousStart) && !date.isAfter(previousEnd) }
            .map { it.second }
        val currentQuestions = current.sumOf { it.questionCount.coerceAtLeast(0) }
        val active = current.filter { it.questionCount > 0 || it.studyMinutes > 0 }
        val goal = dailyGoal?.takeIf { it > 0 }
        return StudyWeekSummary(
            currentQuestions = currentQuestions,
            previousQuestions = previous.sumOf { it.questionCount.coerceAtLeast(0) },
            currentMinutes = current.sumOf { it.studyMinutes.coerceAtLeast(0) },
            activeDays = active.size.coerceAtMost(7),
            goalDays = if (goal == null) 0 else current.count { it.questionCount >= goal },
            estimatedDays = active.count { it.estimated },
            averageQuestionsPerActiveDay = if (active.isEmpty()) 0
                else (currentQuestions.toDouble() / active.size).toInt()
        )
    }
}
