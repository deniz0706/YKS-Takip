package com.deniz0706.ykstakip.util

import org.junit.Assert.assertEquals
import org.junit.Test

class SpacedRepetitionScheduleTest {
    @Test fun followsOneThreeSevenTwentyOneDayIntervals() {
        assertEquals(1L, SpacedRepetitionSchedule.intervalDays(0))
        assertEquals(3L, SpacedRepetitionSchedule.intervalDays(1))
        assertEquals(7L, SpacedRepetitionSchedule.intervalDays(2))
        assertEquals(21L, SpacedRepetitionSchedule.intervalDays(3))
        assertEquals(21L, SpacedRepetitionSchedule.intervalDays(99))
    }

    @Test fun advancesAndCapsReviewLevel() {
        assertEquals(1, SpacedRepetitionSchedule.nextLevel(0))
        assertEquals(2, SpacedRepetitionSchedule.nextLevel(1))
        assertEquals(3, SpacedRepetitionSchedule.nextLevel(2))
        assertEquals(3, SpacedRepetitionSchedule.nextLevel(3))
    }
}
