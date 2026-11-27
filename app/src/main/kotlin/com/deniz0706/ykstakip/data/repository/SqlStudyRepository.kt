package com.deniz0706.ykstakip.data.repository

import android.content.Context
import com.deniz0706.ykstakip.data.local.YksDatabase
import com.deniz0706.ykstakip.data.local.YksDatabase.Companion.get
import com.deniz0706.ykstakip.model.StudyDay
import com.deniz0706.ykstakip.widget.StudySummaryWidgetProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class SqlStudyRepository(private val context: Context) {
    private val db = get(context.applicationContext).database

    fun getAll(): List<StudyDay> = withContext(Dispatchers.IO) {
        val queries = com.deniz0706.ykstakip.data.local.StudyDayQueries(db)
        queries.selectAllStudyDays().executeAsList().map { row ->
            StudyDay(
                date = row.date,
                questionCount = row.questionCount,
                studyMinutes = row.studyMinutes,
                estimated = row.estimated == 1
            )
        }
    }

    fun get(date: String): StudyDay? = withContext(Dispatchers.IO) {
        val queries = com.deniz0706.ykstakip.data.local.StudyDayQueries(db)
        queries.selectStudyDayByDate(date).executeAsOneOrNull()?.let { row ->
            StudyDay(
                date = row.date,
                questionCount = row.questionCount,
                studyMinutes = row.studyMinutes,
                estimated = row.estimated == 1
            )
        }
    }

    fun save(studyDay: StudyDay) = withContext(Dispatchers.IO) {
        val queries = com.deniz0706.ykstakip.data.local.StudyDayQueries(db)
        queries.insertStudyDay(studyDay.date, studyDay.questionCount, studyDay.studyMinutes, if (studyDay.estimated) 1 else 0)
        StudySummaryWidgetProvider.updateAll(context)
    }

    fun delete(date: String) = withContext(Dispatchers.IO) {
        val queries = com.deniz0706.ykstakip.data.local.StudyDayQueries(db)
        queries.deleteStudyDay(date)
        StudySummaryWidgetProvider.updateAll(context)
    }

    /** Atomic merge preserving existing days unless overwritten. */
    fun importDays(incoming: List<StudyDay>, overwrite: Boolean = false): Int = withContext(Dispatchers.IO) {
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
        val queries = com.deniz0706.ykstakip.data.local.StudyDayQueries(db)
        queries.replaceAllStudyDays()
        merged.values.sortedBy { it.date }.forEach { day ->
            queries.insertStudyDay(day.date, day.questionCount, day.studyMinutes, if (day.estimated) 1 else 0)
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
