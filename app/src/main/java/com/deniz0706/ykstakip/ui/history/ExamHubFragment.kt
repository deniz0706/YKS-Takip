package com.deniz0706.ykstakip.ui.history

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.deniz0706.ykstakip.ui.statistics.StatisticsFragment
import com.deniz0706.ykstakip.data.ThemeManager

/**
 * One destination for exam records and analysis.
 * Child fragments preserve their own filters while switching tabs.
 */
class ExamHubFragment : Fragment() {
    private var selectedTab = 0
    private val containerId = 0x00A71B42
    private lateinit var archiveTab: TextView
    private lateinit var analysisTab: TextView

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        selectedTab = savedInstanceState?.getInt("exam_hub_tab") ?: selectedTab
        val density = resources.displayMetrics.density
        fun dp(v: Int) = (v * density).toInt()
        val initialPalette = ThemeManager.nativePalette(requireContext())
        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(initialPalette.background)
        }
        val tabs = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(18), dp(10), dp(18), dp(6))
        }
        fun tab(label: String) = TextView(requireContext()).apply {
            this.text = label
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(initialPalette.onSurface)
            setPadding(dp(8), dp(13), dp(8), dp(13))
            background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = dp(15).toFloat()
                setColor(initialPalette.surface)
            }
        }
        archiveTab = tab("▤  Arşiv")
        analysisTab = tab("◉  Analiz")
        tabs.addView(archiveTab, LinearLayout.LayoutParams(0, dp(49), 1f).apply {
            marginEnd = dp(5)
        })
        tabs.addView(analysisTab, LinearLayout.LayoutParams(0, dp(49), 1f).apply {
            marginStart = dp(5)
        })
        root.addView(tabs)
        root.addView(FrameLayout(requireContext()).apply { id = containerId },
            LinearLayout.LayoutParams(-1, 0, 1f))
        archiveTab.setOnClickListener { switchTo(0) }
        analysisTab.setOnClickListener { switchTo(1) }
        fun applySelection() {
            // Re-evaluate for every theme switch, including Aurora <-> Graphite.
            val colors = ThemeManager.nativePalette(requireContext())
            root.setBackgroundColor(colors.background)
            listOf(archiveTab, analysisTab).forEachIndexed { index, view ->
                val active = index == selectedTab
                view.setTextColor(if (active) colors.accent else colors.onSurface)
                view.setTypeface(null, if (active) 1 else 0)
                view.background = android.graphics.drawable.GradientDrawable().apply {
                    cornerRadius = dp(15).toFloat()
                    setColor(if (active) colors.selected else colors.surface)
                }
            }
        }
        selectionUpdate = { applySelection() }
        applySelection()
        return root
    }

    private var selectionUpdate: (() -> Unit)? = null

    fun refreshTheme() {
        if (isAdded && view != null) selectionUpdate?.invoke()
    }

    override fun onDestroyView() {
        selectionUpdate = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (childFragmentManager.findFragmentById(containerId) == null) {
            showSelected()
        }
    }

    private fun switchTo(tab: Int) {
        if (selectedTab == tab) return
        selectedTab = tab
        selectionUpdate?.invoke()
        showSelected()
    }

    private fun showSelected() {
        if (!isAdded || view == null) return
        val tag = if (selectedTab == 0) "archive" else "analysis"
        val existing = childFragmentManager.findFragmentByTag(tag)
        val transaction = childFragmentManager.beginTransaction()
        childFragmentManager.fragments.forEach {
            transaction.hide(it)
            transaction.setMaxLifecycle(it, Lifecycle.State.STARTED)
        }
        val destination = existing ?: if (selectedTab == 0) HistoryFragment() else StatisticsFragment()
        if (existing != null) transaction.show(destination)
        else transaction.add(containerId, destination, tag)
        transaction.setMaxLifecycle(destination, Lifecycle.State.RESUMED)
        transaction.commitNow()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("exam_hub_tab", selectedTab)
        super.onSaveInstanceState(outState)
    }
}
