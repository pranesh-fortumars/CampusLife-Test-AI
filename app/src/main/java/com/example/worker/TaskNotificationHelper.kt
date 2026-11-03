package com.example.worker

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
import com.example.R
import com.example.data.local.TaskEntity

object TaskNotificationHelper {

    const val CHANNEL_ID = "assignment_deadlines_channel"
    private const val CHANNEL_NAME = "Assignment Deadlines & Tasks"
    private const val CHANNEL_DESCRIPTION = "Alerts and reminders for upcoming course assignments and homework deadlines"
    private const val SUMMARY_NOTIFICATION_ID = 9001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showDeadlineAlert(context: Context, task: TaskEntity) {
        createNotificationChannel(context)

        // Permission check for Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_TO", "TASKS")
            putExtra("EXTRA_TASK_ID", task.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val priorityLabel = when (task.priority) {
            "HIGH" -> "🚨 Urgent"
            "MEDIUM" -> "⚠️ Pending"
            else -> "📌 Reminder"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_deadline)
            .setContentTitle("$priorityLabel: ${task.title}")
            .setContentText("${task.courseCode} • Due ${task.dueDate}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${task.courseCode}: ${task.title}\nDue: ${task.dueDate}\nPriority: ${task.priority}\n\n${task.details.ifBlank { "Don't forget to submit your work before the deadline!" }}")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFF4F46E5.toInt()) // Indigo theme color
            .build()

        NotificationManagerCompat.from(context).notify(task.id.toInt(), notification)
    }

    fun showUpcomingSummaryAlert(context: Context, pendingCount: Int, primaryTask: TaskEntity) {
        createNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_TO", "TASKS")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            SUMMARY_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_deadline)
            .setContentTitle("⏰ You have $pendingCount pending assignments")
            .setContentText("Next up: ${primaryTask.title} (${primaryTask.courseCode})")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("You have $pendingCount uncompleted assignments saved in your academic schedule.\nNext deadline: ${primaryTask.title} (${primaryTask.courseCode}) due ${primaryTask.dueDate}.\nTap to view tasks and submit on time.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFF4F46E5.toInt())
            .build()

        NotificationManagerCompat.from(context).notify(SUMMARY_NOTIFICATION_ID, notification)
    }
}
