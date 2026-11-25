package com.deniz0706.ykstakip.data

import android.content.Context
import android.os.SystemClock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** OpenAI-compatible NVIDIA NIM client with SSE support and cancellation-safe cleanup. */
class NvidiaAiService(private val context: Context) {
    data class Message(val role: String, val content: String)

    suspend fun testConnection(): String =
        chat(listOf(Message("user", "Reply briefly with exactly: bağlantı başarılı")), 256)

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
            put("reasoning_effort", if (model == "mistralai/mistral-large-4-1t-a52b") "high" else "low")
            put("stream", true)
            put("messages", JSONArray().apply {
                messages.forEach { put(JSONObject().put("role", it.role).put("content", it.content)) }
            })
        }

        val started = SystemClock.elapsedRealtime()
        var phase = "hazirlik"
        fun diagnostic() = "[faz=$phase, ${(SystemClock.elapsedRealtime() - started) / 1000}sn]"

        val connection = (URL("$base/chat/completions").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 60_000
            doOutput = true
            setRequestProperty("Authorization", "Bearer $key")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "text/event-stream")
        }

        val cancellationHook = currentCoroutineContext()[Job]?.invokeOnCompletion { cause ->
            if (cause != null) connection.disconnect()
        }

        try {
            phase = "istek-gonderiliyor"
            connection.outputStream.use {
                it.write(payload.toString().toByteArray(Charsets.UTF_8))
            }

            phase = "http-durumu-bekleniyor"
            val status = connection.responseCode
            if (status !in 200..299) {
                phase = "hata-govdesi-okunuyor"
                val response = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
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
                throw IOException(
                    "AI servisi HTTP $status: " +
                        (detail?.takeIf { it.isNotBlank() }?.take(500) ?: "Yanıt gövdesi boş.") +
                        " ${diagnostic()}"
                )
            }

            phase = "yanit-okunuyor"
            val isEventStream = connection.contentType.orEmpty()
                .contains("text/event-stream", ignoreCase = true)
            val (answer, finishReason) = if (isEventStream) readStream(connection) else readJson(connection)

            answer.takeIf { it.isNotBlank() } ?: error(
                "Model cevap metni üretmedi (finish_reason=${finishReason ?: "bilinmiyor"}). ${diagnostic()}"
            )
        } catch (e: IOException) {
            if (!currentCoroutineContext().isActive) {
                throw CancellationException("AI isteği iptal edildi", e)
            }
            throw IOException("${e.javaClass.simpleName}: ${e.message} ${diagnostic()}", e)
        } finally {
            cancellationHook?.dispose()
            connection.disconnect()
        }
    }

    private fun readStream(connection: HttpURLConnection): Pair<String, String?> {
        val answer = StringBuilder()
        var finishReason: String? = null
        var receivedDone = false

        connection.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
            while (true) {
                val line = reader.readLine() ?: break
                if (!line.startsWith("data:")) continue
                val data = line.substring(5).trim()
                if (data.isEmpty()) continue
                if (data == "[DONE]") {
                    receivedDone = true
                    break
                }

                val choice = runCatching {
                    JSONObject(data).optJSONArray("choices")?.optJSONObject(0)
                }.getOrNull() ?: continue
                val delta = choice.optJSONObject("delta")
                if (delta != null && !delta.isNull("content")) answer.append(delta.optString("content"))
                if (!choice.isNull("finish_reason")) finishReason = choice.optString("finish_reason")
            }
        }

        val result = answer.toString()
        if (result.isNotBlank() && !receivedDone && finishReason == null) {
            return (result + "\n\n(Yanıt akışı beklenmedik biçimde kesilmiş olabilir.)") to "kesildi"
        }
        return result to finishReason
    }

    private fun readJson(connection: HttpURLConnection): Pair<String, String?> {
        val response = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        val choice = JSONObject(response).optJSONArray("choices")?.optJSONObject(0)
        val answer = choice?.optJSONObject("message")?.optString("content").orEmpty()
        return answer to choice?.optString("finish_reason")
    }
}
