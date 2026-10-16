package com.deniz0706.ykstakip.ui.history

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.deniz0706.ykstakip.data.ExamRepository
import com.deniz0706.ykstakip.data.ThemeManager
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.ui.MainActivity
import com.deniz0706.ykstakip.ui.theme.YksTheme
import java.util.Calendar

class HistoryFragment : Fragment() {
    private lateinit var repository: ExamRepository
    private val exams = mutableStateOf<List<Exam>>(emptyList())
    private val fromDate = mutableStateOf<String?>(null)
    private val toDate = mutableStateOf<String?>(null)
    private val listener: () -> Unit = { refresh() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = ExamRepository.getInstance(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val dark = when (ThemeManager.currentTheme(context)) {
                    "dark" -> true
                    "light" -> false
                    else -> isSystemInDarkTheme()
                }
                YksTheme(darkTheme = dark) {
                    HistoryScreenV2(
                        exams = exams.value,
                        onExamClick = { (activity as? MainActivity)?.openExamDetail(it) },
                        onPickDate = { pickDate(it) },
                        fromDate = fromDate.value,
                        toDate = toDate.value,
                        onClearDates = { fromDate.value = null; toDate.value = null },
                        modifier = Modifier.background(MaterialTheme.colorScheme.background)
                    )
                }
            }
        }

    private fun pickDate(start: Boolean) {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            val date = "%04d-%02d-%02d".format(java.util.Locale.US, year, month + 1, day)
            if (start) fromDate.value = date else toDate.value = date
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    override fun onResume() {
        super.onResume()
        repository.addChangeListener(listener)
        refresh()
    }

    override fun onPause() {
        repository.removeChangeListener(listener)
        super.onPause()
    }

    private fun refresh() { exams.value = repository.getAllExams() }
}
