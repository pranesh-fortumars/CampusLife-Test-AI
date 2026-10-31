package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.local.CareerEventEntity

/**
 * Notification helper for triggering alerts and reminders for upcoming career events,
 * internship application deadlines, and recruitment talks.
 */
object CareerNotificationHelper {

    const val CHANNEL_ID = "career_deadlines_channel"
    private const val NOTIFICATION_ID_BASE = 5000

    fun createCareerNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Career & Internship Deadlines"
            val descriptionText = "Reminders for upcoming internship deadlines, technical interviews, and placement drives."
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

    fun triggerCareerDeadlineNotification(
        context: Context,
        event: CareerEventEntity
    ): Boolean {
        if (!TaskNotificationHelper.hasNotificationPermission(context)) return false

        createCareerNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_TO", "CAREER")
            putExtra("EXTRA_EVENT_ID", event.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            event.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "🎯 Career Alert: ${event.company} - ${event.role}"
        val contentText = "Deadline: ${event.deadlineDate} • ${event.getUrgencyBadge()}"

        val bigText = buildString {
            append("${event.displayType}: ${event.title}\n\n")
            append("🏢 Company: ${event.company}\n")
            append("💼 Role: ${event.role}\n")
            append("⏰ Deadline Date: ${event.deadlineDate} (${event.getUrgencyBadge()})\n")
            if (event.stipendOrPackage.isNotBlank()) {
                append("💰 Package/Stipend: ${event.stipendOrPackage}\n")
            }
            if (event.eligibility.isNotBlank()) {
                append("📋 Eligibility: ${event.eligibility}\n")
            }
            if (event.locationOrUrl.isNotBlank()) {
                append("📍 Location/Portal: ${event.locationOrUrl}\n")
            }
            append("\n🔔 Notification trigger confirmed for this opportunity.")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = NOTIFICATION_ID_BASE + Math.abs(event.id.hashCode() % 1000)
        notificationManager.notify(notificationId, notification)
        return true
    }
}
