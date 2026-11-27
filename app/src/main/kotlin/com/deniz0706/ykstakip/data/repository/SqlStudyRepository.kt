package com.deniz0706.ykstakip.data.repository

import android.content.Context
import com.deniz0706.ykstakip.data.local.DatabaseHolder
import com.deniz0706.ykstakip.domain.model.StudyDay
import com.deniz0706.ykstakip.widget.StudySummaryWidgetProvider
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class SqlStudyRepository(private val context: Context) {
    private val database = DatabaseHolder.get(context)

    suspend fun getAll(): List<StudyDay> = withContext(Dispatchers.IO) {
        val queries = database.studyDayQueriesQueries
        queries.selectAllStudyDays().executeAsList().map { row ->
            StudyDay(
                date = row.date,
                questionCount = row.question_count.toInt(),
                studyMinutes = row.study_minutes.toInt(),
                estimated = row.estimated == 1L
            )
        }
    }

    suspend fun get(date: String): StudyDay? = withContext(Dispatchers.IO) {
        val queries = database.studyDayQueriesQueries
        queries.selectStudyDayByDate(date).executeAsOneOrNull()?.let { row ->
            StudyDay(
                date = row.date,
                questionCount = row.question_count.toInt(),
                studyMinutes = row.study_minutes.toInt(),
                estimated = row.estimated == 1L
            )
        }
    }

    suspend fun save(studyDay: StudyDay) = withContext(Dispatchers.IO) {
        val queries = database.studyDayQueriesQueries
        queries.insertStudyDay(studyDay.date, studyDay.questionCount.toLong(), studyDay.studyMinutes.toLong(), if (studyDay.estimated) 1L else 0L)
        StudySummaryWidgetProvider.updateAll(context)
    }

    suspend fun delete(date: String) = withContext(Dispatchers.IO) {
        val queries = database.studyDayQueriesQueries
        queries.deleteStudyDay(date)
        StudySummaryWidgetProvider.updateAll(context)
    }

    /** Atomic merge preserving existing days unless overwritten. */
    suspend fun importDays(incoming: List<StudyDay>, overwrite: Boolean = false): Int = withContext(Dispatchers.IO) {
        val current = getAll()
        val merged = current.associateBy { it.date }.toMutableMap()
        var changes = 0
        for (day in incoming) {
            if (merged[day.date] == null || (overwrite && merged[day.date] != day)) {
                merged[day.date] = day
                changes++
            }
        }
        if (changes == 0) return@withContext 0
        val queries = database.studyDayQueriesQueries
        queries.replaceAllStudyDays()
        merged.values.sortedBy { it.date }.forEach { day ->
            queries.insertStudyDay(day.date, day.questionCount.toLong(), day.studyMinutes.toLong(), if (day.estimated) 1L else 0L)
        }
        StudySummaryWidgetProvider.updateAll(context)
        changes
    }

    fun canUndoLastImport(): Boolean = false
    fun undoLastImport(): Boolean = false

    companion object {
        @Volatile private var instance: SqlStudyRepository? = null
        fun getInstance(context: Context): SqlStudyRepository =
            instance ?: synchronized(this) {
                instance ?: SqlStudyRepository(context.applicationContext).also { instance = it }
            }
    }
}
