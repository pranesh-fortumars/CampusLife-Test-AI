package com.example.data.repository

import com.example.data.local.AcademicCalendarDao
import com.example.data.local.AcademicCalendarEventEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

/**
 * Repository layer managing Academic Calendar events and deadlines in the Room database.
 */
class AcademicCalendarRepository(private val dao: AcademicCalendarDao) {

    val allEvents: Flow<List<AcademicCalendarEventEntity>> = dao.getAllEvents()

    fun getEventsForSemester(semesterNumber: Int): Flow<List<AcademicCalendarEventEntity>> =
        dao.getEventsForSemester(semesterNumber)

    fun getEventsByType(eventType: String): Flow<List<AcademicCalendarEventEntity>> =
        dao.getEventsByType(eventType)

    fun getUpcomingDeadlines(): Flow<List<AcademicCalendarEventEntity>> =
        dao.getUpcomingDeadlines(System.currentTimeMillis())

    suspend fun insertEvent(event: AcademicCalendarEventEntity) = dao.insertEvent(event)

    suspend fun insertEvents(events: List<AcademicCalendarEventEntity>) = dao.insertEvents(events)

    suspend fun updateEvent(event: AcademicCalendarEventEntity) = dao.updateEvent(event)

    suspend fun toggleCompleted(id: String, isCompleted: Boolean) = dao.setEventCompleted(id, isCompleted)

    suspend fun updateEventDate(id: String, newDate: String, newTimestamp: Long) =
        dao.updateEventDate(id, newDate, newTimestamp)

    suspend fun deleteEventById(id: String) = dao.deleteEventById(id)

    /**
     * Seeds realistic semester academic calendar deadlines and exams if database table is empty.
     */
    suspend fun seedCalendarIfEmpty() {
        val count = dao.getEventCount()
        if (count == 0) {
            val baseTime = System.currentTimeMillis()
            val dayMillis = 24 * 60 * 60 * 1000L

            val initialEvents = listOf(
                AcademicCalendarEventEntity(
                    id = "cal-midsem-os",
                    title = "Mid-Term Examination: Operating Systems",
                    courseCode = "CS-304",
                    courseName = "Operating Systems",
                    eventType = "EXAM",
                    date = "Nov 04, 2026",
                    time = "10:00 AM - 12:00 PM",
                    timestamp = baseTime + (6 * dayMillis),
                    locationOrRoom = "LH-302 (North Wing)",
                    weightage = "25% Mid-Term",
                    description = "Units 1-3: Process scheduling, IPC, Deadlocks, Semaphore synchronization algorithms.",
                    isCompleted = false
                ),
                AcademicCalendarEventEntity(
                    id = "cal-midsem-dbms",
                    title = "Mid-Term Examination: Database Systems",
                    courseCode = "CS-302",
                    courseName = "Database Management Systems",
                    eventType = "EXAM",
                    date = "Nov 06, 2026",
                    time = "02:00 PM - 04:00 PM",
                    timestamp = baseTime + (8 * dayMillis),
                    locationOrRoom = "LH-302",
                    weightage = "25% Mid-Term",
                    description = "Relational algebra, BCNF normalization, Transaction isolation levels, Indexing.",
                    isCompleted = false
                ),
                AcademicCalendarEventEntity(
                    id = "cal-midsem-cn",
                    title = "Mid-Term Examination: Computer Networks",
                    courseCode = "CS-306",
                    courseName = "Computer Networks",
                    eventType = "EXAM",
                    date = "Nov 08, 2026",
                    time = "10:00 AM - 12:00 PM",
                    timestamp = baseTime + (10 * dayMillis),
                    locationOrRoom = "LH-201",
                    weightage = "30% Mid-Term",
                    description = "TCP/IP sliding window flow control, Congestion control, Subnetting & CIDR routing.",
                    isCompleted = false
                ),
                AcademicCalendarEventEntity(
                    id = "cal-asgn-dbms",
                    title = "Assignment 2: Query Optimization & Indexing",
                    courseCode = "CS-302",
                    courseName = "Database Management Systems",
                    eventType = "ASSIGNMENT",
                    date = "Nov 12, 2026",
                    time = "11:59 PM",
                    timestamp = baseTime + (14 * dayMillis),
                    locationOrRoom = "Google Classroom Portal",
                    weightage = "10% Internal",
                    description = "Analyze EXPLAIN queries on 1M tuples and tune PostgreSQL secondary indexes.",
                    isCompleted = false
                ),
                AcademicCalendarEventEntity(
                    id = "cal-proj-capstone",
                    title = "Capstone Project Milestone 2: Backend Architecture",
                    courseCode = "CS-308",
                    courseName = "Software Engineering",
                    eventType = "PROJECT",
                    date = "Nov 18, 2026",
                    time = "05:00 PM",
                    timestamp = baseTime + (20 * dayMillis),
                    locationOrRoom = "CS Department Lab 2",
                    weightage = "20% Course Project",
                    description = "Demonstrate working REST APIs, database schema migration, and Swagger docs to faculty committee.",
                    isCompleted = false
                ),
                AcademicCalendarEventEntity(
                    id = "cal-lab-cn",
                    title = "Lab Practical & Viva: Socket Programming",
                    courseCode = "CS-306",
                    courseName = "Computer Networks",
                    eventType = "LAB",
                    date = "Nov 22, 2026",
                    time = "09:00 AM - 12:00 PM",
                    timestamp = baseTime + (24 * dayMillis),
                    locationOrRoom = "Networks Lab (Room 214)",
                    weightage = "20% Lab Exam",
                    description = "Implement multi-client chat server with select/epoll in C or Java.",
                    isCompleted = false
                ),
                AcademicCalendarEventEntity(
                    id = "cal-break-midsem",
                    title = "Academic Holiday: Mid-Semester Break",
                    courseCode = "CAMPUS",
                    courseName = "University Holiday",
                    eventType = "HOLIDAY",
                    date = "Nov 25 - Nov 27, 2026",
                    time = "All Day",
                    timestamp = baseTime + (27 * dayMillis),
                    locationOrRoom = "Campus Wide",
                    weightage = "Statutory Break",
                    description = "University declared festival and study recess; no scheduled lectures.",
                    isCompleted = false
                ),
                AcademicCalendarEventEntity(
                    id = "cal-final-dbms",
                    title = "End-Term Final Theory Exam: DBMS",
                    courseCode = "CS-302",
                    courseName = "Database Management Systems",
                    eventType = "EXAM",
                    date = "Dec 04, 2026",
                    time = "09:30 AM - 12:30 PM",
                    timestamp = baseTime + (36 * dayMillis),
                    locationOrRoom = "Examination Hall A",
                    weightage = "40% Final Exam",
                    description = "Comprehensive syllabus covering SQL, Distributed DBs, NoSQL, and Query processing.",
                    isCompleted = false
                ),
                AcademicCalendarEventEntity(
                    id = "cal-final-os",
                    title = "End-Term Final Theory Exam: Operating Systems",
                    courseCode = "CS-304",
                    courseName = "Operating Systems",
                    eventType = "EXAM",
                    date = "Dec 08, 2026",
                    time = "09:30 AM - 12:30 PM",
                    timestamp = baseTime + (40 * dayMillis),
                    locationOrRoom = "Examination Hall B",
                    weightage = "40% Final Exam",
                    description = "Comprehensive syllabus covering Virtual memory, File systems, Kernel I/O, Distributed OS.",
                    isCompleted = false
                )
            )

            dao.insertEvents(initialEvents)
        }
    }
}
