package com.deniz0706.ykstakip.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import com.deniz0706.ykstakip.util.ExamSessionMath

/** Persistent TYT/AYT exam session. Kept separate from the daily focus timer. */
class ExamSessionRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("yks_exam_session_v1", Context.MODE_PRIVATE)

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

    private fun examType() = prefs.getString("exam_type", "TYT") ?: "TYT"
    private fun duration() = prefs.getInt("duration", ExamSessionMath.TYT_MINUTES)
        .coerceIn(ExamSessionMath.MIN_DURATION_MINUTES, ExamSessionMath.MAX_DURATION_MINUTES)
    private fun accumulated() = prefs.getLong("elapsed", 0L).coerceAtLeast(0L)
    private fun startedAt() = prefs.getLong("started_at", 0L)

    fun snapshot(now: Long = System.currentTimeMillis()): State {
        val start = startedAt()
        val elapsed = ExamSessionMath.elapsedMs(accumulated(), start.takeIf { it > 0L }, now)
        return State(examType(), duration(), elapsed, start > 0L && elapsed < duration() * 60_000L, readSplits())
    }

    fun configure(type: String, customMinutes: Int? = null): State {
        require(type in setOf("TYT", "AYT", "BRANŞ"))
        val minutes = customMinutes ?: ExamSessionMath.defaultDurationMinutes(type)
        require(minutes in ExamSessionMath.MIN_DURATION_MINUTES..ExamSessionMath.MAX_DURATION_MINUTES)
        val current = snapshot()
        require(!current.running && current.elapsedMs == 0L) { "Önce açık oturumu sıfırla." }
        prefs.edit().putString("exam_type", type).putInt("duration", minutes)
            .putLong("elapsed", 0L).remove("started_at").putString("splits", "[]").apply()
        return snapshot()
    }

    fun start(): State {
        val state = snapshot()
        if (!state.running && !state.finished) prefs.edit()
            .putLong("started_at", System.currentTimeMillis()).apply()
        return snapshot()
    }

    fun pause(): State {
        val state = snapshot()
        prefs.edit().putLong("elapsed", state.elapsedMs).remove("started_at").apply()
        return snapshot()
    }

    fun reset(): State {
        prefs.edit().putLong("elapsed", 0L).remove("started_at").putString("splits", "[]").apply()
        return snapshot()
    }

    fun recordSplit(label: String): State {
        require(label.isNotBlank())
        val state = pause()
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
