package com.deniz0706.ykstakip.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test

class ClayHomeMathTest {
    @Test fun studyTimeIsFormattedInHoursAndMinutes() {
        assertEquals("3s 20dk", ClayHomeMath.formatStudyTime(200))
        assertEquals("1s 0dk", ClayHomeMath.formatStudyTime(60))
        assertEquals("45dk", ClayHomeMath.formatStudyTime(45))
        assertEquals("0dk", ClayHomeMath.formatStudyTime(0))
    }

    @Test fun negativeStudyMinutesNeverShowNegativeTime() {
        assertEquals("0dk", ClayHomeMath.formatStudyTime(-30))
        assertEquals(0f, ClayHomeMath.studyFraction(-30), 0.0001f)
    }

    @Test fun questionFractionIsClampedAndHandlesMissingGoal() {
        assertEquals(0.7f, ClayHomeMath.fraction(140, 200), 0.0001f)
        assertEquals(1f, ClayHomeMath.fraction(500, 200), 0.0001f)
        assertEquals(0f, ClayHomeMath.fraction(10, null), 0.0001f)
        assertEquals(0f, ClayHomeMath.fraction(10, 0), 0.0001f)
        assertEquals(0f, ClayHomeMath.fraction(-5, 200), 0.0001f)
    }

    @Test fun studyFractionReachesOneAtReferenceMinutes() {
        assertEquals(1f, ClayHomeMath.studyFraction(ClayHomeMath.STUDY_MINUTES_REFERENCE), 0.0001f)
        assertEquals(1f, ClayHomeMath.studyFraction(600), 0.0001f)
        assertEquals(0.5f, ClayHomeMath.studyFraction(120), 0.0001f)
    }

    @Test fun countdownFractionGrowsAsTheExamApproaches() {
        assertEquals(0f, ClayHomeMath.countdownFraction(null), 0.0001f)
        assertEquals(0f, ClayHomeMath.countdownFraction(-3L), 0.0001f)
        assertEquals(0f, ClayHomeMath.countdownFraction(400L), 0.0001f)
        assertEquals(1f, ClayHomeMath.countdownFraction(0L), 0.0001f)
        assertEquals(0.4685f, ClayHomeMath.countdownFraction(194L), 0.001f)
    }
}
