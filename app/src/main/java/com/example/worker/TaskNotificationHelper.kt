package com.example.worker

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
import com.example.MainActivity
import com.example.data.local.TaskEntity

/**
 * Helper class for creating notification channels and building/posting
 * rich local notifications for upcoming academic task deadlines.
 */
object TaskNotificationHelper {

    const val CHANNEL_ID = "upcoming_tasks_channel"
    const val NOTIFICATION_ID = 1001

    /**
     * Ensures the Notification Channel is created on Android 8.0+ (API 26+).
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Upcoming Task Deadlines"
            val descriptionText = "Alerts and reminders for assignments, exams, and project submissions."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Checks if the app has permission to post notifications (Android 13+).
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Posts a notification summarizing upcoming academic tasks.
     * Returns true if notification was posted, false if permission missing or list empty.
     */
    fun postUpcomingTasksNotification(
        context: Context,
        upcomingTasks: List<TaskEntity>
    ): Boolean {
        if (upcomingTasks.isEmpty()) return false
        if (!hasNotificationPermission(context)) return false

        createNotificationChannel(context)

        // PendingIntent to launch MainActivity when tapping notification
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_TO", "TASKS")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val taskCount = upcomingTasks.size
        val firstTask = upcomingTasks.first()

        val title = if (taskCount == 1) {
            "Due Soon: ${firstTask.title}"
        } else {
            "$taskCount Academic Tasks Due Soon!"
        }

        val contentText = if (taskCount == 1) {
            "${firstTask.subjectCode} • Due ${firstTask.dueDate} at ${firstTask.dueTime} (${firstTask.priority} priority)"
        } else {
            "Earliest: ${firstTask.subjectCode} - ${firstTask.title} (${firstTask.dueDate} ${firstTask.dueTime})"
        }

        val bigText = buildString {
            append(contentText)
            if (taskCount > 1) {
                append("\n\nUpcoming Deadlines:")
                upcomingTasks.take(5).forEach { task ->
                    append("\n• ${task.subjectCode}: ${task.title} (Due ${task.dueDate} ${task.dueTime})")
                }
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            true
        } catch (e: SecurityException) {
            false
        }
    }
}
