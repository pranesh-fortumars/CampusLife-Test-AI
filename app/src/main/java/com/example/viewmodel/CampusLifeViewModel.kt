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
}

