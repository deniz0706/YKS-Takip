package com.deniz0706.ykstakip.ui.study

import android.content.res.Resources
import android.graphics.Paint
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.deniz0706.ykstakip.R
import com.deniz0706.ykstakip.data.AppSettings
import com.deniz0706.ykstakip.data.StudyPlanRepository
import com.deniz0706.ykstakip.data.StudyRepository
import com.deniz0706.ykstakip.model.StudyDay
import com.deniz0706.ykstakip.model.StudyPlanItem
import com.deniz0706.ykstakip.ui.statistics.LineChartView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StudyFragment : Fragment(R.layout.fragment_study) {

    private lateinit var repository: StudyRepository
    private lateinit var planRepository: StudyPlanRepository

    private var chartMetric = "questions"
    private var chartRange = "7"

    private var selectedPlanDay =
        Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        repository = StudyRepository(requireContext())
        planRepository = StudyPlanRepository(requireContext())

        setupStudyPlan(view)

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

        if (::planRepository.isInitialized) {
            renderPlan()
        }
    }

    private fun setupStudyPlan(view: View) {
        renderPlanDaySelector(view)

        view.findViewById<View>(R.id.btnAddPlan).setOnClickListener {
            showAddPlanDialog()
        }

        renderPlan()
    }

    private fun renderPlanDaySelector(view: View) {
        val container =
            view.findViewById<LinearLayout>(R.id.planDaySelector)

        container.removeAllViews()

        val days = listOf(
            Calendar.MONDAY to "Pzt",
            Calendar.TUESDAY to "Sal",
            Calendar.WEDNESDAY to "Çar",
            Calendar.THURSDAY to "Per",
            Calendar.FRIDAY to "Cum",
            Calendar.SATURDAY to "Cmt",
            Calendar.SUNDAY to "Paz"
        )

        days.forEach { (day, label) ->

            val button = TextView(requireContext()).apply {
                text = label
                gravity = Gravity.CENTER
                textSize = 12f

                setPadding(
                    4.dp,
                    4.dp,
                    4.dp,
                    4.dp
                )

                background = resources.getDrawable(
                    R.drawable.bg_pill_toggle,
                    requireContext().theme
                )

                isSelected = selectedPlanDay == day

                setOnClickListener {
                    selectedPlanDay = day
                    renderPlanDaySelector(view)
                    renderPlan()
                }

                layoutParams = LinearLayout.LayoutParams(
                    0,
                    42.dp,
                    1f
                ).apply {
                    marginStart = 3.dp
                    marginEnd = 3.dp
                }
            }

            container.addView(button)
        }
    }

    private fun renderPlan() {
        val v = view ?: return

        val container =
            v.findViewById<LinearLayout>(R.id.planList)

        container.removeAllViews()

        val items =
            planRepository.getForDay(selectedPlanDay)

        if (items.isEmpty()) {
            val empty = TextView(requireContext()).apply {
                text = "Bu gün için ders programı yok."
                textSize = 14f

                setPadding(
                    8.dp,
                    16.dp,
                    8.dp,
                    16.dp
                )

                setTextColor(
                    resources.getColor(
                        R.color.on_surface_secondary,
                        requireContext().theme
                    )
                )
            }

            container.addView(empty)
            return
        }

        items.forEach { item ->
            container.addView(createPlanView(item))
        }
    }

    private fun createPlanView(item: StudyPlanItem): View {

        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            setPadding(
                14.dp,
                12.dp,
                14.dp,
                12.dp
            )

            background = resources.getDrawable(
                R.drawable.bg_card,
                requireContext().theme
            )

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8.dp
            }
        }

        val check = CheckBox(requireContext()).apply {
            isChecked = item.completed

            setOnClickListener {
                planRepository.toggleCompleted(item.id)
                renderPlan()
            }
        }

        row.addView(check)

        val textContainer =
            LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL

                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

        val title = TextView(requireContext()).apply {
            text = if (item.topic.isBlank()) {
                item.subject
            } else {
                "${item.subject} — ${item.topic}"
            }

            textSize = 15f

            setTextColor(
                resources.getColor(
                    R.color.on_background,
                    requireContext().theme
                )
            )

            if (item.completed) {
                paintFlags =
                    paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
        }

        val time = TextView(requireContext()).apply {
            text = "${item.startTime} - ${item.endTime}"
            textSize = 12f

            setTextColor(
                resources.getColor(
                    R.color.on_surface_secondary,
                    requireContext().theme
                )
            )
        }

        textContainer.addView(title)
        textContainer.addView(time)

        row.addView(textContainer)

        row.setOnLongClickListener {
            showDeletePlanDialog(item)
            true
        }

        return row
    }

    private fun showAddPlanDialog() {

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                48,
                0,
                48,
                0
            )
        }

        val subjectInput = EditText(requireContext()).apply {
            hint = "Ders (örn. AYT Matematik)"
            inputType = InputType.TYPE_CLASS_TEXT
        }

        val topicInput = EditText(requireContext()).apply {
            hint = "Konu (örn. Fonksiyon)"
            inputType = InputType.TYPE_CLASS_TEXT
        }

        val startInput = EditText(requireContext()).apply {
            hint = "Başlangıç (örn. 10:00)"
            inputType = InputType.TYPE_CLASS_DATETIME
        }

        val endInput = EditText(requireContext()).apply {
            hint = "Bitiş (örn. 11:30)"
            inputType = InputType.TYPE_CLASS_DATETIME
        }

        container.addView(subjectInput)

        container.addView(
            topicInput,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 12
            }
        )

        container.addView(
            startInput,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 12
            }
        )

        container.addView(
            endInput,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 12
            }
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Ders Ekle")
            .setView(container)
            .setPositiveButton("Ekle") { _, _ ->

                val subject =
                    subjectInput.text.toString().trim()

                val topic =
                    topicInput.text.toString().trim()

                val start =
                    startInput.text.toString().trim()

                val end =
                    endInput.text.toString().trim()

                if (
                    subject.isEmpty() ||
                    start.isEmpty() ||
                    end.isEmpty()
                ) {
                    return@setPositiveButton
                }

                planRepository.save(
                    StudyPlanItem(
                        id = System.currentTimeMillis(),
                        dayOfWeek = selectedPlanDay,
                        startTime = start,
                        endTime = end,
                        subject = subject,
                        topic = topic,
                        completed = false
                    )
                )

                renderPlan()
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun showDeletePlanDialog(
        item: StudyPlanItem
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle("Dersi Sil")
            .setMessage(
                "${item.subject} dersini programdan silmek istiyor musun?"
            )
            .setPositiveButton("Sil") { _, _ ->
                planRepository.delete(item.id)
                renderPlan()
            }
            .setNegativeButton("İptal", null)
            .show()
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

        val chart =
            v.findViewById<LineChartView>(R.id.studyChart)

        val emptyText =
            v.findViewById<TextView>(R.id.tvStudyChartEmpty)

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

    private fun buildChartPoints(
        days: List<StudyDay>
    ): List<LineChartView.Point> {

        val dayMap =
            days.associateBy { it.date }

        val dates = when (chartRange) {
            "7" -> datesForLastDays(7)

            "30" -> datesForLastDays(30)

            else -> {
                val firstDate =
                    days.minOfOrNull { it.date }
                        ?: return emptyList()

                datesBetween(
                    firstDate,
                    todayString()
                )
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

            val value =
                if (chartMetric == "questions") {
                    (day?.questionCount ?: 0).toFloat()
                } else {
                    (day?.studyMinutes ?: 0).toFloat()
                }

            LineChartView.Point(
                xLabel = df.format(date),
                value = value,
                title =
                    if (chartMetric == "questions") {
                        "${day?.questionCount ?: 0} soru"
                    } else {
                        formatDuration(
                            day?.studyMinutes ?: 0
                        )
                    },
                fullDate = dfFull.format(date),
                unit = ""
            )
        }
    }

    private fun datesForLastDays(
        count: Int
    ): List<String> {

        val calendar = Calendar.getInstance()

        calendar.add(
            Calendar.DAY_OF_YEAR,
            -(count - 1)
        )

        val result = mutableListOf<String>()

        repeat(count) {
            result.add(
                formatDateKey(calendar.time)
            )

            calendar.add(
                Calendar.DAY_OF_YEAR,
                1
            )
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

        val calendar =
            Calendar.getInstance().apply {
                time = start
            }

        val endCalendar =
            Calendar.getInstance().apply {
                time = end
            }

        val result = mutableListOf<String>()

        while (!calendar.after(endCalendar)) {

            result.add(
                formatDateKey(calendar.time)
            )

            calendar.add(
                Calendar.DAY_OF_YEAR,
                1
            )
        }

        return result
    }

    private fun formatDateKey(
        date: Date
    ): String {

        return SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).format(date)
    }

    private fun showTodayEditor() {

        val today = todayString()
        val current = repository.get(today)

        val questionInput =
            EditText(requireContext()).apply {

                hint = "Çözülen soru sayısı"
                inputType =
                    InputType.TYPE_CLASS_NUMBER

                setText(
                    current?.questionCount?.toString()
                        ?: ""
                )

                setSelectAllOnFocus(true)
            }

        val timeInput =
            EditText(requireContext()).apply {

                hint = "Çalışma süresi (dakika)"
                inputType =
                    InputType.TYPE_CLASS_NUMBER

                setText(
                    current?.studyMinutes?.toString()
                        ?: ""
                )

                setSelectAllOnFocus(true)
            }

        val container =
            LinearLayout(requireContext()).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    48,
                    0,
                    48,
                    0
                )

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
                    questionInput.text
                        .toString()
                        .toIntOrNull()
                        ?: 0

                val minutes =
                    timeInput.text
                        .toString()
                        .toIntOrNull()
                        ?: 0

                repository.save(
                    StudyDay(
                        date = today,
                        questionCount =
                            questions.coerceAtLeast(0),
                        studyMinutes =
                            minutes.coerceAtLeast(0)
                    )
                )

                refresh()
            }
            .setNegativeButton(
                "İptal",
                null
            )
            .show()
    }

    private fun renderReport() {

        val v = view ?: return

        val days = repository.getAll()

        val totalQuestions =
            days.sumOf {
                it.questionCount
            }

        val totalMinutes =
            days.sumOf {
                it.studyMinutes
            }

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

        val goal =
            AppSettings.getDailyGoal(
                requireContext()
            )

        val goalDays =
            if (goal != null) {
                days.count {
                    it.questionCount >= goal
                }
            } else {
                0
            }

        val bestQuestionDay =
            days.maxByOrNull {
                it.questionCount
            }

        val bestStudyDay =
            days.maxByOrNull {
                it.studyMinutes
            }

        v.findViewById<TextView>(
            R.id.tvTotalQuestions
        ).text =
            "Toplam soru: $totalQuestions"

        v.findViewById<TextView>(
            R.id.tvTotalStudyTime
        ).text =
            "Toplam çalışma: ${
                formatDuration(totalMinutes)
            }"

        v.findViewById<TextView>(
            R.id.tvAverageQuestions
        ).text =
            "Günlük ortalama soru: $averageQuestions"

        v.findViewById<TextView>(
            R.id.tvAverageStudyTime
        ).text =
            "Günlük ortalama çalışma: ${
                formatDuration(averageMinutes)
            }"

        v.findViewById<TextView>(
            R.id.tvGoalDays
        ).text =
            if (goal != null) {
                "Hedef tutan gün: $goalDays"
            } else {
                "Hedef tutan gün: —"
            }

        v.findViewById<TextView>(
            R.id.tvBestQuestionDay
        ).text =
            if (bestQuestionDay != null) {
                "En çok soru: ${
                    bestQuestionDay.questionCount
                } (${
                    formatDate(
                        bestQuestionDay.date
                    )
                })"
            } else {
                "En çok soru: —"
            }

        v.findViewById<TextView>(
            R.id.tvBestStudyDay
        ).text =
            if (bestStudyDay != null) {
                "En uzun çalışma: ${
                    formatDuration(
                        bestStudyDay.studyMinutes
                    )
                } (${
                    formatDate(
                        bestStudyDay.date
                    )
                })"
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

    private fun formatDate(
        date: String
    ): String {

        return try {

            val parser =
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.US
                )

            val formatter =
                SimpleDateFormat(
                    "d MMMM yyyy",
                    Locale("tr", "TR")
                )

            val parsed =
                parser.parse(date)

            if (parsed != null) {
                formatter.format(parsed)
            } else {
                date
            }

        } catch (e: Exception) {
            date
        }
    }

    private fun formatDuration(
        minutes: Int
    ): String {

        val hours = minutes / 60
        val remainingMinutes =
            minutes % 60

        return when {

            hours > 0 &&
                    remainingMinutes > 0 ->
                "$hours sa $remainingMinutes dk"

            hours > 0 ->
                "$hours sa"

            else ->
                "$remainingMinutes dk"
        }
    }
}

private val Int.dp: Int
    get() = (
        this *
            Resources.getSystem()
                .displayMetrics
                .density
        ).toInt()
