package com.deniz0706.ykstakip.ui.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.deniz0706.ykstakip.data.AppSettings
import com.deniz0706.ykstakip.data.ExamRepository
import com.deniz0706.ykstakip.data.ThemeManager
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.ui.theme.YksTheme
import com.deniz0706.ykstakip.ui.theme.activeDarkTheme

class StatisticsFragment : Fragment() {
    private lateinit var repository: ExamRepository
    private val exams = mutableStateOf<List<Exam>>(emptyList())
    private val obp = mutableStateOf<Double?>(null)
    private val targetTyt = mutableStateOf<Float?>(null)
    private val targetAyt = mutableStateOf<Float?>(null)
    private val listener = { refresh() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = ExamRepository.getInstance(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val dark = activeDarkTheme(context)
                YksTheme(darkTheme = dark) {
                    StatisticsScreenV2(exams.value, obp.value, targetTyt.value, targetAyt.value)
                }
            }
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

    private fun refresh() {
        exams.value = repository.getAllExams()
        obp.value = AppSettings.getObp(requireContext())?.toDouble()
        targetTyt.value = AppSettings.getTargetNet(requireContext(), ExamType.TYT)
        targetAyt.value = AppSettings.getTargetNet(requireContext(), ExamType.AYT)
    }
}
