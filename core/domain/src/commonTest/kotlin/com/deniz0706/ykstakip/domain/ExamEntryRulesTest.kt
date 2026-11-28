package com.deniz0706.ykstakip.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ExamEntryRulesTest {
    private fun tytSubjects() = listOf(
        SubjectResult("Türkçe", 40, 35, 3, 40),
        SubjectResult("Sosyal Bilimler", 20, 15, 3, 20),
        SubjectResult("Matematik", 40, 30, 2, 60),
        SubjectResult("Fen Bilimleri", 20, 16, 2, 30)
    )

    @Test
    fun acceptsValidTytAndCalculatesBlankAnswers() {
        val result = ExamEntryRules.create(1, "  Deneme  ", "2026-11-26", ExamType.TYT, subjects = tytSubjects())
        assertNotNull(result)
        assertEquals("Deneme", result.title)
        assertEquals(2, result.subjects.first().blank)
        assertEquals(93.5, result.totalNet)
    }

    @Test
    fun rejectsMissingDuplicateOrUnexpectedSubjects() {
        assertEquals(
            ExamValidationResult.IncompleteSubjects,
            ExamEntryRules.validate(ExamType.TYT, null, tytSubjects().dropLast(1))
        )
        assertEquals(
            ExamValidationResult.DuplicateSubject,
            ExamEntryRules.validate(ExamType.TYT, null, tytSubjects() + tytSubjects().first())
        )
    }

    @Test
    fun enforcesAytFieldAndExamTime() {
        assertEquals(
            ExamValidationResult.MissingAytField,
            ExamEntryRules.validate(ExamType.AYT, null, emptyList())
        )
        val tooLong = tytSubjects().mapIndexed { index, item ->
            if (index == 0) item.copy(timeMinutes = 166) else item.copy(timeMinutes = 0)
        }
        assertEquals(
            ExamValidationResult.TotalTimeExceeded(166, 165),
            ExamEntryRules.validate(ExamType.TYT, null, tooLong)
        )
    }

    @Test
    fun validatesQuestionCountsAndCombinedAnswers() {
        val wrongQuestionCount = tytSubjects().map {
            if (it.subject == "Türkçe") it.copy(questionCount = 39) else it
        }
        assertEquals(
            ExamValidationResult.SubjectQuestionCountMismatch("Türkçe", 40, 39),
            ExamEntryRules.validate(ExamType.TYT, null, wrongQuestionCount)
        )

        val tooManyAnswers = tytSubjects().map {
            if (it.subject == "Türkçe") it.copy(correct = 25, wrong = 20) else it
        }
        assertEquals(
            ExamValidationResult.TotalAnswersExceedsSubjectLimit("Türkçe", 40),
            ExamEntryRules.validate(ExamType.TYT, null, tooManyAnswers)
        )
    }

    @Test
    fun validatesNegativeTimeAndAytSubjectSet() {
        val negativeTime = tytSubjects().map {
            if (it.subject == "Türkçe") it.copy(timeMinutes = -1) else it
        }
        assertEquals(
            ExamValidationResult.NegativeTime,
            ExamEntryRules.validate(ExamType.TYT, null, negativeTime)
        )

        val aytSubjects = SubjectConfigs.AYT_SAYISAL.map {
            SubjectResult(it.name, it.questionCount, 0, 0, 0)
        }
        assertEquals(
            ExamValidationResult.Valid,
            ExamEntryRules.validate(ExamType.AYT, AytField.SAYISAL, aytSubjects)
        )
        assertEquals(
            ExamValidationResult.UnexpectedAytField,
            ExamEntryRules.validate(ExamType.TYT, AytField.SAYISAL, tytSubjects())
        )
    }

    @Test
    fun refusesInvalidMetadataAndAnswerCounts() {
        assertNull(ExamEntryRules.create(1, " ", "2026-11-26", ExamType.TYT, subjects = tytSubjects()))
        val invalid = tytSubjects().map { if (it.subject == "Türkçe") it.copy(correct = 41) else it }
        assertEquals(
            ExamValidationResult.AnswerCountExceedsSubjectLimit("Türkçe", 40),
            ExamEntryRules.validate(ExamType.TYT, null, invalid)
        )
    }
}
