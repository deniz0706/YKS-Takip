package com.deniz0706.ykstakip.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExamSessionMathTest {
    @Test fun standardDurationsMatchExamLengths() {
        assertEquals(165, ExamSessionMath.defaultDurationMinutes("TYT"))
        assertEquals(180, ExamSessionMath.defaultDurationMinutes("AYT"))
        assertEquals(165, ExamSessionMath.defaultDurationMinutes("unknown"))
    }

    @Test fun elapsedTimeCombinesPausedAndRunningParts() {
        assertEquals(90_000L, ExamSessionMath.elapsedMs(30_000L, 1_000_000L, 1_060_000L))
        assertEquals(30_000L, ExamSessionMath.elapsedMs(30_000L, null, 1_060_000L))
        assertEquals(30_000L, ExamSessionMath.elapsedMs(30_000L, 1_060_000L, 1_000_000L))
    }

    @Test fun snapshotClampsAtZeroAndMarksFinished() {
        val halfway = ExamSessionMath.snapshot(60, 1_800_000L)
        assertEquals(1_800_000L, halfway.remainingMs)
        assertEquals(0.5f, halfway.progress, 0.0001f)
        assertFalse(halfway.finished)

        val finished = ExamSessionMath.snapshot(60, 3_700_000L)
        assertEquals(0L, finished.remainingMs)
        assertEquals(1f, finished.progress, 0f)
        assertTrue(finished.finished)
    }

    @Test fun clockFormattingRoundsRemainingTimeUp() {
        assertEquals( "02:45:00", ExamSessionMath.formatClock(
            ExamSessionMath.displaySeconds(9_900_001L)
        ))
        assertEquals("00:01", ExamSessionMath.formatClock(
            ExamSessionMath.displaySeconds(1L)
        ))
        assertEquals("00:00", ExamSessionMath.formatClock(0L))
    }

    @Test fun splitCannotExceedElapsedTime() {
        assertEquals(40L, ExamSessionMath.validSplitMs(40L, 100L))
        assertEquals(100L, ExamSessionMath.validSplitMs(500L, 100L))
        assertEquals(0L, ExamSessionMath.validSplitMs(-10L, 100L))
    }

    @Test(expected = IllegalArgumentException::class)
    fun durationMustBeValid() {
        ExamSessionMath.snapshot(0, 0L)
    }
}
