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
        assertEquals("PROJECT", domain.type)
        assertEquals("URGENT", domain.priority)
        assertFalse(domain.isCompleted)
    }
}
