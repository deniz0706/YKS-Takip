package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.model.AytField
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.model.ExamType
import kotlin.math.roundToInt

data class RankingEstimate(
    val center: Int,
    val lower: Int,
    val upper: Int
)

object YksRankingEstimator {

    private data class Anchor(
        val score: Double,
        val rank: Int
    )

    private val tytAnchors = listOf(
        Anchor(550.0, 10),
        Anchor(530.0, 500),
        Anchor(510.0, 2500),
        Anchor(490.0, 8000),
        Anchor(470.0, 18000),
        Anchor(450.0, 35000),
        Anchor(430.0, 60000),
        Anchor(410.0, 95000),
        Anchor(390.0, 145000),
        Anchor(370.0, 210000),
        Anchor(350.0, 300000),
        Anchor(330.0, 420000),
        Anchor(310.0, 580000),
        Anchor(290.0, 800000),
        Anchor(270.0, 1100000),
        Anchor(250.0, 1450000),
        Anchor(230.0, 1750000),
        Anchor(210.0, 2000000),
        Anchor(190.0, 2180000)
    )

    private val sayAnchors = listOf(
        Anchor(560.0, 1),
        Anchor(550.0, 50),
        Anchor(540.0, 300),
        Anchor(530.0, 1200),
        Anchor(520.0, 3000),
        Anchor(510.0, 6500),
        Anchor(500.0, 11000),
        Anchor(490.0, 18000),
        Anchor(480.0, 27000),
        Anchor(470.0, 39000),
        Anchor(460.0, 53000),
        Anchor(450.0, 70000),
        Anchor(440.0, 90000),
        Anchor(430.0, 115000),
        Anchor(420.0, 145000),
        Anchor(410.0, 180000),
        Anchor(400.0, 220000),
        Anchor(390.0, 265000),
        Anchor(380.0, 315000),
        Anchor(370.0, 370000),
        Anchor(360.0, 430000),
        Anchor(350.0, 500000),
        Anchor(340.0, 575000),
        Anchor(330.0, 655000),
        Anchor(320.0, 740000),
        Anchor(310.0, 830000),
        Anchor(300.0, 925000),
        Anchor(290.0, 1020000),
        Anchor(280.0, 1110000)
    )

    private val eaAnchors = listOf(
        Anchor(560.0, 1),
        Anchor(550.0, 20),
        Anchor(540.0, 100),
        Anchor(530.0, 400),
        Anchor(520.0, 1000),
        Anchor(510.0, 2200),
        Anchor(500.0, 4500),
        Anchor(490.0, 8000),
        Anchor(480.0, 13000),
        Anchor(470.0, 20000),
        Anchor(460.0, 30000),
        Anchor(450.0, 43000),
        Anchor(440.0, 60000),
        Anchor(430.0, 80000),
        Anchor(420.0, 105000),
        Anchor(410.0, 135000),
        Anchor(400.0, 170000),
        Anchor(390.0, 210000),
        Anchor(380.0, 255000),
        Anchor(370.0, 305000),
        Anchor(360.0, 360000),
        Anchor(350.0, 425000),
        Anchor(340.0, 500000),
        Anchor(330.0, 580000),
        Anchor(320.0, 670000),
        Anchor(310.0, 770000)
    )

    private val sozAnchors = listOf(
        Anchor(560.0, 1),
        Anchor(550.0, 10),
        Anchor(540.0, 50),
        Anchor(530.0, 150),
        Anchor(520.0, 400),
        Anchor(510.0, 900),
        Anchor(500.0, 1800),
        Anchor(490.0, 3500),
        Anchor(480.0, 6500),
        Anchor(470.0, 11000),
        Anchor(460.0, 18000),
        Anchor(450.0, 28000),
        Anchor(440.0, 42000),
        Anchor(430.0, 60000),
        Anchor(420.0, 85000),
        Anchor(410.0, 115000),
        Anchor(400.0, 150000),
        Anchor(390.0, 190000),
        Anchor(380.0, 235000),
        Anchor(370.0, 285000),
        Anchor(360.0, 345000),
        Anchor(350.0, 410000),
        Anchor(340.0, 485000),
        Anchor(330.0, 570000),
        Anchor(320.0, 665000)
    )

    fun estimateTyt(
        exam: Exam,
        obp: Double
    ): RankingEstimate? {

        if (exam.type != ExamType.TYT) return null

        val turkce = exam.subject("Türkçe")?.net ?: 0.0
        val sosyal = exam.subject("Sosyal Bilimler")?.net ?: 0.0
        val matematik = exam.subject("Matematik")?.net ?: 0.0
        val fen = exam.subject("Fen Bilimleri")?.net ?: 0.0

        if (
            turkce == 0.0 &&
            sosyal == 0.0 &&
            matematik == 0.0 &&
            fen == 0.0
        ) {
            return null
        }

        val score =
            100.0 +
                turkce * 3.30 +
                sosyal * 3.40 +
                matematik * 3.30 +
                fen * 3.40 +
                obp.coerceIn(0.0, 100.0) * 0.60

        val rank = interpolateRank(
            score,
            tytAnchors
        )

        return makeEstimate(rank)
    }

    fun estimateAyt(
        tytExam: Exam,
        aytExam: Exam,
        obp: Double,
        field: AytField
    ): RankingEstimate? {

        if (tytExam.type != ExamType.TYT) return null
        if (aytExam.type != ExamType.AYT) return null

        val tytTurkce =
            tytExam.subject("Türkçe")?.net ?: 0.0

        val tytSosyal =
            tytExam.subject("Sosyal Bilimler")?.net ?: 0.0

        val tytMatematik =
            tytExam.subject("Matematik")?.net ?: 0.0

        val tytFen =
            tytExam.subject("Fen Bilimleri")?.net ?: 0.0

        val tytScore =
            100.0 +
                tytTurkce * 3.30 +
                tytSosyal * 3.40 +
                tytMatematik * 3.30 +
                tytFen * 3.40

        val aytScore = when (field) {

            AytField.SAYISAL -> {

                val mat =
                    aytExam.subject("Matematik")?.net ?: 0.0

                val fizik =
                    aytExam.subject("Fizik")?.net ?: 0.0

                val kimya =
                    aytExam.subject("Kimya")?.net ?: 0.0

                val biyoloji =
                    aytExam.subject("Biyoloji")?.net ?: 0.0

                100.0 +
                    mat * 3.00 +
                    fizik * 2.857 +
                    kimya * 3.077 +
                    biyoloji * 3.077
            }

            AytField.ESIT_AGIRLIK -> {

                val mat =
                    aytExam.subject("Matematik")?.net ?: 0.0

                val edebiyat =
                    aytExam.subject("Edebiyat")?.net ?: 0.0

                val tarih =
                    aytExam.subject("Tarih-1")?.net ?: 0.0

                val cografya =
                    aytExam.subject("Coğrafya-1")?.net ?: 0.0

                100.0 +
                    mat * 3.00 +
                    edebiyat * 3.00 +
                    tarih * 2.857 +
                    cografya * 3.333
            }

            AytField.SOZEL -> {

                val edebiyat =
                    aytExam.subject("Edebiyat")?.net ?: 0.0

                val tarih1 =
                    aytExam.subject("Tarih-1")?.net ?: 0.0

                val cografya1 =
                    aytExam.subject("Coğrafya-1")?.net ?: 0.0

                val tarih2 =
                    aytExam.subject("Tarih-2")?.net ?: 0.0

                val cografya2 =
                    aytExam.subject("Coğrafya-2")?.net ?: 0.0

                val felsefe =
                    aytExam.subject("Felsefe Grubu")?.net ?: 0.0

                val din =
                    aytExam.subject(
                        "Din Kültürü / Ek Felsefe"
                    )?.net ?: 0.0

                100.0 +
                    edebiyat * 3.00 +
                    tarih1 * 2.857 +
                    cografya1 * 3.333 +
                    tarih2 * 1.524 +
                    cografya2 * 1.882 +
                    felsefe * 3.00 +
                    din * 2.50
            }
        }

        val placementScore =
            tytScore * 0.40 +
                aytScore * 0.60 +
                obp.coerceIn(0.0, 100.0) * 0.60

        val anchors = when (field) {
            AytField.SAYISAL -> sayAnchors
            AytField.ESIT_AGIRLIK -> eaAnchors
            AytField.SOZEL -> sozAnchors
        }

        val rank = interpolateRank(
            placementScore,
            anchors
        )

        return makeEstimate(rank)
    }

    private fun interpolateRank(
        score: Double,
        anchors: List<Anchor>
    ): Int {

        val sorted =
            anchors.sortedByDescending { it.score }

        val clamped =
            score.coerceIn(
                sorted.last().score,
                sorted.first().score
            )

        if (clamped >= sorted.first().score) {
            return sorted.first().rank
        }

        if (clamped <= sorted.last().score) {
            return sorted.last().rank
        }

        for (i in 0 until sorted.lastIndex) {

            val high = sorted[i]
            val low = sorted[i + 1]

            if (
                clamped <= high.score &&
                clamped >= low.score
            ) {

                val ratio =
                    (clamped - low.score) /
                        (high.score - low.score)

                return (
                    low.rank +
                        (high.rank - low.rank) * ratio
                    )
                    .roundToInt()
                    .coerceAtLeast(1)
            }
        }

        return sorted.last().rank
    }

    private fun makeEstimate(
        rank: Int
    ): RankingEstimate {

        val safeRank =
            rank.coerceAtLeast(1)

        return RankingEstimate(
            center = safeRank,
            lower = maxOf(
                1,
                (safeRank * 0.85).roundToInt()
            ),
            upper = maxOf(
                safeRank,
                (safeRank * 1.15).roundToInt()
            )
        )
    }
}
