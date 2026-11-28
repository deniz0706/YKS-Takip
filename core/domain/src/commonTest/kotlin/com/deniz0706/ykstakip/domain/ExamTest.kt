package com.deniz0706.ykstakip.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExamTest {
    @Test
    fun aggregatesSubjectResultsAndTotals() {
        val exam = Exam(
            id = 1,
            title = "TYT Deneme",
            date = "2026-11-26",
            type = ExamType.TYT,
            subjects = listOf(
                SubjectResult("Türkçe", 40, 35, 3, 30),
                SubjectResult("Sosyal Bilimler", 20, 11, 5, 15),
                SubjectResult("Matematik", 40, 38, 1, 60),
                SubjectResult("Fen Bilimleri", 20, 19, 1, 25)
            )
        )
        assertEquals(103, exam.totalCorrect)
        assertEquals(10, exam.totalWrong)
        assertEquals(7, exam.totalBlank)
        assertEquals(120, exam.totalQuestions)
        assertEquals(130, exam.totalTimeMinutes)
        assertEquals(100.5, exam.totalNet)
        assertEquals(1.08, exam.overallMinutesPerQuestion)
        assertEquals(38, exam.subject("Matematik")?.correct)
        assertNull(exam.subject("Geometri"))
    }

    @Test
    fun emptyExamHasNoOverallPace() {
        val exam = Exam(2, "Boş", "2026-11-26", ExamType.AYT, subjects = emptyList())
        assertEquals(0.0, exam.totalNet)
        assertNull(exam.overallMinutesPerQuestion)
    }

    @Test
    fun subjectConfigurationKeepsOfficialTytAndAytQuestionCounts() {
        assertEquals(120, SubjectConfigs.TYT_SUBJECTS.sumOf { it.questionCount })
        assertEquals(80, SubjectConfigs.AYT_SAYISAL.sumOf { it.questionCount })
        assertEquals(80, SubjectConfigs.AYT_ESIT_AGIRLIK.sumOf { it.questionCount })
        assertEquals(80, SubjectConfigs.AYT_SOZEL.sumOf { it.questionCount })
        assertEquals(165, ExamType.TYT.totalTimeMinutes)
        assertEquals(180, ExamType.AYT.totalTimeMinutes)
    }
}
