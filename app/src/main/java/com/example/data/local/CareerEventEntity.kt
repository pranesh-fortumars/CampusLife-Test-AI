package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room database entity storing upcoming career events, internship application deadlines,
 * placement drives, and online assessments with notification triggers.
 */
@Entity(
    tableName = "career_events",
    indices = [
        Index(value = ["dueTimestamp"]),
        Index(value = ["status"])
    ]
)
data class CareerEventEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val company: String,
    val role: String,
    val eventType: String = "INTERNSHIP_DEADLINE", // "INTERNSHIP_DEADLINE", "CAREER_TALK", "ONLINE_ASSESSMENT", "PLACEMENT_DRIVE"
    val deadlineDate: String = "Nov 15, 2026",
    val dueTimestamp: Long = System.currentTimeMillis() + (14 * 86400000L),
    val locationOrUrl: String = "Campus Auditorium / Career Portal",
    val reminderEnabled: Boolean = true,
    val notificationTriggered: Boolean = false,
    val status: String = "UPCOMING", // "UPCOMING", "APPLIED", "CLOSED"
    val stipendOrPackage: String = "₹1.25L / mo",
    val description: String = "",
    val eligibility: String = "B.Tech CSE/IT, CGPA >= 7.5",
    val createdAt: Long = System.currentTimeMillis()
) {
    val displayType: String
        get() = when (eventType) {
            "INTERNSHIP_DEADLINE" -> "Internship Deadline"
            "CAREER_TALK" -> "Campus Talk"
            "ONLINE_ASSESSMENT" -> "Online Assessment"
            "PLACEMENT_DRIVE" -> "Placement Drive"
            else -> "Career Event"
        }

    fun getDaysRemaining(currentTimeMillis: Long = System.currentTimeMillis()): Long {
        val diff = dueTimestamp - currentTimeMillis
        return diff / (1000 * 60 * 60 * 24)
    }

    fun getUrgencyBadge(currentTimeMillis: Long = System.currentTimeMillis()): String {
        val days = getDaysRemaining(currentTimeMillis)
        return when {
            status == "CLOSED" -> "Closed"
            status == "APPLIED" -> "Applied ✓"
            days < 0 -> "Expired ${-days}d ago"
            days == 0L -> "Closes Today!"
            days == 1L -> "Closes Tomorrow!"
            else -> "$days days left"
        }
    }
}
