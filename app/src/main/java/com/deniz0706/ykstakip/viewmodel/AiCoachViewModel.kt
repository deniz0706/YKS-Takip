package com.deniz0706.ykstakip.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deniz0706.ykstakip.data.AiContextBuilder
import com.deniz0706.ykstakip.data.AiMemoryRepository
import com.deniz0706.ykstakip.data.NvidiaAiService
import com.deniz0706.ykstakip.data.AiSecretStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Ui state for the AI Coach screen. */
data class AiCoachUiState(
    val messages: List<CoachMessage> = emptyList(),
    val draft: String = "",
    val isSending: Boolean = false,
    val hasApiKey: Boolean = false,
    val error: String? = null,
    val isStreaming: Boolean = false
) {
    /** A single coach conversation message. */
    data class CoachMessage(
        val id: String = java.util.UUID.randomUUID().toString(),
        val role: String, // "user" | "assistant" | "system"
        val content: String,
        val timestamp: Long = System.currentTimeMillis(),
        val isError: Boolean = false,
        val isStreaming: Boolean = false
    )
}

/** ViewModel for the AI Coach screen. Handles draft, send lifecycle, streaming, errors, history. */
class AiCoachViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val aiService = NvidiaAiService(context)
    private val memoryRepository = AiMemoryRepository(context)

    private val _uiState = MutableStateFlow(AiCoachUiState())
    val uiState: StateFlow<AiCoachUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = _uiState.value.copy(
            hasApiKey = AiSecretStore.hasApiKey(context),
            messages = initialMessages()
        )
    }

    private fun initialMessages(): List<AiCoachUiState.CoachMessage> {
        val memories = memoryRepository.all()
        return if (memories.isEmpty()) {
            listOf(AiCoachUiState.CoachMessage(role = "assistant", content = "Merhaba! Size nasıl yardımcı olabilir?"))
        } else {
            memories.map { memory ->
                AiCoachUiState.CoachMessage(role = "user", content = memory.content)
            }
        }
    }

    fun onDraftChange(newDraft: String) {
        _uiState.value = _uiState.value.copy(draft = newDraft, error = null)
    }

    fun onSend() {
        val currentDraft = _uiState.value.draft.trim()
        if (currentDraft.isEmpty() || _uiState.value.isSending) return

        if (!_uiState.value.hasApiKey) {
            _uiState.value = _uiState.value.copy(
                error = "Sohbet için Ayarlar bölümünden NVIDIA API anahtarını eklemelisiniz."
            )
            return
        }

        val userMessage = AiCoachUiState.CoachMessage(role = "user", content = currentDraft)
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + listOf(userMessage),
            draft = "",
            isSending = true,
            error = null,
            isStreaming = true
        )

        // Store as long-term memory
        memoryRepository.remember(currentDraft, source = "user_explicit")

        viewModelScope.launch {
            try {
                val contextText = AiContextBuilder.build(context)
                val systemMessage = NvidiaAiService.Message("system", contextText)
                val userMessageForApi = NvidiaAiService.Message("user", currentDraft)
                val messages = listOf(systemMessage, userMessageForApi)

                val response = aiService.chat(messages, maxTokens = 2048)

                val assistantMessage = AiCoachUiState.CoachMessage(
                    role = "assistant",
                    content = response
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + listOf(assistantMessage),
                    isSending = false,
                    isStreaming = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    isStreaming = false,
                    error = "Cevap alınamadı: ${e.message ?: "Bilinmeyen hata"}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun addSystemMessage(content: String) {
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + listOf(AiCoachUiState.CoachMessage(role = "system", content = content))
        )
    }

    fun rememberFact(content: String, source: String = "user_explicit") {
        memoryRepository.remember(content, source = source)
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + listOf(AiCoachUiState.CoachMessage(role = "user", content = content))
        )
    }

    fun forgetMemory(query: String): Int {
        val removed = memoryRepository.forgetMatching(query)
        if (removed > 0) {
            _uiState.value = _uiState.value.copy(
                messages = initialMessages()
            )
        }
        return removed
    }

    fun hasMemoryContent(): Boolean {
        return memoryRepository.all().isNotEmpty()
    }

    fun refreshMemoryMessages() {
        _uiState.value = _uiState.value.copy(
            messages = initialMessages()
        )
    }
}
