package com.deniz0706.ykstakip.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.deniz0706.ykstakip.R
import com.deniz0706.ykstakip.model.ExamType
import com.deniz0706.ykstakip.ui.history.HistoryFragment
import com.deniz0706.ykstakip.ui.home.HomeFragment
import com.deniz0706.ykstakip.ui.nav.FloatingBottomNavView
import com.deniz0706.ykstakip.ui.newexam.NewExamFragment
import com.deniz0706.ykstakip.ui.settings.SettingsFragment
import com.deniz0706.ykstakip.ui.statistics.StatisticsFragment
import com.deniz0706.ykstakip.ui.study.StudyFragment
import com.deniz0706.ykstakip.util.ThemeTransitionHelper

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

        nav = findViewById(R.id.bottomNav)

        nav.setItems(
            items = listOf(
                FloatingBottomNavView.NavItem(NAV_HOME, "Ana Sayfa"),
                FloatingBottomNavView.NavItem(NAV_HISTORY, "Geçmiş"),
                FloatingBottomNavView.NavItem(NAV_STUDY, "Çalışma"),
                FloatingBottomNavView.NavItem(NAV_STATS, "İstatistik"),
                FloatingBottomNavView.NavItem(NAV_SETTINGS, "Ayarlar")
            ),
            initialSelectedId = currentNavId,
            onSelected = { id -> showFragment(id) }
        )

        if (savedInstanceState == null) {
            showFragment(NAV_HOME)
        }
    
        window.decorView.viewTreeObserver.addOnPreDrawListener(
            object : android.view.ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    window.decorView.viewTreeObserver.removeOnPreDrawListener(this)
                    ThemeTransitionHelper.applyPendingSnapshot(this@MainActivity)
                    return true
                }
            }
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_CURRENT_NAV, currentNavId)
    }

    private fun showFragment(navId: Int) {
        currentNavId = navId

        val fragment = fragmentCache.getOrPut(navId) {
            when (navId) {
                NAV_HOME -> HomeFragment()
                NAV_HISTORY -> HistoryFragment()
                NAV_STUDY -> StudyFragment()
                NAV_STATS -> StatisticsFragment()
                NAV_SETTINGS -> SettingsFragment()
                else -> HomeFragment()
            }
        }

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(R.id.fragmentContainer, fragment)
            .commit()
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
        supportFragmentManager.popBackStack(
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
