package com.deniz0706.ykstakip.ui.ai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.deniz0706.ykstakip.data.AiContextBuilder
import com.deniz0706.ykstakip.data.AiSecretStore
import com.deniz0706.ykstakip.data.NvidiaAiService
import com.deniz0706.ykstakip.ui.theme.YksTheme
import com.deniz0706.ykstakip.ui.theme.activeDarkTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class CoachLine(val role: String, val text: String)

class AiCoachFragment : Fragment() {
    private val lines = mutableStateListOf<CoachLine>()
    private val draft = mutableStateOf("")
    private val sending = mutableStateOf(false)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        androidx.compose.ui.platform.ComposeView(requireContext()).apply {
            setViewCompositionStrategy(androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                YksTheme(darkTheme = activeDarkTheme(context)) {
                    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp)) {
                        Text("YKS AI KOÇU", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        Text("Çalışmanı birlikte değerlendirelim", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(10.dp))
                        if (!AiSecretStore.hasApiKey(context)) {
                            Text("Sohbeti kullanmak için Ayarlar bölümünden kendi API anahtarını eklemelisin.", color = MaterialTheme.colorScheme.error)
                        }
                        LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (lines.isEmpty()) item {
                                Text("Deneme sonuçlarını, eksik konularını veya haftalık planını sorabilirsin. Verilerin yalnızca mesaj gönderdiğinde özetlenir.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            itemsIndexed(lines) { _, line ->
                                Surface(color = if (line.role == "user") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                                    Text(line.text, Modifier.padding(13.dp), color = if (line.role == "user") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (sending.value) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                        }
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = draft.value, onValueChange = { draft.value = it }, modifier = Modifier.weight(1f), placeholder = { Text("Koçuna bir şey sor…") }, maxLines = 4)
                            Button(onClick = { sendMessage() }, enabled = !sending.value && draft.value.isNotBlank() && AiSecretStore.hasApiKey(context)) { Text("Gönder") }
                        }
                    }
                }
            }
        }

    private fun sendMessage() {
        val question = draft.value.trim()
        if (question.isEmpty() || sending.value || !AiSecretStore.hasApiKey(requireContext())) return
        draft.value = ""
        lines.add(CoachLine("user", question))
        sending.value = true
        val appContext = requireContext().applicationContext
        viewLifecycleOwner.lifecycleScope.launch {
            val result = runCatching {
                val contextText = withContext(Dispatchers.IO) { AiContextBuilder.build(appContext) }
                val messages = mutableListOf(NvidiaAiService.Message("system",
                    "Sen YKS-Takip uygulamasının gerçekçi, veri odaklı çalışma koçusun. Türkçe yanıt ver. Öğrencinin verilerinde olmayan bilgileri uydurma. Kısa, uygulanabilir öneriler sun. Aşağıdaki güncel özet yalnızca bu sohbet yanıtı için kullanılmalı.\n\nÖĞRENCİ ÖZETİ:\n$contextText"))
                lines.takeLast(12).forEach { messages.add(NvidiaAiService.Message(it.role, it.text)) }
                NvidiaAiService(appContext).chat(messages)
            }
            if (!isAdded) return@launch
            lines.add(CoachLine("assistant", result.getOrElse { "Yanıt alınamadı: " + (it.message ?: "Bağlantı hatası") }))
            sending.value = false
        }
    }
}