package com.deniz0706.ykstakip

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.round

class MainActivity : AppCompatActivity() {

    private val darkBg = Color.rgb(18, 18, 20)
    private val cardDark = Color.rgb(30, 30, 33)
    private val textDark = Color.WHITE
    private val mutedDark = Color.rgb(160, 160, 165)

    private val lightBg = Color.rgb(247, 247, 249)
    private val cardLight = Color.WHITE
    private val textLight = Color.rgb(20, 20, 22)
    private val mutedLight = Color.rgb(110, 110, 115)

    private var isDark = true
    private var selectedExamType = "TYT"

    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = darkBg
        window.navigationBarColor = darkBg

        showHome()
    }

    private fun setupRoot() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(if (isDark) darkBg else lightBg)
        }

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 12)
        }

        val scroll = ScrollView(this).apply {
            addView(
                content,
                ScrollView.LayoutParams(
                    ScrollView.LayoutParams.MATCH_PARENT,
                    ScrollView.LayoutParams.MATCH_PARENT
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

    private fun showHome() {
        setupRoot()
        content.removeAllViews()

        addTitle("YKS Takip")
        addSubtitle("26 Eylül 2026")

        val switchRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val tyt = button("TYT")
        val ayt = button("AYT")

        tyt.setOnClickListener {
            selectedExamType = "TYT"
            showHome()
        }

        ayt.setOnClickListener {
            selectedExamType = "AYT"
            showHome()
        }

        switchRow.addView(tyt, LinearLayout.LayoutParams(0, 52, 1f))
        switchRow.addView(space(8))
        switchRow.addView(ayt, LinearLayout.LayoutParams(0, 52, 1f))

        content.addView(switchRow)

        content.addView(space(20))

        val lastCard = card()

        val lastLabel = text(
            "SON DENEME",
            13f,
            if (isDark) mutedDark else mutedLight
        )

        val lastNet = text(
            "—",
            46f,
            if (isDark) textDark else textLight
        )

        lastCard.addView(lastLabel)
        lastCard.addView(lastNet)

        val examTypeText = text(
            selectedExamType,
            14f,
            if (isDark) mutedDark else mutedLight
        )

        lastCard.addView(examTypeText)

        content.addView(
            lastCard,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        content.addView(space(14))

        val stats = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        stats.addView(
            statCard("Ortalama", "—"),
            LinearLayout.LayoutParams(0, 130, 1f)
        )
        stats.addView(space(8))
        stats.addView(
            statCard("En yüksek", "—"),
            LinearLayout.LayoutParams(0, 130, 1f)
        )
        stats.addView(space(8))
        stats.addView(
            statCard("Son 5", "—"),
            LinearLayout.LayoutParams(0, 130, 1f)
        )

        content.addView(stats)

        content.addView(space(20))

        val newExam = button("＋  Yeni Deneme").apply {
            textSize = 17f
            setOnClickListener {
                showNewExam()
            }
        }

        content.addView(
            newExam,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                58
            )
        )
    }

    private fun showNewExam() {
        setupRoot()
        content.removeAllViews()

        addTitle("Yeni Deneme")
        addSubtitle("$selectedExamType deneme sonucu gir")

        content.addView(space(20))

        val nameInput = makeInput(
            "Deneme adı",
            false
        )
        content.addView(nameInput)

        content.addView(space(12))

        val subjects = if (selectedExamType == "TYT") {
            listOf(
                Triple("Türkçe", 40, "turkce"),
                Triple("Sosyal", 20, "sosyal"),
                Triple("Matematik", 40, "matematik"),
                Triple("Fen", 20, "fen")
            )
        } else {
            listOf(
                Triple("Matematik", 40, "matematik"),
                Triple("Fen", 40, "fen")
            )
        }

        val correctInputs = mutableMapOf<String, EditText>()
        val wrongInputs = mutableMapOf<String, EditText>()

        for ((subject, count, key) in subjects) {
            val subjectCard = card()

            subjectCard.addView(
                text(
                    "$subject  •  $count soru",
                    18f,
                    if (isDark) textDark else textLight
                )
            )

            subjectCard.addView(space(10))

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val correct = makeNumberInput("Doğru")
            val wrong = makeNumberInput("Yanlış")

            correctInputs[key] = correct
            wrongInputs[key] = wrong

            row.addView(
                correct,
                LinearLayout.LayoutParams(0, 55, 1f)
            )
            row.addView(space(8))
            row.addView(
                wrong,
                LinearLayout.LayoutParams(0, 55, 1f)
            )

            subjectCard.addView(row)

            content.addView(subjectCard)

            content.addView(space(10))
        }

        val timeInput = makeNumberInput("Toplam süre (dk)")
        content.addView(timeInput)

        content.addView(space(18))

        val resultText = text(
            "",
            16f,
            if (isDark) textDark else textLight
        )

        content.addView(resultText)

        content.addView(space(10))

        val saveButton = button("Kaydet").apply {
            textSize = 17f

            setOnClickListener {
                var totalCorrect = 0
                var totalWrong = 0
                var valid = true

                for ((subject, count, key) in subjects) {
                    val correct = correctInputs[key]?.text?.toString()?.toIntOrNull()
                    val wrong = wrongInputs[key]?.text?.toString()?.toIntOrNull()

                    if (correct == null || wrong == null ||
                        correct < 0 || wrong < 0 ||
                        correct + wrong > count
                    ) {
                        valid = false
                        break
                    }

                    totalCorrect += correct
                    totalWrong += wrong
                }

                val time = timeInput.text.toString().toDoubleOrNull()
                    ?: 0.0

                val maxTime = if (selectedExamType == "TYT") 165.0 else 180.0

                if (time < 0 || time > maxTime) {
                    valid = false
                }

                if (!valid) {
                    resultText.text =
                        "Bilgileri kontrol et. Doğru + yanlış soru sayısını geçemez."
                    return@setOnClickListener
                }

                val net = totalCorrect - totalWrong / 4.0

                resultText.text =
                    "Toplam: $totalCorrect doğru • $totalWrong yanlış\n" +
                    "Net: ${formatNet(net)}"

                Toast.makeText(
                    this@MainActivity,
                    "Deneme kaydedildi",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        content.addView(
            saveButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                58
            )
        )
    }

    private fun showHistory() {
        setupRoot()
        content.removeAllViews()

        addTitle("Geçmiş Denemeler")
        addSubtitle("Kaydettiğin denemeler burada görünecek.")

        content.addView(space(30))

        val empty = text(
            "Henüz deneme kaydı yok.",
            17f,
            if (isDark) mutedDark else mutedLight
        )

        empty.gravity = Gravity.CENTER

        content.addView(
            empty,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                200
            )
        )
    }

    private fun showStats() {
        setupRoot()
        content.removeAllViews()

        addTitle("İstatistikler")
        addSubtitle("Deneme performansını incele.")

        content.addView(space(30))

        content.addView(
            text(
                "Henüz yeterli veri yok.",
                17f,
                if (isDark) mutedDark else mutedLight
            )
        )
    }

    private fun showAppearance() {
        setupRoot()
        content.removeAllViews()

        addTitle("Görünüm")
        addSubtitle("Uygulamanın temasını seç.")

        content.addView(space(24))

        val darkButton = button("Koyu tema")
        val lightButton = button("Açık tema")

        darkButton.setOnClickListener {
            isDark = true
            showAppearance()
        }

        lightButton.setOnClickListener {
            isDark = false
            showAppearance()
        }

        content.addView(
            darkButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                56
            )
        )

        content.addView(space(10))

        content.addView(
            lightButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                56
            )
        )
    }

    private fun createBottomBar(): View {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(10, 10, 10, 10)
            setBackgroundColor(if (isDark) Color.rgb(35, 35, 38) else Color.WHITE)
        }

        val home = navButton("Ana Sayfa")
        val history = navButton("Geçmiş")
        val stats = navButton("İstatistik")
        val appearance = navButton("Görünüm")

        home.setOnClickListener { showHome() }
        history.setOnClickListener { showHistory() }
        stats.setOnClickListener { showStats() }
        appearance.setOnClickListener { showAppearance() }

        bar.addView(home, LinearLayout.LayoutParams(0, 60, 1f))
        bar.addView(history, LinearLayout.LayoutParams(0, 60, 1f))
        bar.addView(stats, LinearLayout.LayoutParams(0, 60, 1f))
        bar.addView(appearance, LinearLayout.LayoutParams(0, 60, 1f))

        return bar
    }

    private fun addTitle(value: String) {
        content.addView(
            text(
                value,
                32f,
                if (isDark) textDark else textLight
            )
        )
    }

    private fun addSubtitle(value: String) {
        content.addView(
            text(
                value,
                15f,
                if (isDark) mutedDark else mutedLight
            )
        )
    }

    private fun card(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            setBackgroundColor(if (isDark) cardDark else cardLight)
        }
    }

    private fun statCard(title: String, value: String): LinearLayout {
        val card = card()

        card.addView(
            text(
                title,
                13f,
                if (isDark) mutedDark else mutedLight
            )
        )

        card.addView(space(8))

        card.addView(
            text(
                value,
                24f,
                if (isDark) textDark else textLight
            )
        )

        return card
    }

    private fun makeInput(
        hint: String,
        multiline: Boolean
    ): EditText {
        return EditText(this).apply {
            this.hint = hint
            textSize = 16f
            setTextColor(if (isDark) textDark else textLight)
            setHintTextColor(if (isDark) mutedDark else mutedLight)
            setPadding(16, 0, 16, 0)

            if (multiline) {
                minLines = 3
            }

            background = null
        }
    }

    private fun makeNumberInput(hint: String): EditText {
        return EditText(this).apply {
            this.hint = hint
            textSize = 16f
            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER or
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setTextColor(if (isDark) textDark else textLight)
            setHintTextColor(if (isDark) mutedDark else mutedLight)
            setPadding(16, 0, 16, 0)
            background = null
        }
    }

    private fun button(label: String): Button {
        return Button(this).apply {
            text = label
            textSize = 15f
            isAllCaps = false
            setTextColor(if (isDark) textDark else textLight)
            setBackgroundColor(if (isDark) cardDark else cardLight)
        }
    }

    private fun navButton(label: String): Button {
        return Button(this).apply {
            text = label
            textSize = 12f
            isAllCaps = false
            setTextColor(if (isDark) textDark else textLight)
            background = null
        }
    }

    private fun text(
        value: String,
        size: Float,
        color: Int
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
        }
    }

    private fun space(height: Int): Space {
        return Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                1,
                height
            )
        }
    }

    private fun formatNet(value: Double): String {
        val rounded = round(value * 100) / 100
        return if (rounded % 1.0 == 0.0) {
            rounded.toInt().toString()
        } else {
            rounded.toString()
        }
    }
}
