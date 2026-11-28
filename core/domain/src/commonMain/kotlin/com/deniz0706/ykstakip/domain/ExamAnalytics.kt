package com.deniz0706.ykstakip.domain

/**
 * Deterministic exam-history analytics shared by Android and iOS.
 *
 * Exams are ordered by their ISO date string before calculating chronology. For equal dates,
 * the input order is retained by Kotlin's stable sorting.
 */
data class ExamTrendSummary(
    val attemptCount: Int,
    val latestNet: Double?,
    val previousNet: Double?,
    val change: Double?,
    val averageNet: Double?,
    val bestNet: Double?,
    val recentFiveAverage: Double?,
    val previousFiveAverage: Double?,
    val recentCount: Int
)

object ExamAnalytics {
    fun summarize(exams: List<Exam>): ExamTrendSummary {
        val nets = exams.sortedBy { it.date }.map { it.totalNet }
        val recent = nets.takeLast(5)
        val previousFive = nets.dropLast(5).takeLast(5)
        val latest = nets.lastOrNull()
        val previous = nets.getOrNull(nets.lastIndex - 1)

        return ExamTrendSummary(
            attemptCount = nets.size,
            latestNet = latest,
            previousNet = previous,
            change = if (latest != null && previous != null) latest - previous else null,
            averageNet = nets.takeIf { it.isNotEmpty() }?.average(),
            bestNet = nets.maxOrNull(),
            recentFiveAverage = recent.takeIf { it.isNotEmpty() }?.average(),
            previousFiveAverage = previousFive.takeIf { it.size == 5 }?.average(),
            recentCount = recent.size
        )
    }

    /** Returns chronological net values, optionally limited to the latest [limit] attempts. */
    fun trend(exams: List<Exam>, limit: Int? = null): List<Double> {
        require(limit == null || limit > 0) { "Trend limiti pozitif olmalı." }
        val ordered = exams.sortedBy { it.date }
        val selected = if (limit == null) ordered else ordered.takeLast(limit)
        return selected.map { it.totalNet }
    }
}
