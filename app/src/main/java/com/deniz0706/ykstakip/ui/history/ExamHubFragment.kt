package com.deniz0706.ykstakip.ui.history

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.deniz0706.ykstakip.ui.statistics.StatisticsFragment

/**
 * One destination for exam records and analysis.
 * Child fragments preserve their own filters while switching tabs.
 */
class ExamHubFragment : Fragment() {
    private var selectedTab = 0
    private val containerId = View.generateViewId()
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
        val night = (resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
        val surface = if (night) Color.rgb(23, 35, 55) else Color.rgb(247, 248, 252)
        val text = if (night) Color.rgb(235, 240, 250) else Color.rgb(30, 40, 60)
        val accent = if (night) Color.rgb(185, 164, 245) else Color.rgb(112, 85, 165)
        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(if (night) Color.rgb(13, 20, 34) else Color.rgb(250, 251, 255))
        }
        val tabs = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(18), dp(10), dp(18), dp(6))
        }
        fun tab(label: String) = TextView(requireContext()).apply {
            this.text = label
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(text)
            setPadding(dp(8), dp(13), dp(8), dp(13))
            background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = dp(15).toFloat()
                setColor(surface)
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
            listOf(archiveTab, analysisTab).forEachIndexed { index, view ->
                val active = index == selectedTab
                view.setTextColor(if (active) accent else text)
                view.setTypeface(null, if (active) 1 else 0)
                view.background = android.graphics.drawable.GradientDrawable().apply {
                    cornerRadius = dp(15).toFloat()
                    setColor(if (active) (if (night) Color.rgb(46, 44, 74)
                        else Color.rgb(235, 229, 250)) else surface)
                }
            }
        }
        selectionUpdate = { applySelection() }
        applySelection()
        return root
    }

    private var selectionUpdate: (() -> Unit)? = null

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
        childFragmentManager.fragments.forEach { transaction.hide(it) }
        if (existing != null) transaction.show(existing)
        else transaction.add(containerId,
            if (selectedTab == 0) HistoryFragment() else StatisticsFragment(), tag)
        transaction.commit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("exam_hub_tab", selectedTab)
        super.onSaveInstanceState(outState)
    }
}
