package com.deniz0706.ykstakip.data

import org.junit.Assert.assertEquals
import org.junit.Test

class FocusTimerMathTest {
    @Test fun stopwatchDoesNotStopAtSixtyOrOneHundredTwentyMinutes() {
        val start = 1_000_000L
        val afterTwoHours = start + 120L * 60_000L
        assertEquals(120L * 60_000L,
            FocusTimerMath.elapsed(0, start, afterTwoHours, true, 25))
        assertEquals(125L * 60_000L,
            FocusTimerMath.elapsed(5L * 60_000L, start, afterTwoHours, true, 25))
    }

    @Test fun customCountdownCanRunForTwoHoursButStopsAtItsTarget() {
        val start = 1_000_000L
        assertEquals(95L * 60_000L,
            FocusTimerMath.elapsed(0, start, start + 95L * 60_000L, false, 120))
        assertEquals(120L * 60_000L,
            FocusTimerMath.elapsed(0, start, start + 150L * 60_000L, false, 120))
    }

    @Test fun oldCountdownStillStopsAtTwentyFiveMinutes() {
        val start = 1_000_000L
        assertEquals(25L * 60_000L,
            FocusTimerMath.elapsed(0, start, start + 120L * 60_000L, false, 25))
    }

    @Test fun pauseAndClockRollbackDoNotCreateStudyTime() {
        assertEquals(6000L, FocusTimerMath.elapsed(6000L, 0L, 5000L, true, 25))
        assertEquals(0L, FocusTimerMath.elapsed(0L, 5000L, 3000L, true, 25))
    }

    @Test fun stopwatchHasTwentyFourHourSafetyLimit() {
        val start = 1_000_000L
        assertEquals(1440L * 60_000L,
            FocusTimerMath.elapsed(0L, start, start + 48L * 60L * 60_000L, true, 25))
    }
}
