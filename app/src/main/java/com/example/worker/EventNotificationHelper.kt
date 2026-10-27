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
import com.example.model.CampusEvent
import com.example.model.CommunityEventType
import com.example.model.RsvpStatus

/**
 * Helper utility for creating notification channels and posting local notifications
 * for campus club meetings, academic research seminars, and RSVP reminder alerts.
 */
object EventNotificationHelper {

    const val CHANNEL_ID = "campus_events_channel"
    private const val NOTIFICATION_ID_BASE = 2000

    /**
     * Ensures the Event Notification Channel exists on Android 8.0+ (API 26+).
     */
    fun createEventNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Campus Events & Academic Seminars"
            val descriptionText = "Reminders and RSVP confirmations for club meetings, research colloquia, and workshops."
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
     * Posts an immediate notification when a student updates their RSVP status (e.g. "Going" or "Interested").
     */
    fun postEventRsvpNotification(
        context: Context,
        event: CampusEvent,
        rsvpStatus: RsvpStatus
    ): Boolean {
        if (!TaskNotificationHelper.hasNotificationPermission(context)) return false
        if (rsvpStatus == RsvpStatus.NONE || rsvpStatus == RsvpStatus.DECLINED) return false

        createEventNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_TO", "COMMUNITY")
            putExtra("EXTRA_EVENT_ID", event.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            event.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val typeBadge = when (event.eventType) {
            CommunityEventType.ACADEMIC_SEMINAR -> "🎓 Academic Seminar"
            CommunityEventType.CLUB_MEETING -> "🎪 Club Meeting"
            CommunityEventType.WORKSHOP -> "⚡ Workshop"
        }

        val title = when (rsvpStatus) {
            RsvpStatus.GOING -> "🎉 RSVP Confirmed: Going!"
            RsvpStatus.INTERESTED -> "⭐ Added to Your Saved Events"
            else -> "Campus Event Update"
        }

        val contentText = "${event.title} • ${event.dateFormatted} at ${event.timeFormatted}"
        val bigText = buildString {
            append("$typeBadge hosted by ${event.organizer}\n\n")
            if (event.speakerOrHost.isNotBlank()) {
                append("Speaker: ${event.speakerOrHost}")
                if (event.speakerAffiliation.isNotBlank()) {
                    append(" (${event.speakerAffiliation})")
                }
                append("\n")
            }
            append("📍 Venue: ${event.venue}\n")
            append("⏰ Time: ${event.dateFormatted}, ${event.timeFormatted}\n")
            if (event.isReminderSet || rsvpStatus == RsvpStatus.GOING) {
                append("🔔 Push reminder active: You will be alerted 1 hour before start time.")
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
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .build()

        return try {
            val notificationId = NOTIFICATION_ID_BASE + Math.abs(event.id.hashCode() % 1000)
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            true
        } catch (e: SecurityException) {
            false
        }
    }

    /**
     * Posts a reminder alert notification for an upcoming event.
     */
    fun postUpcomingEventAlert(
        context: Context,
        event: CampusEvent
    ): Boolean {
        if (!TaskNotificationHelper.hasNotificationPermission(context)) return false

        createEventNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_TO", "COMMUNITY")
            putExtra("EXTRA_EVENT_ID", event.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            (event.id + "_reminder").hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "🔔 Upcoming: ${event.title}"
        val contentText = "Starting ${event.dateFormatted} at ${event.timeFormatted} (${event.venue})"
        val bigText = buildString {
            append("Reminder for ${event.eventType.label} by ${event.organizer}\n\n")
            if (event.speakerOrHost.isNotBlank()) {
                append("Host/Speaker: ${event.speakerOrHost} - ${event.speakerAffiliation}\n")
            }
            append("📍 Location: ${event.venue}\n")
            append("👥 Current Attendees: ${event.attendeeCount} going\n\n")
            append(event.description)
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
            val notificationId = NOTIFICATION_ID_BASE + 5000 + Math.abs(event.id.hashCode() % 1000)
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            true
        } catch (e: SecurityException) {
            false
        }
    }
}
