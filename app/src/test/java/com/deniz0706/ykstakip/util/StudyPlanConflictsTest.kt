package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.domain.model.StudyPlanItem
import org.junit.Assert.assertEquals
import org.junit.Test

class StudyPlanConflictsTest {
    private fun item(id: Long, start: String, end: String, day: Int = 2) = StudyPlanItem(
        id = id, dayOfWeek = day, startTime = start, endTime = end,
        subject = "Matematik", topic = "", completed = false
    )

    @Test fun marksBothItemsWhenTimeRangesOverlap() {
        assertEquals(setOf(1L, 2L), StudyPlanConflicts.findConflictingIds(listOf(
            item(1, "09:00", "10:30"), item(2, "10:00", "11:00")
        )))
    }

    @Test fun touchingBoundariesAreNotConflicts() {
        assertEquals(emptySet<Long>(), StudyPlanConflicts.findConflictingIds(listOf(
            item(1, "09:00", "10:00"), item(2, "10:00", "11:00")
        )))
    }

    @Test fun differentDaysDoNotConflict() {
        assertEquals(emptySet<Long>(), StudyPlanConflicts.findConflictingIds(listOf(
            item(1, "09:00", "10:30", day = 2), item(2, "10:00", "11:00", day = 3)
        )))
    }

    @Test fun invalidAndZeroLengthRangesAreIgnored() {
        assertEquals(emptySet<Long>(), StudyPlanConflicts.findConflictingIds(listOf(
            item(1, "25:00", "26:00"), item(2, "10:00", "10:00"), item(3, "bad", "11:00")
        )))
    }

    @Test fun nonPaddedAndMalformedTimesAreIgnoredConsistently() {
        assertEquals(emptySet<Long>(), StudyPlanConflicts.findConflictingIds(listOf(
            item(1, "9:00", "10:00"), item(2, "09:00", "10:60"),
            item(3, "09:00 ", "10:00")
        )))
    }

    @Test fun overlappingItemsAreAllMarkedInAChain() {
        assertEquals(setOf(1L, 2L, 3L), StudyPlanConflicts.findConflictingIds(listOf(
            item(1, "09:00", "10:15"), item(2, "10:00", "11:15"), item(3, "11:00", "12:00")
        )))
    }
}
