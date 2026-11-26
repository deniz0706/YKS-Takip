package com.deniz0706.ykstakip.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.deniz0706.ykstakip.R
import com.deniz0706.ykstakip.data.AppSettings
import com.deniz0706.ykstakip.data.StudyRepository
import com.deniz0706.ykstakip.ui.MainActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Lightweight home-screen snapshot backed by the existing study repository. */
class StudySummaryWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        manager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { updateWidget(context, manager, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action in setOf(
                Intent.ACTION_DATE_CHANGED,
                Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_TIMEZONE_CHANGED
            )
        ) updateAll(context)
    }

    companion object {
        fun updateAll(context: Context) {
            val app = context.applicationContext
            val manager = AppWidgetManager.getInstance(app)
            val component = ComponentName(app, StudySummaryWidgetProvider::class.java)
            manager.getAppWidgetIds(component).forEach { updateWidget(app, manager, it) }
        }

        private fun updateWidget(
            context: Context,
            manager: AppWidgetManager,
            widgetId: Int
        ) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val day = StudyRepository(context).get(today)
            val questions = day?.questionCount ?: 0
            val minutes = day?.studyMinutes ?: 0
            val goal = AppSettings.getDailyGoal(context)
            val dateLabel = SimpleDateFormat("d MMMM", Locale("tr", "TR")).format(Date())
            val timeLabel = if (minutes >= 60) {
                "${minutes / 60} sa ${minutes % 60} dk çalışma"
            } else "$minutes dk çalışma"

            val views = RemoteViews(context.packageName, R.layout.widget_study_summary).apply {
                setTextViewText(R.id.widget_date, "BUGÜN · $dateLabel")
                setTextViewText(R.id.widget_questions, questions.toString())
                setTextViewText(R.id.widget_minutes, timeLabel)
                setTextViewText(
                    R.id.widget_goal,
                    if (goal != null && goal > 0) "Hedef: $questions / $goal soru"
                    else "Günlük hedef belirle"
                )
                val openApp = PendingIntent.getActivity(
                    context, 0,
                    Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.widget_root, openApp)
            }
            manager.updateAppWidget(widgetId, views)
        }
    }
}
