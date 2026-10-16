package com.deniz0706.ykstakip.ui.newexam

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.compose.foundation.isSystemInDarkTheme
import com.deniz0706.ykstakip.data.ThemeManager
import androidx.fragment.app.Fragment
import com.deniz0706.ykstakip.data.ExamRepository
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

    override fun onCreateView(inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?, savedInstanceState: Bundle?): View {
        val repository = ExamRepository.getInstance(requireContext())
        val id = arguments?.getLong(ARG_EDIT_ID, -1L)?.takeIf { it > 0 }
        val existing = id?.let { examId -> repository.getAllExams().find { it.id == examId } }
        val initialType = existing?.type ?: arguments?.getString(ARG_TYPE)
            ?.let { runCatching { ExamType.valueOf(it) }.getOrNull() } ?: ExamType.TYT
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val dark = activeDarkTheme(context)
                YksTheme(darkTheme = dark) {
                NewExamScreenV2(
                    initialType = initialType,
                    existing = existing,
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
}
