package com.example.model

import java.util.Locale

/**
 * Attendance risk / status categorization for academic courses.
 */
enum class AttendanceStatus(val label: String) {
    SAFE("Safe • On Track"),
    WARNING("Warning • Near 75%"),
    CRITICAL("Shortage • Action Needed")
}

/**
 * Data model representing class attendance percentage tracking for a course or subject.
 */
data class CourseAttendance(
    val courseCode: String,
    val courseName: String,
    val attendedClasses: Int,
    val totalClasses: Int,
    val targetPercentage: Double = 75.0,
    val credits: Int = 4,
    val faculty: String = "",
    val room: String = "",
    val department: String = "Dept. of Computer Science"
) {
    val missedClasses: Int
        get() = (totalClasses - attendedClasses).coerceAtLeast(0)

    val attendancePercentage: Double
        get() = if (totalClasses <= 0) 100.0 else (attendedClasses.toDouble() / totalClasses) * 100.0

    val progressFraction: Float
        get() = if (totalClasses <= 0) 1f else (attendedClasses.toFloat() / totalClasses.toFloat()).coerceIn(0f, 1f)

    val isSafe: Boolean
        get() = attendancePercentage >= targetPercentage

    val status: AttendanceStatus
        get() = when {
            attendancePercentage >= targetPercentage -> AttendanceStatus.SAFE
            attendancePercentage >= targetPercentage - 5.0 -> AttendanceStatus.WARNING
            else -> AttendanceStatus.CRITICAL
        }

    val safeBunksLeft: Int
        get() {
            val threshold = targetPercentage / 100.0
            if (threshold <= 0.0) return 99
            val maxAllowed = ((attendedClasses - threshold * totalClasses) / threshold).toInt()
            return if (maxAllowed < 0) 0 else maxAllowed
        }

    val neededToReachTarget: Int
        get() {
            val threshold = targetPercentage / 100.0
            if (attendancePercentage >= targetPercentage) return 0
            val diff = (threshold * totalClasses - attendedClasses)
            if (diff <= 0) return 0
            return kotlin.math.ceil(diff / (1.0 - threshold)).toInt()
        }

    fun calculateProjected(addAttended: Int, addMissed: Int): Double {
        val newAttended = (attendedClasses + addAttended).coerceAtLeast(0)
        val newTotal = (totalClasses + addAttended + addMissed).coerceAtLeast(0)
        return if (newTotal == 0) 100.0 else (newAttended.toDouble() / newTotal) * 100.0
    }

    val formattedPercentage: String
        get() = String.format(Locale.US, "%.1f%%", attendancePercentage)

    companion object {
        fun fromSubject(subject: Subject): CourseAttendance {
            return CourseAttendance(
                courseCode = subject.code,
                courseName = subject.name,
                attendedClasses = subject.attendedClasses,
                totalClasses = subject.totalClasses,
                targetPercentage = subject.targetPercentage,
                credits = subject.credits,
                faculty = subject.faculty,
                room = subject.room,
                department = subject.department
            )
        }
    }
}

/**
 * Summary statistics for a collection of course attendance records.
 */
data class CourseAttendanceSummary(
    val courses: List<CourseAttendance>
) {
    val totalAttended: Int
        get() = courses.sumOf { it.attendedClasses }

    val totalClasses: Int
        get() = courses.sumOf { it.totalClasses }

    val overallPercentage: Double
        get() = if (totalClasses <= 0) 100.0 else (totalAttended.toDouble() / totalClasses) * 100.0

    val overallProgressFraction: Float
        get() = (overallPercentage / 100.0).toFloat().coerceIn(0f, 1f)

    val safeCount: Int
        get() = courses.count { it.isSafe }

    val warningCount: Int
        get() = courses.count { it.status == AttendanceStatus.WARNING }

    val criticalCount: Int
        get() = courses.count { it.status == AttendanceStatus.CRITICAL }

    val formattedOverallPercentage: String
        get() = String.format(Locale.US, "%.1f%%", overallPercentage)
}

fun Subject.toCourseAttendance(): CourseAttendance = CourseAttendance.fromSubject(this)
fun List<Subject>.toCourseAttendanceList(): List<CourseAttendance> = this.map { it.toCourseAttendance() }
