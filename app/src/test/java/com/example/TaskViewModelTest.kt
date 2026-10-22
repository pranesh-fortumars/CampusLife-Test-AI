package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.TaskEntity
import com.example.model.AcademicTask
import com.example.viewmodel.TaskViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TaskViewModelTest {

    private lateinit var context: Context
    private lateinit var appDatabase: AppDatabase
    private lateinit var viewModel: TaskViewModel

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        appDatabase = AppDatabase.getDatabase(context)
        val taskDao = appDatabase.taskDao()
        viewModel = TaskViewModel(taskDao)
    }

    @Test
    fun testViewModelFactoryCreation() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val factory = TaskViewModel.provideFactory(application)
        val vm = factory.create(TaskViewModel::class.java)
        assertNotNull(vm)
        assertNotNull(vm.tasks)
    }

    @Test
    fun testAddAndRetrieveTaskEntity() = runBlocking {
        val taskId = "test-task-${System.currentTimeMillis()}"
        val entity = TaskEntity(
            id = taskId,
            title = "Implement Operating Systems Scheduler",
            subjectCode = "CS-304",
            subjectName = "Operating Systems",
            category = "Project",
            type = "PROJECT",
            dueDate = "Oct 30",
            dueTime = "11:59 PM",
            priority = "URGENT",
            weightage = "20% Project",
            isCompleted = false,
            note = "Round Robin scheduler with priority inheritance"
        )

        viewModel.addTask(entity)

        // Wait/poll until task appears in StateFlow
        val allTasks = viewModel.tasks.first { list -> list.any { it.id == taskId } }
        val retrieved = allTasks.find { it.id == taskId }

        assertNotNull(retrieved)
        assertEquals("Implement Operating Systems Scheduler", retrieved?.title)
        assertEquals("CS-304", retrieved?.subjectCode)
        assertEquals("Project", retrieved?.category)
        assertFalse(retrieved?.isCompleted ?: true)
    }

    @Test
    fun testAddTaskWithParameters() = runBlocking {
        val uniqueTitle = "Computer Networks Socket Lab ${System.currentTimeMillis()}"
        viewModel.addTask(
            title = uniqueTitle,
            subjectCode = "CS-306",
            subjectName = "Computer Networks",
            category = "Assignment",
            type = "ASSIGNMENT",
            dueDate = "Tomorrow",
            dueTime = "5:00 PM",
            priority = "HIGH",
            note = "Multi-client chat server using TCP sockets"
        )

        val allTasks = viewModel.tasks.first { list -> list.any { it.title == uniqueTitle } }
        val found = allTasks.find { it.title == uniqueTitle }
        assertNotNull(found)
        assertEquals("CS-306", found?.subjectCode)
        assertEquals("Assignment", found?.category)
    }

    @Test
    fun testAddTaskFromAcademicTaskDomain() = runBlocking {
        val uniqueId = "domain-task-${System.currentTimeMillis()}"
        val domain = AcademicTask(
            id = uniqueId,
            title = "Database Normalization Exercises",
            subjectCode = "CS-302",
            subjectName = "DBMS",
            category = "Assignment",
            dueDate = "Nov 01",
            priority = "MEDIUM"
        )

        viewModel.addTask(domain)

        val academicList = viewModel.academicTasks.first { list -> list.any { it.id == uniqueId } }
        val found = academicList.find { it.id == uniqueId }
        assertNotNull(found)
        assertEquals("Database Normalization Exercises", found?.title)
        assertEquals("CS-302", found?.subjectCode)
    }

    @Test
    fun testUpdateTask() = runBlocking {
        val taskId = "update-task-${System.currentTimeMillis()}"
        val initial = TaskEntity(
            id = taskId,
            title = "Draft SE Capstone Proposal",
            subjectCode = "CS-308",
            subjectName = "Software Engineering",
            category = "Project",
            dueDate = "Next Week",
            priority = "MEDIUM",
            isCompleted = false
        )
        viewModel.addTask(initial)

        // Ensure task is saved
        viewModel.tasks.first { list -> list.any { it.id == taskId } }

        // Update task title and priority
        val updated = initial.copy(
            title = "Finalize SE Capstone Architecture Document",
            priority = "URGENT",
            progressPercent = 80
        )
        viewModel.updateTask(updated)

        val resultList = viewModel.tasks.first { list ->
            list.any { it.id == taskId && it.title == "Finalize SE Capstone Architecture Document" }
        }
        val found = resultList.find { it.id == taskId }
        assertNotNull(found)
        assertEquals("Finalize SE Capstone Architecture Document", found?.title)
        assertEquals("URGENT", found?.priority)
        assertEquals(80, found?.progressPercent)
    }

    @Test
    fun testToggleAndSetTaskCompleted() = runBlocking {
        val taskId = "complete-task-${System.currentTimeMillis()}"
        val task = TaskEntity(
            id = taskId,
            title = "Revise DBMS Indexing Algorithms",
            subjectCode = "CS-302",
            subjectName = "DBMS",
            category = "Exam",
            dueDate = "Nov 05",
            isCompleted = false
        )
        viewModel.addTask(task)
        viewModel.tasks.first { list -> list.any { it.id == taskId } }

        // Mark completed
        viewModel.setTaskCompleted(taskId, true)
        val afterComplete = viewModel.tasks.first { list -> list.any { it.id == taskId && it.isCompleted } }
        assertTrue(afterComplete.find { it.id == taskId }?.isCompleted == true)

        // Toggle back to incomplete
        viewModel.toggleTaskCompleted(taskId)
        val afterToggle = viewModel.tasks.first { list -> list.any { it.id == taskId && !it.isCompleted } }
        assertFalse(afterToggle.find { it.id == taskId }?.isCompleted ?: true)
    }

    @Test
    fun testDeleteTask() = runBlocking {
        val taskId = "delete-task-${System.currentTimeMillis()}"
        val task = TaskEntity(
            id = taskId,
            title = "Task to be Deleted",
            subjectCode = "CS-310",
            subjectName = "Web Engineering",
            category = "Assignment",
            dueDate = "Tomorrow"
        )
        viewModel.addTask(task)
        viewModel.tasks.first { list -> list.any { it.id == taskId } }

        // Delete by ID
        viewModel.deleteTask(taskId)

        val afterDelete = viewModel.tasks.first { list -> list.none { it.id == taskId } }
        assertNull(afterDelete.find { it.id == taskId })
    }

    @Test
    fun testGetTasksForSubjectAndCategory() = runBlocking {
        val subCode = "TEST-SUBJECT-${System.currentTimeMillis()}"
        val task1 = TaskEntity(
            id = "sub-task-1-${System.currentTimeMillis()}",
            title = "Subject Task 1",
            subjectCode = subCode,
            subjectName = "Test Subject",
            category = "Exam",
            dueDate = "Today"
        )
        val task2 = TaskEntity(
            id = "sub-task-2-${System.currentTimeMillis()}",
            title = "Subject Task 2",
            subjectCode = subCode,
            subjectName = "Test Subject",
            category = "Assignment",
            dueDate = "Tomorrow"
        )
        viewModel.addTask(task1)
        viewModel.addTask(task2)

        val subjectTasks = viewModel.getTasksForSubject(subCode).first { it.size >= 2 }
        assertEquals(2, subjectTasks.size)

        val examTasks = viewModel.getTasksByCategory("Exam").first { list -> list.any { it.subjectCode == subCode } }
        assertTrue(examTasks.any { it.id == task1.id })
    }
}
