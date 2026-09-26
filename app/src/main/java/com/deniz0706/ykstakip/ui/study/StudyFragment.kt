package com.deniz0706.ykstakip.ui.study

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.deniz0706.ykstakip.R
import com.deniz0706.ykstakip.data.AppSettings
import com.deniz0706.ykstakip.data.StudyRepository
import com.deniz0706.ykstakip.model.StudyDay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudyFragment : Fragment(R.layout.fragment_study) {

    private lateinit var repository: StudyRepository

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        repository = StudyRepository(requireContext())

        view.findViewById<View>(R.id.btnEditToday).setOnClickListener {
            showTodayEditor()
        }

        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val v = view ?: return

        val today = todayString()
        val studyDay = repository.get(today)

        val questions = studyDay?.questionCount ?: 0
        val minutes = studyDay?.studyMinutes ?: 0
        val goal = AppSettings.getDailyGoal(requireContext())

        v.findViewById<TextView>(R.id.tvStudyQuestions).text =
            questions.toString()

        v.findViewById<TextView>(R.id.tvStudyTime).text =
            formatDuration(minutes)

        v.findViewById<TextView>(R.id.tvStudyGoal).text =
            if (goal != null) {
                "Hedef: $questions / $goal soru"
            } else {
                "Hedef: —"
            }

        renderReport()
    }

    private fun renderReport() {
        val v = view ?: return

        val days = repository.getAll()

        val totalQuestions =
            days.sumOf { it.questionCount }

        val totalMinutes =
            days.sumOf { it.studyMinutes }

        val averageQuestions =
            if (days.isEmpty()) {
                0
            } else {
                totalQuestions / days.size
            }

        val averageMinutes =
            if (days.isEmpty()) {
                0
            } else {
                totalMinutes / days.size
            }

        val goal = AppSettings.getDailyGoal(requireContext())

        val goalDays =
            if (goal != null) {
                days.count { it.questionCount >= goal }
            } else {
                0
            }

        val bestQuestionDay =
            days.maxByOrNull { it.questionCount }

        val bestStudyDay =
            days.maxByOrNull { it.studyMinutes }

        v.findViewById<TextView>(R.id.tvTotalQuestions).text =
            "Toplam soru: $totalQuestions"

        v.findViewById<TextView>(R.id.tvTotalStudyTime).text =
            "Toplam çalışma: ${formatDuration(totalMinutes)}"

        v.findViewById<TextView>(R.id.tvAverageQuestions).text =
            "Günlük ortalama soru: $averageQuestions"

        v.findViewById<TextView>(R.id.tvAverageStudyTime).text =
            "Günlük ortalama çalışma: ${formatDuration(averageMinutes)}"

        v.findViewById<TextView>(R.id.tvGoalDays).text =
            if (goal != null) {
                "Hedef tutan gün: $goalDays"
            } else {
                "Hedef tutan gün: —"
            }

        v.findViewById<TextView>(R.id.tvBestQuestionDay).text =
            if (bestQuestionDay != null) {
                "En çok soru: ${bestQuestionDay.questionCount} (${formatDate(bestQuestionDay.date)})"
            } else {
                "En çok soru: —"
            }

        v.findViewById<TextView>(R.id.tvBestStudyDay).text =
            if (bestStudyDay != null) {
                "En uzun çalışma: ${formatDuration(bestStudyDay.studyMinutes)} (${formatDate(bestStudyDay.date)})"
            } else {
                "En uzun çalışma: —"
            }
    }

    private fun showTodayEditor() {
        val today = todayString()
        val current = repository.get(today)

        val questionInput = EditText(requireContext()).apply {
            hint = "Çözülen soru sayısı"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(current?.questionCount?.toString() ?: "")
            setSelectAllOnFocus(true)
        }

        val timeInput = EditText(requireContext()).apply {
            hint = "Çalışma süresi (dakika)"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(current?.studyMinutes?.toString() ?: "")
            setSelectAllOnFocus(true)
        }

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 0, 48, 0)

            addView(questionInput)

            addView(
                timeInput,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 16
                }
            )
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Bugünkü çalışma")
            .setView(container)
            .setPositiveButton("Kaydet") { _, _ ->

                val questions =
                    questionInput.text.toString().toIntOrNull() ?: 0

                val minutes =
                    timeInput.text.toString().toIntOrNull() ?: 0

                repository.save(
                    StudyDay(
                        date = today,
                        questionCount = questions.coerceAtLeast(0),
                        studyMinutes = minutes.coerceAtLeast(0)
                    )
                )

                refresh()
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun todayString(): String {
        return SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).format(Date())
    }

    private fun formatDate(date: String): String {
        return try {
            val parser = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            )

            val formatter = SimpleDateFormat(
                "d MMMM yyyy",
                Locale("tr", "TR")
            )

            val parsed = parser.parse(date)

            if (parsed != null) {
                formatter.format(parsed)
            } else {
                date
            }
        } catch (e: Exception) {
            date
        }
    }

    private fun formatDuration(minutes: Int): String {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60

        return when {
            hours > 0 && remainingMinutes > 0 ->
                "$hours sa $remainingMinutes dk"

            hours > 0 ->
                "$hours sa"

            else ->
                "$remainingMinutes dk"
        }
    }
}
