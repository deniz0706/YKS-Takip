package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.model.StudyDay
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/** Study JSON is distinct from exam JSON. Imported estimated days keep their marker. */
object StudyBackupManager {
    const val SCHEMA = "yks_takip_study_backup"
    data class Parsed(val days: List<StudyDay>, val estimatedDays: Int)

    fun exportJson(days: List<StudyDay>): String {
        val rows = JSONArray()
        days.sortedBy { it.date }.forEach { day ->
            rows.put(JSONObject().apply {
                put("date", day.date)
                put("questionCount", day.questionCount)
                put("studyMinutes", day.studyMinutes)
                put("estimated", day.estimated)
            })
        }
        return JSONObject().apply {
            put("schema", SCHEMA)
            put("version", 1)
            put("estimated", days.any { it.estimated })
            put("days", rows)
        }.toString(2)
    }

    fun parseJson(raw: String, today: LocalDate = LocalDate.now()): Parsed {
        val source = raw.trim()
        val envelope = if (source.startsWith("[")) null else JSONObject(source)
        if (envelope != null) {
            require(envelope.optString("schema") == SCHEMA) {
                "Bu çalışma yedeği değil; deneme JSON'u olabilir."
            }
            require(envelope.optInt("version") == 1) { "Bilinmeyen yedek sürümü." }
        }
        val rows = envelope?.getJSONArray("days") ?: JSONArray(source)
        require(rows.length() in 1..5000) { "Dosyada geçerli sayıda gün yok." }
        val defaultEstimate = envelope?.optBoolean("estimated", false) ?: false
        val seen = hashSetOf<String>()
        val days = (0 until rows.length()).map { i ->
            val entry = rows.getJSONObject(i)
            val date = entry.getString("date")
            require(date.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) { "Tarih biçimi yanlış: " + date }
            val actualDate = LocalDate.parse(date)
            require(!actualDate.isAfter(today)) { "Gelecek gün kaydı: " + date }
            require(seen.add(date)) { "Aynı gün tekrarlanmış: " + date }
            val q = entry.getInt("questionCount")
            val min = entry.getInt("studyMinutes")
            require(q in 0..100000 && min in 0..1440) { "Geçersiz sayı/süre: " + date }
            StudyDay(date, q, min, entry.optBoolean("estimated", defaultEstimate))
        }.sortedBy { it.date }
        return Parsed(days, days.count { it.estimated })
    }
}
