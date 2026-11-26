package com.deniz0706.ykstakip.model

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExamJsonAdapterTest {
    @Test
    fun roundTripsExistingExamJsonShape() {
        val original = Exam(
            id = 42,
            title = "Kasım TYT",
            date = "2026-11-26",
            type = ExamType.TYT,
            subjects = listOf(SubjectResult("Türkçe", 40, 35, 3, 30, listOf("Paragraf"))),
            notes = "Süre yönetimi",
            publisher = "ÖSYM"
        )

        val json = original.toJson()
        assertEquals(42L, json.getLong("id"))
        assertEquals("TYT", json.getString("type"))
        assertEquals("Süre yönetimi", json.getString("notes"))
        assertEquals("Paragraf", json.getJSONArray("subjects").getJSONObject(0)
            .getJSONArray("weakTopics").getString(0))
        assertEquals(original, Exam.fromJson(json))
    }

    @Test
    fun keepsLegacyDefaultsForOptionalFields() {
        val legacySubject = JSONObject()
            .put("subject", "Türkçe")
            .put("questionCount", 40)
            .put("correct", 35)
            .put("wrong", 3)
        val legacyExam = JSONObject()
            .put("id", 7L)
            .put("title", "Eski kayıt")
            .put("date", "2025-06-21")
            .put("type", "TYT")
            .put("subjects", JSONArray().put(legacySubject))

        val restored = Exam.fromJson(legacyExam)
        assertNull(restored.aytField)
        assertEquals("", restored.notes)
        assertEquals("", restored.publisher)
        assertEquals(0, restored.subjects.single().timeMinutes)
        assertEquals(emptyList<String>(), restored.subjects.single().weakTopics)
    }
}
