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

    suspend fun testConnection(): String = chat(listOf(Message("user", "Reply briefly with exactly: bağlantı başarılı")), 1024)

    suspend fun chat(messages: List<Message>, maxTokens: Int = 2048): String = withContext(Dispatchers.IO) {
        val key = AiSecretStore.readApiKey(context) ?: error("Önce Ayarlar bölümünden API anahtarını kaydet.")
        val base = AppSettings.getAiBaseUrl(context).trim().trimEnd('/')
        val model = AppSettings.getAiModel(context).trim()
        require(base.startsWith("https://")) { "Base URL HTTPS olmalı." }
        require(model.isNotBlank()) { "Model adı boş olamaz." }
        val payload = JSONObject().apply {
            put("model", model)
            put("temperature", 0.4)
            put("max_tokens", maxTokens.coerceIn(1, 4096))
            // Mistral Large 4 accepts only "none" or "high"; GLM supports "low".
            put("reasoning_effort", if (model == "mistralai/mistral-large-4-1t-a52b") "none" else "low")
            put("messages", JSONArray().apply { messages.forEach { put(JSONObject().put("role", it.role).put("content", it.content)) } })
        }
        val connection = (URL("$base/chat/completions").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"; connectTimeout = 20000; readTimeout = 90000; doOutput = true
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
                val detail = runCatching {
                    val body = JSONObject(response)
                    val nested = body.optJSONObject("error")
                    sequenceOf(
                        nested?.optString("message"),
                        body.optString("detail"),
                        body.optString("message"),
                        body.optString("title"),
                        response
                    ).firstOrNull { !it.isNullOrBlank() && it != "null" }
                }.getOrNull()
                error("AI servisi HTTP $status: " +
                    (detail?.takeIf { it.isNotBlank() }?.take(500) ?: "Yanıt gövdesi boş."))
            }
            val choice = JSONObject(response).optJSONArray("choices")?.optJSONObject(0)
            val answer = choice?.optJSONObject("message")?.optString("content")
            answer?.takeIf { it.isNotBlank() } ?: error(
                "Model cevap metni üretmedi (finish_reason=" +
                    (choice?.optString("finish_reason") ?: "bilinmiyor") + ")."
            )
        } finally { connection.disconnect() }
    }
}
