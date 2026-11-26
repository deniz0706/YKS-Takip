package com.deniz0706.ykstakip.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusSessionSaveMathTest {
    @Test fun twoThirtySecondSessionsBecomeOneJournalMinute() {
        val first = FocusSessionSaveMath.split(30_000L, 0L)
        assertEquals(0, first.loggedMinutes)
        assertEquals(30_000L, first.remainingMs)

        val second = FocusSessionSaveMath.split(30_000L, first.remainingMs)
        assertEquals(1, second.loggedMinutes)
        assertEquals(0L, second.remainingMs)
    }

    @Test fun secondsAreNotRoundedUpOrLost() {
        val result = FocusSessionSaveMath.split(59_999L, 0L)
        assertEquals(0, result.loggedMinutes)
        assertEquals(59_999L, result.remainingMs)
    }

    @Test fun carryIsLimitedToOneMinuteMinusOneMillisecond() {
        val result = FocusSessionSaveMath.split(1_000L, 90_000L)
        assertEquals(1, result.loggedMinutes)
        assertEquals(0L, result.remainingMs)
    }

    @Test fun dailyLimitAllowsExactlyTwentyFourHours() {
        assertTrue(FocusSessionSaveMath.fitsDailyLimit(1_430, 10))
        assertFalse(FocusSessionSaveMath.fitsDailyLimit(1_430, 11))
        assertFalse(FocusSessionSaveMath.fitsDailyLimit(1_441, 0))
    }

    @Test fun zeroMinuteSessionCanStillPreserveSeconds() {
        val result = FocusSessionSaveMath.split(12_345L, 0L)
        assertEquals(0, result.loggedMinutes)
        assertEquals(12_345L, result.remainingMs)
    }
}
