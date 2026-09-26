package com.deniz0706.ykstakip.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.deniz0706.ykstakip.R
import com.deniz0706.ykstakip.data.BackupManager
import com.deniz0706.ykstakip.data.ExamRepository
import com.deniz0706.ykstakip.data.ThemeManager

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var repository: ExamRepository

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@registerForActivityResult
        try {
            BackupManager.writeToUri(requireContext().contentResolver, uri, repository.getAllExams())
            Toast.makeText(requireContext(), "Yedek kaydedildi.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Yedekleme başarısız.", Toast.LENGTH_SHORT).show()
        }
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        when (val result = BackupManager.readFromUri(requireContext().contentResolver, uri)) {
            is BackupManager.ImportResult.Success -> {
                repository.replaceAll(result.exams)
                Toast.makeText(requireContext(), "${result.exams.size} deneme içe aktarıldı.", Toast.LENGTH_SHORT).show()
            }
            is BackupManager.ImportResult.Error -> {
                Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = ExamRepository.getInstance(requireContext())

        refreshThemeSelection()

        view.findViewById<View>(R.id.optLight).setOnClickListener {
            ThemeManager.setTheme(requireContext(), "light"); refreshThemeSelection()
        }
        view.findViewById<View>(R.id.optDark).setOnClickListener {
            ThemeManager.setTheme(requireContext(), "dark"); refreshThemeSelection()
        }
        view.findViewById<View>(R.id.optSystem).setOnClickListener {
            ThemeManager.setTheme(requireContext(), "system"); refreshThemeSelection()
        }

        view.findViewById<View>(R.id.btnExport).setOnClickListener {
            exportLauncher.launch("yks-takip-yedek.json")
        }
        view.findViewById<View>(R.id.btnImport).setOnClickListener {
            importLauncher.launch(arrayOf("application/json"))
        }
    }

    private fun refreshThemeSelection() {
        val v = view ?: return
        val current = ThemeManager.currentTheme(requireContext())
        v.findViewById<View>(R.id.optLight).isSelected = current == "light"
        v.findViewById<View>(R.id.optDark).isSelected = current == "dark"
        v.findViewById<View>(R.id.optSystem).isSelected = current == "system"
    }
}
