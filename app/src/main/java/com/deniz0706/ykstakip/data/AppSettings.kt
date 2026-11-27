package com.deniz0706.ykstakip.data

import android.content.Context
import com.deniz0706.ykstakip.domain.model.AytField
import com.deniz0706.ykstakip.domain.model.ExamType
import com.deniz0706.ykstakip.widget.StudySummaryWidgetProvider

object AppSettings {
    private const val PREFS = "yks_takip_prefs"
    private const val KEY_TARGET_TYT = "target_net_tyt"
    private const val KEY_TARGET_AYT = "target_net_ayt"
    private const val KEY_EXAM_DATE = "yks_exam_date_millis" // yyyy-MM-dd formatında epoch millis
    private const val KEY_DAILY_GOAL = "daily_question_goal"
    private const val KEY_OBP = "obp"
    private const val KEY_LEARNING_FIELD = "learning_ayt_field"
    private const val KEY_AI_BASE_URL = "ai_base_url"
    private const val KEY_AI_MODEL = "ai_model"

    fun getLearningField(context: Context): AytField =
        runCatching {
            AytField.valueOf(prefs(context).getString(KEY_LEARNING_FIELD, "SAYISAL") ?: "SAYISAL")
        }.getOrDefault(AytField.SAYISAL)

    fun setLearningField(context: Context, field: AytField) {
        prefs(context).edit().putString(KEY_LEARNING_FIELD, field.name).apply()
    }

    fun getAiBaseUrl(context: Context): String = prefs(context).getString(KEY_AI_BASE_URL, "https://integrate.api.nvidia.com/v1") ?: "https://integrate.api.nvidia.com/v1"
    fun setAiBaseUrl(context: Context, value: String) { prefs(context).edit().putString(KEY_AI_BASE_URL, value.trim().trimEnd('/')).apply() }
    private const val DEFAULT_AI_MODEL = "mistralai/mistral-large-4-1t-a52b"

    fun getAiModel(context: Context): String {
        val saved = prefs(context).getString(KEY_AI_MODEL, null)?.trim()
        // Move previous app defaults to the new NVIDIA free-endpoint model.
        return when (saved) {
            null, "", "meta/llama-3.3-70b-instruct",
            "nvidia/llama-3.3-nemotron-super-49b-v1.5",
            "z-ai/glm-5.3-flash" -> DEFAULT_AI_MODEL
            else -> saved
        }
    }
    fun setAiModel(context: Context, value: String) { prefs(context).edit().putString(KEY_AI_MODEL, value.trim()).apply() }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getTargetNet(context: Context, type: ExamType): Float? {
        val key = if (type == ExamType.TYT) KEY_TARGET_TYT else KEY_TARGET_AYT
        val v = prefs(context).getFloat(key, -1f)
        return if (v < 0f) null else v
    }

    fun setTargetNet(context: Context, type: ExamType, value: Float?) {
        val key = if (type == ExamType.TYT) KEY_TARGET_TYT else KEY_TARGET_AYT
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
        StudySummaryWidgetProvider.updateAll(context)
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
