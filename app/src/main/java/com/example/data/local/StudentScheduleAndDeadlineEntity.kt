package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room database entity to store and manage student class schedules and academic deadlines.
 * Unifies recurring class timetables with upcoming project/exam deadlines, room locations,
 * reminders, and completion statuses.
 */
@Entity(
    tableName = "student_schedules_and_deadlines",
    indices = [
        Index(value = ["itemType"]),
        Index(value = ["courseCode"]),
        Index(value = ["dueTimestamp"])
    ]
)
data class StudentScheduleAndDeadlineEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,                                // e.g. "Operating Systems Lab", "DBMS Mid-Sem Exam"
    val courseCode: String,                           // e.g. "CS-304", "CS-302"
    val courseName: String,                           // e.g. "Operating Systems", "Database Management Systems"
    val itemType: String = "SCHEDULE",                // "SCHEDULE" for recurring class, "DEADLINE" for academic deadline
    val dayOfWeek: String = "MON",                    // e.g. "MON", "TUE", "WED", "THU", "FRI" (for schedules)
    val timeSlot: String = "09:00 AM - 10:15 AM",     // Class or deadline time
    val roomOrLocation: String = "LH-102 (Block A)",  // Room location or online submission portal
    val instructorOrFaculty: String = "",             // Professor / TA name
    val dueDateString: String = "Nov 15, 2026",       // Formatted date string
    val dueTimestamp: Long = System.currentTimeMillis() + (7 * 86400000L),
    val isCompleted: Boolean = false,
    val reminderEnabled: Boolean = true,
    val notes: String = "",
    val weightageOrPriority: String = "High",         // "High", "Medium", "25% Mid-Term", etc.
    val createdAt: Long = System.currentTimeMillis()
) {
    val isSchedule: Boolean
        get() = itemType.equals("SCHEDULE", ignoreCase = true)

    val isDeadline: Boolean
        get() = itemType.equals("DEADLINE", ignoreCase = true)

    fun getDaysRemaining(currentTimeMillis: Long = System.currentTimeMillis()): Long {
        val diff = dueTimestamp - currentTimeMillis
        return diff / (1000 * 60 * 60 * 24)
    }

    fun getUrgencyLabel(currentTimeMillis: Long = System.currentTimeMillis()): String {
        return when {
            isCompleted -> "Completed ✓"
            !isDeadline -> "Class • $dayOfWeek"
            else -> {
                val days = getDaysRemaining(currentTimeMillis)
                when {
                    days < 0 -> "Overdue"
                    days == 0L -> "Due Today!"
                    days == 1L -> "Due Tomorrow"
                    else -> "$days days left"
                }
            }
        }
    }
}
