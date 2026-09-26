package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.model.SubjectConfig

object ExamValidator {
    sealed class FieldResult {
        object Valid : FieldResult()
        data class Invalid(val message: String) : FieldResult()
    }

    fun validatePair(correct: Int, wrong: Int, config: SubjectConfig): FieldResult {
        if (correct < 0 || wrong < 0) return FieldResult.Invalid("Negatif değer girilemez.")
        if (correct > config.questionCount || wrong > config.questionCount)
            return FieldResult.Invalid("${config.name}: en fazla ${config.questionCount} olabilir.")
        if (correct + wrong > config.questionCount)
            return FieldResult.Invalid("${config.name}: doğru + yanlış toplamı ${config.questionCount} soruyu geçemez.")
        return FieldResult.Valid
    }

    fun validateWholeExam(
        type: ExamType,
        entries: List<Triple<SubjectConfig, Int, Int>>,
        timesBySubject: Map<String, Int>
    ): FieldResult {
        for ((config, correct, wrong) in entries) {
            val r = validatePair(correct, wrong, config)
            if (r is FieldResult.Invalid) return r
        }
        val totalTime = timesBySubject.values.sum()
        if (totalTime > type.totalTimeMinutes)
            return FieldResult.Invalid("Toplam süre ${type.totalTimeMinutes} dakikayı aşamaz (şu an $totalTime dk).")
        return FieldResult.Valid
    }
}
