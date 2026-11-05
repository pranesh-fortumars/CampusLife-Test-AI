package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
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

        val baseMinutes = mapOf(
            "Mon" to 150, // 2.5h
            "Tue" to 195, // 3.25h
            "Wed" to 165, // 2.75h
            "Thu" to 210, // 3.5h
            "Fri" to 135, // 2.25h
            "Sat" to 90,  // 1.5h
            "Sun" to 75   // 1.25h
        )
        val baseSubjects = mapOf(
            "Mon" to "CS201",
            "Tue" to "CS310",
            "Wed" to "MATH204",
            "Thu" to "CS201",
            "Fri" to "PHY102",
            "Sat" to "MATH204",
            "Sun" to "ENG215"
        )

        val extraTodayMinutes = sessions.filter { it.dateDisplay == "Today" }.sumOf { it.durationMinutes }

        days.mapIndexed { index, dayKey ->
            val isToday = (dayKey == "Tue") // 2026-11-03 is Tuesday
            val mins = (baseMinutes[dayKey] ?: 0) + (if (isToday) extraTodayMinutes else 0)
            val count = maxOf(1, mins / 45)
            val subject = if (isToday && sessions.isNotEmpty()) sessions.first().courseCode else (baseSubjects[dayKey] ?: "CS201")

            DayStudyTrend(
                dayKey = dayKey,
                dayLabel = fullDays[index],
                totalMinutes = mins,
                sessionCount = count,
                primarySubject = subject
            )
        }
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
}



