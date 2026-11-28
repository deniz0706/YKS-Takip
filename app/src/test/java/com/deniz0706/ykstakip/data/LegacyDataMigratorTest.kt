package com.deniz0706.ykstakip.data

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class LegacyDataMigratorTest {
    private val context: Context
        get() = RuntimeEnvironment.getApplication()

    private val appPrefs: android.content.SharedPreferences
        get() = context.getSharedPreferences("yks_takip_prefs", Context.MODE_PRIVATE)

    @Before
    fun clearLegacyStores() {
        appPrefs.edit().clear().commit()
        context.getSharedPreferences("yks_takip_study", Context.MODE_PRIVATE)
            .edit().clear().commit()
        context.getSharedPreferences("yks_takip_study_plan", Context.MODE_PRIVATE)
            .edit().clear().commit()
        context.getSharedPreferences("yks_learning_journal", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test
    fun malformedExamJsonDoesNotMarkMigrationCompleteOrOverwriteSource() {
        val original = "[{broken exam json]"
        appPrefs.edit().putString("exams_json", original).commit()

        assertThrows(IllegalStateException::class.java) {
            LegacyDataMigrator.migrateIfNeeded(context)
        }

        assertFalse(appPrefs.getBoolean("migrated_to_sqldelight_persistent_v2", false))
        assertEquals(original, appPrefs.getString("exams_json", null))
    }

    @Test
    fun malformedStudyJsonDoesNotMarkMigrationCompleteOrOverwriteSource() {
        val original = """[{"date":"2026-11-28","questionCount":"broken","studyMinutes":30}]"""
        context.getSharedPreferences("yks_takip_study", Context.MODE_PRIVATE)
            .edit().putString("study_days", original).commit()

        assertThrows(IllegalStateException::class.java) {
            LegacyDataMigrator.migrateIfNeeded(context)
        }

        assertFalse(appPrefs.getBoolean("migrated_to_sqldelight_persistent_v2", false))
        assertEquals(
            original,
            context.getSharedPreferences("yks_takip_study", Context.MODE_PRIVATE)
                .getString("study_days", null)
        )
    }
}
