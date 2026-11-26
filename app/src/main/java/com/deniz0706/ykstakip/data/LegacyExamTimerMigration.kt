package com.deniz0706.ykstakip.data

/** Pure decision logic for importing the pre-unification exam timer without losing either session. */
internal object LegacyExamTimerMigration {
    sealed class Decision {
        object AlreadyMigrated : Decision()
        object NoLegacySession : Decision()
        object WaitForFocusSession : Decision()
        data class Restore(
            val durationMinutes: Int,
            val elapsedMs: Long,
            val running: Boolean
        ) : Decision()
    }

    fun decide(
        alreadyMigrated: Boolean,
        oldElapsedMs: Long,
        oldStartedAtMs: Long,
        oldDurationMinutes: Int,
        nowMs: Long,
        focusElapsedMs: Long,
        focusRunning: Boolean
    ): Decision {
        if (alreadyMigrated) return Decision.AlreadyMigrated

        val pausedElapsed = oldElapsedMs.coerceAtLeast(0L)
        val liveElapsed = if (oldStartedAtMs > 0L) {
            (nowMs - oldStartedAtMs).coerceAtLeast(0L)
        } else 0L
        val elapsed = if (Long.MAX_VALUE - pausedElapsed < liveElapsed) {
            Long.MAX_VALUE
        } else pausedElapsed + liveElapsed
        val wasRunning = oldStartedAtMs > 0L

        if (elapsed == 0L && !wasRunning) return Decision.NoLegacySession
        if (focusElapsedMs > 0L || focusRunning) return Decision.WaitForFocusSession

        return Decision.Restore(
            durationMinutes = oldDurationMinutes.coerceIn(1, 1440),
            elapsedMs = elapsed,
            running = wasRunning
        )
    }
}
