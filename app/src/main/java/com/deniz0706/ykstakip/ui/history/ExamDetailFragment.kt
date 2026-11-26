package com.deniz0706.ykstakip.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.deniz0706.ykstakip.data.ExamRepository
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.ui.MainActivity
import com.deniz0706.ykstakip.ui.theme.YksTheme
import com.deniz0706.ykstakip.ui.theme.activeDarkTheme

class ExamDetailFragment : Fragment() {
    private var examId: Long = -1L
    private lateinit var repository: ExamRepository
    private val currentExam = mutableStateOf<Exam?>(null)

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
        currentExam.value = repository.getAllExams().find { it.id == examId }
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
                if (exam != null) {
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

    override fun onResume() {
        super.onResume()
        if (!::repository.isInitialized) return
        currentExam.value = repository.getAllExams().find { it.id == examId }
        if (currentExam.value == null) (activity as? MainActivity)?.goBack()
    }
}
