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
import com.deniz0706.ykstakip.ui.statistics.LineChartView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StudyFragment : Fragment(R.layout.fragment_study) {

    private lateinit var repository: StudyRepository

    private var chartMetric = "questions"
    private var chartRange = "7"

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        repository = StudyRepository(requireContext())

        view.findViewById<View>(R.id.btnEditToday).setOnClickListener {
            showTodayEditor()
        }

        view.findViewById<View>(R.id.chartMetricQuestions).setOnClickListener {
            chartMetric = "questions"
            refresh()
        }

        view.findViewById<View>(R.id.chartMetricTime).setOnClickListener {
            chartMetric = "time"
            refresh()
        }

        view.findViewById<View>(R.id.chartRange7).setOnClickListener {
            chartRange = "7"
            refresh()
        }

        view.findViewById<View>(R.id.chartRange30).setOnClickListener {
            chartRange = "30"
            refresh()
        }

        view.findViewById<View>(R.id.chartRangeAll).setOnClickListener {
            chartRange = "all"
            refresh()
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

        refreshChartSelection()
        renderChart()
        renderReport()
    }

    private fun refreshChartSelection() {
        val v = view ?: return

        v.findViewById<View>(R.id.chartMetricQuestions).isSelected =
            chartMetric == "questions"

        v.findViewById<View>(R.id.chartMetricTime).isSelected =
            chartMetric == "time"

        v.findViewById<View>(R.id.chartRange7).isSelected =
            chartRange == "7"

        v.findViewById<View>(R.id.chartRange30).isSelected =
            chartRange == "30"

        v.findViewById<View>(R.id.chartRangeAll).isSelected =
            chartRange == "all"
    }

    private fun renderChart() {
        val v = view ?: return

        val chart = v.findViewById<LineChartView>(R.id.studyChart)
        val emptyText = v.findViewById<TextView>(R.id.tvStudyChartEmpty)

        val days = repository.getAll()

        if (days.isEmpty()) {
            chart.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
            return
        }

        val points = buildChartPoints(days)

        if (points.isEmpty()) {
            chart.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
            return
        }

        chart.visibility = View.VISIBLE
        emptyText.visibility = View.GONE

        chart.points = points
        chart.targetValue = null
    }

    private fun buildChartPoints(days: List<StudyDay>): List<LineChartView.Point> {
        val dayMap = days.associateBy { it.date }

        val dates = when (chartRange) {
            "7" -> datesForLastDays(7)
            "30" -> datesForLastDays(30)
            else -> {
                val firstDate = days.minOfOrNull { it.date } ?: return emptyList()
                datesBetween(firstDate, todayString())
            }
        }

        val df = SimpleDateFormat(
            "d MMM",
            Locale("tr", "TR")
        )

        val dfFull = SimpleDateFormat(
            "d MMMM yyyy",
            Locale("tr", "TR")
        )

        val parser = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        )

        return dates.mapNotNull { dateString ->
            val date = try {
                parser.parse(dateString)
            } catch (e: Exception) {
                null
            } ?: return@mapNotNull null

            val day = dayMap[dateString]

            val value = if (chartMetric == "questions") {
                (day?.questionCount ?: 0).toFloat()
            } else {
                (day?.studyMinutes ?: 0).toFloat()
            }

            LineChartView.Point(
                xLabel = df.format(date),
                value = value,
                title = if (chartMetric == "questions") {
                    "${day?.questionCount ?: 0} soru"
                } else {
                    formatDuration(day?.studyMinutes ?: 0)
                },
                fullDate = dfFull.format(date),
                unit = if (chartMetric == "questions") {
                    ""
                } else {
                    ""
                }
            )
        }
    }

    private fun datesForLastDays(count: Int): List<String> {
        val calendar = Calendar.getInstance()

        calendar.add(Calendar.DAY_OF_YEAR, -(count - 1))

        val result = mutableListOf<String>()

        repeat(count) {
            result.add(formatDateKey(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return result
    }

    private fun datesBetween(
        startDate: String,
        endDate: String
    ): List<String> {
        val parser = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        )

        val start = try {
            parser.parse(startDate)
        } catch (e: Exception) {
            null
        } ?: return emptyList()

        val end = try {
            parser.parse(endDate)
        } catch (e: Exception) {
            null
        } ?: return emptyList()

        val calendar = Calendar.getInstance().apply {
            time = start
        }

        val endCalendar = Calendar.getInstance().apply {
            time = end
        }

        val result = mutableListOf<String>()

        while (!calendar.after(endCalendar)) {
            result.add(formatDateKey(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return result
    }

    private fun formatDateKey(date: Date): String {
        return SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).format(date)
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
