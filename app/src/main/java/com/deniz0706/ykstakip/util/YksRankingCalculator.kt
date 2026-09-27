package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.model.AytField
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.model.ExamType

data class TytRankingResult(
    val exam: Exam,
    val estimate: RankingEstimate
)

data class AytRankingResult(
    val tytExam: Exam,
    val aytExam: Exam,
    val field: AytField,
    val estimate: RankingEstimate
)

object YksRankingCalculator {

    fun calculateTyt(
        exams: List<Exam>,
        obp: Double
    ): List<TytRankingResult> {
        return exams
            .filter { it.type == ExamType.TYT }
            .sortedBy { it.date }
            .mapNotNull { exam ->
                val estimate = YksRankingEstimator.estimateTyt(
                    exam = exam,
                    obp = obp
                ) ?: return@mapNotNull null

                TytRankingResult(
                    exam = exam,
                    estimate = estimate
                )
            }
    }

    fun calculateAyt(
        exams: List<Exam>,
        obp: Double,
        field: AytField
    ): List<AytRankingResult> {
        val tytExams = exams
            .filter { it.type == ExamType.TYT }
            .sortedBy { it.date }

        val aytExams = exams
            .filter {
                it.type == ExamType.AYT &&
                    (it.aytField == null || it.aytField == field)
            }
            .sortedBy { it.date }

        return aytExams.mapNotNull { aytExam ->
            val tytExam = findMatchingTytExam(
                aytExam = aytExam,
                tytExams = tytExams
            ) ?: return@mapNotNull null

            val estimate = YksRankingEstimator.estimateAyt(
                tytExam = tytExam,
                aytExam = aytExam,
                obp = obp,
                field = field
            ) ?: return@mapNotNull null

            AytRankingResult(
                tytExam = tytExam,
                aytExam = aytExam,
                field = field,
                estimate = estimate
            )
        }
    }

    private fun findMatchingTytExam(
        aytExam: Exam,
        tytExams: List<Exam>
    ): Exam? {
        if (tytExams.isEmpty()) return null

        val sameDate = tytExams.firstOrNull {
            it.date == aytExam.date
        }

        if (sameDate != null) return sameDate

        return tytExams.minByOrNull {
            kotlin.math.abs(daysBetween(it.date, aytExam.date))
        }?.takeIf {
            daysBetween(it.date, aytExam.date) <= 7
        }
    }

    private fun daysBetween(
        first: String,
        second: String
    ): Long {
        return try {
            val firstDate = java.time.LocalDate.parse(first)
            val secondDate = java.time.LocalDate.parse(second)

            kotlin.math.abs(
                java.time.temporal.ChronoUnit.DAYS.between(
                    firstDate,
                    secondDate
                )
            )
        } catch (e: Exception) {
            Long.MAX_VALUE
        }
    }
}
