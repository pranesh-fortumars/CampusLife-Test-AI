package com.example

import com.example.data.MockData
import com.example.model.Subject
import org.junit.Assert.*
import org.junit.Test

class CampusLifeUnitTest {

    @Test
    fun testAttendancePercentageCalculation() {
        val subject = Subject(
            code = "CS-302",
            name = "DBMS",
            credits = 4,
            faculty = "Dr. Roy",
            attendedClasses = 26,
            totalClasses = 30,
            nextClassTime = "10:30 AM",
            room = "Lab 304"
        )

        val pct = subject.attendancePercentage
        assertEquals(86.666, pct, 0.1)
        assertEquals(4, subject.missedClasses)
        assertTrue(subject.safeBunksLeft >= 3)
    }

    @Test
    fun testAttendanceBelowThresholdNeedsClasses() {
        val subject = Subject(
            code = "CS-304",
            name = "Operating Systems",
            credits = 4,
            faculty = "Prof. Muralidhar",
            attendedClasses = 23,
            totalClasses = 31,
            nextClassTime = "9:15 AM",
            room = "LH-102"
        )

        val pct = subject.attendancePercentage
        assertTrue(pct < 75.0)
        assertEquals(8, subject.missedClasses)
        assertEquals(1, subject.neededToReach75)
    }

    @Test
    fun testLogClassAttendedIncreasesAttendedAndRecalculates() {
        val initial = Subject(
            code = "CS-304",
            name = "Operating Systems",
            credits = 4,
            faculty = "Prof. Muralidhar",
            attendedClasses = 23,
            totalClasses = 31
        )
        assertEquals(74.19, initial.attendancePercentage, 0.1)

        // Student logs 1 class attended
        val updated = initial.copy(
            attendedClasses = initial.attendedClasses + 1,
            totalClasses = initial.totalClasses + 1
        )
        assertEquals(24, updated.attendedClasses)
        assertEquals(8, updated.missedClasses)
        assertEquals(32, updated.totalClasses)
        assertEquals(75.0, updated.attendancePercentage, 0.01)
        assertEquals(0, updated.neededToReach75)
    }

    @Test
    fun testLogClassMissedIncreasesTotalAndLowersPercentage() {
        val initial = Subject(
            code = "CS-302",
            name = "DBMS",
            credits = 4,
            faculty = "Dr. Roy",
            attendedClasses = 26,
            totalClasses = 30
        )
        assertEquals(86.67, initial.attendancePercentage, 0.1)

        // Student misses a class
        val updated = initial.copy(
            attendedClasses = initial.attendedClasses,
            totalClasses = initial.totalClasses + 1
        )
        assertEquals(26, updated.attendedClasses)
        assertEquals(5, updated.missedClasses)
        assertEquals(31, updated.totalClasses)
        assertEquals(83.87, updated.attendancePercentage, 0.1)
    }

    @Test
    fun testProjectedCalculator() {
        val subject = Subject(
            code = "CS-306",
            name = "Computer Networks",
            credits = 4,
            faculty = "Prof. Sanyal",
            attendedClasses = 22,
            totalClasses = 28
        )
        // If student attends next 2 classes: 24 / 30 = 80.0%
        val projectedAttending = subject.calculateProjected(addAttended = 2, addMissed = 0)
        assertEquals(80.0, projectedAttending, 0.01)

        // If student misses next 2 classes: 22 / 30 = 73.33%
        val projectedMissing = subject.calculateProjected(addAttended = 0, addMissed = 2)
        assertEquals(73.33, projectedMissing, 0.1)
    }

    @Test
    fun testMockDataIntegrity() {
        assertNotNull(MockData.defaultProfile)
        assertEquals("Rahul Sharma", MockData.defaultProfile.name)
        assertTrue(MockData.initialSubjects.isNotEmpty())
        assertTrue(MockData.assignments.isNotEmpty())
        assertTrue(MockData.projects.isNotEmpty())
        assertTrue(MockData.societies.isNotEmpty())
    }

    @Test
    fun testTaskEntityToDomainMapping() {
        val entity = com.example.data.local.TaskEntity(
            id = "test-task-1",
            title = "OS Semaphore Lab",
            subjectCode = "CS-304",
            subjectName = "Operating Systems",
            category = "Project",
            type = "PROJECT",
            dueDate = "Oct 29",
            dueTime = "11:59 PM",
            priority = "URGENT",
            weightage = "20% Project",
            isCompleted = false,
            progressPercent = 50,
            faculty = "Prof. Muralidhar",
            note = "Dining philosophers in C"
        )
        val domain = entity.toDomain()
        assertEquals("test-task-1", domain.id)
        assertEquals("OS Semaphore Lab", domain.title)
        assertEquals("CS-304", domain.subjectCode)
        assertEquals("Project", domain.category)
        assertEquals("PROJECT", domain.type)
        assertEquals("URGENT", domain.priority)
        assertFalse(domain.isCompleted)
    }

    @Test
    fun testAcademicTaskCategoryTagging() {
        val examTask = com.example.model.AcademicTask(
            title = "Mid-Term Examination: Operating Systems",
            subjectCode = "CS-304",
            subjectName = "Operating Systems",
            category = "Exam",
            dueDate = "Oct 30"
        )
        assertEquals("Exam", examTask.category)

        val assignmentTask = com.example.model.AcademicTask(
            title = "DBMS Problem Set 4",
            subjectCode = "CS-302",
            subjectName = "DBMS",
            category = "Assignment",
            dueDate = "Oct 25"
        )
        assertEquals("Assignment", assignmentTask.category)

        val projectTask = com.example.model.AcademicTask(
            title = "Capstone Milestone 1",
            subjectCode = "CS-308",
            subjectName = "Software Engineering",
            category = "Project",
            dueDate = "Nov 05"
        )
        assertEquals("Project", projectTask.category)

        // Verify default category
        val defaultTask = com.example.model.AcademicTask(
            title = "Default Category Task",
            subjectCode = "CS-310",
            subjectName = "Web",
            dueDate = "Nov 12"
        )
        assertEquals("Assignment", defaultTask.category)

        // Verify category-based filtering
        val allTasks = listOf(examTask, assignmentTask, projectTask, defaultTask)
        val exams = allTasks.filter { it.category.equals("Exam", ignoreCase = true) }
        val assignments = allTasks.filter { it.category.equals("Assignment", ignoreCase = true) }
        val projects = allTasks.filter { it.category.equals("Project", ignoreCase = true) }

        assertEquals(1, exams.size)
        assertEquals(2, assignments.size)
        assertEquals(1, projects.size)
        assertEquals("Mid-Term Examination: Operating Systems", exams.first().title)
        assertEquals("Capstone Milestone 1", projects.first().title)
    }

    @Test
    fun testCourseAttendanceDataModelCalculations() {
        // Course 1: Safe (> 75%)
        val safeCourse = com.example.model.CourseAttendance(
            courseCode = "CS-302",
            courseName = "Database Management Systems",
            attendedClasses = 26,
            totalClasses = 30,
            targetPercentage = 75.0,
            credits = 4,
            faculty = "Dr. Ananya Roy"
        )
        assertEquals(86.67, safeCourse.attendancePercentage, 0.1)
        assertEquals(0.8667f, safeCourse.progressFraction, 0.01f)
        assertTrue(safeCourse.isSafe)
        assertEquals(com.example.model.AttendanceStatus.SAFE, safeCourse.status)
        assertEquals(4, safeCourse.missedClasses)
        assertEquals(4, safeCourse.safeBunksLeft)
        assertEquals(0, safeCourse.neededToReachTarget)
        assertEquals("86.7%", safeCourse.formattedPercentage)

        // Course 2: Warning / Critical (< 75%)
        val lowCourse = com.example.model.CourseAttendance(
            courseCode = "CS-304",
            courseName = "Operating Systems",
            attendedClasses = 19,
            totalClasses = 28,
            targetPercentage = 75.0,
            credits = 4,
            faculty = "Prof. K. Muralidhar"
        )
        assertEquals(67.86, lowCourse.attendancePercentage, 0.1)
        assertEquals(0.6786f, lowCourse.progressFraction, 0.01f)
        assertFalse(lowCourse.isSafe)
        assertEquals(com.example.model.AttendanceStatus.CRITICAL, lowCourse.status)
        assertEquals(9, lowCourse.missedClasses)
        assertEquals(0, lowCourse.safeBunksLeft)
        assertTrue(lowCourse.neededToReachTarget > 0)

        // Course 3: Edge case 0 total classes
        val newCourse = com.example.model.CourseAttendance(
            courseCode = "CS-312",
            courseName = "Cloud Computing",
            attendedClasses = 0,
            totalClasses = 0
        )
        assertEquals(100.0, newCourse.attendancePercentage, 0.01)
        assertEquals(1.0f, newCourse.progressFraction, 0.01f)
        assertTrue(newCourse.isSafe)

        // Test projection calculation
        val projectedAttending = lowCourse.calculateProjected(addAttended = 4, addMissed = 0)
        // (19 + 4) / (28 + 4) = 23 / 32 = 71.875%
        assertEquals(71.88, projectedAttending, 0.1)

        // Test Summary calculation
        val summary = com.example.model.CourseAttendanceSummary(listOf(safeCourse, lowCourse))
        assertEquals(45, summary.totalAttended)
        assertEquals(58, summary.totalClasses)
        assertEquals(77.59, summary.overallPercentage, 0.1)
        assertEquals(1, summary.safeCount)
        assertEquals(1, summary.criticalCount)
    }

    @Test
    fun testSubjectToCourseAttendanceMapping() {
        val subject = Subject(
            code = "CS-306",
            name = "Computer Networks",
            credits = 4,
            faculty = "Dr. R. Venkat",
            attendedClasses = 22,
            totalClasses = 28,
            room = "Lab 2"
        )
        val courseAttendance = com.example.model.CourseAttendance.fromSubject(subject)
        assertEquals("CS-306", courseAttendance.courseCode)
        assertEquals("Computer Networks", courseAttendance.courseName)
        assertEquals(22, courseAttendance.attendedClasses)
        assertEquals(28, courseAttendance.totalClasses)
        assertEquals(78.57, courseAttendance.attendancePercentage, 0.1)
        assertTrue(courseAttendance.isSafe)
    }

    @Test
    fun testTaskEntityFromDomainAndUpcomingDueSorting() {
        val now = System.currentTimeMillis()
        val examDomain = com.example.model.AcademicTask(
            id = "exam-1",
            title = "Operating Systems Mid-Term",
            subjectCode = "CS-304",
            subjectName = "Operating Systems",
            category = "Exam",
            type = "EXAM_PREP",
            dueDate = "Oct 30",
            dueTimestamp = now + 86400000L * 5
        )
        val entityFromDomain = com.example.data.local.TaskEntity.fromDomain(examDomain)
        assertEquals("exam-1", entityFromDomain.id)
        assertEquals("Operating Systems Mid-Term", entityFromDomain.title)
        assertEquals("CS-304", entityFromDomain.subjectCode)
        assertEquals("Exam", entityFromDomain.category)
        assertEquals("EXAM_PREP", entityFromDomain.type)
        assertEquals(examDomain.dueTimestamp, entityFromDomain.dueTimestamp)

        // Mapping back to domain
        val mappedDomain = entityFromDomain.toDomain()
        assertEquals(examDomain.id, mappedDomain.id)
        assertEquals(examDomain.title, mappedDomain.title)
        assertEquals(examDomain.category, mappedDomain.category)

        // Test sorting by upcoming due dates
        val assignmentDomain = com.example.model.AcademicTask(
            id = "asgn-1",
            title = "Networks Lab Assignment",
            subjectCode = "CS-306",
            subjectName = "Networks",
            category = "Assignment",
            dueDate = "Today",
            dueTimestamp = now + 3600000L * 3 // Due sooner
        )
        val tasks = listOf(examDomain, assignmentDomain)
        val sortedUpcoming = tasks.sortedBy { it.dueTimestamp }
        assertEquals("asgn-1", sortedUpcoming.first().id)
        assertEquals("exam-1", sortedUpcoming.last().id)
    }

    @Test
    fun testSemesterGradesAndCumulativeGpaCalculations() {
        // Course grades with credits & letter grades
        val c1 = com.example.model.CourseGrade(
            courseCode = "CS-101",
            courseName = "Introduction to Programming",
            credits = 4,
            grade = "O" // 10.0 grade points -> 40.0 credit points
        )
        val c2 = com.example.model.CourseGrade(
            courseCode = "MA-101",
            courseName = "Calculus & Linear Algebra",
            credits = 4,
            grade = "A+" // 9.0 grade points -> 36.0 credit points
        )
        val c3 = com.example.model.CourseGrade(
            courseCode = "PH-101",
            courseName = "Physics for Computing",
            credits = 4,
            grade = "A" // 8.0 grade points -> 32.0 credit points
        )

        assertEquals(40.0, c1.creditPoints, 0.001)
        assertEquals(36.0, c2.creditPoints, 0.001)
        assertEquals(32.0, c3.creditPoints, 0.001)

        val sem1 = com.example.model.SemesterGrades(
            semesterNumber = 1,
            semesterName = "Semester 1",
            academicYear = "2023-24",
            courses = listOf(c1, c2, c3),
            isCurrentSemester = false
        )

        // 12 credits, 108 earned points -> SGPA = 9.0
        assertEquals(12, sem1.totalCredits)
        assertEquals(108.0, sem1.totalEarnedPoints, 0.001)
        assertEquals(9.0, sem1.sgpa, 0.001)
        assertEquals("9.00", sem1.formattedSgpa)

        // Semester 2: 12 credits, SGPA 8.0
        val c4 = com.example.model.CourseGrade(
            courseCode = "CS-102",
            courseName = "Data Structures",
            credits = 4,
            grade = "A" // 8.0
        )
        val c5 = com.example.model.CourseGrade(
            courseCode = "CS-104",
            courseName = "Digital Logic",
            credits = 4,
            grade = "B+" // 7.0
        )
        val c6 = com.example.model.CourseGrade(
            courseCode = "MA-102",
            courseName = "Discrete Mathematics",
            credits = 4,
            grade = "A+" // 9.0
        )
        val sem2 = com.example.model.SemesterGrades(
            semesterNumber = 2,
            semesterName = "Semester 2",
            academicYear = "2023-24",
            courses = listOf(c4, c5, c6),
            isCurrentSemester = false
        )
        // 12 credits, (32 + 28 + 36) = 96 points -> SGPA = 8.0
        assertEquals(12, sem2.totalCredits)
        assertEquals(96.0, sem2.totalEarnedPoints, 0.001)
        assertEquals(8.0, sem2.sgpa, 0.001)

        // CGPA calculation across Sem 1 and Sem 2:
        // Total credits = 24, total points = 108 + 96 = 204
        // CGPA = 204 / 24 = 8.50
        val gpaSummary = com.example.model.AcademicGpaSummary(listOf(sem1, sem2))
        assertEquals(24, gpaSummary.totalCreditsEarned)
        assertEquals(204.0, gpaSummary.totalEarnedPoints, 0.001)
        assertEquals(8.50, gpaSummary.cumulativeGpa, 0.001)
        assertEquals("8.50", gpaSummary.formattedCgpa)

        // AICTE Percentage: 8.5 * 9.5 = 80.75%
        assertEquals(80.75, gpaSummary.percentageEquivalent, 0.01)
        assertEquals("80.8%", gpaSummary.formattedPercentage)

        // US 4.0 scale: (8.5 / 10.0) * 4.0 = 3.40
        assertEquals(3.40, gpaSummary.equivalentUsGpa, 0.01)
        assertEquals("3.40", gpaSummary.formattedUsGpa)

        // Forecasting: Projected CGPA if Sem 3 has 20 credits and student scores 9.0 SGPA
        // Total credits = 44, total points = 204 + 180 = 384 -> CGPA = 384 / 44 = 8.727
        val projected = gpaSummary.calculateProjectedCgpa(activeSemesterCredits = 20, projectedSgpa = 9.0)
        assertEquals(8.727, projected, 0.01)
    }

    @Test
    fun testCourseGradeEntityRoomMapping() {
        val domainGrade = com.example.model.CourseGrade(
            id = "grade-test-1",
            courseCode = "CS-401",
            courseName = "Compiler Design",
            credits = 4,
            grade = "A+",
            gradePoint = 9.0,
            isCompleted = true
        )
        val entity = com.example.data.local.CourseGradeEntity.fromDomain(
            domain = domainGrade,
            semesterNumber = 6,
            semesterName = "Semester 6",
            academicYear = "2026-27"
        )
        assertEquals("grade-test-1", entity.id)
        assertEquals("CS-401", entity.courseCode)
        assertEquals("Compiler Design", entity.courseName)
        assertEquals(4, entity.credits)
        assertEquals("A+", entity.grade)
        assertEquals(9.0, entity.gradePoint, 0.001)
        assertEquals(6, entity.semesterNumber)

        val restored = entity.toDomain()
        assertEquals(domainGrade.id, restored.id)
        assertEquals(domainGrade.courseCode, restored.courseCode)
        assertEquals(domainGrade.grade, restored.grade)
        assertEquals(domainGrade.gradePoint, restored.gradePoint, 0.001)
        assertEquals(36.0, restored.creditPoints, 0.001)
    }

    @Test
    fun testDailyAttendanceAndOverallCoursePercentage() {
        val course1 = Subject(
            code = "CS-302",
            name = "DBMS",
            credits = 4,
            faculty = "Dr. Roy",
            attendedClasses = 26,
            totalClasses = 30
        )
        val course2 = Subject(
            code = "CS-304",
            name = "Operating Systems",
            credits = 4,
            faculty = "Prof. Muralidhar",
            attendedClasses = 20,
            totalClasses = 25
        )

        // Course 1: 26/30 = 86.67%
        // Course 2: 20/25 = 80.0%
        // Total: 46 / 55 = 83.64%
        val totalAttended = listOf(course1, course2).sumOf { it.attendedClasses }
        val totalClasses = listOf(course1, course2).sumOf { it.totalClasses }
        val overallPct = (totalAttended.toDouble() / totalClasses) * 100.0

        assertEquals(46, totalAttended)
        assertEquals(55, totalClasses)
        assertEquals(83.636, overallPct, 0.01)

        // Log 1 daily attendance attended for course 2
        val updatedCourse2 = course2.copy(
            attendedClasses = course2.attendedClasses + 1,
            totalClasses = course2.totalClasses + 1
        )
        // New Course 2: 21 / 26 = 80.77%
        assertEquals(80.769, updatedCourse2.attendancePercentage, 0.01)

        // New total: 47 / 56 = 83.93%
        val newTotalAttended = course1.attendedClasses + updatedCourse2.attendedClasses
        val newTotalClasses = course1.totalClasses + updatedCourse2.totalClasses
        val newOverallPct = (newTotalAttended.toDouble() / newTotalClasses) * 100.0
        assertEquals(83.928, newOverallPct, 0.01)
    }

    @Test
    fun testTaskListFilteringAndSorting() {
        val task1 = com.example.model.AcademicTask(
            id = "t-1",
            title = "OS Lab Assignment",
            subjectCode = "CS-304",
            subjectName = "Operating Systems",
            category = "Assignment",
            priority = "URGENT",
            dueTimestamp = 1000L,
            dueDate = "Today",
            isCompleted = false
        )
        val task2 = com.example.model.AcademicTask(
            id = "t-2",
            title = "DBMS Midterm Exam",
            subjectCode = "CS-302",
            subjectName = "DBMS",
            category = "Exam",
            priority = "HIGH",
            dueTimestamp = 2000L,
            dueDate = "Next Week",
            isCompleted = false
        )
        val task3 = com.example.model.AcademicTask(
            id = "t-3",
            title = "CN Socket Programming",
            subjectCode = "CS-306",
            subjectName = "Computer Networks",
            category = "Project",
            priority = "MEDIUM",
            dueTimestamp = 3000L,
            dueDate = "Oct 28",
            isCompleted = true
        )

        val allTasks = listOf(task1, task2, task3)

        // Filter: Pending (All Due)
        val pending = allTasks.filter { !it.isCompleted }
        assertEquals(2, pending.size)

        // Filter: Completed
        val completed = allTasks.filter { it.isCompleted }
        assertEquals(1, completed.size)
        assertEquals("CN Socket Programming", completed.first().title)

        // Filter: Exams
        val exams = allTasks.filter { it.category.equals("Exam", ignoreCase = true) }
        assertEquals(1, exams.size)
        assertEquals("CS-302", exams.first().subjectCode)

        // Sort by Due Date
        val sortedByDate = allTasks.sortedBy { it.dueTimestamp }
        assertEquals("t-1", sortedByDate[0].id)
        assertEquals("t-2", sortedByDate[1].id)
        assertEquals("t-3", sortedByDate[2].id)

        // Toggle task completion
        val toggledTask1 = task1.copy(isCompleted = !task1.isCompleted)
        assertTrue(toggledTask1.isCompleted)
    }

    @Test
    fun testTaskEntityIsCompletedFieldAndToggle() {
        val entity = com.example.data.local.TaskEntity(
            id = "unit-test-task",
            title = "Test Entity Completion Status",
            subjectCode = "CS-302",
            subjectName = "DBMS",
            dueDate = "Tomorrow",
            isCompleted = false,
            progressPercent = 0
        )
        assertFalse(entity.isCompleted)
        assertEquals(0, entity.progressPercent)

        // Toggle to true
        val completedEntity = entity.toggleCompleted()
        assertTrue(completedEntity.isCompleted)
        assertEquals(100, completedEntity.progressPercent)

        // Toggle back to false
        val revertedEntity = completedEntity.toggleCompleted()
        assertFalse(revertedEntity.isCompleted)
        assertEquals(0, revertedEntity.progressPercent)

        // Domain conversion test
        val domain = completedEntity.toDomain()
        assertTrue(domain.isCompleted)
        val fromDomain = com.example.data.local.TaskEntity.fromDomain(domain)
        assertTrue(fromDomain.isCompleted)
    }

    @Test
    fun testCourseLectureAttendanceModelAndForecasting() {
        val course = com.example.model.CourseAttendance(
            courseCode = "CS-302",
            courseName = "Database Management Systems",
            attendedClasses = 26,
            totalClasses = 30,
            targetPercentage = 75.0,
            credits = 4,
            faculty = "Dr. Ananya Roy",
            theoryAttended = 18,
            theoryTotal = 21,
            labAttended = 8,
            labTotal = 9
        )

        // Attendance Percentage: 26 / 30 = 86.67%
        assertEquals(86.666, course.attendancePercentage, 0.01)
        assertEquals("86.7%", course.formattedPercentage)
        assertEquals(4, course.missedClasses)
        assertTrue(course.isSafe)
        assertFalse(course.isBorderline)
        assertEquals(com.example.model.AttendanceStatus.SAFE, course.status)

        // Safe bunks margin: attended / (total + B) >= 0.75
        // 26 / (30 + B) >= 0.75 => 26 >= 22.5 + 0.75B => 3.5 >= 0.75B => B <= 4.66 => 4 safe bunks
        assertTrue(course.safeBunksLeft >= 4)
        assertEquals(0, course.neededToReachTarget)

        // Forecast: If student misses 2 upcoming lectures: 26 / 32 = 81.25%
        val forecastMiss2 = course.getForecast(futureAttends = 0, futureMisses = 2)
        assertEquals(26, forecastMiss2.projectedAttended)
        assertEquals(32, forecastMiss2.projectedTotal)
        assertEquals(81.25, forecastMiss2.projectedPercentage, 0.01)
        assertTrue(forecastMiss2.isProjectedSafe)
        assertTrue(forecastMiss2.percentageDelta < 0.0)

        // Forecast: If student attends 4 upcoming lectures: 30 / 34 = 88.24%
        val forecastAttend4 = course.getForecast(futureAttends = 4, futureMisses = 0)
        assertEquals(30, forecastAttend4.projectedAttended)
        assertEquals(34, forecastAttend4.projectedTotal)
        assertEquals(88.235, forecastAttend4.projectedPercentage, 0.01)
        assertTrue(forecastAttend4.isProjectedSafe)
        assertTrue(forecastAttend4.percentageDelta > 0.0)
    }

    @Test
    fun testCourseLectureAttendanceShortageAndRecoveryCalculation() {
        val criticalCourse = com.example.model.CourseAttendance(
            courseCode = "CS-304",
            courseName = "Operating Systems",
            attendedClasses = 20,
            totalClasses = 30, // 20/30 = 66.67% (Below 75%)
            targetPercentage = 75.0
        )

        assertFalse(criticalCourse.isSafe)
        assertEquals(com.example.model.AttendanceStatus.CRITICAL, criticalCourse.status)
        assertEquals(0, criticalCourse.safeBunksLeft)
        // Recovery calculation: (20 + X) / (30 + X) >= 0.75 => 20 + X >= 22.5 + 0.75X => 0.25X >= 2.5 => X >= 10
        assertEquals(10, criticalCourse.neededToReachTarget)

        // Check forecast after 10 consecutive attends: 30 / 40 = 75.0%
        val recoveryForecast = criticalCourse.getForecast(futureAttends = 10, futureMisses = 0)
        assertEquals(30, recoveryForecast.projectedAttended)
        assertEquals(40, recoveryForecast.projectedTotal)
        assertEquals(75.0, recoveryForecast.projectedPercentage, 0.01)
        assertTrue(recoveryForecast.isProjectedSafe)
    }

    @Test
    fun testLectureSessionLogModelAndTypes() {
        val log = com.example.model.LectureSessionLog(
            courseCode = "CS-302",
            courseName = "DBMS",
            date = "Oct 25, 2026",
            timeSlot = "10:30 AM",
            lectureType = com.example.model.LectureType.THEORY,
            status = com.example.model.LectureAttendanceStatus.PRESENT,
            topic = "Normalization and BCNF"
        )

        assertEquals("CS-302", log.courseCode)
        assertEquals(com.example.model.LectureType.THEORY, log.lectureType)
        assertEquals(com.example.model.LectureAttendanceStatus.PRESENT, log.status)
        assertEquals("Normalization and BCNF", log.topic)
    }

    @Test
    fun testAcademicProjectEntityRoomModelAndMilestones() {
        val project = com.example.data.local.AcademicProjectEntity(
            id = "proj-test-1",
            title = "Distributed In-Memory Cache with Raft Consensus",
            subjectCode = "CS-302",
            subjectName = "DBMS",
            description = "High performance partitioned key-value store with leader election",
            techStack = "Kotlin, Netty, Raft Consensus, Docker",
            deadlineDate = "Nov 12, 2026",
            milestonesCompleted = 3,
            totalMilestones = 5,
            status = "IN_PROGRESS",
            weightage = "25% Major Capstone",
            isCompleted = false
        )

        assertEquals("proj-test-1", project.id)
        assertEquals(60, project.progressPercentage)
        assertEquals(0.6f, project.progressFraction, 0.01f)
        assertFalse(project.isCompleted)

        // Toggle completion status
        val completedProj = project.toggleCompleted()
        assertTrue(completedProj.isCompleted)
        assertEquals("COMPLETED", completedProj.status)
        assertEquals(5, completedProj.milestonesCompleted)
        assertEquals(100, completedProj.progressPercentage)

        // Revert toggle
        val revertedProj = completedProj.toggleCompleted()
        assertFalse(revertedProj.isCompleted)
        assertEquals("IN_PROGRESS", revertedProj.status)
    }

    @Test
    fun testCommunityEventsDataPullsClubMeetingsAndSeminars() {
        val events = com.example.model.CampusEventMockData.sampleEvents
        assertTrue("Events list should not be empty", events.isNotEmpty())

        val clubMeetings = events.filter { it.eventType == com.example.model.CommunityEventType.CLUB_MEETING }
        val academicSeminars = events.filter { it.eventType == com.example.model.CommunityEventType.ACADEMIC_SEMINAR }

        assertTrue("Should contain club meetings", clubMeetings.isNotEmpty())
        assertTrue("Should contain academic seminars", academicSeminars.isNotEmpty())

        // Check seminar specifics
        val topSeminar = academicSeminars.first()
        assertTrue(topSeminar.speakerOrHost.isNotBlank())
        assertTrue(topSeminar.speakerAffiliation.isNotBlank())
        assertTrue(topSeminar.venue.isNotBlank())
        assertTrue(topSeminar.dateFormatted.isNotBlank())
    }

    @Test
    fun testEventRsvpStateTransition() {
        val initialEvent = com.example.model.CampusEvent(
            id = "test-evt-1",
            title = "Quantum Information Seminar",
            organizer = "Dept. of Applied Physics",
            eventType = com.example.model.CommunityEventType.ACADEMIC_SEMINAR,
            dateFormatted = "Tomorrow, 4:00 PM",
            timeFormatted = "4:00 PM - 5:30 PM",
            venue = "Physics Hall 101",
            attendeeCount = 20,
            rsvpStatus = com.example.model.RsvpStatus.NONE,
            isReminderSet = false,
            description = "Quantum seminar"
        )

        // User RSVPs GOING
        val goingEvent = initialEvent.copy(
            rsvpStatus = com.example.model.RsvpStatus.GOING,
            attendeeCount = initialEvent.attendeeCount + 1,
            isReminderSet = true
        )
        assertEquals(com.example.model.RsvpStatus.GOING, goingEvent.rsvpStatus)
        assertEquals(21, goingEvent.attendeeCount)
        assertTrue(goingEvent.isReminderSet)

        // User changes to INTERESTED
        val interestedEvent = goingEvent.copy(
            rsvpStatus = com.example.model.RsvpStatus.INTERESTED,
            attendeeCount = (goingEvent.attendeeCount - 1).coerceAtLeast(0)
        )
        assertEquals(com.example.model.RsvpStatus.INTERESTED, interestedEvent.rsvpStatus)
        assertEquals(20, interestedEvent.attendeeCount)
    }

    @Test
    fun testUpcomingTasksRoomDatabaseFilteringAndSorting() {
        val now = System.currentTimeMillis()
        val hour = 3600000L
        val day = 86400000L

        val task1 = com.example.model.AcademicTask(
            id = "test-task-1",
            title = "Computer Networks Socket Lab",
            subjectCode = "CS-306",
            subjectName = "Computer Networks",
            category = "Assignment",
            type = "ASSIGNMENT",
            dueDate = "Today",
            dueTime = "5:00 PM",
            priority = "URGENT",
            weightage = "15% Internal",
            isCompleted = false,
            dueTimestamp = now + (3 * hour)
        )

        val task2 = com.example.model.AcademicTask(
            id = "test-task-2",
            title = "Operating Systems Mid-Term Viva",
            subjectCode = "CS-304",
            subjectName = "Operating Systems",
            category = "Exam",
            type = "EXAM_PREP",
            dueDate = "Tomorrow",
            dueTime = "10:00 AM",
            priority = "HIGH",
            weightage = "30% Midterm",
            isCompleted = false,
            dueTimestamp = now + (24 * hour)
        )

        val task3 = com.example.model.AcademicTask(
            id = "test-task-3",
            title = "DBMS ER Diagram Design",
            subjectCode = "CS-302",
            subjectName = "Database Management Systems",
            category = "Assignment",
            type = "ASSIGNMENT",
            dueDate = "Oct 30",
            dueTime = "11:59 PM",
            priority = "MEDIUM",
            weightage = "10% Internal",
            isCompleted = true,
            dueTimestamp = now + (3 * day)
        )

        val tasks = listOf(task1, task2, task3)

        // 1. Pending tasks from Room DB (isCompleted == false)
        val pending = tasks.filter { !it.isCompleted }
        assertEquals(2, pending.size)

        // 2. Earliest impending task
        val nearest = pending.minByOrNull { it.dueTimestamp }
        assertNotNull(nearest)
        assertEquals("test-task-1", nearest!!.id)
        assertEquals("CS-306", nearest.subjectCode)
        assertEquals("URGENT", nearest.priority)

        // 3. Urgent filter
        val urgent = tasks.filter { !it.isCompleted && (it.priority == "URGENT" || it.dueDate.contains("Today")) }
        assertEquals(1, urgent.size)
        assertEquals("test-task-1", urgent.first().id)

        // 4. Exam category filter
        val exams = tasks.filter { it.category == "Exam" }
        assertEquals(1, exams.size)
        assertEquals("CS-304", exams.first().subjectCode)

        // 5. Toggle completion
        val completedTask1 = task1.copy(isCompleted = true)
        assertTrue(completedTask1.isCompleted)
        val remainingPending = listOf(completedTask1, task2, task3).filter { !it.isCompleted }
        assertEquals(1, remainingPending.size)
        assertEquals("test-task-2", remainingPending.first().id)
    }

    @Test
    fun testDatePickerFormattingAndParsing() {
        val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            set(2026, java.util.Calendar.NOVEMBER, 15, 0, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val targetMillis = calendar.timeInMillis

        // Format epoch millis to string
        val formatted = com.example.ui.components.TaskDateUtils.formatEpochMillisToDate(targetMillis)
        assertEquals("Nov 15, 2026", formatted)

        // Parse formatted string back to epoch millis
        val parsedMillis = com.example.ui.components.TaskDateUtils.parseDateStringToEpochMillis(formatted)
        assertEquals(targetMillis, parsedMillis)

        // Parse relative keywords
        val todayMillis = com.example.ui.components.TaskDateUtils.parseDateStringToEpochMillis("Today")
        val tomorrowMillis = com.example.ui.components.TaskDateUtils.parseDateStringToEpochMillis("Tomorrow")
        assertTrue(tomorrowMillis > todayMillis)
    }

    @Test
    fun testTasksSortedBySelectedDateTimestampInRoom() {
        val nov10 = com.example.ui.components.TaskDateUtils.parseDateStringToEpochMillis("Nov 10, 2026")
        val nov20 = com.example.ui.components.TaskDateUtils.parseDateStringToEpochMillis("Nov 20, 2026")
        val dec01 = com.example.ui.components.TaskDateUtils.parseDateStringToEpochMillis("Dec 01, 2026")

        val taskEarly = com.example.data.local.TaskEntity(
            id = "task-early",
            title = "Networks Lab",
            subjectCode = "CS-306",
            subjectName = "Computer Networks",
            dueDate = "Nov 10, 2026",
            dueTimestamp = nov10
        )
        val taskMid = com.example.data.local.TaskEntity(
            id = "task-mid",
            title = "DBMS Milestone",
            subjectCode = "CS-302",
            subjectName = "DBMS",
            dueDate = "Nov 20, 2026",
            dueTimestamp = nov20
        )
        val taskLate = com.example.data.local.TaskEntity(
            id = "task-late",
            title = "Final Project Submission",
            subjectCode = "CS-308",
            subjectName = "Software Engineering",
            dueDate = "Dec 01, 2026",
            dueTimestamp = dec01
        )

        val unsortedList = listOf(taskLate, taskEarly, taskMid)
        val sortedList = unsortedList.sortedBy { it.dueTimestamp }

        assertEquals("task-early", sortedList[0].id)
        assertEquals("task-mid", sortedList[1].id)
        assertEquals("task-late", sortedList[2].id)

        // Verify rescheduling updates dueDate and timestamp
        val rescheduledEarly = taskEarly.copy(
            dueDate = "Dec 05, 2026",
            dueTimestamp = com.example.ui.components.TaskDateUtils.parseDateStringToEpochMillis("Dec 05, 2026")
        )
        val reSorted = listOf(taskLate, rescheduledEarly, taskMid).sortedBy { it.dueTimestamp }
        assertEquals("task-mid", reSorted[0].id)
        assertEquals("task-late", reSorted[1].id)
        assertEquals("task-early", reSorted[2].id)
    }

    @Test
    fun testAttendanceManagementAggregateAndProjectionCalculations() {
        val s1 = Subject("CS-302", "DBMS", 4, "Dr. Roy", attendedClasses = 26, totalClasses = 30) // 86.67%
        val s2 = Subject("CS-304", "OS", 4, "Prof. Muralidhar", attendedClasses = 23, totalClasses = 31) // 74.19%
        val s3 = Subject("CS-306", "CN", 4, "Prof. Venkat", attendedClasses = 21, totalClasses = 26) // 80.77%
        val subjects = listOf(s1, s2, s3)

        // 1. Overall Aggregate Percentage Calculation
        val totalAttended = subjects.sumOf { it.attendedClasses } // 26 + 23 + 21 = 70
        val totalClasses = subjects.sumOf { it.totalClasses } // 30 + 31 + 26 = 87
        val aggregatePct = (totalAttended.toDouble() / totalClasses) * 100.0 // 70 / 87 = 80.46%
        assertEquals(70, totalAttended)
        assertEquals(87, totalClasses)
        assertEquals(80.46, aggregatePct, 0.05)

        // 2. Safe vs At-Risk Count
        val safeCount = subjects.count { it.attendancePercentage >= 75.0 }
        val atRiskCount = subjects.count { it.attendancePercentage < 75.0 }
        assertEquals(2, safeCount)
        assertEquals(1, atRiskCount)

        // 3. What-If Simulator: Student with 74.19% attends next 3 classes
        val projectedOS = s2.calculateProjected(addAttended = 3, addMissed = 0)
        assertEquals(76.47, projectedOS, 0.05)
        assertTrue(projectedOS >= 75.0)

        // 4. What-If Simulator: Student with 86.67% misses next 2 classes
        val projectedDBMS = s1.calculateProjected(addAttended = 0, addMissed = 2)
        assertEquals(81.25, projectedDBMS, 0.05)
        assertTrue(projectedDBMS >= 75.0)
    }
}





