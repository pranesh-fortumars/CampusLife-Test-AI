package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dueTimestamp ASC, createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueTimestamp ASC")
    fun getPendingTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueTimestamp ASC")
    fun getUpcomingDueTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE category IN ('Assignment', 'Exam') ORDER BY isCompleted ASC, dueTimestamp ASC")
    fun getAssignmentsAndExams(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE subjectCode = :subjectCode ORDER BY dueTimestamp ASC")
    fun getTasksForSubject(subjectCode: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY isCompleted ASC, dueTimestamp ASC")
    fun getTasksByCategory(category: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun setTaskCompleted(id: String, isCompleted: Boolean)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTaskCount(): Int
}
