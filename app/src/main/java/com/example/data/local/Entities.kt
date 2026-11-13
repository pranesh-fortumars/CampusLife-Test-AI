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

data class SubjectComparisonSnapshot(
    val courseCode: String,
    val loggedMinutes: Int,
    val loggedHours: Double,
    val goalHours: Double,
    val goalAttainmentPct: Int,
    val sessionCount: Int,
    val avgSessionMinutes: Int,
    val latestMasteryPct: Double,
    val masteryGainPct: Double,
    val masteryPerHour: Double,
    val efficiencyScore: Double
)

data class SubjectComparisonBucketPoint(
    val bucketKey: String,
    val bucketLabel: String,
    val subjectAHours: Double,
    val subjectBHours: Double
) {
    val gapHours: Double
        get() = subjectAHours - subjectBHours
}

data class SubjectComparisonReport(
    val subjectA: SubjectComparisonSnapshot,
    val subjectB: SubjectComparisonSnapshot,
    val dateRange: StudyDateRange,
    val bucketPoints: List<SubjectComparisonBucketPoint>,
    val hoursGap: Double,
    val attainmentGapPct: Int,
    val masteryGapPct: Double,
    val avgSessionGapMins: Int,
    val efficiencyGapType: String,
    val laggingCourseCode: String,
    val leaderCourseCode: String,
    val efficiencyGapSummary: String
)

object SubjectComparisonHelper {

    fun buildSubjectSnapshot(
        courseCode: String,
        stats: SubjectStudyStats?,
        goalHours: Double,
        masteryRecords: List<SubjectMasteryEntity>
    ): SubjectComparisonSnapshot {
        val loggedMinutes = stats?.totalMinutes ?: 0
        val loggedHours = loggedMinutes / 60.0
        val safeGoal = goalHours.coerceAtLeast(0.5)
        val attainmentPct = ((loggedHours / safeGoal) * 100.0).toInt()
        val sessionCount = stats?.sessionCount ?: 0
        val avgSessionMinutes = if (sessionCount > 0) loggedMinutes / sessionCount else 0

        val sortedMastery = masteryRecords.sortedBy { it.weekNumber }
        val firstMastery = sortedMastery.firstOrNull()?.masteryPercentage ?: 60.0
        val latestMastery = sortedMastery.lastOrNull()?.masteryPercentage ?: 75.0
        val masteryGain = Math.round((latestMastery - firstMastery) * 10.0) / 10.0

        val masteryPerHour = if (loggedHours >= 0.25) {
            Math.round((latestMastery / loggedHours) * 10.0) / 10.0
        } else {
            0.0
        }

        val goalComponent = attainmentPct.coerceIn(0, 120) * 0.45
        val masteryComponent = latestMastery.coerceIn(0.0, 100.0) * 0.40
        val depthComponent = (avgSessionMinutes.coerceIn(0, 60) / 60.0) * 100.0 * 0.15
        val efficiencyScore = Math.round((goalComponent + masteryComponent + depthComponent) * 10.0) / 10.0

        return SubjectComparisonSnapshot(
            courseCode = courseCode,
            loggedMinutes = loggedMinutes,
            loggedHours = loggedHours,
            goalHours = safeGoal,
            goalAttainmentPct = attainmentPct,
            sessionCount = sessionCount,
            avgSessionMinutes = avgSessionMinutes,
            latestMasteryPct = latestMastery,
            masteryGainPct = masteryGain,
            masteryPerHour = masteryPerHour,
            efficiencyScore = efficiencyScore
        )
    }

    fun buildComparisonReport(
        courseCodeA: String,
        courseCodeB: String,
        statsList: List<SubjectStudyStats>,
        trendsList: List<DayStudyTrend>,
        goalsMap: Map<String, Double>,
        defaultGoalsMap: Map<String, Double>,
        masteryMap: Map<String, List<SubjectMasteryEntity>>,
        defaultMasteryProvider: (String) -> List<SubjectMasteryEntity>,
        dateRange: StudyDateRange
    ): SubjectComparisonReport {
        val statA = statsList.firstOrNull { it.courseCode == courseCodeA }
        val statB = statsList.firstOrNull { it.courseCode == courseCodeB }

        val goalA = (goalsMap[courseCodeA] ?: defaultGoalsMap[courseCodeA] ?: 5.0) * dateRange.goalScaleMultiplier
        val goalB = (goalsMap[courseCodeB] ?: defaultGoalsMap[courseCodeB] ?: 5.0) * dateRange.goalScaleMultiplier

        val masteryA = masteryMap[courseCodeA]?.takeIf { it.isNotEmpty() } ?: defaultMasteryProvider(courseCodeA)
        val masteryB = masteryMap[courseCodeB]?.takeIf { it.isNotEmpty() } ?: defaultMasteryProvider(courseCodeB)

        val snapA = buildSubjectSnapshot(courseCodeA, statA, goalA, masteryA)
        val snapB = buildSubjectSnapshot(courseCodeB, statB, goalB, masteryB)

        val bucketPoints = trendsList.map { bucket ->
            val minsA = bucket.subjectBreakdown[courseCodeA] ?: 0
            val minsB = bucket.subjectBreakdown[courseCodeB] ?: 0
            SubjectComparisonBucketPoint(
                bucketKey = bucket.dayKey,
                bucketLabel = bucket.dayLabel,
                subjectAHours = minsA / 60.0,
                subjectBHours = minsB / 60.0
            )
        }

        val hoursGap = Math.round(kotlin.math.abs(snapA.loggedHours - snapB.loggedHours) * 10.0) / 10.0
        val attainmentGap = kotlin.math.abs(snapA.goalAttainmentPct - snapB.goalAttainmentPct)
        val masteryGap = Math.round(kotlin.math.abs(snapA.latestMasteryPct - snapB.latestMasteryPct) * 10.0) / 10.0
        val avgSessionGap = kotlin.math.abs(snapA.avgSessionMinutes - snapB.avgSessionMinutes)

        val leader = if (snapA.efficiencyScore >= snapB.efficiencyScore) snapA else snapB
        val laggard = if (snapA.efficiencyScore >= snapB.efficiencyScore) snapB else snapA

        val gapType: String
        val summary: String
        if (attainmentGap >= 20) {
            gapType = "Study Volume Gap"
            val hourDiff = String.format(java.util.Locale.US, "%.1f", hoursGap)
            summary = "${laggard.courseCode} trails ${leader.courseCode} by $attainmentGap% goal attainment ($hourDiff h logged gap in ${dateRange.label}). Adding focused blocks to ${laggard.courseCode} will close the gap."
        } else if (masteryGap >= 10.0) {
            gapType = "Mastery Yield Gap"
            val mGap = String.format(java.util.Locale.US, "%.0f", masteryGap)
            val lowerMasterySub = if (snapA.latestMasteryPct <= snapB.latestMasteryPct) snapA else snapB
            val higherMasterySub = if (lowerMasterySub.courseCode == snapA.courseCode) snapB else snapA
            summary = "${lowerMasterySub.courseCode} has a $mGap% lower mastery score than ${higherMasterySub.courseCode} despite ${dateRange.label} study effort. Shift toward active recall & spaced review."
        } else if (avgSessionGap >= 12) {
            gapType = "Focus Depth Gap"
            val shorterSub = if (snapA.avgSessionMinutes <= snapB.avgSessionMinutes) snapA else snapB
            val deeperSub = if (shorterSub.courseCode == snapA.courseCode) snapB else snapA
            summary = "${shorterSub.courseCode} averages ${shorterSub.avgSessionMinutes}m/session vs ${deeperSub.avgSessionMinutes}m for ${deeperSub.courseCode}. Longer uninterrupted blocks can boost retention."
        } else {
            gapType = "Balanced Pace"
            summary = "${snapA.courseCode} and ${snapB.courseCode} are tracking closely in ${dateRange.label} (${attainmentGap}% goal gap, ${String.format(java.util.Locale.US, "%.0f", masteryGap)}% mastery gap)."
        }

        return SubjectComparisonReport(
            subjectA = snapA,
            subjectB = snapB,
            dateRange = dateRange,
            bucketPoints = bucketPoints,
            hoursGap = hoursGap,
            attainmentGapPct = attainmentGap,
            masteryGapPct = masteryGap,
            avgSessionGapMins = avgSessionGap,
            efficiencyGapType = gapType,
            laggingCourseCode = laggard.courseCode,
            leaderCourseCode = leader.courseCode,
            efficiencyGapSummary = summary
        )
    }
}







