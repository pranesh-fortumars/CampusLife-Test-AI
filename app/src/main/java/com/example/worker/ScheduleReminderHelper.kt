package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.model.ScheduleSlot

/**
 * Helper class for creating notification channels and posting local reminder notifications
 * for upcoming university classes, labs, and lectures with room locations and times.
 */
object ScheduleReminderHelper {

    const val CHANNEL_ID = "class_schedule_reminders_channel"
    const val CHANNEL_NAME = "Class & Lecture Reminders"
    private const val NOTIFICATION_ID_BASE = 5000

    /**
     * Ensures the Notification Channel for Class Reminders is created on Android 8.0+ (API 26+).
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = CHANNEL_NAME
            val descriptionText = "Alerts and reminders for upcoming daily lectures, labs, and room locations."
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
     * Posts a local push notification reminding the student of an upcoming class or lab session.
     */
    fun postClassReminderNotification(
        context: Context,
        slot: ScheduleSlot,
        minutesBefore: Int = slot.reminderMinutesBefore
    ): Boolean {
        createNotificationChannel(context)

        if (!TaskNotificationHelper.hasNotificationPermission(context)) {
            return false
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_TO", "TIMETABLE")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            slot.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val emoji = slot.slotType.iconEmoji
        val title = "$emoji Upcoming ${slot.slotType.displayName}: ${slot.courseCode}"
        val content = "Starts at ${slot.timeStart} in ${slot.room} • ${slot.faculty}"
        val bigText = "${slot.courseName}\nLocation: ${slot.room}\nTime: ${slot.timeStart} – ${slot.timeEnd}\nInstructor: ${slot.faculty}\nReminder set for $minutesBefore minutes prior."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_BASE + (slot.id.hashCode() % 1000),
                notification
            )
            return true
        } catch (e: SecurityException) {
            return false
        }
    }
}
