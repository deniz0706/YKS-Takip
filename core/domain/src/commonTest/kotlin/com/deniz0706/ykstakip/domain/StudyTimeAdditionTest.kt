package com.deniz0706.ykstakip.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertFailsWith

class StudyTimeAdditionTest {
    private val date = "2026-10-19"

    @Test
    fun addsMinutesWithoutResettingQuestionsOrEstimatedMarker() {
        assertEquals(
            StudyDay(date, 310, 275),
            StudyTimeAddition.add(StudyDay(date, 310, 155), date, 120)
        )
        assertEquals(
            StudyDay(date, 200, 450, estimated = true),
            StudyTimeAddition.add(StudyDay(date, 200, 420, estimated = true), date, 30)
        )
    }

    @Test
    fun createsDayWhenNoPreviousRecordExists() {
        assertEquals(StudyDay(date, 0, 45), StudyTimeAddition.add(null, date, 45))
    }

    @Test
    fun rejectsZeroNegativeAndOverLimitEntries() {
        assertNull(StudyTimeAddition.add(null, date, 0))
        assertNull(StudyTimeAddition.add(null, date, -4))
        assertNull(StudyTimeAddition.add(null, date, 1441))
    }

    @Test
    fun enforcesTwentyFourHourDailyLimitWithoutMutatingPreviousValue() {
        val old = StudyDay(date, 25, 1400)
        assertNull(StudyTimeAddition.add(old, date, 60))
        assertEquals(1400, old.studyMinutes)
    }

    @Test
    fun rejectsMismatchedDates() {
        assertFailsWith<IllegalArgumentException> {
            StudyTimeAddition.add(StudyDay("2026-10-18", 1, 1), date, 1)
        }
    }
}
