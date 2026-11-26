package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.domain.FocusTimerMath as SharedFocusTimerMath

/** Android compatibility adapter; elapsed-time rules live in commonMain. */
internal object FocusTimerMath {
    fun elapsed(
        accumulatedMs: Long,
        startedAtMs: Long,
        nowMs: Long,
        stopwatch: Boolean,
        targetMinutes: Int
    ): Long = SharedFocusTimerMath.elapsed(
        accumulatedMs = accumulatedMs,
        startedAtMs = startedAtMs,
        nowMs = nowMs,
        stopwatch = stopwatch,
        targetMinutes = targetMinutes
    )
}
