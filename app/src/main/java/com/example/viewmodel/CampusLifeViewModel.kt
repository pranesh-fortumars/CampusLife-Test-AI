package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.model.AcademicTask
import com.example.model.Subject
import com.example.worker.DeadlineWorkScheduler
import com.example.worker.ProjectAssignmentDeadlineService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppDestination(val label: String) {
    HOME("Home"),
    SEARCH("Search"),
    SCHEDULE("Schedule"),
    COMMUNITY("Community"),
    ME("Profile")
}

enum class SearchCategory(val label: String) {
    ALL("All"),
    COURSES("Courses"),
    MATERIALS("Study Notes"),
    POSTS("Peer Posts"),
    TASKS("Tasks"),
    CALENDAR("Academic Dates")
}

data class SearchSummary(
    val totalCount: Int,
    val coursesCount: Int,
    val materialsCount: Int,
    val postsCount: Int,
    val tasksCount: Int,
    val calendarCount: Int = 0
)

class CampusLifeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val courseDao = db.courseDao()
    private val materialDao = db.studyMaterialDao()
    private val postDao = db.communityPostDao()
    private val taskDao = db.taskDao()
    private val academicCalendarDao = db.academicCalendarDao()
    private val semesterDao = db.semesterDao()
    private val studySessionDao = db.studySessionDao()




    init {
        viewModelScope.launch(Dispatchers.IO) {
            db.populateInitialData()
        }
    }

    // Navigation state
    private val _currentDestination = MutableStateFlow(AppDestination.HOME)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    fun navigateTo(destination: AppDestination) {
        _currentDestination.value = destination
    }

    // Search query & category
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(SearchCategory.ALL)
    val selectedCategory: StateFlow<SearchCategory> = _selectedCategory.asStateFlow()

    private val _recentSearches = MutableStateFlow(
        listOf("Data Structures", "Operating Systems", "Cheat Sheet", "Room 302", "Heap Sort")
    )
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
    }

    fun selectCategory(category: SearchCategory) {
        _selectedCategory.value = category
    }

    fun applyRecentSearch(term: String) {
        _searchQuery.value = term
        _currentDestination.value = AppDestination.SEARCH
    }

    fun addRecentSearch(term: String) {
        val trimmed = term.trim()
        if (trimmed.isNotBlank()) {
            val updated = (_recentSearches.value.filter { it.lowercase() != trimmed.lowercase() }).toMutableList()
            updated.add(0, trimmed)
            _recentSearches.value = updated.take(8)
        }
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }

    // Reactive data flows
    @OptIn(ExperimentalCoroutinesApi::class)
    val courses: StateFlow<List<CourseEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) courseDao.getAllCourses()
            else courseDao.searchCourses(query.trim())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val materials: StateFlow<List<StudyMaterialEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) materialDao.getAllMaterials()
            else materialDao.searchMaterials(query.trim())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val posts: StateFlow<List<CommunityPostEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) postDao.getAllPosts()
            else postDao.searchPosts(query.trim())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<List<TaskEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) taskDao.getAllTasks()
            else taskDao.searchTasks(query.trim())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subjects: StateFlow<List<Subject>> = courses.map { courseList ->
        courseList.map { course ->
            Subject(
                code = course.code,
                name = course.name,
                credits = 4,
                faculty = course.instructor,
                department = "Dept. of Computer Science",
                attendedClasses = course.attendedClasses,
                totalClasses = course.totalClasses,
                targetPercentage = course.targetPercent,
                nextClassTime = "${course.dayOfWeek} • ${course.timeSlot}",
                room = course.room
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val academicTasks: StateFlow<List<AcademicTask>> = combine(tasks, courses) { taskList, courseList ->
        val courseMap = courseList.associate { it.code to it.name }
        taskList.map { task ->
            AcademicTask(
                id = task.id.toString(),
                title = task.title,
                subjectCode = task.courseCode,
                subjectName = courseMap[task.courseCode] ?: task.courseCode,
                category = "Assignment",
                type = "ASSIGNMENT",
                dueDate = task.dueDate,
                dueTime = "11:59 PM",
                priority = task.priority,
                weightage = "15% Internal",
                isCompleted = task.isCompleted,
                progressPercent = if (task.isCompleted) 100 else 0,
                faculty = "",
                note = task.details,
                dueTimestamp = System.currentTimeMillis() + 86400000L,
                createdAt = System.currentTimeMillis()
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addAcademicTask(
        title: String,
        subjectCode: String,
        subjectName: String = "",
        category: String = "Assignment",
        type: String = "ASSIGNMENT",
        dueDate: String,
        dueTime: String = "11:59 PM",
        priority: String = "HIGH",
        weightage: String = "",
        note: String = "",
        dueTimestamp: Long = System.currentTimeMillis() + 86400000L
    ) {
        addTask(
            title = title,
            courseCode = subjectCode,
            dueDate = dueDate,
            priority = priority,
            details = if (note.isNotBlank()) note else "$category - $type ($weightage)"
        )
    }

    fun toggleAcademicTaskComplete(id: String) {
        val numericId = id.toLongOrNull() ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val task = tasks.value.firstOrNull { it.id == numericId } ?: return@launch
            toggleTask(task)
        }
    }

    fun deleteAcademicTask(id: String) {
        val numericId = id.toLongOrNull() ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val task = tasks.value.firstOrNull { it.id == numericId } ?: return@launch
            taskDao.deleteTask(task)
        }
    }

    fun rescheduleTaskDueDate(id: String, newDate: String, newTimestamp: Long) {
        val numericId = id.toLongOrNull() ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val task = tasks.value.firstOrNull { it.id == numericId } ?: return@launch
            taskDao.updateTask(task.copy(dueDate = newDate))
        }
    }

    fun triggerUpcomingTasksReminder() {
        val context = getApplication<Application>()
        DeadlineWorkScheduler.schedulePeriodicDeadlineChecks(context)
        ProjectAssignmentDeadlineService.startDeadlineScan(context, forceNotify = true)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val calendarEvents: StateFlow<List<AcademicCalendarEventEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) academicCalendarDao.getAllEvents()
            else academicCalendarDao.searchEvents(query.trim())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCalendarFilter = MutableStateFlow("ALL")
    val selectedCalendarFilter: StateFlow<String> = _selectedCalendarFilter.asStateFlow()

    fun setCalendarFilter(filter: String) {
        _selectedCalendarFilter.value = filter
    }

    val filteredAcademicEvents: StateFlow<List<AcademicCalendarEventEntity>> = combine(
        calendarEvents,
        _selectedCalendarFilter
    ) { events, filter ->
        if (filter == "ALL") events else events.filter { it.type == filter }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined search summary count
    val searchSummary: StateFlow<SearchSummary> = combine(courses, materials, posts, tasks, calendarEvents) { c, m, p, t, cal ->
        SearchSummary(
            totalCount = c.size + m.size + p.size + t.size + cal.size,
            coursesCount = c.size,
            materialsCount = m.size,
            postsCount = p.size,
            tasksCount = t.size,
            calendarCount = cal.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchSummary(0, 0, 0, 0, 0, 0))

    // Attendance logging actions
    fun logAttendance(course: CourseEntity, isPresent: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = course.copy(
                attendedClasses = if (isPresent) course.attendedClasses + 1 else course.attendedClasses,
                totalClasses = course.totalClasses + 1
            )
            courseDao.updateCourse(updated)
        }
    }

    // Task toggle action
    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            taskDao.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun addTask(title: String, courseCode: String, dueDate: String, priority: String, details: String) {
        viewModelScope.launch(Dispatchers.IO) {
            taskDao.insertTask(
                TaskEntity(
                    title = title,
                    courseCode = courseCode,
                    dueDate = dueDate,
                    priority = priority,
                    details = details
                )
            )
        }
    }

    // Academic Calendar Events actions
    fun toggleEventReminder(event: AcademicCalendarEventEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            academicCalendarDao.updateReminder(event.id, !event.isReminderSet)
        }
    }

    fun addAcademicEvent(
        title: String,
        type: String,
        startDate: String,
        endDate: String,
        locationOrVenue: String,
        description: String,
        daysRemaining: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            academicCalendarDao.insertEvent(
                AcademicCalendarEventEntity(
                    title = title,
                    type = type,
                    startDate = startDate,
                    endDate = endDate,
                    locationOrVenue = locationOrVenue,
                    description = description,
                    daysRemaining = daysRemaining,
                    isReminderSet = false
                )
            )
        }
    }

    fun deleteAcademicEvent(event: AcademicCalendarEventEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            academicCalendarDao.deleteEvent(event)
        }
    }

    // Community post actions
    fun upvotePost(post: CommunityPostEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            postDao.upvotePost(post.id)
        }
    }

    fun createPost(title: String, content: String, courseCode: String, tag: String) {
        viewModelScope.launch(Dispatchers.IO) {
            postDao.insertPost(
                CommunityPostEntity(
                    author = "You (Student)",
                    courseCode = courseCode,
                    title = title,
                    content = content,
                    tag = tag,
                    timeAgo = "Just now"
                )
            )
        }
    }

    // Study material actions
    fun toggleMaterialDownloaded(material: StudyMaterialEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            materialDao.updateMaterial(material.copy(isDownloaded = !material.isDownloaded))
        }
    }

    // Academic Semester & Next Exam flows
    val currentSemester: StateFlow<SemesterEntity?> = semesterDao.getCurrentSemester()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allSemesters: StateFlow<List<SemesterEntity>> = semesterDao.getAllSemesters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nextScheduledExam: StateFlow<AcademicCalendarEventEntity?> = academicCalendarDao.getNextExam()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun switchCurrentSemester(semesterId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            semesterDao.setCurrentSemester(semesterId)
        }
    }

    fun updateSemester(semester: SemesterEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            semesterDao.updateSemester(semester)
        }
    }

    // Study Sessions & Pomodoro tracking
    val studyStatsPerSubject: StateFlow<List<SubjectStudyStats>> = studySessionDao.getStudyStatsPerSubject()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalFocusMinutes: StateFlow<Int> = studySessionDao.getTotalFocusMinutes()
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allStudySessions: StateFlow<List<StudySessionEntity>> = studySessionDao.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyStudyTrends: StateFlow<List<DayStudyTrend>> = allStudySessions.map { sessions ->
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val fullDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

        val baseBreakdowns = mapOf(
            "Mon" to mapOf("CS201" to 90, "MATH204" to 60),      // 150m (2.5h)
            "Tue" to mapOf("CS310" to 120, "CS201" to 75),       // 195m (3.25h)
            "Wed" to mapOf("MATH204" to 105, "PHY102" to 60),    // 165m (2.75h)
            "Thu" to mapOf("CS201" to 120, "CS310" to 90),       // 210m (3.5h)
            "Fri" to mapOf("PHY102" to 90, "ENG215" to 45),      // 135m (2.25h)
            "Sat" to mapOf("MATH204" to 60, "CS201" to 30),      // 90m (1.5h)
            "Sun" to mapOf("ENG215" to 45, "CS310" to 30)        // 75m (1.25h)
        )

        val todaySessions = sessions.filter { it.dateDisplay == "Today" }

        days.mapIndexed { index, dayKey ->
            val isToday = (dayKey == "Tue") // 2026-11-03 is Tuesday
            val dayBreakdown = (baseBreakdowns[dayKey] ?: emptyMap()).toMutableMap()
            if (isToday) {
                todaySessions.forEach { s ->
                    val cur = dayBreakdown[s.courseCode] ?: 0
                    dayBreakdown[s.courseCode] = cur + s.durationMinutes
                }
            }
            val totalMins = dayBreakdown.values.sum()
            val sessionCount = maxOf(1, totalMins / 45)
            val topSubject = dayBreakdown.maxByOrNull { it.value }?.key ?: "CS201"

            DayStudyTrend(
                dayKey = dayKey,
                dayLabel = fullDays[index],
                totalMinutes = totalMins,
                sessionCount = sessionCount,
                primarySubject = topSubject,
                subjectBreakdown = dayBreakdown
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklySubjectContributions: StateFlow<List<WeeklySubjectContribution>> = weeklyStudyTrends.map { trends ->
        val totalWeekMinutes = trends.sumOf { it.totalMinutes }
        val subjectMinutesMap = mutableMapOf<String, Int>()
        val subjectDaysMap = mutableMapOf<String, MutableList<String>>()

        trends.forEach { day ->
            day.subjectBreakdown.forEach { (subject, mins) ->
                if (mins > 0) {
                    subjectMinutesMap[subject] = (subjectMinutesMap[subject] ?: 0) + mins
                    val daysList = subjectDaysMap.getOrPut(subject) { mutableListOf() }
                    if (!daysList.contains(day.dayKey)) {
                        daysList.add(day.dayKey)
                    }
                }
            }
        }

        subjectMinutesMap.map { (courseCode, mins) ->
            val pct = if (totalWeekMinutes > 0) (mins.toDouble() / totalWeekMinutes) * 100.0 else 0.0
            val sessionEst = maxOf(1, mins / 45)
            WeeklySubjectContribution(
                courseCode = courseCode,
                totalMinutes = mins,
                sessionCount = sessionEst,
                percentageOfTotal = pct,
                daysActive = subjectDaysMap[courseCode] ?: emptyList()
            )
        }.sortedByDescending { it.totalMinutes }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun recordStudySession(

        courseCode: String,
        subjectName: String,
        durationMinutes: Int,
        mode: String,
        notes: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            studySessionDao.insertSession(
                StudySessionEntity(
                    courseCode = courseCode,
                    subjectName = subjectName,
                    durationMinutes = durationMinutes,
                    timestampMs = System.currentTimeMillis(),
                    dateDisplay = "Today",
                    mode = mode,
                    notes = notes
                )
            )
        }
    }

    fun deleteStudySession(session: StudySessionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            studySessionDao.deleteSession(session)
        }
    }

    // Daily study hour goal customization for analytics & chart thresholds
    private val analyticsPrefs = getApplication<Application>().getSharedPreferences("campus_life_analytics_prefs", android.content.Context.MODE_PRIVATE)

    private val _dailyStudyGoalHours = MutableStateFlow(
        analyticsPrefs.getFloat("daily_study_goal_hours", 2.0f).toDouble()
    )
    val dailyStudyGoalHours: StateFlow<Double> = _dailyStudyGoalHours.asStateFlow()

    fun updateDailyStudyGoalHours(hours: Double) {
        val clamped = (Math.round(hours * 10.0) / 10.0).coerceIn(0.5, 12.0)
        _dailyStudyGoalHours.value = clamped
        viewModelScope.launch(Dispatchers.IO) {
            analyticsPrefs.edit().putFloat("daily_study_goal_hours", clamped.toFloat()).apply()
        }
    }

    // Custom weekly study goals for individual subjects
    val defaultSubjectWeeklyGoals = mapOf(
        "CS201" to 5.0,
        "MATH204" to 5.0,
        "CS310" to 5.0,
        "PHY102" to 4.0,
        "ENG215" to 3.0
    )

    private val _subjectWeeklyStudyGoals = MutableStateFlow<Map<String, Double>>(
        loadPersistedSubjectGoals()
    )
    val subjectWeeklyStudyGoals: StateFlow<Map<String, Double>> = _subjectWeeklyStudyGoals.asStateFlow()

    fun getSubjectWeeklyGoal(courseCode: String): Double {
        return _subjectWeeklyStudyGoals.value[courseCode] ?: defaultSubjectWeeklyGoals[courseCode] ?: 5.0
    }

    fun updateSubjectWeeklyStudyGoal(courseCode: String, goalHours: Double) {
        val clamped = (Math.round(goalHours * 10.0) / 10.0).coerceIn(0.5, 25.0)
        val currentMap = _subjectWeeklyStudyGoals.value.toMutableMap()
        currentMap[courseCode] = clamped
        _subjectWeeklyStudyGoals.value = currentMap
        persistSubjectGoals(currentMap)
    }

    fun resetSubjectWeeklyStudyGoals() {
        val defaults = defaultSubjectWeeklyGoals.toMutableMap()
        _subjectWeeklyStudyGoals.value = defaults
        persistSubjectGoals(defaults)
    }

    private fun loadPersistedSubjectGoals(): Map<String, Double> {
        val jsonString = analyticsPrefs.getString("subject_weekly_goals_json", null)
        if (!jsonString.isNullOrEmpty()) {
            try {
                val json = org.json.JSONObject(jsonString)
                val map = mutableMapOf<String, Double>()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    map[key] = json.optDouble(key, 5.0)
                }
                val result = defaultSubjectWeeklyGoals.toMutableMap()
                result.putAll(map)
                return result
            } catch (e: Exception) {
                // fallback to defaults
            }
        }
        return defaultSubjectWeeklyGoals
    }

    private fun persistSubjectGoals(goals: Map<String, Double>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = org.json.JSONObject()
                goals.forEach { (code, goal) ->
                    json.put(code, goal)
                }
                analyticsPrefs.edit().putString("subject_weekly_goals_json", json.toString()).apply()
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}



