package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Project Focus Sessions.
 * Enables reactive observation of logged study sessions, total time spent per project,
 * and session insertions.
 */
@Dao
interface ProjectFocusSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ProjectFocusSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<ProjectFocusSessionEntity>)

    @Query("SELECT * FROM project_focus_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<ProjectFocusSessionEntity>>

    @Query("SELECT * FROM project_focus_sessions WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getSessionsForProject(projectId: String): Flow<List<ProjectFocusSessionEntity>>

    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM project_focus_sessions WHERE projectId = :projectId")
    fun getTotalMinutesForProject(projectId: String): Flow<Int>

    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM project_focus_sessions")
    fun getTotalMinutesOverall(): Flow<Int>

    @Query(
        """
        SELECT 
            projectId, 
            projectTitle, 
            COALESCE(SUM(durationMinutes), 0) as totalMinutes, 
            COUNT(*) as sessionCount 
        FROM project_focus_sessions 
        GROUP BY projectId, projectTitle
        ORDER BY totalMinutes DESC
        """
    )
    fun getProjectTimeSummary(): Flow<List<ProjectTimeSpentSummary>>

    @Query("DELETE FROM project_focus_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: String)

    @Query("SELECT COUNT(*) FROM project_focus_sessions")
    suspend fun getSessionCount(): Int
}
