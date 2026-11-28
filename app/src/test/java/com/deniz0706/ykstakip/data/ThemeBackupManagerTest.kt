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
class ThemeBackupManagerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("yks_takip_prefs", Context.MODE_PRIVATE).edit().clear().apply()
    }

    @Test fun exportsThemeSettings() {
        val prefs = context.getSharedPreferences("yks_takip_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("theme_mode", "aurora").putBoolean("theme_palette_v2", true).apply()

        val json = ThemeBackupManager.exportJson(context)
        val parsed = ThemeBackupManager.importJson(context, json)

        assertEquals("aurora", parsed.themeMode)
        assertEquals(true, parsed.paletteV2)
    }

    @Test fun importAppliesThemeValues() {
        val json = """{"schema":"yks_takip_theme_backup","version":1,"theme_mode":"dark","theme_palette_v2":true}"""
        ThemeBackupManager.importJson(context, json)

        val prefs = context.getSharedPreferences("yks_takip_prefs", Context.MODE_PRIVATE)
        assertEquals("dark", prefs.getString("theme_mode", null))
        assertEquals(true, prefs.getBoolean("theme_palette_v2", false))
    }

    @Test fun missingThemeModeIsAcceptedAsNull() {
        val json = """{"schema":"yks_takip_theme_backup","version":1,"theme_palette_v2":false}"""
        val parsed = ThemeBackupManager.importJson(context, json)
        assertNull(parsed.themeMode)
        assertEquals(false, parsed.paletteV2)
    }

    @Test fun missingSchemaIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            ThemeBackupManager.importJson(context, """{"version":1,"theme_mode":"light"}""")
        }
    }

    @Test fun unknownVersionIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            ThemeBackupManager.importJson(context,
                """{"schema":"yks_takip_theme_backup","version":99,"theme_mode":"light"}""")
        }
    }
}
