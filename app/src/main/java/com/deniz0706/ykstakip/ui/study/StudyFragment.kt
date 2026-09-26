package com.deniz0706.ykstakip.ui.study

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.deniz0706.ykstakip.R
import com.deniz0706.ykstakip.data.AppSettings
import com.deniz0706.ykstakip.data.StudyRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudyFragment : Fragment(R.layout.fragment_study) {

    private lateinit var repository: StudyRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = StudyRepository(requireContext())

        refresh()
    }

    private fun refresh() {
        val v = view ?: return

        val today = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).format(Date())

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
