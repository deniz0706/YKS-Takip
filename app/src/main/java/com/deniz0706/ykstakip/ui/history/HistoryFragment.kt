package com.deniz0706.ykstakip.ui.history

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deniz0706.ykstakip.R
import com.deniz0706.ykstakip.data.ExamRepository
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.ui.MainActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private lateinit var repository: ExamRepository
    private lateinit var adapter: ExamHistoryAdapter
    private var selectedType = ExamType.TYT
    private var searchQuery = ""
    private var dateFromMillis: Long? = null
    private var dateToMillis: Long? = null
    private val changeListener = { refresh() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = ExamRepository.getInstance(requireContext())

        adapter = ExamHistoryAdapter(emptyList()) { exam -> (activity as? MainActivity)?.openExamDetail(exam.id) }
        val rv = view.findViewById<RecyclerView>(R.id.recyclerView)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        view.findViewById<View>(R.id.toggleTyt).setOnClickListener { selectedType = ExamType.TYT; refresh() }
        view.findViewById<View>(R.id.toggleAyt).setOnClickListener { selectedType = ExamType.AYT; refresh() }

        view.findViewById<android.widget.EditText>(R.id.etSearch).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) { searchQuery = s?.toString() ?: ""; refresh() }
        })

        view.findViewById<View>(R.id.dateFrom).setOnClickListener { pickDate(true) }
        view.findViewById<View>(R.id.dateTo).setOnClickListener { pickDate(false) }
        view.findViewById<View>(R.id.btnClearDates).setOnClickListener {
            dateFromMillis = null; dateToMillis = null
            view.findViewById<TextView>(R.id.dateFrom).text = "Başlangıç"
            view.findViewById<TextView>(R.id.dateTo).text = "Bitiş"
            refresh()
        }

        refresh()
    }

    private fun pickDate(isFrom: Boolean) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                cal.set(y, m, d)
                val label = SimpleDateFormat("d MMM yyyy", Locale("tr", "TR")).format(cal.time)
                if (isFrom) {
                    dateFromMillis = cal.timeInMillis
                    view?.findViewById<TextView>(R.id.dateFrom)?.text = label
                } else {
                    dateToMillis = cal.timeInMillis
                    view?.findViewById<TextView>(R.id.dateTo)?.text = label
                }
                refresh()
            },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).show()
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

        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        var exams = repository.getAllExams().filter { it.type == selectedType }

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            exams = exams.filter { it.title.lowercase().contains(q) || it.publisher.lowercase().contains(q) }
        }
        if (dateFromMillis != null || dateToMillis != null) {
            exams = exams.filter { exam ->
                val examMillis = try { parser.parse(exam.date)?.time } catch (e: Exception) { null } ?: return@filter true
                (dateFromMillis == null || examMillis >= dateFromMillis!!) &&
                    (dateToMillis == null || examMillis <= dateToMillis!!)
            }
        }
        exams = exams.sortedByDescending { it.date }

        adapter.submit(exams)
        v.findViewById<View>(R.id.emptyState).visibility = if (exams.isEmpty()) View.VISIBLE else View.GONE
        v.findViewById<RecyclerView>(R.id.recyclerView).visibility = if (exams.isEmpty()) View.GONE else View.VISIBLE
    }
}
