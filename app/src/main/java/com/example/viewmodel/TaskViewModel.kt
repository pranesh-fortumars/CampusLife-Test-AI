package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.TaskDao
import com.example.data.local.TaskEntity
import com.example.data.repository.TaskRepository
import com.example.model.AcademicTask
import com.example.ui.screens.TaskSortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Dedicated TaskViewModel receiving injected TaskDao from the Room Database singleton provider.
 * Manages academic task state, filtering, sorting, and CRUD persistence.
 */
class TaskViewModel(
    private val taskDao: TaskDao,
    private val repository: TaskRepository = TaskRepository(taskDao)
) : ViewModel() {

    // Secondary constructor allowing direct initialization from context using the singleton provider
    constructor(context: Context) : this(AppDatabase.provideTaskDao(context))

    val allTasks: StateFlow<List<AcademicTask>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val tasks: StateFlow<List<AcademicTask>> = allTasks
    val academicTasks: StateFlow<List<AcademicTask>> = allTasks

    val pendingTasks: StateFlow<List<AcademicTask>> = repository.pendingTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val upcomingDueTasks: StateFlow<List<AcademicTask>> = repository.upcomingDueTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val assignmentsAndExams: StateFlow<List<AcademicTask>> = repository.assignmentsAndExams
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val completedTasks: StateFlow<List<AcademicTask>> = repository.completedTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val rawTaskEntities: StateFlow<List<TaskEntity>> = taskDao.getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedSort = MutableStateFlow(TaskSortOption.DUE_DATE)
    val selectedSort: StateFlow<TaskSortOption> = _selectedSort.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialTasksIfEmpty()
        }
    }

    fun setCategoryFilter(category: String) {
        _selectedCategory.value = category
    }

    fun setSortOption(sortOption: TaskSortOption) {
        _selectedSort.value = sortOption
    }

    /**
     * Toggles task completion by ID atomically via the repository.
     */
    fun toggleTaskCompletion(taskId: String) {
        viewModelScope.launch {
            val newStatus = repository.toggleTaskCompletion(taskId)
            _userMessage.value = if (newStatus) "Marked task as completed" else "Marked task as pending"
        }
    }

    /**
     * Toggles task completion for domain AcademicTask.
     */
    fun toggleTaskCompletion(task: AcademicTask) {
        toggleTaskCompletion(task.id)
    }

    /**
     * Toggles task completion for Room TaskEntity.
     */
    fun toggleTaskCompletion(taskEntity: TaskEntity) {
        toggleTaskCompletion(taskEntity.id)
    }

    fun setTaskCompleted(id: String, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.setTaskCompleted(id, isCompleted)
            _userMessage.value = if (isCompleted) "Marked task as completed" else "Marked task as pending"
        }
    }

    fun insertTask(task: AcademicTask) {
        viewModelScope.launch {
            repository.insertTask(task)
            _userMessage.value = "Task '${task.title}' saved"
        }
    }

    fun insertTask(taskEntity: TaskEntity) {
        viewModelScope.launch {
            taskDao.insertTask(taskEntity)
            _userMessage.value = "Task '${taskEntity.title}' saved"
        }
    }

    fun updateTask(taskEntity: TaskEntity) {
        viewModelScope.launch {
            taskDao.updateTask(taskEntity)
            _userMessage.value = "Task updated"
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            repository.deleteTask(id)
            _userMessage.value = "Task deleted"
        }
    }

    fun toggleTaskCompleted(id: String) {
        toggleTaskCompletion(id)
    }

    fun addTask(task: TaskEntity) {
        insertTask(task)
    }

    fun addTask(task: AcademicTask) {
        insertTask(task)
    }

    fun addTask(
        title: String,
        subjectCode: String,
        subjectName: String,
        category: String = "Assignment",
        type: String = "ASSIGNMENT",
        dueDate: String = "Upcoming",
        dueTime: String = "11:59 PM",
        priority: String = "HIGH",
        weightage: String = "",
        note: String = ""
    ) {
        val newTask = AcademicTask(
            id = java.util.UUID.randomUUID().toString(),
            title = title.trim(),
            subjectCode = subjectCode.trim().uppercase(),
            subjectName = subjectName.trim(),
            category = category,
            type = type,
            dueDate = dueDate.trim(),
            dueTime = dueTime.trim(),
            priority = priority,
            weightage = weightage.trim(),
            isCompleted = false,
            note = note.trim()
        )
        insertTask(newTask)
    }

    fun getTasksForSubject(subjectCode: String): kotlinx.coroutines.flow.Flow<List<AcademicTask>> {
        return repository.getTasksForSubject(subjectCode)
    }

    fun getTasksByCategory(category: String): kotlinx.coroutines.flow.Flow<List<AcademicTask>> {
        return repository.getTasksByCategory(category)
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun triggerUpcomingTasksReminder(context: Context) {
        com.example.worker.TaskNotificationScheduler.triggerImmediateDueCheck(context)
        _userMessage.value = "WorkManager checking upcoming deadlines..."
    }

    fun schedulePeriodicReminders(context: Context, intervalHours: Long = 4) {
        com.example.worker.TaskNotificationScheduler.schedulePeriodicTaskReminder(context, intervalHours)
    }

    companion object {
        /**
         * Factory provider to inject TaskDao into TaskViewModel.
         */
        fun provideFactory(taskDao: TaskDao): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
                        return TaskViewModel(taskDao) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }

        /**
         * Factory provider using the Room Database singleton provider.
         */
        fun provideFactory(context: Context): ViewModelProvider.Factory {
            val taskDao = AppDatabase.provideTaskDao(context)
            return provideFactory(taskDao)
        }
    }
}
