package com.example.data.repository

import com.example.data.local.AcademicProjectDao
import com.example.data.local.ProjectFocusSessionDao
import com.example.data.local.ProjectFocusSessionEntity
import com.example.data.local.ProjectTimeSpentSummary
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repository for Project Focus Sessions and study time tracking.
 * Coordinates logging study sprints, aggregating time per project, and syncing with Room DB.
 */
class ProjectFocusSessionRepository(
    private val sessionDao: ProjectFocusSessionDao,
    private val projectDao: AcademicProjectDao
) {

    val allSessions: Flow<List<ProjectFocusSessionEntity>> = sessionDao.getAllSessions()
    val projectTimeSummary: Flow<List<ProjectTimeSpentSummary>> = sessionDao.getProjectTimeSummary()
    val totalMinutesOverall: Flow<Int> = sessionDao.getTotalMinutesOverall()

    fun getSessionsForProject(projectId: String): Flow<List<ProjectFocusSessionEntity>> {
        return sessionDao.getSessionsForProject(projectId)
    }

    fun getTotalMinutesForProject(projectId: String): Flow<Int> {
        return sessionDao.getTotalMinutesForProject(projectId)
    }

    suspend fun logProjectFocusSession(
        projectId: String,
        projectTitle: String,
        subjectCode: String = "",
        durationMinutes: Int,
        notes: String = "",
        sessionType: String = "FOCUS"
    ): ProjectFocusSessionEntity {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val todayStr = sdf.format(Date())

        val session = ProjectFocusSessionEntity(
            projectId = projectId,
            projectTitle = projectTitle,
            subjectCode = subjectCode,
            durationMinutes = durationMinutes,
            durationSeconds = (durationMinutes * 60).toLong(),
            timestamp = System.currentTimeMillis(),
            sessionDate = todayStr,
            sessionNotes = notes,
            sessionType = sessionType
        )

        // 1. Persist the session in Room
        sessionDao.insertSession(session)

        // 2. Increment total focus minutes on the project record
        projectDao.addFocusTimeToProject(projectId, durationMinutes)

        return session
    }

    suspend fun deleteSession(id: String) {
        sessionDao.deleteSessionById(id)
    }

    suspend fun seedInitialSessionsIfEmpty() {
        val count = sessionDao.getSessionCount()
        if (count == 0) {
            val now = System.currentTimeMillis()
            val day = 86400000L

            val initialSessions = listOf(
                ProjectFocusSessionEntity(
                    id = "session-1",
                    projectId = "proj-1",
                    projectTitle = "Distributed In-Memory Cache with Raft Consensus",
                    subjectCode = "CS-302",
                    durationMinutes = 50,
                    durationSeconds = 3000L,
                    timestamp = now - (2 * day) - 3600000L,
                    sessionDate = "Oct 29, 2026",
                    sessionNotes = "Implemented Raft heartbeat timers and election term transitions",
                    sessionType = "DEEP_WORK"
                ),
                ProjectFocusSessionEntity(
                    id = "session-2",
                    projectId = "proj-1",
                    projectTitle = "Distributed In-Memory Cache with Raft Consensus",
                    subjectCode = "CS-302",
                    durationMinutes = 25,
                    durationSeconds = 1500L,
                    timestamp = now - (1 * day) - 7200000L,
                    sessionDate = "Oct 30, 2026",
                    sessionNotes = "Refactored log compaction and snapshotting logic",
                    sessionType = "FOCUS"
                ),
                ProjectFocusSessionEntity(
                    id = "session-3",
                    projectId = "proj-2",
                    projectTitle = "Process Scheduling & IPC Semaphore Visualizer",
                    subjectCode = "CS-304",
                    durationMinutes = 45,
                    durationSeconds = 2700L,
                    timestamp = now - (1 * day) - 18000000L,
                    sessionDate = "Oct 30, 2026",
                    sessionNotes = "Built Round Robin quantum Gantt chart animator",
                    sessionType = "DEEP_WORK"
                ),
                ProjectFocusSessionEntity(
                    id = "session-4",
                    projectId = "proj-3",
                    projectTitle = "Real-Time Campus Life Hub & Peer Network",
                    subjectCode = "CS-308",
                    durationMinutes = 60,
                    durationSeconds = 3600L,
                    timestamp = now - 3600000L,
                    sessionDate = "Oct 31, 2026",
                    sessionNotes = "Integrated Room SQLite database with Pomodoro focus logging",
                    sessionType = "DEEP_WORK"
                )
            )

            sessionDao.insertSessions(initialSessions)

            // Sync project totalFocusMinutes
            projectDao.addFocusTimeToProject("proj-1", 75)
            projectDao.addFocusTimeToProject("proj-2", 45)
            projectDao.addFocusTimeToProject("proj-3", 60)
        }
    }
}
