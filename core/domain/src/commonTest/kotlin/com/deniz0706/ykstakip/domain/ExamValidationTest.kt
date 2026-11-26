package com.deniz0706.ykstakip.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ExamValidationTest {
    private val tytTurkish = SubjectRule("Türkçe", 40)

    @Test
    fun acceptsAnswersWithinSubjectLimit() {
        assertEquals(
            ExamValidationResult.Valid,
            ExamValidation.validateAnswers(35, 3, tytTurkish)
        )
    }

    @Test
    fun rejectsNegativeAnswers() {
        assertEquals(
            ExamValidationResult.NegativeAnswers,
            ExamValidation.validateAnswers(-1, 0, tytTurkish)
        )
    }

    @Test
    fun rejectsAnIndividualAnswerCountOverLimit() {
        assertEquals(
            ExamValidationResult.AnswerCountExceedsSubjectLimit("Türkçe", 40),
            ExamValidation.validateAnswers(41, 0, tytTurkish)
        )
    }

    @Test
    fun rejectsCombinedAnswersOverLimit() {
        assertEquals(
            ExamValidationResult.TotalAnswersExceedsSubjectLimit("Türkçe", 40),
            ExamValidation.validateAnswers(30, 11, tytTurkish)
        )
    }

    @Test
    fun enforcesTytAndAytDurationWithoutChangingBoundary() {
        assertEquals(ExamValidationResult.Valid, ExamValidation.validateTotalTime(165, 165))
        assertEquals(
            ExamValidationResult.TotalTimeExceeded(166, 165),
            ExamValidation.validateTotalTime(166, 165)
        )
        assertEquals(ExamValidationResult.Valid, ExamValidation.validateTotalTime(180, 180))
    }
}
