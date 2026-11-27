package com.deniz0706.ykstakip.data.repository

import com.deniz0706.ykstakip.model.StudyDay
import org.junit.Test

class SqlStudyRepositoryTest {

    @Test
    fun save_and_retrieve_study_day() {
        val repo = SqlStudyRepository.getInstance(androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>())

        val day = StudyDay("2026-01-15", 25, 120, estimated = false)
        repo.save(day)

        val retrieved = repo.get("2026-01-15")
        assert(retrieved != null)
        assert(retrieved!!.questionCount == 25)
        assert(retrieved.studyMinutes == 120)
        assert(retrieved.estimated == false)

        val all = repo.getAll()
        assert(all.any { it.date == "2026-01-15" })
    }

    @Test
    fun import_days_merges_without_overwrite() {
        val repo = SqlStudyRepository.getInstance(androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>())

        repo.save(StudyDay("2026-01-10", 10, 60, estimated = false))

        val incoming = listOf(
            StudyDay("2026-01-10", 99, 999, estimated = true),
            StudyDay("2026-01-11", 20, 100, estimated = false)
        )
        val changes = repo.importDays(incoming, overwrite = false)
        // Existing day preserved, new day added = 1 change
        assert(changes == 1)

        val day10 = repo.get("2026-01-10")!!
        assert(day10.questionCount == 10) // not overwritten
        val day11 = repo.get("2026-01-11")!!
        assert(day11.questionCount == 20) // new day inserted
    }

    @Test
    fun import_days_with_overwrite_replaces_existing() {
        val repo = SqlStudyRepository.getInstance(androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>())

        repo.save(StudyDay("2026-01-10", 10, 60, estimated = false))

        val incoming = listOf(StudyDay("2026-01-10", 99, 999, estimated = true))
        val changes = repo.importDays(incoming, overwrite = true)
        assert(changes == 1)

        val day10 = repo.get("2026-01-10")!!
        assert(day10.questionCount == 99)
    }
}
