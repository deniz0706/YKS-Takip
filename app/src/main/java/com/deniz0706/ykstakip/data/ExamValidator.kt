package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.domain.ExamValidation
import com.deniz0706.ykstakip.domain.ExamValidationResult
import com.deniz0706.ykstakip.domain.SubjectRule
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.model.SubjectConfig

/**
 * Android-facing adapter: domain rules live in commonMain; Turkish copy stays here.
 */
object ExamValidator {
    sealed class FieldResult {
        object Valid : FieldResult()
        data class Invalid(val message: String) : FieldResult()
    }

    fun validatePair(correct: Int, wrong: Int, config: SubjectConfig): FieldResult =
        when (ExamValidation.validateAnswers(
            correct = correct,
            wrong = wrong,
            subject = SubjectRule(config.name, config.questionCount)
        )) {
            ExamValidationResult.Valid -> FieldResult.Valid
            ExamValidationResult.NegativeAnswers ->
                FieldResult.Invalid("Negatif değer girilemez.")
            is ExamValidationResult.AnswerCountExceedsSubjectLimit ->
                FieldResult.Invalid("${config.name}: en fazla ${config.questionCount} olabilir.")
            is ExamValidationResult.TotalAnswersExceedsSubjectLimit ->
                FieldResult.Invalid("${config.name}: doğru + yanlış toplamı ${config.questionCount} soruyu geçemez.")
            else -> FieldResult.Invalid("Geçersiz deneme verisi.")
        }

    fun validateWholeExam(
        type: ExamType,
        entries: List<Triple<SubjectConfig, Int, Int>>,
        timesBySubject: Map<String, Int>
    ): FieldResult {
        for ((config, correct, wrong) in entries) {
            val result = validatePair(correct, wrong, config)
            if (result is FieldResult.Invalid) return result
        }
        val totalTime = timesBySubject.values.sum()
        return when (val result = ExamValidation.validateTotalTime(totalTime, type.totalTimeMinutes)) {
            ExamValidationResult.Valid -> FieldResult.Valid
            is ExamValidationResult.TotalTimeExceeded ->
                FieldResult.Invalid("Toplam süre ${result.limitMinutes} dakikayı aşamaz (şu an ${result.actualMinutes} dk).")
            else -> FieldResult.Invalid("Geçersiz deneme verisi.")
        }
    }
}
