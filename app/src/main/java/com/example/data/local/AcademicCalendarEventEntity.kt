package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room database entity for Semester Academic Calendar events and deadlines.
 * Tracks exam dates, assignment deadlines, capstone milestones, practicals, and holidays.
 */
@Entity(tableName = "academic_calendar_events")
data class AcademicCalendarEventEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val courseCode: String = "CAMPUS",
    val courseName: String = "Academic Calendar",
    val eventType: String = "EXAM", // "EXAM", "ASSIGNMENT", "PROJECT", "LAB", "HOLIDAY", "MILESTONE"
    val date: String,               // e.g. "Nov 04, 2026"
    val time: String = "10:00 AM",  // e.g. "10:00 AM - 01:00 PM"
    val timestamp: Long,            // Epoch millis for timeline sorting and calendar view
    val locationOrRoom: String = "LH-302", // "LH-302", "Lab 4", "Portal"
    val weightage: String = "25% Mid-Term", // "40% Final Exam", "15% Lab", etc.
    val description: String = "",   // syllabus topics, exam guidelines
    val isCompleted: Boolean = false,
    val reminderEnabled: Boolean = true,
    val semesterNumber: Int = 5,
    val academicYear: String = "2026-2027",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toggleCompleted(): AcademicCalendarEventEntity {
        return copy(isCompleted = !isCompleted)
    }

    val isExam: Boolean
        get() = eventType.equals("EXAM", ignoreCase = true) || eventType.contains("FINAL", ignoreCase = true) || eventType.contains("MID", ignoreCase = true)

    val isAssignment: Boolean
        get() = eventType.equals("ASSIGNMENT", ignoreCase = true)

    val isProject: Boolean
        get() = eventType.equals("PROJECT", ignoreCase = true) || eventType.equals("MILESTONE", ignoreCase = true)

    val isLab: Boolean
        get() = eventType.equals("LAB", ignoreCase = true)

    val isHoliday: Boolean
        get() = eventType.equals("HOLIDAY", ignoreCase = true)

    val daysRemaining: Long
        get() {
            val now = System.currentTimeMillis()
            val diff = timestamp - now
            return if (diff >= 0) {
                (diff + 12 * 60 * 60 * 1000L) / (24 * 60 * 60 * 1000L)
            } else {
                diff / (24 * 60 * 60 * 1000L)
            }
        }

    val urgencyLabel: String
        get() {
            val days = daysRemaining
            return when {
                isCompleted -> "Completed"
                days < 0 -> "Passed"
                days == 0L -> "Due Today"
                days == 1L -> "Tomorrow"
                days in 2..7 -> "In $days days"
                else -> "In ${days / 7} weeks"
            }
        }
}
