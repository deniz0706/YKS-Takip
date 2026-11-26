package com.deniz0706.ykstakip.data

/** Pure arithmetic for converting timer milliseconds into whole journal minutes. */
internal object FocusSessionSaveMath {
    data class Breakdown(val loggedMinutes: Int, val remainingMs: Long)

    fun split(sessionMs: Long, carriedMs: Long): Breakdown {
        val safeSession = sessionMs.coerceAtLeast(0L)
        val safeCarry = carriedMs.coerceIn(0L, 59_999L)
        val combined = safeSession + safeCarry
        return Breakdown(
            loggedMinutes = (combined / 60_000L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
            remainingMs = combined % 60_000L
        )
    }

    fun fitsDailyLimit(existingMinutes: Int, additionalMinutes: Int): Boolean =
        existingMinutes in 0..1440 && additionalMinutes >= 0 &&
            additionalMinutes <= 1440 - existingMinutes
}
