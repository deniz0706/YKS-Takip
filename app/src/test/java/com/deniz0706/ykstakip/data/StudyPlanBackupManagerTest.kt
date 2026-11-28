package com.deniz0706.ykstakip.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.deniz0706.ykstakip.domain.model.StudyPlanItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class StudyPlanBackupManagerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        // Clear plan prefs before each test
        context.getSharedPreferences("yks_takip_study_plan", Context.MODE_PRIVATE).edit().clear().apply()
    }

    private fun saveItem(item: StudyPlanItem) {
        StudyPlanRepository(context).save(item)
    }

    @Test fun exportsAllPlanItems() {
        val currentWeek = java.time.LocalDate.now()
            .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).toString()
        saveItem(StudyPlanItem(id = 1, dayOfWeek = 1, startTime = "08:00", endTime = "10:00", subject = "Matematik", topic = "Fonksiyonlar", completed = true, completedWeek = currentWeek))
        saveItem(StudyPlanItem(id = 2, dayOfWeek = 2, startTime = "14:00", endTime = "16:00", subject = "Fizik", topic = "Mekanik"))

        val json = StudyPlanBackupManager.exportJson(context)
        val parsed = StudyPlanBackupManager.importJson(context, json)

        assertEquals(2, parsed.items.size)
        assertEquals(1L, parsed.items[0].id)
        assertEquals("Matematik", parsed.items[0].subject)
        assertTrue(parsed.items[0].completed)
        assertEquals(currentWeek, parsed.items[0].completedWeek)
        assertEquals(2L, parsed.items[1].id)
        assertEquals("Fizik", parsed.items[1].subject)
        assertFalse(parsed.items[1].completed)
        assertNull(parsed.items[1].completedWeek)
    }

    @Test fun mergeKeepsExistingItemsAddsNewOnes() {
        val currentWeek = java.time.LocalDate.now()
            .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).toString()
        // Existing item
        saveItem(StudyPlanItem(id = 5, dayOfWeek = 3, startTime = "09:00", endTime = "11:00", subject = "Kimya", topic = "Organik", completed = true, completedWeek = currentWeek))

        // Backup with one existing id (id=5) and one new id (id=6)
        val json = """{"schema":"yks_takip_study_plan_backup","version":1,"items":[
            {"id":5,"dayOfWeek":3,"startTime":"10:00","endTime":"12:00","subject":"Kimya","topic":"Anorganik","completed":false,"completedWeek":""},
            {"id":6,"dayOfWeek":4,"startTime":"08:00","endTime":"10:00","subject":"Biyoloji","topic":"Hücre","completed":true,"completedWeek":"$currentWeek"}
        ]}"""
        val parsed = StudyPlanBackupManager.importJson(context, json)

        // id=5 should keep its original values (merge keeps existing)
        val existing = StudyPlanRepository(context).getForDay(3).single { it.id == 5L }
        assertEquals("Organik", existing.topic)
        // id=6 should be added
        val added = StudyPlanRepository(context).getForDay(4).single { it.id == 6L }
        assertEquals("Biyoloji", added.subject)
        assertTrue(added.completed)
        assertEquals(2, parsed.items.size)
    }

    @Test fun missingSchemaIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            StudyPlanBackupManager.importJson(context, """{"version":1,"items":[]}""")
        }
    }

    @Test fun unknownVersionIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            StudyPlanBackupManager.importJson(context,
                """{"schema":"yks_takip_study_plan_backup","version":99,"items":[]}""")
        }
    }

    @Test fun emptyPlanExportsAndImportsCorrectly() {
        val json = StudyPlanBackupManager.exportJson(context)
        val parsed = StudyPlanBackupManager.importJson(context, json)
        assertTrue(parsed.items.isEmpty())
    }
}
