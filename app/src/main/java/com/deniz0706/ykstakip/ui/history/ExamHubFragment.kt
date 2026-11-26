package com.deniz0706.ykstakip.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.deniz0706.ykstakip.ui.statistics.StatisticsFragment
import com.deniz0706.ykstakip.ui.theme.YksTheme
import com.deniz0706.ykstakip.ui.theme.activeDarkTheme

/**
 * Compose tab header with fragment-backed destinations.
 * Child fragments are intentionally retained so their filters and scroll state survive tab changes.
 */
class ExamHubFragment : Fragment() {
    private var selectedTab = 0
    private val selectedTabUi = mutableIntStateOf(0)
    private val themeRevision = mutableIntStateOf(0)
    private val containerId = 0x00A71B42

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        selectedTab = savedInstanceState?.getInt("exam_hub_tab") ?: selectedTab
        selectedTabUi.intValue = selectedTab
        val density = resources.displayMetrics.density
        fun dp(v: Int) = (v * density).toInt()

        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
        }
        val tabs = ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                // Reading this value refreshes the palette when the user changes themes in Settings.
                themeRevision.intValue
                YksTheme(darkTheme = activeDarkTheme(context)) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = selectedTabUi.intValue == 0,
                            onClick = { switchTo(0) },
                            label = { Text("Arşiv") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedTabUi.intValue == 1,
                            onClick = { switchTo(1) },
                            label = { Text("Analiz") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        root.addView(tabs, LinearLayout.LayoutParams(-1, dp(66)))
        root.addView(FrameLayout(requireContext()).apply { id = containerId },
            LinearLayout.LayoutParams(-1, 0, 1f))
        return root
    }

    fun refreshTheme() {
        if (isAdded && view != null) themeRevision.intValue++
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (childFragmentManager.findFragmentById(containerId) == null) showSelected()
    }

    private fun switchTo(tab: Int) {
        if (selectedTab == tab) return
        selectedTab = tab
        selectedTabUi.intValue = tab
        showSelected()
    }

    private fun showSelected() {
        if (!isAdded || view == null) return
        val tag = if (selectedTab == 0) "archive" else "analysis"
        val existing = childFragmentManager.findFragmentByTag(tag)
        val transaction = childFragmentManager.beginTransaction().setReorderingAllowed(true)
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
