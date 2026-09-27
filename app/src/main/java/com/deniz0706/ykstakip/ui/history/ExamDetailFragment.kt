package com.deniz0706.ykstakip.ui.history

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.deniz0706.ykstakip.R
import com.deniz0706.ykstakip.data.ExamRepository
import com.deniz0706.ykstakip.ui.MainActivity
import com.deniz0706.ykstakip.util.Fmt
import java.text.SimpleDateFormat
import java.util.Locale

class ExamDetailFragment : Fragment(R.layout.fragment_exam_detail) {

    private var examId: Long = -1

    companion object {
        private const val ARG_ID = "exam_id"
        fun newInstance(id: Long): ExamDetailFragment {
            val f = ExamDetailFragment()
            f.arguments = Bundle().apply { putLong(ARG_ID, id) }
            return f
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        examId = arguments?.getLong(ARG_ID) ?: -1
        val repository = ExamRepository.getInstance(requireContext())
        val exam = repository.getAllExams().find { it.id == examId }

        if (exam == null) {
            (activity as? MainActivity)?.goBack()
            return
        }

        view.findViewById<View>(R.id.btnBack).setOnClickListener { (activity as? MainActivity)?.goBack() }
        view.findViewById<View>(R.id.btnEdit).setOnClickListener { (activity as? MainActivity)?.openEditExam(examId) }
        view.findViewById<View>(R.id.btnDelete).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Denemeyi sil")
                .setMessage("\"${exam.title}\" silinecek. Bu işlem geri alınamaz.")
                .setPositiveButton("Sil") { _, _ ->
                    repository.deleteExam(examId)
                    (activity as? MainActivity)?.goBack()
                }
                .setNegativeButton("Vazgeç", null)
                .show()
        }

        view.findViewById<TextView>(R.id.tvTitle).text = exam.title
        val displayDate = try {
            SimpleDateFormat("d MMMM yyyy", Locale("tr", "TR"))
                .format(SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(exam.date)!!)
        } catch (e: Exception) { exam.date }
        view.findViewById<TextView>(R.id.tvSubtitle).text = "${exam.type.label}  •  $displayDate"

        val tvPublisher = view.findViewById<TextView>(R.id.tvPublisher)
        if (exam.publisher.isBlank()) {
            tvPublisher.visibility = View.GONE
        } else {
            tvPublisher.visibility = View.VISIBLE
            tvPublisher.text = "Yayın evi: ${exam.publisher}"
        }

        val notesContainer = view.findViewById<View>(R.id.notesContainer)
        val tvNotes = view.findViewById<TextView>(R.id.tvNotes)
        if (exam.notes.isBlank()) {
            notesContainer.visibility = View.GONE
        } else {
            notesContainer.visibility = View.VISIBLE
            tvNotes.text = exam.notes
        }

        view.findViewById<TextView>(R.id.tvTotalNet).text = Fmt.net(exam.totalNet)
        view.findViewById<TextView>(R.id.tvTotalMeta).text =
            "${exam.totalCorrect} doğru · ${exam.totalWrong} yanlış · ${exam.totalBlank} boş · ${exam.totalTimeMinutes} dk" +
                (exam.overallMinutesPerQuestion?.let { " · ${Fmt.minutesPerQuestion(it)} dk/soru" } ?: "")

        val container = view.findViewById<android.widget.LinearLayout>(R.id.llSubjects)
        val inflater = LayoutInflater.from(requireContext())
        for (s in exam.subjects) {
            val row = inflater.inflate(R.layout.item_subject_detail, container, false)
            row.findViewById<TextView>(R.id.tvName).text = "${s.subject} (${s.questionCount} soru)"
            row.findViewById<TextView>(R.id.tvNet).text = "${Fmt.net(s.net)} net"
            row.findViewById<TextView>(R.id.tvDetail).text =
                "${s.correct} doğru · ${s.wrong} yanlış · ${s.blank} boş · ${s.timeMinutes} dk" +
                    (s.minutesPerQuestion?.let { " · ${Fmt.minutesPerQuestion(it)} dk/soru" } ?: "")

            val tvWeakTopics = row.findViewById<TextView>(R.id.tvWeakTopics)
            if (s.weakTopics.isEmpty()) {
                tvWeakTopics.visibility = View.GONE
            } else {
                tvWeakTopics.visibility = View.VISIBLE
                tvWeakTopics.text = "Zayıf konular: ${s.weakTopics.joinToString(", ")}"
            }

            container.addView(row)
        }
    }
}
