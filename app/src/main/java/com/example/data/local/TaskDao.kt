package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Room tasks entity.
 * Provides queries and updates for task completion status, upcoming deadlines, and CRUD.
 */
@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dueTimestamp ASC, createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueTimestamp ASC")
    fun getPendingTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY dueTimestamp DESC")
    fun getCompletedTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueTimestamp ASC")
    fun getUpcomingDueTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE category IN ('Assignment', 'Exam') ORDER BY isCompleted ASC, dueTimestamp ASC")
    fun getAssignmentsAndExams(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE subjectCode = :subjectCode ORDER BY isCompleted ASC, dueTimestamp ASC")
    fun getTasksForSubject(subjectCode: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY isCompleted ASC, dueTimestamp ASC")
    fun getTasksByCategory(category: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun setTaskCompleted(id: String, isCompleted: Boolean)

    @Transaction
    suspend fun toggleTaskCompletion(id: String): Boolean {
        val task = getTaskById(id) ?: return false
        val newStatus = !task.isCompleted
        setTaskCompleted(id, newStatus)
        return newStatus
    }

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTaskCount(): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1")
    suspend fun getCompletedTaskCount(): Int
}
