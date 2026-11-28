package com.deniz0706.ykstakip.ui.home

internal object ClayHomeMath {
    const val STUDY_MINUTES_REFERENCE = 240
    const val EXAM_SPAN_DAYS = 365L

    fun fraction(done: Int, total: Int?): Float {
        if (total == null || total <= 0) return 0f
        return (done.coerceAtLeast(0).toFloat() / total.toFloat()).coerceIn(0f, 1f)
    }

    fun studyFraction(minutes: Int): Float =
        (minutes.coerceAtLeast(0).toFloat() / STUDY_MINUTES_REFERENCE).coerceIn(0f, 1f)

    fun countdownFraction(daysLeft: Long?): Float {
        if (daysLeft == null || daysLeft < 0L) return 0f
        return (1f - daysLeft.toFloat() / EXAM_SPAN_DAYS.toFloat()).coerceIn(0f, 1f)
    }

    fun formatStudyTime(minutes: Int): String {
        val safe = minutes.coerceAtLeast(0)
        val hours = safe / 60
        val rest = safe % 60
        return if (hours == 0) "${rest}dk" else "${hours}s ${rest}dk"
    }
}
