package com.deniz0706.ykstakip.ui.ai

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.deniz0706.ykstakip.data.AiContextBuilder
import com.deniz0706.ykstakip.data.AiMemoryRepository
import com.deniz0706.ykstakip.data.AiSecretStore
import com.deniz0706.ykstakip.data.NvidiaAiService
import com.deniz0706.ykstakip.ui.theme.YksTheme
import com.deniz0706.ykstakip.ui.theme.activeDarkTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.CancellationException
import org.json.JSONArray
import org.json.JSONObject

private data class CoachLine(val role: String, val text: String)

class AiCoachFragment : Fragment() {
    private val lines = mutableStateListOf<CoachLine>()
    private val draft = mutableStateOf("")
    private val sending = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lines.clear()
        lines.addAll(loadConversation(requireContext()))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        androidx.compose.ui.platform.ComposeView(requireContext()).apply {
            setViewCompositionStrategy(androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                YksTheme(darkTheme = activeDarkTheme(context)) {
                    val listState = rememberLazyListState()
                    LaunchedEffect(lines.size, sending.value) {
                        val count = lines.size + if (sending.value) 1 else 0
                        if (count > 0) listState.animateScrollToItem(count - 1)
                    }
                    Column(
                        Modifier.fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("AI KOÇUN", style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text("YKS sürecini birlikte yönetelim", style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold)
                                Text("Denemelerin ve çalışma kayıtların mesajlarına bağlam sağlar.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer) {
                                Text("NVIDIA AI", Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        if (!AiSecretStore.hasApiKey(context)) {
                            Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(14.dp)) {
                                Text("Sohbet için Ayarlar bölümünden NVIDIA API anahtarını eklemelisin.",
                                    Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (lines.isEmpty()) item {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Merhaba! 👋", style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold)
                                        Text("Deneme sonuçlarını yorumlayabilir, eksik konularını önceliklendirebilir veya haftalık çalışma planını birlikte düzenleyebiliriz.",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Örnek: Son üç denememde en çok nerede net kaybediyorum?",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            itemsIndexed(lines) { _, line ->
                                val isUser = line.role == "user"
                                val isError = line.role == "error"
                                Row(Modifier.fillMaxWidth(), horizontalArrangement =
                                    if (isUser) Arrangement.End else Arrangement.Start) {
                                    Surface(
                                        modifier = Modifier.widthIn(max = 340.dp, min = 80.dp),
                                        color = when {
                                            isUser -> MaterialTheme.colorScheme.primaryContainer
                                            isError -> MaterialTheme.colorScheme.errorContainer
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        shape = RoundedCornerShape(
                                            topStart = 18.dp, topEnd = 18.dp,
                                            bottomStart = if (isUser) 18.dp else 5.dp,
                                            bottomEnd = if (isUser) 5.dp else 18.dp
                                        )
                                    ) {
                                        MarkdownMessage(
                                            text = line.text,
                                            modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
                                            color = when {
                                                isUser -> MaterialTheme.colorScheme.onPrimaryContainer
                                                isError -> MaterialTheme.colorScheme.onErrorContainer
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }
                            }
                            if (sending.value) item {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Text("Koçun yanıt hazırlıyor…", style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(22.dp),
                            tonalElevation = 1.dp
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(7.dp),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = draft.value,
                                    onValueChange = { draft.value = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Koçuna mesaj yaz…") },
                                    maxLines = 5,
                                    shape = RoundedCornerShape(18.dp),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = { sendMessage() })
                                )
                                FilledIconButton(
                                    onClick = { sendMessage() },
                                    enabled = !sending.value && draft.value.isNotBlank() && AiSecretStore.hasApiKey(context),
                                    modifier = Modifier.size(50.dp)
                                ) {
                                    Text("➤", style = MaterialTheme.typography.titleLarge)
                                }
                            }
                        }
                    }
                }
            }
        }

    /**
     * Memory commands are handled locally: they do not spend an NVIDIA request
     * and the saved facts survive model changes and conversation pruning.
     */
    private fun handleMemoryCommand(message: String): String? {
        val repository = AiMemoryRepository(requireContext().applicationContext)
        val trimmed = message.trim()

        val showPattern = Regex("^(hafızamda ne var|neleri hatırlıyorsun|hafızanı göster)\\??$", RegexOption.IGNORE_CASE)
        if (showPattern.matches(trimmed)) {
            val memories = repository.all()
            if (memories.isEmpty()) return "Henüz kalıcı hafızama kaydedilmiş bir bilgi yok. Hatırla: ... yazarak bir bilgi ekleyebilirsin."
            return "Kalıcı hafızamda şunlar var:\n" + memories.take(30).joinToString("\n") {
                "• [${it.category}] ${it.content}"
            } + if (memories.size > 30) "\n… ve ${memories.size - 30} kayıt daha." else ""
        }

        val rememberPattern = Regex("^(?:(?:lütfen )?(?:bunu )?)?(?:hatırla|hafızana kaydet|aklında tut)\\s*[:：-]?\\s*(.+)$", RegexOption.IGNORE_CASE)
        val rememberText = rememberPattern.matchEntire(trimmed)?.groupValues?.getOrNull(1)
        if (!rememberText.isNullOrBlank()) {
            val saved = repository.remember(rememberText) ?: return "Bu bilgiyi kaydedemedim; biraz daha açık yazar mısın?"
            return "Tamam, bunu kalıcı hafızama **${saved.category}** olarak kaydettim. Sonraki konuşmalarda da kullanacağım."
        }

        val forgetPattern = Regex("^(?:unut|hafızandan sil|hafızadan sil)\\s*[:：-]?\\s*(.+)$", RegexOption.IGNORE_CASE)
        val forgetText = forgetPattern.matchEntire(trimmed)?.groupValues?.getOrNull(1)
        if (!forgetText.isNullOrBlank()) {
            val removed = repository.forgetMatching(forgetText)
            return if (removed > 0) "${removed} hafıza kaydını sildim."
                else "Bu ifadeyle eşleşen bir hafıza kaydı bulamadım."
        }
        return null
    }
    private fun loadConversation(context: Context): List<CoachLine> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_LINES, null)
            ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                val item = array.getJSONObject(index)
                val role = item.optString("role")
                val text = item.optString("text")
                if (role in setOf("user", "assistant", "error") && text.isNotBlank()) CoachLine(role, text) else null
            }.takeLast(MAX_SAVED_LINES)
        }.getOrDefault(emptyList())
    }

    private fun persistConversation(context: Context) {
        val array = JSONArray()
        lines.takeLast(MAX_SAVED_LINES).forEach { line ->
            array.put(JSONObject().put("role", line.role).put("text", line.text))
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_LINES, array.toString()).apply()
    }

    private fun sendMessage() {
        val question = draft.value.trim()
        if (question.isEmpty() || sending.value || !AiSecretStore.hasApiKey(requireContext())) return
        draft.value = ""
        lines.add(CoachLine("user", question))
        val appContext = requireContext().applicationContext
        AiMemoryRepository(appContext).captureFromUserMessage(question)
        persistConversation(appContext)
        val memoryReply = handleMemoryCommand(question)
        if (memoryReply != null) {
            lines.add(CoachLine("assistant", memoryReply))
            persistConversation(requireContext().applicationContext)
            return
        }
        sending.value = true
        val appContext = requireContext().applicationContext
        viewLifecycleOwner.lifecycleScope.launch {
            var answer: String? = null
            var failure: String? = null
            try {
                answer = withTimeout(80_000) {
                    val contextText = withContext(Dispatchers.IO) { AiContextBuilder.build(appContext) }
                    val messages = mutableListOf(NvidiaAiService.Message("system",
                        "Sen YKS-Takip uygulamasının gerçekçi, veri odaklı çalışma koçusun. Türkçe yanıt ver. Öğrencinin verilerinde olmayan bilgileri uydurma. Kısa, uygulanabilir öneriler sun. Markdown biçimlendirmesi kullanabilirsin. Kalıcı öğrenci hafızasındaki isimleri, tercihleri, kaynakları ve kararları sonraki konuşmalarda hatırla; bunları güncel uygulama verileriyle karıştırma. Hafızadaki bilgi ile yeni söylenen bilgi çelişirse bunu belirt ve netleştir. Güncel öğrenci özeti her yanıtta yenilenir; önceki konuşmayı da dikkate al.\n\nÖĞRENCİ ÖZETİ:\n$contextText"))
                    lines.asSequence()
                        .filter { it.role == "user" || it.role == "assistant" }
                        .toList()
                        .takeLast(12)
                        .dropWhile { it.role != "user" }
                        .forEach { line ->
                            val last = messages.last()
                            if (messages.size > 1 && last.role == line.role) {
                                messages[messages.lastIndex] = last.copy(content = last.content + "\n\n" + line.text)
                            } else {
                                messages.add(NvidiaAiService.Message(line.role, line.text))
                            }
                        }
                    NvidiaAiService(appContext).chat(messages)
                }
            } catch (e: TimeoutCancellationException) {
                failure = "Yanıt 80 saniyede tamamlanmadı. Tekrar deneyebilirsin."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                failure = e.message ?: "Bağlantı hatası"
            } finally {
                sending.value = false
            }
            if (!isAdded) return@launch
            val result = if (answer != null) CoachLine("assistant", answer!!) else
                CoachLine("error", "Yanıt alınamadı: " + (failure ?: "Bilinmeyen hata"))
            lines.add(result)
            persistConversation(appContext)
        }
    }

    companion object {
        private const val PREFS = "ai_coach_conversation"
        private const val KEY_LINES = "conversation_lines"
        private const val MAX_SAVED_LINES = 200
    }
}

@Composable
private fun MarkdownMessage(text: String, modifier: Modifier = Modifier, color: androidx.compose.ui.graphics.Color) {
    val annotated = remember(text, color) {
        buildAnnotatedString {
            val bold = Regex("\\*\\*(.+?)\\*\\*")
            val lines = text.split("\n")
            lines.forEachIndexed { index, line ->
                val trimmed = line.trimStart()
                val heading = trimmed.startsWith("#")
                val clean = if (heading) trimmed.trimStart('#').trim() else
                    trimmed.removePrefix("- ").removePrefix("* ").removePrefix("• ")
                if (index > 0) append("\n")
                if (trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ")) append("• ")
                val weight = if (heading) FontWeight.Bold else FontWeight.Normal
                val start = length
                var cursor = 0
                bold.findAll(clean).forEach { match ->
                    append(clean.substring(cursor, match.range.first))
                    val boldStart = length
                    append(match.groupValues[1])
                    addStyle(SpanStyle(fontWeight = FontWeight.Bold), boldStart, length)
                    cursor = match.range.last + 1
                }
                append(clean.substring(cursor))
                if (heading) addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, length)
                if (clean.startsWith("**") && clean.endsWith("**") && length > start) {
                    addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, length)
                }
            }
        }
    }
    Text(annotated, modifier = modifier, color = color, style = MaterialTheme.typography.bodyMedium)
}
