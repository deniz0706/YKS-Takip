package com.deniz0706.ykstakip.data

import android.content.Context

object AppSettings {
    private const val PREFS = "yks_takip_prefs"
    private const val KEY_TARGET_TYT = "target_net_tyt"
    private const val KEY_TARGET_AYT = "target_net_ayt"
    private const val KEY_EXAM_DATE = "yks_exam_date_millis" // yyyy-MM-dd formatında epoch millis
    private const val KEY_DAILY_GOAL = "daily_question_goal"
    private const val KEY_OBP = "obp"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getTargetNet(context: Context, type: com.deniz0706.ykstakip.model.ExamType): Float? {
        val key = if (type == com.deniz0706.ykstakip.model.ExamType.TYT) KEY_TARGET_TYT else KEY_TARGET_AYT
        val v = prefs(context).getFloat(key, -1f)
        return if (v < 0f) null else v
    }

    fun setTargetNet(context: Context, type: com.deniz0706.ykstakip.model.ExamType, value: Float?) {
        val key = if (type == com.deniz0706.ykstakip.model.ExamType.TYT) KEY_TARGET_TYT else KEY_TARGET_AYT
        prefs(context).edit().apply {
            if (value == null) remove(key) else putFloat(key, value)
        }.apply()
    }

    fun getExamDateMillis(context: Context): Long? {
        val v = prefs(context).getLong(KEY_EXAM_DATE, -1L)
        return if (v < 0L) null else v
    }

    fun setExamDateMillis(context: Context, millis: Long?) {
        prefs(context).edit().apply {
            if (millis == null) remove(KEY_EXAM_DATE) else putLong(KEY_EXAM_DATE, millis)
        }.apply()
    }

    fun getDailyGoal(context: Context): Int? {
        val v = prefs(context).getInt(KEY_DAILY_GOAL, -1)
        return if (v < 0) null else v
    }

    fun setDailyGoal(context: Context, value: Int?) {
        prefs(context).edit().apply {
            if (value == null) remove(KEY_DAILY_GOAL) else putInt(KEY_DAILY_GOAL, value)
        }.apply()
    }

    fun getObp(context: Context): Float? {
        val v = prefs(context).getFloat(KEY_OBP, -1f)
        return if (v < 0f) null else v
    }

    fun setObp(context: Context, value: Float?) {
        prefs(context).edit().apply {
            if (value == null) {
                remove(KEY_OBP)
            } else {
                putFloat(KEY_OBP, value)
            }
        }.apply()
    }
}
