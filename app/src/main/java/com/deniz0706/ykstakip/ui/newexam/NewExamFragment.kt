package com.deniz0706.ykstakip.ui.newexam

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.deniz0706.ykstakip.R
import com.deniz0706.ykstakip.data.ExamRepository
import com.deniz0706.ykstakip.data.ExamValidator
import com.deniz0706.ykstakip.model.AytField
import com.deniz0706.ykstakip.model.Exam
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.model.SubjectConfig
import com.deniz0706.ykstakip.model.SubjectConfigs
import com.deniz0706.ykstakip.model.SubjectResult
import com.deniz0706.ykstakip.ui.MainActivity
import com.deniz0706.ykstakip.util.Fmt
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NewExamFragment : Fragment(R.layout.fragment_new_exam) {

    private lateinit var repository: ExamRepository
    private var selectedType = ExamType.TYT
    private var selectedAytField = AytField.SAYISAL
    private var editingId: Long? = null
    private var selectedDate: Calendar = Calendar.getInstance()

    private data class SubjectRow(
        val config: SubjectConfig,
        val etCorrect: EditText,
        val etWrong: EditText,
        val etTime: EditText,
        val etTopics: EditText,
        val tvNet: TextView,
        val tvBlank: TextView,
        val tvError: TextView
    )

    private val rows = mutableListOf<SubjectRow>()

    companion object {
        private const val ARG_EDIT_ID = "edit_id"
        private const val ARG_TYPE = "exam_type"

        fun newInstanceForType(type: ExamType): NewExamFragment {
            val f = NewExamFragment()
            f.arguments = Bundle().apply { putString(ARG_TYPE, type.name) }
            return f
        }

        fun newInstanceForEdit(examId: Long): NewExamFragment {
            val f = NewExamFragment()
            f.arguments = Bundle().apply { putLong(ARG_EDIT_ID, examId) }
            return f
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = ExamRepository.getInstance(requireContext())

        val editId = arguments?.getLong(ARG_EDIT_ID, -1L)?.takeIf { it > 0 }
        editingId = editId

        if (editId == null) {
            arguments?.getString(ARG_TYPE)?.let {
                selectedType = ExamType.valueOf(it)
            }
        }

        view.findViewById<View>(R.id.btnCancel).setOnClickListener {
            (activity as? MainActivity)?.goBack()
        }

        view.findViewById<View>(R.id.toggleTyt).setOnClickListener {
            setType(ExamType.TYT)
        }

        view.findViewById<View>(R.id.toggleAyt).setOnClickListener {
            setType(ExamType.AYT)
        }

        view.findViewById<View>(R.id.fieldSayisal).setOnClickListener {
            setAytField(AytField.SAYISAL)
        }

        view.findViewById<View>(R.id.fieldEa).setOnClickListener {
            setAytField(AytField.ESIT_AGIRLIK)
        }

        view.findViewById<View>(R.id.fieldSozel).setOnClickListener {
            setAytField(AytField.SOZEL)
        }

        updateDateLabel()

        view.findViewById<View>(R.id.tvDatePicker).setOnClickListener {
            showDatePicker()
        }

        view.findViewById<View>(R.id.btnSave).setOnClickListener {
            onSaveClicked()
        }

        if (editId != null) {
            val exam = repository.getAllExams().find { it.id == editId }

            if (exam != null) {
                view.findViewById<TextView>(R.id.tvScreenTitle).text =
                    "Denemeyi Düzenle"

                view.findViewById<EditText>(R.id.etTitle)
                    .setText(exam.title)

                view.findViewById<EditText>(R.id.etNotes)
                    .setText(exam.notes)

                view.findViewById<EditText>(R.id.etPublisher)
                    .setText(exam.publisher)

                selectedType = exam.type

                if (selectedType == ExamType.AYT) {
                    selectedAytField =
                        exam.aytField ?: AytField.SAYISAL
                }

                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.US
                ).parse(exam.date)?.let {
                    selectedDate.time = it
                }

                updateDateLabel()
                buildSubjectRows()
                prefill(exam)
                updateTotals()
                return
            }
        }

        buildSubjectRows()
        updateTypeToggle()
        updateAytFieldUi()
        updateTotals()
    }

    private fun setType(type: ExamType) {
        if (type == selectedType) return

        selectedType = type
        buildSubjectRows()
        updateTypeToggle()
        updateAytFieldUi()
        updateTotals()
    }

    private fun setAytField(field: AytField) {
        if (selectedType != ExamType.AYT) return
        if (field == selectedAytField) return

        selectedAytField = field
        buildSubjectRows()
        updateAytFieldUi()
        updateTotals()
    }

    private fun updateAytFieldUi() {
        val v = view ?: return

        val container =
            v.findViewById<View>(R.id.aytFieldContainer)

        container.visibility =
            if (selectedType == ExamType.AYT) {
                View.VISIBLE
            } else {
                View.GONE
            }

        v.findViewById<View>(R.id.fieldSayisal).isSelected =
            selectedAytField == AytField.SAYISAL

        v.findViewById<View>(R.id.fieldEa).isSelected =
            selectedAytField == AytField.ESIT_AGIRLIK

        v.findViewById<View>(R.id.fieldSozel).isSelected =
            selectedAytField == AytField.SOZEL
    }

    private fun updateTypeToggle() {
        val v = view ?: return

        v.findViewById<View>(R.id.toggleTyt).isSelected =
            selectedType == ExamType.TYT

        v.findViewById<View>(R.id.toggleAyt).isSelected =
            selectedType == ExamType.AYT
    }

    private fun updateDateLabel() {
        view?.findViewById<TextView>(R.id.tvDatePicker)?.text =
            SimpleDateFormat(
                "d MMMM yyyy",
                Locale("tr", "TR")
            ).format(selectedDate.time)
    }

    private fun showDatePicker() {
        DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                selectedDate.set(y, m, d)
                updateDateLabel()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun buildSubjectRows() {
        val container =
            view?.findViewById<android.widget.LinearLayout>(
                R.id.llSubjectsContainer
            ) ?: return

        container.removeAllViews()
        rows.clear()

        val inflater = LayoutInflater.from(requireContext())

        val subjectConfigs =
            if (selectedType == ExamType.AYT) {
                SubjectConfigs.subjectsFor(selectedAytField)
            } else {
                SubjectConfigs.subjectsFor(selectedType)
            }

        for (config in subjectConfigs) {
            val itemView =
                inflater.inflate(
                    R.layout.item_subject_input,
                    container,
                    false
                )

            itemView.findViewById<TextView>(
                R.id.tvSubjectTitle
            ).text = "${config.name}  •  ${config.questionCount} soru"

            val etCorrect =
                itemView.findViewById<EditText>(R.id.etCorrect)

            val etWrong =
                itemView.findViewById<EditText>(R.id.etWrong)

            val etTime =
                itemView.findViewById<EditText>(R.id.etTime)

            val etTopics =
                itemView.findViewById<EditText>(R.id.etTopics)

            val tvNet =
                itemView.findViewById<TextView>(R.id.tvSubjectNet)

            val tvBlank =
                itemView.findViewById<TextView>(R.id.tvSubjectBlank)

            val tvError =
                itemView.findViewById<TextView>(R.id.tvSubjectError)

            val row =
                SubjectRow(
                    config,
                    etCorrect,
                    etWrong,
                    etTime,
                    etTopics,
                    tvNet,
                    tvBlank,
                    tvError
                )

            rows.add(row)

            // Alanın içindeki varsayılan 0'a ilk dokunuşta
            // 0'ın tamamını seç. Böylece örneğin 5 yazınca
            // "05" değil direkt "5" olur.
            selectDefaultZeroOnFocus(etCorrect)
            selectDefaultZeroOnFocus(etWrong)
            selectDefaultZeroOnFocus(etTime)

            val watcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    a: Int,
                    b: Int,
                    c: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    a: Int,
                    b: Int,
                    c: Int
                ) {
                }

                override fun afterTextChanged(s: Editable?) {
                    clampField(
                        etCorrect,
                        config.questionCount
                    )

                    clampField(
                        etWrong,
                        config.questionCount
                    )

                    onRowChanged(row)
                    updateTotals()
                }
            }

            etCorrect.addTextChangedListener(watcher)
            etWrong.addTextChangedListener(watcher)

            etTime.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        a: Int,
                        b: Int,
                        c: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        a: Int,
                        b: Int,
                        c: Int
                    ) {
                    }

                    override fun afterTextChanged(
                        s: Editable?
                    ) {
                        updateTotals()
                    }
                }
            )

            container.addView(itemView)
            onRowChanged(row)
        }

        updateSaveButtonState()
    }

    private fun selectDefaultZeroOnFocus(
    editText: EditText
) {
    editText.setOnFocusChangeListener { _, hasFocus ->
        if (hasFocus && editText.text.toString() == "0") {
            editText.selectAll()
        }
    }

    editText.setOnClickListener {
        if (editText.text.toString() == "0") {
            editText.selectAll()
        }
    }
}

    
    private fun clampField(
        et: EditText,
        max: Int
    ) {
        val v =
            et.text
                .toString()
                .toIntOrNull() ?: 0

        if (v > max) {
            et.setText(max.toString())
            et.setSelection(et.text.length)
        }
    }

    private fun onRowChanged(row: SubjectRow) {
        val correct =
            row.etCorrect.text
                .toString()
                .toIntOrNull() ?: 0

        val wrong =
            row.etWrong.text
                .toString()
                .toIntOrNull() ?: 0

        val result =
            ExamValidator.validatePair(
                correct,
                wrong,
                row.config
            )

        if (result is ExamValidator.FieldResult.Invalid) {
            row.tvError.text = result.message
            row.tvError.visibility = View.VISIBLE
        } else {
            row.tvError.visibility = View.GONE
        }

        val net =
            Fmt.round2(
                correct - wrong / 4.0
            )

        row.tvNet.text =
            "${Fmt.net(net)} net"

        val blank =
            (
                row.config.questionCount -
                    correct -
                    wrong
                ).coerceAtLeast(0)

        row.tvBlank.text =
            blank.toString()

        updateSaveButtonState()
    }

    private fun updateTotals() {
        val v = view ?: return

        var totalCorrect = 0
        var totalWrong = 0
        var totalBlank = 0
        var totalNet = 0.0
        var totalTime = 0

        for (row in rows) {
            val correct =
                row.etCorrect.text
                    .toString()
                    .toIntOrNull() ?: 0

            val wrong =
                row.etWrong.text
                    .toString()
                    .toIntOrNull() ?: 0

            val time =
                row.etTime.text
                    .toString()
                    .toIntOrNull() ?: 0

            val blank =
                (
                    row.config.questionCount -
                        correct -
                        wrong
                    ).coerceAtLeast(0)

            totalCorrect += correct
            totalWrong += wrong
            totalBlank += blank
            totalNet += correct - wrong / 4.0
            totalTime += time
        }

        v.findViewById<TextView>(
            R.id.tvTotalNet
        ).text =
            "${Fmt.net(Fmt.round2(totalNet))} net"

        v.findViewById<TextView>(
            R.id.tvTotalBreakdown
        ).text =
            "$totalCorrect doğru · $totalWrong yanlış · $totalBlank boş · $totalTime dk"

        val warning =
            v.findViewById<TextView>(
                R.id.tvTimeWarning
            )

        if (totalTime > selectedType.totalTimeMinutes) {
            warning.visibility = View.VISIBLE

            warning.text =
                "Toplam süre: $totalTime / ${selectedType.totalTimeMinutes} dakika — sınav süresini aştınız."
        } else {
            warning.visibility = View.GONE
        }

        updateSaveButtonState()
    }

    private fun updateSaveButtonState() {
        val v = view ?: return

        val btn =
            v.findViewById<TextView>(R.id.btnSave)

        val anyInvalid =
            rows.any { row ->
                val correct =
                    row.etCorrect.text
                        .toString()
                        .toIntOrNull() ?: 0

                val wrong =
                    row.etWrong.text
                        .toString()
                        .toIntOrNull() ?: 0

                ExamValidator.validatePair(
                    correct,
                    wrong,
                    row.config
                ) is ExamValidator.FieldResult.Invalid
            }

        val totalTime =
            rows.sumOf {
                it.etTime.text
                    .toString()
                    .toIntOrNull() ?: 0
            }

        val timeExceeded =
            totalTime > selectedType.totalTimeMinutes

        val valid =
            !anyInvalid && !timeExceeded

        btn.isEnabled = valid
        btn.alpha =
            if (valid) 1f else 0.4f
    }

    private fun prefill(exam: Exam) {
        for (row in rows) {
            val subj =
                exam.subject(row.config.name)
                    ?: continue

            row.etCorrect.setText(
                subj.correct.toString()
            )

            row.etWrong.setText(
                subj.wrong.toString()
            )

            row.etTime.setText(
                subj.timeMinutes.toString()
            )

            row.etTopics.setText(
                subj.weakTopics.joinToString(", ")
            )

            onRowChanged(row)
        }
    }

    private fun onSaveClicked() {
        val v = view ?: return

        val titleInput =
            v.findViewById<EditText>(R.id.etTitle)

        val title =
            titleInput.text
                .toString()
                .trim()

        if (title.isEmpty()) {
            titleInput.error = "Deneme adı gir"
            return
        }

        val entries =
            rows.map { row ->
                Triple(
                    row.config,
                    row.etCorrect.text
                        .toString()
                        .toIntOrNull() ?: 0,
                    row.etWrong.text
                        .toString()
                        .toIntOrNull() ?: 0
                )
            }

        val timesBySubject =
            rows.associate {
                it.config.name to (
                    it.etTime.text
                        .toString()
                        .toIntOrNull() ?: 0
                    )
            }

        val validation =
            ExamValidator.validateWholeExam(
                selectedType,
                entries,
                timesBySubject
            )

        if (validation is ExamValidator.FieldResult.Invalid) {
            android.widget.Toast.makeText(
                requireContext(),
                validation.message,
                android.widget.Toast.LENGTH_LONG
            ).show()

            return
        }

        val subjects =
            rows.map { row ->
                SubjectResult(
                    subject = row.config.name,
                    questionCount = row.config.questionCount,
                    correct =
                        row.etCorrect.text
                            .toString()
                            .toIntOrNull() ?: 0,
                    wrong =
                        row.etWrong.text
                            .toString()
                            .toIntOrNull() ?: 0,
                    timeMinutes =
                        row.etTime.text
                            .toString()
                            .toIntOrNull() ?: 0,
                    weakTopics =
                        row.etTopics.text
                            .toString()
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                )
            }

        val exam =
            Exam(
                id =
                    editingId
                        ?: repository.nextId(),
                title = title,
                date =
                    SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.US
                    ).format(selectedDate.time),
                type = selectedType,
                aytField =
                    if (selectedType == ExamType.AYT) {
                        selectedAytField
                    } else {
                        null
                    },
                subjects = subjects,
                notes =
                    v.findViewById<EditText>(
                        R.id.etNotes
                    ).text.toString().trim(),
                publisher =
                    v.findViewById<EditText>(
                        R.id.etPublisher
                    ).text.toString().trim()
            )

        repository.saveExam(exam)
        (activity as? MainActivity)?.closeAndReturnHome()
    }
}
