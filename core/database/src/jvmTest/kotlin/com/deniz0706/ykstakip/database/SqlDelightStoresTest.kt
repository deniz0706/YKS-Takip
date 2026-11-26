package com.deniz0706.ykstakip.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.deniz0706.ykstakip.domain.AytField
import com.deniz0706.ykstakip.domain.Exam
import com.deniz0706.ykstakip.domain.ExamType
import com.deniz0706.ykstakip.domain.StudyDay
import com.deniz0706.ykstakip.domain.SubjectResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull\nimport kotlin.test.assertTrue

class SqlDelightStoresTest {
    private fun database(): Pair<JdbcSqliteDriver, YksDatabase> {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        YksDatabase.Schema.create(driver)
        return driver to YksDatabase(driver)
    }

    private fun exam(
        id: Long,
        title: String,
        subjects: List<SubjectResult>
    ) = Exam(
        id = id,
        title = title,
        date = "2026-11-26",
        type = ExamType.AYT,
        aytField = AytField.SAYISAL,
        subjects = subjects,
        notes = "Türkçe not",
        publisher = "ÖSYM"
    )

    @Test
    fun savingExamReplacesItsChildrenAsOneAggregate() {
        val (driver, database) = database()
        try {
            val store = SqlDelightExamStore(database)
            store.save(exam(7, "İlk kayıt", listOf(
                SubjectResult("Matematik", 40, 35, 2, 60, listOf("Problemler")),
                SubjectResult("Fizik", 14, 12, 1, 25)
            )))
            store.save(exam(7, "Güncel kayıt", listOf(
                SubjectResult("Kimya", 13, 11, 1, 20, listOf("Denge", "Kimyasal hız"))
            )))

            val actual = store.get(7)
            assertEquals("Güncel kayıt", actual?.title)
            assertEquals(1, actual?.subjects?.size)
            assertEquals("Kimya", actual?.subjects?.single()?.subject)
            assertEquals(listOf("Denge", "Kimyasal hız"), actual?.subjects?.single()?.weakTopics)
        } finally {
            driver.close()
        }
    }

    @Test
    fun duplicateIdsAreRejectedBeforeReplaceAllDeletesExistingRows() {
        val (driver, database) = database()
        try {
            val store = SqlDelightExamStore(database)
            val original = exam(1, "Korunacak kayıt", emptyList())
            store.save(original)

            assertFailsWith<IllegalArgumentException> {
                store.replaceAll(listOf(original, original.copy(title = "Çakışma")))
            }
            assertEquals("Korunacak kayıt", store.get(1)?.title)
        } finally {
            driver.close()
        }
    }

    @Test
    fun deleteExamAlsoDeletesItsSubjectRows() {
        val (driver, database) = database()
        try {
            val store = SqlDelightExamStore(database)
            store.save(exam(2, "Silinecek", listOf(SubjectResult("Türkçe", 40, 30, 5, 35))))
            store.delete(2)
            assertNull(store.get(2))
            assertEquals(0, store.getAll().size)
        } finally {
            driver.close()
        }
    }

    @Test
    fun studyDayStoreUpsertsAndPreservesEstimatedFlag() {
        val (driver, database) = database()
        try {
            val store = SqlDelightStudyDayStore(database)
            store.save(StudyDay("2026-11-26", 80, 120, estimated = true))
            store.save(StudyDay("2026-11-26", 95, 145, estimated = false))

            assertEquals(StudyDay("2026-11-26", 95, 145, estimated = false), store.get("2026-11-26"))
            assertEquals(1, store.getAll().size)
        } finally {
            driver.close()
        }
    }
}
