package com.deniz0706.ykstakip.domain.usecase

import com.deniz0706.ykstakip.domain.model.AytField
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import kotlin.math.roundToInt

/**
 * Ranking estimator ported from Android app layer, now using domain models
 * so it compiles for iOS, JVM, and Android.
 *
 * Calculation logic is IDENTICAL to the Android YksRankingEstimator;
 * only the model types have changed from com.deniz0706.ykstakip.model.*
 * to com.deniz0706.ykstakip.domain.model.*.
 */
object YksRankingEstimator {
    private const val TABLE_YEAR = 2026
    private const val TYT_INTERCEPT = 124.1
    private const val SAY_INTERCEPT = 124.3
    private const val SAY_TYT_SHARE = 0.4
    private const val OBP_COEFFICIENT = 0.12
    private const val PREVIOUSLY_PLACED_OBP_COEFFICIENT = 0.06
    private const val ASSUMED_BAND_POINTS = 8.0

    private val tytSlopes = mapOf(
        "Türkçe" to 3.2,
        "Sosyal Bilimler" to 3.6,
        "Matematik" to 3.4,
        "Fen Bilimleri" to 2.6
    )

    private val sayAytSlopes = mapOf(
        "Matematik" to 3.1,
        "Fizik" to 2.5,
        "Kimya" to 2.5,
        "Biyoloji" to 2.6
    )

    fun estimateTyt(
        exam: Exam,
        obp: Double,
        previouslyPlaced: Boolean = false
    ): RankingEstimate? {
        if (exam.type != ExamType.TYT || !validDiploma(obp)) return null
        val nets = subjectNets(exam, tytSlopes.keys)
        if (!hasAnyNet(nets)) return null

        val rawScore = TYT_INTERCEPT + weightedNets(nets, tytSlopes)
        val placementScore = placementScore(rawScore, obp, previouslyPlaced)
        return rankRange(placementScore, null, isTyt = true)
    }

    fun estimateAyt(
        tytExam: Exam,
        aytExam: Exam,
        obp: Double,
        field: AytField,
        previouslyPlaced: Boolean = false
    ): RankingEstimate? {
        if (field != AytField.SAYISAL) return null
        if (tytExam.type != ExamType.TYT || aytExam.type != ExamType.AYT) return null
        if (aytExam.aytField != null && aytExam.aytField != field) return null
        if (!validDiploma(obp)) return null

        val tytNets = subjectNets(tytExam, tytSlopes.keys)
        val aytNets = subjectNets(aytExam, sayAytSlopes.keys)
        if (!hasAnyNet(tytNets) || !hasAnyNet(aytNets)) return null

        val rawScore = SAY_INTERCEPT +
            SAY_TYT_SHARE * weightedNets(tytNets, tytSlopes) +
            weightedNets(aytNets, sayAytSlopes)
        val placementScore = placementScore(rawScore, obp, previouslyPlaced)
        return rankRange(placementScore, field, isTyt = false)
    }

    private fun subjectNets(exam: Exam, names: Set<String>): Map<String, Double> =
        names.associateWith { exam.subject(it)?.net ?: 0.0 }

    private fun hasAnyNet(nets: Map<String, Double>): Boolean =
        nets.values.any { it.isFinite() && it != 0.0 }

    private fun weightedNets(
        nets: Map<String, Double>,
        slopes: Map<String, Double>
    ): Double = slopes.entries.sumOf { (name, slope) ->
        val net = nets[name] ?: 0.0
        if (net.isFinite()) net * slope else 0.0
    }

    private fun validDiploma(obp: Double): Boolean =
        obp.isFinite() && obp in 0.0..100.0

    private fun placementScore(
        rawScore: Double,
        diplomaGrade: Double,
        previouslyPlaced: Boolean
    ): Double {
        val coefficient = if (previouslyPlaced) {
            PREVIOUSLY_PLACED_OBP_COEFFICIENT
        } else {
            OBP_COEFFICIENT
        }
        val obpContribution = diplomaGrade.coerceAtLeast(50.0) * 5.0 * coefficient
        return rawScore.coerceIn(100.0, 500.0) + obpContribution
    }

    private fun rankRange(
        score: Double,
        field: AytField?,
        isTyt: Boolean
    ): RankingEstimate? {
        val center = YksOfficialRankingLookup.estimateForYearLog(
            score, TABLE_YEAR, field, isTyt
        ) ?: return null
        val betterRank = YksOfficialRankingLookup.estimateForYearLog(
            score + ASSUMED_BAND_POINTS, TABLE_YEAR, field, isTyt
        ) ?: return null
        val worseRank = YksOfficialRankingLookup.estimateForYearLog(
            score - ASSUMED_BAND_POINTS, TABLE_YEAR, field, isTyt
        ) ?: return null

        return RankingEstimate(
            center = center,
            lower = minOf(betterRank, worseRank),
            upper = maxOf(betterRank, worseRank)
        )
    }
}
