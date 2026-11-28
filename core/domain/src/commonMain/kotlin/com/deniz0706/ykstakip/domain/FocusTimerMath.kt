package com.deniz0706.ykstakip.domain

/** Platform-independent elapsed-time arithmetic for countdowns and stopwatches. */
object FocusTimerMath {
    private const val MAX_MS = 1440L * 60_000L

    fun elapsed(
        accumulatedMs: Long,
        startedAtMs: Long,
        nowMs: Long,
        stopwatch: Boolean,
        targetMinutes: Int
    ): Long {
        val limit = if (stopwatch) MAX_MS
            else targetMinutes.coerceIn(1, 1440) * 60_000L
        val previous = accumulatedMs.coerceIn(0L, limit)
        val current = if (startedAtMs > 0 && nowMs > startedAtMs)
            (nowMs - startedAtMs).coerceAtMost(limit - previous)
        else 0L
        return previous + current
    }
}
