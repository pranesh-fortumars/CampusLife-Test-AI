package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Career Events and Internship Deadlines.
 * Supports reactive queries, reminder toggles, and status updates.
 */
@Dao
interface CareerEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CareerEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<CareerEventEntity>)

    @Query("SELECT * FROM career_events ORDER BY dueTimestamp ASC")
    fun getAllEvents(): Flow<List<CareerEventEntity>>

    @Query("SELECT * FROM career_events WHERE status != 'CLOSED' ORDER BY dueTimestamp ASC")
    fun getUpcomingEvents(): Flow<List<CareerEventEntity>>

    @Query("SELECT * FROM career_events WHERE eventType = :type ORDER BY dueTimestamp ASC")
    fun getEventsByType(type: String): Flow<List<CareerEventEntity>>

    @Query("SELECT * FROM career_events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: String): CareerEventEntity?

    @Query("UPDATE career_events SET reminderEnabled = :enabled WHERE id = :id")
    suspend fun toggleReminder(id: String, enabled: Boolean)

    @Query("UPDATE career_events SET notificationTriggered = :triggered WHERE id = :id")
    suspend fun setNotificationTriggered(id: String, triggered: Boolean)

    @Query("UPDATE career_events SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM career_events WHERE id = :id")
    suspend fun deleteEventById(id: String)

    @Query("SELECT COUNT(*) FROM career_events")
    suspend fun getEventCount(): Int
}
