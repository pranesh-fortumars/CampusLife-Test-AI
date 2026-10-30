package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Room database course schedules table.
 * Supports reactive queries for daily classes, inserts, updates, deletes, and reminder preferences.
 */
@Dao
interface CourseScheduleDao {

    @Query("SELECT * FROM course_schedules ORDER BY createdAt ASC")
    fun getAllSchedules(): Flow<List<CourseScheduleEntity>>

    @Query("SELECT * FROM course_schedules WHERE daysOfWeek LIKE '%' || :dayShortCode || '%' ORDER BY timeStart ASC")
    fun getSchedulesForDay(dayShortCode: String): Flow<List<CourseScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: CourseScheduleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<CourseScheduleEntity>)

    @Update
    suspend fun updateSchedule(schedule: CourseScheduleEntity)

    @Query("UPDATE course_schedules SET reminderEnabled = :enabled, reminderMinutesBefore = :minutes WHERE id = :id")
    suspend fun updateReminder(id: String, enabled: Boolean, minutes: Int)

    @Query("UPDATE course_schedules SET isAttendedToday = :attended WHERE id = :id")
    suspend fun updateAttendedToday(id: String, attended: Boolean)

    @Query("DELETE FROM course_schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: String)

    @Query("SELECT COUNT(*) FROM course_schedules")
    suspend fun getScheduleCount(): Int
}
