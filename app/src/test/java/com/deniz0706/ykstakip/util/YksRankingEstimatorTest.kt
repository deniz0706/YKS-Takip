package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.model.AytField
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.model.SubjectResult
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class YksRankingEstimatorTest {
    private val diploma = 88.2

    private fun subj(name: String, questions: Int, correct: Int, wrong: Int) =
        SubjectResult(name, questions, correct, wrong, 0)

    private fun tyt(vararg subjects: SubjectResult) = Exam(
        id = 1, title = "TYT", date = "2026-06-20",
        type = ExamType.TYT, subjects = subjects.toList()
    )

    private fun ayt(vararg subjects: SubjectResult) = Exam(
        id = 2, title = "AYT", date = "2026-06-21",
        type = ExamType.AYT, aytField = AytField.SAYISAL, subjects = subjects.toList()
    )

    private val realTyt = tyt(
        subj("Türkçe", 40, 35, 3),
        subj("Sosyal Bilimler", 20, 11, 5),
        subj("Matematik", 40, 33, 1),
        subj("Fen Bilimleri", 20, 19, 1)
    )

    private val realAyt = ayt(
        subj("Matematik", 40, 29, 3),
        subj("Fizik", 14, 12, 2),
        subj("Kimya", 13, 11, 2),
        subj("Biyoloji", 13, 11, 2)
    )

    @Test fun realTytCalibrationRegression() {
        val result = YksRankingEstimator.estimateTyt(realTyt, diploma)
        assertNotNull(result)
        assertTrue("center ${result!!.center}", result.center in 29_500..30_600)
        assertTrue(result.lower < result.center && result.center < result.upper)
    }

    @Test fun realSayCalibrationRegression() {
        val result = YksRankingEstimator.estimateAyt(realTyt, realAyt, diploma, AytField.SAYISAL)
        assertNotNull(result)
        assertTrue("center ${result!!.center}", result.center in 45_200..46_200)
        assertTrue(result.lower < result.center && result.center < result.upper)
    }

    @Test fun moreNetMeansBetterRank() {
        val better = tyt(
            subj("Türkçe", 40, 36, 2),
            subj("Sosyal Bilimler", 20, 11, 5),
            subj("Matematik", 40, 34, 0),
            subj("Fen Bilimleri", 20, 19, 1)
        )
        val base = YksRankingEstimator.estimateTyt(realTyt, diploma)!!
        val improved = YksRankingEstimator.estimateTyt(better, diploma)!!
        assertTrue(improved.center < base.center)
    }

    @Test fun previouslyPlacedHalvesDiplomaContributionSoRankGetsWorse() {
        val normal = YksRankingEstimator.estimateTyt(realTyt, diploma)!!
        val previous = YksRankingEstimator.estimateTyt(realTyt, diploma, previouslyPlaced = true)!!
        assertTrue(previous.center > normal.center)
    }

    @Test fun obpOutsideDiplomaRangeReturnsNull() {
        assertNull(YksRankingEstimator.estimateTyt(realTyt, 440.0))
        assertNull(YksRankingEstimator.estimateTyt(realTyt, Double.NaN))
        assertNull(YksRankingEstimator.estimateTyt(realTyt, -1.0))
    }

    @Test fun uncalibratedFieldsReturnNull() {
        assertNull(YksRankingEstimator.estimateAyt(realTyt, realAyt, diploma, AytField.ESIT_AGIRLIK))
        assertNull(YksRankingEstimator.estimateAyt(realTyt, realAyt, diploma, AytField.SOZEL))
    }

    @Test fun wrongExamTypeOrEmptyNetsReturnNull() {
        assertNull(YksRankingEstimator.estimateTyt(realAyt, diploma))
        val empty = tyt(subj("Türkçe", 40, 0, 0))
        assertNull(YksRankingEstimator.estimateTyt(empty, diploma))
    }
}
