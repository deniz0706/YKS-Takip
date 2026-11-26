package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.model.ExamType

data class SubjectNetChange(val subject: String, val delta: Double)

data class ExamComparisonAnalysis(
    val netDelta: Double,
    val subjectChanges: List<SubjectNetChange>
) {
    val bestImprovement: SubjectNetChange?
        get() = subjectChanges.filter { it.delta > 0.0 }.maxByOrNull { it.delta }
    val biggestDecline: SubjectNetChange?
        get() = subjectChanges.filter { it.delta < 0.0 }.minByOrNull { it.delta }
}

object ExamComparisonAnalyzer {
    /** Returns null when the two exams do not represent the same exam type/field. */
    fun analyze(first: Exam, second: Exam): ExamComparisonAnalysis? {
        if (first.type != second.type) return null
        if (first.type == ExamType.AYT && first.aytField != second.aytField) return null
        val firstSubjects = first.subjects.associateBy { it.subject }
        val changes = second.subjects.mapNotNull { newer ->
            val older = firstSubjects[newer.subject] ?: return@mapNotNull null
            SubjectNetChange(newer.subject, newer.net - older.net)
        }
        return ExamComparisonAnalysis(second.totalNet - first.totalNet, changes)
    }
}
