package com.deniz0706.ykstakip.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import com.deniz0706.ykstakip.util.ExamSessionMath

/**
 * Exam-mode adapter over the ONE FocusSessionRepository timer.
 * Exam metadata/splits remain separate, but elapsed/running state is shared
 * and every mode can save into the daily study journal.
 */
class ExamSessionRepository(context: Context) {
    private val app = context.applicationContext
    private val prefs = app.getSharedPreferences("yks_exam_session_v1", Context.MODE_PRIVATE)
    private val timer = FocusSessionRepository(app)

    data class Split(val label: String, val elapsedMs: Long)
    data class State(
        val examType: String,
        val durationMinutes: Int,
        val elapsedMs: Long,
        val running: Boolean,
        val splits: List<Split>
    ) {
        val remainingMs: Long get() = (durationMinutes * 60_000L - elapsedMs).coerceAtLeast(0L)
        val finished: Boolean get() = elapsedMs >= durationMinutes * 60_000L
    }

    init { migrateLegacyTimerOnce() }

    private fun migrateLegacyTimerOnce() {
        if (prefs.getBoolean("shared_timer_migrated", false)) return
        val focus = timer.snapshot()
        val oldElapsed = prefs.getLong("elapsed", 0L).coerceAtLeast(0L)
        val oldStarted = prefs.getLong("started_at", 0L)
        val oldDuration = prefs.getInt("duration", ExamSessionMath.TYT_MINUTES)
            .coerceIn(ExamSessionMath.MIN_DURATION_MINUTES, ExamSessionMath.MAX_DURATION_MINUTES)
        val elapsed = oldElapsed + if (oldStarted > 0L)
            (System.currentTimeMillis() - oldStarted).coerceAtLeast(0L) else 0L
        // Never overwrite an existing focus session with legacy exam data.
        if (focus.elapsedMs == 0L && !focus.running && elapsed > 0L) {
            timer.restoreSession(oldDuration, elapsed, oldStarted > 0L)
        }
        prefs.edit().putBoolean("shared_timer_migrated", true).commit()
    }

    private fun examType() = prefs.getString("exam_type", "TYT") ?: "TYT"
    private fun duration() = prefs.getInt("duration", if (examType() == "BRANŞ") 60 else ExamSessionMath.defaultDurationMinutes(examType()))
        .coerceIn(ExamSessionMath.MIN_DURATION_MINUTES, ExamSessionMath.MAX_DURATION_MINUTES)

    fun snapshot(): State {
        val shared = timer.snapshot()
        return State(examType(), duration(), shared.elapsedMs, shared.running, readSplits())
    }

    fun configure(type: String, customMinutes: Int? = null): State {
        require(type in setOf("TYT", "AYT", "BRANŞ"))
        val minutes = customMinutes ?: if (type == "BRANŞ") 60 else ExamSessionMath.defaultDurationMinutes(type)
        require(minutes in ExamSessionMath.MIN_DURATION_MINUTES..ExamSessionMath.MAX_DURATION_MINUTES)
        val current = snapshot()
        require(!current.running && current.elapsedMs == 0L) { "Önce açık oturumu sıfırla." }
        timer.chooseDuration(minutes)
        prefs.edit().putString("exam_type", type).putInt("duration", minutes)
            .putString("splits", "[]").apply()
        return snapshot()
    }

    fun start(): State { timer.start(); return snapshot() }
    fun pause(): State { timer.pause(); return snapshot() }
    fun reset(): State {
        timer.discard()
        prefs.edit().putString("splits", "[]").apply()
        return snapshot()
    }

    fun saveToToday(): FocusSessionRepository.SaveResult = timer.saveToToday()

    fun recordSplit(label: String): State {
        require(label.isNotBlank())
        val state = snapshot()
        val entries = state.splits.toMutableList()
        entries.add(Split(label.trim().take(40), state.elapsedMs))
        val array = JSONArray()
        entries.forEach { array.put(JSONObject().put("label", it.label).put("elapsedMs", it.elapsedMs)) }
        prefs.edit().putString("splits", array.toString()).apply()
        return snapshot()
    }

    private fun readSplits(): List<Split> = try {
        val array = JSONArray(prefs.getString("splits", "[]"))
        (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            Split(item.optString("label", "Ders"), item.optLong("elapsedMs", 0L).coerceAtLeast(0L))
        }
    } catch (_: Exception) { emptyList() }
}
