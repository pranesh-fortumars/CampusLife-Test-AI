package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.SubjectGoalAlertHelper
import com.example.data.local.SubjectGoalAlertReport
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Manages Subject Analytics push notifications that alert students when their
 * 7-day study hours fall behind their custom weekly subject study goals.
 */
object SubjectGoalNotificationHelper {

    private const val TAG = "SubjectGoalAlerts"
    const val CHANNEL_ID = "subject_analytics_goal_alerts_channel"
    private const val CHANNEL_NAME = "Subject Weekly Goal Alerts"
    private const val CHANNEL_DESCRIPTION =
        "Alerts from Subject Analytics when study hours fall behind custom weekly subject targets"
    const val NOTIFICATION_ID = 4045

    const val UNIQUE_PERIODIC_WORK_NAME = "subject_analytics_goal_alerts_periodic"
    const val UNIQUE_IMMEDIATE_WORK_NAME = "subject_analytics_goal_alerts_immediate"

    const val ANALYTICS_PREFS_NAME = "campus_life_analytics_prefs"
    const val KEY_ALERTS_ENABLED = "subject_goal_push_alerts_enabled"
    private const val KEY_GOALS_JSON = "subject_weekly_goals_json"
    private const val KEY_LAST_ALERT_MS = "subject_goal_last_alert_ms"

    private const val COOLDOWN_MS = 3L * 60L * 60L * 1000L

    private val defaultWeeklyGoals = mapOf(
        "CS201" to 5.0,
        "MATH204" to 5.0,
        "CS310" to 5.0,
        "PHY102" to 4.0,
        "ENG215" to 3.0
    )

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 120, 250)
                setShowBadge(true)
                setSound(defaultSoundUri, audioAttributes)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isAlertsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(ANALYTICS_PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ALERTS_ENABLED, false)
    }

    fun setAlertsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(ANALYTICS_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ALERTS_ENABLED, enabled).apply()
        if (enabled) {
            createNotificationChannel(context)
            schedulePeriodicGoalAlerts(context)
        } else {
            cancelPeriodicGoalAlerts(context)
        }
    }

    fun loadCustomGoalsMap(context: Context): Map<String, Double> {
        val prefs = context.getSharedPreferences(ANALYTICS_PREFS_NAME, Context.MODE_PRIVATE)
        val goalsJson = prefs.getString(KEY_GOALS_JSON, null)
        val result = defaultWeeklyGoals.toMutableMap()
        if (!goalsJson.isNullOrEmpty()) {
            try {
                val json = JSONObject(goalsJson)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    result[key] = json.optDouble(key, 5.0)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error parsing custom weekly goals JSON: ${e.message}")
            }
        }
        return result
    }

    suspend fun evaluateFromDatabase(
        context: Context,
        nowMs: Long = System.currentTimeMillis()
    ): SubjectGoalAlertReport {
        val db = AppDatabase.getDatabase(context)
        val courses = db.courseDao().getAllCoursesList()
        val sessions = db.studySessionDao().getAllSessionsList()
        val goalsMap = loadCustomGoalsMap(context)
        return SubjectGoalAlertHelper.evaluateWeeklySubjectGoals(
            courses = courses,
            sessions = sessions,
            goalsMap = goalsMap,
            defaultGoalsMap = defaultWeeklyGoals,
            nowMs = nowMs
        )
    }

    suspend fun checkAndNotifyIfBehind(
        context: Context,
        forceNotify: Boolean = false
    ): Boolean {
        createNotificationChannel(context)

        if (!hasNotificationPermission(context)) {
            Log.w(TAG, "POST_NOTIFICATIONS permission not granted; skipping Subject Analytics goal alert.")
            return false
        }

        if (!isAlertsEnabled(context) && !forceNotify) {
            Log.d(TAG, "Subject Analytics goal alerts disabled by user.")
            return false
        }

        val prefs = context.getSharedPreferences(ANALYTICS_PREFS_NAME, Context.MODE_PRIVATE)
        val lastAlertMs = prefs.getLong(KEY_LAST_ALERT_MS, 0L)
        val now = System.currentTimeMillis()

        if (!forceNotify && (now - lastAlertMs) < COOLDOWN_MS) {
            Log.d(TAG, "Subject goal alert cooldown active.")
            return false
        }

        val report = evaluateFromDatabase(context, now)
        if (!report.hasLaggingSubjects) {
            Log.d(TAG, "All subjects are on pace with weekly goals.")
            return false
        }

        val (title, body) = SubjectGoalAlertHelper.formatAlertNotificationContent(report)
        val top = report.laggingSubjects.first()

        val analyticsIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_TO", "SCHEDULE")
            putExtra("EXTRA_OPEN_SUBJECT_ANALYTICS", true)
        }
        val analyticsPendingIntent = PendingIntent.getActivity(
            context,
            405,
            analyticsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val focusIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_TO", "HOME")
            putExtra("EXTRA_START_POMODORO", true)
            putExtra("EXTRA_COURSE_CODE", top.courseCode)
        }
        val focusPendingIntent = PendingIntent.getActivity(
            context,
            406,
            focusIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val topSummary = String.format(
            Locale.US,
            "%s: %.1fh logged / %.1fh weekly goal",
            top.courseCode,
            top.loggedHours,
            top.weeklyGoalHours
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(topSummary)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body)
                    .setSummaryText("Subject Analytics • Weekly Goal Pace")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(analyticsPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_agenda,
                "View Analytics",
                analyticsPendingIntent
            )
            .addAction(
                android.R.drawable.ic_media_play,
                "Study ${top.courseCode}",
                focusPendingIntent
            )
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            prefs.edit().putLong(KEY_LAST_ALERT_MS, now).apply()
            Log.d(TAG, "Dispatched Subject Analytics goal alert for ${report.laggingSubjects.size} subject(s).")
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException posting Subject Analytics goal alert: ${e.message}")
            false
        }
    }

    fun schedulePeriodicGoalAlerts(context: Context) {
        val request = PeriodicWorkRequestBuilder<SubjectGoalAlertWorker>(
            4, TimeUnit.HOURS,
            15, TimeUnit.MINUTES
        )
            .addTag("subject_analytics_goal_alerts")
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
        Log.d(TAG, "Scheduled periodic Subject Analytics goal alerts.")
    }

    fun triggerImmediateGoalCheck(context: Context, forceNotify: Boolean = true) {
        val request = OneTimeWorkRequestBuilder<SubjectGoalAlertWorker>()
            .setInputData(workDataOf("FORCE_NOTIFY" to forceNotify))
            .addTag("subject_analytics_goal_alert_immediate")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
        Log.d(TAG, "Enqueued immediate Subject Analytics goal alert check (forceNotify=$forceNotify).")
    }

    fun cancelPeriodicGoalAlerts(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_PERIODIC_WORK_NAME)
        Log.d(TAG, "Cancelled periodic Subject Analytics goal alerts.")
    }
}
