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
    private val subjectMasteryDao = db.subjectMasteryDao()
    private val weeklyDayNoteDao = db.weeklyDayNoteDao()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            db.populateInitialData()
            ensureSubjectMasteryData()
            ensureWeeklyDayNotesData()
            ensureExtendedStudyHistoryData()
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
    val availableSessionTags = listOf("All", "Exam Prep", "Homework", "Research", "Project", "Revision")

    private val _selectedSessionTagFilter = MutableStateFlow("All")
    val selectedSessionTagFilter: StateFlow<String> = _selectedSessionTagFilter.asStateFlow()

    fun selectSessionTagFilter(tag: String) {
        _selectedSessionTagFilter.value = tag
    }

    private val _selectedDateRange = MutableStateFlow(StudyDateRange.LAST_7_DAYS)
    val selectedDateRange: StateFlow<StudyDateRange> = _selectedDateRange.asStateFlow()

    fun selectDateRange(range: StudyDateRange) {
        _selectedDateRange.value = range
    }

    val allStudySessions: StateFlow<List<StudySessionEntity>> = studySessionDao.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyStatsPerSubject: StateFlow<List<SubjectStudyStats>> = combine(
        allStudySessions,
        _selectedSessionTagFilter,
        courses,
        _selectedDateRange,
        currentSemester
    ) { sessions, tagFilter, courseList, dateRange, semester ->
        val nowMs = System.currentTimeMillis()
        val filteredByRange = sessions.filter {
            StudyDateRangeHelper.isSessionInRange(it.timestampMs, dateRange, semester, nowMs)
        }
        val filtered = if (tagFilter == "All") {
            filteredByRange
        } else {
            filteredByRange.filter { it.tag.equals(tagFilter, ignoreCase = true) }
        }
        val grouped = filtered.groupBy { it.courseCode }
        val allCodes = (courseList.map { it.code } + grouped.keys).distinct()
        allCodes.map { code ->
            val list = grouped[code] ?: emptyList()
            SubjectStudyStats(
                courseCode = code,
                totalMinutes = list.sumOf { it.durationMinutes },
                sessionCount = list.size
            )
        }.sortedByDescending { it.totalMinutes }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalFocusMinutes: StateFlow<Int> = combine(
        allStudySessions,
        _selectedSessionTagFilter,
        _selectedDateRange,
        currentSemester
    ) { sessions, tagFilter, dateRange, semester ->
        val nowMs = System.currentTimeMillis()
        val filteredByRange = sessions.filter {
            StudyDateRangeHelper.isSessionInRange(it.timestampMs, dateRange, semester, nowMs)
        }
        val filtered = if (tagFilter == "All") {
            filteredByRange
        } else {
            filteredByRange.filter { it.tag.equals(tagFilter, ignoreCase = true) }
        }
        filtered.filter { it.mode == "FOCUS" }.sumOf { it.durationMinutes }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun getCurrentDayKey(): String {
        val cal = java.util.Calendar.getInstance()
        return when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> "Mon"
            java.util.Calendar.TUESDAY -> "Tue"
            java.util.Calendar.WEDNESDAY -> "Wed"
            java.util.Calendar.THURSDAY -> "Thu"
            java.util.Calendar.FRIDAY -> "Fri"
            java.util.Calendar.SATURDAY -> "Sat"
            java.util.Calendar.SUNDAY -> "Sun"
            else -> "Mon"
        }
    }

    val weeklyDayNotes: StateFlow<Map<String, String>> = weeklyDayNoteDao
        .getAllNotes()
        .map { list -> list.associate { it.dayKey to it.note } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private suspend fun ensureWeeklyDayNotesData() {
        val alreadySeeded = analyticsPrefs.getBoolean("seeded_weekly_day_notes", false)
        if (!alreadySeeded && weeklyDayNoteDao.getCount() == 0) {
            weeklyDayNoteDao.insertAllNotes(
                listOf(
                    WeeklyDayNoteEntity(dayKey = "Thu", note = "Exam Prep"),
                    WeeklyDayNoteEntity(dayKey = "Tue", note = "Group Study")
                )
            )
            analyticsPrefs.edit().putBoolean("seeded_weekly_day_notes", true).apply()
        } else if (!alreadySeeded) {
            analyticsPrefs.edit().putBoolean("seeded_weekly_day_notes", true).apply()
        }
    }

    fun saveQuickNoteForDay(dayKey: String, note: String) {
        val trimmed = note.trim().take(40)
        viewModelScope.launch(Dispatchers.IO) {
            if (trimmed.isEmpty()) {
                weeklyDayNoteDao.deleteNoteForDay(dayKey)
            } else {
                weeklyDayNoteDao.upsertNote(
                    WeeklyDayNoteEntity(
                        dayKey = dayKey,
                        note = trimmed,
                        updatedTimestampMs = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    private suspend fun ensureExtendedStudyHistoryData() {
        val alreadySeeded = analyticsPrefs.getBoolean("seeded_extended_study_history_v1", false)
        if (!alreadySeeded) {
            val nowMs = System.currentTimeMillis()
            val sevenDaysAgo = nowMs - (7L * StudyDateRangeHelper.ONE_DAY_MS)
            val existing = studySessionDao.getAllSessionsList()
            val hasHistorical = existing.any { it.timestampMs < sevenDaysAgo }
            if (!hasHistorical) {
                studySessionDao.insertAllSessions(AppDatabase.buildExtendedHistoricalStudySessions(nowMs))
            }
            analyticsPrefs.edit().putBoolean("seeded_extended_study_history_v1", true).apply()
        }
    }

    val weeklyStudyTrends: StateFlow<List<DayStudyTrend>> = combine(
        allStudySessions,
        _selectedSessionTagFilter,
        weeklyDayNotes,
        _selectedDateRange,
        currentSemester
    ) { sessions, tagFilter, notesMap, dateRange, semester ->
        val nowMs = System.currentTimeMillis()
        val tagFiltered = if (tagFilter == "All") {
            sessions
        } else {
            sessions.filter { it.tag.equals(tagFilter, ignoreCase = true) }
        }

        when (dateRange) {
            StudyDateRange.THIS_MONTH -> {
                val monthSessions = tagFiltered.filter {
                    StudyDateRangeHelper.isSessionInRange(it.timestampMs, StudyDateRange.THIS_MONTH, semester, nowMs)
                }
                StudyDateRangeHelper.buildMonthlyWeeklyTrends(monthSessions, nowMs)
            }
            StudyDateRange.SEMESTER -> {
                val semSessions = tagFiltered.filter {
                    StudyDateRangeHelper.isSessionInRange(it.timestampMs, StudyDateRange.SEMESTER, semester, nowMs)
                }
                StudyDateRangeHelper.buildSemesterMonthlyTrends(semSessions, semester, nowMs)
            }
            StudyDateRange.LAST_7_DAYS -> {
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                val fullDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                val currentDayKey = getCurrentDayKey()

                val tagMultiplier = when (tagFilter) {
                    "Exam Prep" -> 0.45
                    "Homework" -> 0.35
                    "Research" -> 0.20
                    "Project" -> 0.25
                    "Revision" -> 0.20
                    else -> 1.0
                }

                val baseBreakdowns = mapOf(
                    "Mon" to mapOf("CS201" to 90, "MATH204" to 60),      // 150m (2.5h)
                    "Tue" to mapOf("CS310" to 120, "CS201" to 75),       // 195m (3.25h)
                    "Wed" to mapOf("MATH204" to 105, "PHY102" to 60),    // 165m (2.75h)
                    "Thu" to mapOf("CS201" to 120, "CS310" to 90),       // 210m (3.5h)
                    "Fri" to mapOf("PHY102" to 90, "ENG215" to 45),      // 135m (2.25h)
                    "Sat" to mapOf("MATH204" to 60, "CS201" to 30),      // 90m (1.5h)
                    "Sun" to mapOf("ENG215" to 45, "CS310" to 30)        // 75m (1.25h)
                )

                val recentSessions = tagFiltered.filter {
                    StudyDateRangeHelper.isSessionInRange(it.timestampMs, StudyDateRange.LAST_7_DAYS, semester, nowMs)
                }

                days.mapIndexed { index, dayKey ->
                    val isToday = (dayKey == currentDayKey)
                    val rawBreakdown = baseBreakdowns[dayKey] ?: emptyMap()
                    val dayBreakdown = mutableMapOf<String, Int>()

                    rawBreakdown.forEach { (sub, mins) ->
                        val scaled = if (tagFilter == "All") mins else maxOf(10, kotlin.math.round(mins * tagMultiplier).toInt())
                        dayBreakdown[sub] = scaled
                    }

                    val daySessions = recentSessions.filter { s ->
                        (isToday && s.dateDisplay.equals("Today", ignoreCase = true)) ||
                            s.dateDisplay.equals(dayKey, ignoreCase = true)
                    }
                    daySessions.forEach { s ->
                        val cur = dayBreakdown[s.courseCode] ?: 0
                        dayBreakdown[s.courseCode] = cur + s.durationMinutes
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
                        subjectBreakdown = dayBreakdown,
                        quickNote = notesMap[dayKey] ?: ""
                    )
                }
            }
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

    val previousWeeklyStudyTrends: StateFlow<List<DayStudyTrend>> = combine(
        allStudySessions,
        _selectedSessionTagFilter,
        _selectedDateRange,
        currentSemester
    ) { sessions, tagFilter, dateRange, semester ->
        val now = System.currentTimeMillis()
        val tagFiltered = sessions.filter {
            tagFilter == "All" || it.tag.equals(tagFilter, ignoreCase = true)
        }

        when (dateRange) {
            StudyDateRange.THIS_MONTH -> {
                // Compare against prior 30-day window (31..60 days ago)
                val priorWindowSessions = tagFiltered.filter {
                    val ageDays = ((now - it.timestampMs) / StudyDateRangeHelper.ONE_DAY_MS).toInt()
                    ageDays in 31..60
                }
                val priorSpecs = listOf(
                    Triple("Wk 1", "Prior Month Wk 1", 53..60),
                    Triple("Wk 2", "Prior Month Wk 2", 46..52),
                    Triple("Wk 3", "Prior Month Wk 3", 39..45),
                    Triple("Wk 4", "Prior Month Wk 4", 31..38)
                )
                priorSpecs.map { (key, label, range) ->
                    val bucket = priorWindowSessions.filter {
                        val ageDays = ((now - it.timestampMs) / StudyDateRangeHelper.ONE_DAY_MS).toInt()
                        ageDays in range
                    }
                    val breakdown = mutableMapOf<String, Int>()
                    bucket.forEach { s ->
                        breakdown[s.courseCode] = (breakdown[s.courseCode] ?: 0) + s.durationMinutes
                    }
                    val totalMins = breakdown.values.sum()
                    val topSub = breakdown.maxByOrNull { it.value }?.key ?: "CS201"
                    DayStudyTrend(
                        dayKey = key,
                        dayLabel = label,
                        totalMinutes = totalMins,
                        sessionCount = bucket.size,
                        primarySubject = topSub,
                        subjectBreakdown = breakdown
                    )
                }
            }
            StudyDateRange.SEMESTER -> {
                // Benchmark ~85% of current semester buckets for prior-term comparison
                val currentBuckets = StudyDateRangeHelper.buildSemesterMonthlyTrends(
                    tagFiltered.filter { StudyDateRangeHelper.isSessionInRange(it.timestampMs, StudyDateRange.SEMESTER, semester, now) },
                    semester,
                    now
                )
                currentBuckets.map { cur ->
                    val prevBreakdown = cur.subjectBreakdown.mapValues { (_, mins) ->
                        kotlin.math.round(mins * 0.85).toInt()
                    }
                    val totalMins = prevBreakdown.values.sum()
                    DayStudyTrend(
                        dayKey = cur.dayKey,
                        dayLabel = "Prior ${cur.dayLabel}",
                        totalMinutes = totalMins,
                        sessionCount = maxOf(0, cur.sessionCount - 1),
                        primarySubject = cur.primarySubject,
                        subjectBreakdown = prevBreakdown
                    )
                }
            }
            StudyDateRange.LAST_7_DAYS -> {
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                val fullDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

                val tagMultiplier = when (tagFilter) {
                    "Exam Prep" -> 0.40
                    "Homework" -> 0.35
                    "Research" -> 0.20
                    "Project" -> 0.20
                    "Revision" -> 0.15
                    else -> 1.0
                }

                // Previous week benchmark baseline (consistent prior baseline)
                val prevBaseBreakdowns = mapOf(
                    "Mon" to mapOf("CS201" to 60, "MATH204" to 60),      // 120m (2.0h)
                    "Tue" to mapOf("CS310" to 90, "CS201" to 60),        // 150m (2.5h)
                    "Wed" to mapOf("MATH204" to 120, "PHY102" to 60),    // 180m (3.0h)
                    "Thu" to mapOf("CS201" to 90, "CS310" to 75),        // 165m (2.75h)
                    "Fri" to mapOf("PHY102" to 75, "ENG215" to 45),      // 120m (2.0h)
                    "Sat" to mapOf("MATH204" to 30, "CS201" to 30),      // 60m (1.0h)
                    "Sun" to mapOf("ENG215" to 45)                       // 45m (0.75h)
                )

                val sevenDaysAgo = now - (7L * 24 * 3600 * 1000)
                val fourteenDaysAgo = now - (14L * 24 * 3600 * 1000)
                val pastSessions = tagFiltered.filter {
                    it.timestampMs in fourteenDaysAgo..sevenDaysAgo
                }

                days.mapIndexed { index, dayKey ->
                    val rawBreakdown = prevBaseBreakdowns[dayKey] ?: emptyMap()
                    val dayBreakdown = mutableMapOf<String, Int>()

                    rawBreakdown.forEach { (sub, mins) ->
                        val scaled = if (tagFilter == "All") mins else maxOf(10, kotlin.math.round(mins * tagMultiplier).toInt())
                        dayBreakdown[sub] = scaled
                    }

                    pastSessions.forEach { s ->
                        val cur = dayBreakdown[s.courseCode] ?: 0
                        dayBreakdown[s.courseCode] = cur + (s.durationMinutes / 7)
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
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val previousWeekSubjectStats: StateFlow<List<SubjectStudyStats>> = combine(
        previousWeeklyStudyTrends,
        courses
    ) { prevTrends, courseList ->
        val subjectMins = mutableMapOf<String, Int>()
        prevTrends.forEach { day ->
            day.subjectBreakdown.forEach { (sub, mins) ->
                subjectMins[sub] = (subjectMins[sub] ?: 0) + mins
            }
        }
        val allCodes = (courseList.map { it.code } + subjectMins.keys).distinct()
        allCodes.map { code ->
            val mins = subjectMins[code] ?: 0
            SubjectStudyStats(
                courseCode = code,
                totalMinutes = mins,
                sessionCount = maxOf(0, mins / 45)
            )
        }.sortedByDescending { it.totalMinutes }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weekComparisonSummary: StateFlow<WeekComparisonSummary> = combine(
        weeklyStudyTrends,
        previousWeeklyStudyTrends
    ) { curTrends, prevTrends ->
        val curMins = curTrends.sumOf { it.totalMinutes }
        val prevMins = prevTrends.sumOf { it.totalMinutes }
        val deltaMins = curMins - prevMins
        val pct = if (prevMins > 0) ((deltaMins.toDouble() / prevMins.toDouble()) * 100.0) else 0.0
        WeekComparisonSummary(
            currentWeekTotalMinutes = curMins,
            previousWeekTotalMinutes = prevMins,
            deltaMinutes = deltaMins,
            percentChange = Math.round(pct * 10.0) / 10.0,
            isIncreased = deltaMins >= 0
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeekComparisonSummary())

    // 4-Week Mastery Trends from Room Database
    val subjectMasteryRecords: StateFlow<Map<String, List<SubjectMasteryEntity>>> = subjectMasteryDao
        .getAllMasteryRecords()
        .map { records ->
            records.groupBy { it.courseCode }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun getDefaultMasteryForCourse(courseCode: String): List<SubjectMasteryEntity> {
        val nowMs = System.currentTimeMillis()
        val oneWeekMs = 7L * 24 * 3600 * 1000
        val basePcts = when (courseCode) {
            "CS201" -> listOf(64.0, 72.0, 81.0, 92.0)
            "CS310" -> listOf(58.0, 68.0, 77.0, 88.0)
            "MATH204" -> listOf(52.0, 61.0, 71.0, 84.0)
            "PHY102" -> listOf(45.0, 53.0, 62.0, 70.0)
            "ENG215" -> listOf(70.0, 75.0, 80.0, 86.0)
            else -> listOf(50.0, 60.0, 70.0, 80.0)
        }
        return basePcts.mapIndexed { index, pct ->
            SubjectMasteryEntity(
                courseCode = courseCode,
                weekNumber = index + 1,
                weekLabel = "W${index + 1}",
                masteryPercentage = pct,
                studyHoursInWeek = 2.0 + (index * 0.8),
                recordedTimestampMs = nowMs - ((3 - index) * oneWeekMs)
            )
        }
    }

    private suspend fun ensureSubjectMasteryData() {
        if (subjectMasteryDao.getCount() == 0) {
            val allCodes = listOf("CS201", "CS310", "MATH204", "PHY102", "ENG215")
            val records = allCodes.flatMap { code ->
                getDefaultMasteryForCourse(code)
            }
            subjectMasteryDao.insertAllMasteryRecords(records)
        }
    }

    fun updateSubjectMastery(courseCode: String, weekNumber: Int, newMastery: Double, studyHours: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            subjectMasteryDao.updateMasteryScore(courseCode, weekNumber, newMastery, studyHours)
        }
    }

    fun quickAddThirtyMinutes(courseCode: String, dateDisplay: String = "Today") {
        val courseName = courses.value.firstOrNull { it.code == courseCode }?.name ?: "Course $courseCode"
        val activeTag = _selectedSessionTagFilter.value.takeIf { it != "All" } ?: "Exam Prep"
        recordStudySession(
            courseCode = courseCode,
            subjectName = courseName,
            durationMinutes = 30,
            mode = "FOCUS",
            notes = "Quick Add +30m ($dateDisplay)",
            tag = activeTag,
            dateDisplay = dateDisplay
        )
    }

    fun recordStudySession(
        courseCode: String,
        subjectName: String,
        durationMinutes: Int,
        mode: String,
        notes: String = "",
        tag: String = "Exam Prep",
        dateDisplay: String = "Today"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            studySessionDao.insertSession(
                StudySessionEntity(
                    courseCode = courseCode,
                    subjectName = subjectName,
                    durationMinutes = durationMinutes,
                    timestampMs = System.currentTimeMillis(),
                    dateDisplay = dateDisplay,
                    mode = mode,
                    notes = notes,
                    tag = tag
                )
            )
            // Dynamically boost current week's mastery based on study logging
            val currentList = subjectMasteryDao.getAllMasteryRecordsList().filter { it.courseCode == courseCode }
            val w4 = currentList.firstOrNull { it.weekNumber == 4 }
            if (w4 != null) {
                val newHours = w4.studyHoursInWeek + (durationMinutes / 60.0)
                val newMastery = (w4.masteryPercentage + (durationMinutes / 50.0 * 1.5)).coerceAtMost(99.0)
                subjectMasteryDao.updateMasteryScore(courseCode, 4, Math.round(newMastery * 10.0) / 10.0, Math.round(newHours * 10.0) / 10.0)
            }
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

    val defaultSubjectPriorities = mapOf(
        "CS201" to "High",
        "MATH204" to "Medium",
        "CS310" to "High",
        "PHY102" to "Medium",
        "ENG215" to "Low"
    )

    private val _subjectPriorities = MutableStateFlow<Map<String, String>>(
        loadPersistedSubjectPriorities()
    )
    val subjectPriorities: StateFlow<Map<String, String>> = _subjectPriorities.asStateFlow()

    fun getSubjectPriority(courseCode: String): String {
        return _subjectPriorities.value[courseCode] ?: defaultSubjectPriorities[courseCode] ?: "Medium"
    }

    fun updateSubjectPriority(courseCode: String, priority: String) {
        val validPriority = if (priority in listOf("High", "Medium", "Low")) priority else "Medium"
        val currentMap = _subjectPriorities.value.toMutableMap()
        currentMap[courseCode] = validPriority
        _subjectPriorities.value = currentMap
        persistSubjectPriorities(currentMap)
    }

    private fun loadPersistedSubjectPriorities(): Map<String, String> {
        val jsonString = analyticsPrefs.getString("subject_priorities_json", null)
        if (!jsonString.isNullOrEmpty()) {
            try {
                val json = org.json.JSONObject(jsonString)
                val map = mutableMapOf<String, String>()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    map[key] = json.optString(key, "Medium")
                }
                val result = defaultSubjectPriorities.toMutableMap()
                result.putAll(map)
                return result
            } catch (e: Exception) {
                // fallback to defaults
            }
        }
        return defaultSubjectPriorities
    }

    private fun persistSubjectPriorities(priorities: Map<String, String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = org.json.JSONObject()
                priorities.forEach { (code, priority) ->
                    json.put(code, priority)
                }
                analyticsPrefs.edit().putString("subject_priorities_json", json.toString()).apply()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    // Global Dark / Light Theme mode toggle for accessibility
    private val themePrefs = getApplication<Application>().getSharedPreferences("campus_life_theme_prefs", android.content.Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow<Boolean?>(
        if (themePrefs.contains("is_dark_mode")) themePrefs.getBoolean("is_dark_mode", false) else null
    )
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    fun toggleTheme(systemIsDark: Boolean = false) {
        val current = _isDarkMode.value ?: systemIsDark
        val newMode = !current
        _isDarkMode.value = newMode
        viewModelScope.launch(Dispatchers.IO) {
            themePrefs.edit().putBoolean("is_dark_mode", newMode).apply()
        }
    }

    fun setDarkMode(dark: Boolean) {
        _isDarkMode.value = dark
        viewModelScope.launch(Dispatchers.IO) {
            themePrefs.edit().putBoolean("is_dark_mode", dark).apply()
        }
    }

    // Compare Subjects overlay state within Subject Analytics
    private val _compareSubjectsEnabled = MutableStateFlow(false)
    val compareSubjectsEnabled: StateFlow<Boolean> = _compareSubjectsEnabled.asStateFlow()

    private val _compareSubjectA = MutableStateFlow("CS201")
    val compareSubjectA: StateFlow<String> = _compareSubjectA.asStateFlow()

    private val _compareSubjectB = MutableStateFlow("MATH204")
    val compareSubjectB: StateFlow<String> = _compareSubjectB.asStateFlow()

    fun toggleCompareSubjects(enabled: Boolean? = null) {
        val next = enabled ?: !_compareSubjectsEnabled.value
        _compareSubjectsEnabled.value = next
        if (next) {
            ensureDistinctCompareSubjects()
        }
    }

    fun selectCompareSubjectA(courseCode: String) {
        if (courseCode == _compareSubjectB.value) {
            _compareSubjectB.value = _compareSubjectA.value
        }
        _compareSubjectA.value = courseCode
    }

    fun selectCompareSubjectB(courseCode: String) {
        if (courseCode == _compareSubjectA.value) {
            _compareSubjectA.value = _compareSubjectB.value
        }
        _compareSubjectB.value = courseCode
    }

    fun swapCompareSubjects() {
        val prevA = _compareSubjectA.value
        val prevB = _compareSubjectB.value
        _compareSubjectA.value = prevB
        _compareSubjectB.value = prevA
    }

    private fun ensureDistinctCompareSubjects() {
        val available = studyStatsPerSubject.value.map { it.courseCode }
            .ifEmpty { courses.value.map { it.code } }
            .ifEmpty { listOf("CS201", "MATH204", "CS310", "PHY102", "ENG215") }
        if (available.size >= 2) {
            if (_compareSubjectA.value !in available) {
                _compareSubjectA.value = available[0]
            }
            if (_compareSubjectB.value !in available || _compareSubjectB.value == _compareSubjectA.value) {
                _compareSubjectB.value = available.first { it != _compareSubjectA.value }
            }
        }
    }

    val subjectComparisonReport: StateFlow<SubjectComparisonReport?> = combine(
        combine(studyStatsPerSubject, weeklyStudyTrends, _selectedDateRange) { stats, trends, range ->
            Triple(stats, trends, range)
        },
        combine(_subjectWeeklyStudyGoals, subjectMasteryRecords, _compareSubjectA, _compareSubjectB) { goals, mastery, subA, subB ->
            listOf(goals, mastery, subA, subB)
        }
    ) { (stats, trends, range), config ->
        @Suppress("UNCHECKED_CAST")
        val goals = config[0] as Map<String, Double>
        @Suppress("UNCHECKED_CAST")
        val mastery = config[1] as Map<String, List<SubjectMasteryEntity>>
        val rawA = config[2] as String
        val rawB = config[3] as String

        val availableCodes = stats.map { it.courseCode }
            .ifEmpty { defaultSubjectWeeklyGoals.keys.toList() }
        if (availableCodes.size < 2) {
            null
        } else {
            val codeA = if (rawA in availableCodes) rawA else availableCodes[0]
            val codeB = if (rawB in availableCodes && rawB != codeA) rawB else availableCodes.first { it != codeA }
            SubjectComparisonHelper.buildComparisonReport(
                courseCodeA = codeA,
                courseCodeB = codeB,
                statsList = stats,
                trendsList = trends,
                goalsMap = goals,
                defaultGoalsMap = defaultSubjectWeeklyGoals,
                masteryMap = mastery,
                defaultMasteryProvider = ::getDefaultMasteryForCourse,
                dateRange = range
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Monthly 4-Week Trend Line view mode state
    private val _monthlySubjectFilter = MutableStateFlow<String?>(null)
    val monthlySubjectFilter: StateFlow<String?> = _monthlySubjectFilter.asStateFlow()

    fun selectMonthlySubjectFilter(courseCode: String?) {
        _monthlySubjectFilter.value = courseCode
    }

    val monthlyTrendReport: StateFlow<MonthlyTrendReport> = combine(
        allStudySessions,
        _selectedSessionTagFilter,
        _monthlySubjectFilter
    ) { sessions, tagFilter, subjectFilter ->
        val tagFiltered = if (tagFilter == "All") {
            sessions
        } else {
            sessions.filter { it.tag.equals(tagFilter, ignoreCase = true) }
        }
        MonthlyTrendHelper.buildFourWeekReport(
            sessions = tagFiltered,
            subjectFilter = subjectFilter,
            nowMs = System.currentTimeMillis()
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MonthlyTrendHelper.buildFourWeekReport(emptyList())
    )

    // Subject Analytics Push Notification Alert state (alerts when falling behind custom weekly goals)
    private val _subjectGoalAlertsEnabled = MutableStateFlow(
        com.example.worker.SubjectGoalNotificationHelper.isAlertsEnabled(getApplication())
    )
    val subjectGoalAlertsEnabled: StateFlow<Boolean> = _subjectGoalAlertsEnabled.asStateFlow()

    val subjectGoalAlertReport: StateFlow<SubjectGoalAlertReport> = combine(
        courses,
        allStudySessions,
        _subjectWeeklyStudyGoals
    ) { courseList, sessions, goals ->
        SubjectGoalAlertHelper.evaluateWeeklySubjectGoals(
            courses = courseList,
            sessions = sessions,
            goalsMap = goals,
            defaultGoalsMap = defaultSubjectWeeklyGoals,
            nowMs = System.currentTimeMillis()
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SubjectGoalAlertHelper.evaluateWeeklySubjectGoals(
            courses = emptyList(),
            sessions = emptyList(),
            goalsMap = defaultSubjectWeeklyGoals,
            defaultGoalsMap = defaultSubjectWeeklyGoals
        )
    )

    fun setSubjectGoalAlertsEnabled(enabled: Boolean, triggerImmediateCheck: Boolean = true) {
        _subjectGoalAlertsEnabled.value = enabled
        val appContext = getApplication<Application>()
        com.example.worker.SubjectGoalNotificationHelper.setAlertsEnabled(appContext, enabled)
        if (enabled && triggerImmediateCheck) {
            com.example.worker.SubjectGoalNotificationHelper.triggerImmediateGoalCheck(
                appContext,
                forceNotify = true
            )
        }
    }

    fun sendSubjectGoalAlertCheckNow() {
        val appContext = getApplication<Application>()
        com.example.worker.SubjectGoalNotificationHelper.createNotificationChannel(appContext)
        com.example.worker.SubjectGoalNotificationHelper.triggerImmediateGoalCheck(
            appContext,
            forceNotify = true
        )
    }
}
