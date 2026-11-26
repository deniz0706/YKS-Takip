package com.deniz0706.ykstakip.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertFailsWith

class ExamAnalyticsTest {
    private fun exam(day: Int, correct: Int, wrong: Int = 0) = Exam(
        id = day.toLong(),
        title = "Deneme $day",
        date = "2026-11-%02d".format(day),
        type = ExamType.TYT,
        subjects = listOf(SubjectResult("Türkçe", 40, correct, wrong, 30))
    )

    @Test
    fun emptyHistoryHasNoInventedMetrics() {
        val summary = ExamAnalytics.summarize(emptyList())
        assertEquals(0, summary.attemptCount)
        assertNull(summary.latestNet)
        assertNull(summary.previousNet)
        assertNull(summary.change)
        assertNull(summary.averageNet)
        assertNull(summary.bestNet)
        assertNull(summary.recentFiveAverage)
        assertNull(summary.previousFiveAverage)
        assertEquals(emptyList(), ExamAnalytics.trend(emptyList()))
    }

    @Test
    fun sortsChronologicallyAndCalculatesLatestMetrics() {
        val summary = ExamAnalytics.summarize(listOf(exam(3, 35), exam(1, 25), exam(2, 30)))
        assertEquals(3, summary.attemptCount)
        assertEquals(35.0, summary.latestNet)
        assertEquals(30.0, summary.previousNet)
        assertEquals(5.0, summary.change)
        assertEquals(30.0, summary.averageNet)
        assertEquals(35.0, summary.bestNet)
        assertEquals(listOf(25.0, 30.0, 35.0), ExamAnalytics.trend(
            listOf(exam(3, 35), exam(1, 25), exam(2, 30))
        ))
        assertEquals(listOf(30.0, 35.0), ExamAnalytics.trend(
            listOf(exam(3, 35), exam(1, 25), exam(2, 30)), limit = 2
        ))
    }

    @Test
    fun comparesOnlyCompleteFiveExamWindows() {
        val ten = (1..10).map { exam(it, 20 + it) }
        val summary = ExamAnalytics.summarize(ten)
        assertEquals(5, summary.recentCount)
        assertEquals(28.0, summary.previousFiveAverage)
        assertEquals(33.0, summary.recentFiveAverage)

        val nine = ExamAnalytics.summarize(ten.dropLast(1))
        assertEquals(4, nine.recentCount)
        assertNull(nine.previousFiveAverage)
    }

    @Test
    fun rejectsNonPositiveTrendLimit() {
        assertFailsWith<IllegalArgumentException> {
            ExamAnalytics.trend(listOf(exam(1, 20)), limit = 0)
        }
    }
}
