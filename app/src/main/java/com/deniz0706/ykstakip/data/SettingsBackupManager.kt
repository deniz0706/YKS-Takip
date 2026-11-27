package com.deniz0706.ykstakip.data

import android.content.Context
import com.deniz0706.ykstakip.domain.model.AytField
import com.deniz0706.ykstakip.domain.model.ExamType
import org.json.JSONObject

/** Persists and restores user settings (OBP, target nets, exam date, daily goal,
 * AI settings, learning field) so they survive backup/restore. */
object SettingsBackupManager {
    const val SCHEMA = "yks_takip_settings_backup"

    data class Parsed(
        val obp: Float?,
        val targetNetTyt: Float?,
        val targetNetAyt: Float?,
        val examDateMillis: Long?,
        val dailyGoal: Int?,
        val learningField: AytField?,
        val aiBaseUrl: String?,
        val aiModel: String?
    )

    /** Exports all mutable user settings to a JSON string. */
    fun exportJson(context: Context): String {
        return JSONObject().apply {
            put("schema", SCHEMA)
            put("version", 1)
            put("obp", AppSettings.getObp(context))
            put("target_net_tyt", AppSettings.getTargetNet(context, ExamType.TYT))
            put("target_net_ayt", AppSettings.getTargetNet(context, ExamType.AYT))
            put("exam_date_millis", AppSettings.getExamDateMillis(context))
            put("daily_goal", AppSettings.getDailyGoal(context))
            put("learning_field", AppSettings.getLearningField(context).name)
            put("ai_base_url", AppSettings.getAiBaseUrl(context))
            put("ai_model", AppSettings.getAiModel(context))
        }.toString(2)
    }

    /** Imports settings from JSON, applying each present field to SharedPreferences. */
    fun importJson(context: Context, raw: String): Parsed {
        val obj = JSONObject(raw)
        require(obj.optString("schema") == SCHEMA) { "Bu ayarlar yedeği değil." }
        require(obj.optInt("version") == 1) { "Bilinmeyen ayarlar yedek sürümü." }

        val obpRaw = obj.optString("obp", "")
        val obp = if (obpRaw.isEmpty()) null else obpRaw.toFloat()
        val targetNetTyt = nullableFloat(obj, "target_net_tyt")
        val targetNetAyt = nullableFloat(obj, "target_net_ayt")
        val examDateMillis = if (obj.isNull("exam_date_millis")) null else obj.optLong("exam_date_millis")
        val dailyGoal = if (obj.isNull("daily_goal")) null else obj.optInt("daily_goal")
        val learningFieldRaw = obj.optString("learning_field", "SAYISAL")
        val learningField = AytField.valueOf(learningFieldRaw.takeIf { it.isNotEmpty() } ?: "SAYISAL")
        val aiBaseUrlRaw = obj.optString("ai_base_url", "")
        val aiBaseUrl = aiBaseUrlRaw.takeIf { it.isNotEmpty() }
        val aiModelRaw = obj.optString("ai_model", "")
        val aiModel = aiModelRaw.takeIf { it.isNotEmpty() }

        AppSettings.setObp(context, obp)
        AppSettings.setTargetNet(context, ExamType.TYT, targetNetTyt)
        AppSettings.setTargetNet(context, ExamType.AYT, targetNetAyt)
        AppSettings.setExamDateMillis(context, examDateMillis)
        AppSettings.setDailyGoal(context, dailyGoal)
        AppSettings.setLearningField(context, learningField)
        aiBaseUrl?.let { AppSettings.setAiBaseUrl(context, it) }
        aiModel?.let { AppSettings.setAiModel(context, it) }

        return Parsed(obp, targetNetTyt, targetNetAyt, examDateMillis, dailyGoal,
            learningField, aiBaseUrl, aiModel)
    }

    private fun nullableFloat(obj: JSONObject, key: String): Float? {
        return if (obj.isNull(key) || obj.optString(key).isEmpty()) null
            else obj.optDouble(key).toFloat()
    }
}
