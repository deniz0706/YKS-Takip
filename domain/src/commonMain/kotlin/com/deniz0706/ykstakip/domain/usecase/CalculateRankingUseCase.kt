package com.deniz0706.ykstakip.domain.usecase

import com.deniz0706.ykstakip.domain.model.AytField
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType

/** Protected: ranking calculation orchestration. Delegates to platform estimator implementations. */
class CalculateRankingUseCase(private val estimator: RankingEstimator) {

    data class TytResult(val exam: Exam, val estimate: RankingEstimate)
    data class AytResult(val tytExam: Exam, val aytExam: Exam, val field: AytField, val estimate: RankingEstimate)

    data class RankingEstimate(val center: Int, val lower: Int, val upper: Int)

    interface RankingEstimator {
        fun estimateTyt(exam: Exam, obp: Double, previouslyPlaced: Boolean): RankingEstimate?
        fun estimateAyt(tytExam: Exam, aytExam: Exam, obp: Double, field: AytField, previouslyPlaced: Boolean): RankingEstimate?
    }

    fun calculateTyt(exams: List<Exam>, obp: Double): List<TytResult> =
        exams
            .filter { it.type == ExamType.TYT }
            .sortedBy { it.date }
            .mapNotNull { exam ->
                val estimate = estimator.estimateTyt(exam, obp, previouslyPlaced = false) ?: return@mapNotNull null
                TytResult(exam, estimate)
            }

    fun calculateAyt(exams: List<Exam>, obp: Double, field: AytField): List<AytResult> {
        val tytExams = exams.filter { it.type == ExamType.TYT }.sortedBy { it.date }
        val aytExams = exams.filter { it.type == ExamType.AYT }.sortedBy { it.date }
        if (tytExams.isEmpty() || aytExams.isEmpty()) return emptyList()

        val fieldAytExams = aytExams.filter { it.aytField == null || it.aytField == field }
        val effectiveAyt = if (fieldAytExams.isNotEmpty()) fieldAytExams else aytExams

        return effectiveAyt.mapNotNull { aytExam ->
            val tytExam = findMatchingTyt(aytExam, tytExams) ?: return@mapNotNull null
            val estimate = estimator.estimateAyt(tytExam, aytExam, obp, field, previouslyPlaced = false) ?: return@mapNotNull null
            AytResult(tytExam, aytExam, field, estimate)
        }
    }

    private fun findMatchingTyt(aytExam: Exam, tytExams: List<Exam>): Exam? {
        if (tytExams.isEmpty()) return null
        return tytExams.firstOrNull { it.date == aytExam.date }
            ?: tytExams.minByOrNull { daysBetween(it.date, aytExam.date) }
    }

    private fun daysBetween(first: String, second: String): Long {
        return try {
            java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDate.parse(first), java.time.LocalDate.parse(second)
            ).absoluteValue
        } catch (_: Exception) {
            Long.MAX_VALUE
        }
    }
}
