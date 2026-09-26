package com.deniz0706.ykstakip

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

class MainActivity : Activity() {

    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout

    private var darkMode = true
    private var selectedExam = "TYT"

    private val darkBg = Color.rgb(10, 10, 12)
    private val darkCard = Color.rgb(25, 25, 28)
    private val lightBg = Color.rgb(247, 247, 249)
    private val lightCard = Color.WHITE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun textColor(): Int =
        if (darkMode) Color.WHITE else Color.rgb(20, 20, 22)

    private fun secondaryColor(): Int =
        if (darkMode) Color.rgb(165, 165, 170) else Color.rgb(105, 105, 110)

    private fun cardColor(): Int =
        if (darkMode) darkCard else lightCard

    private fun setupRoot() {
        root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(if (darkMode) darkBg else lightBg)

        setContentView(root)
    }

    private fun baseContent(): LinearLayout {
        setupRoot()

        val scroll = ScrollView(this)
        scroll.isFillViewport = true

        content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.setPadding(dp(24), dp(28), dp(24), dp(120))

        scroll.addView(
            content,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        addBottomIsland()

        return content
    }

    private fun addBottomIsland() {
        val island = LinearLayout(this)
        island.orientation = LinearLayout.HORIZONTAL
        island.gravity = Gravity.CENTER
        island.setPadding(dp(8), dp(8), dp(8), dp(8))

        val background = GradientDrawable()
        background.cornerRadius = dp(40).toFloat()
        background.setColor(
            if (darkMode) Color.rgb(30, 30, 34) else Color.WHITE
        )
        island.background = background
        island.elevation = dp(8).toFloat()

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(66)
        )
        params.setMargins(dp(18), 0, dp(18), dp(16))

        val items = listOf(
            Triple("⌂", "Ana Sayfa", "home"),
            Triple("◷", "Geçmiş", "history"),
            Triple("◒", "İstatistik", "stats"),
            Triple("☼", "Görünüm", "theme")
        )

        items.forEach { item ->
            val button = LinearLayout(this)
            button.orientation = LinearLayout.VERTICAL
            button.gravity = Gravity.CENTER
            button.setPadding(dp(4), 0, dp(4), 0)

            val icon = TextView(this)
            icon.text = item.first
            icon.textSize = 22f
            icon.gravity = Gravity.CENTER
            icon.setTextColor(textColor())

            val label = TextView(this)
            label.text = item.second
            label.textSize = 10f
            label.gravity = Gravity.CENTER
            label.setTextColor(secondaryColor())

            button.addView(icon)
            button.addView(label)

            button.setOnClickListener {
                when (item.third) {
                    "home" -> showHome()
                    "history" -> showHistory()
                    "stats" -> showStats()
                    "theme" -> showTheme()
                }
            }

            island.addView(
                button,
                LinearLayout.LayoutParams(0, dp(52), 1f)
            )
        }

        root.addView(island, params)
    }

    private fun addTitle(title: String, subtitle: String? = null) {
        val titleView = TextView(this)
        titleView.text = title
        titleView.textSize = 30f
        titleView.setTypeface(null, android.graphics.Typeface.BOLD)
        titleView.setTextColor(textColor())

        content.addView(
            titleView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        if (subtitle != null) {
            val sub = TextView(this)
            sub.text = subtitle
            sub.textSize = 15f
            sub.setTextColor(secondaryColor())
            sub.setPadding(0, dp(6), 0, dp(20))

            content.addView(sub)
        } else {
            val space = View(this)
            content.addView(
                space,
                LinearLayout.LayoutParams(
                    1,
                    dp(24)
                )
            )
        }
    }

    private fun addCard(
        title: String,
        value: String,
        subtitle: String = ""
    ) {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(dp(20), dp(18), dp(20), dp(18))

        val bg = GradientDrawable()
        bg.cornerRadius = dp(24).toFloat()
        bg.setColor(cardColor())
        card.background = bg

        val titleView = TextView(this)
        titleView.text = title
        titleView.textSize = 13f
        titleView.setTextColor(secondaryColor())

        val valueView = TextView(this)
        valueView.text = value
        valueView.textSize = 34f
        valueView.setTypeface(null, android.graphics.Typeface.BOLD)
        valueView.setTextColor(textColor())
        valueView.setPadding(0, dp(6), 0, 0)

        card.addView(titleView)
        card.addView(valueView)

        if (subtitle.isNotEmpty()) {
            val sub = TextView(this)
            sub.text = subtitle
            sub.textSize = 12f
            sub.setTextColor(secondaryColor())
            sub.setPadding(0, dp(4), 0, 0)
            card.addView(sub)
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, dp(14))

        content.addView(card, params)
    }

    private fun addExamSwitch() {
        val switch = LinearLayout(this)
        switch.orientation = LinearLayout.HORIZONTAL
        switch.setPadding(dp(5), dp(5), dp(5), dp(5))

        val bg = GradientDrawable()
        bg.cornerRadius = dp(30).toFloat()
        bg.setColor(cardColor())
        switch.background = bg

        val tyt = makeSwitchButton("TYT")
        val ayt = makeSwitchButton("AYT")

        switch.addView(tyt, LinearLayout.LayoutParams(0, dp(48), 1f))
        switch.addView(ayt, LinearLayout.LayoutParams(0, dp(48), 1f))

        tyt.setOnClickListener {
            selectedExam = "TYT"
            showHome()
        }

        ayt.setOnClickListener {
            selectedExam = "AYT"
            showHome()
        }

        content.addView(
            switch,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(58)
            )
        )

        val space = View(this)
        content.addView(space, LinearLayout.LayoutParams(1, dp(24)))
    }

    private fun makeSwitchButton(text: String): TextView {
        val button = TextView(this)
        button.text = text
        button.textSize = 15f
        button.gravity = Gravity.CENTER
        button.setTypeface(null, android.graphics.Typeface.BOLD)
        button.setTextColor(textColor())

        if (text == selectedExam) {
            val bg = GradientDrawable()
            bg.cornerRadius = dp(25).toFloat()
            bg.setColor(
                if (darkMode) Color.rgb(55, 55, 60)
                else Color.rgb(230, 230, 235)
            )
            button.background = bg
        }

        return button
    }

    private fun showHome() {
        baseContent()

        addTitle(
            "YKS Takip",
            SimpleDateFormat("d MMMM yyyy", Locale("tr", "TR")).format(Date())
        )

        addExamSwitch()

        val section = TextView(this)
        section.text = "Son Deneme"
        section.textSize = 16f
        section.setTypeface(null, android.graphics.Typeface.BOLD)
        section.setTextColor(textColor())
        content.addView(section)

        val lastCard = LinearLayout(this)
        lastCard.orientation = LinearLayout.VERTICAL
        lastCard.gravity = Gravity.CENTER
        lastCard.setPadding(dp(24), dp(30), dp(24), dp(30))

        val bg = GradientDrawable()
        bg.cornerRadius = dp(28).toFloat()
        bg.setColor(cardColor())
        lastCard.background = bg

        val net = TextView(this)
        net.text = "—"
        net.textSize = 58f
        net.setTypeface(null, android.graphics.Typeface.BOLD)
        net.setTextColor(textColor())
        net.gravity = Gravity.CENTER

        val netLabel = TextView(this)
        netLabel.text = "NET"
        netLabel.textSize = 13f
        netLabel.setTextColor(secondaryColor())
        netLabel.gravity = Gravity.CENTER

        val info = TextView(this)
        info.text = "$selectedExam · Henüz deneme yok"
        info.textSize = 13f
        info.setTextColor(secondaryColor())
        info.gravity = Gravity.CENTER
        info.setPadding(0, dp(10), 0, 0)

        lastCard.addView(net)
        lastCard.addView(netLabel)
        lastCard.addView(info)

        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardParams.setMargins(0, dp(10), 0, dp(18))
        content.addView(lastCard, cardParams)

        addCard("Ortalama", "—", "Henüz veri yok")
        addCard("En yüksek", "—", "Henüz veri yok")
        addCard("Son 5 deneme", "—", "Henüz veri yok")

        val newExam = Button(this)
        newExam.text = "＋  Yeni Deneme"
        newExam.textSize = 17f
        newExam.setTextColor(textColor())
        newExam.setAllCaps(false)

        val newBg = GradientDrawable()
        newBg.cornerRadius = dp(32).toFloat()
        newBg.setColor(
            if (darkMode) Color.rgb(45, 45, 50)
            else Color.rgb(225, 225, 230)
        )
        newExam.background = newBg

        newExam.setOnClickListener {
            showNewExam()
        }

        val buttonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(62)
        )
        buttonParams.setMargins(0, dp(8), 0, dp(20))

        content.addView(newExam, buttonParams)
    }

    private fun showNewExam() {
        baseContent()

        addTitle(
            "Yeni Deneme",
            "$selectedExam deneme sonucu gir"
        )

        val nameInput = makeInput(
            "Deneme adı",
            "Örn. Dershane Denemesi 1"
        )
        content.addView(nameInput)

        val subjects = if (selectedExam == "TYT") {
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

        val inputs = mutableListOf<Triple<String, EditText, EditText>>()

        subjects.forEach { subject ->
            val card = LinearLayout(this)
            card.orientation = LinearLayout.VERTICAL
            card.setPadding(dp(18), dp(18), dp(18), dp(18))

            val bg = GradientDrawable()
            bg.cornerRadius = dp(22).toFloat()
            bg.setColor(cardColor())
            card.background = bg

            val title = TextView(this)
            title.text = "${subject.first}  ·  ${subject.second} soru"
            title.textSize = 16f
            title.setTypeface(null, android.graphics.Typeface.BOLD)
            title.setTextColor(textColor())
            title.setPadding(0, 0, 0, dp(12))

            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL

            val correct = makeNumberInput("Doğru")
            val wrong = makeNumberInput("Yanlış")

            row.addView(
                correct,
                LinearLayout.LayoutParams(0, dp(58), 1f).apply {
                    setMargins(0, 0, dp(6), 0)
                }
            )

            row.addView(
                wrong,
                LinearLayout.LayoutParams(0, dp(58), 1f).apply {
                    setMargins(dp(6), 0, 0, 0)
                }
            )

            card.addView(title)
            card.addView(row)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, dp(12))

            content.addView(card, params)

            inputs.add(Triple(subject.first, correct, wrong))
        }

        val timeInput = makeNumberInput("Toplam süre (dk)")
        content.addView(
            timeInput,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(58)
            ).apply {
                setMargins(0, dp(4), 0, dp(14))
            }
        )

        val result = TextView(this)
        result.text = ""
        result.textSize = 18f
        result.setTypeface(null, android.graphics.Typeface.BOLD)
        result.setTextColor(textColor())
        result.gravity = Gravity.CENTER
        result.setPadding(0, dp(12), 0, dp(12))

        content.addView(result)

        val save = Button(this)
        save.text = "Kaydet"
        save.textSize = 17f
        save.setAllCaps(false)
        save.setTextColor(textColor())

        val saveBg = GradientDrawable()
        saveBg.cornerRadius = dp(30).toFloat()
        saveBg.setColor(
            if (darkMode) Color.rgb(55, 55, 60)
            else Color.rgb(220, 220, 225)
        )
        save.background = saveBg

        save.setOnClickListener {
            var totalNet = 0.0
            var valid = true
            var message = ""

            inputs.forEach { item ->
                val subject = item.first
                val correct = item.second.text.toString().toIntOrNull() ?: 0
                val wrong = item.third.text.toString().toIntOrNull() ?: 0
                val questionCount = subjects.first { it.first == subject }.second

                if (correct < 0 || wrong < 0 || correct + wrong > questionCount) {
                    valid = false
                    message = "$subject: doğru + yanlış soru sayısını geçemez."
                }

                totalNet += correct - wrong / 4.0
            }

            val totalTime = timeInput.text.toString().toDoubleOrNull() ?: 0.0
            val maxTime = if (selectedExam == "TYT") 165 else 180

            if (totalTime < 0 || totalTime > maxTime) {
                valid = false
                message = "Toplam süre $maxTime dakikayı geçemez."
            }

            if (valid) {
                result.text = "Toplam: ${formatNet(totalNet)} net"
                save.text = "✓ Kaydedildi"

                save.postDelayed({
                    showHome()
                }, 900)
            } else {
                result.text = message
            }
        }

        content.addView(
            save,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(62)
            )
        )
    }

    private fun makeInput(
        hint: String,
        placeholder: String
    ): EditText {
        val input = EditText(this)
        input.hint = placeholder
        input.textSize = 15f
        input.setTextColor(textColor())
        input.setHintTextColor(secondaryColor())
        input.setSingleLine(true)
        input.setPadding(dp(16), 0, dp(16), 0)

        val bg = GradientDrawable()
        bg.cornerRadius = dp(20).toFloat()
        bg.setColor(cardColor())
        input.background = bg

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(58)
        )
        params.setMargins(0, 0, 0, dp(14))

        input.tag = hint
        content.addView(input, params)

        return input
    }

    private fun makeNumberInput(label: String): EditText {
        val input = EditText(this)
        input.hint = label
        input.textSize = 15f
        input.inputType =
            android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.setTextColor(textColor())
        input.setHintTextColor(secondaryColor())
        input.setSingleLine(true)
        input.gravity = Gravity.CENTER
        input.setPadding(dp(10), 0, dp(10), 0)

        val bg = GradientDrawable()
        bg.cornerRadius = dp(18).toFloat()
        bg.setColor(
            if (darkMode) Color.rgb(35, 35, 39)
            else Color.rgb(242, 242, 245)
        )
        input.background = bg

        return input
    }

    private fun formatNet(value: Double): String {
        return String.format(Locale.US, "%.2f", value)
            .replace(".", ",")
    }

    private fun showHistory() {
        baseContent()

        addTitle(
            "Geçmiş",
            "Kaydettiğin denemeler"
        )

        val empty = TextView(this)
        empty.text = "Henüz kayıtlı deneme yok."
        empty.textSize = 17f
        empty.setTextColor(secondaryColor())
        empty.gravity = Gravity.CENTER
        empty.setPadding(0, dp(100), 0, dp(100))

        content.addView(
            empty,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun showStats() {
        baseContent()

        addTitle(
            "İstatistik",
            "$selectedExam gelişimin"
        )

        addCard("Ortalama net", "—")
        addCard("En yüksek net", "—")
        addCard("En düşük net", "—")

        val empty = TextView(this)
        empty.text = "Deneme ekledikçe grafiklerin burada görünecek."
        empty.textSize = 14f
        empty.setTextColor(secondaryColor())
        empty.gravity = Gravity.CENTER
        empty.setPadding(dp(10), dp(30), dp(10), dp(30))

        content.addView(empty)
    }

    private fun showTheme() {
        baseContent()

        addTitle(
            "Görünüm",
            "Uygulamanın görünümünü seç"
        )

        val dark = makeThemeButton("Koyu", darkMode)
        val light = makeThemeButton("Açık", !darkMode)

        content.addView(
            dark,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(62)
            ).apply {
                setMargins(0, 0, 0, dp(12))
            }
        )

        content.addView(
            light,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(62)
            )
        )

        dark.setOnClickListener {
            darkMode = true
            showTheme()
        }

        light.setOnClickListener {
            darkMode = false
            showTheme()
        }
    }

    private fun makeThemeButton(
        text: String,
        selected: Boolean
    ): Button {
        val button = Button(this)
        button.text = if (selected) "✓  $text" else text
        button.textSize = 16f
        button.setAllCaps(false)
        button.setTextColor(textColor())

        val bg = GradientDrawable()
        bg.cornerRadius = dp(25).toFloat()
        bg.setColor(
            if (selected) {
                if (darkMode) Color.rgb(55, 55, 60)
                else Color.rgb(225, 225, 230)
            } else {
                cardColor()
            }
        )
        button.background = bg

        return button
    }
}
