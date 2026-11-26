package com.deniz0706.ykstakip.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class LegacyExamTimerMigrationTest {
    @Test fun alreadyMigratedSessionIsNeverImportedTwice() {
        val result = LegacyExamTimerMigration.decide(
            alreadyMigrated = true, oldElapsedMs = 90_000L, oldStartedAtMs = 0L,
            oldDurationMinutes = 165, nowMs = 100_000L, focusElapsedMs = 0L, focusRunning = false
        )
        assertSame(LegacyExamTimerMigration.Decision.AlreadyMigrated, result)
    }

    @Test fun emptyLegacyTimerCanBeMarkedMigrated() {
        val result = LegacyExamTimerMigration.decide(
            alreadyMigrated = false, oldElapsedMs = 0L, oldStartedAtMs = 0L,
            oldDurationMinutes = 165, nowMs = 100_000L, focusElapsedMs = 0L, focusRunning = false
        )
        assertSame(LegacyExamTimerMigration.Decision.NoLegacySession, result)
    }

    @Test fun legacyTimerWaitsWithoutOverwritingAnActiveFocusSession() {
        val result = LegacyExamTimerMigration.decide(
            alreadyMigrated = false, oldElapsedMs = 30_000L, oldStartedAtMs = 0L,
            oldDurationMinutes = 165, nowMs = 100_000L, focusElapsedMs = 60_000L, focusRunning = false
        )
        assertSame(LegacyExamTimerMigration.Decision.WaitForFocusSession, result)
    }

    @Test fun pausedLegacyElapsedTimeIsRestored() {
        val result = LegacyExamTimerMigration.decide(
            alreadyMigrated = false, oldElapsedMs = 72_000L, oldStartedAtMs = 0L,
            oldDurationMinutes = 165, nowMs = 100_000L, focusElapsedMs = 0L, focusRunning = false
        )
        assertEquals(LegacyExamTimerMigration.Decision.Restore(165, 72_000L, false), result)
    }

    @Test fun runningLegacyTimerIncludesTimeSinceItsLastStart() {
        val result = LegacyExamTimerMigration.decide(
            alreadyMigrated = false, oldElapsedMs = 30_000L, oldStartedAtMs = 90_000L,
            oldDurationMinutes = 165, nowMs = 150_000L, focusElapsedMs = 0L, focusRunning = false
        )
        assertEquals(LegacyExamTimerMigration.Decision.Restore(165, 90_000L, true), result)
    }

    @Test fun justStartedLegacyTimerIsNotMistakenForAnEmptySession() {
        val result = LegacyExamTimerMigration.decide(
            alreadyMigrated = false, oldElapsedMs = 0L, oldStartedAtMs = 100_000L,
            oldDurationMinutes = 165, nowMs = 100_000L, focusElapsedMs = 0L, focusRunning = false
        )
        assertEquals(LegacyExamTimerMigration.Decision.Restore(165, 0L, true), result)
    }

    @Test fun invalidLegacyDurationIsClampedSafely() {
        val result = LegacyExamTimerMigration.decide(
            alreadyMigrated = false, oldElapsedMs = 1_000L, oldStartedAtMs = 0L,
            oldDurationMinutes = 10_000, nowMs = 100_000L, focusElapsedMs = 0L, focusRunning = false
        )
        assertEquals(LegacyExamTimerMigration.Decision.Restore(1440, 1_000L, false), result)
    }
}
