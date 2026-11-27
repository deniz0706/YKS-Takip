package com.deniz0706.ykstakip.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class FocusSessionBackupManagerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("yks_focus_session", Context.MODE_PRIVATE).edit().clear().apply()
    }

    @Test fun exportsFocusSessionState() {
        val prefs = context.getSharedPreferences("yks_focus_session", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("duration", 50)
            .putLong("accumulated", 120000L)
            .putLong("started_at", 1000000L)
            .putBoolean("stopwatch_mode", true)
            .putString("carry_date", "2026-11-01")
            .putLong("carry_ms", 30000L)
            .apply()

        val json = FocusSessionBackupManager.exportJson(context)
        val parsed = FocusSessionBackupManager.importJson(context, json)

        assertEquals(50, parsed.durationMinutes)
        assertEquals(120000L, parsed.accumulatedMs)
        assertEquals(1000000L, parsed.startedAt)
        assertTrue(parsed.stopwatchMode!!)
        assertEquals("2026-11-01", parsed.carryDate)
        assertEquals(30000L, parsed.carryMs)
    }

    @Test fun missingStartAtIsAcceptedAsNull() {
        val json = """{"schema":"yks_takip_focus_session_backup","version":1,"duration":25,"accumulated":0}"""
        val parsed = FocusSessionBackupManager.importJson(context, json)
        assertNull(parsed.startedAt)
        assertNull(parsed.running)
    }

    @Test fun missingSchemaIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            FocusSessionBackupManager.importJson(context, """{"version":1,"duration":25}""")
        }
    }

    @Test fun unknownVersionIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            FocusSessionBackupManager.importJson(context,
                """{"schema":"yks_takip_focus_session_backup","version":99,"duration":25}""")
        }
    }
}
