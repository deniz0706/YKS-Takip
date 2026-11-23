package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.model.AytField
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YksOfficialRankingLookupTest {

    @Test
    fun exact2026SayAnchorReturnsOfficialCumulativeCount() {
        assertEquals(
            63_669,
            YksOfficialRankingLookup.estimateForYear(
                placementScore = 450.0,
                year = 2026,
                field = AytField.SAYISAL
            )
        )
    }

    @Test
    fun exact2026TytAnchorReturnsOfficialCumulativeCount() {
        assertEquals(
            76_021,
            YksOfficialRankingLookup.estimateForYear(
                placementScore = 450.0,
                year = 2026,
                field = null,
                isTyt = true
            )
        )
    }

    @Test
    fun exact2025TytAnchorReturnsOfficialCumulativeCount() {
        assertEquals(
            52_500,
            YksOfficialRankingLookup.estimateForYear(
                placementScore = 450.0,
                year = 2025,
                field = null,
                isTyt = true
            )
        )
    }

    @Test
    fun midpointIsInterpolatedBetweenAdjacentThresholds() {
        assertEquals(
            54_294,
            YksOfficialRankingLookup.estimateForYear(
                placementScore = 460.0,
                year = 2026,
                field = AytField.SAYISAL
            )
        )
    }

    @Test
    fun combinedEstimateContainsBothYearEstimates() {
        val result = YksOfficialRankingLookup.estimate(
            placementScore = 450.0,
            field = AytField.SAYISAL
        )!!

        assertEquals(54_906, result.estimate)
        assertEquals(46_142, result.bestCase)
        assertEquals(63_669, result.worstCase)
    }

    @Test
    fun scoresOutsideTableAreClampedToNearestPublishedThreshold() {
        assertEquals(
            154,
            YksOfficialRankingLookup.estimateForYear(
                placementScore = 600.0,
                year = 2026,
                field = AytField.SAYISAL
            )
        )
        assertEquals(
            1_135_718,
            YksOfficialRankingLookup.estimateForYear(
                placementScore = 50.0,
                year = 2026,
                field = AytField.SAYISAL
            )
        )
    }

    @Test
    fun unsupportedYearReturnsNull() {
        assertNull(
            YksOfficialRankingLookup.estimateForYear(
                placementScore = 450.0,
                year = 2024,
                field = AytField.SAYISAL
            )
        )
    }
}
