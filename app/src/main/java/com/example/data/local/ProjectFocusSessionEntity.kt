package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room database entity representing a completed or active focus study session for a student project.
 * Logs duration, timestamp, notes, and project metadata.
 */
@Entity(
    tableName = "project_focus_sessions",
    indices = [
        Index(value = ["projectId"]),
        Index(value = ["timestamp"])
    ]
)
data class ProjectFocusSessionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val projectTitle: String,
    val subjectCode: String = "",
    val durationMinutes: Int, // e.g. 25, 45, 50, etc.
    val durationSeconds: Long = (durationMinutes * 60).toLong(),
    val timestamp: Long = System.currentTimeMillis(),
    val sessionDate: String = "", // e.g. "Oct 31, 2026"
    val sessionNotes: String = "",
    val sessionType: String = "FOCUS" // "FOCUS", "DEEP_WORK", "SPRINT", "REVIEW"
) {
    val formattedDuration: String
        get() {
            val hours = durationMinutes / 60
            val mins = durationMinutes % 60
            return when {
                hours > 0 && mins > 0 -> "${hours}h ${mins}m"
                hours > 0 -> "${hours}h"
                else -> "${mins}m"
            }
        }
}

/**
 * Summary DTO used for aggregating total time spent per project directly in Room SQLite.
 */
data class ProjectTimeSpentSummary(
    val projectId: String,
    val projectTitle: String,
    val totalMinutes: Int,
    val sessionCount: Int
) {
    val formattedTotalTime: String
        get() {
            val hours = totalMinutes / 60
            val mins = totalMinutes % 60
            return when {
                hours > 0 && mins > 0 -> "${hours}h ${mins}m"
                hours > 0 -> "${hours}h"
                else -> "${mins}m"
            }
        }
}
