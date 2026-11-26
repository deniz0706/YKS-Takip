package com.deniz0706.ykstakip.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeInsightsTest {
    @Test
    fun subtitleReflectsDailyGoalState() {
        assertEquals("Hazırlığını verilerle takip et.", HomeInsights.subtitle(0, null))
        assertEquals("Bugün 35 soru çözdün.", HomeInsights.subtitle(35, null))
        assertEquals("Bugün 35 / 50 soru.", HomeInsights.subtitle(35, 50))
        assertEquals("Günlük hedefini tamamladın.", HomeInsights.subtitle(50, 50))
        assertEquals("Günlük hedefini tamamladın.", HomeInsights.subtitle(65, 50))
    }

    @Test
    fun goalPercentCalculatesAndCapsDisplayValue() {
        assertEquals(95, HomeInsights.goalPercent(114.0, 120f))
        assertEquals(125, HomeInsights.goalPercent(100.0, 80f))
        assertEquals(999, HomeInsights.goalPercent(1200.0, 100f))
    }

    @Test
    fun goalPercentRejectsMissingAndInvalidInputs() {
        assertNull(HomeInsights.goalPercent(null, 100f))
        assertNull(HomeInsights.goalPercent(100.0, null))
        assertNull(HomeInsights.goalPercent(100.0, 0f))
        assertNull(HomeInsights.goalPercent(100.0, -1f))
        assertNull(HomeInsights.goalPercent(Double.NaN, 100f))
        assertNull(HomeInsights.goalPercent(Double.POSITIVE_INFINITY, 100f))
        assertNull(HomeInsights.goalPercent(100.0, Float.POSITIVE_INFINITY))
    }

    @Test
    fun trendCopyDoesNotClaimFivePreviousExamsExistWhenTheyDoNot() {
        val sentence = HomeInsights.trendSentence(105.0, null, 3)
        assertEquals(
            "Son 3 denemenin ortalaması hazır. Dönem karşılaştırması için en az 5 eski deneme daha ekle.",
            sentence
        )
    }

    @Test
    fun trendCopyUsesHalfNetAsNeutralBand() {
        assertEquals(
            "Son 5 denemede yükseliş var. Önceki 5 denemeye göre ortalaman arttı.",
            HomeInsights.trendSentence(100.6, 100.0, 5)
        )
        assertEquals(
            "Son 5 denemede ortalama geriledi. Zayıf branşlarını inceleyebilirsin.",
            HomeInsights.trendSentence(99.4, 100.0, 5)
        )
        assertEquals(
            "Son 5 denemede performansın dengeli seyrediyor.",
            HomeInsights.trendSentence(100.5, 100.0, 5)
        )
    }
}
