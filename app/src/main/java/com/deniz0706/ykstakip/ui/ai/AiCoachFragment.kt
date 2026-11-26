package com.deniz0706.ykstakip.ui.ai

import android.content.Context
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.deniz0706.ykstakip.data.AiContextBuilder
import com.deniz0706.ykstakip.data.AiImageEncoder
import com.deniz0706.ykstakip.data.AiMemoryRepository
import com.deniz0706.ykstakip.data.AiSecretStore
import com.deniz0706.ykstakip.data.NvidiaAiService
import com.deniz0706.ykstakip.data.StudyPlanRepository
import com.deniz0706.ykstakip.model.StudyPlanItem
import com.deniz0706.ykstakip.util.PlanActionParser
import com.deniz0706.ykstakip.util.StudyPlanConflicts
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

private data class CoachLine(val role: String, val text: String, val imageDataUrl: String? = null)

class AiCoachFragment : Fragment() {
    private val lines = mutableStateListOf<CoachLine>()
    private val draft = mutableStateOf("")
    private val sending = mutableStateOf(false)
    private val pendingImageData = mutableStateOf<String?>(null)
    private val imageLoading = mutableStateOf(false)

    private val pickAiImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && isAdded) {
            val appContext = requireContext().applicationContext
            imageLoading.value = true
            (requireActivity().application as com.deniz0706.ykstakip.YksApp).applicationScope.launch {
                val result = runCatching {
                    withContext(Dispatchers.IO) { AiImageEncoder.encodeJpegDataUrl(appContext, uri) }
                }
                if (isAdded) {
                    imageLoading.value = false
                    result.onSuccess { pendingImageData.value = it }
                        .onFailure {
                            Toast.makeText(requireContext(),
                                "Görsel hazırlanamadı: ${it.message ?: "Dosya hatası"}",
                                Toast.LENGTH_LONG).show()
                        }
                }
            }
        }
    }

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
                    val appContext = LocalContext.current.applicationContext
                    val studyPlanRepository = remember(appContext) { StudyPlanRepository(appContext) }
                    var pendingPlanDraft by remember { mutableStateOf<PlanActionParser.Draft?>(null) }
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
                            Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(18.dp)) {
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
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Merhaba! 👋", style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold)
                                        Text("Deneme sonuçlarını yorumlayabilir, eksik konularını önceliklendirebilir veya haftalık çalışma planını birlikte düzenleyebiliriz.",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Örnek: Son üç denememde en çok nerede net kaybediyorum?",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary)
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            AssistChip(
                                                onClick = { draft.value = "Son 5 denememin gelişimini analiz et." },
                                                label = { Text("Net trendim") }
                                            )
                                            AssistChip(
                                                onClick = { draft.value = "Bu haftam için gerçekçi bir çalışma önceliği çıkar." },
                                                label = { Text("Haftalık öncelik") }
                                            )
                                        }
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
                                            else -> MaterialTheme.colorScheme.surfaceContainerLow
                                        },
                                        shape = RoundedCornerShape(
                                            topStart = 18.dp, topEnd = 18.dp,
                                            bottomStart = if (isUser) 18.dp else 5.dp,
                                            bottomEnd = if (isUser) 5.dp else 18.dp
                                        )
                                    ) {
                                        Column(Modifier.padding(horizontal = 15.dp, vertical = 12.dp)) {
                                            if (line.imageDataUrl != null) {
                                                Text("📎 Görsel eklendi · bu istek için gönderildi",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    style = MaterialTheme.typography.labelSmall)
                                            }
                                            MarkdownMessage(
                                                text = if (line.role == "assistant") PlanActionParser.visibleText(line.text) else line.text,
                                                modifier = Modifier,
                                                color = when {
                                                    isUser -> MaterialTheme.colorScheme.onPrimaryContainer
                                                    isError -> MaterialTheme.colorScheme.onErrorContainer
                                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                            val planDraft = if (line.role == "assistant") PlanActionParser.parse(line.text) else null
                                            if (planDraft != null) {
                                                TextButton(onClick = { pendingPlanDraft = planDraft }) {
                                                    Text("✓  Öneriyi haftalık plana ekle")
                                                }
                                            }
                                        }
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
                        pendingPlanDraft?.let { proposed ->
                            val existing = studyPlanRepository.getAll()
                            val candidateId = maxOf(
                                System.currentTimeMillis(),
                                (existing.maxOfOrNull { it.id } ?: 0L) + 1L
                            )
                            val candidate = StudyPlanItem(
                                id = candidateId,
                                dayOfWeek = proposed.dayOfWeek,
                                startTime = proposed.startTime,
                                endTime = proposed.endTime,
                                subject = proposed.subject,
                                topic = proposed.topic,
                                completed = false
                            )
                            val duplicate = existing.any {
                                it.dayOfWeek == proposed.dayOfWeek &&
                                    it.startTime == proposed.startTime &&
                                    it.endTime == proposed.endTime &&
                                    it.subject.equals(proposed.subject, ignoreCase = true) &&
                                    it.topic.equals(proposed.topic, ignoreCase = true)
                            }
                            val conflict = candidate.id in StudyPlanConflicts.findConflictingIds(existing + candidate)
                            val dayName = listOf("", "Pazar", "Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi")[proposed.dayOfWeek]
                            AlertDialog(
                                onDismissRequest = { pendingPlanDraft = null },
                                title = { Text("Çalışma planına eklensin mi?") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("$dayName · ${proposed.startTime}–${proposed.endTime}")
                                        Text(proposed.subject, fontWeight = FontWeight.SemiBold)
                                        if (proposed.topic.isNotBlank()) Text(proposed.topic)
                                        Text("Bu işlem haftalık tekrar eden planına kaydedilecek.")
                                        if (duplicate) Text("Bu görev planında zaten var.", color = MaterialTheme.colorScheme.error)
                                        else if (conflict) Text("Bu saat aralığı mevcut bir dersle çakışıyor. Önce saatleri düzenle.", color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                confirmButton = {
                                    TextButton(
                                        enabled = !duplicate && !conflict,
                                        onClick = {
                                            studyPlanRepository.save(candidate)
                                            lines.add(CoachLine("assistant", "✓ $dayName ${proposed.startTime}–${proposed.endTime} ${proposed.subject} haftalık planına eklendi."))
                                            persistConversation(appContext)
                                            pendingPlanDraft = null
                                        }
                                    ) { Text("Onayla ve ekle") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { pendingPlanDraft = null }) { Text("Vazgeç") }
                                }
                            )
                        }
                        Row(
                            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "Son 5 denememi karşılaştır" to "Son 5 denememi önceki 5 denememle karşılaştır; net, doğru oranı ve değişkenliği değerlendir.",
                                "Önceliğim ne?" to "Mevcut verilerime göre bu hafta en yüksek getirili üç çalışma önceliğimi sırala.",
                                "AYT analizi" to "AYT denemelerimde branş bazında güçlü ve gelişmesi gereken alanları analiz et.",
                                "Planımı düzenle" to "Mevcut çalışma düzenimi dikkate alarak gerçekçi bir haftalık plan öner."
                            ).forEach { (label, prompt) ->
                                AssistChip(onClick = { draft.value = prompt }, label = { Text(label, maxLines = 1) })
                            }
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(
                                onClick = { pickAiImage.launch("image/*") },
                                enabled = !sending.value && !imageLoading.value
                            ) { Text(if (imageLoading.value) "Görsel hazırlanıyor…" else "＋ Görsel ekle") }
                            if (pendingImageData.value != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Görsel hazır", color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.labelSmall)
                                    TextButton(onClick = { pendingImageData.value = null }) {
                                        Text("Kaldır")
                                    }
                                }
                            }
                        }
                        Text(
                            "Görsel, mesajı gönderdiğinde NVIDIA AI’ye iletilir; sohbet geçmişine dosya olarak kaydedilmez.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(24.dp),
                            tonalElevation = 0.dp
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
                                    enabled = !sending.value && !imageLoading.value && (draft.value.isNotBlank() || pendingImageData.value != null),
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
            if (forgetText.trim().length < 4) {
                return "Güvenlik için en az 4 karakterlik bir ifade yazmalısın. Kayıtlarımı görmek için ‘Hafızamda ne var?’ diyebilirsin."
            }
            val removed = repository.forgetMatching(forgetText)
            if (removed > 0) return "Tam eşleşen ${removed} hafıza kaydını sildim."
            val matches = repository.findMatching(forgetText)
            return if (matches.isNotEmpty()) {
                "Bu ifade ${matches.size} kayıtla eşleşiyor; yanlışlıkla toplu silme yapmamak için hiçbirini silmedim. Silmek istediğin kaydın tam metnini ‘Unut: [kaydın tamamı]’ şeklinde gönder.\n" +
                    matches.take(10).joinToString("\n") { "• ${it.content}" }
            } else {
                "Bu ifadeyle eşleşen bir kayıt bulamadım. Silme işlemi için kaydın tam metnini yazmalısın."
            }
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
        if (sending.value || imageLoading.value) return
        val imageData = pendingImageData.value
        val question = draft.value.trim().ifBlank {
            if (imageData != null) "Bu görseldeki YKS sorusunu çöz ve çözümünü adım adım açıkla." else return
        }
        val appContext = requireContext().applicationContext
        val memoryReply = if (imageData == null) handleMemoryCommand(question) else null
        if (memoryReply == null && !AiSecretStore.hasApiKey(appContext)) {
            lines.add(CoachLine("error", "Görsel analizi ve normal sohbet için Ayarlar bölümünden NVIDIA API anahtarını eklemelisin."))
            persistConversation(appContext)
            return
        }
        draft.value = ""
        pendingImageData.value = null
        lines.add(CoachLine("user", question, imageData))
        persistConversation(appContext)
        if (memoryReply != null) {
            lines.add(CoachLine("assistant", memoryReply))
            persistConversation(appContext)
            return
        }
        sending.value = true
        (requireActivity().application as com.deniz0706.ykstakip.YksApp).applicationScope.launch {
            var answer: String? = null
            var failure: String? = null
            try {
                answer = withTimeout(80_000) {
                    val contextText = withContext(Dispatchers.IO) { AiContextBuilder.build(appContext) }
                    val messages = mutableListOf(NvidiaAiService.Message("system",
                        "Sen YKS-Takip uygulamasının gerçekçi, veri odaklı çalışma koçusun. Her zaman Türkçe yanıt ver. Öğrencinin verilerinde olmayan bilgileri uydurma. Kısa, uygulanabilir ve önceliklendirilmiş öneriler sun. Performans özetinde son 5 denemeyi önceki 5 denemeyle karşılaştır; veri azsa bunu açıkça söyle. Branş önceliğini yalnızca nete göre değil doğru oranı, değişkenlik, süre ve öğrencinin etiketlediği tekrar konularına göre değerlendir. Net verisinden dikkat hatası veya bilgi eksiği teşhisi koyma; kullanıcıdan hata türü verisi yoksa bunu bilmediğini söyle. Tekrar zamanı gelen kazanımları ve çözülmemiş soru kayıtlarını önceliklendir. Günlük/haftalık plan önerirken mevcut programı dikkate al; uygulamada kaydetmediğin bir görevi kaydedilmiş gibi sunma. Kalıcı öğrenci hafızasındaki isimleri, tercihleri, kaynakları ve kararları sonraki konuşmalarda hatırla; bunları güncel uygulama verileriyle karıştırma. Hafızadaki bilgi ile yeni söylenen bilgi çelişirse bunu belirt ve netleştir. Güncel öğrenci özeti her yanıtta yenilenir; önceki konuşmayı da dikkate al. Haftalık plan için somut ve saatli bir görev önerdiğinde yanıtın sonuna yalnızca tek bir makine-okunur blok ekle: [[YKS_PLAN_ACTION]]{\"type\":\"add_plan\",\"dayOfWeek\":2,\"startTime\":\"09:00\",\"endTime\":\"10:00\",\"subject\":\"Matematik\",\"topic\":\"Fonksiyonlar\"}[[/YKS_PLAN_ACTION]]. dayOfWeek uygulamanın takvim düzeniyle aynıdır: 2 Pazartesi, 3 Salı, 4 Çarşamba, 5 Perşembe, 6 Cuma, 7 Cumartesi, 1 Pazar. Saatler 24 saat biçiminde ve bitiş başlangıçtan sonra olmalı. Birden fazla görev için blok üretme; belirsiz önerilerde blok ekleme. Bu blok yalnızca kullanıcı açıkça onaylarsa kaydedilir.\n\nÖĞRENCİ ÖZETİ:\n$contextText"))
                    lines.asSequence()
                        .filter { it.role == "user" || it.role == "assistant" }
                        .toList()
                        .takeLast(12)
                        .dropWhile { it.role != "user" }
                        .forEach { line ->
                            val last = messages.last()
                            val imageParts = line.imageDataUrl?.let { listOf(it) }.orEmpty()
                            if (messages.size > 1 && last.role == line.role) {
                                messages[messages.lastIndex] = last.copy(
                                    content = last.content + "\n\n" + line.text,
                                    imageDataUrls = last.imageDataUrls + imageParts
                                )
                            } else {
                                messages.add(NvidiaAiService.Message(line.role, line.text, imageParts))
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
