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
        val running: Boolean,
        val stopwatch: Boolean = false
    ) {
        val totalMs: Long get() = durationMinutes * 60_000L
        val remainingMs: Long get() = (totalMs - elapsedMs).coerceAtLeast(0L)
        val completed: Boolean get() = !stopwatch && elapsedMs >= totalMs
        val limitReached: Boolean get() = stopwatch && elapsedMs >= MAX_STOPWATCH_MS
        val savedMinutes: Int get() = (elapsedMs / 60_000L).toInt()
    }

    companion object {
        const val MAX_MINUTES = 1440
        const val MAX_STOPWATCH_MS = MAX_MINUTES * 60_000L
    }

    private fun targetMinutes(): Int = prefs.getInt("duration", 25).coerceIn(1, MAX_MINUTES)
    private fun accumulated(): Long = prefs.getLong("accumulated", 0L).coerceAtLeast(0L)
    private fun startTime(): Long = prefs.getLong("started_at", 0L)

    /** Keep prior countdown sessions compatible; stopwatch has no chosen target. */
    fun snapshot(): Snapshot {
        val target = targetMinutes()
        val stopwatch = prefs.getBoolean("stopwatch_mode", false)
        val at = startTime()
        val running = at > 0L
        val limit = if (stopwatch) MAX_STOPWATCH_MS else target * 60_000L
        val elapsed = FocusTimerMath.elapsed(
            accumulated(), at, System.currentTimeMillis(), stopwatch, target
        )
        return Snapshot(target, elapsed, running && elapsed < limit, stopwatch)
    }

    fun chooseDuration(minutes: Int): Snapshot {
        require(minutes in 1..MAX_MINUTES)
        val state = snapshot()
        require(!state.running && state.elapsedMs == 0L) { "Önce mevcut seansı kaydet veya sıfırla." }
        prefs.edit().putBoolean("stopwatch_mode", false)
            .putInt("duration", minutes).putLong("accumulated", 0L)
            .remove("started_at").apply()
        return snapshot()
    }

    fun chooseStopwatch(): Snapshot {
        val state = snapshot()
        require(!state.running && state.elapsedMs == 0L) { "Önce mevcut seansı kaydet veya sıfırla." }
        prefs.edit().putBoolean("stopwatch_mode", true)
            .putLong("accumulated", 0L).remove("started_at").apply()
        return snapshot()
    }

    fun start(): Snapshot {
        val state = snapshot()
        if (!state.running && !state.completed && !state.limitReached) {
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

    fun addManualMinutes(minutes: Int): SaveResult {
        if (minutes <= 0) return SaveResult.NoTime
        if (minutes > MAX_MINUTES) return SaveResult.DailyLimit
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val previous = studyRepository.get(date)
        val merged = StudyTimeAddition.add(previous, date, minutes) ?: return SaveResult.DailyLimit
        studyRepository.save(merged)
        return SaveResult.Saved(minutes, 0)
    }

    sealed class SaveResult {
        data class Saved(val loggedMinutes: Int, val carriedSeconds: Int) : SaveResult()
        object NoTime : SaveResult()
        object DailyLimit : SaveResult()
    }

    /**
     * Paused sessions can be saved even before a full minute has elapsed.
     * The journal stores whole minutes, so remaining seconds are carried into
     * the next session saved on the SAME day rather than rounded away or inflated.
     * A successful save clears the session; failed saves leave its time intact.
     */
    fun saveToToday(): SaveResult {
        val session = pause()
        if (session.elapsedMs <= 0L) return SaveResult.NoTime
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val remainder = if (prefs.getString("carry_date", null) == date) {
            prefs.getLong("carry_ms", 0L).coerceIn(0L, 59_999L)
        } else 0L
        val combinedMs = session.elapsedMs + remainder
        val wholeMinutes = (combinedMs / 60_000L).toInt()
        val remainingMs = combinedMs % 60_000L
        val previous = studyRepository.get(date) ?: StudyDay(date, 0, 0)
        if (previous.studyMinutes + wholeMinutes > 1440) return SaveResult.DailyLimit
        if (wholeMinutes > 0) {
            studyRepository.save(previous.copy(
                studyMinutes = previous.studyMinutes + wholeMinutes
            ))
        }
        prefs.edit()
            .putString("carry_date", date)
            .putLong("carry_ms", remainingMs)
            .putLong("accumulated", 0L)
            .remove("started_at")
            .apply()
        return SaveResult.Saved(wholeMinutes, (remainingMs / 1000L).toInt())
    }
}
