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

    fun toggleTaskCompletion(task: AcademicTask) {
        viewModelScope.launch {
            val newStatus = !task.isCompleted
            repository.setTaskCompleted(task.id, newStatus)
            _userMessage.value = if (newStatus) "Marked task as completed" else "Marked task as pending"
        }
    }

    fun setTaskCompleted(id: String, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.setTaskCompleted(id, isCompleted)
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

    fun clearUserMessage() {
        _userMessage.value = null
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
