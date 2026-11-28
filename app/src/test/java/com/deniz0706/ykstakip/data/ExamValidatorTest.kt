package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.domain.model.ExamType
import com.deniz0706.ykstakip.domain.model.SubjectConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class ExamValidatorTest {
    private val turkish = SubjectConfig("Türkçe", 40)

    @Test
    fun preservesTurkishNegativeAnswerMessage() {
        assertEquals(
            ExamValidator.FieldResult.Invalid("Negatif değer girilemez."),
            ExamValidator.validatePair(-1, 0, turkish)
        )
    }

    @Test
    fun preservesIndividualAndCombinedQuestionLimitMessages() {
        assertEquals(
            ExamValidator.FieldResult.Invalid("Türkçe: en fazla 40 olabilir."),
            ExamValidator.validatePair(41, 0, turkish)
        )
        assertEquals(
            ExamValidator.FieldResult.Invalid("Türkçe: doğru + yanlış toplamı 40 soruyu geçemez."),
            ExamValidator.validatePair(30, 11, turkish)
        )
    }

    @Test
    fun preservesTytDurationBoundaryAndMessage() {
        assertEquals(
            ExamValidator.FieldResult.Valid,
            ExamValidator.validateWholeExam(ExamType.TYT, emptyList(), mapOf("TYT" to 165))
        )
        assertEquals(
            ExamValidator.FieldResult.Invalid("Toplam süre 165 dakikayı aşamaz (şu an 166 dk)."),
            ExamValidator.validateWholeExam(ExamType.TYT, emptyList(), mapOf("TYT" to 166))
        )
    }

    @Test
    fun preservesAytDurationBoundary() {
        assertEquals(
            ExamValidator.FieldResult.Valid,
            ExamValidator.validateWholeExam(ExamType.AYT, emptyList(), mapOf("AYT" to 180))
        )
    }
}
