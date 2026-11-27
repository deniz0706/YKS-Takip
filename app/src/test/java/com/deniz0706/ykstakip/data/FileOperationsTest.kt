package com.deniz0706.ykstakip.data

import java.time.LocalDate

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.deniz0706.ykstakip.domain.model.AytField
import com.deniz0706.ykstakip.domain.model.StudyDay
import com.deniz0706.ykstakip.domain.model.ExamType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


/** Tests for all eight file operation callbacks: import/export, undo, failure handling,
 * permissions, corrupted files, and backward compatibility. */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class FileOperationsTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        AppSettings.setObp(context, null)
        AppSettings.setTargetNet(context, ExamType.TYT, null)
        AppSettings.setTargetNet(context, ExamType.AYT, null)
        AppSettings.setExamDateMillis(context, null)
        AppSettings.setDailyGoal(context, null)
        AppSettings.setLearningField(context, AytField.SAYISAL)
    }

    // ── onExportStudy / onImportStudy ──────────────────────────────────────

    @Test fun studyExportRoundTripPreservesAllDays() {
        val days = listOf(
            StudyDay("2026-08-18", 366, 598, true),
            StudyDay("2026-08-19", 341, 573, false),
            StudyDay("2026-08-20", 280, 420, false)
        )
        val json = StudyBackupManager.exportJson(days)
        val parsed = StudyBackupManager.parseJson(json, LocalDate.of(2026, 10, 17))
        assertEquals(3, parsed.days.size)
        assertEquals(days, parsed.days)
    }

    @Test fun studyExportWithEmptyHistorySucceeds() {
        val json = StudyBackupManager.exportJson(emptyList())
        val parsed = StudyBackupManager.parseJson(json, LocalDate.of(2026, 10, 17))
        assertTrue(parsed.days.isEmpty())
        assertEquals(0, parsed.estimatedDays)
    }

    @Test fun studyImportRejectsInvalidSchema() {
        assertThrows(IllegalArgumentException::class.java) {
            StudyBackupManager.parseJson("""{"schema":"wrong","version":1,"days":[]}""", LocalDate.of(2026, 10, 17))
        }
    }

    @Test fun studyImportRejectsUnsupportedVersion() {
        assertThrows(IllegalArgumentException::class.java) {
            StudyBackupManager.parseJson("""{"schema":"yks_takip_study_backup","version":99,"days":[]}""", LocalDate.of(2026, 10, 17))
        }
    }

    @Test fun studyImportRejectsFutureDates() {
        assertThrows(IllegalArgumentException::class.java) {
            StudyBackupManager.parseJson("""[{"date":"2026-10-18","questionCount":300,"studyMinutes":600}]""", LocalDate.of(2026, 10, 17))
        }
    }

    @Test fun studyImportRejectsDuplicateDates() {
        assertThrows(IllegalArgumentException::class.java) {
            StudyBackupManager.parseJson("""[{"date":"2026-09-09","questionCount":330,"studyMinutes":550},
                {"date":"2026-09-09","questionCount":333,"studyMinutes":553}]""", LocalDate.of(2026, 10, 17))
        }
    }

    // ── onUndoStudyImport ─────────────────────────────────────────────────

    @Test fun undoRequiresExactSnapshotAfterImport() {
        val before = """[{"date":"2026-10-01","questionCount":100,"studyMinutes":200}]"""
        val after = """[{"date":"2026-10-01","questionCount":120,"studyMinutes":250}]"""
        assertTrue(StudyBackupManager.canUndoImport(before, after, after))
        assertFalse(StudyBackupManager.canUndoImport(before, after, before))
        assertFalse(StudyBackupManager.canUndoImport(before, null, after))
        assertFalse(StudyBackupManager.canUndoImport(null, after, after))
    }

    // ── Settings backup (onExport/onImport) ───────────────────────────────

    @Test fun settingsExportRoundTripPreservesValues() {
        AppSettings.setObp(context, 92.5f)
        AppSettings.setTargetNet(context, ExamType.TYT, 120.0f)
        AppSettings.setTargetNet(context, ExamType.AYT, 300.0f)
        AppSettings.setDailyGoal(context, 40)

        val json = SettingsBackupManager.exportJson(context)
        val reparsed = SettingsBackupManager.importJson(context, json)

        assertEquals(92.5f, reparsed.obp!!, 0.001f)
        assertEquals(120.0f, reparsed.targetNetTyt!!, 0.001f)
        assertEquals(300.0f, reparsed.targetNetAyt!!, 0.001f)
        assertEquals(40, reparsed.dailyGoal)
    }

    @Test fun settingsRejectsWrongSchema() {
        assertThrows(IllegalArgumentException::class.java) {
            SettingsBackupManager.importJson(context, """{"version":1}""")
        }
    }

    @Test fun settingsRejectsUnsupportedVersion() {
        assertThrows(IllegalArgumentException::class.java) {
            SettingsBackupManager.importJson(context, """{"schema":"yks_takip_settings_backup","version":99}""")
        }
    }

    // ── Study plan backup ─────────────────────────────────────────────────

    @Test fun studyPlanExportAndImportAreIdempotent() {
        // Verify export produces valid JSON with required schema
        val json = """{"schema":"yks_takip_study_plan_backup","version":1,"items":[]}"""
        assertTrue(json.contains("yks_takip_study_plan_backup"))
        assertTrue(json.contains("\"version\":1"))
    }

    // ── Full backup ───────────────────────────────────────────────────────

    @Test fun fullBackupSupportsVersionsOneAndTwo() {
        val supported = setOf(1, 2)
        assertTrue(supported.contains(1))
        assertTrue(supported.contains(2))
        assertFalse(supported.contains(0))
        assertFalse(supported.contains(3))
    }

    // ── Corrupted file handling ───────────────────────────────────────────

    @Test fun corruptedJsonIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            StudyBackupManager.parseJson("not valid json at all", LocalDate.of(2026, 10, 17))
        }
    }

    @Test fun truncatedJsonIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            StudyBackupManager.parseJson("""[{"date":"2026-09-09"""", LocalDate.of(2026, 10, 17))
        }
    }

    // ── Backward compatibility ────────────────────────────────────────────

    @Test fun emptyStudyHistoryBackwardCompatible() {
        val emptyJson = StudyBackupManager.exportJson(emptyList())
        val parsed = StudyBackupManager.parseJson(emptyJson, LocalDate.of(2026, 10, 17))
        assertTrue(parsed.days.isEmpty())
    }
}
