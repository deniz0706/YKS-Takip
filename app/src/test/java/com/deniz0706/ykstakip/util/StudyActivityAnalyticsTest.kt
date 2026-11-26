package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.model.StudyDay
import org.junit.Assert.assertEquals
import org.junit.Test

class StudyActivityAnalyticsTest {
    @Test fun summarizesCurrentAndPreviousSevenDayWindows() {
        val days = listOf(
            StudyDay("2026-11-13", 40, 60),
            StudyDay("2026-11-18", 80, 90),
            StudyDay("2026-11-20", 60, 45, estimated = true),
            StudyDay("2026-11-25", 100, 120),
            StudyDay("2026-11-26", 20, 30)
        )
        val result = StudyActivityAnalytics.summarize(days, "2026-11-26", 50)
        assertEquals(260, result.currentQuestions)
        assertEquals(40, result.previousQuestions)
        assertEquals(285, result.currentMinutes)
        assertEquals(4, result.activeDays)
        assertEquals(3, result.goalDays)
        assertEquals(1, result.estimatedDays)
        assertEquals(65, result.averageQuestionsPerActiveDay)
    }

    @Test fun excludesDatesOutsideBothWindowsAndClampsNegativeValues() {
        val days = listOf(
            StudyDay("2026-11-12", 999, 999),
            StudyDay("2026-11-19", -4, -5),
            StudyDay("2026-11-26", 10, 20)
        )
        val result = StudyActivityAnalytics.summarize(days, "2026-11-26", 0)
        assertEquals(10, result.currentQuestions)
        assertEquals(0, result.previousQuestions)
        assertEquals(20, result.currentMinutes)
        assertEquals(1, result.activeDays)
        assertEquals(0, result.goalDays)
        assertEquals(10, result.averageQuestionsPerActiveDay)
    }

    @Test fun invalidTodayReturnsAnEmptySummary() {
        val result = StudyActivityAnalytics.summarize(
            listOf(StudyDay("2026-11-26", 10, 20)), "invalid", 10
        )
        assertEquals(0, result.currentQuestions)
        assertEquals(0, result.activeDays)
    }
}
