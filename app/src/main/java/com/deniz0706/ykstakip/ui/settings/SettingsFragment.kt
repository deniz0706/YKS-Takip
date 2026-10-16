package com.deniz0706.ykstakip.ui.settings

import android.os.Bundle
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
import com.deniz0706.ykstakip.ui.MainActivity
import com.deniz0706.ykstakip.ui.theme.YksTheme
import com.deniz0706.ykstakip.ui.theme.activeDarkTheme

class SettingsFragment : Fragment() {
    private lateinit var repository: ExamRepository
    private val theme = mutableStateOf("system")

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
                                // The Activity now handles uiMode itself rather than recreating.
                            }
                        },
                        tyt = AppSettings.getTargetNet(context, ExamType.TYT)?.toString().orEmpty(),
                        ayt = AppSettings.getTargetNet(context, ExamType.AYT)?.toString().orEmpty(),
                        obp = AppSettings.getObp(context)?.toString().orEmpty(),
                        daily = AppSettings.getDailyGoal(context)?.toString().orEmpty(),
                        onSave = { tyt, ayt, obp, daily -> saveGoals(tyt, ayt, obp, daily) },
                        onExport = { exportLauncher.launch("yks-takip-yedek.json") },
                        onImport = { importLauncher.launch(arrayOf("application/json")) }
                    )
                }
            }
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
