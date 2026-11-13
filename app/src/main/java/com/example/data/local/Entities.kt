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
    val subjectBreakdown: Map<String, Int> = emptyMap(),
    val quickNote: String = ""
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

data class WeekComparisonSummary(
    val currentWeekTotalMinutes: Int = 0,
    val previousWeekTotalMinutes: Int = 0,
    val deltaMinutes: Int = 0,
    val percentChange: Double = 0.0,
    val isIncreased: Boolean = true
) {
    val currentWeekTotalHours: Double get() = currentWeekTotalMinutes / 60.0
    val previousWeekTotalHours: Double get() = previousWeekTotalMinutes / 60.0
    val deltaHours: Double get() = Math.abs(currentWeekTotalHours - previousWeekTotalHours)
}

@Entity(tableName = "subject_mastery_records")
data class SubjectMasteryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseCode: String,
    val weekNumber: Int, // 1 (4 weeks ago), 2 (3 weeks ago), 3 (last week), 4 (current week)
    val weekLabel: String, // "W1", "W2", "W3", "W4"
    val masteryPercentage: Double, // 0.0 to 100.0 (e.g. 64.0, 75.0, 82.0, 92.0)
    val studyHoursInWeek: Double = 4.0,
    val targetMastery: Double = 85.0,
    val recordedTimestampMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "weekly_day_notes")
data class WeeklyDayNoteEntity(
    @PrimaryKey
    val dayKey: String, // "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
    val note: String,
    val updatedTimestampMs: Long = System.currentTimeMillis()
)

enum class StudyDateRange(
    val label: String,
    val shortLabel: String,
    val goalScaleMultiplier: Double
) {
    LAST_7_DAYS("Last 7 Days", "7D", 1.0),
    THIS_MONTH("This Month", "1M", 2.0),
    SEMESTER("Semester", "Sem", 3.0)
}

object StudyDateRangeHelper {
    const val ONE_DAY_MS: Long = 24L * 60L * 60L * 1000L

    fun isSessionInRange(
        sessionTimestampMs: Long,
        range: StudyDateRange,
        semester: SemesterEntity? = null,
        nowMs: Long = System.currentTimeMillis()
    ): Boolean {
        return when (range) {
            StudyDateRange.LAST_7_DAYS -> {
                val startMs = nowMs - (7L * ONE_DAY_MS)
                sessionTimestampMs >= startMs
            }
            StudyDateRange.THIS_MONTH -> {
                val startMs = nowMs - (30L * ONE_DAY_MS)
                sessionTimestampMs >= startMs
            }
            StudyDateRange.SEMESTER -> {
                val startMs = semester?.startTimestampMs ?: (nowMs - (120L * ONE_DAY_MS))
                val endMs = semester?.endTimestampMs ?: (nowMs + ONE_DAY_MS)
                sessionTimestampMs in startMs..maxOf(endMs, nowMs)
            }
        }
    }

    fun buildMonthlyWeeklyTrends(
        sessions: List<StudySessionEntity>,
        nowMs: Long = System.currentTimeMillis()
    ): List<DayStudyTrend> {
        val weekSpecs = listOf(
            Triple("Wk 1", "Week 1 (22–30d ago)", 22..30),
            Triple("Wk 2", "Week 2 (15–21d ago)", 15..21),
            Triple("Wk 3", "Week 3 (8–14d ago)", 8..14),
            Triple("Wk 4", "Week 4 (Last 7d)", 0..7)
        )
        return weekSpecs.map { (key, label, dayOffsetRange) ->
            val bucketSessions = sessions.filter { s ->
                val ageDays = ((nowMs - s.timestampMs) / ONE_DAY_MS).toInt().coerceAtLeast(0)
                if (key == "Wk 1") ageDays in dayOffsetRange
                else if (key == "Wk 4") ageDays in 0..7
                else ageDays in dayOffsetRange
            }
            val breakdown = mutableMapOf<String, Int>()
            bucketSessions.forEach { s ->
                breakdown[s.courseCode] = (breakdown[s.courseCode] ?: 0) + s.durationMinutes
            }
            val totalMins = breakdown.values.sum()
            val topSubject = breakdown.maxByOrNull { it.value }?.key ?: "CS201"
            DayStudyTrend(
                dayKey = key,
                dayLabel = label,
                totalMinutes = totalMins,
                sessionCount = bucketSessions.size,
                primarySubject = topSubject,
                subjectBreakdown = breakdown
            )
        }
    }

    fun buildSemesterMonthlyTrends(
        sessions: List<StudySessionEntity>,
        semester: SemesterEntity? = null,
        nowMs: Long = System.currentTimeMillis()
    ): List<DayStudyTrend> {
        val startMs = semester?.startTimestampMs ?: (nowMs - (75L * ONE_DAY_MS))
        val endMs = semester?.endTimestampMs ?: (nowMs + (40L * ONE_DAY_MS))

        val startCal = java.util.Calendar.getInstance().apply {
            timeInMillis = startMs
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val endCal = java.util.Calendar.getInstance().apply {
            timeInMillis = endMs
        }
        val shortFmt = java.text.SimpleDateFormat("MMM", java.util.Locale.US)
        val fullFmt = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.US)

        data class MonthBucket(val year: Int, val month: Int, val shortKey: String, val fullLabel: String)
        val buckets = mutableListOf<MonthBucket>()
        val iter = startCal.clone() as java.util.Calendar
        while (
            iter.get(java.util.Calendar.YEAR) < endCal.get(java.util.Calendar.YEAR) ||
            (iter.get(java.util.Calendar.YEAR) == endCal.get(java.util.Calendar.YEAR) &&
                iter.get(java.util.Calendar.MONTH) <= endCal.get(java.util.Calendar.MONTH))
        ) {
            buckets.add(
                MonthBucket(
                    year = iter.get(java.util.Calendar.YEAR),
                    month = iter.get(java.util.Calendar.MONTH),
                    shortKey = shortFmt.format(iter.time),
                    fullLabel = fullFmt.format(iter.time)
                )
            )
            iter.add(java.util.Calendar.MONTH, 1)
            if (buckets.size >= 6) break
        }

        val sessionCal = java.util.Calendar.getInstance()
        return buckets.map { bucket ->
            val bucketSessions = sessions.filter { s ->
                sessionCal.timeInMillis = s.timestampMs
                sessionCal.get(java.util.Calendar.YEAR) == bucket.year &&
                    sessionCal.get(java.util.Calendar.MONTH) == bucket.month
            }
            val breakdown = mutableMapOf<String, Int>()
            bucketSessions.forEach { s ->
                breakdown[s.courseCode] = (breakdown[s.courseCode] ?: 0) + s.durationMinutes
            }
            val totalMins = breakdown.values.sum()
            val topSubject = breakdown.maxByOrNull { it.value }?.key ?: "CS201"
            DayStudyTrend(
                dayKey = bucket.shortKey,
                dayLabel = bucket.fullLabel,
                totalMinutes = totalMins,
                sessionCount = bucketSessions.size,
                primarySubject = topSubject,
                subjectBreakdown = breakdown
            )
        }
    }
}






