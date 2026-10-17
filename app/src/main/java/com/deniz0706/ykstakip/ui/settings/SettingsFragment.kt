package com.deniz0706.ykstakip.ui.settings

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.deniz0706.ykstakip.data.*
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.model.StudyDay
import com.deniz0706.ykstakip.ui.MainActivity
import com.deniz0706.ykstakip.ui.theme.YksTheme
import com.deniz0706.ykstakip.ui.theme.activeDarkTheme

class SettingsFragment : Fragment() {
    private lateinit var repository: ExamRepository
    private lateinit var studyRepository: StudyRepository
    private val theme = mutableStateOf("system")
    private val learningZipBusy = mutableStateOf(false)
    private var stagedLearningZip: LearningArchiveManager.PreparedImport? = null

    private val exportStudy = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null || !isAdded) return@registerForActivityResult
        lifecycleScope.launch {
            val saved = withContext(Dispatchers.IO) {
                runCatching {
                    val json = StudyBackupManager.exportJson(studyRepository.getAll())
                    val out = requireContext().contentResolver.openOutputStream(uri)
                        ?: error("Dosya yazılamadı.")
                    out.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                }.isSuccess
            }
            if (isAdded) Toast.makeText(requireContext(),
                if (saved) "Çalışma JSON yedeği oluşturuldu." else "Yedek oluşturulamadı.",
                Toast.LENGTH_LONG).show()
        }
    }

    private val importStudy = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null || !isAdded) return@registerForActivityResult
        lifecycleScope.launch {
            val parsed = withContext(Dispatchers.IO) {
                runCatching {
                    val input = requireContext().contentResolver.openInputStream(uri)
                        ?: error("Dosya okunamadı.")
                    val text = input.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    require(text.length <= 1500000) { "Yedek dosyası çok büyük." }
                    StudyBackupManager.parseJson(text)
                }
            }
            if (!isAdded) return@launch
            parsed.onFailure {
                Toast.makeText(requireContext(), "Dosya geçersiz: " +
                    (it.message ?: "Okuma hatası"), Toast.LENGTH_LONG).show()
            }
            parsed.onSuccess { data ->
                val existing = studyRepository.getAll().map { it.date }.toSet()
                val duplicates = data.days.count { it.date in existing }
                AlertDialog.Builder(requireContext())
                    .setTitle("Çalışma geçmişini içe aktar")
                    .setMessage(
                        data.days.size.toString() + " gün bulundu (" +
                        data.estimatedDays + " tahmini). " + duplicates +
                        " gün zaten kayıtlı.\n\n" +
                        "Eksik günleri ekle: eskilere dokunma.\n" +
                        "Üzerine yaz: çakışan günleri değiştir."
                    )
                    .setPositiveButton("Eksik günleri ekle") { _, _ ->
                        applyStudyImport(data.days, false)
                    }
                    .setNeutralButton("Üzerine yaz") { _, _ ->
                        AlertDialog.Builder(requireContext())
                            .setTitle("Mevcut günlerin üzerine yazılsın mı?")
                            .setMessage("Kayıtlı gerçek çalışma verileri değişebilir. Önce kendi JSON yedeğini alman önerilir.")
                            .setNegativeButton("Vazgeç", null)
                            .setPositiveButton("Değiştir") { _, _ ->
                                applyStudyImport(data.days, true)
                            }.show()
                    }
                    .setNegativeButton("Vazgeç", null)
                    .show()
            }
        }
    }

    private val exportLearningZip = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null && isAdded) {
            val app = requireContext().applicationContext
            learningZipBusy.value = true
            lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        LearningArchiveManager(app).exportTo(uri)
                    }
                    if (isAdded) Toast.makeText(requireContext(),
                        "ZIP kaydedildi: ${result.outcomes} kazanım, ${result.questions} soru, ${result.photos} fotoğraf.",
                        Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    if (isAdded) Toast.makeText(requireContext(),
                        "Yedekleme başarısız: ${e.message ?: "Dosya hatası"}",
                        Toast.LENGTH_LONG).show()
                } finally { learningZipBusy.value = false }
            }
        }
    }

    private val importLearningZip = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null && isAdded) {
            val app = requireContext().applicationContext
            learningZipBusy.value = true
            lifecycleScope.launch {
                try {
                    val pending = withContext(Dispatchers.IO) {
                        LearningArchiveManager(app).prepareImport(uri)
                    }
                    if (!isAdded) { pending.discard(); return@launch }
                    stagedLearningZip?.discard()
                    stagedLearningZip = pending
                    AlertDialog.Builder(requireContext())
                        .setTitle("Öğrenme ZIP'ini birleştir")
                        .setMessage("${pending.outcomes.size} kazanım, ${pending.questions.size} soru, " +
                            "${pending.photoCount} fotoğraf doğrulandı. Mevcut kayıtlar silinmez; " +
                            "aynı soruların üzerine yazılmaz. Yeni kayıtlar eklenir. Devam edilsin mi?")
                        .setNegativeButton("Vazgeç") { _, _ -> cancelLearningZip() }
                        .setOnCancelListener { cancelLearningZip() }
                        .setPositiveButton("Birleştir") { _, _ ->
                            val selection = stagedLearningZip ?: return@setPositiveButton
                            stagedLearningZip = null
                            learningZipBusy.value = true
                            lifecycleScope.launch {
                                try {
                                    val result = withContext(Dispatchers.IO) {
                                        LearningArchiveManager(app).merge(selection)
                                    }
                                    if (isAdded) Toast.makeText(requireContext(),
                                        "${result.outcomesAdded} kazanım, ${result.questionsAdded} soru, " +
                                            "${result.photosAdded} fotoğraf eklendi.",
                                        Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    if (isAdded) Toast.makeText(requireContext(),
                                        "Birleştirme başarısız: ${e.message ?: "Veri hatası"}",
                                        Toast.LENGTH_LONG).show()
                                } finally {
                                    selection.discard()
                                    learningZipBusy.value = false
                                }
                            }
                        }.show()
                } catch (e: Exception) {
                    if (isAdded) Toast.makeText(requireContext(),
                        "ZIP açılamadı: ${e.message ?: "Arşiv hatası"}",
                        Toast.LENGTH_LONG).show()
                } finally { learningZipBusy.value = false }
            }
        }
    }

    private fun applyStudyImport(imported: List<StudyDay>, overwrite: Boolean) {
        val result = runCatching { studyRepository.importDays(imported, overwrite) }
        result.onSuccess { count ->
            Toast.makeText(requireContext(), "$count çalışma günü içe aktarıldı.",
                Toast.LENGTH_LONG).show()
        }.onFailure {
            Toast.makeText(requireContext(), "İçe aktarılamadı: " +
                (it.message ?: "Kayıt hatası"), Toast.LENGTH_LONG).show()
        }
    }

    private fun confirmUndoStudyImport() {
        if (!studyRepository.canUndoLastImport()) {
            Toast.makeText(requireContext(), "Geri alınacak içe aktarma yok.",
                Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Son içe aktarmayı geri al")
            .setMessage("Çalışma günlerini son içe aktarmadan beri düzenlemediysen önceki kayıtlar geri yüklenir. Sonradan eklenen veya düzenlenen günler varsa geri alma seçeneği kapanır.")
            .setNegativeButton("Vazgeç", null)
            .setPositiveButton("Geri al") { _, _ ->
                val done = studyRepository.undoLastImport()
                Toast.makeText(requireContext(),
                    if (done) "Son içe aktarma geri alındı." else "Geri alma başarısız.",
                    Toast.LENGTH_LONG).show()
            }.show()
    }

    private fun cancelLearningZip() {
        stagedLearningZip?.discard()
        stagedLearningZip = null
    }

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                BackupManager.writeToUri(requireContext().contentResolver, uri, repository.getAllExams())
                Toast.makeText(requireContext(), "Yedek kaydedildi.", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                Toast.makeText(requireContext(), "Yedekleme başarısız.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            when (val result = BackupManager.readFromUri(requireContext().contentResolver, uri)) {
                is BackupManager.ImportResult.Success -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Yedeği içe aktar")
                        .setMessage("Yedekte ${result.exams.size} deneme var. Mevcut ${repository.getAllExams().size} kayıt değiştirilecek. Devam edilsin mi?")
                        .setNegativeButton("Vazgeç", null)
                        .setPositiveButton("İçe aktar") { _, _ ->
                            repository.replaceAll(result.exams)
                            Toast.makeText(requireContext(), "${result.exams.size} deneme içe aktarıldı.", Toast.LENGTH_SHORT).show()
                        }.show()
                }
                is BackupManager.ImportResult.Error ->
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = ExamRepository.getInstance(requireContext())
        studyRepository = StudyRepository(requireContext())
        theme.value = ThemeManager.currentTheme(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val selected = theme.value
                // The saved theme and LocalConfiguration both drive repaint.
                val dark = activeDarkTheme(context)
                YksTheme(darkTheme = dark) {
                    SettingsScreenV2(
                        selectedTheme = selected,
                        onThemeChange = { selected ->
                            if (theme.value != selected) {
                                theme.value = selected
                                ThemeManager.setTheme(requireContext(), selected)
                                (activity as? MainActivity)?.refreshAppearance()
                            }
                        },
                        tyt = AppSettings.getTargetNet(context, ExamType.TYT)?.toString().orEmpty(),
                        ayt = AppSettings.getTargetNet(context, ExamType.AYT)?.toString().orEmpty(),
                        obp = AppSettings.getObp(context)?.toString().orEmpty(),
                        daily = AppSettings.getDailyGoal(context)?.toString().orEmpty(),
                        onSave = { tyt, ayt, obp, daily -> saveGoals(tyt, ayt, obp, daily) },
                        onExport = { exportLauncher.launch("yks-takip-yedek.json") },
                        onImport = { importLauncher.launch(arrayOf("application/json")) },
                        onImportStudy = {
                            importStudy.launch(arrayOf("application/json", "text/plain", "*/*"))
                        },
                        onExportStudy = {
                            exportStudy.launch("yks-calisma-${LocalDate.now()}.json")
                        },
                        onUndoStudyImport = { confirmUndoStudyImport() },
                        learningZipBusy = learningZipBusy.value,
                        onExportLearningZip = {
                            if (!learningZipBusy.value)
                                exportLearningZip.launch("yks-ogrenme-${LocalDate.now()}.zip")
                        },
                        onImportLearningZip = {
                            if (!learningZipBusy.value)
                                importLearningZip.launch(arrayOf(
                                    "application/zip", "application/x-zip-compressed",
                                    "application/octet-stream"
                                ))
                        }
                    )
                }
            }
        }

    override fun onDestroy() {
        cancelLearningZip()
        super.onDestroy()
    }

    private fun saveGoals(tytText: String, aytText: String, obpText: String, dailyText: String): String? {
        fun parse(text: String): Float? = text.trim().replace(',', '.').toFloatOrNull()
        val tyt = parse(tytText)
        val ayt = parse(aytText)
        val obp = parse(obpText)
        val daily = dailyText.trim().toIntOrNull()
        if (tytText.isNotBlank() && (tyt == null || !tyt.isFinite() || tyt !in 0f..120f))
            return "TYT hedefi 0–120 arasında olmalı."
        if (aytText.isNotBlank() && (ayt == null || !ayt.isFinite() || ayt !in 0f..80f))
            return "AYT hedefi 0–80 arasında olmalı."
        if (obpText.isNotBlank() && (obp == null || !obp.isFinite() || obp !in 0f..100f))
            return "OBP 0–100 arasında olmalı."
        if (dailyText.isNotBlank() && (daily == null || daily !in 0..100000))
            return "Günlük soru hedefi geçersiz."
        AppSettings.setTargetNet(requireContext(), ExamType.TYT, tyt)
        AppSettings.setTargetNet(requireContext(), ExamType.AYT, ayt)
        AppSettings.setObp(requireContext(), obp)
        AppSettings.setDailyGoal(requireContext(), daily)
        return null
    }
}
