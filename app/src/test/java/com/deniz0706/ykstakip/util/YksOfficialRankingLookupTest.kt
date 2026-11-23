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
            38_869,
            YksOfficialRankingLookup.estimateForYear(
                placementScore = 460.0,
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
