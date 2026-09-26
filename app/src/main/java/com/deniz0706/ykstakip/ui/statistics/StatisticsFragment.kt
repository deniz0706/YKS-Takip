package com.deniz0706.ykstakip.ui.statistics

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.deniz0706.ykstakip.R
import com.deniz0706.ykstakip.data.ExamRepository
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.model.SubjectConfigs
import com.deniz0706.ykstakip.util.Fmt
import java.text.SimpleDateFormat
import java.util.Locale

class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private lateinit var repository: ExamRepository

    private var examType = ExamType.TYT
    private var mode = "toplam" // toplam | branş
    private var branch: String? = null
    private var metricKey = "net"
    private var range = "10"

    private val totalMetrics = listOf(
        "net" to "Toplam Net", "correct" to "Toplam Doğru", "wrong" to "Toplam Yanlış",
        "blank" to "Toplam Boş", "time" to "Toplam Süre", "mpq" to "Genel Dakika/Soru"
    )
    private val branchMetrics = listOf(
        "net" to "Net", "correct" to "Doğru", "wrong" to "Yanlış",
        "blank" to "Boş", "time" to "Süre", "mpq" to "Dakika/Soru"
    )

    private val changeListener = { refresh() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = ExamRepository.getInstance(requireContext())

        view.findViewById<View>(R.id.toggleTyt).setOnClickListener { examType = ExamType.TYT; branch = null; refresh() }
        view.findViewById<View>(R.id.toggleAyt).setOnClickListener { examType = ExamType.AYT; branch = null; refresh() }
        view.findViewById<View>(R.id.modeToplam).setOnClickListener { mode = "toplam"; refresh() }
        view.findViewById<View>(R.id.modeBranch).setOnClickListener {
            mode = "branş"
            if (branch == null) branch = SubjectConfigs.subjectsFor(examType).first().name
            refresh()
        }
        view.findViewById<View>(R.id.selectorBranch).setOnClickListener { showBranchPicker() }
        view.findViewById<View>(R.id.selectorMetric).setOnClickListener { showMetricPicker() }

        view.findViewById<View>(R.id.range5).setOnClickListener { range = "5"; refresh() }
        view.findViewById<View>(R.id.range10).setOnClickListener { range = "10"; refresh() }
        view.findViewById<View>(R.id.range20).setOnClickListener { range = "20"; refresh() }
        view.findViewById<View>(R.id.rangeAll).setOnClickListener { range = "all"; refresh() }

        refresh()
    }

    override fun onResume() {
        super.onResume()
        repository.addChangeListener(changeListener)
        refresh()
    }

    override fun onPause() {
        super.onPause()
        repository.removeChangeListener(changeListener)
    }

    private fun showBranchPicker() {
        val subjects = SubjectConfigs.subjectsFor(examType).map { it.name }
        AlertDialog.Builder(requireContext())
            .setTitle("Branş seç")
            .setItems(subjects.toTypedArray()) { _, i -> branch = subjects[i]; refresh() }
            .show()
    }

    private fun showMetricPicker() {
        val list = if (mode == "toplam") totalMetrics else branchMetrics
        val labels = list.map { it.second }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Metrik seç")
            .setItems(labels) { _, i -> metricKey = list[i].first; refresh() }
            .show()
    }

    private fun metricValue(exam: Exam): Float? {
        if (mode == "toplam") {
            return when (metricKey) {
                "net" -> exam.totalNet.toFloat()
                "correct" -> exam.totalCorrect.toFloat()
                "wrong" -> exam.totalWrong.toFloat()
                "blank" -> exam.totalBlank.toFloat()
                "time" -> exam.totalTimeMinutes.toFloat()
                "mpq" -> exam.overallMinutesPerQuestion?.toFloat()
                else -> null
            }
        } else {
            val s = exam.subject(branch ?: return null) ?: return null
            return when (metricKey) {
                "net" -> s.net.toFloat()
                "correct" -> s.correct.toFloat()
                "wrong" -> s.wrong.toFloat()
                "blank" -> s.blank.toFloat()
                "time" -> s.timeMinutes.toFloat()
                "mpq" -> s.minutesPerQuestion?.toFloat()
                else -> null
            }
        }
    }

    private fun metricUnit(): String = when (metricKey) {
        "net" -> " net"; "time" -> " dk"; "mpq" -> " dk/soru"; else -> ""
    }

    private fun refresh() {
        val v = view ?: return
        v.findViewById<View>(R.id.toggleTyt).isSelected = examType == ExamType.TYT
        v.findViewById<View>(R.id.toggleAyt).isSelected = examType == ExamType.AYT
        v.findViewById<View>(R.id.modeToplam).isSelected = mode == "toplam"
        v.findViewById<View>(R.id.modeBranch).isSelected = mode == "branş"
        v.findViewById<View>(R.id.selectorBranch).visibility = if (mode == "branş") View.VISIBLE else View.GONE
        v.findViewById<View>(R.id.range5).isSelected = range == "5"
        v.findViewById<View>(R.id.range10).isSelected = range == "10"
        v.findViewById<View>(R.id.range20).isSelected = range == "20"
        v.findViewById<View>(R.id.rangeAll).isSelected = range == "all"

        val metricList = if (mode == "toplam") totalMetrics else branchMetrics
        if (metricList.none { it.first == metricKey }) metricKey = metricList.first().first
        val metricLabel = metricList.first { it.first == metricKey }.second
        v.findViewById<TextView>(R.id.selectorMetric).text = "Metrik: $metricLabel"

        if (mode == "branş") {
            if (branch == null || SubjectConfigs.subjectsFor(examType).none { it.name == branch }) {
                branch = SubjectConfigs.subjectsFor(examType).first().name
            }
            v.findViewById<TextView>(R.id.selectorBranch).text = "Branş: $branch"
        }

        val allExams = repository.getAllExams().filter { it.type == examType }.sortedBy { it.date }
        val df = SimpleDateFormat("d MMM", Locale("tr", "TR"))
        val dfFull = SimpleDateFormat("d MMMM yyyy", Locale("tr", "TR"))
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        var series = allExams.mapNotNull { exam ->
            val value = metricValue(exam) ?: return@mapNotNull null
            val date = try { parser.parse(exam.date) } catch (e: Exception) { null } ?: return@mapNotNull null
            LineChartView.Point(
                xLabel = df.format(date),
                value = value,
                title = exam.title,
                fullDate = dfFull.format(date),
                unit = metricUnit()
            )
        }
        if (range != "all") {
            val n = range.toInt()
            series = series.takeLast(n)
        }

        val chart = v.findViewById<LineChartView>(R.id.chart)
        val emptyText = v.findViewById<View>(R.id.tvEmptyChart)

        if (series.isEmpty()) {
            chart.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
            v.findViewById<TextView>(R.id.tvSon).text = "—"
            v.findViewById<TextView>(R.id.tvOrt).text = "—"
            v.findViewById<TextView>(R.id.tvMax).text = "—"
            v.findViewById<TextView>(R.id.tvMin).text = "—"
            return
        }

        chart.visibility = View.VISIBLE
        emptyText.visibility = View.GONE
        chart.points = series

        val values = series.map { it.value }
        v.findViewById<TextView>(R.id.tvSon).text = Fmt.net(values.last().toDouble())
        v.findViewById<TextView>(R.id.tvOrt).text = Fmt.net(values.average())
        v.findViewById<TextView>(R.id.tvMax).text = Fmt.net(values.max().toDouble())
        v.findViewById<TextView>(R.id.tvMin).text = Fmt.net(values.min().toDouble())
    }
}
