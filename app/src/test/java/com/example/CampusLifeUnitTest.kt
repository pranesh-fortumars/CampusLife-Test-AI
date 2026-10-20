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
}

