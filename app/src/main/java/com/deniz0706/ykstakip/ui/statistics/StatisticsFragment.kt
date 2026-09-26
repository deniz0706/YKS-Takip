package com.deniz0706.ykstakip.ui.statistics

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.deniz0706.ykstakip.R
import com.deniz0706.ykstakip.data.AppSettings
import com.deniz0706.ykstakip.data.ExamRepository
import com.deniz0706.ykstakip.model.AytField
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.model.SubjectConfigs
import com.deniz0706.ykstakip.util.Fmt
import com.deniz0706.ykstakip.util.WeakTopicAnalyzer
import com.deniz0706.ykstakip.util.YksRankingCalculator
import java.text.SimpleDateFormat
import java.util.Locale

class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private lateinit var repository: ExamRepository

    private var examType = ExamType.TYT
    private var mode = "toplam"
    private var branch: String? = null
    private var metricKey = "net"
    private var range = "10"
    private var aytField = AytField.SAYISAL

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

        view.findViewById<View>(R.id.toggleTyt).setOnClickListener {
            examType = ExamType.TYT
            branch = null
            refresh()
        }

        view.findViewById<View>(R.id.toggleAyt).setOnClickListener {
            examType = ExamType.AYT
            branch = null
            refresh()
        }

        view.findViewById<View>(R.id.modeToplam).setOnClickListener {
            mode = "toplam"
            refresh()
        }

        view.findViewById<View>(R.id.modeBranch).setOnClickListener {
            mode = "branş"
            if (branch == null) {
                branch = SubjectConfigs.subjectsFor(examType).first().name
            }
            refresh()
        }

        view.findViewById<View>(R.id.selectorBranch).setOnClickListener {
            showBranchPicker()
        }

        view.findViewById<View>(R.id.selectorMetric).setOnClickListener {
            showMetricPicker()
        }

        view.findViewById<View>(R.id.selectorAytField).setOnClickListener {
            showAytFieldPicker()
        }

        view.findViewById<View>(R.id.range5).setOnClickListener {
            range = "5"
            refresh()
        }

        view.findViewById<View>(R.id.range10).setOnClickListener {
            range = "10"
            refresh()
        }

        view.findViewById<View>(R.id.range20).setOnClickListener {
            range = "20"
            refresh()
        }

        view.findViewById<View>(R.id.rangeAll).setOnClickListener {
            range = "all"
            refresh()
        }

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
            .setItems(subjects.toTypedArray()) { _, i ->
                branch = subjects[i]
                refresh()
            }
            .show()
    }

    private fun showMetricPicker() {
        val list = if (mode == "toplam") totalMetrics else branchMetrics
        val labels = list.map { it.second }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Metrik seç")
            .setItems(labels) { _, i ->
                metricKey = list[i].first
                refresh()
            }
            .show()
    }

    private fun showAytFieldPicker() {
        val fields = arrayOf(
            "Sayısal",
            "Eşit Ağırlık",
            "Sözel"
        )

        val selected = when (aytField) {
            AytField.SAYISAL -> 0
            AytField.ESIT_AGIRLIK -> 1
            AytField.SOZEL -> 2
        }

        AlertDialog.Builder(requireContext())
            .setTitle("AYT alanı")
            .setSingleChoiceItems(fields, selected) { dialog, which ->
                aytField = when (which) {
                    0 -> AytField.SAYISAL
                    1 -> AytField.ESIT_AGIRLIK
                    else -> AytField.SOZEL
                }
                dialog.dismiss()
                refresh()
            }
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
        "net" -> " net"
        "time" -> " dk"
        "mpq" -> " dk/soru"
        else -> ""
    }

    private fun refresh() {
        val v = view ?: return

        v.findViewById<View>(R.id.toggleTyt).isSelected =
            examType == ExamType.TYT

        v.findViewById<View>(R.id.toggleAyt).isSelected =
            examType == ExamType.AYT

        v.findViewById<View>(R.id.modeToplam).isSelected =
            mode == "toplam"

        v.findViewById<View>(R.id.modeBranch).isSelected =
            mode == "branş"

        v.findViewById<View>(R.id.selectorBranch).visibility =
            if (mode == "branş") View.VISIBLE else View.GONE

        v.findViewById<View>(R.id.selectorAytField).visibility =
            if (examType == ExamType.AYT) View.VISIBLE else View.GONE

        v.findViewById<View>(R.id.range5).isSelected =
            range == "5"

        v.findViewById<View>(R.id.range10).isSelected =
            range == "10"

        v.findViewById<View>(R.id.range20).isSelected =
            range == "20"

        v.findViewById<View>(R.id.rangeAll).isSelected =
            range == "all"

        val metricList =
            if (mode == "toplam") totalMetrics else branchMetrics

        if (metricList.none { it.first == metricKey }) {
            metricKey = metricList.first().first
        }

        val metricLabel =
            metricList.first { it.first == metricKey }.second

        v.findViewById<TextView>(R.id.selectorMetric).text =
            "Metrik: $metricLabel"

        if (mode == "branş") {
            if (
                branch == null ||
                SubjectConfigs.subjectsFor(examType)
                    .none { it.name == branch }
            ) {
                branch = SubjectConfigs.subjectsFor(examType).first().name
            }

            v.findViewById<TextView>(R.id.selectorBranch).text =
                "Branş: $branch"
        }

        v.findViewById<TextView>(R.id.selectorAytField).text =
            "AYT alanı: ${aytFieldLabel()}"

        val allExams =
            repository.getAllExams()
                .filter { it.type == examType }
                .sortedBy { it.date }

        val df = SimpleDateFormat("d MMM", Locale("tr", "TR"))
        val dfFull = SimpleDateFormat("d MMMM yyyy", Locale("tr", "TR"))
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        var series = allExams.mapNotNull { exam ->
            val value = metricValue(exam)
                ?: return@mapNotNull null

            val date =
                try {
                    parser.parse(exam.date)
                } catch (e: Exception) {
                    null
                } ?: return@mapNotNull null

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

            renderWeakSpots(allExams)
            renderRanking()
            return
        }

        chart.visibility = View.VISIBLE
        emptyText.visibility = View.GONE

        chart.points = series

        chart.targetValue =
            if (mode == "toplam" && metricKey == "net") {
                AppSettings.getTargetNet(
                    requireContext(),
                    examType
                )
            } else {
                null
            }

        val values = series.map { it.value }

        fun formatStat(value: Double): String =
            when (metricKey) {
                "time" -> "${value.toInt()} dk"
                "mpq" -> "${Fmt.round2(value)} dk/soru"
                else -> Fmt.net(value)
            }

        v.findViewById<TextView>(R.id.tvSon).text =
            formatStat(values.last().toDouble())

        v.findViewById<TextView>(R.id.tvOrt).text =
            formatStat(values.average())

        v.findViewById<TextView>(R.id.tvMax).text =
            formatStat(values.max().toDouble())

        v.findViewById<TextView>(R.id.tvMin).text =
            formatStat(values.min().toDouble())

        renderWeakSpots(allExams)
        renderRanking()
    }

    private fun renderRanking() {
        val v = view ?: return

        val card = v.findViewById<View>(R.id.rankingCard)
        val empty = v.findViewById<TextView>(R.id.tvRankingEmpty)

        val obp = AppSettings.getObp(requireContext())?.toDouble()

        if (obp == null) {
            card.visibility = View.VISIBLE
            empty.visibility = View.VISIBLE
            empty.text = "Sıralama tahmini için Ayarlar'dan OBP gir."
            clearRankingStats()
            return
        }

        val exams = repository.getAllExams()

        if (examType == ExamType.TYT) {
            val results =
                YksRankingCalculator.calculateTyt(
                    exams = exams,
                    obp = obp
                )

            if (results.isEmpty()) {
                showRankingEmpty("Henüz hesaplanabilecek TYT denemesi yok.")
                return
            }

            val estimates =
                results.map { it.estimate }

            showRankingStats(estimates)
            renderRankingTrend()
        } else {
            val results =
                YksRankingCalculator.calculateAyt(
                    exams = exams,
                    obp = obp,
                    field = aytField
                )

            if (results.isEmpty()) {
                showRankingEmpty(
                    "Seçili alan için eşleşen TYT + AYT denemesi yok."
                )
                return
            }

            val estimates =
                results.map { it.estimate }

            showRankingStats(estimates)
            renderRankingTrend()
        }
    }

    private fun showRankingStats(
        estimates: List<com.deniz0706.ykstakip.util.RankingEstimate>
    ) {
        val v = view ?: return

        v.findViewById<View>(R.id.rankingCard).visibility =
            View.VISIBLE

        v.findViewById<TextView>(R.id.tvRankingEmpty).visibility =
            View.GONE

        val latest = estimates.last()
        val last3 = estimates.takeLast(3)
        val last5 = estimates.takeLast(5)

        val best = estimates.minByOrNull { it.center } ?: latest
        val worst = estimates.maxByOrNull { it.center } ?: latest

        v.findViewById<TextView>(R.id.tvRankingLast).text =
            formatRanking(latest)

        v.findViewById<TextView>(R.id.tvRankingAvg3).text =
            formatRankingAverage(last3)

        v.findViewById<TextView>(R.id.tvRankingAvg5).text =
            formatRankingAverage(last5)

        v.findViewById<TextView>(R.id.tvRankingBest).text =
            formatRanking(best)

        v.findViewById<TextView>(R.id.tvRankingWorst).text =
            formatRanking(worst)
    }

        private fun renderRankingTrend() {
        val v = view ?: return

        val chart =
            v.findViewById<LineChartView>(R.id.rankingChart)

        val empty =
            v.findViewById<TextView>(R.id.tvEmptyRankingChart)

        val obp =
            AppSettings.getObp(requireContext())?.toDouble()

        if (obp == null) {
            chart.visibility = View.GONE
            empty.visibility = View.VISIBLE
            return
        }

        val exams =
            repository.getAllExams()

        val points =
            if (examType == ExamType.TYT) {
                YksRankingCalculator
                    .calculateTyt(
                        exams = exams,
                        obp = obp
                    )
                    .map { result ->
                        rankingPoint(
                            date = result.exam.date,
                            title = result.exam.title,
                            estimate = result.estimate
                        )
                    }
            } else {
                YksRankingCalculator
                    .calculateAyt(
                        exams = exams,
                        obp = obp,
                        field = aytField
                    )
                    .map { result ->
                        rankingPoint(
                            date = result.aytExam.date,
                            title = result.aytExam.title,
                            estimate = result.estimate
                        )
                    }
            }

        if (points.isEmpty()) {
            chart.visibility = View.GONE
            empty.visibility = View.VISIBLE
            return
        }

        chart.visibility = View.VISIBLE
        empty.visibility = View.GONE
        chart.reverseY = true
        chart.targetValue = null
        chart.points = points
    }

    private fun rankingPoint(
        date: String,
        title: String,
        estimate: com.deniz0706.ykstakip.util.RankingEstimate
    ): LineChartView.Point {
        val parser =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            )

        val df =
            SimpleDateFormat(
                "d MMM",
                Locale("tr", "TR")
            )

        val dfFull =
            SimpleDateFormat(
                "d MMMM yyyy",
                Locale("tr", "TR")
            )

        val parsed =
            try {
                parser.parse(date)
            } catch (e: Exception) {
                null
            }

        return LineChartView.Point(
            xLabel = if (parsed != null) df.format(parsed) else date,
            value = estimate.center.toFloat(),
            title = title,
            fullDate = if (parsed != null) dfFull.format(parsed) else date,
            unit = " sıra"
        )
    }

    private fun showRankingEmpty(
        message: String
    ) {
        val v = view ?: return

        v.findViewById<View>(R.id.rankingCard).visibility =
            View.VISIBLE

        val empty = v.findViewById<TextView>(R.id.tvRankingEmpty)
        empty.visibility = View.VISIBLE
        empty.text = message

        clearRankingStats()
        renderRankingTrend()
    }

    private fun renderRankingTrend() {
    val v = view ?: return

    val chart =
        v.findViewById<LineChartView>(R.id.rankingChart)

    val empty =
        v.findViewById<TextView>(R.id.tvEmptyRankingChart)

    val obp =
        AppSettings.getObp(requireContext())?.toDouble()

    if (obp == null) {
        chart.visibility = View.GONE
        empty.visibility = View.VISIBLE
        return
    }

    val exams =
        repository.getAllExams()

    val points =
        if (examType == ExamType.TYT) {
            YksRankingCalculator
                .calculateTyt(
                    exams = exams,
                    obp = obp
                )
                .map { result ->
                    rankingPoint(
                        date = result.exam.date,
                        title = result.exam.title,
                        estimate = result.estimate
                    )
                }
        } else {
            YksRankingCalculator
                .calculateAyt(
                    exams = exams,
                    obp = obp,
                    field = aytField
                )
                .map { result ->
                    rankingPoint(
                        date = result.aytExam.date,
                        title = result.aytExam.title,
                        estimate = result.estimate
                    )
                }
        }

    if (points.isEmpty()) {
        chart.visibility = View.GONE
        empty.visibility = View.VISIBLE
        return
    }

    chart.visibility = View.VISIBLE
    empty.visibility = View.GONE

    chart.reverseY = true
    chart.targetValue = null
    chart.points = points
}

    private fun clearRankingStats()
    renderRankingTrend(){
        val v = view ?: return

        v.findViewById<TextView>(R.id.tvRankingLast).text = "—"
        v.findViewById<TextView>(R.id.tvRankingAvg3).text = "—"
        v.findViewById<TextView>(R.id.tvRankingAvg5).text = "—"
        v.findViewById<TextView>(R.id.tvRankingBest).text = "—"
        v.findViewById<TextView>(R.id.tvRankingWorst).text = "—"
    }
    private fun rankingPoint(
    date: String,
    title: String,
    estimate: com.deniz0706.ykstakip.util.RankingEstimate
): LineChartView.Point {
    val parser =
        SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        )

    val df =
        SimpleDateFormat(
            "d MMM",
            Locale("tr", "TR")
        )

    val dfFull =
        SimpleDateFormat(
            "d MMMM yyyy",
            Locale("tr", "TR")
        )

    val parsed =
        try {
            parser.parse(date)
        } catch (e: Exception) {
            null
        }

    return LineChartView.Point(
        xLabel = if (parsed != null) df.format(parsed) else date,
        value = estimate.center.toFloat(),
        title = title,
        fullDate = if (parsed != null) dfFull.format(parsed) else date,
        unit = " sıra"
    )
}

    private fun formatRanking(
        estimate: com.deniz0706.ykstakip.util.RankingEstimate
    ): String {
        return "~${formatNumber(estimate.center)}"
    }

    private fun formatRankingAverage(
        estimates: List<com.deniz0706.ykstakip.util.RankingEstimate>
    ): String {
        if (estimates.isEmpty()) return "—"

        val average =
            estimates.map { it.center }.average().toInt()

        return "~${formatNumber(average)}"
    }

    private fun formatNumber(value: Int): String {
        return String.format(
            Locale("tr", "TR"),
            "%,d",
            value
        )
    }

    private fun aytFieldLabel(): String =
        when (aytField) {
            AytField.SAYISAL -> "Sayısal"
            AytField.ESIT_AGIRLIK -> "Eşit Ağırlık"
            AytField.SOZEL -> "Sözel"
        }

    private fun renderWeakSpots(exams: List<Exam>) {
        val v = view ?: return
        val card = v.findViewById<View>(R.id.weakSpotCard)
        val list = v.findViewById<LinearLayout>(R.id.weakSpotList)

        val spots =
            WeakTopicAnalyzer.findWeakSpots(
                exams,
                sampleSize = 5,
                top = 3
            )

        if (spots.isEmpty()) {
            card.visibility = View.GONE
            return
        }

        card.visibility = View.VISIBLE
        list.removeAllViews()

        for (spot in spots) {
            val pct =
                if (spot.outOf > 0) {
                    spot.count * 100 / spot.outOf
                } else {
                    0
                }

            val tv = TextView(requireContext()).apply {
                text =
                    "${spot.subject} — ${spot.topic}: son ${spot.outOf} denemede ${spot.count} kez (%$pct)"

                textSize = 13f
                setTextColor(
                    resources.getColor(
                        R.color.on_background,
                        null
                    )
                )
                setPadding(0, 6, 0, 6)
            }

            list.addView(tv)
        }
    }
}
