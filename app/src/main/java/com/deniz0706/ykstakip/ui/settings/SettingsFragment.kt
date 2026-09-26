package com.deniz0706.ykstakip.ui.settings

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.deniz0706.ykstakip.R
import com.deniz0706.ykstakip.data.AppSettings
import com.deniz0706.ykstakip.data.BackupManager
import com.deniz0706.ykstakip.data.ExamRepository
import com.deniz0706.ykstakip.data.ThemeManager
import com.deniz0706.ykstakip.model.ExamType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.deniz0706.ykstakip.util.Fmt

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var repository: ExamRepository
    private var examDateCalendar: Calendar = Calendar.getInstance()

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
            is BackupManager.ImportResult.Error -> Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = ExamRepository.getInstance(requireContext())

        refreshThemeSelection()

        view.findViewById<View>(R.id.optLight).setOnClickListener { ThemeManager.setTheme(requireContext(), "light"); refreshThemeSelection() }
        view.findViewById<View>(R.id.optDark).setOnClickListener { ThemeManager.setTheme(requireContext(), "dark"); refreshThemeSelection() }
        view.findViewById<View>(R.id.optSystem).setOnClickListener { ThemeManager.setTheme(requireContext(), "system"); refreshThemeSelection() }

        view.findViewById<View>(R.id.btnExport).setOnClickListener { exportLauncher.launch("yks-takip-yedek.json") }
        view.findViewById<View>(R.id.btnImport).setOnClickListener { importLauncher.launch(arrayOf("application/json")) }

        AppSettings.getTargetNet(requireContext(), ExamType.TYT)?.let {
            view.findViewById<EditText>(R.id.etTargetTyt).setText(Fmt.net(it.toDouble()).replace(',', '.'))
        }
        AppSettings.getTargetNet(requireContext(), ExamType.AYT)?.let {
            view.findViewById<EditText>(R.id.etTargetAyt).setText(Fmt.net(it.toDouble()).replace(',', '.'))
        }
        AppSettings.getDailyGoal(requireContext())?.let {
            view.findViewById<EditText>(R.id.etDailyGoal).setText(it.toString())
        }
        AppSettings.getExamDateMillis(requireContext())?.let { millis ->
            examDateCalendar.timeInMillis = millis
        }
        updateExamDateLabel()

        view.findViewById<View>(R.id.tvExamDate).setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, y, m, d -> examDateCalendar.set(y, m, d); updateExamDateLabel() },
                examDateCalendar.get(Calendar.YEAR), examDateCalendar.get(Calendar.MONTH), examDateCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        view.findViewById<View>(R.id.btnSaveGoals).setOnClickListener { saveGoals() }
    }

    private fun updateExamDateLabel() {
        view?.findViewById<TextView>(R.id.tvExamDate)?.text =
            "YKS tarihi: " + SimpleDateFormat("d MMMM yyyy", Locale("tr", "TR")).format(examDateCalendar.time)
    }

    private fun saveGoals() {
        val v = view ?: return
        val targetTyt = v.findViewById<EditText>(R.id.etTargetTyt).text.toString().replace(',', '.').toFloatOrNull()
        val targetAyt = v.findViewById<EditText>(R.id.etTargetAyt).text.toString().replace(',', '.').toFloatOrNull()
        val dailyGoal = v.findViewById<EditText>(R.id.etDailyGoal).text.toString().toIntOrNull()

        AppSettings.setTargetNet(requireContext(), ExamType.TYT, targetTyt)
        AppSettings.setTargetNet(requireContext(), ExamType.AYT, targetAyt)
        AppSettings.setDailyGoal(requireContext(), dailyGoal)
        AppSettings.setExamDateMillis(requireContext(), examDateCalendar.timeInMillis)

        Toast.makeText(requireContext(), "Hedefler kaydedildi.", Toast.LENGTH_SHORT).show()
    }

    private fun refreshThemeSelection() {
        val v = view ?: return
        val current = ThemeManager.currentTheme(requireContext())
        v.findViewById<View>(R.id.optLight).isSelected = current == "light"
        v.findViewById<View>(R.id.optDark).isSelected = current == "dark"
        v.findViewById<View>(R.id.optSystem).isSelected = current == "system"
    }
}
