package com.example.data.repository

import com.example.data.local.CareerEventDao
import com.example.data.local.CareerEventEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for Career Events and Internship Deadlines.
 * Intermediates between Room SQLite database and ViewModels.
 */
class CareerEventRepository(private val dao: CareerEventDao) {

    val allEvents: Flow<List<CareerEventEntity>> = dao.getAllEvents()
    val upcomingEvents: Flow<List<CareerEventEntity>> = dao.getUpcomingEvents()

    fun getEventsByType(type: String): Flow<List<CareerEventEntity>> {
        return dao.getEventsByType(type)
    }

    suspend fun insertEvent(event: CareerEventEntity) {
        dao.insertEvent(event)
    }

    suspend fun toggleReminder(id: String, enabled: Boolean) {
        dao.toggleReminder(id, enabled)
    }

    suspend fun setNotificationTriggered(id: String, triggered: Boolean) {
        dao.setNotificationTriggered(id, triggered)
    }

    suspend fun updateStatus(id: String, status: String) {
        dao.updateStatus(id, status)
    }

    suspend fun deleteEvent(id: String) {
        dao.deleteEventById(id)
    }

    suspend fun getEventById(id: String): CareerEventEntity? {
        return dao.getEventById(id)
    }

    suspend fun seedInitialCareerEventsIfEmpty() {
        val count = dao.getEventCount()
        if (count == 0) {
            val now = System.currentTimeMillis()
            val day = 86400000L

            val initialEvents = listOf(
                CareerEventEntity(
                    id = "career-1",
                    title = "Google Summer of Code & SWE Intern 2027",
                    company = "Google",
                    role = "Software Engineering Intern",
                    eventType = "INTERNSHIP_DEADLINE",
                    deadlineDate = "Nov 08, 2026",
                    dueTimestamp = now + (8 * day),
                    locationOrUrl = "careers.google.com/students",
                    reminderEnabled = true,
                    stipendOrPackage = "₹1.40L / month",
                    description = "Summer 2027 SWE internship opportunity for 2nd and 3rd year students. DSA and System Design foundation.",
                    eligibility = "B.Tech/Dual Degree CSE/IT/ECE (CGPA >= 8.0)"
                ),
                CareerEventEntity(
                    id = "career-2",
                    title = "Microsoft Campus Pre-Placement Talk & Q&A",
                    company = "Microsoft",
                    role = "SWE & Cloud Solution Architect",
                    eventType = "CAREER_TALK",
                    deadlineDate = "Nov 04, 2026",
                    dueTimestamp = now + (4 * day),
                    locationOrUrl = "Campus Convocation Hall / MS Teams",
                    reminderEnabled = true,
                    stipendOrPackage = "₹1.25L / mo • 44 LPA Full-Time",
                    description = "Technical leadership talk by Azure engineering director followed by resume shortlisting.",
                    eligibility = "Open to all pre-final & final year engineering batches"
                ),
                CareerEventEntity(
                    id = "career-3",
                    title = "Goldman Sachs Global Quantitative & Engineering Drive",
                    company = "Goldman Sachs",
                    role = "Quantitative Analyst & Software Engineer",
                    eventType = "INTERNSHIP_DEADLINE",
                    deadlineDate = "Nov 14, 2026",
                    dueTimestamp = now + (14 * day),
                    locationOrUrl = "goldmansachs.com/careers",
                    reminderEnabled = true,
                    stipendOrPackage = "₹1.50L / month",
                    description = "Algorithmic trading platforms, low-latency microservices, and distributed risk analytics.",
                    eligibility = "B.Tech/M.Tech with strong Math and Data Structures background"
                ),
                CareerEventEntity(
                    id = "career-4",
                    title = "Amazon AWS SDE Online Assessment Round",
                    company = "Amazon",
                    role = "Software Development Engineer (Intern)",
                    eventType = "ONLINE_ASSESSMENT",
                    deadlineDate = "Nov 02, 2026",
                    dueTimestamp = now + (2 * day),
                    locationOrUrl = "HackerRank Proctored Platform",
                    reminderEnabled = true,
                    stipendOrPackage = "₹1.10L / month",
                    description = "90-minute coding assessment with 2 algorithmic problems and leadership principles work-simulation.",
                    eligibility = "Pre-screened applicants only"
                ),
                CareerEventEntity(
                    id = "career-5",
                    title = "Atlassian Autumn Hackathon & Fast-Track Hiring",
                    company = "Atlassian",
                    role = "Full-Stack Developer Intern",
                    eventType = "PLACEMENT_DRIVE",
                    deadlineDate = "Nov 22, 2026",
                    dueTimestamp = now + (22 * day),
                    locationOrUrl = "atlassian.com/hackhouse",
                    reminderEnabled = false,
                    stipendOrPackage = "₹1.20L / month",
                    description = "Collaborative 36-hour hackathon to build integrations for Jira and Confluence cloud.",
                    eligibility = "All B.Tech batches welcome"
                )
            )

            dao.insertEvents(initialEvents)
        }
    }
}
