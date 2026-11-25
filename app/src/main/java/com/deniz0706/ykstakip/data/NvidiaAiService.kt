package com.deniz0706.ykstakip.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Minimal OpenAI-compatible HTTP client; network work always runs off the main thread. */
class NvidiaAiService(private val context: Context) {
    data class Message(val role: String, val content: String)

    suspend fun testConnection(): String = chat(listOf(Message("user", "Reply with exactly: bağlantı başarılı")), 24)

    suspend fun chat(messages: List<Message>, maxTokens: Int = 700): String = withContext(Dispatchers.IO) {
        val key = AiSecretStore.readApiKey(context) ?: error("Önce Ayarlar bölümünden API anahtarını kaydet.")
        val base = AppSettings.getAiBaseUrl(context).trim().trimEnd("/")
        val model = AppSettings.getAiModel(context).trim()
        require(base.startsWith("https://")) { "Base URL HTTPS olmalı." }
        require(model.isNotBlank()) { "Model adı boş olamaz." }
        val payload = JSONObject().apply {
            put("model", model)
            put("temperature", 0.4)
            put("max_tokens", maxTokens.coerceIn(1, 2048))
            put("messages", JSONArray().apply { messages.forEach { put(JSONObject().put("role", it.role).put("content", it.content)) } })
        }
        val connection = (URL("$base/chat/completions").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"; connectTimeout = 15000; readTimeout = 45000; doOutput = true
            setRequestProperty("Authorization", "Bearer $key")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }
        try {
            connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                val detail = runCatching { JSONObject(response).optJSONObject("error")?.optString("message") }.getOrNull()
                error("AI servisi HTTP $status: " + (detail?.takeIf { it.isNotBlank() }?.take(220) ?: "İstek başarısız."))
            }
            JSONObject(response).optJSONArray("choices")?.optJSONObject(0)?.optJSONObject("message")?.optString("content")
                ?.takeIf { it.isNotBlank() } ?: error("AI yanıtı boş veya beklenmeyen biçimde.")
        } finally { connection.disconnect() }
    }
}