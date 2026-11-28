package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.domain.model.StudyPlanItem

data class StudyPlanSubjectLoad(val subject: String, val plannedMinutes: Int, val completedMinutes: Int, val sessions: Int)
data class StudyPlanSummary(
    val plannedMinutes: Int, val completedMinutes: Int,
    val completedSessions: Int, val totalSessions: Int,
    val subjects: List<StudyPlanSubjectLoad>
)

/** Time-based weekly plan insights. Invalid legacy times are ignored, never guessed. */
object StudyPlanAnalytics {
    fun summarize(items: List<StudyPlanItem>): StudyPlanSummary {
        fun duration(item: StudyPlanItem): Int? {
            val pattern = Regex("""^(?:[01][0-9]|2[0-3]):[0-5][0-9]$""")
            if (!pattern.matches(item.startTime) || !pattern.matches(item.endTime)) return null
            val start = item.startTime.substring(0, 2).toInt() * 60 + item.startTime.substring(3, 5).toInt()
            val end = item.endTime.substring(0, 2).toInt() * 60 + item.endTime.substring(3, 5).toInt()
            return (end - start).takeIf { it > 0 }
        }
        val valid = items.mapNotNull { item -> duration(item)?.let { item to it } }
        val subjects = valid.groupBy { it.first.subject.trim().ifBlank { "Diğer" } }
            .map { (subject, entries) ->
                StudyPlanSubjectLoad(subject, entries.sumOf { it.second },
                    entries.filter { it.first.completed }.sumOf { it.second }, entries.size)
            }.sortedWith(compareByDescending<StudyPlanSubjectLoad> { it.plannedMinutes }.thenBy { it.subject })
        return StudyPlanSummary(
            plannedMinutes = valid.sumOf { it.second },
            completedMinutes = valid.filter { it.first.completed }.sumOf { it.second },
            completedSessions = items.count { it.completed },
            totalSessions = items.size,
            subjects = subjects
        )
    }
}
