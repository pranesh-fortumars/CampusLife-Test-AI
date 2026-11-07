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
}
