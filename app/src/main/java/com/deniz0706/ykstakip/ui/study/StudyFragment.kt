package com.deniz0706.ykstakip.ui.study

import android.os.Bundle
import android.app.DatePickerDialog
import java.util.Calendar
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.deniz0706.ykstakip.R
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.deniz0706.ykstakip.data.AppSettings
import com.deniz0706.ykstakip.data.StudyPlanRepository
import com.deniz0706.ykstakip.data.StudyRepository
import com.deniz0706.ykstakip.data.ThemeManager
import com.deniz0706.ykstakip.model.StudyDay
import com.deniz0706.ykstakip.model.StudyPlanItem
import com.deniz0706.ykstakip.ui.theme.YksTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudyFragment : Fragment() {
    private lateinit var repository: StudyRepository
    private lateinit var planRepository: StudyPlanRepository
    private val days = mutableStateOf<List<StudyDay>>(emptyList())
    private val plan = mutableStateOf<List<StudyPlanItem>>(emptyList())
    private val goal = mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = StudyRepository(requireContext())
        planRepository = StudyPlanRepository(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val dark = when (ThemeManager.currentTheme(context)) {
                    "dark" -> true
                    "light" -> false
                    else -> isSystemInDarkTheme()
                }
                YksTheme(darkTheme = dark) {
                    StudyScreenV2(
                        days = days.value,
                        plan = plan.value,
                        goal = goal.value,
                        onEditToday = { editDay(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) },
                        onFocusSaved = { refresh() },
                        onEditDay = { editDay(it) },
                        onPickDay = { pickDay() },
                        onAddPlan = { addPlan(it) },
                        onEditPlan = { editPlan(it) },
                        onTogglePlan = { planRepository.toggleCompleted(it); refresh() },
                        onDeletePlan = { deletePlan(it) }
                    )
                }
            }
        }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        days.value = repository.getAll()
        plan.value = planRepository.getAll()
        goal.value = AppSettings.getDailyGoal(requireContext())
    }

    private fun input(hintText: String, initial: String = "", number: Boolean = false): EditText =
        EditText(requireContext()).apply {
            // The study editor is a native Android dialog, not Compose.
            // Resolve user-selected DayNight colors explicitly instead of
            // relying on the platform's default (often black) EditText style.
            setTextColor(ContextCompat.getColor(context, R.color.on_background))
            setHintTextColor(ContextCompat.getColor(context, R.color.on_surface_secondary))
            hint = hintText
            setText(initial)
            isSingleLine = true
            inputType = if (number) InputType.TYPE_CLASS_NUMBER else InputType.TYPE_CLASS_TEXT
        }

    private fun form(vararg fields: EditText): LinearLayout =
        LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (24 * resources.displayMetrics.density).toInt()
            setPadding(pad, 0, pad, 0)
            fields.forEach { addView(it) }
        }

    private fun editDay(date: String) {
        val current = repository.get(date)
        val q = input("Soru sayısı", current?.questionCount?.toString().orEmpty(), true)
        val m = input("Çalışma dakikası", current?.studyMinutes?.toString().orEmpty(), true)
        val dialog = AlertDialog.Builder(requireContext()).setTitle("$date · Çalışma kaydı")
            .setView(form(q, m)).setNegativeButton("Vazgeç", null)
            .setPositiveButton("Kaydet", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val questions = q.text.toString().toIntOrNull()
                val minutes = m.text.toString().toIntOrNull()
                if (questions == null || questions !in 0..100000) {
                    q.error = "0–100000 arasında bir sayı gir"
                    return@setOnClickListener
                }
                if (minutes == null || minutes !in 0..1440) {
                    m.error = "0–1440 arasında dakika gir"
                    return@setOnClickListener
                }
                repository.save(StudyDay(date, questions, minutes))
                refresh()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun pickDay() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            val key = "%04d-%02d-%02d".format(Locale.US, year, month + 1, day)
            editDay(key)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    private fun addPlan(day: Int) = showPlanEditor(day, null)

    private fun editPlan(item: StudyPlanItem) = showPlanEditor(item.dayOfWeek, item)

    private fun showPlanEditor(day: Int, existing: StudyPlanItem?) {
        val subject = input("Ders (örn. AYT Matematik)", existing?.subject.orEmpty())
        val topic = input("Konu", existing?.topic.orEmpty())
        val start = input("Başlangıç (09:00)", existing?.startTime ?: "09:00")
        val end = input("Bitiş (10:30)", existing?.endTime ?: "10:30")
        val editing = existing != null
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (editing) "Dersi düzenle" else "Yeni ders")
            .setView(form(subject, topic, start, end))
            .setNegativeButton("Vazgeç", null)
            .setPositiveButton(if (editing) "Değişiklikleri kaydet" else "Ekle", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val a = start.text.toString().trim()
                val b = end.text.toString().trim()
                val pattern = Regex("""^(?:[01][0-9]|2[0-3]):[0-5][0-9]$""")
                if (subject.text.isBlank()) {
                    subject.error = "Ders adı gerekli"
                    return@setOnClickListener
                }
                if (!pattern.matches(a)) {
                    start.error = "HH:mm biçiminde saat gir"
                    return@setOnClickListener
                }
                if (!pattern.matches(b) || a >= b) {
                    end.error = "Başlangıçtan sonraki bir saat gir"
                    return@setOnClickListener
                }
                val clash = planRepository.getForDay(day).any { other ->
                    other.id != existing?.id && a < other.endTime && b > other.startTime
                }
                if (clash) {
                    start.error = "Başka bir dersle saatler çakışıyor"
                    return@setOnClickListener
                }
                planRepository.save(StudyPlanItem(
                    id = existing?.id ?: System.currentTimeMillis(),
                    dayOfWeek = day,
                    startTime = a,
                    endTime = b,
                    subject = subject.text.toString().trim(),
                    topic = topic.text.toString().trim(),
                    completed = existing?.completed ?: false,
                    completedWeek = existing?.completedWeek
                ))
                refresh()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun deletePlan(item: StudyPlanItem) {
        AlertDialog.Builder(requireContext()).setTitle("Dersi sil")
            .setMessage("${item.subject} programdan silinsin mi?")
            .setNegativeButton("Vazgeç", null)
            .setPositiveButton("Sil") { _, _ ->
                planRepository.delete(item.id)
                refresh()
            }.show()
    }
}
