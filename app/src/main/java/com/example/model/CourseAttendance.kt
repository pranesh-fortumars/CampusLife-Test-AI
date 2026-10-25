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
 * Type of academic lecture / session.
 */
enum class LectureType(val label: String, val shortLabel: String, val typicalDuration: String) {
    THEORY("Theory Lecture", "Theory", "1 Hour"),
    LAB("Practical Lab", "Lab", "2 Hours"),
    TUTORIAL("Tutorial Session", "Tutorial", "1 Hour"),
    SEMINAR("Guest Seminar", "Seminar", "1.5 Hours")
}

/**
 * Individual session attendance status with institutional exemptions.
 */
enum class LectureAttendanceStatus(val label: String, val impactDescription: String) {
    PRESENT("Present", "+1 Attended"),
    ABSENT("Absent", "+1 Missed"),
    CANCELLED("Cancelled", "Excluded from total"),
    DUTY_LEAVE("Duty Leave (OD)", "College event exemption"),
    MEDICAL_LEAVE("Medical Leave", "Approved medical exemption")
}

/**
 * Log entry for a single lecture session.
 */
data class LectureSessionLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val courseCode: String,
    val courseName: String,
    val date: String,
    val timeSlot: String,
    val lectureType: LectureType = LectureType.THEORY,
    val status: LectureAttendanceStatus = LectureAttendanceStatus.PRESENT,
    val topic: String = "",
    val room: String = "",
    val faculty: String = "",
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Projected forecast for what-if simulations.
 */
data class AttendanceForecast(
    val courseCode: String,
    val currentPercentage: Double,
    val additionalAttends: Int,
    val additionalMisses: Int,
    val projectedAttended: Int,
    val projectedTotal: Int,
    val projectedPercentage: Double,
    val percentageDelta: Double,
    val isProjectedSafe: Boolean,
    val targetPercentage: Double = 75.0
)

/**
 * Data model representing class/lecture attendance percentage tracking for a course or subject.
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
    val department: String = "Dept. of Computer Science",
    val theoryAttended: Int = (attendedClasses * 0.7).toInt(),
    val theoryTotal: Int = (totalClasses * 0.7).toInt(),
    val labAttended: Int = attendedClasses - (attendedClasses * 0.7).toInt(),
    val labTotal: Int = totalClasses - (totalClasses * 0.7).toInt(),
    val cancelledClasses: Int = 0,
    val dutyLeaveClasses: Int = 0,
    val nextClassTime: String = "Today, 10:30 AM"
) {
    val missedClasses: Int
        get() = (totalClasses - attendedClasses).coerceAtLeast(0)

    val attendancePercentage: Double
        get() = if (totalClasses <= 0) 100.0 else (attendedClasses.toDouble() / totalClasses) * 100.0

    val progressFraction: Float
        get() = if (totalClasses <= 0) 1f else (attendedClasses.toFloat() / totalClasses.toFloat()).coerceIn(0f, 1f)

    val isSafe: Boolean
        get() = attendancePercentage >= targetPercentage

    val isBorderline: Boolean
        get() = attendancePercentage in (targetPercentage - 5.0)..<targetPercentage

    val marginPercentage: Double
        get() = attendancePercentage - targetPercentage

    val status: AttendanceStatus
        get() = when {
            attendancePercentage >= targetPercentage -> AttendanceStatus.SAFE
            attendancePercentage >= targetPercentage - 5.0 -> AttendanceStatus.WARNING
            else -> AttendanceStatus.CRITICAL
        }

    /**
     * How many upcoming consecutive lectures can be missed without dropping below targetPercentage:
     * attendedClasses / (totalClasses + bunks) >= targetPercentage / 100.0
     */
    val safeBunksLeft: Int
        get() {
            val threshold = targetPercentage / 100.0
            if (threshold <= 0.0) return 99
            val maxAllowed = ((attendedClasses - threshold * totalClasses) / threshold).toInt()
            return if (maxAllowed < 0) 0 else maxAllowed
        }

    /**
     * How many consecutive lectures must be attended to reach targetPercentage:
     * (attendedClasses + X) / (totalClasses + X) >= targetPercentage / 100.0
     */
    val neededToReachTarget: Int
        get() {
            val threshold = targetPercentage / 100.0
            if (attendancePercentage >= targetPercentage) return 0
            val diff = (threshold * totalClasses - attendedClasses)
            if (diff <= 0) return 0
            return kotlin.math.ceil(diff / (1.0 - threshold)).toInt()
        }

    val maxAchievablePercentage: Double
        get() {
            val remainingLectures = 20
            val projectedAttended = attendedClasses + remainingLectures
            val projectedTotal = totalClasses + remainingLectures
            return if (projectedTotal == 0) 100.0 else (projectedAttended.toDouble() / projectedTotal) * 100.0
        }

    fun calculateProjected(addAttended: Int, addMissed: Int): Double {
        val newAttended = (attendedClasses + addAttended).coerceAtLeast(0)
        val newTotal = (totalClasses + addAttended + addMissed).coerceAtLeast(0)
        return if (newTotal == 0) 100.0 else (newAttended.toDouble() / newTotal) * 100.0
    }

    fun getForecast(futureAttends: Int, futureMisses: Int): AttendanceForecast {
        val projAttended = (attendedClasses + futureAttends).coerceAtLeast(0)
        val projTotal = (totalClasses + futureAttends + futureMisses).coerceAtLeast(0)
        val projPct = if (projTotal == 0) 100.0 else (projAttended.toDouble() / projTotal) * 100.0
        return AttendanceForecast(
            courseCode = courseCode,
            currentPercentage = attendancePercentage,
            additionalAttends = futureAttends,
            additionalMisses = futureMisses,
            projectedAttended = projAttended,
            projectedTotal = projTotal,
            projectedPercentage = projPct,
            percentageDelta = projPct - attendancePercentage,
            isProjectedSafe = projPct >= targetPercentage,
            targetPercentage = targetPercentage
        )
    }

    val formattedPercentage: String
        get() = String.format(Locale.US, "%.1f%%", attendancePercentage)

    companion object {
        fun fromSubject(subject: Subject): CourseAttendance {
            val total = subject.totalClasses
            val attended = subject.attendedClasses
            val tTotal = (total * 0.7).toInt()
            val tAttended = (attended * 0.7).toInt().coerceAtMost(tTotal)
            val lTotal = (total - tTotal).coerceAtLeast(0)
            val lAttended = (attended - tAttended).coerceAtLeast(0).coerceAtMost(lTotal)

            return CourseAttendance(
                courseCode = subject.code,
                courseName = subject.name,
                attendedClasses = attended,
                totalClasses = total,
                targetPercentage = subject.targetPercentage,
                credits = subject.credits,
                faculty = subject.faculty,
                room = subject.room,
                department = subject.department,
                theoryAttended = tAttended,
                theoryTotal = tTotal,
                labAttended = lAttended,
                labTotal = lTotal,
                nextClassTime = subject.nextClassTime
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

    val totalMissed: Int
        get() = courses.sumOf { it.missedClasses }

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

    val totalSafeBunksAcrossCourses: Int
        get() = courses.sumOf { it.safeBunksLeft }

    val lowestAttendanceCourse: CourseAttendance?
        get() = courses.minByOrNull { it.attendancePercentage }

    val highestAttendanceCourse: CourseAttendance?
        get() = courses.maxByOrNull { it.attendancePercentage }

    val allMeetTarget: Boolean
        get() = courses.all { it.isSafe }

    val formattedOverallPercentage: String
        get() = String.format(Locale.US, "%.1f%%", overallPercentage)
}

fun Subject.toCourseAttendance(): CourseAttendance = CourseAttendance.fromSubject(this)
fun List<Subject>.toCourseAttendanceList(): List<CourseAttendance> = this.map { it.toCourseAttendance() }
