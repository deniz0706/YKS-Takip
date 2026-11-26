package com.deniz0706.ykstakip.util

/** Review intervals advance only after a successful review: 1, 3, 7, then 21 days. */
object SpacedRepetitionSchedule {
    private val intervals = listOf(1L, 3L, 7L, 21L)

    fun intervalDays(level: Int): Long = intervals[level.coerceIn(0, intervals.lastIndex)]

    fun nextLevel(level: Int): Int = (level + 1).coerceIn(0, intervals.lastIndex)
}
