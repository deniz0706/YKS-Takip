package com.deniz0706.ykstakip.data

import android.content.Context
import com.deniz0706.ykstakip.model.StudyDay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Persisted focus timer. Elapsed time survives navigation, process death and app restarts. */
class FocusSessionRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("yks_focus_session", Context.MODE_PRIVATE)
    private val studyRepository = StudyRepository(context.applicationContext)

    data class Snapshot(
        val durationMinutes: Int,
        val elapsedMs: Long,
        val running: Boolean
    ) {
        val totalMs: Long get() = durationMinutes * 60_000L
        val remainingMs: Long get() = (totalMs - elapsedMs).coerceAtLeast(0L)
        val completed: Boolean get() = elapsedMs >= totalMs
        val savedMinutes: Int get() = (elapsedMs / 60_000L).toInt()
    }

    private fun targetMinutes(): Int = prefs.getInt("duration", 25).coerceIn(1, 180)
    private fun accumulated(): Long = prefs.getLong("accumulated", 0L).coerceAtLeast(0L)
    private fun startTime(): Long = prefs.getLong("started_at", 0L)

    /** A clock change cannot produce negative study time or more than one session's target. */
    fun snapshot(): Snapshot {
        val target = targetMinutes()
        val at = startTime()
        val running = at > 0L
        val additional = if (running) (System.currentTimeMillis() - at).coerceAtLeast(0L) else 0L
        val elapsed = (accumulated() + additional).coerceIn(0L, target * 60_000L)
        return Snapshot(target, elapsed, running && elapsed < target * 60_000L)
    }

    fun chooseDuration(minutes: Int): Snapshot {
        require(minutes in listOf(25, 45, 60))
        prefs.edit().putInt("duration", minutes).putLong("accumulated", 0L)
            .remove("started_at").apply()
        return snapshot()
    }

    fun start(): Snapshot {
        val state = snapshot()
        if (!state.running && !state.completed) {
            prefs.edit().putLong("started_at", System.currentTimeMillis()).apply()
        }
        return snapshot()
    }

    fun pause(): Snapshot {
        val state = snapshot()
        prefs.edit().putLong("accumulated", state.elapsedMs)
            .remove("started_at").apply()
        return snapshot()
    }

    fun discard(): Snapshot {
        prefs.edit().putLong("accumulated", 0L).remove("started_at").apply()
        return snapshot()
    }

    /**
     * Append completed minutes to the current study day without overwriting its
     * question count. Discard only after writing succeeds, to prevent lost sessions.
     * The journal currently supports a maximum of 1440 recorded minutes per day.
     */
    fun saveToToday(): Int? {
        val state = pause()
        val minutes = state.savedMinutes
        if (minutes < 1) return null
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val previous = studyRepository.get(date) ?: StudyDay(date, 0, 0)
        if (previous.studyMinutes + minutes > 1440) return null
        studyRepository.save(previous.copy(studyMinutes = previous.studyMinutes + minutes))
        discard()
        return minutes
    }
}
