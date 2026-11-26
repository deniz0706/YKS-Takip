package com.deniz0706.ykstakip.domain

data class SubjectRule(
    val name: String,
    val questionCount: Int
)

sealed interface ExamValidationResult {
    data object Valid : ExamValidationResult
    data object NegativeAnswers : ExamValidationResult
    data class AnswerCountExceedsSubjectLimit(val subject: String, val limit: Int) : ExamValidationResult
    data class TotalAnswersExceedsSubjectLimit(val subject: String, val limit: Int) : ExamValidationResult
    data class TotalTimeExceeded(val actualMinutes: Int, val limitMinutes: Int) : ExamValidationResult
}

/**
 * Pure YKS exam-entry rules shared by Android and iOS.
 * Presentation-specific messages deliberately remain in platform UI adapters.
 */
object ExamValidation {
    fun validateAnswers(
        correct: Int,
        wrong: Int,
        subject: SubjectRule
    ): ExamValidationResult {
        if (correct < 0 || wrong < 0) return ExamValidationResult.NegativeAnswers
        if (correct > subject.questionCount || wrong > subject.questionCount) {
            return ExamValidationResult.AnswerCountExceedsSubjectLimit(
                subject = subject.name,
                limit = subject.questionCount
            )
        }
        if (correct + wrong > subject.questionCount) {
            return ExamValidationResult.TotalAnswersExceedsSubjectLimit(
                subject = subject.name,
                limit = subject.questionCount
            )
        }
        return ExamValidationResult.Valid
    }

    fun validateTotalTime(actualMinutes: Int, limitMinutes: Int): ExamValidationResult =
        if (actualMinutes > limitMinutes) {
            ExamValidationResult.TotalTimeExceeded(actualMinutes, limitMinutes)
        } else {
            ExamValidationResult.Valid
        }
}
