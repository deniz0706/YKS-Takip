package com.deniz0706.ykstakip.data

import android.content.Context
import android.content.SharedPreferences
import com.deniz0706.ykstakip.domain.model.Exam
import org.json.JSONArray

class ExamRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("yks_takip_prefs", Context.MODE_PRIVATE)
    private val listeners = mutableListOf<() -> Unit>()
    @Volatile private var cachedExams: List<Exam>? = null
    private val writeGuard = LegacyDataWriteGuard("Sınav")

    fun addChangeListener(listener: () -> Unit) { listeners.add(listener) }
    fun removeChangeListener(listener: () -> Unit) { listeners.remove(listener) }
    private fun notifyChanged() { listeners.forEach { it() } }

    fun getAllExams(): List<Exam> {
        cachedExams?.let { return it }
        return synchronized(this) {
            cachedExams?.let { return@synchronized it }
            val raw = prefs.getString(KEY_EXAMS, null)
            val loaded = if (raw.isNullOrBlank()) emptyList() else try {
                val array = JSONArray(raw)
                (0 until array.length()).map { Exam.fromJson(array.getJSONObject(it)) }.sortedBy { it.date }
            } catch (_: Exception) {
                writeGuard.markUnreadable()
                emptyList()
            }
            cachedExams = loaded
            loaded
        }
    }

    @Synchronized
    fun saveExam(exam: Exam) {
        val current = getAllExams().toMutableList()
        val idx = current.indexOfFirst { it.id == exam.id }
        if (idx >= 0) current[idx] = exam else current.add(exam)
        persist(current)
    }

    @Synchronized
    fun deleteExam(id: Long) { persist(getAllExams().filterNot { it.id == id }) }
    @Synchronized
    fun replaceAll(exams: List<Exam>) { persist(exams) }

    fun hasUnreadableLegacyData(): Boolean = writeGuard.isBlocked

    @Synchronized
    private fun persist(exams: List<Exam>) {
        if (cachedExams == null) getAllExams()
        writeGuard.requireWritable()
        val sorted = exams.sortedBy { it.date }
        cachedExams = sorted
        val array = JSONArray(sorted.map { it.toJson() })
        prefs.edit().putString(KEY_EXAMS, array.toString()).apply()
        notifyChanged()
    }

    fun nextId(): Long = System.currentTimeMillis()

    companion object {
        private const val KEY_EXAMS = "exams_json"
        @Volatile private var instance: ExamRepository? = null
        fun getInstance(context: Context): ExamRepository =
            instance ?: synchronized(this) {
                instance ?: ExamRepository(context.applicationContext).also { instance = it }
            }
    }
}
