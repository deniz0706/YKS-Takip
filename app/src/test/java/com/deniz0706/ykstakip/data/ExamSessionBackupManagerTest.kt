package com.deniz0706.ykstakip.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ExamSessionBackupManagerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("yks_exam_session_v1", Context.MODE_PRIVATE).edit().clear().apply()
    }

    @Test fun exportsExamSessionState() {
        val prefs = context.getSharedPreferences("yks_exam_session_v1", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("exam_type", "AYT")
            .putInt("duration", 180)
            .putBoolean("shared_timer_migrated", true)
            .putString("splits", """[{"label":"Matematik","elapsedMs":3600000}]""")
            .apply()

        val json = ExamSessionBackupManager.exportJson(context)
        val parsed = ExamSessionBackupManager.importJson(context, json)

        assertEquals("AYT", parsed.examType)
        assertEquals(180, parsed.durationMinutes)
        assertEquals(1, parsed.splits.size)
        assertEquals("Matematik", parsed.splits[0].label)
        assertEquals(3600000L, parsed.splits[0].elapsedMs)
    }

    @Test fun missingExamTypeIsAcceptedAsNull() {
        val json = """{"schema":"yks_takip_exam_session_backup","version":1,"duration":120}"""
        val parsed = ExamSessionBackupManager.importJson(context, json)
        assertNull(parsed.examType)
        assertEquals(120, parsed.durationMinutes)
    }

    @Test fun missingSchemaIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            ExamSessionBackupManager.importJson(context, """{"version":1,"exam_type":"TYT"}""")
        }
    }

    @Test fun unknownVersionIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            ExamSessionBackupManager.importJson(context,
                """{"schema":"yks_takip_exam_session_backup","version":99,"exam_type":"TYT"}""")
        }
    }
}
