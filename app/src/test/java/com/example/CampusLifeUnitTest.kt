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
        assertEquals(1, subject.neededToReach75)
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
}
