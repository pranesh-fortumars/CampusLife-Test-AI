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

    @Test
    fun `test global theme toggle switching and accessibility descriptions`() {
        // Initial state: not set (null), follows system dark theme
        var isDarkModePref: Boolean? = null
        val systemIsDark = false

        val initialEffectiveDark = isDarkModePref ?: systemIsDark
        assertFalse(initialEffectiveDark)

        // Toggle from Light to Dark
        val current = isDarkModePref ?: systemIsDark
        isDarkModePref = !current
        assertTrue(isDarkModePref!!)

        // Verify accessibility content description for Dark mode
        val darkDescription = if (isDarkModePref!!) "Switch to Light Mode" else "Switch to Dark Mode"
        assertEquals("Switch to Light Mode", darkDescription)

        // Toggle again from Dark to Light
        val nextCurrent = isDarkModePref ?: systemIsDark
        isDarkModePref = !nextCurrent
        assertFalse(isDarkModePref!!)

        // Verify accessibility content description for Light mode
        val lightDescription = if (isDarkModePref!!) "Switch to Light Mode" else "Switch to Dark Mode"
        assertEquals("Switch to Dark Mode", lightDescription)
    }

    @Test
    fun `test Pomodoro session calculation based on weekly study goal deficit`() {
        val targetWeeklyHours = 5.0
        val loggedHours = 2.0
        val deficitHours = targetWeeklyHours - loggedHours // 3.0h
        val remainingMinutes = (deficitHours * 60.0) // 180 min
        val pomodorosRemaining = Math.ceil(remainingMinutes / 25.0).toInt() // 180 / 25 = 7.2 -> 8 sessions

        assertEquals(3.0, deficitHours, 0.001)
        assertEquals(8, pomodorosRemaining)

        val progress = com.example.worker.PomodoroGoalNotificationHelper.SubjectGoalProgress(
            courseCode = "CS201",
            subjectName = "Data Structures",
            completedHours = loggedHours,
            targetWeeklyHours = targetWeeklyHours,
            deficitHours = deficitHours,
            pomodorosRemaining = pomodorosRemaining,
            isBelow50Pct = loggedHours < (targetWeeklyHours * 0.5),
            isMet = loggedHours >= targetWeeklyHours
        )

        assertTrue(progress.isBelow50Pct)
        assertFalse(progress.isMet)
        assertEquals("CS201", progress.courseCode)
    }

    @Test
    fun `test Pomodoro reminder priority ordering when subjects are below 50 percent goal`() {
        val list = listOf(
            com.example.worker.PomodoroGoalNotificationHelper.SubjectGoalProgress(
                courseCode = "PHY102",
                subjectName = "Physics",
                completedHours = 3.0,
                targetWeeklyHours = 4.0,
                deficitHours = 1.0,
                pomodorosRemaining = 3,
                isBelow50Pct = false,
                isMet = false
            ),
            com.example.worker.PomodoroGoalNotificationHelper.SubjectGoalProgress(
                courseCode = "CS201",
                subjectName = "Data Structures",
                completedHours = 1.5,
                targetWeeklyHours = 5.0,
                deficitHours = 3.5,
                pomodorosRemaining = 9,
                isBelow50Pct = true, // < 50%
                isMet = false
            ),
            com.example.worker.PomodoroGoalNotificationHelper.SubjectGoalProgress(
                courseCode = "MATH204",
                subjectName = "Linear Algebra",
                completedHours = 5.0,
                targetWeeklyHours = 5.0,
                deficitHours = 0.0,
                pomodorosRemaining = 0,
                isBelow50Pct = false,
                isMet = true
            )
        ).sortedWith(
            compareByDescending<com.example.worker.PomodoroGoalNotificationHelper.SubjectGoalProgress> { it.isBelow50Pct }
                .thenByDescending { it.deficitHours }
        )

        // Subject below 50% (CS201) must be prioritized first
        assertEquals("CS201", list.first().courseCode)
        assertTrue(list.first().isBelow50Pct)
        assertEquals(9, list.first().pomodorosRemaining)

        // Then other unfinished subjects
        assertEquals("PHY102", list[1].courseCode)

        // Completed subjects last
        assertEquals("MATH204", list.last().courseCode)
        assertTrue(list.last().isMet)
    }

    @Test
    fun `test SubjectStudyStats Pomodoro sessions and total hours logged calculation for hover popup`() {
        val stat = com.example.data.local.SubjectStudyStats(
            courseCode = "CS201",
            totalMinutes = 240, // 4.0 hours
            sessionCount = 8 // 8 Pomodoro sessions
        )
        assertEquals(4.0, stat.totalHours, 0.001)
        assertEquals(8, stat.sessionCount)
        val avgDuration = if (stat.sessionCount > 0) stat.totalMinutes / stat.sessionCount else 0
        assertEquals(30, avgDuration)

        val customGoal = 6.0
        val progressPct = ((stat.totalHours / customGoal) * 100).toInt()
        assertEquals(66, progressPct)
        assertFalse(stat.totalHours >= customGoal)
        assertFalse(stat.totalHours < (customGoal * 0.5)) // 4.0 is >= 3.0 (not below 50%)
    }

    @Test
    fun `test Subject priority assignment and High Priority filtering logic`() {
        val subjectPriorities = mutableMapOf(
            "CS201" to "High",
            "MATH204" to "Medium",
            "CS310" to "High",
            "PHY102" to "Medium",
            "ENG215" to "Low"
        )

        val stats = listOf(
            com.example.data.local.SubjectStudyStats("CS201", 360, 8),   // 6.0h
            com.example.data.local.SubjectStudyStats("MATH204", 300, 6),  // 5.0h
            com.example.data.local.SubjectStudyStats("CS310", 240, 5),    // 4.0h
            com.example.data.local.SubjectStudyStats("PHY102", 180, 4),   // 3.0h
            com.example.data.local.SubjectStudyStats("ENG215", 90, 2)     // 1.5h
        )

        // Filter only High Priority
        val highPriorityStats = stats.filter { stat ->
            subjectPriorities[stat.courseCode].equals("High", ignoreCase = true)
        }

        assertEquals(2, highPriorityStats.size)
        assertTrue(highPriorityStats.any { it.courseCode == "CS201" })
        assertTrue(highPriorityStats.any { it.courseCode == "CS310" })
        assertFalse(highPriorityStats.any { it.courseCode == "MATH204" })

        // Updating priority changes the filter dynamically
        subjectPriorities["MATH204"] = "High"
        val updatedHighPriorityStats = stats.filter { stat ->
            subjectPriorities[stat.courseCode].equals("High", ignoreCase = true)
        }
        assertEquals(3, updatedHighPriorityStats.size)
        assertTrue(updatedHighPriorityStats.any { it.courseCode == "MATH204" })
    }

    @Test
    fun `test Study logs JSON and CSV export generation contains subject names and hours`() {
        val stats = listOf(
            com.example.data.local.SubjectStudyStats("CS201", 300, 6),  // 5.0h
            com.example.data.local.SubjectStudyStats("MATH204", 180, 4) // 3.0h
        )
        val courseNames = mapOf("CS201" to "Data Structures", "MATH204" to "Linear Algebra")
        val priorities = mapOf("CS201" to "High", "MATH204" to "Medium")
        val goals = mapOf("CS201" to 6.0, "MATH204" to 4.0)

        // CSV export format check
        val csvHeader = "Course Code,Subject Name,Logged Hours,Goal Hours,Priority,Session Count,Completion Pct"
        val csvRows = stats.map { stat ->
            val name = courseNames[stat.courseCode] ?: stat.courseCode
            val priority = priorities[stat.courseCode] ?: "Medium"
            val goal = goals[stat.courseCode] ?: 5.0
            val pct = ((stat.totalHours / goal) * 100).toInt()
            "${stat.courseCode},$name,${String.format(java.util.Locale.US, "%.1f", stat.totalHours)},${String.format(java.util.Locale.US, "%.1f", goal)},$priority,${stat.sessionCount},$pct%"
        }
        val csvContent = listOf(csvHeader) + csvRows

        assertTrue(csvContent[0].contains("Course Code"))
        assertTrue(csvContent[1].contains("CS201,Data Structures,5.0,6.0,High,6,83%"))
        assertTrue(csvContent[2].contains("MATH204,Linear Algebra,3.0,4.0,Medium,4,75%"))

        // JSON export format check
        val jsonStringBuilder = StringBuilder()
        jsonStringBuilder.append("{\n  \"subjects\": [\n")
        stats.forEachIndexed { idx, stat ->
            val name = courseNames[stat.courseCode] ?: stat.courseCode
            val priority = priorities[stat.courseCode] ?: "Medium"
            val goal = goals[stat.courseCode] ?: 5.0
            jsonStringBuilder.append("    {\"courseCode\": \"${stat.courseCode}\", \"subjectName\": \"$name\", \"loggedHours\": ${stat.totalHours}, \"priority\": \"$priority\"}${if (idx < stats.size - 1) "," else ""}\n")
        }
        jsonStringBuilder.append("  ]\n}")
        val jsonOutput = jsonStringBuilder.toString()

        assertTrue(jsonOutput.contains("\"courseCode\": \"CS201\""))
        assertTrue(jsonOutput.contains("\"subjectName\": \"Data Structures\""))
        assertTrue(jsonOutput.contains("\"loggedHours\": 5.0"))
        assertTrue(jsonOutput.contains("\"priority\": \"High\""))
    }

    @Test
    fun `test SubjectMasteryEntity 4-week trend progression and growth calculations`() {
        val records = listOf(
            com.example.data.local.SubjectMasteryEntity(courseCode = "CS201", weekNumber = 1, weekLabel = "W1", masteryPercentage = 64.0, studyHoursInWeek = 3.5),
            com.example.data.local.SubjectMasteryEntity(courseCode = "CS201", weekNumber = 2, weekLabel = "W2", masteryPercentage = 72.0, studyHoursInWeek = 4.2),
            com.example.data.local.SubjectMasteryEntity(courseCode = "CS201", weekNumber = 3, weekLabel = "W3", masteryPercentage = 81.0, studyHoursInWeek = 5.0),
            com.example.data.local.SubjectMasteryEntity(courseCode = "CS201", weekNumber = 4, weekLabel = "W4", masteryPercentage = 92.0, studyHoursInWeek = 5.8)
        )

        assertEquals(4, records.size)
        val sorted = records.sortedBy { it.weekNumber }
        assertEquals("W1", sorted.first().weekLabel)
        assertEquals("W4", sorted.last().weekLabel)

        val initialMastery = sorted.first().masteryPercentage
        val finalMastery = sorted.last().masteryPercentage
        val netGain = finalMastery - initialMastery

        assertEquals(64.0, initialMastery, 0.001)
        assertEquals(92.0, finalMastery, 0.001)
        assertEquals(28.0, netGain, 0.001)
        assertTrue(finalMastery > initialMastery) // Upward trend
    }

    @Test
    fun `test WeeklyDayNoteEntity and DayStudyTrend quickNote attachment`() {
        val noteEntity = com.example.data.local.WeeklyDayNoteEntity(
            dayKey = "Thu",
            note = "Exam Prep"
        )
        assertEquals("Thu", noteEntity.dayKey)
        assertEquals("Exam Prep", noteEntity.note)

        val dayTrend = com.example.data.local.DayStudyTrend(
            dayKey = "Thu",
            dayLabel = "Thursday",
            totalMinutes = 270, // 4.5h spike
            sessionCount = 5,
            primarySubject = "CS201",
            subjectBreakdown = mapOf("CS201" to 180, "MATH204" to 90),
            quickNote = noteEntity.note
        )
        assertEquals(4.5, dayTrend.totalHours, 0.001)
        assertEquals("Exam Prep", dayTrend.quickNote)
    }

    @Test
    fun `test Quick Add +30m session increment calculation`() {
        val initialMinutes = 240 // 4.0h
        val quickAddMinutes = 30
        val updatedMinutes = initialMinutes + quickAddMinutes
        val updatedHours = updatedMinutes / 60.0

        assertEquals(270, updatedMinutes)
        assertEquals(4.5, updatedHours, 0.001)

        val quickSession = com.example.data.local.StudySessionEntity(
            courseCode = "CS201",
            subjectName = "Data Structures",
            durationMinutes = 30,
            dateDisplay = "Today",
            mode = "FOCUS",
            notes = "Quick Add +30m study block",
            tag = "Exam Prep"
        )
        assertEquals(30, quickSession.durationMinutes)
        assertEquals("FOCUS", quickSession.mode)
        assertEquals("Today", quickSession.dateDisplay)
    }

    @Test
    fun `test StudyDateRange goal scaling and session range filtering`() {
        assertEquals("Last 7 Days", com.example.data.local.StudyDateRange.LAST_7_DAYS.label)
        assertEquals("This Month", com.example.data.local.StudyDateRange.THIS_MONTH.label)
        assertEquals("Semester", com.example.data.local.StudyDateRange.SEMESTER.label)
        assertEquals(1.0, com.example.data.local.StudyDateRange.LAST_7_DAYS.goalScaleMultiplier, 0.001)
        assertEquals(2.0, com.example.data.local.StudyDateRange.THIS_MONTH.goalScaleMultiplier, 0.001)
        assertEquals(3.0, com.example.data.local.StudyDateRange.SEMESTER.goalScaleMultiplier, 0.001)

        val dayMs = com.example.data.local.StudyDateRangeHelper.ONE_DAY_MS
        val nowMs = 1_760_000_000_000L // fixed reference timestamp
        val semester = SemesterEntity(
            name = "Fall 2026",
            code = "FA26",
            startDate = "Aug 2026",
            endDate = "Dec 2026",
            startTimestampMs = nowMs - (75L * dayMs),
            endTimestampMs = nowMs + (40L * dayMs),
            totalWeeks = 16
        )

        val ts2DaysAgo = nowMs - (2L * dayMs)
        val ts15DaysAgo = nowMs - (15L * dayMs)
        val ts50DaysAgo = nowMs - (50L * dayMs)
        val ts100DaysAgo = nowMs - (100L * dayMs)

        // LAST_7_DAYS
        assertTrue(com.example.data.local.StudyDateRangeHelper.isSessionInRange(ts2DaysAgo, com.example.data.local.StudyDateRange.LAST_7_DAYS, semester, nowMs))
        assertFalse(com.example.data.local.StudyDateRangeHelper.isSessionInRange(ts15DaysAgo, com.example.data.local.StudyDateRange.LAST_7_DAYS, semester, nowMs))

        // THIS_MONTH (rolling 30d)
        assertTrue(com.example.data.local.StudyDateRangeHelper.isSessionInRange(ts2DaysAgo, com.example.data.local.StudyDateRange.THIS_MONTH, semester, nowMs))
        assertTrue(com.example.data.local.StudyDateRangeHelper.isSessionInRange(ts15DaysAgo, com.example.data.local.StudyDateRange.THIS_MONTH, semester, nowMs))
        assertFalse(com.example.data.local.StudyDateRangeHelper.isSessionInRange(ts50DaysAgo, com.example.data.local.StudyDateRange.THIS_MONTH, semester, nowMs))

        // SEMESTER
        assertTrue(com.example.data.local.StudyDateRangeHelper.isSessionInRange(ts50DaysAgo, com.example.data.local.StudyDateRange.SEMESTER, semester, nowMs))
        assertFalse(com.example.data.local.StudyDateRangeHelper.isSessionInRange(ts100DaysAgo, com.example.data.local.StudyDateRange.SEMESTER, semester, nowMs))
    }

    @Test
    fun `test StudyDateRangeHelper monthly weekly buckets and semester monthly buckets`() {
        val dayMs = com.example.data.local.StudyDateRangeHelper.ONE_DAY_MS
        val nowMs = 1_760_000_000_000L
        val semester = SemesterEntity(
            name = "Fall 2026",
            code = "FA26",
            startDate = "Aug 2026",
            endDate = "Dec 2026",
            startTimestampMs = nowMs - (75L * dayMs),
            endTimestampMs = nowMs + (40L * dayMs),
            totalWeeks = 16
        )

        val sessions = listOf(
            com.example.data.local.StudySessionEntity(
                courseCode = "CS201",
                subjectName = "Data Structures",
                durationMinutes = 60,
                timestampMs = nowMs - (1L * dayMs),
                dateDisplay = "Yesterday"
            ),
            com.example.data.local.StudySessionEntity(
                courseCode = "MATH204",
                subjectName = "Linear Algebra",
                durationMinutes = 90,
                timestampMs = nowMs - (10L * dayMs),
                dateDisplay = "10d ago"
            ),
            com.example.data.local.StudySessionEntity(
                courseCode = "CS310",
                subjectName = "Operating Systems",
                durationMinutes = 120,
                timestampMs = nowMs - (18L * dayMs),
                dateDisplay = "18d ago"
            ),
            com.example.data.local.StudySessionEntity(
                courseCode = "PHY102",
                subjectName = "Physics",
                durationMinutes = 45,
                timestampMs = nowMs - (25L * dayMs),
                dateDisplay = "25d ago"
            )
        )

        val monthlyTrends = com.example.data.local.StudyDateRangeHelper.buildMonthlyWeeklyTrends(sessions, nowMs)
        assertEquals(4, monthlyTrends.size)
        assertEquals(listOf("Wk 1", "Wk 2", "Wk 3", "Wk 4"), monthlyTrends.map { it.dayKey })
        assertEquals(45, monthlyTrends[0].totalMinutes)
        assertEquals(120, monthlyTrends[1].totalMinutes)
        assertEquals(90, monthlyTrends[2].totalMinutes)
        assertEquals(60, monthlyTrends[3].totalMinutes)
        assertEquals("CS201", monthlyTrends[3].primarySubject)

        val semesterTrends = com.example.data.local.StudyDateRangeHelper.buildSemesterMonthlyTrends(sessions, semester, nowMs)
        assertTrue(semesterTrends.isNotEmpty())
        assertTrue(semesterTrends.size <= 6)
        assertEquals(315, semesterTrends.sumOf { it.totalMinutes })
    }

    @Test
    fun `test SubjectComparisonHelper identifies Study Volume Gap across same period`() {
        val stats = listOf(
            com.example.data.local.SubjectStudyStats("CS201", totalMinutes = 270, sessionCount = 6), // 4.5h / 5.0h = 90%
            com.example.data.local.SubjectStudyStats("PHY102", totalMinutes = 60, sessionCount = 2)  // 1.0h / 4.0h = 25%
        )
        val trends = listOf(
            com.example.data.local.DayStudyTrend("Mon", "Monday", 90, 2, "CS201", mapOf("CS201" to 90)),
            com.example.data.local.DayStudyTrend("Tue", "Tuesday", 90, 2, "CS201", mapOf("CS201" to 60, "PHY102" to 30)),
            com.example.data.local.DayStudyTrend("Thu", "Thursday", 150, 3, "CS201", mapOf("CS201" to 120, "PHY102" to 30))
        )
        val goals = mapOf("CS201" to 5.0, "PHY102" to 4.0)
        val mastery = mapOf(
            "CS201" to listOf(
                com.example.data.local.SubjectMasteryEntity(courseCode = "CS201", weekNumber = 1, weekLabel = "W1", masteryPercentage = 64.0),
                com.example.data.local.SubjectMasteryEntity(courseCode = "CS201", weekNumber = 4, weekLabel = "W4", masteryPercentage = 92.0)
            ),
            "PHY102" to listOf(
                com.example.data.local.SubjectMasteryEntity(courseCode = "PHY102", weekNumber = 1, weekLabel = "W1", masteryPercentage = 45.0),
                com.example.data.local.SubjectMasteryEntity(courseCode = "PHY102", weekNumber = 4, weekLabel = "W4", masteryPercentage = 70.0)
            )
        )

        val report = com.example.data.local.SubjectComparisonHelper.buildComparisonReport(
            courseCodeA = "CS201",
            courseCodeB = "PHY102",
            statsList = stats,
            trendsList = trends,
            goalsMap = goals,
            defaultGoalsMap = goals,
            masteryMap = mastery,
            defaultMasteryProvider = { emptyList() },
            dateRange = com.example.data.local.StudyDateRange.LAST_7_DAYS
        )

        assertEquals("CS201", report.subjectA.courseCode)
        assertEquals("PHY102", report.subjectB.courseCode)
        assertEquals(90, report.subjectA.goalAttainmentPct)
        assertEquals(25, report.subjectB.goalAttainmentPct)
        assertEquals(65, report.attainmentGapPct)
        assertEquals(3.5, report.hoursGap, 0.001)
        assertEquals(22.0, report.masteryGapPct, 0.001)
        assertEquals("Study Volume Gap", report.efficiencyGapType)
        assertEquals("PHY102", report.laggingCourseCode)
        assertEquals("CS201", report.leaderCourseCode)
        assertEquals(3, report.bucketPoints.size)
        assertEquals(2.0, report.bucketPoints[2].subjectAHours, 0.001)
        assertEquals(0.5, report.bucketPoints[2].subjectBHours, 0.001)
        assertEquals(1.5, report.bucketPoints[2].gapHours, 0.001)
    }

    @Test
    fun `test SubjectComparisonHelper identifies Mastery Yield Gap and Focus Depth Gap`() {
        val goals = mapOf("CS201" to 5.0, "MATH204" to 5.0)
        // Same logged hours (4.0h / 80% each), same avg session (40m), but 18% mastery gap
        val sameHoursStats = listOf(
            com.example.data.local.SubjectStudyStats("CS201", totalMinutes = 240, sessionCount = 6),
            com.example.data.local.SubjectStudyStats("MATH204", totalMinutes = 240, sessionCount = 6)
        )
        val masteryYieldGap = mapOf(
            "CS201" to listOf(com.example.data.local.SubjectMasteryEntity(courseCode = "CS201", weekNumber = 4, weekLabel = "W4", masteryPercentage = 92.0)),
            "MATH204" to listOf(com.example.data.local.SubjectMasteryEntity(courseCode = "MATH204", weekNumber = 4, weekLabel = "W4", masteryPercentage = 74.0))
        )
        val masteryReport = com.example.data.local.SubjectComparisonHelper.buildComparisonReport(
            courseCodeA = "CS201",
            courseCodeB = "MATH204",
            statsList = sameHoursStats,
            trendsList = emptyList(),
            goalsMap = goals,
            defaultGoalsMap = goals,
            masteryMap = masteryYieldGap,
            defaultMasteryProvider = { emptyList() },
            dateRange = com.example.data.local.StudyDateRange.THIS_MONTH
        )
        assertEquals("Mastery Yield Gap", masteryReport.efficiencyGapType)
        assertEquals(10.0, masteryReport.subjectA.goalHours, 0.001) // THIS_MONTH 2x scaling
        assertEquals(0, masteryReport.attainmentGapPct)
        assertEquals(18.0, masteryReport.masteryGapPct, 0.001)

        // Focus Depth Gap: similar hours & mastery, but 50m/session vs 25m/session
        val depthStats = listOf(
            com.example.data.local.SubjectStudyStats("CS201", totalMinutes = 250, sessionCount = 5),  // 50m avg
            com.example.data.local.SubjectStudyStats("MATH204", totalMinutes = 250, sessionCount = 10) // 25m avg
        )
        val closeMastery = mapOf(
            "CS201" to listOf(com.example.data.local.SubjectMasteryEntity(courseCode = "CS201", weekNumber = 4, weekLabel = "W4", masteryPercentage = 86.0)),
            "MATH204" to listOf(com.example.data.local.SubjectMasteryEntity(courseCode = "MATH204", weekNumber = 4, weekLabel = "W4", masteryPercentage = 84.0))
        )
        val depthReport = com.example.data.local.SubjectComparisonHelper.buildComparisonReport(
            courseCodeA = "CS201",
            courseCodeB = "MATH204",
            statsList = depthStats,
            trendsList = emptyList(),
            goalsMap = goals,
            defaultGoalsMap = goals,
            masteryMap = closeMastery,
            defaultMasteryProvider = { emptyList() },
            dateRange = com.example.data.local.StudyDateRange.LAST_7_DAYS
        )
        assertEquals("Focus Depth Gap", depthReport.efficiencyGapType)
        assertEquals(25, depthReport.avgSessionGapMins)
    }

    @Test
    fun `test MonthlyTrendHelper aggregates 4-week trend line and growth metrics`() {
        val dayMs = com.example.data.local.StudyDateRangeHelper.ONE_DAY_MS
        val nowMs = 1_760_000_000_000L
        val sessions = listOf(
            // Wk 1 (22-30d ago): 2.0h (120m)
            com.example.data.local.StudySessionEntity(courseCode = "CS201", subjectName = "Data Structures", durationMinutes = 60, timestampMs = nowMs - (25L * dayMs), dateDisplay = "25d ago"),
            com.example.data.local.StudySessionEntity(courseCode = "MATH204", subjectName = "Linear Algebra", durationMinutes = 60, timestampMs = nowMs - (24L * dayMs), dateDisplay = "24d ago"),
            // Wk 2 (15-21d ago): 3.0h (180m)
            com.example.data.local.StudySessionEntity(courseCode = "CS201", subjectName = "Data Structures", durationMinutes = 120, timestampMs = nowMs - (18L * dayMs), dateDisplay = "18d ago"),
            com.example.data.local.StudySessionEntity(courseCode = "PHY102", subjectName = "Physics", durationMinutes = 60, timestampMs = nowMs - (17L * dayMs), dateDisplay = "17d ago"),
            // Wk 3 (8-14d ago): 2.5h (150m)
            com.example.data.local.StudySessionEntity(courseCode = "MATH204", subjectName = "Linear Algebra", durationMinutes = 90, timestampMs = nowMs - (10L * dayMs), dateDisplay = "10d ago"),
            com.example.data.local.StudySessionEntity(courseCode = "CS201", subjectName = "Data Structures", durationMinutes = 60, timestampMs = nowMs - (9L * dayMs), dateDisplay = "9d ago"),
            // Wk 4 (0-7d ago): 4.0h (240m)
            com.example.data.local.StudySessionEntity(courseCode = "CS201", subjectName = "Data Structures", durationMinutes = 180, timestampMs = nowMs - (2L * dayMs), dateDisplay = "2d ago"),
            com.example.data.local.StudySessionEntity(courseCode = "CS310", subjectName = "OS", durationMinutes = 60, timestampMs = nowMs - (1L * dayMs), dateDisplay = "Yesterday"),
            // Outside 30d window (excluded from 4-week monthly view)
            com.example.data.local.StudySessionEntity(courseCode = "CS201", subjectName = "Data Structures", durationMinutes = 120, timestampMs = nowMs - (45L * dayMs), dateDisplay = "45d ago")
        )

        val report = com.example.data.local.MonthlyTrendHelper.buildFourWeekReport(sessions, subjectFilter = null, nowMs = nowMs)
        assertEquals(4, report.weeks.size)
        assertEquals(listOf("Wk 1", "Wk 2", "Wk 3", "Wk 4"), report.weeks.map { it.weekKey })
        assertEquals(2.0, report.weeks[0].totalHours, 0.001)
        assertEquals(3.0, report.weeks[1].totalHours, 0.001)
        assertEquals(2.5, report.weeks[2].totalHours, 0.001)
        assertEquals(4.0, report.weeks[3].totalHours, 0.001)

        // Week-over-week deltas
        assertEquals(null, report.weeks[0].deltaFromPrevWeekHours)
        assertEquals(1.0, report.weeks[1].deltaFromPrevWeekHours!!, 0.001)
        assertEquals(-0.5, report.weeks[2].deltaFromPrevWeekHours!!, 0.001)
        assertEquals(1.5, report.weeks[3].deltaFromPrevWeekHours!!, 0.001)

        assertEquals(11.5, report.totalMonthHours, 0.001)
        assertEquals(2.875, report.weeklyAverageHours, 0.001)
        assertEquals("Wk 4", report.peakWeek?.weekKey)
        assertEquals(2.0, report.w1ToW4GrowthHours, 0.001)
        assertEquals(100, report.w1ToW4GrowthPct)
        assertEquals("Upward Momentum", report.momentumLabel)

        // Filter by CS201 only -> 1.0h, 2.0h, 1.0h, 3.0h (Total 7.0h)
        val cs201Report = com.example.data.local.MonthlyTrendHelper.buildFourWeekReport(sessions, subjectFilter = "CS201", nowMs = nowMs)
        assertEquals("CS201", cs201Report.activeSubjectFilter)
        assertEquals(7.0, cs201Report.totalMonthHours, 0.001)
        assertEquals(1.0, cs201Report.weeks[0].totalHours, 0.001)
        assertEquals(3.0, cs201Report.weeks[3].totalHours, 0.001)
    }

    @Test
    fun `test SubjectGoalAlertHelper identifies lagging subjects against custom weekly goals`() {
        val dayMs = com.example.data.local.StudyDateRangeHelper.ONE_DAY_MS
        val nowMs = 1_760_000_000_000L
        val courses = listOf(
            CourseEntity(code = "CS201", name = "Data Structures", instructor = "Prof. A", room = "R1", dayOfWeek = "Mon", timeSlot = "10:00"),
            CourseEntity(code = "MATH204", name = "Linear Algebra", instructor = "Prof. B", room = "R2", dayOfWeek = "Tue", timeSlot = "11:00"),
            CourseEntity(code = "PHY102", name = "Physics", instructor = "Prof. C", room = "R3", dayOfWeek = "Wed", timeSlot = "12:00")
        )
        val customGoals = mapOf(
            "CS201" to 5.0,    // 4.0h logged -> 80% (on track, >=75%)
            "MATH204" to 6.0,  // 3.6h logged -> 60% (behind, not critical)
            "PHY102" to 4.0    // 1.0h logged -> 25% (critically behind <50%)
        )
        val sessions = listOf(
            com.example.data.local.StudySessionEntity(courseCode = "CS201", subjectName = "Data Structures", durationMinutes = 240, timestampMs = nowMs - (1L * dayMs), dateDisplay = "Yesterday"),
            com.example.data.local.StudySessionEntity(courseCode = "MATH204", subjectName = "Linear Algebra", durationMinutes = 216, timestampMs = nowMs - (2L * dayMs), dateDisplay = "2d ago"),
            com.example.data.local.StudySessionEntity(courseCode = "PHY102", subjectName = "Physics", durationMinutes = 60, timestampMs = nowMs - (3L * dayMs), dateDisplay = "3d ago"),
            // Old session outside 7-day window should be excluded
            com.example.data.local.StudySessionEntity(courseCode = "PHY102", subjectName = "Physics", durationMinutes = 300, timestampMs = nowMs - (14L * dayMs), dateDisplay = "14d ago")
        )

        val report = com.example.data.local.SubjectGoalAlertHelper.evaluateWeeklySubjectGoals(
            courses = courses,
            sessions = sessions,
            goalsMap = customGoals,
            defaultGoalsMap = customGoals,
            nowMs = nowMs
        )

        assertTrue(report.hasLaggingSubjects)
        assertEquals(2, report.laggingSubjects.size)
        assertEquals(1, report.criticallyBehindCount)

        // Sorted with critically behind PHY102 first, then MATH204
        assertEquals("PHY102", report.laggingSubjects[0].courseCode)
        assertTrue(report.laggingSubjects[0].isCriticallyBehind)
        assertEquals(25, report.laggingSubjects[0].attainmentPct)
        assertEquals(3.0, report.laggingSubjects[0].deficitHours, 0.001)

        assertEquals("MATH204", report.laggingSubjects[1].courseCode)
        assertFalse(report.laggingSubjects[1].isCriticallyBehind)
        assertEquals(60, report.laggingSubjects[1].attainmentPct)
        assertEquals(2.4, report.laggingSubjects[1].deficitHours, 0.001)

        val (title, body) = com.example.data.local.SubjectGoalAlertHelper.formatAlertNotificationContent(report)
        assertTrue(title.contains("2 Subjects Behind"))
        assertTrue(body.contains("PHY102"))
        assertTrue(body.contains("MATH204"))
    }

    @Test
    fun `test SubjectGoalAlertHelper reports no alerts when all weekly goals are on track`() {
        val dayMs = com.example.data.local.StudyDateRangeHelper.ONE_DAY_MS
        val nowMs = 1_760_000_000_000L
        val courses = listOf(
            CourseEntity(code = "CS201", name = "Data Structures", instructor = "Prof. A", room = "R1", dayOfWeek = "Mon", timeSlot = "10:00")
        )
        val goals = mapOf("CS201" to 4.0)
        val sessions = listOf(
            com.example.data.local.StudySessionEntity(courseCode = "CS201", subjectName = "Data Structures", durationMinutes = 240, timestampMs = nowMs - (1L * dayMs), dateDisplay = "Yesterday")
        )

        val report = com.example.data.local.SubjectGoalAlertHelper.evaluateWeeklySubjectGoals(
            courses = courses,
            sessions = sessions,
            goalsMap = goals,
            defaultGoalsMap = goals,
            nowMs = nowMs
        )

        assertFalse(report.hasLaggingSubjects)
        assertEquals(0, report.laggingSubjects.size)
        assertEquals(0, report.criticallyBehindCount)
        val (title, _) = com.example.data.local.SubjectGoalAlertHelper.formatAlertNotificationContent(report)
        assertTrue(title.contains("On Track"))
    }

    @Test
    fun `test FocusScoreHelper computes focused vs total ratio and detects shallow studying`() {
        val dayMs = com.example.data.local.StudyDateRangeHelper.ONE_DAY_MS
        val nowMs = 1_760_000_000_000L
        val courses = listOf(
            CourseEntity(code = "CS201", name = "Data Structures", instructor = "Prof. A", room = "R1", dayOfWeek = "Mon", timeSlot = "10:00"),
            CourseEntity(code = "MATH204", name = "Linear Algebra", instructor = "Prof. B", room = "R2", dayOfWeek = "Tue", timeSlot = "11:00"),
            CourseEntity(code = "PHY102", name = "Physics", instructor = "Prof. C", room = "R3", dayOfWeek = "Wed", timeSlot = "12:00"),
            CourseEntity(code = "ENG215", name = "Technical Writing", instructor = "Prof. D", room = "R4", dayOfWeek = "Thu", timeSlot = "13:00")
        )
        val sessions = listOf(
            // CS201: 90m FOCUS + 10m SHORT_BREAK = 90/100 -> 90% Deep Focus
            com.example.data.local.StudySessionEntity(courseCode = "CS201", subjectName = "Data Structures", durationMinutes = 90, timestampMs = nowMs - dayMs, dateDisplay = "Yesterday", mode = "FOCUS"),
            com.example.data.local.StudySessionEntity(courseCode = "CS201", subjectName = "Data Structures", durationMinutes = 10, timestampMs = nowMs - dayMs, dateDisplay = "Yesterday", mode = "SHORT_BREAK"),
            // MATH204: 70m FOCUS + 30m SHALLOW = 70/100 -> 70% Moderate Focus
            com.example.data.local.StudySessionEntity(courseCode = "MATH204", subjectName = "Linear Algebra", durationMinutes = 70, timestampMs = nowMs - (2L * dayMs), dateDisplay = "2d ago", mode = "FOCUS"),
            com.example.data.local.StudySessionEntity(courseCode = "MATH204", subjectName = "Linear Algebra", durationMinutes = 30, timestampMs = nowMs - (2L * dayMs), dateDisplay = "2d ago", mode = "SHALLOW"),
            // PHY102: 40m FOCUS + 40m SHALLOW + 20m LONG_BREAK = 40/100 -> 40% Shallow Studying
            com.example.data.local.StudySessionEntity(courseCode = "PHY102", subjectName = "Physics", durationMinutes = 40, timestampMs = nowMs - (3L * dayMs), dateDisplay = "3d ago", mode = "FOCUS"),
            com.example.data.local.StudySessionEntity(courseCode = "PHY102", subjectName = "Physics", durationMinutes = 40, timestampMs = nowMs - (3L * dayMs), dateDisplay = "3d ago", mode = "SHALLOW"),
            com.example.data.local.StudySessionEntity(courseCode = "PHY102", subjectName = "Physics", durationMinutes = 20, timestampMs = nowMs - (3L * dayMs), dateDisplay = "3d ago", mode = "LONG_BREAK"),
            // Session outside 7-day window (20 days ago) should be excluded in LAST_7_DAYS
            com.example.data.local.StudySessionEntity(courseCode = "ENG215", subjectName = "Technical Writing", durationMinutes = 60, timestampMs = nowMs - (20L * dayMs), dateDisplay = "20d ago", mode = "FOCUS")
        )

        val report7d = com.example.data.local.FocusScoreHelper.buildFocusScoreReport(
            courses = courses,
            sessions = sessions,
            dateRange = com.example.data.local.StudyDateRange.LAST_7_DAYS,
            nowMs = nowMs
        )

        // Overall 7d: 200m focused / 300m total = 67% Moderate
        assertEquals(200, report7d.overallFocusedMinutes)
        assertEquals(70, report7d.overallShallowMinutes)
        assertEquals(30, report7d.overallBreakMinutes)
        assertEquals(300, report7d.overallTotalLoggedMinutes)
        assertEquals(67, report7d.overallFocusScorePct)
        assertEquals(com.example.data.local.FocusQualityTier.MODERATE, report7d.overallTier)
        assertEquals(1, report7d.shallowStudyingCount)
        assertEquals(1, report7d.deepFocusCount)
        assertEquals("PHY102", report7d.topShallowSubject?.courseCode)
        assertEquals("CS201", report7d.bestFocusSubject?.courseCode)

        // Sorted lowest active focus score first, then no-data last
        assertEquals(listOf("PHY102", "MATH204", "CS201", "ENG215"), report7d.subjects.map { it.courseCode })
        assertEquals(com.example.data.local.FocusQualityTier.SHALLOW, report7d.subjects[0].tier)
        assertTrue(report7d.subjects[0].isShallowStudying)
        assertTrue(report7d.subjects[0].recommendedStrategies.isNotEmpty())
        assertEquals(com.example.data.local.FocusQualityTier.NO_DATA, report7d.subjects[3].tier)

        // When switched to THIS_MONTH, ENG215's 20d-ago 60m focus session is included
        val reportMonth = com.example.data.local.FocusScoreHelper.buildFocusScoreReport(
            courses = courses,
            sessions = sessions,
            dateRange = com.example.data.local.StudyDateRange.THIS_MONTH,
            nowMs = nowMs
        )
        assertEquals(260, reportMonth.overallFocusedMinutes)
        assertEquals(360, reportMonth.overallTotalLoggedMinutes)
        assertEquals(2, reportMonth.deepFocusCount)
    }
}
