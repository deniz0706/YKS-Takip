package com.deniz0706.ykstakip.util

/**
 * Pure timing rules for a real exam session.
 *
 * Stores elapsed time as monotonic accumulated milliseconds plus a wall-clock
 * start timestamp so the caller can persist sessions across recompositions.
 * This class deliberately does not own Android storage or notification APIs.
 */
object ExamSessionMath {
    const val TYT_MINUTES = 165
    const val AYT_MINUTES = 180
    const val MIN_DURATION_MINUTES = 1
    const val MAX_DURATION_MINUTES = 600

    data class Snapshot(
        val durationMinutes: Int,
        val elapsedMs: Long,
        val remainingMs: Long,
        val progress: Float,
        val finished: Boolean
    )

    fun defaultDurationMinutes(examType: String): Int = when (examType.uppercase()) {
        "TYT" -> TYT_MINUTES
        "AYT" -> AYT_MINUTES
        else -> TYT_MINUTES
    }

    fun elapsedMs(
        accumulatedMs: Long,
        startedAtEpochMs: Long?,
        nowEpochMs: Long
    ): Long {
        val accumulated = accumulatedMs.coerceAtLeast(0L)
        val live = startedAtEpochMs?.let { (nowEpochMs - it).coerceAtLeast(0L) } ?: 0L
        return accumulated + live
    }

    fun snapshot(durationMinutes: Int, elapsedMs: Long): Snapshot {
        require(durationMinutes in MIN_DURATION_MINUTES..MAX_DURATION_MINUTES) {
            "Sınav süresi 1–600 dakika arasında olmalı."
        }
        val total = durationMinutes * 60_000L
        val elapsed = elapsedMs.coerceAtLeast(0L)
        val remaining = (total - elapsed).coerceAtLeast(0L)
        return Snapshot(
            durationMinutes = durationMinutes,
            elapsedMs = elapsed,
            remainingMs = remaining,
            progress = (elapsed.toDouble() / total.toDouble()).toFloat().coerceIn(0f, 1f),
            finished = elapsed >= total
        )
    }

    /** Round remaining milliseconds up so 00:01 is not shown as 00:00 early. */
    fun displaySeconds(remainingMs: Long): Long =
        (remainingMs.coerceAtLeast(0L) + 999L) / 1_000L

    fun formatClock(seconds: Long): String {
        val safe = seconds.coerceAtLeast(0L)
        val hours = safe / 3_600L
        val minutes = (safe / 60L) % 60L
        val secs = safe % 60L
        return if (hours > 0L) "%02d:%02d:%02d".format(hours, minutes, secs)
        else "%02d:%02d".format(safe / 60L, secs)
    }

    /**
     * Returns a clamped split total. A split can never be negative or exceed
     * the elapsed exam time; callers can use this to validate per-course laps.
     */
    fun validSplitMs(splitMs: Long, elapsedMs: Long): Long =
        splitMs.coerceIn(0L, elapsedMs.coerceAtLeast(0L))
}
