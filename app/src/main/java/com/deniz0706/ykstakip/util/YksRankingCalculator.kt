package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.model.AytField
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.model.ExamType
import java.text.Normalizer
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs

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

        if (sameDate != null) {
            return sameDate
        }

        val aytTitle = normalizeText(aytExam.title)
        val aytPublisher = normalizeText(aytExam.publisher)

        val sameTitle = tytExams
            .filter {
                normalizeText(it.title) == aytTitle &&
                    aytTitle.isNotEmpty()
            }
            .minByOrNull {
                dateDistance(it.date, aytExam.date)
            }

        if (sameTitle != null) {
            return sameTitle
        }

        if (aytPublisher.isNotEmpty()) {
            val samePublisher = tytExams
                .filter {
                    normalizeText(it.publisher) == aytPublisher
                }
                .minByOrNull {
                    dateDistance(it.date, aytExam.date)
                }

            if (samePublisher != null &&
                dateDistance(
                    samePublisher.date,
                    aytExam.date
                ) <= 30L
            ) {
                return samePublisher
            }
        }

        val nearest = tytExams.minByOrNull {
            dateDistance(it.date, aytExam.date)
        }

        return nearest?.takeIf {
            dateDistance(
                it.date,
                aytExam.date
            ) <= 30L
        }
    }

    private fun normalizeText(
        value: String
    ): String {

        return Normalizer
            .normalize(
                value.lowercase(Locale("tr", "TR")),
                Normalizer.Form.NFD
            )
            .replace(
                Regex("\\p{InCombiningDiacriticalMarks}+"),
                ""
            )
            .replace(
                Regex("[^a-z0-9]+"),
                ""
            )
    }

    private fun dateDistance(
        first: String,
        second: String
    ): Long {

        return try {
            val firstDate = LocalDate.parse(first)
            val secondDate = LocalDate.parse(second)

            abs(
                ChronoUnit.DAYS.between(
                    firstDate,
                    secondDate
                )
            )
        } catch (e: Exception) {
            Long.MAX_VALUE
        }
    }
}
