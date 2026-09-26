package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.model.AytField
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.model.SubjectConfigs

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
        if (exam.type != ExamType.AYT) return null

        val aytSubjects = SubjectConfigs.subjectsFor(field)
            .map { it.name }
            .toSet()

        val aytNet = exam.subjects
            .filter { it.subject in aytSubjects }
            .sumOf { it.net }

        if (aytNet <= 0.0) return null

        /*
         * Bu sürüm henüz gerçek sıralama katsayılarını içermez.
         *
         * Ama artık uygulamadaki gerçek AYT alan/ders yapısını
         * doğru şekilde kullanıyor.
         *
         * Bir sonraki adımda:
         * 1. TYT + AYT puan hesabını kuracağız.
         * 2. OBP katkısını doğru şekilde ekleyeceğiz.
         * 3. 2025-2026 ÖSYM verileriyle sıralama aralığını
         *    kalibre edeceğiz.
         */

        val adjusted = when (field) {
            AytField.SAYISAL ->
                aytNet + obp * 0.10

            AytField.ESIT_AGIRLIK ->
                aytNet + obp * 0.10

            AytField.SOZEL ->
                aytNet + obp * 0.10
        }

        val center = when {
            adjusted >= 90 -> 1000
            adjusted >= 80 -> 5000
            adjusted >= 70 -> 15000
            adjusted >= 60 -> 30000
            adjusted >= 50 -> 60000
            adjusted >= 40 -> 100000
            adjusted >= 30 -> 200000
            adjusted >= 20 -> 400000
            else -> 600000
        }

        return RankingEstimate(
            center = center,
            lower = (center * 0.75).toInt(),
            upper = (center * 1.25).toInt()
        )
    }
}
