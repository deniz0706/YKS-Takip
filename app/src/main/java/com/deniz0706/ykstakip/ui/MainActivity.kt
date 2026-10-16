package com.deniz0706.ykstakip.ui

import android.os.Bundle
import android.content.res.Configuration
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.deniz0706.ykstakip.R
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.ui.history.ExamHubFragment
import com.deniz0706.ykstakip.ui.home.HomeFragment
import com.deniz0706.ykstakip.ui.nav.FloatingBottomNavView
import com.deniz0706.ykstakip.ui.newexam.NewExamFragment
import com.deniz0706.ykstakip.ui.settings.SettingsFragment
import com.deniz0706.ykstakip.ui.study.StudyFragment

class MainActivity : AppCompatActivity() {

    private val fragmentCache = mutableMapOf<Int, Fragment>()
    private lateinit var nav: FloatingBottomNavView
    private var currentNavId: Int = NAV_HOME

    companion object {
        const val NAV_HOME = 1
        const val NAV_HISTORY = 2
        const val NAV_STUDY = 3
        const val NAV_STATS = 4
        const val NAV_SETTINGS = 5
        private const val KEY_CURRENT_NAV = "current_nav_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currentNavId = savedInstanceState?.getInt(KEY_CURRENT_NAV, NAV_HOME) ?: NAV_HOME
        // Older saved sessions may still point at the retired Statistics tab.
        if (currentNavId == NAV_STATS) currentNavId = NAV_HISTORY

        nav = findViewById(R.id.bottomNav)

        nav.setItems(
            items = listOf(
                FloatingBottomNavView.NavItem(NAV_HOME, "Ana Sayfa"),
                FloatingBottomNavView.NavItem(NAV_HISTORY, "Denemeler"),
                FloatingBottomNavView.NavItem(NAV_STUDY, "Çalışma"),
                FloatingBottomNavView.NavItem(NAV_SETTINGS, "Ayarlar")
            ),
            initialSelectedId = currentNavId,
            onSelected = { id -> showFragment(id) }
        )

        // Restore the selected tab without rebuilding every screen.
        showFragment(currentNavId)
        updateThemeChrome()
    }

    /**
     * Handling uiMode in place avoids destroying the Activity, its fragments and
     * all Compose scroll/input state on each appearance change.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateThemeChrome()
    }

    private fun updateThemeChrome() {
        val isDark = when (com.deniz0706.ykstakip.data.ThemeManager.currentTheme(this)) {
            "dark" -> true
            "light" -> false
            else -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        }
        val bg = ContextCompat.getColor(this, R.color.background)
        findViewById<View>(R.id.mainRoot)?.setBackgroundColor(bg)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }
        if (::nav.isInitialized) nav.refreshTheme()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_CURRENT_NAV, currentNavId)
    }

    private fun showFragment(navId: Int) {
        // Detail/editor pages use the back stack. Clear them before switching tabs
        // so hidden tab fragments are not stranded behind an editor.
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate(
                null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
        currentNavId = navId
        val manager = supportFragmentManager
        val tag = "main_tab_$navId"
        val fragment = fragmentCache.getOrPut(navId) {
            manager.findFragmentByTag(tag) ?: when (navId) {
                NAV_HOME -> HomeFragment()
                NAV_HISTORY -> ExamHubFragment()
                NAV_STUDY -> StudyFragment()
                NAV_STATS -> ExamHubFragment()
                NAV_SETTINGS -> SettingsFragment()
                else -> HomeFragment()
            }
        }

        // Hide/show retains Compose views and their scroll/filter state. Previously
        // replace() discarded the view and rebuilt the entire page on each tab tap.
        val transaction = manager.beginTransaction()
            .setReorderingAllowed(true)
        manager.fragments.filter {
            it.isAdded && it.id == R.id.fragmentContainer && it !== fragment
        }.forEach {
            transaction.hide(it)
            transaction.setMaxLifecycle(it, Lifecycle.State.STARTED)
        }
        if (fragment.isAdded) {
            transaction.show(fragment)
        } else {
            transaction.add(R.id.fragmentContainer, fragment, tag)
        }
        transaction.setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
        // This tab transaction has no back-stack entry: execute it synchronously
        // to avoid queued swaps and a secondary fade on rapid taps.
        transaction.commitNow()
    }

    fun openStudy() {
        nav.select(NAV_STUDY)
        showFragment(NAV_STUDY)
    }

    fun openNewExam(type: ExamType) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(
                R.id.fragmentContainer,
                NewExamFragment.newInstanceForType(type)
            )
            .addToBackStack("new_exam")
            .commit()
    }

    fun openExamDetail(examId: Long) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(
                R.id.fragmentContainer,
                com.deniz0706.ykstakip.ui.history.ExamDetailFragment.newInstance(examId)
            )
            .addToBackStack("exam_detail")
            .commit()
    }

    fun openEditExam(examId: Long) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(
                R.id.fragmentContainer,
                NewExamFragment.newInstanceForEdit(examId)
            )
            .addToBackStack("edit_exam")
            .commit()
    }

    fun closeAndReturnHome() {
        supportFragmentManager.popBackStackImmediate(
            null,
            androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        currentNavId = NAV_HOME
        nav.select(NAV_HOME)
        showFragment(NAV_HOME)
    }

    fun goBack() {
        if (!supportFragmentManager.popBackStackImmediate()) {
            super.onBackPressed()
        }
    }
}
