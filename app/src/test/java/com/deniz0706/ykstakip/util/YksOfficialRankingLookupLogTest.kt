package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.model.AytField
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class YksOfficialRankingLookupLogTest {
    private fun say(score: Double): Int? =
        YksOfficialRankingLookup.estimateForYearLog(score, 2026, AytField.SAYISAL)

    private fun tyt(score: Double): Int? =
        YksOfficialRankingLookup.estimateForYearLog(score, 2026, null, isTyt = true)

    private fun assertNear(expected: Int, actual: Int?, tolerance: Int = 1) {
        assertTrue("expected ~$expected but was $actual", actual != null)
        assertTrue("expected ~$expected but was $actual", kotlin.math.abs(actual!! - expected) <= tolerance)
    }

    @Test fun exactAnchorsReturnOfficialCounts() {
        assertEquals(63_669, say(450.0))
        assertEquals(76_021, tyt(450.0))
    }

    @Test fun midpointIsGeometricMeanOfNeighbours() {
        assertNear(53_478, say(460.0))
        assertNear(58_692, tyt(460.0))
    }

    @Test fun scoresOutsideTableAreClamped() {
        assertEquals(154, say(600.0))
        assertEquals(1_135_718, say(50.0))
    }

    @Test fun rankNeverImprovesWhenScoreDrops() {
        var previous = 0
        var score = 550.0
        while (score >= 115.0) {
            val rank = say(score)!!
            assertTrue("rank dropped at score $score", rank >= previous)
            previous = rank
            score -= 0.5
        }
    }

    @Test fun logEstimateNeverExceedsLinearEstimate() {
        var score = 120.0
        while (score <= 545.0) {
            val log = tyt(score)!!
            val linear = YksOfficialRankingLookup.estimateForYear(score, 2026, null, isTyt = true)!!
            assertTrue("log $log > linear $linear at $score", log <= linear + 1)
            score += 1.0
        }
    }

    @Test fun unsupportedInputsReturnNull() {
        assertNull(YksOfficialRankingLookup.estimateForYearLog(450.0, 2024, AytField.SAYISAL))
        assertNull(YksOfficialRankingLookup.estimateForYearLog(Double.NaN, 2026, AytField.SAYISAL))
        assertNull(YksOfficialRankingLookup.estimateForYearLog(450.0, 2026, null))
    }
}
