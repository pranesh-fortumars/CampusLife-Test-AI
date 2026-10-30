package com.example.model

import java.util.UUID

/**
 * Type of academic class session (Lectures vs Labs vs Tutorials).
 */
enum class ScheduleSlotType(val displayName: String, val shortName: String, val iconEmoji: String) {
    LECTURE("Lecture", "Lecture", "🎓"),
    LAB("Laboratory Practicum", "Lab", "🧪"),
    TUTORIAL("Tutorial Session", "Tutorial", "📝"),
    SEMINAR("Research Seminar", "Seminar", "💡")
}

/**
 * Days of the week for academic scheduling.
 */
enum class DayOfWeekEnum(val shortCode: String, val fullDayName: String, val dayNumber: Int) {
    MON("Mon", "Monday", 1),
    TUE("Tue", "Tuesday", 2),
    WED("Wed", "Wednesday", 3),
    THU("Thu", "Thursday", 4),
    FRI("Fri", "Friday", 5),
    SAT("Sat", "Saturday", 6)
}

/**
 * Model representing a recurring class schedule slot (Lecture or Lab)
 * with recurring weekly time slots across specified days of the week.
 */
data class ScheduleSlot(
    val id: String = UUID.randomUUID().toString(),
    val courseCode: String,
    val courseName: String,
    val slotType: ScheduleSlotType = ScheduleSlotType.LECTURE,
    val recurringDays: List<DayOfWeekEnum>, // Recurring days (e.g. MON, WED, FRI or TUE, THU)
    val timeStart: String,                 // e.g. "09:00 AM"
    val timeEnd: String,                   // e.g. "10:15 AM"
    val room: String,                      // e.g. "LH-102" or "CS Lab 304"
    val faculty: String,                   // e.g. "Dr. Ananya Roy"
    val batchOrGroup: String = "All Batches",
    val notes: String = "",
    val reminderEnabled: Boolean = true,
    val reminderMinutesBefore: Int = 15,
    val isLiveNow: Boolean = false,
    val isAttendedToday: Boolean = false
) {
    val durationText: String
        get() = "$timeStart – $timeEnd"

    val recurrenceSummary: String
        get() = when {
            recurringDays.size == 6 -> "Daily (Mon–Sat)"
            recurringDays == listOf(DayOfWeekEnum.MON, DayOfWeekEnum.WED, DayOfWeekEnum.FRI) -> "Recurring: Mon, Wed, Fri"
            recurringDays == listOf(DayOfWeekEnum.TUE, DayOfWeekEnum.THU) -> "Recurring: Tue, Thu"
            recurringDays.size == 1 -> "Weekly on ${recurringDays.first().fullDayName}"
            else -> "Recurring: " + recurringDays.joinToString(", ") { it.shortCode }
        }

    fun occursOn(day: DayOfWeekEnum): Boolean = recurringDays.contains(day)
}

/**
 * Bundled default schedule providing realistic university recurring lectures and labs.
 */
object ScheduleMockData {
    val defaultScheduleSlots: List<ScheduleSlot> = listOf(
        ScheduleSlot(
            id = "slot-cs301-lec",
            courseCode = "CS-301",
            courseName = "Distributed Systems & Cloud",
            slotType = ScheduleSlotType.LECTURE,
            recurringDays = listOf(DayOfWeekEnum.MON, DayOfWeekEnum.WED, DayOfWeekEnum.FRI),
            timeStart = "09:00 AM",
            timeEnd = "10:15 AM",
            room = "LH-101 (Block A)",
            faculty = "Prof. K. Muralidhar",
            notes = "Consensus Algorithms, Raft & MapReduce"
        ),
        ScheduleSlot(
            id = "slot-cs302-lec",
            courseCode = "CS-302",
            courseName = "Database Management Systems",
            slotType = ScheduleSlotType.LECTURE,
            recurringDays = listOf(DayOfWeekEnum.MON, DayOfWeekEnum.WED, DayOfWeekEnum.FRI),
            timeStart = "10:30 AM",
            timeEnd = "11:45 AM",
            room = "LH-102 (Block A)",
            faculty = "Dr. Ananya Roy",
            notes = "Query Optimization & B+ Tree Indexing"
        ),
        ScheduleSlot(
            id = "slot-cs304-lec",
            courseCode = "CS-304",
            courseName = "Operating Systems & Virtualization",
            slotType = ScheduleSlotType.LECTURE,
            recurringDays = listOf(DayOfWeekEnum.TUE, DayOfWeekEnum.THU),
            timeStart = "09:00 AM",
            timeEnd = "10:15 AM",
            room = "LH-201 (Block B)",
            faculty = "Dr. R. Venkat",
            notes = "Virtual Memory & Page Replacement"
        ),
        ScheduleSlot(
            id = "slot-cs306-lec",
            courseCode = "CS-306",
            courseName = "Computer Networks",
            slotType = ScheduleSlotType.LECTURE,
            recurringDays = listOf(DayOfWeekEnum.TUE, DayOfWeekEnum.THU),
            timeStart = "10:30 AM",
            timeEnd = "11:45 AM",
            room = "Room 214 (Academic Block)",
            faculty = "Prof. Neha Gupta",
            notes = "TCP Congestion Control & Socket Programming"
        ),
        ScheduleSlot(
            id = "slot-cs302-lab",
            courseCode = "CS-302",
            courseName = "DBMS Advanced Query Lab",
            slotType = ScheduleSlotType.LAB,
            recurringDays = listOf(DayOfWeekEnum.THU),
            timeStart = "02:00 PM",
            timeEnd = "04:30 PM",
            room = "CS Lab 304 (3rd Floor)",
            faculty = "Dr. Ananya Roy",
            batchOrGroup = "Batch B1 & B2",
            notes = "PostgreSQL Indexing & 3NF Normalization scripts",
            isLiveNow = true
        ),
        ScheduleSlot(
            id = "slot-cs304-lab",
            courseCode = "CS-304",
            courseName = "Linux Kernel & OS Lab Practicum",
            slotType = ScheduleSlotType.LAB,
            recurringDays = listOf(DayOfWeekEnum.WED),
            timeStart = "02:00 PM",
            timeEnd = "04:30 PM",
            room = "Systems Lab (West Wing)",
            faculty = "Dr. R. Venkat",
            batchOrGroup = "Batch B1",
            notes = "Fork syscall, Pipes & Shared Memory IPC"
        ),
        ScheduleSlot(
            id = "slot-cs308-lab",
            courseCode = "CS-308",
            courseName = "SE Agile & DevOps Practicum Lab",
            slotType = ScheduleSlotType.LAB,
            recurringDays = listOf(DayOfWeekEnum.FRI),
            timeStart = "02:00 PM",
            timeEnd = "04:45 PM",
            room = "Software Engineering Lab",
            faculty = "Prof. Neha Gupta",
            batchOrGroup = "All Batches",
            notes = "CI/CD Pipeline & JUnit testing milestone"
        ),
        ScheduleSlot(
            id = "slot-cs310-lec",
            courseCode = "CS-310",
            courseName = "Artificial Intelligence & ML",
            slotType = ScheduleSlotType.LECTURE,
            recurringDays = listOf(DayOfWeekEnum.MON, DayOfWeekEnum.WED),
            timeStart = "01:15 PM",
            timeEnd = "02:30 PM",
            room = "LH-302 (Auditorium Wing)",
            faculty = "Dr. Vikram Singhania",
            notes = "Backpropagation & Neural Network Architectures"
        ),
        ScheduleSlot(
            id = "slot-cs312-sem",
            courseCode = "CS-312",
            courseName = "Capstone Project Mentoring & Seminar",
            slotType = ScheduleSlotType.SEMINAR,
            recurringDays = listOf(DayOfWeekEnum.SAT),
            timeStart = "10:00 AM",
            timeEnd = "12:30 PM",
            room = "Auditorium Hall B",
            faculty = "Prof. K. Muralidhar & Guides",
            notes = "Mid-term sprint progress review"
        )
    )
}
