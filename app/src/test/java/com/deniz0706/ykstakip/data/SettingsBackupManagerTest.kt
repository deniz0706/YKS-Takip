package com.deniz0706.ykstakip.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.deniz0706.ykstakip.domain.model.AytField
import com.deniz0706.ykstakip.domain.model.ExamType
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
class SettingsBackupManagerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        // Reset settings to a known state before each test
        AppSettings.setObp(context, null)
        AppSettings.setTargetNet(context, ExamType.TYT, null)
        AppSettings.setTargetNet(context, ExamType.AYT, null)
        AppSettings.setExamDateMillis(context, null)
        AppSettings.setDailyGoal(context, null)
        AppSettings.setLearningField(context, AytField.SAYISAL)
        AppSettings.setAiBaseUrl(context, "https://integrate.api.nvidia.com/v1")
        AppSettings.setAiModel(context, "mistralai/mistral-large-4-1t-a52b")
    }

    @Test fun exportsAllSettings() {
        AppSettings.setObp(context, 92.5f)
        AppSettings.setTargetNet(context, ExamType.TYT, 120.0f)
        AppSettings.setTargetNet(context, ExamType.AYT, 300.0f)
        AppSettings.setExamDateMillis(context, 1780000000000L)
        AppSettings.setDailyGoal(context, 40)
        AppSettings.setLearningField(context, AytField.SOZEL)
        AppSettings.setAiBaseUrl(context, "https://custom.api/v1")
        AppSettings.setAiModel(context, "custom-model-v1")

        val json = SettingsBackupManager.exportJson(context)
        val parsed = SettingsBackupManager.Parsed(
            obp = 92.5f,
            targetNetTyt = 120.0f,
            targetNetAyt = 300.0f,
            examDateMillis = 1780000000000L,
            dailyGoal = 40,
            learningField = AytField.SOZEL,
            aiBaseUrl = "https://custom.api/v1",
            aiModel = "custom-model-v1"
        )

        val reparsed = SettingsBackupManager.importJson(context, json)
        assertEquals(parsed.obp, reparsed.obp)
        assertEquals(parsed.targetNetTyt, reparsed.targetNetTyt)
        assertEquals(parsed.targetNetAyt, reparsed.targetNetAyt)
        assertEquals(parsed.examDateMillis, reparsed.examDateMillis)
        assertEquals(parsed.dailyGoal, reparsed.dailyGoal)
        assertEquals(parsed.learningField, reparsed.learningField)
        assertEquals(parsed.aiBaseUrl, reparsed.aiBaseUrl)
        assertEquals(parsed.aiModel, reparsed.aiModel)
    }

    @Test fun exportAndImportRoundTripPreservesValues() {
        AppSettings.setObp(context, 88.0f)
        AppSettings.setDailyGoal(context, 30)
        AppSettings.setExamDateMillis(context, 1790000000000L)

        val json = SettingsBackupManager.exportJson(context)
        SettingsBackupManager.importJson(context, json)

        assertEquals(88.0f, AppSettings.getObp(context)!!, 0.001f)
        assertEquals(30, AppSettings.getDailyGoal(context))
        assertEquals(1790000000000L, AppSettings.getExamDateMillis(context))
    }

    @Test fun missingSchemaIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            SettingsBackupManager.importJson(context, """{"version":1}""")
        }
    }

    @Test fun unknownVersionIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            SettingsBackupManager.importJson(context, """{"schema":"yks_takip_settings_backup","version":99}""")
        }
    }

    @Test fun defaultValuesAreAbsentWhenNotSet() {
        val json = SettingsBackupManager.exportJson(context)
        val parsed = SettingsBackupManager.importJson(context, json)
        assertNull(parsed.obp)
        assertNull(parsed.targetNetTyt)
        assertNull(parsed.targetNetAyt)
        assertNull(parsed.examDateMillis)
        assertNull(parsed.dailyGoal)
        assertEquals(AytField.SAYISAL, parsed.learningField)
        assertFalse(parsed.aiBaseUrl?.startsWith("https://custom") ?: false)
    }
}
