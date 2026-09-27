package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.model.AytField
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.model.ExamType
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class RankingEstimate(
    val center: Int,
    val lower: Int,
    val upper: Int
)

object YksRankingEstimator {

    private data class Stats(
        val mean: Double,
        val sd: Double
    )

    private val tyt2025 = mapOf(
        "Türkçe" to Stats(21.243, 7.848),
        "Sosyal Bilimler" to Stats(9.496, 4.042),
        "Matematik" to Stats(6.006, 7.067),
        "Fen Bilimleri" to Stats(4.122, 4.862)
    )

    private val ayt2025 = mapOf(
        "Edebiyat" to Stats(6.630, 5.452),
        "Tarih-1" to Stats(2.243, 2.423),
        "Coğrafya-1" to Stats(1.436, 1.515),
        "Tarih-2" to Stats(1.426, 1.965),
        "Coğrafya-2" to Stats(2.635, 2.808),
        "Felsefe Grubu" to Stats(1.918, 2.372),
        "Din Kültürü / Ek Felsefe" to Stats(1.473, 1.796),
        "Matematik" to Stats(6.798, 8.120),
        "Fizik" to Stats(2.442, 3.313),
        "Kimya" to Stats(1.758, 3.229),
        "Biyoloji" to Stats(2.596, 3.227)
    )

    private val tyt2026 = mapOf(
        "Türkçe" to Stats(20.1, 8.3),
        "Sosyal Bilimler" to Stats(10.0, 4.0),
        "Matematik" to Stats(6.4, 7.5),
        "Fen Bilimleri" to Stats(3.9, 5.0)
    )

    private val ayt2026 = mapOf(
        "Edebiyat" to Stats(6.5, 5.4),
        "Tarih-1" to Stats(2.9, 2.9),
        "Coğrafya-1" to Stats(1.9, 1.8),
        "Tarih-2" to Stats(1.9, 2.5),
        "Coğrafya-2" to Stats(2.4, 2.7),
        "Felsefe Grubu" to Stats(1.8, 2.2),
        "Din Kültürü / Ek Felsefe" to Stats(2.0, 2.2),
        "Matematik" to Stats(7.1, 8.9),
        "Fizik" to Stats(2.6, 3.6),
        "Kimya" to Stats(2.1, 3.6),
        "Biyoloji" to Stats(3.0, 3.6)
    )

    private const val TOTAL_TYT_CANDIDATES_2025 = 2_310_579
    private const val TOTAL_TYT_CANDIDATES_2026 = 2_187_743

    private val TOTAL_PLACEMENT_CANDIDATES_2025 = mapOf(
        "TYT" to 2_310_579,
        "SAY" to 1_291_531,
        "EA" to 1_494_612,
        "SÖZ" to 1_174_047
    )

    private val TOTAL_PLACEMENT_CANDIDATES_2026 = mapOf(
        "TYT" to 2_187_743,
        "SAY" to 1_135_718,
        "EA" to 1_421_290,
        "SÖZ" to 1_085_698
    )

    fun estimateTyt(
        exam: Exam,
        obp: Double
    ): RankingEstimate? {

        if (exam.type != ExamType.TYT) return null

        val nets = mapOf(
            "Türkçe" to (exam.subject("Türkçe")?.net ?: 0.0),
            "Sosyal Bilimler" to (exam.subject("Sosyal Bilimler")?.net ?: 0.0),
            "Matematik" to (exam.subject("Matematik")?.net ?: 0.0),
            "Fen Bilimleri" to (exam.subject("Fen Bilimleri")?.net ?: 0.0)
        )

        if (nets.values.none { it != 0.0 }) {
            return null
        }

        val weights = tytWeights()

        val z2025 = normalizedZ(
            nets,
            tyt2025,
            weights
        )

        val z2026 = normalizedZ(
            nets,
            tyt2026,
            weights
        )

        val rank2025 = rankFromZ(
            z2025,
            TOTAL_TYT_CANDIDATES_2025
        )

        val rank2026 = rankFromZ(
            z2026,
            TOTAL_TYT_CANDIDATES_2026
        )

        return combine(
            rank2025,
            rank2026
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

        val tytNets = mapOf(
            "Türkçe" to (tytExam.subject("Türkçe")?.net ?: 0.0),
            "Sosyal Bilimler" to (tytExam.subject("Sosyal Bilimler")?.net ?: 0.0),
            "Matematik" to (tytExam.subject("Matematik")?.net ?: 0.0),
            "Fen Bilimleri" to (tytExam.subject("Fen Bilimleri")?.net ?: 0.0)
        )

        val aytNets = getAytNets(
            aytExam,
            field
        )

        if (tytNets.values.none { it != 0.0 }) {
            return null
        }

        if (aytNets.values.none { it != 0.0 }) {
            return null
        }

        val tytZ2025 = normalizedZ(
            tytNets,
            tyt2025,
            tytWeights()
        )

        val tytZ2026 = normalizedZ(
            tytNets,
            tyt2026,
            tytWeights()
        )

        val aytWeights = aytWeightsFor(field)

        val aytZ2025 = normalizedZ(
            aytNets,
            ayt2025,
            aytWeights
        )

        val aytZ2026 = normalizedZ(
            aytNets,
            ayt2026,
            aytWeights
        )

        val obpZ = ((obp.coerceIn(0.0, 100.0) - 50.0) / 15.0)

        val combinedZ2025 = combinePlacementZ(
            tytZ = tytZ2025,
            aytZ = aytZ2025,
            obpZ = obpZ
        )

        val combinedZ2026 = combinePlacementZ(
            tytZ = tytZ2026,
            aytZ = aytZ2026,
            obpZ = obpZ
        )

        val key = fieldKey(field)

        val totalCandidates2025 =
            TOTAL_PLACEMENT_CANDIDATES_2025.getValue(key)

        val totalCandidates2026 =
            TOTAL_PLACEMENT_CANDIDATES_2026.getValue(key)

        val rank2025 = rankFromZ(
            combinedZ2025,
            totalCandidates2025
        )

        val rank2026 = rankFromZ(
            combinedZ2026,
            totalCandidates2026
        )

        return combine(
            rank2025,
            rank2026
        )
    }

    private fun tytWeights(): List<Pair<String, Double>> =
        listOf(
            "Türkçe" to 0.33,
            "Sosyal Bilimler" to 0.17,
            "Matematik" to 0.33,
            "Fen Bilimleri" to 0.17
        )

    private fun aytWeightsFor(
        field: AytField
    ): List<Pair<String, Double>> {

        return when (field) {

            AytField.SAYISAL ->
                listOf(
                    "Matematik" to 0.30,
                    "Fizik" to 0.10,
                    "Kimya" to 0.10,
                    "Biyoloji" to 0.10
                )

            AytField.ESIT_AGIRLIK ->
                listOf(
                    "Matematik" to 0.30,
                    "Edebiyat" to 0.18,
                    "Tarih-1" to 0.07,
                    "Coğrafya-1" to 0.05
                )

            AytField.SOZEL ->
                listOf(
                    "Edebiyat" to 0.18,
                    "Tarih-1" to 0.07,
                    "Coğrafya-1" to 0.05,
                    "Tarih-2" to 0.08,
                    "Coğrafya-2" to 0.08,
                    "Felsefe Grubu" to 0.09,
                    "Din Kültürü / Ek Felsefe" to 0.05
                )
        }
    }

    private fun normalizedZ(
        nets: Map<String, Double>,
        stats: Map<String, Stats>,
        weights: List<Pair<String, Double>>
    ): Double {

        var weightedSum = 0.0
        var totalWeight = 0.0

        for ((key, weight) in weights) {

            val net = nets[key] ?: continue
            val stat = stats[key] ?: continue

            val z = (net - stat.mean) / stat.sd

            weightedSum += z * weight
            totalWeight += weight
        }

        if (totalWeight <= 0.0) {
            return 0.0
        }

        return weightedSum / totalWeight
    }

    private fun combinePlacementZ(
        tytZ: Double,
        aytZ: Double,
        obpZ: Double
    ): Double {

        val tytWeight = 0.40
        val aytWeight = 0.60
        val obpWeight = 0.08

        val totalWeight =
            tytWeight +
            aytWeight +
            obpWeight

        return (
            tytZ * tytWeight +
            aytZ * aytWeight +
            obpZ * obpWeight
        ) / totalWeight
    }

    private fun rankFromZ(
        z: Double,
        totalCandidates: Int
    ): Int {

        val percentileAbove =
            1.0 - normalCdf(z)

        val rank =
            totalCandidates * percentileAbove

        return rank
            .roundToInt()
            .coerceIn(1, totalCandidates)
    }

    private fun normalCdf(
        z: Double
    ): Double {

        return 0.5 * (
            1.0 +
                erf(
                    z / sqrt(2.0)
                )
        )
    }

    private fun erf(
        x: Double
    ): Double {

        val t =
            1.0 /
                (
                    1.0 +
                        0.3275911 *
                        kotlin.math.abs(x)
                )

        val y =
            1.0 -
                (
                    (
                        (
                            (
                                1.061405429 * t -
                                    1.453152027
                            ) * t +
                                1.421413741
                        ) * t -
                            0.284496736
                    ) * t +
                        0.254829592
                ) *
                t *
                kotlin.math.exp(-x * x)

        return if (x >= 0) {
            y
        } else {
            -y
        }
    }

    private fun getAytNets(
        exam: Exam,
        field: AytField
    ): Map<String, Double> {

        return when (field) {

            AytField.SAYISAL ->
                mapOf(
                    "Matematik" to
                        (exam.subject("Matematik")?.net ?: 0.0),

                    "Fizik" to
                        (exam.subject("Fizik")?.net ?: 0.0),

                    "Kimya" to
                        (exam.subject("Kimya")?.net ?: 0.0),

                    "Biyoloji" to
                        (exam.subject("Biyoloji")?.net ?: 0.0)
                )

            AytField.ESIT_AGIRLIK ->
                mapOf(
                    "Matematik" to
                        (exam.subject("Matematik")?.net ?: 0.0),

                    "Edebiyat" to
                        (exam.subject("Edebiyat")?.net ?: 0.0),

                    "Tarih-1" to
                        (exam.subject("Tarih-1")?.net ?: 0.0),

                    "Coğrafya-1" to
                        (exam.subject("Coğrafya-1")?.net ?: 0.0)
                )

            AytField.SOZEL ->
                mapOf(
                    "Edebiyat" to
                        (exam.subject("Edebiyat")?.net ?: 0.0),

                    "Tarih-1" to
                        (exam.subject("Tarih-1")?.net ?: 0.0),

                    "Coğrafya-1" to
                        (exam.subject("Coğrafya-1")?.net ?: 0.0),

                    "Tarih-2" to
                        (exam.subject("Tarih-2")?.net ?: 0.0),

                    "Coğrafya-2" to
                        (exam.subject("Coğrafya-2")?.net ?: 0.0),

                    "Felsefe Grubu" to
                        (exam.subject("Felsefe Grubu")?.net ?: 0.0),

                    "Din Kültürü / Ek Felsefe" to
                        (
                            exam.subject(
                                "Din Kültürü / Ek Felsefe"
                            )?.net ?: 0.0
                        )
                )
        }
    }

    private fun combine(
        rank2025: Int,
        rank2026: Int
    ): RankingEstimate {

        val center =
            (
                rank2025 +
                    rank2026
            ) / 2.0

        return RankingEstimate(
            center = center
                .roundToInt()
                .coerceAtLeast(1),

            lower = minOf(
                rank2025,
                rank2026
            ).coerceAtLeast(1),

            upper = maxOf(
                rank2025,
                rank2026
            ).coerceAtLeast(1)
        )
    }

    private fun fieldKey(
        field: AytField
    ): String {

        return when (field) {

            AytField.SAYISAL ->
                "SAY"

            AytField.ESIT_AGIRLIK ->
                "EA"

            AytField.SOZEL ->
                "SÖZ"
        }
    }
}
