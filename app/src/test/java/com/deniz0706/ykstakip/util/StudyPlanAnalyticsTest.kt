package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.domain.model.StudyPlanItem
import org.junit.Assert.assertEquals
import org.junit.Test

class StudyPlanAnalyticsTest {
    private fun item(id: Long, subject: String, start: String, end: String, completed: Boolean = false) =
        StudyPlanItem(id, 1, start, end, subject, "", completed)

    @Test fun sumsPlannedAndCompletedMinutesBySubject() {
        val result = StudyPlanAnalytics.summarize(listOf(
            item(1, "Matematik", "09:00", "10:30", true),
            item(2, "Matematik", "11:00", "12:00"),
            item(3, "Fizik", "13:00", "14:30", true)
        ))
        assertEquals(240, result.plannedMinutes)
        assertEquals(180, result.completedMinutes)
        assertEquals(2, result.completedSessions)
        assertEquals(3, result.totalSessions)
        assertEquals("Matematik", result.subjects.first().subject)
        assertEquals(150, result.subjects.first().plannedMinutes)
        assertEquals(90, result.subjects.first().completedMinutes)
    }

    @Test fun ignoresInvalidAndNonPositiveTimeRanges() {
        val result = StudyPlanAnalytics.summarize(listOf(
            item(1, "Matematik", "25:00", "26:00", true),
            item(2, "Fizik", "10:00", "10:00"),
            item(3, "Kimya", "bozuk", "11:00")
        ))
        assertEquals(0, result.plannedMinutes)
        assertEquals(0, result.completedMinutes)
        assertEquals(0, result.subjects.size)
        assertEquals(1, result.completedSessions)
        assertEquals(3, result.totalSessions)
    }

    @Test fun blankSubjectsAreGroupedAsOther() {
        val result = StudyPlanAnalytics.summarize(listOf(item(1, "", "09:00", "10:00")))
        assertEquals("Diğer", result.subjects.single().subject)
    }
}
