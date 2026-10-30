package com.example.data.repository

import com.example.data.local.CourseScheduleDao
import com.example.data.local.CourseScheduleEntity
import com.example.model.DayOfWeekEnum
import com.example.model.ScheduleMockData
import com.example.model.ScheduleSlot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository layer abstracting Room local persistence for course schedules.
 * Provides reactive flows and seeds default class schedule data on first run.
 */
class CourseScheduleRepository(private val dao: CourseScheduleDao) {

    val allSchedules: Flow<List<ScheduleSlot>> = dao.getAllSchedules().map { list ->
        list.map { it.toDomain() }
    }

    fun getSchedulesForDay(day: DayOfWeekEnum): Flow<List<ScheduleSlot>> {
        return dao.getSchedulesForDay(day.shortCode).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun seedInitialSchedulesIfEmpty() {
        val count = dao.getScheduleCount()
        if (count == 0) {
            val entities = ScheduleMockData.defaultScheduleSlots.map { slot ->
                CourseScheduleEntity.fromDomain(slot)
            }
            dao.insertSchedules(entities)
        }
    }

    suspend fun insertSchedule(slot: ScheduleSlot) {
        dao.insertSchedule(CourseScheduleEntity.fromDomain(slot))
    }

    suspend fun updateSchedule(slot: ScheduleSlot) {
        dao.updateSchedule(CourseScheduleEntity.fromDomain(slot))
    }

    suspend fun updateReminder(id: String, enabled: Boolean, minutesBefore: Int) {
        dao.updateReminder(id, enabled, minutesBefore)
    }

    suspend fun updateAttendedToday(id: String, attended: Boolean) {
        dao.updateAttendedToday(id, attended)
    }

    suspend fun deleteSchedule(id: String) {
        dao.deleteScheduleById(id)
    }
}
