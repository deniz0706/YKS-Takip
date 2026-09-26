package com.deniz0706.ykstakip

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

class MainActivity : Activity() {

    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout

    private var selectedExamType = "TYT"
    private var darkMode = true
    private var currentPage = "home"

    private val exams = mutableListOf<Exam>()

    data class Exam(
        val type: String,
        val name: String,
        val date: String,
        val net: Double
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupRoot()
        showHome()
    }

    // ---------------------------------------------------------
    // ROOT
    // ---------------------------------------------------------

    private fun setupRoot() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bgColor())
            fitsSystemWindows = true
        }

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(24), dp(22), dp(20))
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(
                content,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        root.addView(createBottomBar())

        setContentView(root)
    }

    // ---------------------------------------------------------
    // HOME
    // ---------------------------------------------------------

    private fun showHome() {
        currentPage = "home"
        setupRoot()
        content.removeAllViews()

        addTitle("YKS Takip")
        addText(
            SimpleDateFormat("d MMMM yyyy", Locale("tr", "TR")).format(Date()),
            14,
            secondaryColor()
        )

        addSpace(26)

        createExamSwitch()

        addSpace(24)

        val lastExam = exams.lastOrNull { it.type == selectedExamType }

        addText(
            "Son Deneme",
            15,
            secondaryColor()
        )

        addSpace(6)

        val netText = if (lastExam != null) {
            formatNet(lastExam.net)
        } else {
            "—"
        }

        addText(
            "$netText net",
            44,
            textColor(),
            true
        )

        addSpace(22)

        val stats = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        addStatCard(
            stats,
            "Ortalama",
            averageFor(selectedExamType)
        )

        addStatCard(
            stats,
            "En Yüksek",
            highestFor(selectedExamType)
        )

        addStatCard(
            stats,
            "Son 5",
            lastFiveAverage(selectedExamType)
        )

        content.addView(
            stats,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        addSpace(28)

        val newButton = roundedButton(
            "＋  Yeni Deneme",
            accentColor()
        )

        newButton.setOnClickListener {
            showNewExam()
        }

        content.addView(
            newButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(58)
            )
        )

        addSpace(18)

        if (lastExam == null) {
            addCard {
                addText(
                    "Henüz deneme eklemedin.",
                    16,
                    textColor(),
                    true
                )
                addSpace(6)
                addText(
                    "İlk denemeni ekleyerek takip etmeye başlayabilirsin.",
                    14,
                    secondaryColor()
                )
            }
        }
    }

    private fun createExamSwitch() {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val tyt = roundedButton(
            "TYT",
            if (selectedExamType == "TYT") accentColor() else cardColor()
        )

        val ayt = roundedButton(
            "AYT",
            if (selectedExamType == "AYT") accentColor() else cardColor()
        )

        tyt.setOnClickListener {
            selectedExamType = "TYT"
            showHome()
        }

        ayt.setOnClickListener {
            selectedExamType = "AYT"
            showHome()
        }

        row.addView(
            tyt,
            LinearLayout.LayoutParams(
                0,
                dp(48),
                1f
            )
        )

        row.addView(
            Space(this),
            LinearLayout.LayoutParams(
                dp(10),
                1
            )
        )

        row.addView(
            ayt,
            LinearLayout.LayoutParams(
                0,
                dp(48),
                1f
            )
        )

        content.addView(row)
    }

    // ---------------------------------------------------------
    // NEW EXAM
    // ---------------------------------------------------------

    private fun showNewExam() {
        currentPage = "new"
        setupRoot()
        content.removeAllViews()

        addText(
            "Yeni Deneme",
            30,
            textColor(),
            true
        )

        addSpace(8)

        addText(
            "$selectedExamType denemesi",
            15,
            secondaryColor()
        )

        addSpace(24)

        val nameInput = input(
            "Deneme adı",
            "Örn. Dershane Denemesi 1"
        )

        content.addView(nameInput)

        addSpace(16)

        val subjects = if (selectedExamType == "TYT") {
            listOf(
                "Türkçe" to 40,
                "Sosyal" to 20,
                "Matematik" to 40,
                "Fen" to 20
            )
        } else {
            listOf(
                "Matematik" to 40,
                "Fen" to 40
            )
        }

        val fields = mutableListOf<Pair<String, EditText>>()

        for ((subject, count) in subjects) {
            addText(
                "$subject  •  $count soru",
                15,
                textColor(),
                true
            )

            addSpace(8)

            val correct = input(
                "Doğru",
                "0",
                integer = true
            )

            val wrong = input(
                "Yanlış",
                "0",
                integer = true
            )

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            row.addView(
                correct,
                LinearLayout.LayoutParams(
                    0,
                    dp(54),
                    1f
                )
            )

            row.addView(
                Space(this),
                LinearLayout.LayoutParams(dp(10), 1)
            )

            row.addView(
                wrong,
                LinearLayout.LayoutParams(
                    0,
                    dp(54),
                    1f
                )
            )

            content.addView(row)

            fields.add("$subject-d" to correct)
            fields.add("$subject-y" to wrong)

            addSpace(16)
        }

        val save = roundedButton(
            "Denemeyi Kaydet",
            accentColor()
        )

        save.setOnClickListener {
            val name = nameInput.text.toString().trim()

            if (name.isEmpty()) {
                nameInput.error = "Deneme adı gir"
                return@setOnClickListener
            }

            var totalNet = 0.0
            var valid = true

            for ((key, edit) in fields) {
                val value = edit.text.toString().toIntOrNull() ?: 0

                if (value < 0) {
                    valid = false
                }

                if (key.endsWith("-d")) {
                    totalNet += value
                } else {
                    totalNet -= value / 4.0
                }
            }

            if (!valid) return@setOnClickListener

            exams.add(
                Exam(
                    selectedExamType,
                    name,
                    SimpleDateFormat(
                        "dd.MM.yyyy",
                        Locale("tr", "TR")
                    ).format(Date()),
                    totalNet
                )
            )

            showHome()
        }

        content.addView(
            save,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(58)
            )
        )

        addSpace(20)

        val cancel = roundedButton(
            "Vazgeç",
            cardColor()
        )

        cancel.setOnClickListener {
            showHome()
        }

        content.addView(
            cancel,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(54)
            )
        )
    }

    // ---------------------------------------------------------
    // HISTORY
    // ---------------------------------------------------------

    private fun showHistory() {
        currentPage = "history"
        setupRoot()
        content.removeAllViews()

        addTitle("Geçmiş Denemeler")

        addSpace(8)

        addText(
            "Kaydedilen tüm denemelerin",
            14,
            secondaryColor()
        )

        addSpace(24)

        if (exams.isEmpty()) {
            addCard {
                addText(
                    "Henüz deneme yok.",
                    17,
                    textColor(),
                    true
                )
            }
            return
        }

        exams.asReversed().forEach { exam ->
            addCard {
                val top = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                }

                val left = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                }

                left.addView(
                    textView(
                        exam.name,
                        17,
                        textColor(),
                        true
                    )
                )

                left.addView(
                    textView(
                        "${exam.type}  •  ${exam.date}",
                        13,
                        secondaryColor()
                    )
                )

                top.addView(
                    left,
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                )

                top.addView(
                    textView(
                        "${formatNet(exam.net)}",
                        22,
                        textColor(),
                        true
                    )
                )

                addView(top)
            }

            addSpace(10)
        }
    }

    // ---------------------------------------------------------
    // STATS
    // ---------------------------------------------------------

    private fun showStats() {
        currentPage = "stats"
        setupRoot()
        content.removeAllViews()

        addTitle("İstatistikler")

        addSpace(8)

        addText(
            selectedExamType,
            15,
            secondaryColor()
        )

        addSpace(24)

        if (exams.none { it.type == selectedExamType }) {
            addCard {
                addText(
                    "Henüz yeterli veri yok.",
                    17,
                    textColor(),
                    true
                )
            }
            return
        }

        addStatLine(
            "Son",
            formatNet(lastFor(selectedExamType))
        )

        addStatLine(
            "Ortalama",
            formatNet(averageFor(selectedExamType))
        )

        addStatLine(
            "En yüksek",
            formatNet(highestFor(selectedExamType))
        )

        addStatLine(
            "En düşük",
            formatNet(lowestFor(selectedExamType))
        )

        addStatLine(
            "Deneme sayısı",
            exams.count { it.type == selectedExamType }.toString()
        )
    }

    // ---------------------------------------------------------
    // APPEARANCE
    // ---------------------------------------------------------

    private fun showAppearance() {
        currentPage = "appearance"
        setupRoot()
        content.removeAllViews()

        addTitle("Görünüm")

        addSpace(24)

        addText(
            "Tema",
            15,
            secondaryColor()
        )

        addSpace(10)

        val dark = roundedButton(
            "Koyu",
            if (darkMode) accentColor() else cardColor()
        )

        dark.setOnClickListener {
            darkMode = true
            showAppearance()
        }

        content.addView(
            dark,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(54)
            )
        )

        addSpace(10)

        val light = roundedButton(
            "Açık",
            if (!darkMode) accentColor() else cardColor()
        )

        light.setOnClickListener {
            darkMode = false
            showAppearance()
        }

        content.addView(
            light,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(54)
            )
        )

        addSpace(20)

        addText(
            "Tema değişiklikleri uygulama yeniden açıldığında şu an korunmaz.",
            13,
            secondaryColor()
        )
    }

    // ---------------------------------------------------------
    // BOTTOM BAR
    // ---------------------------------------------------------

    private fun createBottomBar(): View {
        val wrapper = FrameLayout(this).apply {
            setPadding(
                dp(14),
                dp(8),
                dp(14),
                dp(14)
            )
        }

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(
                dp(8),
                dp(7),
                dp(8),
                dp(7)
            )
            setBackgroundColor(cardColor())
        }

        val items = listOf(
            Triple("⌂", "Ana", "home"),
            Triple("≡", "Geçmiş", "history"),
            Triple("⌁", "İstatistik", "stats"),
            Triple("◐", "Görünüm", "appearance")
        )

        for ((icon, label, page) in items) {
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(
                    dp(8),
                    dp(4),
                    dp(8),
                    dp(4)
                )
                isClickable = true
                isFocusable = true
            }

            val iconView = textView(
                icon,
                22,
                if (currentPage == page) accentColor() else textColor(),
                true
            )

            val labelView = textView(
                label,
                11,
                if (currentPage == page) accentColor() else secondaryColor(),
                false
            )

            item.addView(iconView)
            item.addView(labelView)

            item.setOnClickListener {
                when (page) {
                    "home" -> showHome()
                    "history" -> showHistory()
                    "stats" -> showStats()
                    "appearance" -> showAppearance()
                }
            }

            bar.addView(
                item,
                LinearLayout.LayoutParams(
                    0,
                    dp(58),
                    1f
                )
            )
        }

        wrapper.addView(
            bar,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dp(70),
                Gravity.BOTTOM
            )
        )

        return wrapper
    }

    // ---------------------------------------------------------
    // UI HELPERS
    // ---------------------------------------------------------

    private fun addTitle(value: String) {
        addText(value, 32, textColor(), true)
    }

    private fun addText(
        value: String,
        size: Int,
        color: Int,
        bold: Boolean = false
    ) {
        content.addView(
            textView(value, size, color, bold)
        )
    }

    private fun textView(
        value: String,
        size: Int,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size.toFloat()
            setTextColor(color)
            if (bold) {
                typeface = Typeface.DEFAULT_BOLD
            }
        }
    }

    private fun addSpace(height: Int) {
        content.addView(
            Space(this),
            LinearLayout.LayoutParams(
                1,
                dp(height)
            )
        )
    }

    private fun addCard(block: LinearLayout.() -> Unit) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(18)
            )
            setBackgroundColor(cardColor())
            block()
        }

        content.addView(
            card,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun addStatCard(
        parent: LinearLayout,
        title: String,
        value: Double?
    ) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(
                dp(8),
                dp(14),
                dp(8),
                dp(14)
            )
            setBackgroundColor(cardColor())
        }

        card.addView(
            textView(
                title,
                12,
                secondaryColor()
            )
        )

        card.addView(
            textView(
                if (value == null) "—" else formatNet(value),
                19,
                textColor(),
                true
            )
        )

        parent.addView(
            card,
            LinearLayout.LayoutParams(
                0,
                dp(82),
                1f
            )
        )
    }

    private fun addStatLine(
        title: String,
        value: String
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                dp(16),
                dp(18),
                dp(16),
                dp(18)
            )
            setBackgroundColor(cardColor())
        }

        row.addView(
            textView(
                title,
                15,
                secondaryColor()
            ),
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        row.addView(
            textView(
                value,
                18,
                textColor(),
                true
            )
        )

        content.addView(
            row,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        addSpace(8)
    }

    private fun roundedButton(
        text: String,
        color: Int
    ): Button {
        return Button(this).apply {
            this.text = text
            textSize = 15f
            setTextColor(
                if (color == accentColor()) Color.WHITE else textColor()
            )
            setBackgroundColor(color)
            isAllCaps = false
            stateListAnimator = null
        }
    }

    private fun input(
        hint: String,
        placeholder: String = "",
        integer: Boolean = false
    ): EditText {
        return EditText(this).apply {
            this.hint = hint
            setTextColor(textColor())
            setHintTextColor(secondaryColor())
            textSize = 15f
            setPadding(
                dp(14),
                dp(8),
                dp(14),
                dp(8)
            )

            if (integer) {
                inputType = InputType.TYPE_CLASS_NUMBER
            } else {
                inputType = InputType.TYPE_CLASS_TEXT
            }

            if (placeholder.isNotEmpty()) {
                contentDescription = placeholder
            }

            setBackgroundColor(cardColor())
        }
    }

    // ---------------------------------------------------------
    // STATS HELPERS
    // ---------------------------------------------------------

    private fun valuesFor(type: String): List<Double> {
        return exams
            .filter { it.type == type }
            .map { it.net }
    }

    private fun lastFor(type: String): Double {
        return valuesFor(type).lastOrNull() ?: 0.0
    }

    private fun averageFor(type: String): Double? {
        val values = valuesFor(type)
        return if (values.isEmpty()) null else values.average()
    }

    private fun highestFor(type: String): Double? {
        return valuesFor(type).maxOrNull()
    }

    private fun lowestFor(type: String): Double? {
        return valuesFor(type).minOrNull()
    }

    private fun lastFiveAverage(type: String): Double? {
        val values = valuesFor(type).takeLast(5)
        return if (values.isEmpty()) null else values.average()
    }

    private fun formatNet(value: Double): String {
        return String.format(Locale.US, "%.2f", max(0.0, value))
            .replace('.', ',')
    }

    // ---------------------------------------------------------
    // COLORS / DIMENSIONS
    // ---------------------------------------------------------

    private fun bgColor(): Int {
        return if (darkMode) Color.rgb(12, 12, 14)
        else Color.rgb(247, 247, 249)
    }

    private fun cardColor(): Int {
        return if (darkMode) Color.rgb(28, 28, 31)
        else Color.WHITE
    }

    private fun textColor(): Int {
        return if (darkMode) Color.WHITE
        else Color.rgb(20, 20, 22)
    }

    private fun secondaryColor(): Int {
        return if (darkMode) Color.rgb(160, 160, 166)
        else Color.rgb(105, 105, 112)
    }

    private fun accentColor(): Int {
        return Color.rgb(10, 132, 255)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
