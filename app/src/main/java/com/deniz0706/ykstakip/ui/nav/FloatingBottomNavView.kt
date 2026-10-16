package com.deniz0706.ykstakip.ui.nav

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.widget.LinearLayout
import android.widget.TextView
import com.deniz0706.ykstakip.data.ThemeManager

class FloatingBottomNavView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    data class NavItem(val id: Int, val label: String)
    private var items = emptyList<NavItem>()
    private var selectedId = -1
    private var onItemSelected: ((Int) -> Unit)? = null
    private val cells = mutableMapOf<Int, LinearLayout>()
    private val titles = mutableMapOf<Int, TextView>()
    private val icons = mutableMapOf<Int, TextView>()

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private val dark: Boolean get() = when (ThemeManager.currentTheme(context)) {
        "dark" -> true
        "light" -> false
        else -> (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
    private val muted get() = if (dark) Color.rgb(154,175,197) else Color.rgb(83,101,125)
    private val active get() = if (dark) Color.rgb(185,164,245) else Color.rgb(112,85,165)
    private val foreground get() = if (dark) Color.rgb(243,248,255) else Color.rgb(21,34,56)

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(6), dp(5), dp(6), dp(5))
        elevation = dp(10).toFloat()
        updateBackground()
    }

    private fun pill(color: Int, radius: Int = 20) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = dp(radius).toFloat()
        setColor(color)
    }

    private fun updateBackground() {
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(28).toFloat()
            setColor(if (dark) Color.rgb(23,35,55) else Color.WHITE)
            setStroke(dp(1), if (dark) Color.rgb(48,68,92) else Color.rgb(202,211,223))
        }
    }

    fun setItems(items: List<NavItem>, initialSelectedId: Int, onSelected: (Int) -> Unit) {
        this.items = items
        selectedId = initialSelectedId
        onItemSelected = onSelected
        rebuild()
    }

    fun select(id: Int) {
        if (selectedId == id) return
        selectedId = id
        updateSelectionVisuals()
    }

    private fun symbol(id: Int) = when (id) {
        1 -> "⌂"
        2 -> "▤"
        3 -> "✎"
        4 -> "▥"
        5 -> "⚙"
        else -> "•"
    }

    private fun rebuild() {
        removeAllViews()
        cells.clear()
        titles.clear()
        icons.clear()
        updateBackground()
        items.forEach { item ->
            val cell = LinearLayout(context).apply {
                orientation = VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f).apply {
                    marginStart = dp(2)
                    marginEnd = dp(2)
                }
                background = pill(Color.TRANSPARENT)
                isClickable = true
                isFocusable = true
                contentDescription = item.label
                setOnClickListener {
                    if (selectedId != item.id) {
                        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        selectedId = item.id
                        updateSelectionVisuals()
                        animate().scaleX(.94f).scaleY(.94f).setDuration(85)
                            .withEndAction { animate().scaleX(1f).scaleY(1f).setDuration(130).start() }
                            .start()
                        onItemSelected?.invoke(item.id)
                    }
                }
            }
            val icon = TextView(context).apply {
                text = symbol(item.id)
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
                includeFontPadding = false
            }
            val title = TextView(context).apply {
                text = item.label
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                maxLines = 1
                includeFontPadding = false
            }
            cell.addView(icon, LayoutParams(LayoutParams.MATCH_PARENT, dp(28)))
            cell.addView(title, LayoutParams(LayoutParams.MATCH_PARENT, dp(18)))
            cells[item.id] = cell
            titles[item.id] = title
            icons[item.id] = icon
            addView(cell)
        }
        updateSelectionVisuals(animate = false)
    }

    private fun updateSelectionVisuals(animate: Boolean = true) {
        items.forEach { item ->
            val selected = item.id == selectedId
            val cell = cells[item.id] ?: return@forEach
            val title = titles[item.id] ?: return@forEach
            val icon = icons[item.id] ?: return@forEach
            val end = if (selected) active else muted
            title.setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
            val targetBackground = if (selected) {
                if (dark) Color.rgb(45,52,79) else Color.rgb(238,232,250)
            } else Color.TRANSPARENT
            if (animate) {
                val old = (cell.background as? GradientDrawable)?.let { if (selected) Color.TRANSPARENT else targetBackground } ?: Color.TRANSPARENT
                ValueAnimator.ofObject(ArgbEvaluator(), old, targetBackground).apply {
                    duration = 180
                    addUpdateListener { cell.background = pill(it.animatedValue as Int) }
                    start()
                }
            } else cell.background = pill(targetBackground)
            title.setTextColor(end)
            icon.setTextColor(end)
        }
    }
}
