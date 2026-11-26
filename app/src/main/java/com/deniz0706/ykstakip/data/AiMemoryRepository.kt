package com.deniz0706.ykstakip.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.Normalizer
import java.util.Locale
import java.util.UUID

/**
 * Long-term, on-device memory for the AI coach.
 *
 * Conversation messages remain in AiCoachFragment's conversation store. This repository
 * contains only durable facts/preferences the user explicitly asks the coach to remember.
 */
class AiMemoryRepository(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    data class Memory(
        val id: String,
        val category: String,
        val content: String,
        val createdAt: Long,
        val updatedAt: Long,
        val source: String
    )

    fun all(): List<Memory> = readAll().sortedByDescending { it.updatedAt }

    fun remember(content: String, source: String = "user_explicit"): Memory? {
        val clean = content.trim().replace(Regex("\\s+"), " ")
        if (clean.length < 2) return null
        val now = System.currentTimeMillis()
        val current = readAll().toMutableList()
        val normalized = normalize(clean)
        val index = current.indexOfFirst { normalize(it.content) == normalized }
        val previous = current.getOrNull(index)
        val memory = Memory(
            id = previous?.id ?: UUID.randomUUID().toString(),
            category = categorize(clean),
            content = clean.take(MAX_CONTENT_LENGTH),
            createdAt = previous?.createdAt ?: now,
            updatedAt = now,
            source = source
        )
        if (index >= 0) current[index] = memory else current.add(memory)
        persist(current.sortedByDescending { it.updatedAt }.take(MAX_MEMORIES))
        return memory
    }

    /** Capture only explicit self-descriptions; ordinary chat is never mined for guesses. */
    fun captureFromUserMessage(message: String): List<Memory> {
        val patterns = listOf(
            Regex("""(?:benim\s+)?(?:adım|ismim)\s+([^,.!?\n]{2,60})""", RegexOption.IGNORE_CASE) to "Kullanıcının adı: ",
            Regex("""(?:öğretmenimin adı|hocamın adı|öğretmeninin adı)\s*[:：-]?\s*([^,.!?\n]{2,80})""", RegexOption.IGNORE_CASE) to "Öğretmeninin adı: ",
            Regex("""(?:kaynak olarak|kullandığım kaynak(?:lardan biri)?|çözdüğüm kaynak)\s+([^,.!?\n]{2,100})""", RegexOption.IGNORE_CASE) to "Kullandığı kaynak: ",
            Regex("""(?:tercihim|tercih ediyorum|bundan sonra)\s+([^.!?\n]{2,200})""", RegexOption.IGNORE_CASE) to "Çalışma tercihi: ",
            Regex("""(?:hedefim|amacım)\s+([^.!?\n]{2,160})""", RegexOption.IGNORE_CASE) to "Hedefi: "
        )
        val captured = mutableListOf<Memory>()
        patterns.forEach { (pattern, prefix) ->
            pattern.findAll(message).forEach matchLoop@ { match ->
                val detail = match.groupValues.getOrNull(1)?.trim()?.trimEnd(' ', '.') ?: return@matchLoop
                if (detail.isNotBlank()) remember(prefix + detail, source = "explicit_self_description")?.let(captured::add)
            }
        }
        return captured.distinctBy { it.id }
    }
    fun forgetMatching(query: String): Int {
        val needle = normalize(query)
        if (needle.isBlank()) return 0
        val current = readAll()
        val remaining = current.filterNot {
            normalize(it.content).contains(needle) || needle.contains(normalize(it.content))
        }
        val removed = current.size - remaining.size
        if (removed > 0) persist(remaining)
        return removed
    }

    /** A compact, stable snapshot for the model; most recently updated memories come first. */
    fun promptSnapshot(maxItems: Int = 24, maxChars: Int = 2400): String {
        val selected = all().take(maxItems)
        if (selected.isEmpty()) return ""
        val output = StringBuilder()
        selected.forEach { memory ->
            val line = "- [${memory.category}] ${memory.content}\n"
            if (output.length + line.length > maxChars) return@forEach
            output.append(line)
        }
        return output.toString().trim()
    }

    private fun readAll(): List<Memory> {
        val raw = prefs.getString(KEY_MEMORIES, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                val item = array.optJSONObject(index) ?: return@mapNotNull null
                val content = item.optString("content").trim()
                if (content.isBlank()) return@mapNotNull null
                Memory(
                    id = item.optString("id").ifBlank { UUID.randomUUID().toString() },
                    category = item.optString("category", "Not"),
                    content = content,
                    createdAt = item.optLong("createdAt", 0L),
                    updatedAt = item.optLong("updatedAt", 0L),
                    source = item.optString("source", "legacy")
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun persist(memories: List<Memory>) {
        val array = JSONArray()
        memories.forEach { memory ->
            array.put(JSONObject().apply {
                put("id", memory.id)
                put("category", memory.category)
                put("content", memory.content)
                put("createdAt", memory.createdAt)
                put("updatedAt", memory.updatedAt)
                put("source", memory.source)
            })
        }
        check(prefs.edit().putString(KEY_MEMORIES, array.toString()).commit()) {
            "AI hafızası kaydedilemedi."
        }
    }

    private fun categorize(value: String): String {
        val text = value.lowercase(Locale.forLanguageTag("tr-TR"))
        return when {
            listOf("adım ", "benim adım", "ismim", "adı ").any(text::contains) -> "İsim"
            listOf("hocam", "öğretmenim", "öğretmeni", "arkadaşım", "annem", "babam").any(text::contains) -> "Kişi"
            listOf("tercih", "seviyorum", "sevmiyorum", "istemiyorum", "bundan sonra", "şöyle anlat").any(text::contains) -> "Tercih"
            listOf("hedefim", "hedefim", "hedef olarak", "istiyorum", "amacım").any(text::contains) -> "Hedef"
            listOf("kaynak", "kitap", "deneme", "yayın").any(text::contains) -> "Kaynak"
            listOf("konusunda", "konuyu", "dersinde", "matematik", "fizik", "kimya", "biyoloji", "türkçe").any(text::contains) -> "Akademik"
            else -> "Not"
        }
    }

    private fun normalize(value: String): String =
        Normalizer.normalize(value.lowercase(Locale.forLanguageTag("tr-TR")), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
            .trim()

    companion object {
        private const val PREFS = "ai_coach_long_term_memory"
        private const val KEY_MEMORIES = "memories_v1"
        private const val MAX_MEMORIES = 100
        private const val MAX_CONTENT_LENGTH = 500
    }
}
