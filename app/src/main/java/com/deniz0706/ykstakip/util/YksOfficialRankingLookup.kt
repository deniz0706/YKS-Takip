package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.model.AytField
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.roundToInt

/**
 * Looks up estimated ranks from annual ÖSYM cumulative placement-score distributions.
 * Input is the placement score (exam score + OBP contribution).
 */
data class OfficialRankRange(
    val estimate: Int,
    val bestCase: Int,
    val worstCase: Int
)

object YksOfficialRankingLookup {
    private data class Point(val score: Double, val rank: Int)

    private val distributions: Map<Int, Map<String, List<Point>>> = mapOf(
        2025 to mapOf(
            "TYT" to points(550 to 14, 530 to 601, 510 to 3648, 490 to 11733, 470 to 27141, 450 to 52500, 430 to 88915, 410 to 137133, 390 to 201126, 370 to 286158, 350 to 397823, 330 to 545280, 310 to 737144, 290 to 976188, 270 to 1251236, 250 to 1542701, 230 to 1835041, 210 to 2099883, 190 to 2270224, 170 to 2308495, 150 to 2310553, 130 to 2310579, 115 to 2310579),
            "SAY" to points(550 to 57, 530 to 1930, 510 to 7081, 490 to 16140, 470 to 29410, 450 to 46142, 430 to 65449, 410 to 87117, 390 to 111498, 370 to 139619, 350 to 174355, 330 to 217778, 310 to 274252, 290 to 348397, 270 to 449880, 250 to 596635, 230 to 811201, 210 to 1052783, 190 to 1228141, 170 to 1287932, 150 to 1291491, 130 to 1291531, 115 to 1291531),
            "EA" to points(550 to 4, 530 to 58, 510 to 261, 490 to 742, 470 to 1629, 450 to 3422, 430 to 8145, 410 to 20244, 390 to 42590, 370 to 76959, 350 to 125389, 330 to 192949, 310 to 287630, 290 to 418975, 270 to 592803, 250 to 806796, 230 to 1042314, 210 to 1262545, 190 to 1425544, 170 to 1489209, 150 to 1494521, 130 to 1494611, 115 to 1494612),
            "SOZ" to points(550 to 1, 530 to 7, 510 to 26, 490 to 77, 470 to 254, 450 to 721, 430 to 1926, 410 to 5036, 390 to 12522, 370 to 28323, 350 to 57848, 330 to 108894, 310 to 190203, 290 to 309551, 270 to 468930, 250 to 658973, 230 to 849410, 210 to 1010174, 190 to 1121933, 170 to 1168866, 150 to 1173955, 130 to 1174046, 115 to 1174047)
        ),
        2026 to mapOf(
            "TYT" to points(550 to 112, 530 to 2045, 510 to 8638, 490 to 22600, 470 to 45313, 450 to 76021, 430 to 115071, 410 to 163211, 390 to 225038, 370 to 305570, 350 to 412011, 330 to 553526, 310 to 735519, 290 to 961261, 270 to 1219171, 250 to 1499060, 230 to 1782951, 210 to 2033331, 190 to 2166477, 170 to 2186977, 150 to 2187734, 130 to 2187742, 115 to 2187743),
            "SAY" to points(550 to 154, 530 to 3500, 510 to 12887, 490 to 27402, 470 to 44919, 450 to 63669, 430 to 83511, 410 to 105112, 390 to 129485, 370 to 157778, 350 to 191247, 330 to 232317, 310 to 282213, 290 to 344726, 270 to 425443, 250 to 533920, 230 to 681176, 210 to 858167, 190 to 1019046, 170 to 1117304, 150 to 1135198, 130 to 1135713, 115 to 1135718),
            "EA" to points(550 to 12, 530 to 98, 510 to 394, 490 to 1118, 470 to 2482, 450 to 5299, 430 to 12363, 410 to 29700, 390 to 58772, 370 to 97839, 350 to 148570, 330 to 215631, 310 to 307918, 290 to 429479, 270 to 585271, 250 to 775922, 230 to 990764, 210 to 1196809, 190 to 1347025, 170 to 1412649, 150 to 1421093, 130 to 1421289, 115 to 1421290),
            "SOZ" to points(550 to 4, 530 to 14, 510 to 69, 490 to 221, 470 to 606, 450 to 1566, 430 to 4058, 410 to 10184, 390 to 22750, 370 to 45237, 350 to 82479, 330 to 140496, 310 to 223004, 290 to 333238, 270 to 474443, 250 to 642816, 230 to 814264, 210 to 953036, 190 to 1040347, 170 to 1078859, 150 to 1085505, 130 to 1085697, 115 to 1085698)
        )
    )

    fun estimate(placementScore: Double, field: AytField?, isTyt: Boolean = false): OfficialRankRange? {
        if (!placementScore.isFinite()) return null
        val key = tableKey(field, isTyt) ?: return null
        val ranks = distributions.values.mapNotNull { it[key]?.let { table -> interpolate(table, placementScore) } }
        if (ranks.isEmpty()) return null
        val sorted = ranks.sorted()
        return OfficialRankRange(
            estimate = ((sorted.first().toDouble() + sorted.last()) / 2.0).roundToInt(),
            bestCase = sorted.first(),
            worstCase = sorted.last()
        )
    }

    fun estimateForYear(
        placementScore: Double,
        year: Int,
        field: AytField?,
        isTyt: Boolean = false
    ): Int? {
        if (!placementScore.isFinite()) return null
        val key = tableKey(field, isTyt) ?: return null
        val table = distributions[year]?.get(key) ?: return null
        return interpolate(table, placementScore)
    }

    fun estimateForYearLog(
        placementScore: Double,
        year: Int,
        field: AytField?,
        isTyt: Boolean = false
    ): Int? {
        if (!placementScore.isFinite()) return null
        val key = tableKey(field, isTyt) ?: return null
        val table = distributions[year]?.get(key) ?: return null
        return interpolateLog(table, placementScore)
    }

    private fun tableKey(field: AytField?, isTyt: Boolean): String? =
        if (isTyt) "TYT" else when (field) {
            AytField.SAYISAL -> "SAY"
            AytField.ESIT_AGIRLIK -> "EA"
            AytField.SOZEL -> "SOZ"
            null -> null
        }

    private fun interpolate(points: List<Point>, score: Double): Int {
        if (score >= points.first().score) return points.first().rank
        if (score <= points.last().score) return points.last().rank
        for (index in 0 until points.lastIndex) {
            val high = points[index]
            val low = points[index + 1]
            if (score <= high.score && score >= low.score) {
                val ratio = (high.score - score) / (high.score - low.score)
                return (high.rank + ratio * (low.rank - high.rank)).roundToInt().coerceAtLeast(1)
            }
        }
        return points.last().rank
    }

    private fun interpolateLog(points: List<Point>, score: Double): Int {
        if (score >= points.first().score) return points.first().rank
        if (score <= points.last().score) return points.last().rank
        for (index in 0 until points.lastIndex) {
            val high = points[index]
            val low = points[index + 1]
            if (score <= high.score && score >= low.score) {
                val ratio = (high.score - score) / (high.score - low.score)
                val interpolatedLogRank = ln(high.rank.toDouble()) +
                    ratio * (ln(low.rank.toDouble()) - ln(high.rank.toDouble()))
                return exp(interpolatedLogRank).roundToInt().coerceAtLeast(1)
            }
        }
        return points.last().rank
    }

    private fun points(vararg values: Pair<Int, Int>): List<Point> =
        values.map { Point(it.first.toDouble(), it.second) }

    private data class Point(val score: Double, val rank: Int)
}
