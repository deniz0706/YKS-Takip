package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.model.LearningOutcome
import com.deniz0706.ykstakip.model.QuestionCard
import com.deniz0706.ykstakip.model.QuestionStatus
import java.time.LocalDate
import java.util.Locale

data class ReviewSignal(
    val outcome: LearningOutcome,
    val examHits: Int,
    val examSample: Int,
    val openPhotos: Int,
    val unresolvedWrongCount: Int,
    val due: Boolean,
    val urgent: Boolean,
    val explanation: String
)

/**
 * Transparent, offline rule-based recommendations; no invented knowledge of
 * question images. Exam evidence comes from user-tagged weakTopics, not raw net.
 */
object ReviewAdvisor {
    private val tr = Locale("tr", "TR")
    private fun normalize(input: String): String = input.lowercase(tr)
        .replace(Regex("[^\\p{L}\\p{N}]+"), " ").trim().replace(Regex("\\s+"), " ")

    fun recommendations(
        outcomes: List<LearningOutcome>,
        questions: List<QuestionCard>,
        exams: List<Exam>,
        today: LocalDate = LocalDate.now()
    ): List<ReviewSignal> {
        val date = today.toString()
        // We need the LAST three exams for EACH type and subject, not the last
        // three exams globally (TYT and AYT interleave).
        val byCourse = mutableMapOf<Pair<String,String>, List<Exam>>()
        outcomes.forEach { outcome ->
            val key = outcome.examType to outcome.subject
            if (key !in byCourse) {
                byCourse[key] = exams.filter { it.type.name == outcome.examType &&
                    it.subjects.any { sub ->
                        LearningCourseMatcher.matches(outcome.examType, sub.subject, outcome.subject)
                    } }
                    .sortedWith(compareBy<Exam> { it.date }.thenBy { it.id })
                    .takeLast(3)
            }
        }
        return outcomes.mapNotNull { outcome ->
            val last = byCourse[outcome.examType to outcome.subject].orEmpty()
            val normalizedTopic = normalize(outcome.topic)
            val normalizedTitle = normalize(outcome.title)
            val hits = last.count { exam ->
                (outcome.lastReviewedDate.isBlank() || exam.date > outcome.lastReviewedDate) &&
                exam.subjects.filter { sub ->
                    LearningCourseMatcher.matches(outcome.examType, sub.subject, outcome.subject)
                }.flatMap { it.weakTopics }.any { raw ->
                    val tag = normalize(raw)
                    tag.isNotBlank() && (
                        tag == normalizedTopic || tag == normalizedTitle ||
                        (tag.length >= 5 && normalizedTitle.contains(tag)) ||
                        (normalizedTopic.length >= 5 && tag.contains(normalizedTopic))
                    )
                }
            }
            val open = questions.count {
                it.outcomeId == outcome.id && it.status != QuestionStatus.COZULDU
            }
            val freshWrong = (outcome.wrongCount - outcome.acknowledgedWrongCount).coerceAtLeast(0)
            val due = outcome.nextReviewDate.isNotBlank() && outcome.nextReviewDate <= date
            val reasons = mutableListOf<String>()
            if (hits >= 2) reasons += "Son ${last.size} denemenin $hits tanesinde bu konu işaretlendi"
            else if (hits > 0) reasons += "Son denemelerde $hits konu uyarısı"
            if (freshWrong > 0) reasons += "$freshWrong kez yanlış olarak işaretledin"
            if (open > 0) reasons += "$open çözülmemiş/geri dönülecek soru"
            if (due) reasons += "Tekrar zamanı geldi"
            if (outcome.reviewRequested) reasons.add(0, "Kendin tekrar için işaretledin")
            if (reasons.isEmpty()) null
            else ReviewSignal(
                outcome = outcome, examHits = hits, examSample = last.size,
                openPhotos = open, unresolvedWrongCount = freshWrong,
                due = due,
                urgent = hits >= 2 || freshWrong >= 2 || open >= 2,
                explanation = reasons.joinToString(" · ")
            )
        }.sortedWith(compareByDescending<ReviewSignal> { it.outcome.reviewRequested }
            .thenByDescending { it.urgent }
            .thenByDescending { it.due }
            .thenByDescending { it.examHits }
            .thenBy { it.outcome.subject }
            .thenBy { it.outcome.topic })
    }
}
