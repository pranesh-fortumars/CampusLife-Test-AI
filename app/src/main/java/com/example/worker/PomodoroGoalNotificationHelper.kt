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
import org.json.JSONObject
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Manages recurring local notifications that remind students to start their Pomodoro sessions
 * based on their weekly study goals and current progress in the Room database.
 */
object PomodoroGoalNotificationHelper {

    private const val TAG = "PomodoroGoalNotifier"
    const val CHANNEL_ID = "pomodoro_goal_reminders_channel"
    private const val CHANNEL_NAME = "Pomodoro Study Goal Reminders"
    private const val CHANNEL_DESCRIPTION = "Recurring notifications reminding students to start Pomodoro sessions to hit weekly study targets"
    const val NOTIFICATION_ID = 4040

    const val UNIQUE_PERIODIC_WORK_NAME = "pomodoro_goal_periodic_reminders"
    const val UNIQUE_IMMEDIATE_WORK_NAME = "pomodoro_goal_immediate_reminder"

    private const val PREFS_NAME = "pomodoro_goal_reminder_prefs"
    private const val KEY_ENABLED = "reminders_enabled"
    private const val KEY_INTERVAL_HOURS = "reminder_interval_hours"
    private const val KEY_LAST_NOTIFIED_MS = "last_notified_timestamp_ms"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()

            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
                setShowBadge(true)
                setSound(defaultSoundUri, audioAttributes)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
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

    fun isRemindersEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ENABLED, true)
    }

    fun setRemindersEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
        if (enabled) {
            schedulePeriodicGoalReminders(context)
        } else {
            cancelPeriodicGoalReminders(context)
        }
    }

    fun getReminderIntervalHours(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_INTERVAL_HOURS, 4L)
    }

    fun setReminderIntervalHours(context: Context, hours: Long) {
        val clamped = hours.coerceIn(1L, 24L)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_INTERVAL_HOURS, clamped).apply()
        if (isRemindersEnabled(context)) {
            schedulePeriodicGoalReminders(context, intervalHours = clamped)
        }
    }

    data class SubjectGoalProgress(
        val courseCode: String,
        val subjectName: String,
        val completedHours: Double,
        val targetWeeklyHours: Double,
        val deficitHours: Double,
        val pomodorosRemaining: Int,
        val isBelow50Pct: Boolean,
        val isMet: Boolean
    )

    /**
     * Calculates the student's study progress toward weekly goals for all active courses.
     */
    suspend fun calculateWeeklyGoalProgress(context: Context): List<SubjectGoalProgress> {
        val database = AppDatabase.getDatabase(context)
        val sessionDao = database.studySessionDao()
        val courseDao = database.courseDao()

        // Calculate Monday 00:00 of the current week
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeekMs = calendar.timeInMillis

        val currentWeekSessions = sessionDao.getSessionsSince(startOfWeekMs)
        val allCourses = courseDao.getAllCoursesList().ifEmpty {
            listOf(
                com.example.data.local.CourseEntity(code = "CS201", name = "Data Structures", instructor = "Prof. Smith", room = "Hall 3B", dayOfWeek = "Mon, Wed", timeSlot = "10:00 AM"),
                com.example.data.local.CourseEntity(code = "MATH204", name = "Linear Algebra", instructor = "Prof. Brown", room = "Hall 1A", dayOfWeek = "Tue, Thu", timeSlot = "2:00 PM"),
                com.example.data.local.CourseEntity(code = "CS310", name = "Operating Systems", instructor = "Prof. Clark", room = "Lab 2", dayOfWeek = "Wed, Fri", timeSlot = "11:00 AM"),
                com.example.data.local.CourseEntity(code = "PHY102", name = "Engineering Physics", instructor = "Prof. Davis", room = "Hall 2C", dayOfWeek = "Tue, Thu", timeSlot = "9:00 AM"),
                com.example.data.local.CourseEntity(code = "ENG215", name = "Technical Writing", instructor = "Prof. White", room = "Hall 4A", dayOfWeek = "Mon, Wed", timeSlot = "3:00 PM")
            )
        }

        // Load custom goals from analytics prefs
        val analyticsPrefs = context.getSharedPreferences("campus_life_analytics_prefs", Context.MODE_PRIVATE)
        val goalsJson = analyticsPrefs.getString("subject_weekly_goals_json", null)
        val customGoalsMap = mutableMapOf<String, Double>()
        if (!goalsJson.isNullOrEmpty()) {
            try {
                val json = JSONObject(goalsJson)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    customGoalsMap[key] = json.optDouble(key, 5.0)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error parsing subject weekly goals JSON: ${e.message}")
            }
        }

        val defaultGoals = mapOf(
            "CS201" to 5.0,
            "MATH204" to 5.0,
            "CS310" to 5.0,
            "PHY102" to 4.0,
            "ENG215" to 3.0
        )

        return allCourses.map { course ->
            val code = course.code
            val sessionsForCourse = currentWeekSessions.filter { it.courseCode == code }
            val minutesLogged = sessionsForCourse.sumOf { it.durationMinutes }
            val completedHours = minutesLogged / 60.0
            val targetWeeklyHours = customGoalsMap[code] ?: defaultGoals[code] ?: 5.0
            val deficitHours = maxOf(0.0, targetWeeklyHours - completedHours)
            val pomodorosRemaining = if (deficitHours > 0) Math.ceil((deficitHours * 60.0) / 25.0).toInt() else 0
            val isBelow50Pct = completedHours < (targetWeeklyHours * 0.5)
            val isMet = completedHours >= targetWeeklyHours

            SubjectGoalProgress(
                courseCode = code,
                subjectName = course.name,
                completedHours = completedHours,
                targetWeeklyHours = targetWeeklyHours,
                deficitHours = deficitHours,
                pomodorosRemaining = pomodorosRemaining,
                isBelow50Pct = isBelow50Pct,
                isMet = isMet
            )
        }.sortedWith(
            compareByDescending<SubjectGoalProgress> { it.isBelow50Pct }
                .thenByDescending { it.deficitHours }
        )
    }

    /**
     * Checks study progress against weekly goals and dispatches a local notification
     * prompting the student to start a Pomodoro session.
     */
    suspend fun checkAndNotifyGoalReminders(
        context: Context,
        forceNotify: Boolean = false
    ): Boolean {
        createNotificationChannel(context)

        if (!hasNotificationPermission(context)) {
            Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Skipping Pomodoro reminder.")
            return false
        }

        if (!isRemindersEnabled(context) && !forceNotify) {
            Log.d(TAG, "Pomodoro goal reminders disabled by user setting.")
            return false
        }

        // Cooldown check (minimum 2 hours between reminders unless forced)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastNotified = prefs.getLong(KEY_LAST_NOTIFIED_MS, 0L)
        val now = System.currentTimeMillis()
        val cooldownMs = 2L * 60 * 60 * 1000L

        if (!forceNotify && (now - lastNotified) < cooldownMs) {
            Log.d(TAG, "Cooldown active. Skipping goal reminder (last sent ${(now - lastNotified) / 60000} mins ago).")
            return false
        }

        val progressList = calculateWeeklyGoalProgress(context)
        if (progressList.isEmpty()) {
            return false
        }

        // Prioritize subjects below 50% target, then subjects with greatest deficit
        val urgentSubject = progressList.firstOrNull { it.isBelow50Pct && !it.isMet }
            ?: progressList.firstOrNull { !it.isMet }
            ?: progressList.first()

        val isAllMet = progressList.all { it.isMet }
        val courseCode = urgentSubject.courseCode
        val targetHours = String.format(Locale.US, "%.1f", urgentSubject.targetWeeklyHours)
        val completedHours = String.format(Locale.US, "%.1f", urgentSubject.completedHours)
        val deficitHours = String.format(Locale.US, "%.1f", urgentSubject.deficitHours)
        val pomodoros = urgentSubject.pomodorosRemaining

        val title: String
        val shortBody: String
        val detailedSummary: String

        if (urgentSubject.isBelow50Pct && !urgentSubject.isMet) {
            title = "⚠️ Weekly Study Alert: $courseCode (<50% Goal)"
            shortBody = "You've studied ${completedHours}h / ${targetHours}h for $courseCode. Start a 25-min Pomodoro session to catch up!"
            detailedSummary = "Your total study time for $courseCode (${urgentSubject.subjectName}) is currently below 50% of your weekly target.\n\n" +
                    "• Current: ${completedHours}h / ${targetHours}h target\n" +
                    "• Remaining: ${deficitHours}h ($pomodoros Pomodoro blocks of 25m needed)\n\n" +
                    "Start a focus block now to stay on track for your exams!"
        } else if (!urgentSubject.isMet) {
            title = "🍅 Pomodoro Goal Reminder • $courseCode"
            shortBody = "$deficitHours hrs remaining for $courseCode weekly goal. Ready for a 25-min focus sprint?"
            detailedSummary = "Keep your study momentum going for $courseCode (${urgentSubject.subjectName})!\n\n" +
                    "• Weekly Progress: ${completedHours}h / ${targetHours}h (${pomodoros} Pomodoro sessions left to goal)\n\n" +
                    "Consistency is key—tap below to start your next 25-minute focus session."
        } else {
            title = "🎉 Weekly Study Goals Crushed!"
            shortBody = "You've met your weekly study goals across all subjects! Ready for a bonus Pomodoro session?"
            detailedSummary = "Awesome job Devon! You've achieved your weekly study targets across all active subjects.\n\n" +
                    "Keep up the momentum with a light revision or research Pomodoro sprint."
        }

        // Action 1: Open app directly to Pomodoro study timer
        val pomodoroIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_TO", "HOME")
            putExtra("EXTRA_START_POMODORO", true)
            putExtra("EXTRA_COURSE_CODE", courseCode)
        }
        val pomodoroPendingIntent = PendingIntent.getActivity(
            context,
            201,
            pomodoroIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: Open app to Schedule & Academic goals
        val analyticsIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_TO", "SCHEDULE")
        }
        val analyticsPendingIntent = PendingIntent.getActivity(
            context,
            202,
            analyticsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(shortBody)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(detailedSummary)
                    .setSummaryText("Weekly Study Goal Progress")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pomodoroPendingIntent)
            .addAction(
                android.R.drawable.ic_media_play,
                "Start 25m Focus",
                pomodoroPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_agenda,
                "View Study Goals",
                analyticsPendingIntent
            )
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
            prefs.edit().putLong(KEY_LAST_NOTIFIED_MS, now).apply()
            Log.d(TAG, "Pomodoro goal reminder notification successfully dispatched for $courseCode.")
            return true
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException dispatching notification: ${e.message}")
            return false
        }
    }

    /**
     * Schedules periodic WorkManager checks based on the configured interval hours.
     */
    fun schedulePeriodicGoalReminders(context: Context, intervalHours: Long = 4) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        val request = PeriodicWorkRequestBuilder<PomodoroGoalReminderWorker>(
            intervalHours.coerceAtLeast(2L), TimeUnit.HOURS,
            15, TimeUnit.MINUTES // Flex interval
        )
            .setConstraints(constraints)
            .addTag("pomodoro_goal_reminders")
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
        Log.d(TAG, "Scheduled periodic Pomodoro goal reminders every $intervalHours hours.")
    }

    /**
     * Triggers an immediate one-time Pomodoro goal reminder check.
     */
    fun triggerImmediateReminder(context: Context, forceNotify: Boolean = true) {
        val workData = workDataOf("FORCE_NOTIFY" to forceNotify)
        val request = OneTimeWorkRequestBuilder<PomodoroGoalReminderWorker>()
            .setInputData(workData)
            .addTag("immediate_pomodoro_reminder")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
        Log.d(TAG, "Enqueued immediate Pomodoro goal reminder check.")
    }

    fun cancelPeriodicGoalReminders(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_PERIODIC_WORK_NAME)
        Log.d(TAG, "Cancelled periodic Pomodoro goal reminders.")
    }
}
