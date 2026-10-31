package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Academic Projects and Capstones.
 * Provides reactive Flow streams and suspend functions for CRUD operations.
 */
@Dao
interface AcademicProjectDao {
    @Query("SELECT * FROM academic_projects ORDER BY isCompleted ASC, dueTimestamp ASC, createdAt DESC")
    fun getAllProjects(): Flow<List<AcademicProjectEntity>>

    @Query("SELECT * FROM academic_projects WHERE isCompleted = 0 ORDER BY dueTimestamp ASC")
    fun getActiveProjects(): Flow<List<AcademicProjectEntity>>

    @Query("SELECT * FROM academic_projects WHERE isCompleted = 1 ORDER BY dueTimestamp DESC")
    fun getCompletedProjects(): Flow<List<AcademicProjectEntity>>

    @Query("SELECT * FROM academic_projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: String): AcademicProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: AcademicProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<AcademicProjectEntity>)

    @Update
    suspend fun updateProject(project: AcademicProjectEntity)

    @Query("DELETE FROM academic_projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)

    @Query("UPDATE academic_projects SET isCompleted = :isCompleted, status = :status WHERE id = :id")
    suspend fun setProjectCompletion(id: String, isCompleted: Boolean, status: String)

    @Query("UPDATE academic_projects SET status = :status, isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateProjectStatus(id: String, status: String, isCompleted: Boolean)

    @Query("UPDATE academic_projects SET deadlineDate = :deadlineDate, dueTimestamp = :dueTimestamp WHERE id = :id")
    suspend fun updateProjectDeadline(id: String, deadlineDate: String, dueTimestamp: Long)

    @Query("UPDATE academic_projects SET totalFocusMinutes = totalFocusMinutes + :additionalMinutes WHERE id = :id")
    suspend fun addFocusTimeToProject(id: String, additionalMinutes: Int)

    @Query("SELECT * FROM academic_projects WHERE status = :status ORDER BY dueTimestamp ASC")
    fun getProjectsByStatus(status: String): Flow<List<AcademicProjectEntity>>

    @Transaction
    suspend fun toggleProjectCompletion(id: String): Boolean {
        val proj = getProjectById(id) ?: return false
        val newStatus = !proj.isCompleted
        val code = if (newStatus) "DONE" else "IN_PROGRESS"
        setProjectCompletion(id, newStatus, code)
        return newStatus
    }

    @Query("SELECT COUNT(*) FROM academic_projects")
    suspend fun getProjectCount(): Int
}

