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

    private data class Stats(
        val mean: Double,
        val sd: Double
    )

    private data class Anchor(
        val score: Double,
        val rank: Int
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

    private val tytExam2025 = anchors(
        500 to 1,
        480 to 180,
        460 to 2_050,
        440 to 8_163,
        420 to 21_061,
        400 to 44_193,
        380 to 79_260,
        360 to 127_655,
        340 to 193_064,
        320 to 282_276,
        300 to 404_024,
        280 to 570_335,
        260 to 794_784,
        240 to 1_073_527,
        220 to 1_379_866,
        200 to 1_686_626,
        180 to 1_977_665,
        160 to 2_210_463,
        140 to 2_303_695,
        120 to 2_310_493,
        100 to 2_310_579
    )

    private val tytExam2026 = anchors(
        500 to 1,
        480 to 822,
        460 to 5_524,
        440 to 17_050,
        420 to 37_770,
        400 to 67_394,
        380 to 106_404,
        360 to 155_008,
        340 to 218_156,
        320 to 302_758,
        300 to 417_935,
        280 to 577_094,
        260 to 787_244,
        240 to 1_045_340,
        220 to 1_332_391,
        200 to 1_630_698,
        180 to 1_914_717,
        160 to 2_125_244,
        140 to 2_184_873,
        120 to 2_187_723,
        100 to 2_187_743
    )

    private val placement2025 = mapOf(
        "TYT" to anchors(
            550 to 14,
            530 to 601,
            510 to 3_648,
            490 to 11_733,
            470 to 27_141,
            450 to 52_500,
            430 to 88_915,
            410 to 137_133,
            390 to 201_126,
            370 to 286_158,
            350 to 397_823,
            330 to 545_280,
            310 to 737_144,
            290 to 976_188,
            270 to 1_251_236,
            250 to 1_542_701,
            230 to 1_835_041,
            210 to 2_099_883,
            190 to 2_270_224,
            170 to 2_308_495,
            150 to 2_310_553,
            130 to 2_310_579,
            115 to 2_310_579
        ),
        "SAY" to anchors(
            550 to 57,
            530 to 1_930,
            510 to 7_081,
            490 to 16_140,
            470 to 29_410,
            450 to 46_142,
            430 to 65_449,
            410 to 87_117,
            390 to 111_498,
            370 to 139_619,
            350 to 174_355,
            330 to 217_778,
            310 to 274_252,
            290 to 348_397,
            270 to 449_880,
            250 to 596_635,
            230 to 811_201,
            210 to 1_052_783,
            190 to 1_228_141,
            170 to 1_287_932,
            150 to 1_291_491,
            130 to 1_291_531,
            115 to 1_291_531
        ),
        "EA" to anchors(
            550 to 4,
            530 to 58,
            510 to 261,
            490 to 742,
            470 to 1_629,
            450 to 3_422,
            430 to 8_145,
            410 to 20_244,
            390 to 42_590,
            370 to 76_959,
            350 to 125_389,
            330 to 192_949,
            310 to 287_630,
            290 to 418_975,
            270 to 592_803,
            250 to 806_796,
            230 to 1_042_314,
            210 to 1_262_545,
            190 to 1_425_544,
            170 to 1_489_209,
            150 to 1_494_521,
            130 to 1_494_611,
            115 to 1_494_612
        ),
        "SÖZ" to anchors(
            550 to 1,
            530 to 7,
            510 to 26,
            490 to 77,
            470 to 254,
            450 to 721,
            430 to 1_926,
            410 to 5_036,
            390 to 12_522,
            370 to 28_323,
            350 to 57_848,
            330 to 108_894,
            310 to 190_203,
            290 to 309_551,
            270 to 468_930,
            250 to 658_973,
            230 to 849_410,
            210 to 1_010_174,
            190 to 1_121_933,
            170 to 1_168_866,
            150 to 1_173_955,
            130 to 1_174_046,
            115 to 1_174_047
        )
    )

    private val placement2026 = mapOf(
        "TYT" to anchors(
            550 to 112,
            530 to 2_045,
            510 to 8_638,
            490 to 22_600,
            470 to 45_313,
            450 to 76_021,
            430 to 115_071,
            410 to 163_211,
            390 to 225_038,
            370 to 305_570,
            350 to 412_011,
            330 to 553_526,
            310 to 735_519,
            290 to 961_261,
            270 to 1_219_171,
            250 to 1_499_060,
            230 to 1_782_951,
            210 to 2_033_331,
            190 to 2_166_477,
            170 to 2_186_977,
            150 to 2_187_734,
            130 to 2_187_742,
            115 to 2_187_743
        ),
        "SAY" to anchors(
            550 to 154,
            530 to 3_500,
            510 to 12_887,
            490 to 27_402,
            470 to 44_919,
            450 to 63_669,
            430 to 83_511,
            410 to 105_112,
            390 to 129_485,
            370 to 157_778,
            350 to 191_247,
            330 to 232_317,
            310 to 282_213,
            290 to 344_726,
            270 to 425_443,
            250 to 533_920,
            230 to 681_176,
            210 to 858_167,
            190 to 1_019_046,
            170 to 1_117_304,
            150 to 1_135_198,
            130 to 1_135_713,
            115 to 1_135_718
        ),
        "EA" to anchors(
            550 to 12,
            530 to 98,
            510 to 394,
            490 to 1_118,
            470 to 2_482,
            450 to 5_299,
            430 to 12_363,
            410 to 29_700,
            390 to 58_772,
            370 to 97_839,
            350 to 148_570,
            330 to 215_631,
            310 to 307_918,
            290 to 429_479,
            270 to 585_271,
            250 to 775_922,
            230 to 990_764,
            210 to 1_196_809,
            190 to 1_347_025,
            170 to 1_412_649,
            150 to 1_421_093,
            130 to 1_421_289,
            115 to 1_421_290
        ),
        "SÖZ" to anchors(
            550 to 4,
            530 to 14,
            510 to 69,
            490 to 221,
            470 to 606,
            450 to 1_566,
            430 to 4_058,
            410 to 10_184,
            390 to 22_750,
            370 to 45_237,
            350 to 82_479,
            330 to 140_496,
            310 to 223_004,
            290 to 333_238,
            270 to 474_443,
            250 to 642_816,
            230 to 814_264,
            210 to 953_036,
            190 to 1_040_347,
            170 to 1_078_859,
            150 to 1_085_505,
            130 to 1_085_697,
            115 to 1_085_698
        )
    )

    fun estimateTyt(exam: Exam, obp: Double): RankingEstimate? {
        if (exam.type != ExamType.TYT) return null

        val nets = mapOf(
            "Türkçe" to (exam.subject("Türkçe")?.net ?: 0.0),
            "Sosyal Bilimler" to (exam.subject("Sosyal Bilimler")?.net ?: 0.0),
            "Matematik" to (exam.subject("Matematik")?.net ?: 0.0),
            "Fen Bilimleri" to (exam.subject("Fen Bilimleri")?.net ?: 0.0)
        )

        if (nets.values.none { it != 0.0 }) return null

        val weights = tytWeights()

        val z2025 = normalizedZ(nets, tyt2025, weights)
        val z2026 = normalizedZ(nets, tyt2026, weights)

        val rank2025 = rankFromZ(z2025, tytExam2025.last().rank)
        val rank2026 = rankFromZ(z2026, tytExam2026.last().rank)

        return combine(rank2025, rank2026)
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

        val aytNets = getAytNets(aytExam, field)

        if (tytNets.values.none { it != 0.0 }) return null
        if (aytNets.values.none { it != 0.0 }) return null

        val aytWeights = aytWeightsFor(field)
        val key = fieldKey(field)

        val obpZ = (obp.coerceIn(0.0, 100.0) - 65.0) / 15.0

        val tytZ2025 = normalizedZ(tytNets, tyt2025, tytWeights())
        val tytZ2026 = normalizedZ(tytNets, tyt2026, tytWeights())

        val aytZ2025 = normalizedZ(aytNets, ayt2025, aytWeights)
        val aytZ2026 = normalizedZ(aytNets, ayt2026, aytWeights)

        val combinedZ2025 = combinePlacementZ(tytZ2025, aytZ2025, obpZ)
        val combinedZ2026 = combinePlacementZ(tytZ2026, aytZ2026, obpZ)

        val rank2025 = rankFromZ(
            combinedZ2025,
            placement2025.getValue(key).last().rank
        )

        val rank2026 = rankFromZ(
            combinedZ2026,
            placement2026.getValue(key).last().rank
        )

        return combine(rank2025, rank2026)
    }

    private fun tytWeights(): List<Pair<String, Double>> = listOf(
        "Türkçe" to 0.33,
        "Sosyal Bilimler" to 0.17,
        "Matematik" to 0.33,
        "Fen Bilimleri" to 0.17
    )

    private fun aytWeightsFor(field: AytField): List<Pair<String, Double>> {
        return when (field) {
            AytField.SAYISAL -> listOf(
                "Matematik" to 0.30,
                "Fizik" to 0.10,
                "Kimya" to 0.10,
                "Biyoloji" to 0.10
            )

            AytField.ESIT_AGIRLIK -> listOf(
                "Matematik" to 0.30,
                "Edebiyat" to 0.18,
                "Tarih-1" to 0.07,
                "Coğrafya-1" to 0.05
            )

            AytField.SOZEL -> listOf(
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
        val weightedSum = weights.sumOf { (key, weight) ->
            val net = nets[key] ?: 0.0
            val stat = stats[key] ?: return@sumOf 0.0
            ((net - stat.mean) / stat.sd) * weight
        }

        val variance = weights.sumOf { it.second * it.second }
        val sd = kotlin.math.sqrt(variance).takeIf { it > 0.0 } ?: 1.0

        return weightedSum / sd
    }

    private fun combinePlacementZ(
        tytZ: Double,
        aytZ: Double,
        obpZ: Double
    ): Double {
        val wt = 0.40
        val wa = 0.60
        val wo = 0.08

        val weighted = wt * tytZ + wa * aytZ + wo * obpZ
        val norm = kotlin.math.sqrt(
            wt * wt + wa * wa + wo * wo
        )

        return weighted / norm
    }

    private fun rankFromZ(
        z: Double,
        totalCandidates: Int
    ): Int {
        val percentileAbove = 1.0 - normalCdf(z)

        return (totalCandidates * percentileAbove)
            .roundToInt()
            .coerceAtLeast(1)
    }

    private fun normalCdf(z: Double): Double {
        return 0.5 * (
            1.0 + erf(z / kotlin.math.sqrt(2.0))
        )
    }

    private fun erf(x: Double): Double {
        val t = 1.0 / (
            1.0 + 0.3275911 * kotlin.math.abs(x)
        )

        val y = 1.0 -
                (((((1.061405429 * t - 1.453152027) * t)
                    + 1.421413741) * t
                    - 0.284496736) * t
                    + 0.254829592) * t *
                    kotlin.math.exp(-x * x)

        return if (x >= 0) y else -y
    }

    private fun getAytNets(
        exam: Exam,
        field: AytField
    ): Map<String, Double> {
        return when (field) {
            AytField.SAYISAL -> mapOf(
                "Matematik" to (exam.subject("Matematik")?.net ?: 0.0),
                "Fizik" to (exam.subject("Fizik")?.net ?: 0.0),
                "Kimya" to (exam.subject("Kimya")?.net ?: 0.0),
                "Biyoloji" to (exam.subject("Biyoloji")?.net ?: 0.0)
            )

            AytField.ESIT_AGIRLIK -> mapOf(
                "Matematik" to (exam.subject("Matematik")?.net ?: 0.0),
                "Edebiyat" to (exam.subject("Edebiyat")?.net ?: 0.0),
                "Tarih-1" to (exam.subject("Tarih-1")?.net ?: 0.0),
                "Coğrafya-1" to (exam.subject("Coğrafya-1")?.net ?: 0.0)
            )

            AytField.SOZEL -> mapOf(
                "Edebiyat" to (exam.subject("Edebiyat")?.net ?: 0.0),
                "Tarih-1" to (exam.subject("Tarih-1")?.net ?: 0.0),
                "Coğrafya-1" to (exam.subject("Coğrafya-1")?.net ?: 0.0),
                "Tarih-2" to (exam.subject("Tarih-2")?.net ?: 0.0),
                "Coğrafya-2" to (exam.subject("Coğrafya-2")?.net ?: 0.0),
                "Felsefe Grubu" to (exam.subject("Felsefe Grubu")?.net ?: 0.0),
                "Din Kültürü / Ek Felsefe" to (exam.subject("Din Kültürü / Ek Felsefe")?.net ?: 0.0)
            )
        }
    }

    private fun combine(
        rank2025: Int,
        rank2026: Int
    ): RankingEstimate {
        val center = (
            (rank2025 + rank2026) / 2.0
        ).roundToInt()

        return RankingEstimate(
            center = center.coerceAtLeast(1),
            lower = minOf(rank2025, rank2026).coerceAtLeast(1),
            upper = maxOf(rank2025, rank2026).coerceAtLeast(1)
        )
    }

    private fun anchors(
        vararg values: Pair<Int, Int>
    ): List<Anchor> {
        return values
            .map {
                Anchor(
                    score = it.first.toDouble(),
                    rank = it.second
                )
            }
            .sortedByDescending { it.score }
    }

    private fun fieldKey(field: AytField): String {
        return when (field) {
            AytField.SAYISAL -> "SAY"
            AytField.ESIT_AGIRLIK -> "EA"
            AytField.SOZEL -> "SÖZ"
        }
    }
}
