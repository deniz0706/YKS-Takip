package com.deniz0706.ykstakip.ui.home

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.deniz0706.ykstakip.R
import com.deniz0706.ykstakip.data.AppSettings
import com.deniz0706.ykstakip.data.ExamRepository
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.ui.MainActivity
import com.deniz0706.ykstakip.util.Fmt
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var repository: ExamRepository
    private var selectedType = ExamType.TYT
    private val changeListener = { refresh() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = ExamRepository.getInstance(requireContext())

        view.findViewById<TextView>(R.id.tvDate).text =
            SimpleDateFormat("d MMMM yyyy, EEEE", Locale("tr", "TR")).format(Date())

        view.findViewById<View>(R.id.toggleTyt).setOnClickListener {
            selectedType = ExamType.TYT
            refresh()
        }

        view.findViewById<View>(R.id.toggleAyt).setOnClickListener {
            selectedType = ExamType.AYT
            refresh()
        }

        view.findViewById<View>(R.id.btnNewExam).setOnClickListener {
            (activity as? MainActivity)?.openNewExam(selectedType)
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

    private fun refresh() {
        val view = view ?: return

        view.findViewById<View>(R.id.toggleTyt).isSelected =
            selectedType == ExamType.TYT

        view.findViewById<View>(R.id.toggleAyt).isSelected =
            selectedType == ExamType.AYT

        val exams = repository.getAllExams()
            .filter { it.type == selectedType }
            .sortedBy { it.date }

        val nets = exams.map { it.totalNet }

        val tvLastNet = view.findViewById<TextView>(R.id.tvLastNet)
        val tvDelta = view.findViewById<TextView>(R.id.tvDelta)
        val emptyState = view.findViewById<View>(R.id.emptyState)

        if (nets.isEmpty()) {
            tvLastNet.text = "—"
            tvDelta.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            emptyState.visibility = View.GONE
            tvLastNet.text = Fmt.net(nets.last())

            if (nets.size >= 2) {
                val delta = Fmt.round2(nets.last() - nets[nets.size - 2])
                tvDelta.visibility = View.VISIBLE
                tvDelta.text = Fmt.netWithSign(delta)

                val color =
                    if (delta >= 0) R.color.positive
                    else R.color.negative

                tvDelta.setTextColor(
                    ContextCompat.getColor(requireContext(), color)
                )
            } else {
                tvDelta.visibility = View.GONE
            }
        }

        view.findViewById<TextView>(R.id.tvAvg).text =
            if (nets.isEmpty()) "—" else Fmt.net(nets.average())

        view.findViewById<TextView>(R.id.tvMax).text =
            if (nets.isEmpty()) "—" else Fmt.net(nets.max())

        view.findViewById<TextView>(R.id.tvLast5).text =
            if (nets.isEmpty()) "—" else Fmt.net(nets.takeLast(5).average())

        view.findViewById<TextView>(R.id.tvTotalCount).text =
            "Toplam ${exams.size} ${selectedType.label} denemesi"

        renderCountdown(view)
    }

    private fun renderCountdown(view: View) {
        val card = view.findViewById<View>(R.id.countdownCard)
        val dailyGoal = AppSettings.getDailyGoal(requireContext())

        card.visibility = View.VISIBLE

        view.findViewById<TextView>(R.id.tvCountdownLabel).text =
            "${selectedType.label}'ye kalan"

        val days = daysUntilExam(selectedType)

        view.findViewById<TextView>(R.id.tvDaysLeft).text =
            if (days >= 0) "$days gün" else "Geçti"

        val todayStr =
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        val todayQuestionCount = repository.getAllExams()
            .filter { it.date == todayStr }
            .sumOf { it.totalQuestions }

        view.findViewById<TextView>(R.id.tvTodayQuestions).text =
            if (dailyGoal != null) {
                "$todayQuestionCount / $dailyGoal"
            } else {
                todayQuestionCount.toString()
            }
    }

    private fun daysUntilExam(type: ExamType): Long {
        val exam = Calendar.getInstance().apply {
            set(
                2027,
                Calendar.JUNE,
                if (type == ExamType.TYT) 19 else 20,
                0,
                0,
                0
            )
            set(Calendar.MILLISECOND, 0)
        }

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return (exam.timeInMillis - today.timeInMillis) /
                (24L * 60L * 60L * 1000L)
    }
}
