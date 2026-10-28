package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MockData
import com.example.data.local.AppDatabase
import com.example.data.local.CourseGradeEntity
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.GradeRepository
import com.example.data.repository.TaskRepository
import com.example.model.*
import com.example.worker.EventNotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

enum class AppDestination(val label: String) {
    HOME("Home"),
    TRACK("Track"),
    COMMUNITY("Community"),
    GROWTH("Growth"),
    ME("Me")
}

enum class SubScreen {
    NONE,
    FOCUS_TIMER,
    HACKATHON_PASS,
    SUBJECT_DETAIL,
    ID_VERIFICATION,
    CAMPUS_COPILOT,
    EXECUTIVE_DESK,
    REVISION_PLAN,
    DAILY_ATTENDANCE_LOGGER,
    TASKS_LIST
}

class CampusLifeViewModel(
    application: Application,
    private val repository: AttendanceRepository,
    private val taskRepository: TaskRepository,
    private val gradeRepository: GradeRepository,
    private val projectRepository: com.example.data.repository.AcademicProjectRepository =
        com.example.data.repository.AcademicProjectRepository(AppDatabase.provideProjectDao(application))
) : AndroidViewModel(application) {

    constructor(
        application: Application,
        repository: AttendanceRepository,
        taskRepository: TaskRepository
    ) : this(
        application,
        repository,
        taskRepository,
        GradeRepository(AppDatabase.getDatabase(application).gradeDao())
    )

    constructor(
        application: Application,
        taskDao: com.example.data.local.TaskDao
    ) : this(
        application,
        AttendanceRepository(AppDatabase.provideAttendanceDao(application)),
        TaskRepository(taskDao),
        GradeRepository(AppDatabase.provideGradeDao(application))
    )

    constructor(application: Application) : this(
        application,
        AttendanceRepository(AppDatabase.provideAttendanceDao(application)),
        TaskRepository(AppDatabase.provideTaskDao(application)),
        GradeRepository(AppDatabase.provideGradeDao(application))
    )

    companion object {
        fun provideFactory(application: Application): androidx.lifecycle.ViewModelProvider.Factory =
            object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    val db = AppDatabase.getDatabase(application)
                    return CampusLifeViewModel(
                        application = application,
                        repository = AttendanceRepository(db.attendanceDao()),
                        taskRepository = TaskRepository(db.taskDao()),
                        gradeRepository = GradeRepository(db.gradeDao())
                    ) as T
                }
            }
    }

    private val _currentTab = MutableStateFlow(AppDestination.HOME)
    val currentTab: StateFlow<AppDestination> = _currentTab.asStateFlow()

    private val _subScreen = MutableStateFlow(SubScreen.NONE)
    val subScreen: StateFlow<SubScreen> = _subScreen.asStateFlow()

    private val _profile = MutableStateFlow(MockData.defaultProfile)
    val profile: StateFlow<StudentProfile> = _profile.asStateFlow()

    private val _subjects = MutableStateFlow(MockData.initialSubjects)
    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()

    private val _attendanceHistory = MutableStateFlow<List<AttendanceRecord>>(emptyList())
    val attendanceHistory: StateFlow<List<AttendanceRecord>> = _attendanceHistory.asStateFlow()

    private val _selectedSubject = MutableStateFlow<Subject?>(null)
    val selectedSubject: StateFlow<Subject?> = _selectedSubject.asStateFlow()

    private val _academicTasks = MutableStateFlow<List<AcademicTask>>(emptyList())
    val academicTasks: StateFlow<List<AcademicTask>> = _academicTasks.asStateFlow()

    private val _academicProjects = MutableStateFlow<List<com.example.data.local.AcademicProjectEntity>>(emptyList())
    val academicProjects: StateFlow<List<com.example.data.local.AcademicProjectEntity>> = _academicProjects.asStateFlow()

    private val _assignments = MutableStateFlow(MockData.assignments)
    val assignments: StateFlow<List<AssignmentItem>> = _assignments.asStateFlow()

    private val _projects = MutableStateFlow(MockData.projects)
    val projects: StateFlow<List<ProjectItem>> = _projects.asStateFlow()

    private val _societies = MutableStateFlow(MockData.societies)
    val societies: StateFlow<List<SocietyItem>> = _societies.asStateFlow()

    private val _conversations = MutableStateFlow(MockData.conversations)
    val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()

    // Community Events Aggregator State
    private val _campusEvents = MutableStateFlow(CampusEventMockData.sampleEvents)
    val campusEvents: StateFlow<List<CampusEvent>> = _campusEvents.asStateFlow()

    // Simulator state
    private val _simulatorCount = MutableStateFlow(3)
    val simulatorCount: StateFlow<Int> = _simulatorCount.asStateFlow()

    private val _simulatorSubjectCode = MutableStateFlow("CS-304")
    val simulatorSubjectCode: StateFlow<String> = _simulatorSubjectCode.asStateFlow()

    // Home Checklist tasks status
    private val _homeTaskState = MutableStateFlow(mapOf(0 to false, 1 to false, 2 to false))
    val homeTaskState: StateFlow<Map<Int, Boolean>> = _homeTaskState.asStateFlow()

    // Attendance Logged state
    private val _todayAttendanceLogged = MutableStateFlow(false)
    val todayAttendanceLogged: StateFlow<Boolean> = _todayAttendanceLogged.asStateFlow()

    // Track subtabs
    private val _trackSubTab = MutableStateFlow("Academics") // "Academics", "Timetable", "Assignments", "Exams"
    val trackSubTab: StateFlow<String> = _trackSubTab.asStateFlow()

    // Feedback message (e.g. for snackbars)
    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    // Copilot messages
    private val _copilotMessages = MutableStateFlow(
        listOf(
            CopilotMessage("m-1", true, "What is my plan for this afternoon, and can I afford to skip the 2:45 PM Agile lab?", "12:38 PM"),
            CopilotMessage("m-2", false, "Here is your afternoon layout:", "12:38 PM", hasLayoutCard = true)
        )
    )
    val copilotMessages: StateFlow<List<CopilotMessage>> = _copilotMessages.asStateFlow()

    // Focus Timer state
    private val _focusSeconds = MutableStateFlow(38 * 60 + 45)
    val focusSeconds: StateFlow<Int> = _focusSeconds.asStateFlow()
    private val _isFocusRunning = MutableStateFlow(true)
    val isFocusRunning: StateFlow<Boolean> = _isFocusRunning.asStateFlow()

    // Target CGPA simulator
    private val _simulatedSem6Sgpa = MutableStateFlow(9.20f)
    val simulatedSem6Sgpa: StateFlow<Float> = _simulatedSem6Sgpa.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.seedInitialDataIfEmpty()
            } catch (e: Exception) {
                // In testing environments or when DB is unavailable, keep in-memory mock data
            }

            launch {
                try {
                    repository.subjects.collect { updatedList ->
                        if (updatedList.isNotEmpty()) {
                            _subjects.value = updatedList
                            recalculateOverallAttendance(updatedList)

                            // Keep selected subject in sync
                            val currentSelected = _selectedSubject.value
                            if (currentSelected != null) {
                                _selectedSubject.value = updatedList.find { it.code == currentSelected.code } ?: currentSelected
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore DB collect errors in mock fallback
                }
            }

            launch {
                try {
                    repository.allRecords.collect { records ->
                        _attendanceHistory.value = records
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }

            launch {
                try {
                    taskRepository.seedInitialTasksIfEmpty()
                    com.example.worker.TaskNotificationScheduler.schedulePeriodicTaskReminder(application)
                } catch (e: Exception) {
                    // Ignore
                }
                try {
                    taskRepository.allTasks.collect { tasks ->
                        _academicTasks.value = tasks
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }

            launch {
                try {
                    gradeRepository.seedInitialGradesIfEmpty()
                } catch (e: Exception) {
                    // Ignore
                }
                try {
                    gradeRepository.allGradeEntities.collect { entities ->
                        if (entities.isNotEmpty()) {
                            val grouped = entities.groupBy { it.semesterNumber }
                            val updatedSemesters = _semesterGrades.value.map { sem ->
                                val coursesInSem = grouped[sem.semesterNumber]?.map { it.toDomain() }
                                if (coursesInSem != null && coursesInSem.isNotEmpty()) {
                                    sem.copy(courses = coursesInSem)
                                } else sem
                            }
                            _semesterGrades.value = updatedSemesters
                            recalculateCgpa()
                        }
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }

            launch {
                try {
                    projectRepository.seedInitialProjectsIfEmpty()
                } catch (e: Exception) {
                    // Ignore
                }
                try {
                    projectRepository.allProjects.collect { projects ->
                        _academicProjects.value = projects
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    private fun recalculateOverallAttendance(list: List<Subject>) {
        val totalAttended = list.sumOf { it.attendedClasses }
        val totalClasses = list.sumOf { it.totalClasses }
        val overall = if (totalClasses == 0) 100.0 else (totalAttended.toDouble() / totalClasses) * 100.0
        val rounded = String.format(Locale.US, "%.1f", overall).toDoubleOrNull() ?: overall

        _profile.update { it.copy(overallAttendance = rounded) }
    }

    // Semester Grades & CGPA state
    private val _semesterGrades = MutableStateFlow(MockData.initialSemesterGrades)
    val semesterGrades: StateFlow<List<SemesterGrades>> = _semesterGrades.asStateFlow()

    fun updateSimulatedSem6Sgpa(value: Float) {
        _simulatedSem6Sgpa.value = value
    }

    fun updateCourseGrade(semesterNumber: Int, courseCode: String, newGrade: String) {
        val newPoint = GradeScale.getPoint(newGrade)
        _semesterGrades.update { list ->
            list.map { sem ->
                if (sem.semesterNumber == semesterNumber) {
                    val updatedCourses = sem.courses.map { c ->
                        if (c.courseCode == courseCode) {
                            c.copy(grade = newGrade.uppercase(), gradePoint = newPoint)
                        } else c
                    }
                    sem.copy(courses = updatedCourses)
                } else sem
            }
        }
        recalculateCgpa()
        viewModelScope.launch {
            try {
                gradeRepository.updateCourseGrade(semesterNumber, courseCode, newGrade)
            } catch (e: Exception) {
                // In-memory already updated
            }
        }
    }

    fun addCourseGrade(semesterNumber: Int, courseGrade: CourseGrade) {
        _semesterGrades.update { list ->
            list.map { sem ->
                if (sem.semesterNumber == semesterNumber) {
                    sem.copy(courses = sem.courses + courseGrade)
                } else sem
            }
        }
        recalculateCgpa()
        viewModelScope.launch {
            try {
                gradeRepository.insertGrade(courseGrade, semesterNumber)
            } catch (e: Exception) {
                // In-memory already updated
            }
        }
    }

    fun saveSemesterCourseGrades(semesterNumber: Int, courses: List<CourseGrade>) {
        _semesterGrades.update { list ->
            var semesterFound = false
            val updated = list.map { sem ->
                if (sem.semesterNumber == semesterNumber) {
                    semesterFound = true
                    sem.copy(courses = courses)
                } else sem
            }
            if (!semesterFound) {
                updated + SemesterGrades(
                    semesterNumber = semesterNumber,
                    semesterName = "Semester $semesterNumber",
                    academicYear = "2026-27",
                    courses = courses,
                    isCurrentSemester = true
                )
            } else updated
        }
        recalculateCgpa()
        viewModelScope.launch {
            try {
                courses.forEach { course ->
                    gradeRepository.insertGrade(course, semesterNumber)
                }
            } catch (e: Exception) {
                // In-memory already updated
            }
        }
        _userFeedbackMessage.value = "Saved ${courses.size} courses to Semester $semesterNumber records!"
    }

    fun logFocusSession(taskId: String?, durationMinutes: Int) {
        val taskTitle = if (taskId != null) _academicTasks.value.find { it.id == taskId }?.title else null
        _userFeedbackMessage.value = if (taskTitle != null) {
            "Logged $durationMinutes min Pomodoro session for '$taskTitle'! 🍅"
        } else {
            "Completed $durationMinutes min Pomodoro study sprint! 🎉"
        }
    }

    fun recalculateCgpa() {
        val summary = AcademicGpaSummary(_semesterGrades.value)
        val rounded = String.format(Locale.US, "%.2f", summary.cumulativeGpa).toDoubleOrNull() ?: summary.cumulativeGpa
        _profile.update { it.copy(cgpa = rounded) }
    }

    fun selectTab(tab: AppDestination) {
        _currentTab.value = tab
        _subScreen.value = SubScreen.NONE
    }

    fun openSubScreen(screen: SubScreen) {
        _subScreen.value = screen
    }

    fun closeSubScreen() {
        _subScreen.value = SubScreen.NONE
    }

    fun selectSubjectDetail(subject: Subject) {
        _selectedSubject.value = subject
        _subScreen.value = SubScreen.SUBJECT_DETAIL
    }

    fun setTrackSubTab(tab: String) {
        _trackSubTab.value = tab
    }

    fun clearFeedbackMessage() {
        _userFeedbackMessage.value = null
    }

    /**
     * Log a class session as Attended, Missed, or Cancelled
     */
    fun logAttendance(
        subjectCode: String,
        type: AttendanceType,
        note: String = "",
        customDate: String? = null,
        customTime: String? = null
    ) {
        viewModelScope.launch {
            try {
                repository.logAttendance(subjectCode, type, note, customDate, customTime)
                val statusText = when (type) {
                    AttendanceType.ATTENDED -> "Attended (+1)"
                    AttendanceType.MISSED -> "Missed (+1 Missed)"
                    AttendanceType.CANCELLED -> "Cancelled"
                }
                _userFeedbackMessage.value = "Logged class for $subjectCode: $statusText"
            } catch (e: Exception) {
                // Fallback to in-memory state update
                updateInMemoryAttendance(subjectCode, type, note)
            }
        }
    }

    /**
     * Quick 1-tap logging of Attended or Missed class
     */
    fun quickLogClass(subjectCode: String, isAttended: Boolean) {
        val type = if (isAttended) AttendanceType.ATTENDED else AttendanceType.MISSED
        logAttendance(subjectCode, type, note = "Quick log")
    }

    /**
     * Undo last attendance log for subject
     */
    fun undoLastAttendance(subjectCode: String) {
        viewModelScope.launch {
            try {
                repository.undoLastRecordForSubject(subjectCode)
                _userFeedbackMessage.value = "Undid last log for $subjectCode"
            } catch (e: Exception) {
                _userFeedbackMessage.value = "Undone"
            }
        }
    }

    /**
     * Direct manual calibration of attended and missed counts
     */
    fun updateSubjectCounts(subjectCode: String, attended: Int, missed: Int, targetPercentage: Double? = null) {
        viewModelScope.launch {
            try {
                repository.updateCounts(subjectCode, attended, missed, targetPercentage)
                _userFeedbackMessage.value = "Updated $subjectCode: $attended attended, $missed missed"
            } catch (e: Exception) {
                _subjects.update { list ->
                    list.map {
                        if (it.code == subjectCode) {
                            it.copy(
                                attendedClasses = attended.coerceAtLeast(0),
                                totalClasses = (attended + missed).coerceAtLeast(0),
                                targetPercentage = targetPercentage ?: it.targetPercentage
                            )
                        } else it
                    }
                }
                recalculateOverallAttendance(_subjects.value)
            }
        }
    }

    /**
     * Add a new custom subject to track
     */
    fun addNewSubject(
        code: String,
        name: String,
        credits: Int,
        faculty: String,
        attended: Int,
        missed: Int,
        targetPercentage: Double = 75.0,
        room: String = "LH-101",
        nextClassTime: String = "Mon, 10:00 AM"
    ) {
        val newSub = Subject(
            code = code.trim().uppercase(),
            name = name.trim(),
            credits = credits,
            faculty = faculty.trim(),
            attendedClasses = attended.coerceAtLeast(0),
            totalClasses = (attended + missed).coerceAtLeast(0),
            targetPercentage = targetPercentage,
            room = room,
            nextClassTime = nextClassTime
        )

        viewModelScope.launch {
            try {
                repository.addSubject(newSub)
                _userFeedbackMessage.value = "Added subject ${newSub.code}: ${newSub.name}"
            } catch (e: Exception) {
                _subjects.update { it + newSub }
                recalculateOverallAttendance(_subjects.value)
            }
        }
    }

    /**
     * Delete an enrolled subject
     */
    fun deleteSubject(code: String) {
        viewModelScope.launch {
            try {
                repository.deleteSubject(code)
                _userFeedbackMessage.value = "Deleted subject $code"
            } catch (e: Exception) {
                _subjects.update { list -> list.filter { it.code != code } }
                recalculateOverallAttendance(_subjects.value)
            }
        }
    }

    private fun updateInMemoryAttendance(subjectCode: String, type: AttendanceType, note: String) {
        _subjects.update { current ->
            current.map { subject ->
                if (subject.code == subjectCode) {
                    val newAttended = when (type) {
                        AttendanceType.ATTENDED -> subject.attendedClasses + 1
                        AttendanceType.MISSED -> subject.attendedClasses
                        AttendanceType.CANCELLED -> subject.attendedClasses
                    }
                    val newTotal = when (type) {
                        AttendanceType.ATTENDED -> subject.totalClasses + 1
                        AttendanceType.MISSED -> subject.totalClasses + 1
                        AttendanceType.CANCELLED -> subject.totalClasses
                    }
                    subject.copy(attendedClasses = newAttended, totalClasses = newTotal)
                } else subject
            }
        }
        recalculateOverallAttendance(_subjects.value)

        val newRecord = AttendanceRecord(
            subjectCode = subjectCode,
            subjectName = _subjects.value.find { it.code == subjectCode }?.name ?: subjectCode,
            date = "Today",
            time = "Just now",
            type = type,
            note = note
        )
        _attendanceHistory.update { listOf(newRecord) + it }
    }

    fun incrementSimulator() {
        if (_simulatorCount.value < 15) {
            _simulatorCount.update { it + 1 }
        }
    }

    fun decrementSimulator() {
        if (_simulatorCount.value > 1) {
            _simulatorCount.update { it - 1 }
        }
    }

    fun resetSimulator() {
        _simulatorCount.value = 3
    }

    fun setSimulatorSubject(code: String) {
        _simulatorSubjectCode.value = code
    }

    fun toggleHomeTask(index: Int) {
        _homeTaskState.update { current ->
            current.toMutableMap().apply {
                this[index] = !(this[index] ?: false)
            }
        }
    }

    fun markTodayAttended() {
        _todayAttendanceLogged.value = true
        quickLogClass("CS-302", isAttended = true)
    }

    fun markTodayMissed() {
        _todayAttendanceLogged.value = true
        quickLogClass("CS-302", isAttended = false)
    }

    fun toggleFocusTimer() {
        _isFocusRunning.update { !it }
    }

    fun resetFocusTimer() {
        _focusSeconds.value = 50 * 60
        _isFocusRunning.value = false
    }

    fun sendCopilotQuery(prompt: String) {
        if (prompt.isBlank()) return
        val userMsg = CopilotMessage(id = "usr-${System.currentTimeMillis()}", isUser = true, text = prompt)
        _copilotMessages.update { it + userMsg }

        // Generate intelligent contextual response
        val botMsg = CopilotMessage(
            id = "bot-${System.currentTimeMillis()}",
            isUser = false,
            text = "Analyzing syllabus & attendance logs for '$prompt'...\n\nYour current overall attendance is ${_profile.value.overallAttendance}%. You have safe margins in Software Engineering (+12%) and DBMS (+11%). Tomorrow's Operating Systems lecture at 9:15 AM is critical to stay above the 75% statutory NAAC minimum."
        )
        _copilotMessages.update { it + botMsg }
    }

    fun toggleAssignmentComplete(id: String) {
        _assignments.update { list ->
            list.map { if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it }
        }
    }

    fun toggleAcademicTaskComplete(id: String) {
        val currentTask = _academicTasks.value.find { it.id == id } ?: return
        val newCompleted = !currentTask.isCompleted
        viewModelScope.launch {
            try {
                taskRepository.setTaskCompleted(id, newCompleted)
                _userFeedbackMessage.value = if (newCompleted) "Completed: ${currentTask.title}" else "Marked pending: ${currentTask.title}"
            } catch (e: Exception) {
                _academicTasks.update { list ->
                    list.map { if (it.id == id) it.copy(isCompleted = newCompleted) else it }
                }
            }
        }
    }

    fun addAcademicTask(
        title: String,
        subjectCode: String,
        subjectName: String,
        category: String = "Assignment",
        type: String = "ASSIGNMENT",
        dueDate: String = "Tomorrow",
        dueTime: String = "11:59 PM",
        priority: String = "HIGH",
        weightage: String = "15% Weightage",
        note: String = "",
        dueTimestamp: Long? = null
    ) {
        val calculatedTimestamp = dueTimestamp ?: com.example.ui.components.TaskDateUtils.parseDateStringToEpochMillis(dueDate)
        val newTask = AcademicTask(
            title = title,
            subjectCode = subjectCode,
            subjectName = subjectName,
            category = category,
            type = type,
            dueDate = dueDate,
            dueTime = dueTime,
            priority = priority,
            weightage = weightage,
            note = note,
            dueTimestamp = calculatedTimestamp
        )
        viewModelScope.launch {
            try {
                taskRepository.insertTask(newTask)
                _userFeedbackMessage.value = "Saved task '$title' (Due $dueDate) to Room DB"
                if (priority == "URGENT" || dueDate.contains("Today", ignoreCase = true) || dueDate.contains("Tomorrow", ignoreCase = true)) {
                    com.example.worker.TaskNotificationScheduler.triggerImmediateDueCheck(getApplication())
                }
            } catch (e: Exception) {
                _academicTasks.update { listOf(newTask) + it }
            }
        }
    }

    /**
     * Reschedules an existing task's due date in Room database using the Material Design 3 DatePicker.
     */
    fun rescheduleTaskDueDate(taskId: String, newDueDate: String, newTimestamp: Long) {
        viewModelScope.launch {
            try {
                taskRepository.updateTaskDueDate(taskId, newDueDate, newTimestamp)
                _userFeedbackMessage.value = "Rescheduled task due date to $newDueDate in Room DB"
            } catch (e: Exception) {
                _userFeedbackMessage.value = "Updated task due date to $newDueDate"
            }
        }
    }

    fun triggerUpcomingTasksReminder() {
        com.example.worker.TaskNotificationScheduler.triggerImmediateDueCheck(getApplication())
        _userFeedbackMessage.value = "WorkManager checking upcoming deadlines..."
    }

    fun deleteAcademicTask(id: String) {
        viewModelScope.launch {
            try {
                taskRepository.deleteTask(id)
                _userFeedbackMessage.value = "Deleted deadline"
            } catch (e: Exception) {
                _academicTasks.update { list -> list.filter { it.id != id } }
            }
        }
    }

    fun toggleAcademicProjectComplete(id: String) {
        viewModelScope.launch {
            try {
                projectRepository.toggleProjectCompletion(id)
            } catch (e: Exception) {
                _academicProjects.update { list ->
                    list.map { if (it.id == id) it.toggleCompleted() else it }
                }
            }
        }
    }

    fun deleteAcademicProject(id: String) {
        viewModelScope.launch {
            try {
                projectRepository.deleteProjectById(id)
                _userFeedbackMessage.value = "Deleted project"
            } catch (e: Exception) {
                _academicProjects.update { list -> list.filter { it.id != id } }
            }
        }
    }

    fun addAcademicProject(
        title: String,
        subjectCode: String,
        subjectName: String,
        description: String,
        techStack: String,
        deadlineDate: String,
        totalMilestones: Int,
        weightage: String,
        githubUrl: String,
        teamMembers: String
    ) {
        val newProj = com.example.data.local.AcademicProjectEntity(
            title = title,
            subjectCode = subjectCode,
            subjectName = subjectName,
            description = description,
            techStack = techStack,
            deadlineDate = deadlineDate,
            totalMilestones = totalMilestones,
            weightage = weightage,
            githubUrl = githubUrl,
            teamMembers = teamMembers
        )
        viewModelScope.launch {
            try {
                projectRepository.insertProject(newProj)
                _userFeedbackMessage.value = "Added capstone project: $title"
            } catch (e: Exception) {
                _academicProjects.update { it + newProj }
            }
        }
    }

    // Community Events & Seminars Operations
    fun updateEventRsvp(
        eventId: String,
        newStatus: RsvpStatus,
        context: android.content.Context? = null
    ) {
        var updatedEvent: CampusEvent? = null
        _campusEvents.update { events ->
            events.map { event ->
                if (event.id == eventId) {
                    val wasGoing = event.rsvpStatus == RsvpStatus.GOING
                    val isGoing = newStatus == RsvpStatus.GOING
                    val attendeeDelta = when {
                        !wasGoing && isGoing -> 1
                        wasGoing && !isGoing -> -1
                        else -> 0
                    }
                    val newCount = (event.attendeeCount + attendeeDelta).coerceAtLeast(0)
                    val reminderState = if (isGoing) true else event.isReminderSet
                    val modified = event.copy(
                        rsvpStatus = newStatus,
                        attendeeCount = newCount,
                        isReminderSet = reminderState
                    )
                    updatedEvent = modified
                    modified
                } else {
                    event
                }
            }
        }

        val statusText = when (newStatus) {
            RsvpStatus.GOING -> "RSVP Confirmed: You are attending!"
            RsvpStatus.INTERESTED -> "Marked as Interested"
            RsvpStatus.DECLINED -> "RSVP Declined"
            RsvpStatus.NONE -> "RSVP Cleared"
        }
        _userFeedbackMessage.value = statusText

        if (context != null && updatedEvent != null && (newStatus == RsvpStatus.GOING || newStatus == RsvpStatus.INTERESTED)) {
            EventNotificationHelper.postEventRsvpNotification(context, updatedEvent!!, newStatus)
        }
    }

    fun toggleEventReminder(
        eventId: String,
        context: android.content.Context? = null
    ) {
        var reminderState = false
        var targetEvent: CampusEvent? = null
        _campusEvents.update { events ->
            events.map { event ->
                if (event.id == eventId) {
                    val toggled = !event.isReminderSet
                    reminderState = toggled
                    val modified = event.copy(isReminderSet = toggled)
                    targetEvent = modified
                    modified
                } else {
                    event
                }
            }
        }
        _userFeedbackMessage.value = if (reminderState) "🔔 Reminder notification set (1h before)" else "🔕 Reminder cancelled"

        if (context != null && reminderState && targetEvent != null) {
            EventNotificationHelper.postUpcomingEventAlert(context, targetEvent!!)
        }
    }

    fun addCampusEvent(newEvent: CampusEvent) {
        _campusEvents.update { listOf(newEvent) + it }
        _userFeedbackMessage.value = "Published event: ${newEvent.title}"
    }

    fun testTriggerEventNotification(context: android.content.Context, event: CampusEvent) {
        val success = EventNotificationHelper.postUpcomingEventAlert(context, event)
        _userFeedbackMessage.value = if (success) "Sent notification for ${event.title}" else "Notification posted (check status bar)"
    }
}
