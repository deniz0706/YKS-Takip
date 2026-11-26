package com.deniz0706.ykstakip.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.deniz0706.ykstakip.data.ExamRepository
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.ui.MainActivity
import com.deniz0706.ykstakip.ui.theme.YksTheme
import com.deniz0706.ykstakip.ui.theme.activeDarkTheme

class ExamDetailFragment : Fragment() {
    private var examId: Long = -1L
    private lateinit var repository: ExamRepository
    private val currentExam = mutableStateOf<Exam?>(null)
    private val loading = mutableStateOf(true)
    private var refreshJob: Job? = null

    companion object {
        private const val ARG_ID = "exam_id"

        fun newInstance(id: Long): ExamDetailFragment {
            val fragment = ExamDetailFragment()
            fragment.arguments = Bundle().apply { putLong(ARG_ID, id) }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        examId = arguments?.getLong(ARG_ID) ?: -1L
        repository = ExamRepository.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            YksTheme(darkTheme = activeDarkTheme(context)) {
                val exam = currentExam.value
                if (loading.value) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (exam != null) {
                    ExamDetailScreen(
                        exam = exam,
                        onBack = { (activity as? MainActivity)?.goBack() },
                        onEdit = { (activity as? MainActivity)?.openEditExam(examId) },
                        onDelete = {
                            repository.deleteExam(examId)
                            (activity as? MainActivity)?.goBack()
                        },
                        onOpenLearning = { type, subject, topic ->
                            (activity as? MainActivity)?.openLearning(type, subject, topic)
                        }
                    )
                }
            }
        }
    }

    private fun refresh() {
        if (!isAdded || view == null) return
        refreshJob?.cancel()
        loading.value = true
        refreshJob = viewLifecycleOwner.lifecycleScope.launch {
            val loaded = withContext(Dispatchers.IO) {
                repository.getAllExams().find { it.id == examId }
            }
            if (!isAdded) return@launch
            currentExam.value = loaded
            loading.value = false
            if (loaded == null) (activity as? MainActivity)?.goBack()
        }
    }
    override fun onResume() {
        super.onResume()
        if (!::repository.isInitialized) return
        refresh()
    }
}
