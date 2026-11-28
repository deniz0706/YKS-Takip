package com.deniz0706.ykstakip.util

import com.deniz0706.ykstakip.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class ExamComparisonAnalyzerTest {
    private fun exam(id: Long, type: ExamType, field: AytField? = null, maths: Int, turkish: Int = 30) = Exam(
        id = id, title = "Deneme $id", date = "2026-11-${if (id == 1L) "01" else "02"}",
        type = type, aytField = field,
        subjects = listOf(
            SubjectResult("Matematik", maths, 0, 0, 60),
            SubjectResult("Türkçe", turkish, 0, 0, 45)
        )
    )

    @Test fun identifiesStrongestGainAndDecline() {
        val result = ExamComparisonAnalyzer.analyze(
            exam(1, ExamType.TYT, maths = 20, turkish = 30),
            exam(2, ExamType.TYT, maths = 30, turkish = 25)
        )!!
        assertEquals("Matematik", result.bestImprovement?.subject)
        assertEquals(10.0, result.bestImprovement!!.delta, 0.001)
        assertEquals("Türkçe", result.biggestDecline?.subject)
        assertEquals(-5.0, result.biggestDecline!!.delta, 0.001)
    }

    @Test fun sameNetAcrossSubjectsHasNoImprovementOrDecline() {
        val result = ExamComparisonAnalyzer.analyze(
            exam(1, ExamType.TYT, maths = 20), exam(2, ExamType.TYT, maths = 20)
        )!!
        assertNull(result.bestImprovement)
        assertNull(result.biggestDecline)
    }

    @Test fun differentAytFieldsCannotBeCompared() {
        assertNull(ExamComparisonAnalyzer.analyze(
            exam(1, ExamType.AYT, AytField.SAYISAL, maths = 20),
            exam(2, ExamType.AYT, AytField.ESIT_AGIRLIK, maths = 25)
        ))
    }

    @Test fun differentExamTypesCannotBeCompared() {
        assertNull(ExamComparisonAnalyzer.analyze(
            exam(1, ExamType.TYT, maths = 20),
            exam(2, ExamType.AYT, AytField.SAYISAL, maths = 25)
        ))
    }
}
