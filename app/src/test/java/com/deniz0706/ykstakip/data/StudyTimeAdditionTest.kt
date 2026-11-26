package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.model.StudyDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StudyTimeAdditionTest {
    private val date = "2026-10-19"

    @Test fun manualTwoHoursAreAddedToExistingTimeAndQuestionsAreKept() {
        val old = StudyDay(date, questionCount = 310, studyMinutes = 155)
        assertEquals(StudyDay(date, 310, 275), StudyTimeAddition.add(old, date, 120))
    }

    @Test fun manualMinutesCreateAStudyDayWhenNoneExists() {
        assertEquals(StudyDay(date, 0, 45), StudyTimeAddition.add(null, date, 45))
    }

    @Test fun existingEstimatedMarkerIsNotSilentlyRewritten() {
        val old = StudyDay(date, 200, 420, estimated = true)
        assertEquals(StudyDay(date, 200, 450, estimated = true),
            StudyTimeAddition.add(old, date, 30))
    }

    @Test fun zeroNegativeAndTooLongEntriesAreRejected() {
        assertNull(StudyTimeAddition.add(null, date, 0))
        assertNull(StudyTimeAddition.add(null, date, -4))
        assertNull(StudyTimeAddition.add(null, date, 1441))
    }

    @Test fun exceedingDailyTimeLeavesExistingDayUnchanged() {
        val old = StudyDay(date, 25, 1400)
        assertNull(StudyTimeAddition.add(old, date, 60))
        assertEquals(1400, old.studyMinutes)
    }
}
