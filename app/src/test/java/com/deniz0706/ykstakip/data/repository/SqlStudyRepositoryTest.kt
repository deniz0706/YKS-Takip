package com.deniz0706.ykstakip.data.repository

import com.deniz0706.ykstakip.domain.model.StudyDay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SqlStudyRepositoryTest {

    private fun newRepo(): SqlStudyRepository {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val database = com.deniz0706.ykstakip.data.local.DatabaseHolder.get(context)
        database.studyDayQueriesQueries.replaceAllStudyDays()
        return SqlStudyRepository(context.applicationContext)
    }

    @Test
    fun save_and_retrieve_study_day() = runBlocking {
        val repo = newRepo()

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
    fun import_days_merges_without_overwrite() = runBlocking {
        val repo = newRepo()

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
    fun import_days_with_overwrite_replaces_existing() = runBlocking {
        val repo = newRepo()

        repo.save(StudyDay("2026-01-10", 10, 60, estimated = false))

        val incoming = listOf(StudyDay("2026-01-10", 99, 999, estimated = true))
        val changes = repo.importDays(incoming, overwrite = true)
        assert(changes == 1)

        val day10 = repo.get("2026-01-10")!!
        assert(day10.questionCount == 99)
    }
}
