package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.domain.model.StudyPlanItem

/** Finds every scheduled item that overlaps another item on the same day. */
object StudyPlanConflicts {
    private val TIME_PATTERN = Regex("""^(?:[01][0-9]|2[0-3]):[0-5][0-9]$""")

    fun findConflictingIds(items: List<StudyPlanItem>): Set<Long> {
        val ranges = items.mapNotNull { item ->
            val start = parseMinutes(item.startTime) ?: return@mapNotNull null
            val end = parseMinutes(item.endTime) ?: return@mapNotNull null
            if (end <= start) return@mapNotNull null
            Triple(item, start, end)
        }
        val conflicts = mutableSetOf<Long>()
        for (i in ranges.indices) {
            for (j in i + 1 until ranges.size) {
                val first = ranges[i]
                val second = ranges[j]
                if (first.first.dayOfWeek == second.first.dayOfWeek &&
                    first.first.id != second.first.id &&
                    maxOf(first.second, second.second) < minOf(first.third, second.third)
                ) {
                    conflicts += first.first.id
                    conflicts += second.first.id
                }
            }
        }
        return conflicts
    }

    private fun parseMinutes(value: String): Int? {
        if (!TIME_PATTERN.matches(value)) return null
        val hour = value.substring(0, 2).toInt()
        val minute = value.substring(3, 5).toInt()
        return hour * 60 + minute
    }
}
