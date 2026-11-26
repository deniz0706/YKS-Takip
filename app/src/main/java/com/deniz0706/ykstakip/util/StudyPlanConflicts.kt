package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.model.StudyPlanItem

/** Finds every scheduled item that overlaps another item on the same day. */
object StudyPlanConflicts {
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
        val parts = value.split(":")
        if (parts.size != 2) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null
        return hour * 60 + minute
    }
}
