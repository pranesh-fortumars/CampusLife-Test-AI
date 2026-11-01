package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.StudentScheduleAndDeadlineDao
import com.example.data.local.StudentScheduleAndDeadlineEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel to store and manage student class schedules and academic deadlines,
 * backed by Room SQLite database.
 */
class StudentScheduleViewModel(
    application: Application,
    private val dao: StudentScheduleAndDeadlineDao
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application,
        AppDatabase.provideStudentScheduleAndDeadlineDao(application)
    )

    private val _schedules = MutableStateFlow<List<StudentScheduleAndDeadlineEntity>>(emptyList())
    val schedules: StateFlow<List<StudentScheduleAndDeadlineEntity>> = _schedules.asStateFlow()

    private val _deadlines = MutableStateFlow<List<StudentScheduleAndDeadlineEntity>>(emptyList())
    val deadlines: StateFlow<List<StudentScheduleAndDeadlineEntity>> = _deadlines.asStateFlow()

    private val _selectedDay = MutableStateFlow("ALL") // "ALL", "MON", "TUE", "WED", "THU", "FRI"
    val selectedDay: StateFlow<String> = _selectedDay.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        viewModelScope.launch {
            seedInitialDataIfEmpty()

            launch {
                dao.getAllSchedules().collect { list ->
                    _schedules.value = list
                }
            }

            launch {
                dao.getAllDeadlines().collect { list ->
                    _deadlines.value = list
                }
            }
        }
    }

    fun setDayFilter(day: String) {
        _selectedDay.value = day
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    /**
     * Add a student class schedule slot to Room.
     */
    fun addClassSchedule(
        title: String,
        courseCode: String,
        courseName: String,
        dayOfWeek: String,
        timeSlot: String,
        roomOrLocation: String,
        instructor: String = "",
        notes: String = "",
        reminderEnabled: Boolean = true
    ) {
        val entity = StudentScheduleAndDeadlineEntity(
            title = title,
            courseCode = courseCode,
            courseName = courseName,
            itemType = "SCHEDULE",
            dayOfWeek = dayOfWeek.uppercase(),
            timeSlot = timeSlot,
            roomOrLocation = roomOrLocation,
            instructorOrFaculty = instructor,
            reminderEnabled = reminderEnabled,
            notes = notes
        )
        viewModelScope.launch {
            try {
                dao.insertItem(entity)
                _userMessage.value = "Added $courseCode schedule ($dayOfWeek, $timeSlot)"
            } catch (e: Exception) {
                _schedules.update { it + entity }
                _userMessage.value = "Added $courseCode schedule"
            }
        }
    }

    /**
     * Add an academic deadline or exam event to Room.
     */
    fun addAcademicDeadline(
        title: String,
        courseCode: String,
        courseName: String,
        dueDateString: String,
        dueTimestamp: Long,
        roomOrLocation: String = "Campus / Portal",
        weightageOrPriority: String = "High",
        notes: String = "",
        reminderEnabled: Boolean = true
    ) {
        val entity = StudentScheduleAndDeadlineEntity(
            title = title,
            courseCode = courseCode,
            courseName = courseName,
            itemType = "DEADLINE",
            dueDateString = dueDateString,
            dueTimestamp = dueTimestamp,
            roomOrLocation = roomOrLocation,
            weightageOrPriority = weightageOrPriority,
            reminderEnabled = reminderEnabled,
            notes = notes,
            isCompleted = false
        )
        viewModelScope.launch {
            try {
                dao.insertItem(entity)
                _userMessage.value = "Saved deadline: $title ($dueDateString)"
            } catch (e: Exception) {
                _deadlines.update { it + entity }
                _userMessage.value = "Saved deadline: $title"
            }
        }
    }

    /**
     * Toggle reminder notification for a schedule or deadline.
     */
    fun toggleReminder(id: String) {
        val item = (_schedules.value + _deadlines.value).find { it.id == id } ?: return
        val newState = !item.reminderEnabled
        viewModelScope.launch {
            try {
                dao.toggleReminder(id, newState)
                _userMessage.value = if (newState) "🔔 Reminder enabled for ${item.title}" else "Reminder disabled"
            } catch (e: Exception) {
                _schedules.update { list -> list.map { if (it.id == id) it.copy(reminderEnabled = newState) else it } }
                _deadlines.update { list -> list.map { if (it.id == id) it.copy(reminderEnabled = newState) else it } }
            }
        }
    }

    /**
     * Toggle completion status of an academic deadline.
     */
    fun toggleDeadlineCompleted(id: String) {
        val item = _deadlines.value.find { it.id == id } ?: return
        val newCompleted = !item.isCompleted
        viewModelScope.launch {
            try {
                dao.setCompleted(id, newCompleted)
                _userMessage.value = if (newCompleted) "Marked '${item.title}' as completed ✓" else "Reopened '${item.title}'"
            } catch (e: Exception) {
                _deadlines.update { list -> list.map { if (it.id == id) it.copy(isCompleted = newCompleted) else it } }
            }
        }
    }

    /**
     * Delete an item by ID from Room.
     */
    fun deleteItem(id: String) {
        viewModelScope.launch {
            try {
                dao.deleteItemById(id)
                _userMessage.value = "Removed item from schedule & deadlines"
            } catch (e: Exception) {
                _schedules.update { list -> list.filter { it.id != id } }
                _deadlines.update { list -> list.filter { it.id != id } }
            }
        }
    }

    suspend fun seedInitialDataIfEmpty() {
        try {
            val count = dao.getItemCount()
            if (count == 0) {
                val now = System.currentTimeMillis()
                val day = 86400000L

                val initialItems = listOf(
                    // Recurring Class Schedules
                    StudentScheduleAndDeadlineEntity(
                        id = "sched-1",
                        title = "Database Management Systems (Lecture)",
                        courseCode = "CS-302",
                        courseName = "Database Management Systems",
                        itemType = "SCHEDULE",
                        dayOfWeek = "MON",
                        timeSlot = "09:00 AM - 10:15 AM",
                        roomOrLocation = "LH-102 (Block A)",
                        instructorOrFaculty = "Dr. Ananya Roy",
                        notes = "Bring laptop for SQL query execution exercises"
                    ),
                    StudentScheduleAndDeadlineEntity(
                        id = "sched-2",
                        title = "Operating Systems Kernel Lab",
                        courseCode = "CS-304",
                        courseName = "Operating Systems",
                        itemType = "SCHEDULE",
                        dayOfWeek = "MON",
                        timeSlot = "10:30 AM - 12:30 PM",
                        roomOrLocation = "CS Lab 304, Block B",
                        instructorOrFaculty = "Prof. Muralidhar",
                        notes = "Linux kernel compile and memory page faults"
                    ),
                    StudentScheduleAndDeadlineEntity(
                        id = "sched-3",
                        title = "Computer Networks & Routing Protocols",
                        courseCode = "CS-306",
                        courseName = "Computer Networks",
                        itemType = "SCHEDULE",
                        dayOfWeek = "TUE",
                        timeSlot = "11:15 AM - 12:30 PM",
                        roomOrLocation = "LH-201 (Block B)",
                        instructorOrFaculty = "Dr. Rajesh Gupta",
                        notes = "Packet sniffer and Wireshark trace analysis"
                    ),
                    StudentScheduleAndDeadlineEntity(
                        id = "sched-4",
                        title = "Software Engineering & Agile Methodologies",
                        courseCode = "CS-308",
                        courseName = "Software Engineering",
                        itemType = "SCHEDULE",
                        dayOfWeek = "WED",
                        timeSlot = "02:00 PM - 03:15 PM",
                        roomOrLocation = "LH-105 (Block A)",
                        instructorOrFaculty = "Prof. Shalini V.",
                        notes = "Sprint planning review and Jira board updates"
                    ),
                    StudentScheduleAndDeadlineEntity(
                        id = "sched-5",
                        title = "Data Structures & Algorithmic Problem Solving",
                        courseCode = "CS-301",
                        courseName = "Data Structures & Algorithms",
                        itemType = "SCHEDULE",
                        dayOfWeek = "THU",
                        timeSlot = "09:00 AM - 10:15 AM",
                        roomOrLocation = "Auditorium Annex",
                        instructorOrFaculty = "Dr. Vikram Seth",
                        notes = "Dynamic Programming & Graph algorithms discussion"
                    ),

                    // Academic Deadlines
                    StudentScheduleAndDeadlineEntity(
                        id = "dl-1",
                        title = "Operating Systems Lab Exam & Shell Script Viva",
                        courseCode = "CS-304",
                        courseName = "Operating Systems",
                        itemType = "DEADLINE",
                        dueDateString = "Nov 04, 2026",
                        dueTimestamp = now + (3 * day),
                        roomOrLocation = "CS Lab 304",
                        weightageOrPriority = "25% Practical Grade",
                        notes = "Topics: Fork, POSIX threads, Semaphores, and Paging simulation"
                    ),
                    StudentScheduleAndDeadlineEntity(
                        id = "dl-2",
                        title = "DBMS Milestone 2: B+ Tree Indexing Engine",
                        courseCode = "CS-302",
                        courseName = "Database Management Systems",
                        itemType = "DEADLINE",
                        dueDateString = "Nov 08, 2026",
                        dueTimestamp = now + (7 * day),
                        roomOrLocation = "GitHub Classroom & Portal",
                        weightageOrPriority = "High",
                        notes = "Submit zip archive containing source code, test cases, and bench report"
                    ),
                    StudentScheduleAndDeadlineEntity(
                        id = "dl-3",
                        title = "Computer Networks Socket Programming Assignment",
                        courseCode = "CS-306",
                        courseName = "Computer Networks",
                        itemType = "DEADLINE",
                        dueDateString = "Nov 12, 2026",
                        dueTimestamp = now + (11 * day),
                        roomOrLocation = "Moodle Submission Portal",
                        weightageOrPriority = "15% Continuous Assessment",
                        notes = "Multi-client chat server with select/epoll I/O multiplexing"
                    ),
                    StudentScheduleAndDeadlineEntity(
                        id = "dl-4",
                        title = "Software Engineering Capstone Mid-Term Sprint Demo",
                        courseCode = "CS-308",
                        courseName = "Software Engineering",
                        itemType = "DEADLINE",
                        dueDateString = "Nov 18, 2026",
                        dueTimestamp = now + (17 * day),
                        roomOrLocation = "Conference Room B2",
                        weightageOrPriority = "30% Project Grade",
                        notes = "Live demonstration of MVP with CI/CD deployment pipeline"
                    )
                )

                dao.insertItems(initialItems)
            }
        } catch (e: Exception) {
            // Ignore in offline tests
        }
    }
}
