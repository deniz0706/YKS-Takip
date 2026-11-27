package com.deniz0706.ykstakip.domain.usecase

import com.deniz0706.ykstakip.domain.model.AytField
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import kotlin.math.absoluteValue

/** Protected: ranking calculation orchestration.
 *
 * Uses the portable [YksRankingEstimator] from commonMain, which compiles
 * for Android, iOS, and JVM. No Android dependencies. Calculation logic is
 * identical to the previous Android-only estimator.
 */
class CalculateRankingUseCase {

    data class TytResult(val exam: Exam, val estimate: RankingEstimate)
    data class AytResult(val tytExam: Exam, val aytExam: Exam, val field: AytField, val estimate: RankingEstimate)

    fun calculateTyt(exams: List<Exam>, obp: Double): List<TytResult> =
        exams
            .filter { it.type == ExamType.TYT }
            .sortedBy { it.date }
            .mapNotNull { exam ->
                val estimate = YksRankingEstimator.estimateTyt(exam, obp, previouslyPlaced = false) ?: return@mapNotNull null
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
            val estimate = YksRankingEstimator.estimateAyt(tytExam, aytExam, obp, field, previouslyPlaced = false) ?: return@mapNotNull null
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
