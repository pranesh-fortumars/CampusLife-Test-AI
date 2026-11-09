package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val code: String,
    val name: String,
    val instructor: String,
    val room: String,
    val dayOfWeek: String,
    val timeSlot: String,
    val attendedClasses: Int = 12,
    val totalClasses: Int = 14,
    val targetPercent: Double = 75.0
) {
    val attendancePercentage: Double
        get() = if (totalClasses == 0) 100.0 else (attendedClasses.toDouble() / totalClasses) * 100.0

    // Number of classes student can safely miss and still keep >= 75%
    val safeBunks: Int
        get() {
            var bunks = 0
            while (true) {
                val nextTotal = totalClasses + bunks + 1
                val nextPercent = (attendedClasses.toDouble() / nextTotal) * 100.0
                if (nextPercent >= targetPercent) {
                    bunks++
                } else {
                    break
                }
            }
            return bunks
        }
}

@Entity(tableName = "study_materials")
data class StudyMaterialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val courseCode: String,
    val type: String, // PDF, Slides, Cheat Sheet, Past Paper
    val fileSize: String,
    val uploader: String,
    val tags: String,
    val isDownloaded: Boolean = false
)

@Entity(tableName = "community_posts")
data class CommunityPostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val author: String,
    val courseCode: String,
    val title: String,
    val content: String,
    val tag: String, // Question, Tip, Group Study, Notes
    val upvotes: Int = 0,
    val repliesCount: Int = 0,
    val timeAgo: String = "Just now",
    val isPinned: Boolean = false
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val courseCode: String,
    val dueDate: String,
    val priority: String, // HIGH, MEDIUM, LOW, URGENT
    val isCompleted: Boolean = false,
    val details: String = "",
    val category: String = "Assignment",
    val type: String = "ASSIGNMENT",
    val dueTime: String = "11:59 PM",
    val weightage: String = "15% Internal",
    val dueTimestamp: Long = 0L
) {
    val subjectCode: String get() = courseCode
    val note: String get() = details
    val subjectName: String get() = courseCode
}

@Entity(tableName = "academic_calendar_events")
data class AcademicCalendarEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val type: String, // SEMESTER, EXAM, HOLIDAY, DEADLINE
    val startDate: String,
    val endDate: String = "",
    val daysRemaining: Int = 0,
    val targetTimestampMs: Long = 0L,
    val locationOrVenue: String = "Campus-Wide",
    val description: String = "",
    val isReminderSet: Boolean = false
)

@Entity(tableName = "academic_semesters")
data class SemesterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // e.g. "Fall 2026 Semester"
    val code: String, // e.g. "FA26"
    val academicYear: String = "2026-2027",
    val startDate: String, // e.g. "Aug 24, 2026"
    val endDate: String, // e.g. "Dec 18, 2026"
    val startTimestampMs: Long,
    val endTimestampMs: Long,
    val isCurrent: Boolean = false,
    val totalWeeks: Int = 17,
    val targetGpa: Double = 3.8
) {
    fun calculateProgress(nowMs: Long = System.currentTimeMillis()): Float {
        if (endTimestampMs <= startTimestampMs) return 1f
        if (nowMs <= startTimestampMs) return 0f
        if (nowMs >= endTimestampMs) return 1f
        return ((nowMs - startTimestampMs).toFloat() / (endTimestampMs - startTimestampMs).toFloat()).coerceIn(0f, 1f)
    }

    fun daysElapsed(nowMs: Long = System.currentTimeMillis()): Int {
        if (nowMs <= startTimestampMs) return 0
        val diff = (nowMs.coerceAtMost(endTimestampMs) - startTimestampMs)
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    fun daysRemaining(nowMs: Long = System.currentTimeMillis()): Int {
        if (nowMs >= endTimestampMs) return 0
        val diff = (endTimestampMs - nowMs.coerceAtLeast(startTimestampMs))
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    fun currentWeek(nowMs: Long = System.currentTimeMillis()): Int {
        val days = daysElapsed(nowMs)
        val week = (days / 7) + 1
        return week.coerceIn(1, totalWeeks)
    }
}

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseCode: String,
    val subjectName: String,
    val durationMinutes: Int,
    val timestampMs: Long = System.currentTimeMillis(),
    val dateDisplay: String,
    val mode: String = "FOCUS", // FOCUS, SHORT_BREAK, LONG_BREAK
    val notes: String = "",
    val tag: String = "Exam Prep" // Exam Prep, Homework, Research, Project, Revision
)

data class SubjectStudyStats(
    val courseCode: String,
    val totalMinutes: Int,
    val sessionCount: Int
) {
    val totalHours: Double
        get() = totalMinutes / 60.0
}

data class DayStudyTrend(
    val dayKey: String, // "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
    val dayLabel: String, // "Monday", etc.
    val totalMinutes: Int,
    val sessionCount: Int,
    val primarySubject: String,
    val subjectBreakdown: Map<String, Int> = emptyMap()
) {
    val totalHours: Double
        get() = totalMinutes / 60.0
}

data class WeeklySubjectContribution(
    val courseCode: String,
    val totalMinutes: Int,
    val sessionCount: Int,
    val percentageOfTotal: Double,
    val daysActive: List<String> = emptyList()
) {
    val totalHours: Double
        get() = totalMinutes / 60.0
}




