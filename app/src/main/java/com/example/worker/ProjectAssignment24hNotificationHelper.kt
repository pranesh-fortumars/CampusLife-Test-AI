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
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.TaskEntity

/**
 * Helper object managing local notifications for projects and assignments
 * due within the 24-hour window, sourced from the local Room database.
 */
object ProjectAssignment24hNotificationHelper {

    private const val TAG = "Project24hNotifier"
    const val CHANNEL_ID = "project_assignment_24h_channel"
    private const val CHANNEL_NAME = "Project & Assignment 24h Deadlines"
    private const val CHANNEL_DESCRIPTION = "High-priority notifications 24 hours before project or assignment due dates"

    private const val PREFS_NAME = "project_deadline_24h_prefs"
    private const val KEY_PREFIX_NOTIFIED = "notified_24h_"

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
                vibrationPattern = longArrayOf(0, 350, 200, 350)
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

    /**
     * Checks the Room database for projects and assignments due within the next 24 hours
     * (or due tomorrow) and dispatches individual local notifications for each.
     * Returns the count of notifications dispatched.
     */
    suspend fun checkAndNotifyUpcomingDeadlines(
        context: Context,
        forceNotify: Boolean = false
    ): Int {
        createNotificationChannel(context)

        if (!hasNotificationPermission(context)) {
            Log.w(TAG, "Notification permission not granted, skipping 24h alert scan.")
            return 0
        }

        val database = AppDatabase.getDatabase(context)
        val taskDao = database.taskDao()

        val now = System.currentTimeMillis()
        val twentyFourHoursMs = 24L * 60 * 60 * 1000L
        val minTimestamp = now
        val maxTimestamp = now + twentyFourHoursMs + (2L * 3600 * 1000L) // Include slight buffer for same-day submissions

        // Fetch from Room
        val candidateTasks = taskDao.getPendingTasksList().filter { task ->
            val isProjectOrAssignment = task.category.equals("Project", ignoreCase = true) ||
                    task.category.equals("Assignment", ignoreCase = true) ||
                    task.type.equals("PROJECT", ignoreCase = true) ||
                    task.type.equals("ASSIGNMENT", ignoreCase = true)

            val isDueTomorrowOrToday = task.dueDate.contains("Tomorrow", ignoreCase = true) ||
                    task.dueDate.contains("Today", ignoreCase = true)

            val isWithin24Hours = task.dueTimestamp in minTimestamp..maxTimestamp

            !task.isCompleted && isProjectOrAssignment && (isWithin24Hours || isDueTomorrowOrToday)
        }

        var dispatchedCount = 0
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        for (task in candidateTasks) {
            val key = "$KEY_PREFIX_NOTIFIED${task.id}"
            val alreadyNotified = prefs.getBoolean(key, false)

            if (!alreadyNotified || forceNotify) {
                val timeRemainingMs = task.dueTimestamp - now
                val hoursRemaining = if (timeRemainingMs > 0) {
                    val h = (timeRemainingMs / (3600 * 1000L)).toInt()
                    maxOf(1, h)
                } else {
                    1
                }

                val posted = post24HourDeadlineNotification(context, task, hoursRemaining)
                if (posted) {
                    prefs.edit().putBoolean(key, true).apply()
                    dispatchedCount++
                }
            }
        }

        Log.d(TAG, "Scanned Room database: found ${candidateTasks.size} tasks, posted $dispatchedCount notifications.")
        return dispatchedCount
    }

    /**
     * Posts a rich local notification notifying the student 24 hours before
     * their project or assignment deadline.
     */
    fun post24HourDeadlineNotification(
        context: Context,
        task: TaskEntity,
        hoursRemaining: Int = 24
    ): Boolean {
        createNotificationChannel(context)

        if (!hasNotificationPermission(context)) {
            return false
        }

        val notifId = (task.id.hashCode() and 0x7FFFFFFF)

        // Intent to launch Tasks view in MainActivity
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_TO", "TASKS")
            putExtra("EXTRA_TASK_ID", task.id)
            putExtra("EXTRA_CATEGORY", task.category)
        }

        val openPendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val subjectDisplay = task.subjectCode.ifBlank { task.courseCode }
        val categoryLabel = if (task.category.isNotBlank()) task.category else "Assignment"
        val urgencyEmoji = if (task.priority.equals("URGENT", ignoreCase = true)) "🚨" else "⏳"

        val title = "$urgencyEmoji 24h Deadline: ${task.title}"
        val shortContent = "$subjectDisplay • Due in ~$hoursRemaining hrs (${task.dueDate} at ${task.dueTime})"

        val notesDetail = if (task.note.isNotBlank()) "\nNotes: ${task.note}" else if (task.details.isNotBlank()) "\nDetails: ${task.details}" else ""

        val bigText = StringBuilder().apply {
            append("⚠️ ${categoryLabel.uppercase()} DEADLINE IN ~${hoursRemaining} HOURS\n\n")
            append("Course: $subjectDisplay - ${task.subjectName}\n")
            append("Submission Due: ${task.dueDate} at ${task.dueTime}\n")
            append("Priority: ${task.priority} | Weight: ${task.weightage}\n")
            if (notesDetail.isNotBlank()) append("$notesDetail\n")
            append("\nPlease review your final deliverables, citations, and upload to the academic portal before the deadline closes.")
        }.toString()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_deadline)
            .setContentTitle(title)
            .setContentText(shortContent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .setColor(0xFFDC2626.toInt()) // Alert Red theme color
            .addAction(
                R.drawable.ic_notification_deadline,
                "View ${categoryLabel}",
                openPendingIntent
            )
            .build()

        NotificationManagerCompat.from(context).notify(notifId, notification)
        return true
    }

    /**
     * Resets notification history for a task (e.g. if deadline was rescheduled).
     */
    fun clearNotificationHistoryForTask(context: Context, taskId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove("$KEY_PREFIX_NOTIFIED$taskId").apply()
    }
}
