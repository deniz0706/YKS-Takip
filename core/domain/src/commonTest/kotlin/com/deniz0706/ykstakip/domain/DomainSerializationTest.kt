package com.deniz0706.ykstakip.domain

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class DomainSerializationTest {
    private val json = Json {
        classDiscriminator = "kind"
        ignoreUnknownKeys = true
    }

    @Test
    fun examRoundTripsWithTurkishTextAndNestedSubjects() {
        val original = Exam(
            id = 42L,
            title = "Kasım genel denemesi",
            date = "2026-11-26",
            type = ExamType.TYT,
            subjects = listOf(
                SubjectResult("Türkçe", 40, 39, 1, 32, listOf("Paragrafta anlam")),
                SubjectResult("Sosyal Bilimler", 20, 18, 2, 15)
            ),
            notes = "Süre yönetimi",
            publisher = "ÖSYM"
        )

        val encoded = json.encodeToString(original)
        val decoded = json.decodeFromString<Exam>(encoded)

        assertEquals(original, decoded)
        assertEquals(56.25, decoded.subjects.sumOf { it.net })
    }

    @Test
    fun optionalFieldsUseDefaultsWhenMissingInStoredJson() {
        val legacy = """{
            "id":7,
            "title":"Eski kayıt",
            "date":"2026-01-01",
            "type":"AYT",
            "subjects":[]
        }"""

        val decoded = json.decodeFromString<Exam>(legacy)

        assertEquals(null, decoded.aytField)
        assertEquals("", decoded.notes)
        assertEquals("", decoded.publisher)
    }

    @Test
    fun validationResultsCanBeSerializedPolymorphically() {
        val result: ExamValidationResult =
            ExamValidationResult.TotalTimeExceeded(actualMinutes = 190, limitMinutes = 180)

        val decoded = json.decodeFromString<ExamValidationResult>(
            json.encodeToString<ExamValidationResult>(result)
        )

        assertEquals(result, decoded)
    }

    @Test
    fun studyDayAndAytFieldAreSerializable() {
        val day = StudyDay("2026-11-26", 125, 240, estimated = true)
        assertEquals(day, json.decodeFromString<StudyDay>(json.encodeToString(day)))
        assertEquals(
            AytField.ESIT_AGIRLIK,
            json.decodeFromString<AytField>(json.encodeToString(AytField.ESIT_AGIRLIK))
        )
    }
}
