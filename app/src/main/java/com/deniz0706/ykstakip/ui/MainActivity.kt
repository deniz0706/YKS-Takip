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

class MainActivity : AppCompatActivity() {

    private val fragmentCache = mutableMapOf<Int, Fragment>()
    private lateinit var nav: FloatingBottomNavView

    companion object {
        const val NAV_HOME = 1
        const val NAV_HISTORY = 2
        const val NAV_STUDY = 3
        const val NAV_STATS = 4
        const val NAV_SETTINGS = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nav = findViewById(R.id.bottomNav)

        nav.setItems(
            items = listOf(
                FloatingBottomNavView.NavItem(NAV_HOME, "Ana Sayfa"),
                FloatingBottomNavView.NavItem(NAV_HISTORY, "Geçmiş"),
                FloatingBottomNavView.NavItem(NAV_STUDY, "Çalışma"),
                FloatingBottomNavView.NavItem(NAV_STATS, "İstatistik"),
                FloatingBottomNavView.NavItem(NAV_SETTINGS, "Görünüm")
            ),
            initialSelectedId = NAV_HOME,
            onSelected = { id -> showFragment(id) }
        )

        if (savedInstanceState == null) {
            showFragment(NAV_HOME)
        }
    }

    private fun showFragment(navId: Int) {
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
        nav.select(NAV_HOME)
        showFragment(NAV_HOME)
    }

    fun goBack() {
        if (!supportFragmentManager.popBackStackImmediate()) {
            super.onBackPressed()
        }
    }
}
