package com.example

import com.example.data.local.CourseEntity
import com.example.data.local.SemesterEntity
import com.example.data.local.TaskEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `test CourseEntity attendance percentage calculation`() {
        val course = CourseEntity(
            code = "CS201",
            name = "Data Structures",
            instructor = "Prof. Smith",
            room = "Hall 3B",
            dayOfWeek = "Mon, Wed",
            timeSlot = "10:00 AM",
            attendedClasses = 15,
            totalClasses = 20,
            targetPercent = 75.0
        )
        assertEquals(75.0, course.attendancePercentage, 0.001)
    }

    @Test
    fun `test CourseEntity safe bunks calculation when well above target`() {
        // Attended 18 out of 20 = 90%. Target is 75%.
        // 18 / 21 = 85.7% (1 bunk)
        // 18 / 22 = 81.8% (2 bunks)
        // 18 / 23 = 78.2% (3 bunks)
        // 18 / 24 = 75.0% (4 bunks)
        // 18 / 25 = 72.0% (< 75%, stops)
        val course = CourseEntity(
            code = "CS201",
            name = "Data Structures",
            instructor = "Prof. Smith",
            room = "Hall 3B",
            dayOfWeek = "Mon, Wed",
            timeSlot = "10:00 AM",
            attendedClasses = 18,
            totalClasses = 20,
            targetPercent = 75.0
        )
        assertEquals(4, course.safeBunks)
    }

    @Test
    fun `test CourseEntity safe bunks when below target`() {
        // Attended 10 out of 20 = 50%. Target is 75%. Safe bunks should be 0.
        val course = CourseEntity(
            code = "MATH204",
            name = "Linear Algebra",
            instructor = "Prof. Brown",
            room = "Hall 1A",
            dayOfWeek = "Tue, Thu",
            timeSlot = "2:00 PM",
            attendedClasses = 10,
            totalClasses = 20,
            targetPercent = 75.0
        )
        assertEquals(0, course.safeBunks)
    }

    @Test
    fun `test TaskEntity default fields and accessor helpers`() {
        val task = TaskEntity(
            title = "Machine Learning Project",
            courseCode = "CS310",
            dueDate = "Tomorrow",
            priority = "HIGH",
            details = "Implement CNN classifier"
        )
        assertFalse(task.isCompleted)
        assertEquals("CS310", task.subjectCode)
        assertEquals("CS310", task.subjectName)
        assertEquals("Implement CNN classifier", task.note)
        assertEquals("Assignment", task.category)
    }

    @Test
    fun `test SemesterEntity progress and duration calculations`() {
        val oneDayMs = 24L * 60 * 60 * 1000L
        val startMs = 1_000_000_000L
        val endMs = startMs + (100 * oneDayMs) // 100 days
        val midMs = startMs + (50 * oneDayMs)  // 50 days

        val semester = SemesterEntity(
            name = "Fall 2026",
            code = "FA26",
            startDate = "Aug 2026",
            endDate = "Dec 2026",
            startTimestampMs = startMs,
            endTimestampMs = endMs,
            totalWeeks = 14
        )

        assertEquals(0.5f, semester.calculateProgress(midMs), 0.01f)
        assertEquals(50, semester.daysElapsed(midMs))
        assertEquals(50, semester.daysRemaining(midMs))
        assertEquals(8, semester.currentWeek(midMs)) // (50/7) + 1 = 8
    }

    @Test
    fun `test WeeklySubjectContribution and DayStudyTrend subject breakdown`() {
        val breakdown = mapOf("CS201" to 90, "MATH204" to 60)
        val day = com.example.data.local.DayStudyTrend(
            dayKey = "Mon",
            dayLabel = "Monday",
            totalMinutes = 150,
            sessionCount = 3,
            primarySubject = "CS201",
            subjectBreakdown = breakdown
        )
        assertEquals(2.5, day.totalHours, 0.001)
        assertEquals(90, day.subjectBreakdown["CS201"])
        assertEquals(60, day.subjectBreakdown["MATH204"])

        val contribution = com.example.data.local.WeeklySubjectContribution(
            courseCode = "CS201",
            totalMinutes = 315,
            sessionCount = 7,
            percentageOfTotal = 32.5,
            daysActive = listOf("Mon", "Tue", "Thu", "Sat")
        )
        assertEquals(5.25, contribution.totalHours, 0.001)
        assertEquals(4, contribution.daysActive.size)
        assertTrue(contribution.daysActive.contains("Mon"))
    }

    @Test
    fun `test StudySessionEntity tag support and category filtering`() {
        val sessions = listOf(
            com.example.data.local.StudySessionEntity(
                courseCode = "CS201",
                subjectName = "Data Structures",
                durationMinutes = 50,
                dateDisplay = "Today",
                mode = "FOCUS",
                notes = "Tree traversals",
                tag = "Exam Prep"
            ),
            com.example.data.local.StudySessionEntity(
                courseCode = "CS201",
                subjectName = "Data Structures",
                durationMinutes = 45,
                dateDisplay = "Today",
                mode = "FOCUS",
                notes = "Homework 3",
                tag = "Homework"
            ),
            com.example.data.local.StudySessionEntity(
                courseCode = "CS310",
                subjectName = "Operating Systems",
                durationMinutes = 50,
                dateDisplay = "Yesterday",
                mode = "FOCUS",
                notes = "Kernel scheduling research",
                tag = "Research"
            )
        )

        // Verify default and custom tags
        assertEquals("Exam Prep", sessions[0].tag)
        assertEquals("Homework", sessions[1].tag)
        assertEquals("Research", sessions[2].tag)

        // Filter by Exam Prep
        val examPrep = sessions.filter { it.tag.equals("Exam Prep", ignoreCase = true) }
        assertEquals(1, examPrep.size)
        assertEquals(50, examPrep.sumOf { it.durationMinutes })

        // Filter by All
        val all = sessions.filter { "All" == "All" || it.tag.equals("All", ignoreCase = true) }
        assertEquals(3, all.size)
        assertEquals(145, all.sumOf { it.durationMinutes })
    }

    @Test
    fun `test Subject bar breakdown tooltip metrics calculation`() {
        val sessions = listOf(
            com.example.data.local.StudySessionEntity(
                courseCode = "CS201",
                subjectName = "Data Structures",
                durationMinutes = 50,
                dateDisplay = "Today"
            ),
            com.example.data.local.StudySessionEntity(
                courseCode = "CS201",
                subjectName = "Data Structures",
                durationMinutes = 40,
                dateDisplay = "Yesterday"
            ),
            com.example.data.local.StudySessionEntity(
                courseCode = "CS201",
                subjectName = "Data Structures",
                durationMinutes = 30,
                dateDisplay = "2 days ago"
            )
        )

        val totalMinutes = sessions.sumOf { it.durationMinutes } // 120
        val totalSessions = sessions.size // 3
        val avgDuration = if (totalSessions > 0) totalMinutes / totalSessions else 0
        assertEquals(40, avgDuration)

        val tasks = listOf(
            TaskEntity(
                title = "Binary Tree Project",
                courseCode = "CS201",
                dueDate = "Tomorrow",
                priority = "HIGH",
                isCompleted = false
            ),
            TaskEntity(
                title = "Heaps Homework",
                courseCode = "CS201",
                dueDate = "Friday",
                priority = "MEDIUM",
                isCompleted = false
            ),
            TaskEntity(
                title = "Lab 1 Submission",
                courseCode = "CS201",
                dueDate = "Past",
                priority = "LOW",
                isCompleted = true
            ),
            TaskEntity(
                title = "OS Lab 2",
                courseCode = "CS310",
                dueDate = "Next Week",
                priority = "HIGH",
                isCompleted = false
            )
        )

        val cs201Tasks = tasks.filter { it.courseCode == "CS201" }
        val cs201Pending = cs201Tasks.filter { !it.isCompleted }
        assertEquals(3, cs201Tasks.size)
        assertEquals(2, cs201Pending.size)
        assertEquals("Binary Tree Project", cs201Pending[0].title)
        assertEquals("Heaps Homework", cs201Pending[1].title)
    }

    @Test
    fun `test WeekComparisonSummary calculation for increased study hours`() {
        val curMins = 900 // 15.0h
        val prevMins = 780 // 13.0h
        val deltaMins = curMins - prevMins // 120m
        val pct = if (prevMins > 0) ((deltaMins.toDouble() / prevMins.toDouble()) * 100.0) else 0.0
        val summary = com.example.data.local.WeekComparisonSummary(
            currentWeekTotalMinutes = curMins,
            previousWeekTotalMinutes = prevMins,
            deltaMinutes = deltaMins,
            percentChange = Math.round(pct * 10.0) / 10.0,
            isIncreased = deltaMins >= 0
        )

        assertEquals(15.0, summary.currentWeekTotalHours, 0.001)
        assertEquals(13.0, summary.previousWeekTotalHours, 0.001)
        assertEquals(2.0, summary.deltaHours, 0.001)
        assertEquals(15.4, summary.percentChange, 0.001)
        assertTrue(summary.isIncreased)
    }

    @Test
    fun `test WeekComparisonSummary calculation for decreased study hours`() {
        val curMins = 600 // 10.0h
        val prevMins = 750 // 12.5h
        val deltaMins = curMins - prevMins // -150m
        val pct = if (prevMins > 0) ((deltaMins.toDouble() / prevMins.toDouble()) * 100.0) else 0.0
        val summary = com.example.data.local.WeekComparisonSummary(
            currentWeekTotalMinutes = curMins,
            previousWeekTotalMinutes = prevMins,
            deltaMinutes = deltaMins,
            percentChange = Math.round(pct * 10.0) / 10.0,
            isIncreased = deltaMins >= 0
        )

        assertEquals(10.0, summary.currentWeekTotalHours, 0.001)
        assertEquals(12.5, summary.previousWeekTotalHours, 0.001)
        assertEquals(2.5, summary.deltaHours, 0.001)
        assertEquals(-20.0, summary.percentChange, 0.001)
        assertFalse(summary.isIncreased)
    }

    @Test
    fun `test subject and day comparative delta calculations`() {
        // Test subject comparison
        val currentSubjectHours = 6.5
        val prevSubjectHours = 4.0
        val deltaSubjectHours = currentSubjectHours - prevSubjectHours
        val pctDelta = if (prevSubjectHours > 0) ((deltaSubjectHours / prevSubjectHours) * 100).toInt() else 0
        assertEquals(2.5, deltaSubjectHours, 0.001)
        assertEquals(62, pctDelta)

        // Test day comparison
        val mondayCurrentHours = 3.0
        val mondayPrevHours = 2.0
        val mondayDelta = mondayCurrentHours - mondayPrevHours
        assertTrue(mondayDelta > 0)
        assertEquals(1.0, mondayDelta, 0.001)
    }
}
