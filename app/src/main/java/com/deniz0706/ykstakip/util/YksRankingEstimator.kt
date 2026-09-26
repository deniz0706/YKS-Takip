package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.model.AytField
import com.deniz0706.ykstakip.model.Exam

data class RankingEstimate(
    val center: Int,
    val lower: Int,
    val upper: Int
)

object YksRankingEstimator {

    fun estimate(
        exam: Exam,
        obp: Double,
        field: AytField
    ): RankingEstimate? {
        if (exam.type.name != "AYT") return null

        val tytNet = exam.subjects
            .filter { it.subject.startsWith("TYT") }
            .sumOf { it.net }

        val aytNet = exam.subjects
            .filter { !it.subject.startsWith("TYT") }
            .sumOf { it.net }

        if (tytNet <= 0.0 || aytNet <= 0.0) return null

        /*
         * Bu ilk sürümün iskeletidir.
         *
         * Gerçek sıralama katsayıları ÖSYM'nin 2025-2026
         * dağılım verileriyle ayrıca kalibre edilecektir.
         */
        val score = when (field) {
            AytField.SAYISAL -> tytNet * 0.4 + aytNet * 0.6
            AytField.EA -> tytNet * 0.45 + aytNet * 0.55
            AytField.SOZEL -> tytNet * 0.5 + aytNet * 0.5
        }

        val obpEffect = obp.coerceIn(0.0, 100.0) * 0.1
        val adjusted = score + obpEffect

        val center = when {
            adjusted >= 100 -> 1000
            adjusted >= 90 -> 5000
            adjusted >= 80 -> 15000
            adjusted >= 70 -> 30000
            adjusted >= 60 -> 60000
            adjusted >= 50 -> 100000
            adjusted >= 40 -> 200000
            adjusted >= 30 -> 400000
            else -> 600000
        }

        return RankingEstimate(
            center = center,
            lower = (center * 0.75).toInt(),
            upper = (center * 1.25).toInt()
        )
    }
}
