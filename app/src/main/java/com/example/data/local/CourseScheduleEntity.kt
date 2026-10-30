package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.DayOfWeekEnum
import com.example.model.ScheduleSlot
import com.example.model.ScheduleSlotType
import java.util.UUID

/**
 * Room database entity representing a student's daily and weekly recurring class schedule.
 * Persists course code, course title, session type, recurring days, room locations,
 * start/end times, instructor, and reminder preferences.
 */
@Entity(tableName = "course_schedules")
data class CourseScheduleEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val courseCode: String,
    val courseName: String,
    val slotType: String = "LECTURE", // LECTURE, LAB, TUTORIAL, SEMINAR
    val daysOfWeek: String,           // Comma-separated short codes: e.g. "MON,WED,FRI"
    val timeStart: String,            // e.g. "09:00 AM"
    val timeEnd: String,              // e.g. "10:15 AM"
    val room: String,                 // e.g. "LH-102 (Block A)"
    val faculty: String,              // e.g. "Dr. Ananya Roy"
    val batchOrGroup: String = "All Batches",
    val notes: String = "",
    val reminderEnabled: Boolean = true,
    val reminderMinutesBefore: Int = 15,
    val isAttendedToday: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): ScheduleSlot {
        val daysList = daysOfWeek.split(",")
            .mapNotNull { code ->
                DayOfWeekEnum.values().firstOrNull { it.shortCode.equals(code.trim(), ignoreCase = true) }
            }
            .ifEmpty { listOf(DayOfWeekEnum.MON) }

        val type = try {
            ScheduleSlotType.valueOf(slotType.uppercase())
        } catch (e: Exception) {
            ScheduleSlotType.LECTURE
        }

        return ScheduleSlot(
            id = id,
            courseCode = courseCode,
            courseName = courseName,
            slotType = type,
            recurringDays = daysList,
            timeStart = timeStart,
            timeEnd = timeEnd,
            room = room,
            faculty = faculty,
            batchOrGroup = batchOrGroup,
            notes = notes,
            reminderEnabled = reminderEnabled,
            reminderMinutesBefore = reminderMinutesBefore,
            isAttendedToday = isAttendedToday
        )
    }

    companion object {
        fun fromDomain(slot: ScheduleSlot): CourseScheduleEntity {
            return CourseScheduleEntity(
                id = slot.id,
                courseCode = slot.courseCode,
                courseName = slot.courseName,
                slotType = slot.slotType.name,
                daysOfWeek = slot.recurringDays.joinToString(",") { it.shortCode },
                timeStart = slot.timeStart,
                timeEnd = slot.timeEnd,
                room = slot.room,
                faculty = slot.faculty,
                batchOrGroup = slot.batchOrGroup,
                notes = slot.notes,
                reminderEnabled = slot.reminderEnabled,
                reminderMinutesBefore = slot.reminderMinutesBefore,
                isAttendedToday = slot.isAttendedToday
            )
        }
    }
}
