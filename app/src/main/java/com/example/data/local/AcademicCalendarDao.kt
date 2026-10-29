package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Academic Calendar events and deadlines in Room.
 */
@Dao
interface AcademicCalendarDao {

    @Query("SELECT * FROM academic_calendar_events ORDER BY timestamp ASC")
    fun getAllEvents(): Flow<List<AcademicCalendarEventEntity>>

    @Query("SELECT * FROM academic_calendar_events WHERE semesterNumber = :semesterNumber ORDER BY timestamp ASC")
    fun getEventsForSemester(semesterNumber: Int): Flow<List<AcademicCalendarEventEntity>>

    @Query("SELECT * FROM academic_calendar_events WHERE eventType = :eventType ORDER BY timestamp ASC")
    fun getEventsByType(eventType: String): Flow<List<AcademicCalendarEventEntity>>

    @Query("SELECT * FROM academic_calendar_events WHERE timestamp >= :nowTimestamp AND isCompleted = 0 ORDER BY timestamp ASC")
    fun getUpcomingDeadlines(nowTimestamp: Long): Flow<List<AcademicCalendarEventEntity>>

    @Query("SELECT * FROM academic_calendar_events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: String): AcademicCalendarEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: AcademicCalendarEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<AcademicCalendarEventEntity>)

    @Update
    suspend fun updateEvent(event: AcademicCalendarEventEntity)

    @Query("UPDATE academic_calendar_events SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun setEventCompleted(id: String, isCompleted: Boolean)

    @Query("UPDATE academic_calendar_events SET date = :newDate, timestamp = :newTimestamp WHERE id = :id")
    suspend fun updateEventDate(id: String, newDate: String, newTimestamp: Long)

    @Query("DELETE FROM academic_calendar_events WHERE id = :id")
    suspend fun deleteEventById(id: String)

    @Query("DELETE FROM academic_calendar_events WHERE semesterNumber = :semesterNumber")
    suspend fun clearEventsForSemester(semesterNumber: Int)

    @Query("SELECT COUNT(*) FROM academic_calendar_events")
    suspend fun getEventCount(): Int
}
