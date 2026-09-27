package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.model.AytField
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.model.ExamType

data class RankingEstimate(
    val center: Int,
    val lower: Int,
    val upper: Int
)

object YksRankingEstimator {

    fun estimateTyt(
        exam: Exam,
        obp: Double
    ): RankingEstimate? {
        if (exam.type != ExamType.TYT) return null

        val turkce = exam.subject("Türkçe")?.net ?: 0.0
        val sosyal = exam.subject("Sosyal Bilimler")?.net ?: 0.0
        val matematik = exam.subject("Matematik")?.net ?: 0.0
        val fen = exam.subject("Fen Bilimleri")?.net ?: 0.0

        val tytWeightedNet =
            turkce * 0.33 +
            sosyal * 0.17 +
            matematik * 0.33 +
            fen * 0.17

        if (tytWeightedNet <= 0.0) return null

        return estimateFromScore(
            score = tytWeightedNet,
            obp = obp,
            type = "TYT"
        )
    }

    fun estimateAyt(
        tytExam: Exam,
        aytExam: Exam,
        obp: Double,
        field: AytField
    ): RankingEstimate? {
        if (tytExam.type != ExamType.TYT) return null
        if (aytExam.type != ExamType.AYT) return null

        val tytScore = calculateTytScore(tytExam)
        val aytScore = calculateAytScore(aytExam, field)

        if (tytScore <= 0.0 || aytScore <= 0.0) return null

        val combinedScore =
            tytScore * 0.40 +
            aytScore * 0.60 +
            obp * 0.10

        return estimateFromScore(
            score = combinedScore,
            obp = obp,
            type = field.name
        )
    }

    private fun calculateTytScore(exam: Exam): Double {
        val turkce = exam.subject("Türkçe")?.net ?: 0.0
        val sosyal = exam.subject("Sosyal Bilimler")?.net ?: 0.0
        val matematik = exam.subject("Matematik")?.net ?: 0.0
        val fen = exam.subject("Fen Bilimleri")?.net ?: 0.0

        return (
            turkce * 0.33 +
            sosyal * 0.17 +
            matematik * 0.33 +
            fen * 0.17
        )
    }

    private fun calculateAytScore(
        exam: Exam,
        field: AytField
    ): Double {
        return when (field) {
            AytField.SAYISAL -> {
                val matematik = exam.subject("Matematik")?.net ?: 0.0
                val fizik = exam.subject("Fizik")?.net ?: 0.0
                val kimya = exam.subject("Kimya")?.net ?: 0.0
                val biyoloji = exam.subject("Biyoloji")?.net ?: 0.0

                matematik * 0.50 +
                    fizik * 0.17 +
                    kimya * 0.17 +
                    biyoloji * 0.16
            }

            AytField.ESIT_AGIRLIK -> {
                val matematik = exam.subject("Matematik")?.net ?: 0.0
                val edebiyat = exam.subject("Edebiyat")?.net ?: 0.0
                val tarih = exam.subject("Tarih-1")?.net ?: 0.0
                val cografya = exam.subject("Coğrafya-1")?.net ?: 0.0

                matematik * 0.50 +
                    edebiyat * 0.30 +
                    tarih * 0.12 +
                    cografya * 0.08
            }

            AytField.SOZEL -> {
                val edebiyat = exam.subject("Edebiyat")?.net ?: 0.0
                val tarih1 = exam.subject("Tarih-1")?.net ?: 0.0
                val cografya1 = exam.subject("Coğrafya-1")?.net ?: 0.0
                val tarih2 = exam.subject("Tarih-2")?.net ?: 0.0
                val cografya2 = exam.subject("Coğrafya-2")?.net ?: 0.0
                val felsefe = exam.subject("Felsefe Grubu")?.net ?: 0.0
                val din = exam.subject("Din Kültürü / Ek Felsefe")?.net ?: 0.0

                edebiyat * 0.26 +
                    tarih1 * 0.10 +
                    cografya1 * 0.07 +
                    tarih2 * 0.12 +
                    cografya2 * 0.12 +
                    felsefe * 0.19 +
                    din * 0.14
            }
        }
    }

    private fun estimateFromScore(
        score: Double,
        obp: Double,
        type: String
    ): RankingEstimate {

        val normalized = when (type) {
            "TYT" -> score
            else -> score
        }

        val center = when {
            normalized >= 90 -> 1_000
            normalized >= 80 -> 5_000
            normalized >= 70 -> 15_000
            normalized >= 60 -> 30_000
            normalized >= 50 -> 60_000
            normalized >= 40 -> 100_000
            normalized >= 30 -> 200_000
            normalized >= 20 -> 400_000
            else -> 600_000
        }

        val spread = when {
            center <= 5_000 -> 0.40
            center <= 50_000 -> 0.30
            else -> 0.25
        }

        return RankingEstimate(
            center = center,
            lower = (center * (1.0 - spread)).toInt().coerceAtLeast(1),
            upper = (center * (1.0 + spread)).toInt()
        )
    }
}
