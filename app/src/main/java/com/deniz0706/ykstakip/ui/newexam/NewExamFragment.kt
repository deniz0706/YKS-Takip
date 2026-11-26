package com.deniz0706.ykstakip.ui.newexam

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.deniz0706.ykstakip.data.ThemeManager
import com.deniz0706.ykstakip.data.ExamRepository
import com.deniz0706.ykstakip.data.LearningRepository
import com.deniz0706.ykstakip.data.ExamValidator
import com.deniz0706.ykstakip.model.*
import com.deniz0706.ykstakip.ui.MainActivity
import com.deniz0706.ykstakip.ui.theme.YksTheme
import com.deniz0706.ykstakip.ui.theme.activeDarkTheme
import java.time.LocalDate
import java.util.Calendar

class NewExamFragment : Fragment() {
    companion object {
        private const val ARG_EDIT_ID = "edit_id"
        private const val ARG_TYPE = "exam_type"
        fun newInstanceForType(type: ExamType) = NewExamFragment().apply {
            arguments = Bundle().apply { putString(ARG_TYPE, type.name) }
        }
        fun newInstanceForEdit(examId: Long) = NewExamFragment().apply {
            arguments = Bundle().apply { putLong(ARG_EDIT_ID, examId) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val appContext = requireContext().applicationContext
        val repository = ExamRepository.getInstance(appContext)
        val id = arguments?.getLong(ARG_EDIT_ID, -1L)?.takeIf { it > 0 }
        val requestedType = arguments?.getString(ARG_TYPE)
            ?.let { runCatching { ExamType.valueOf(it) }.getOrNull() } ?: ExamType.TYT
        val learningState = mutableStateOf<List<LearningOutcome>>(emptyList())
        val existingState = mutableStateOf<Exam?>(null)
        val loadingState = mutableStateOf(true)
        val errorState = mutableStateOf<String?>(null)

        val composeView = ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val dark = activeDarkTheme(context)
                YksTheme(darkTheme = dark) {
                    if (loadingState.value) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (errorState.value != null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(errorState.value.orEmpty(), color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        val existing = existingState.value
                        NewExamScreenV2(
                            initialType = existing?.type ?: requestedType,
                            existing = existing,
                            learningOutcomes = learningState.value,
                            onBack = { (activity as? MainActivity)?.goBack() },
                            onPickDate = { current, onDate ->
                                val cal = Calendar.getInstance().apply {
                                    set(current.year, current.monthValue - 1, current.dayOfMonth)
                                }
                                DatePickerDialog(requireContext(), { _, y, m, d ->
                                    onDate(LocalDate.of(y, m + 1, d))
                                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)).show()
                            },
                            onSave = { draft ->
                                val validation = ExamValidator.validateWholeExam(
                                    draft.type,
                                    draft.subjects.map { row ->
                                        val config = if (draft.type == ExamType.AYT)
                                            SubjectConfigs.subjectsFor(draft.aytField ?: AytField.SAYISAL)
                                                .first { it.name == row.subject }
                                        else SubjectConfigs.subjectsFor(draft.type)
                                            .first { it.name == row.subject }
                                        Triple(config, row.correct, row.wrong)
                                    },
                                    draft.subjects.associate { it.subject to it.timeMinutes }
                                )
                                if (validation is ExamValidator.FieldResult.Invalid) {
                                    validation.message
                                } else {
                                    repository.saveExam(draft.copy(id = existing?.id ?: repository.nextId()))
                                    (activity as? MainActivity)?.closeAndReturnHome()
                                    null
                                }
                            }
                        )
                    }
                }
            }
        }

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val outcomes = LearningRepository(appContext).outcomes()
                    val existing = id?.let { examId -> repository.getAllExams().find { it.id == examId } }
                    outcomes to existing
                }
            }
            if (!isAdded) return@launch
            result.onSuccess { (outcomes, existing) ->
                learningState.value = outcomes
                existingState.value = existing
                loadingState.value = false
            }.onFailure { error ->
                errorState.value = "Deneme formu yüklenemedi: ${error.message ?: "Veri hatası"}"
                loadingState.value = false
            }
        }
        return composeView
    }
}
