package com.deniz0706.ykstakip.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.deniz0706.ykstakip.R
import com.deniz0706.ykstakip.data.AppSettings
import com.deniz0706.ykstakip.data.BackupManager
import com.deniz0706.ykstakip.data.ExamRepository
import com.deniz0706.ykstakip.data.ThemeManager
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.ui.MainActivity
import com.deniz0706.ykstakip.util.Fmt
import com.deniz0706.ykstakip.util.ThemeTransitionHelper

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var repository: ExamRepository

    private val exportLauncher =
        registerForActivityResult(
            ActivityResultContracts.CreateDocument("application/json")
        ) { uri ->
            if (uri == null) return@registerForActivityResult

            try {
                BackupManager.writeToUri(
                    requireContext().contentResolver,
                    uri,
                    repository.getAllExams()
                )

                Toast.makeText(
                    requireContext(),
                    "Yedek kaydedildi.",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Yedekleme başarısız.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val importLauncher =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri == null) return@registerForActivityResult

            when (
                val result = BackupManager.readFromUri(
                    requireContext().contentResolver,
                    uri
                )
            ) {
                is BackupManager.ImportResult.Success -> {
                    androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Yedeği içe aktar")
                        .setMessage(
                            "Bu yedekte ${result.exams.size} deneme var. " +
                                "Mevcut ${repository.getAllExams().size} deneme kaydının tamamı " +
                                "yedekteki kayıtlarla değiştirilecek. Devam edilsin mi?"
                        )
                        .setNegativeButton("Vazgeç", null)
                        .setPositiveButton("İçe aktar") { _, _ ->
                            repository.replaceAll(result.exams)
                            Toast.makeText(
                                requireContext(),
                                "${result.exams.size} deneme içe aktarıldı.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .show()
                }

                is BackupManager.ImportResult.Error -> {
                    Toast.makeText(
                        requireContext(),
                        result.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        repository = ExamRepository.getInstance(requireContext())

        refreshThemeSelection()

        view.findViewById<View>(R.id.optLight).setOnClickListener {
            (activity as? MainActivity)?.let { ThemeTransitionHelper.captureSnapshot(it) }
            ThemeManager.setTheme(requireContext(), "light")
            refreshThemeSelection()
        }

        view.findViewById<View>(R.id.optDark).setOnClickListener {
            (activity as? MainActivity)?.let { ThemeTransitionHelper.captureSnapshot(it) }
            ThemeManager.setTheme(requireContext(), "dark")
            refreshThemeSelection()
        }

        view.findViewById<View>(R.id.optSystem).setOnClickListener {
            (activity as? MainActivity)?.let { ThemeTransitionHelper.captureSnapshot(it) }
            ThemeManager.setTheme(requireContext(), "system")
            refreshThemeSelection()
        }

        view.findViewById<View>(R.id.btnExport).setOnClickListener {
            exportLauncher.launch("yks-takip-yedek.json")
        }

        view.findViewById<View>(R.id.btnImport).setOnClickListener {
            importLauncher.launch(arrayOf("application/json"))
        }

        AppSettings.getTargetNet(
            requireContext(),
            ExamType.TYT
        )?.let {
            view.findViewById<EditText>(R.id.etTargetTyt)
                .setText(
                    Fmt.net(it.toDouble()).replace(',', '.')
                )
        }

        AppSettings.getTargetNet(
            requireContext(),
            ExamType.AYT
        )?.let {
            view.findViewById<EditText>(R.id.etTargetAyt)
                .setText(
                    Fmt.net(it.toDouble()).replace(',', '.')
                )
        }

        AppSettings.getDailyGoal(requireContext())?.let {
            view.findViewById<EditText>(R.id.etDailyGoal)
                .setText(it.toString())
        }

        AppSettings.getObp(requireContext())?.let {
            view.findViewById<EditText>(R.id.etObp)
                .setText(
                    Fmt.net(it.toDouble()).replace(',', '.')
                )
        }

        view.findViewById<View>(R.id.btnSaveGoals).setOnClickListener {
            saveGoals()
        }
    }

    private fun refreshThemeSelection() {
        val context = requireContext()
        val current = ThemeManager.currentTheme(context)

        view?.findViewById<View>(R.id.optLight)?.isSelected =
            current == "light"

        view?.findViewById<View>(R.id.optDark)?.isSelected =
            current == "dark"

        view?.findViewById<View>(R.id.optSystem)?.isSelected =
            current == "system"
    }

    private fun saveGoals() {
        val v = view ?: return

        val tytText =
            v.findViewById<EditText>(R.id.etTargetTyt)
                .text
                .toString()
                .trim()

        val aytText =
            v.findViewById<EditText>(R.id.etTargetAyt)
                .text
                .toString()
                .trim()

        val dailyGoalText =
            v.findViewById<EditText>(R.id.etDailyGoal)
                .text
                .toString()
                .trim()

        val obpText =
            v.findViewById<EditText>(R.id.etObp)
                .text
                .toString()
                .trim()

        val tyt = tytText.replace(',', '.').toFloatOrNull()
        val ayt = aytText.replace(',', '.').toFloatOrNull()
        val dailyGoal = dailyGoalText.toIntOrNull()
        val obp = obpText.replace(',', '.').toFloatOrNull()

        fun invalid(fieldId: Int, message: String): Boolean {
            v.findViewById<EditText>(fieldId).apply {
                error = message
                requestFocus()
            }
            return true
        }

        if (tytText.isNotEmpty() && (tyt == null || !tyt.isFinite() || tyt !in 0f..120f)) {
            invalid(R.id.etTargetTyt, "TYT hedefi 0–120 arasında olmalı")
            return
        }
        if (aytText.isNotEmpty() && (ayt == null || !ayt.isFinite() || ayt !in 0f..80f)) {
            invalid(R.id.etTargetAyt, "AYT hedefi 0–80 arasında olmalı")
            return
        }
        if (dailyGoalText.isNotEmpty() && (dailyGoal == null || dailyGoal !in 0..100000)) {
            invalid(R.id.etDailyGoal, "Geçerli bir soru hedefi gir")
            return
        }
        if (obpText.isNotEmpty() && (obp == null || !obp.isFinite() || obp !in 0f..100f)) {
            invalid(R.id.etObp, "OBP 0–100 arasında olmalı")
            return
        }

        AppSettings.setTargetNet(
            requireContext(),
            ExamType.TYT,
            tyt
        )

        AppSettings.setTargetNet(
            requireContext(),
            ExamType.AYT,
            ayt
        )

        AppSettings.setDailyGoal(
            requireContext(),
            dailyGoal?.takeIf { it >= 0 }
        )

        AppSettings.setObp(
            requireContext(),
            obp
        )

        Toast.makeText(
            requireContext(),
            "Hedefler kaydedildi.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
