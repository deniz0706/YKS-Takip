package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.model.StudyDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StudyBackupManagerTest {
    private val asOf = LocalDate.parse("2026-10-17")

    @Test fun estimatedDaysSurviveExportAndImport() {
        val source = listOf(StudyDay("2026-08-18", 366, 598, true),
            StudyDay("2026-08-19", 341, 573, false))
        val parsed = StudyBackupManager.parseJson(
            StudyBackupManager.exportJson(source), asOf)
        assertEquals(source, parsed.days)
        assertEquals(1, parsed.estimatedDays)
    }

    @Test fun providedSampleFormatIsAccepted() {
        val json = """{"schema":"yks_takip_study_backup","version":1,
            "estimated":true,"days":[{"date":"2026-09-04",
            "questionCount":345,"studyMinutes":600}]}"""
        val parsed = StudyBackupManager.parseJson(json, asOf)
        assertTrue(parsed.days.single().estimated)
        assertEquals(600, parsed.days.single().studyMinutes)
    }

    @Test fun duplicateDatesFailBeforeWritingAnything() {
        val json = """[{"date":"2026-09-09","questionCount":330,"studyMinutes":550},
            {"date":"2026-09-09","questionCount":333,"studyMinutes":553}]"""
        assertThrows(IllegalArgumentException::class.java) {
            StudyBackupManager.parseJson(json, asOf)
        }
    }

    @Test fun cannotImportFutureDaysOrInvalidMinutes() {
        assertThrows(IllegalArgumentException::class.java) {
            StudyBackupManager.parseJson("""[{"date":"2026-10-18",
                "questionCount":300,"studyMinutes":600}]""", asOf)
        }
        assertThrows(IllegalArgumentException::class.java) {
            StudyBackupManager.parseJson("""[{"date":"2026-09-10",
                "questionCount":300,"studyMinutes":1500}]""", asOf)
        }
    }

    @Test fun examJsonIsNotMistakenForStudyHistory() {
        assertThrows(IllegalArgumentException::class.java) {
            StudyBackupManager.parseJson(
                """{"schema":"exam_backup","version":1,"days":[]}""", asOf)
        }
    }
}
