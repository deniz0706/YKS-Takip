package com.deniz0706.ykstakip.data

import android.content.Context
import com.deniz0706.ykstakip.model.StudyDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class LegacyRepositoryWriteProtectionTest {
    private val context: Context
        get() = RuntimeEnvironment.getApplication()

    @Before
    fun clearPreferences() {
        context.getSharedPreferences("yks_takip_prefs", Context.MODE_PRIVATE)
            .edit().clear().commit()
        context.getSharedPreferences("yks_takip_study", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test
    fun malformedExamJsonCannotBeReplacedByEmptyList() {
        val prefs = context.getSharedPreferences("yks_takip_prefs", Context.MODE_PRIVATE)
        val original = """[{"id":broken-and-valuable}]"""
        prefs.edit().putString("exams_json", original).commit()
        val repository = ExamRepository(context)

        assertTrue(repository.getAllExams().isEmpty())
        assertTrue(repository.hasUnreadableLegacyData())
        assertThrows(IllegalStateException::class.java) {
            repository.replaceAll(emptyList())
        }
        assertEquals(original, prefs.getString("exams_json", null))
    }

    @Test
    fun malformedStudyJsonBlocksDeleteAndPreservesOriginalValue() {
        val prefs = context.getSharedPreferences("yks_takip_study", Context.MODE_PRIVATE)
        val original = """[{"date":"not-a-date","questionCount":"broken"}]"""
        prefs.edit().putString("study_days", original).commit()
        val repository = StudyRepository(context)

        assertTrue(repository.getAll().isEmpty())
        assertTrue(repository.hasUnreadableLegacyData())
        assertThrows(IllegalStateException::class.java) {
            repository.delete("2026-11-27")
        }
        assertEquals(original, prefs.getString("study_days", null))
    }

    @Test
    fun malformedStudyJsonBlocksImportAndPreservesOriginalValue() {
        val prefs = context.getSharedPreferences("yks_takip_study", Context.MODE_PRIVATE)
        val original = """{this-is-not-valid-json"""
        prefs.edit().putString("study_days", original).commit()
        val repository = StudyRepository(context)

        assertThrows(IllegalStateException::class.java) {
            repository.importDays(listOf(StudyDay("2026-11-27", 50, 60)))
        }
        assertEquals(original, prefs.getString("study_days", null))
    }
}
