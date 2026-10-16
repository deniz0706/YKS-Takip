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
    private val selectionAnimators = mutableMapOf<Int, ValueAnimator>()
    private val cellBackgrounds = mutableMapOf<Int, GradientDrawable>()
    private val backgroundColors = mutableMapOf<Int, Int>()

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private val palette get() = ThemeManager.nativePalette(context)
    private val muted get() = palette.muted
    private val active get() = palette.accent

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
            setColor(palette.surface)
            setStroke(dp(1), palette.border)
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

    /** Recolor the existing views after a DayNight configuration change. */
    fun refreshTheme() {
        updateBackground()
        updateSelectionVisuals(animate = false)
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
        selectionAnimators.values.forEach { it.cancel() }
        selectionAnimators.clear()
        cellBackgrounds.clear()
        backgroundColors.clear()
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
            cellBackgrounds[item.id] = cell.background as GradientDrawable
            backgroundColors[item.id] = Color.TRANSPARENT
            cells[item.id] = cell
            titles[item.id] = title
            icons[item.id] = icon
            addView(cell)
        }
        updateSelectionVisuals(animate = false)
    }

    override fun onDetachedFromWindow() {
        selectionAnimators.values.forEach { it.cancel() }
        selectionAnimators.clear()
        super.onDetachedFromWindow()
    }

    private fun updateSelectionVisuals(animate: Boolean = true) {
        items.forEach { item ->
            val selected = item.id == selectedId
            val cell = cells[item.id] ?: return@forEach
            val title = titles[item.id] ?: return@forEach
            val icon = icons[item.id] ?: return@forEach
            val end = if (selected) active else muted
            title.setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
            val targetBackground = if (selected) palette.selected
                else Color.TRANSPARENT
            selectionAnimators.remove(item.id)?.cancel()
            val drawable = cellBackgrounds[item.id] ?: return@forEach
            val startColor = backgroundColors[item.id] ?: Color.TRANSPARENT
            if (animate && startColor != targetBackground) {
                ValueAnimator.ofObject(ArgbEvaluator(), startColor, targetBackground).apply {
                    duration = 190
                    addUpdateListener {
                        val color = it.animatedValue as Int
                        drawable.setColor(color)
                        backgroundColors[item.id] = color
                    }
                    selectionAnimators[item.id] = this
                    start()
                }
            } else {
                drawable.setColor(targetBackground)
                backgroundColors[item.id] = targetBackground
            }
            title.setTextColor(end)
            icon.setTextColor(end)
        }
    }
}
