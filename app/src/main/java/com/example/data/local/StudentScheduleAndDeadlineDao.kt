package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object for student class schedules and academic deadlines.
 */
@Dao
interface StudentScheduleAndDeadlineDao {

    @Query("SELECT * FROM student_schedules_and_deadlines ORDER BY dueTimestamp ASC")
    fun getAllItems(): Flow<List<StudentScheduleAndDeadlineEntity>>

    @Query("SELECT * FROM student_schedules_and_deadlines WHERE itemType = 'SCHEDULE' ORDER BY dayOfWeek, timeSlot ASC")
    fun getAllSchedules(): Flow<List<StudentScheduleAndDeadlineEntity>>

    @Query("SELECT * FROM student_schedules_and_deadlines WHERE itemType = 'DEADLINE' ORDER BY dueTimestamp ASC")
    fun getAllDeadlines(): Flow<List<StudentScheduleAndDeadlineEntity>>

    @Query("SELECT * FROM student_schedules_and_deadlines WHERE itemType = 'DEADLINE' AND isCompleted = 0 ORDER BY dueTimestamp ASC")
    fun getPendingDeadlines(): Flow<List<StudentScheduleAndDeadlineEntity>>

    @Query("SELECT * FROM student_schedules_and_deadlines WHERE itemType = 'SCHEDULE' AND dayOfWeek = :dayCode ORDER BY timeSlot ASC")
    fun getSchedulesForDay(dayCode: String): Flow<List<StudentScheduleAndDeadlineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: StudentScheduleAndDeadlineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<StudentScheduleAndDeadlineEntity>)

    @Update
    suspend fun updateItem(item: StudentScheduleAndDeadlineEntity)

    @Query("UPDATE student_schedules_and_deadlines SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompleted(id: String, completed: Boolean)

    @Query("UPDATE student_schedules_and_deadlines SET reminderEnabled = :enabled WHERE id = :id")
    suspend fun toggleReminder(id: String, enabled: Boolean)

    @Query("DELETE FROM student_schedules_and_deadlines WHERE id = :id")
    suspend fun deleteItemById(id: String)

    @Query("SELECT COUNT(*) FROM student_schedules_and_deadlines")
    suspend fun getItemCount(): Int
}
