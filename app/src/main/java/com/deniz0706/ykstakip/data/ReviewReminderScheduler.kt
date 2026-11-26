package com.deniz0706.ykstakip.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.deniz0706.ykstakip.R
import com.deniz0706.ykstakip.model.LearningOutcome
import com.deniz0706.ykstakip.ui.MainActivity
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/** Schedules one durable notification per learning outcome, replacing stale dates. */
object ReviewReminderScheduler {
    private const val WORK_PREFIX = "learning-review-"

    fun schedule(context: Context, outcome: LearningOutcome) {
        val date = outcome.nextReviewDate.takeIf { it.isNotBlank() }
        if (date == null) {
            cancel(context, outcome.id)
            return
        }
        val dueDate = runCatching { LocalDate.parse(date) }.getOrNull() ?: return
        val dueAt = dueDate.atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val delay = (dueAt - System.currentTimeMillis()).coerceAtLeast(0L)
        val request = OneTimeWorkRequestBuilder<ReviewReminderWorker>()
            .setInputData(workDataOf(
                ReviewReminderWorker.KEY_OUTCOME_ID to outcome.id,
                ReviewReminderWorker.KEY_DUE_DATE to date
            ))
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            WORK_PREFIX + outcome.id, ExistingWorkPolicy.REPLACE, request
        )
    }

    fun cancel(context: Context, outcomeId: String) {
        WorkManager.getInstance(context.applicationContext)
            .cancelUniqueWork(WORK_PREFIX + outcomeId)
    }

    fun rescheduleAll(context: Context, outcomes: List<LearningOutcome>) {
        outcomes.forEach { schedule(context, it) }
    }
}

class ReviewReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val id = inputData.getString(KEY_OUTCOME_ID) ?: return Result.failure()
        val expectedDate = inputData.getString(KEY_DUE_DATE) ?: return Result.failure()
        val outcome = LearningRepository(applicationContext).outcomes()
            .firstOrNull { it.id == id } ?: return Result.success()
        if (outcome.nextReviewDate != expectedDate) return Result.success()
        val dueDate = runCatching { LocalDate.parse(expectedDate) }.getOrNull()
            ?: return Result.success()
        if (dueDate.isAfter(LocalDate.now())) return Result.success()
        if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
            return Result.success()
        }
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) return Result.success()

        createChannel()
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val title = outcome.topic.ifBlank { outcome.title }
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("${outcome.subject} tekrar zamanı")
            .setContentText(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "$title konusunu öğrenme merkezinde tekrar et."
            ))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        return try {
            NotificationManagerCompat.from(applicationContext)
                .notify(id.hashCode(), notification)
            Result.success()
        } catch (_: SecurityException) {
            Result.success()
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
            manager.createNotificationChannel(NotificationChannel(
                CHANNEL_ID, "Konu tekrarları", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Öğrenme kazanımlarının tekrar zamanı geldiğinde hatırlatır." })
        }
    }

    companion object {
        const val KEY_OUTCOME_ID = "outcome_id"
        const val KEY_DUE_DATE = "due_date"
        private const val CHANNEL_ID = "learning_review_reminders"
    }
}
