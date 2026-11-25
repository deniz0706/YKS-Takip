package com.deniz0706.ykstakip.data

import android.content.Context
import java.time.LocalDate

/** Stores only the latest generated advice; it remains readable offline. */
object AiCoachCache {
    private const val PREFS = "ai_coach_cache"
    private const val KEY_ADVICE = "advice"
    private const val KEY_DATE = "advice_date"
    private const val KEY_LAST_ATTEMPT = "last_attempt_date"

    fun getAdvice(context: Context): Pair<String, String>? {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val advice = prefs.getString(KEY_ADVICE, null)?.takeIf { it.isNotBlank() } ?: return null
        return advice to prefs.getString(KEY_DATE, "").orEmpty()
    }

    @Synchronized
    fun claimDailySlot(context: Context, examId: Long): Boolean {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = LocalDate.now().toString()
        if (prefs.getString(KEY_LAST_ATTEMPT, "") == today) return false
        return prefs.edit().putString(KEY_LAST_ATTEMPT, today)
            .putLong("last_exam_id", examId).commit()
    }

    fun save(context: Context, advice: String) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_ADVICE, advice.trim())
            .putString(KEY_DATE, LocalDate.now().toString())
            .apply()
    }
}
