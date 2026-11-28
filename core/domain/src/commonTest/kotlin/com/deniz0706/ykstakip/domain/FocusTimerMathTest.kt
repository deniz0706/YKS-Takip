package com.deniz0706.ykstakip.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class FocusTimerMathTest {
    @Test
    fun stopwatchContinuesPastTwoHoursAndAddsAccumulatedTime() {
        val start = 1_000_000L
        val afterTwoHours = start + 120L * 60_000L
        assertEquals(120L * 60_000L, FocusTimerMath.elapsed(0, start, afterTwoHours, true, 25))
        assertEquals(125L * 60_000L, FocusTimerMath.elapsed(5L * 60_000L, start, afterTwoHours, true, 25))
    }

    @Test
    fun countdownStopsAtItsConfiguredTarget() {
        val start = 1_000_000L
        assertEquals(95L * 60_000L, FocusTimerMath.elapsed(0, start, start + 95L * 60_000L, false, 120))
        assertEquals(120L * 60_000L, FocusTimerMath.elapsed(0, start, start + 150L * 60_000L, false, 120))
    }

    @Test
    fun legacyTwentyFiveMinuteCountdownKeepsItsLimit() {
        val start = 1_000_000L
        assertEquals(25L * 60_000L, FocusTimerMath.elapsed(0, start, start + 120L * 60_000L, false, 25))
    }

    @Test
    fun pauseAndClockRollbackDoNotCreateElapsedTime() {
        assertEquals(6000L, FocusTimerMath.elapsed(6000L, 0L, 5000L, true, 25))
        assertEquals(0L, FocusTimerMath.elapsed(0L, 5000L, 3000L, true, 25))
    }

    @Test
    fun stopwatchHasTwentyFourHourSafetyLimit() {
        val start = 1_000_000L
        assertEquals(1440L * 60_000L,
            FocusTimerMath.elapsed(0L, start, start + 48L * 60L * 60_000L, true, 25))
    }

    @Test
    fun countdownTargetIsClampedToOneThrough1440Minutes() {
        val start = 1_000_000L
        assertEquals(60_000L, FocusTimerMath.elapsed(0, start, start + 90_000L, false, 0))
        assertEquals(1440L * 60_000L,
            FocusTimerMath.elapsed(0, start, start + 48L * 60L * 60_000L, false, 2000))
    }
}
