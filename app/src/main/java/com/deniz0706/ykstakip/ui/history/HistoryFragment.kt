package com.deniz0706.ykstakip.ui.history

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deniz0706.ykstakip.R
import com.deniz0706.ykstakip.data.ExamRepository
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.ui.MainActivity

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private lateinit var repository: ExamRepository
    private lateinit var adapter: ExamHistoryAdapter
    private var selectedType = ExamType.TYT
    private val changeListener = { refresh() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = ExamRepository.getInstance(requireContext())

        adapter = ExamHistoryAdapter(emptyList()) { exam ->
            (activity as? MainActivity)?.openExamDetail(exam.id)
        }
        val rv = view.findViewById<RecyclerView>(R.id.recyclerView)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        view.findViewById<View>(R.id.toggleTyt).setOnClickListener { selectedType = ExamType.TYT; refresh() }
        view.findViewById<View>(R.id.toggleAyt).setOnClickListener { selectedType = ExamType.AYT; refresh() }

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
        val v = view ?: return
        v.findViewById<View>(R.id.toggleTyt).isSelected = selectedType == ExamType.TYT
        v.findViewById<View>(R.id.toggleAyt).isSelected = selectedType == ExamType.AYT

        val exams = repository.getAllExams().filter { it.type == selectedType }.sortedByDescending { it.date }
        adapter.submit(exams)
        v.findViewById<View>(R.id.emptyState).visibility = if (exams.isEmpty()) View.VISIBLE else View.GONE
        v.findViewById<RecyclerView>(R.id.recyclerView).visibility = if (exams.isEmpty()) View.GONE else View.VISIBLE
    }
}
