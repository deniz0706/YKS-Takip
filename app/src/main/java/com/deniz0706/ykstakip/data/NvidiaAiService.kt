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

/** OpenAI-compatible NVIDIA NIM client with SSE support and a non-streaming fallback. */
class NvidiaAiService(private val context: Context) {
    data class Message(val role: String, val content: String, val imageDataUrls: List<String> = emptyList())

    private class HttpFailure(val status: Int, message: String) : IOException(message)

    suspend fun testConnection(): String =
        chat(listOf(Message("user", "Reply briefly with exactly: bağlantı başarılı")), 1024)

    suspend fun chat(messages: List<Message>, maxTokens: Int = 2048): String = withContext(Dispatchers.IO) {
        val key = AiSecretStore.readApiKey(context)
            ?: error("Önce Ayarlar bölümünden API anahtarını kaydet.")
        val base = AppSettings.getAiBaseUrl(context).trim().trimEnd('/')
        val model = AppSettings.getAiModel(context).trim()
        require(base.startsWith("https://")) { "Base URL HTTPS olmalı." }
        require(model.isNotBlank()) { "Model adı boş olamaz." }

        try {
            request(key, base, model, messages, maxTokens, stream = true)
        } catch (streamFailure: HttpFailure) {
            // A 400 on the SSE request may be specific to streaming. Retry exactly once
            // with the same model, messages and reasoning setting, but a JSON response.
            if (streamFailure.status != 400) throw streamFailure
            try {
                request(key, base, model, messages, maxTokens, stream = false)
            } catch (fallbackFailure: Exception) {
                if (fallbackFailure is CancellationException) throw fallbackFailure
                throw IOException(
                    "Streaming isteği başarısız oldu: ${streamFailure.message}\n" +
                        "Streaming kapalı tekrar da başarısız oldu: ${fallbackFailure.message}",
                    fallbackFailure
                )
            }
        }
    }

    /**
     * Only send reasoning_effort for models whose setting is explicitly configured here.
     * Unknown models receive no reasoning_effort parameter.
     */
    private fun reasoningEffort(model: String): String? = when {
        model.startsWith("mistralai/mistral-large-4") -> "low"
        model.startsWith("nvidia/nemotron-3.5-super-vl-120b") -> "none"
        else -> null
    }

    private suspend fun request(
        key: String,
        base: String,
        model: String,
        messages: List<Message>,
        maxTokens: Int,
        stream: Boolean
    ): String {
        val effort = reasoningEffort(model)
        val payload = JSONObject().apply {
            put("model", model)
            put("temperature", 0.4)
            put("max_tokens", maxTokens.coerceIn(1, 4096))
            if (effort != null) put("reasoning_effort", effort)
            put("stream", stream)
            put("messages", JSONArray().apply {
                messages.forEach { message ->
                    require(message.imageDataUrls.size <= 8) { "Tek mesajda en fazla 8 görsel gönderilebilir." }
                    require(message.imageDataUrls.all {
                        it.startsWith("data:image/") && it.contains(";base64,")
                    }) { "Görsel biçimi desteklenmiyor." }
                    val content: Any = if (message.imageDataUrls.isEmpty()) {
                        message.content
                    } else {
                        JSONArray().apply {
                            put(JSONObject().put("type", "text").put("text", message.content))
                            message.imageDataUrls.forEach { dataUrl ->
                                put(JSONObject().put("type", "image_url").put(
                                    "image_url", JSONObject().put("url", dataUrl)
                                ))
                            }
                        }
                    }
                    put(JSONObject().put("role", message.role).put("content", content))
                }
            })
        }

        val started = SystemClock.elapsedRealtime()
        var phase = "hazirlik"
        fun diagnostic(): String =
            "[faz=$phase, ${(SystemClock.elapsedRealtime() - started) / 1000}sn]"

        val connection = (URL("$base/chat/completions").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 60_000
            doOutput = true
            setRequestProperty("Authorization", "Bearer $key")
            setRequestProperty("Content-Type", "application/json")
            // Do not combine media types in Accept; request the format expected for this attempt.
            setRequestProperty("Accept", if (stream) "text/event-stream" else "application/json")
        }

        val cancellationHook = currentCoroutineContext()[Job]?.invokeOnCompletion { cause ->
            if (cause != null) connection.disconnect()
        }

        return try {
            phase = "istek-gonderiliyor"
            connection.outputStream.use {
                it.write(payload.toString().toByteArray(Charsets.UTF_8))
            }

            phase = "http-durumu-bekleniyor"
            val status = connection.responseCode
            if (status !in 200..299) {
                phase = "hata-govdesi-okunuyor"
                val rawBody = connection.errorStream
                    ?.bufferedReader(Charsets.UTF_8)
                    ?.use { it.readText() }
                    .orEmpty()
                val detail = extractError(rawBody)
                throw HttpFailure(
                    status,
                    "AI servisi HTTP $status: $detail" +
                        " | ham: ${rawBody.take(1200).ifBlank { "(boş)" }}" +
                        " | gönderilen: model=$model stream=$stream effort=${effort ?: "yok"}" +
                        " | faz: $phase"
                )
            }

            phase = "yanit-okunuyor"
            val isEventStream = connection.contentType.orEmpty()
                .contains("text/event-stream", ignoreCase = true)
            val (answer, finishReason) =
                if (isEventStream) readStream(connection) else readJson(connection)

            val nonBlankAnswer = answer.takeIf { it.isNotBlank() } ?: error(
                "Model cevap metni üretmedi (finish_reason=${finishReason ?: "bilinmiyor"}). " +
                    "${diagnostic()} model=$model stream=$stream effort=${effort ?: "yok"}"
            )
            if (finishReason == "length") {
                nonBlankAnswer + "\n\n⚠️ (Yanıt, modelin çıktı uzunluğu sınırına ulaştığı için kesildi.)"
            } else {
                nonBlankAnswer
            }
        } catch (e: IOException) {
            if (!currentCoroutineContext().isActive) {
                throw CancellationException("AI isteği iptal edildi", e)
            }
            if (e is HttpFailure) throw e
            throw IOException("${e.javaClass.simpleName}: ${e.message} ${diagnostic()}", e)
        } finally {
            cancellationHook?.dispose()
            connection.disconnect()
        }
    }

    private fun extractError(rawBody: String): String {
        if (rawBody.isBlank()) return "Yanıt gövdesi boş."
        return runCatching {
            val body = JSONObject(rawBody)
            val nested = body.optJSONObject("error")
            sequenceOf(
                nested?.optString("message"),
                body.optString("detail"),
                body.optString("message"),
                body.optString("title")
            ).firstOrNull { !it.isNullOrBlank() && it != "null" }
        }.getOrNull()?.take(500) ?: rawBody.take(500)
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
                if (delta != null && !delta.isNull("content")) {
                    answer.append(delta.optString("content"))
                }
                if (!choice.isNull("finish_reason")) {
                    finishReason = choice.optString("finish_reason")
                }
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
