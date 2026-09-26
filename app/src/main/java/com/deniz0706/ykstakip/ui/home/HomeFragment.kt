package com.deniz0706.ykstakip.ui.home

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.deniz0706.ykstakip.R
import com.deniz0706.ykstakip.data.AppSettings
import com.deniz0706.ykstakip.data.ExamRepository
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.ui.MainActivity
import com.deniz0706.ykstakip.util.Fmt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var repository: ExamRepository
    private var selectedType = ExamType.TYT
    private val changeListener = { refresh() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = ExamRepository.getInstance(requireContext())

        view.findViewById<android.widget.TextView>(R.id.tvDate).text =
            SimpleDateFormat("d MMMM yyyy, EEEE", Locale("tr", "TR")).format(Date())

        view.findViewById<View>(R.id.toggleTyt).setOnClickListener { selectedType = ExamType.TYT; refresh() }
        view.findViewById<View>(R.id.toggleAyt).setOnClickListener { selectedType = ExamType.AYT; refresh() }
        view.findViewById<View>(R.id.btnNewExam).setOnClickListener { (activity as? MainActivity)?.openNewExam(selectedType) }

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
        view.findViewById<View>(R.id.toggleTyt).isSelected = selectedType == ExamType.TYT
        view.findViewById<View>(R.id.toggleAyt).isSelected = selectedType == ExamType.AYT

        val exams = repository.getAllExams().filter { it.type == selectedType }.sortedBy { it.date }
        val nets = exams.map { it.totalNet }

        val tvLastNet = view.findViewById<android.widget.TextView>(R.id.tvLastNet)
        val tvDelta = view.findViewById<android.widget.TextView>(R.id.tvDelta)
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
                val color = if (delta >= 0) R.color.positive else R.color.negative
                tvDelta.setTextColor(ContextCompat.getColor(requireContext(), color))
            } else {
                tvDelta.visibility = View.GONE
            }
        }

        view.findViewById<android.widget.TextView>(R.id.tvAvg).text = if (nets.isEmpty()) "—" else Fmt.net(nets.average())
        view.findViewById<android.widget.TextView>(R.id.tvMax).text = if (nets.isEmpty()) "—" else Fmt.net(nets.max())
        view.findViewById<android.widget.TextView>(R.id.tvLast5).text = if (nets.isEmpty()) "—" else Fmt.net(nets.takeLast(5).average())
        view.findViewById<android.widget.TextView>(R.id.tvTotalCount).text = "Toplam ${exams.size} ${selectedType.label} denemesi"

        renderCountdown(view)
    }

    private fun renderCountdown(view: View) {
        val card = view.findViewById<View>(R.id.countdownCard)
        val examMillis = AppSettings.getExamDateMillis(requireContext())
        val dailyGoal = AppSettings.getDailyGoal(requireContext())

        if (examMillis == null && dailyGoal == null) {
            card.visibility = View.GONE
            return
        }
        card.visibility = View.VISIBLE

        val tvDaysLeft = view.findViewById<android.widget.TextView>(R.id.tvDaysLeft)
        if (examMillis != null) {
            val diffMillis = examMillis - System.currentTimeMillis()
            val days = TimeUnit.MILLISECONDS.toDays(diffMillis) + 1
            tvDaysLeft.text = if (days >= 0) "$days gün" else "Geçti"
        } else {
            tvDaysLeft.text = "—"
        }

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val todayQuestionCount = repository.getAllExams().filter { it.date == todayStr }.sumOf { it.totalQuestions }
        val tvTodayQuestions = view.findViewById<android.widget.TextView>(R.id.tvTodayQuestions)
        tvTodayQuestions.text = if (dailyGoal != null) "$todayQuestionCount / $dailyGoal" else todayQuestionCount.toString()
    }
}
