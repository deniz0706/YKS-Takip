package com.deniz0706.ykstakip.ui.nav

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.deniz0706.ykstakip.R

class FloatingBottomNavView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    data class NavItem(val id: Int, val label: String)

    private var items: List<NavItem> = emptyList()
    private var selectedId: Int = -1
    private var onItemSelected: ((Int) -> Unit)? = null
    private val itemViews = mutableMapOf<Int, TextView>()

    init {
        orientation = HORIZONTAL
        background = ContextCompat.getDrawable(context, R.drawable.bg_nav_island)
        elevation = 12f
        gravity = Gravity.CENTER_VERTICAL
    }

    fun setItems(items: List<NavItem>, initialSelectedId: Int, onSelected: (Int) -> Unit) {
        this.items = items
        this.selectedId = initialSelectedId
        this.onItemSelected = onSelected
        rebuild()
    }

    fun select(id: Int) {
        if (id == selectedId) return
        selectedId = id
        updateSelectionVisuals()
    }

    private fun rebuild() {
        removeAllViews()
        itemViews.clear()
        for (item in items) {
            val tv = TextView(context).apply {
                text = item.label
                gravity = Gravity.CENTER
                textSize = 13f
                setPadding(8, 8, 8, 8)
                layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
                setOnClickListener {
                    if (selectedId != item.id) {
                        selectedId = item.id
                        updateSelectionVisuals()
                        animatePress(this)
                        onItemSelected?.invoke(item.id)
                    }
                }
            }
            itemViews[item.id] = tv
            addView(tv)
        }
        updateSelectionVisuals()
    }

    private fun updateSelectionVisuals() {
        val selectedColor = ContextCompat.getColor(context, R.color.nav_island_selected)
        val unselectedColor = ContextCompat.getColor(context, R.color.on_surface_secondary)
        for ((id, tv) in itemViews) {
            val target = if (id == selectedId) selectedColor else unselectedColor
            animateTextColor(tv, target)
        }
    }

    private fun animateTextColor(view: TextView, targetColor: Int) {
        val startColor = if (view.currentTextColor == 0) Color.GRAY else view.currentTextColor
        ValueAnimator.ofArgb(startColor, targetColor).apply {
            duration = 150
            addUpdateListener { view.setTextColor(it.animatedValue as Int) }
            start()
        }
    }

    private fun animatePress(view: TextView) {
        view.animate().scaleX(0.92f).scaleY(0.92f).setDuration(70)
            .withEndAction { view.animate().scaleX(1f).scaleY(1f).setDuration(90).start() }
            .start()
    }
}
