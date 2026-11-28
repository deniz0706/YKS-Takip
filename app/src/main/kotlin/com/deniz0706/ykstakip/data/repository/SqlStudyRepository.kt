package com.deniz0706.ykstakip.data.repository

import android.content.Context
import com.deniz0706.ykstakip.data.LegacyDataMigrator
import com.deniz0706.ykstakip.data.StudyBackupManager
import com.deniz0706.ykstakip.data.local.DatabaseHolder
import com.deniz0706.ykstakip.domain.model.StudyDay
import com.deniz0706.ykstakip.widget.StudySummaryWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SqlStudyRepository(private val context: Context) {
    private val database = DatabaseHolder.get(context)
    private val undoPrefs = context.getSharedPreferences("yks_takip_sql_study_import_undo", Context.MODE_PRIVATE)

    suspend fun getAll(): List<StudyDay> = withContext(Dispatchers.IO) {
        database.studyDayQueriesQueries.selectAllStudyDays().executeAsList().map { row ->
            StudyDay(
                date = row.date,
                questionCount = row.question_count.toInt(),
                studyMinutes = row.study_minutes.toInt(),
                estimated = row.estimated == 1L
            )
        }
    }

    suspend fun get(date: String): StudyDay? = withContext(Dispatchers.IO) {
        database.studyDayQueriesQueries.selectStudyDayByDate(date).executeAsOneOrNull()?.let { row ->
            StudyDay(
                date = row.date,
                questionCount = row.question_count.toInt(),
                studyMinutes = row.study_minutes.toInt(),
                estimated = row.estimated == 1L
            )
        }
    }

    suspend fun save(studyDay: StudyDay) = withContext(Dispatchers.IO) {
        LegacyDataMigrator.ensureMigrationReadyForWrites(context)
        database.studyDayQueriesQueries.insertStudyDay(
            studyDay.date,
            studyDay.questionCount.toLong(),
            studyDay.studyMinutes.toLong(),
            if (studyDay.estimated) 1L else 0L
        )
        clearUndoSnapshot()
        StudySummaryWidgetProvider.updateAll(context)
    }

    suspend fun delete(date: String) = withContext(Dispatchers.IO) {
        LegacyDataMigrator.ensureMigrationReadyForWrites(context)
        database.studyDayQueriesQueries.deleteStudyDay(date)
        clearUndoSnapshot()
        StudySummaryWidgetProvider.updateAll(context)
    }

    /** Merge imported days without replacing existing dates unless explicitly requested. */
    suspend fun importDays(incoming: List<StudyDay>, overwrite: Boolean = false): Int = withContext(Dispatchers.IO) {
        LegacyDataMigrator.ensureMigrationReadyForWrites(context)
        val current = getAll()
        val before = StudyBackupManager.exportJson(current)
        val merged = current.associateBy { it.date }.toMutableMap()
        var changes = 0
        incoming.forEach { day ->
            if (merged[day.date] == null || (overwrite && merged[day.date] != day)) {
                merged[day.date] = day
                changes++
            }
        }
        if (changes == 0) return@withContext 0

        val mergedDays = merged.values.toList()
        replaceAllInternal(mergedDays)
        val after = StudyBackupManager.exportJson(mergedDays)
        check(undoPrefs.edit()
            .putString(KEY_BEFORE_IMPORT, before)
            .putString(KEY_AFTER_IMPORT, after)
            .commit()) { "Çalışma aktarımının geri alma kaydı kaydedilemedi." }
        StudySummaryWidgetProvider.updateAll(context)
        changes
    }

    suspend fun replaceAll(days: List<StudyDay>) = withContext(Dispatchers.IO) {
        LegacyDataMigrator.ensureMigrationReadyForWrites(context)
        replaceAllInternal(days)
        clearUndoSnapshot()
        StudySummaryWidgetProvider.updateAll(context)
    }

    suspend fun canUndoLastImport(): Boolean = withContext(Dispatchers.IO) {
        val before = undoPrefs.getString(KEY_BEFORE_IMPORT, null)
        val after = undoPrefs.getString(KEY_AFTER_IMPORT, null)
        before != null && after != null && StudyBackupManager.exportJson(getAll()) == after
    }

    suspend fun undoLastImport(): Boolean = withContext(Dispatchers.IO) {
        if (!canUndoLastImport()) return@withContext false
        val before = undoPrefs.getString(KEY_BEFORE_IMPORT, null) ?: return@withContext false
        val parsed = StudyBackupManager.parseJson(before)
        replaceAllInternal(parsed.days)
        clearUndoSnapshot()
        StudySummaryWidgetProvider.updateAll(context)
        true
    }

    private fun replaceAllInternal(days: List<StudyDay>) {
        require(days.map { it.date }.distinct().size == days.size) {
            "Gün tarihleri benzersiz olmalı."
        }
        val queries = database.studyDayQueriesQueries
        database.transaction {
            queries.replaceAllStudyDays()
            days.sortedBy { it.date }.forEach { day ->
                queries.insertStudyDay(
                    day.date,
                    day.questionCount.toLong(),
                    day.studyMinutes.toLong(),
                    if (day.estimated) 1L else 0L
                )
            }
        }
    }

    private fun clearUndoSnapshot() {
        undoPrefs.edit().remove(KEY_BEFORE_IMPORT).remove(KEY_AFTER_IMPORT).apply()
    }

    companion object {
        private const val KEY_BEFORE_IMPORT = "before"
        private const val KEY_AFTER_IMPORT = "after"

        @Volatile private var instance: SqlStudyRepository? = null
        fun getInstance(context: Context): SqlStudyRepository =
            instance ?: synchronized(this) {
                instance ?: SqlStudyRepository(context.applicationContext).also { instance = it }
            }
    }
}
