package com.example.data.repository

import com.example.data.local.TaskDao
import com.example.data.local.TaskEntity
import com.example.model.AcademicTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepository(private val dao: TaskDao) {

    val allTasks: Flow<List<AcademicTask>> = dao.getAllTasks().map { entities ->
        entities.map { it.toDomain() }
    }

    val pendingTasks: Flow<List<AcademicTask>> = dao.getPendingTasks().map { entities ->
        entities.map { it.toDomain() }
    }

    val upcomingDueTasks: Flow<List<AcademicTask>> = dao.getUpcomingDueTasks().map { entities ->
        entities.map { it.toDomain() }
    }

    val assignmentsAndExams: Flow<List<AcademicTask>> = dao.getAssignmentsAndExams().map { entities ->
        entities.map { it.toDomain() }
    }

    val completedTasks: Flow<List<AcademicTask>> = dao.getCompletedTasks().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getTasksForSubject(subjectCode: String): Flow<List<AcademicTask>> {
        return dao.getTasksForSubject(subjectCode).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getTasksByCategory(category: String): Flow<List<AcademicTask>> {
        return dao.getTasksByCategory(category).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun seedInitialTasksIfEmpty() {
        val count = dao.getTaskCount()
        if (count == 0) {
            val now = System.currentTimeMillis()
            val hour = 3600000L
            val day = 86400000L

            val initialTasks = listOf(
                TaskEntity(
                    id = "task-1",
                    title = "Computer Networks Lab Assignment 4",
                    subjectCode = "CS-306",
                    subjectName = "Computer Networks",
                    category = "Assignment",
                    type = "ASSIGNMENT",
                    dueDate = "Today",
                    dueTime = "5:00 PM",
                    priority = "URGENT",
                    weightage = "15% Lab",
                    isCompleted = false,
                    progressPercent = 65,
                    faculty = "Dr. R. Venkat",
                    note = "Packet trace analysis using Wireshark & TCP handshake capture",
                    dueTimestamp = now + (6 * hour),
                    createdAt = now - (2 * day)
                ),
                TaskEntity(
                    id = "task-2",
                    title = "DBMS Relational Calculus & B+ Trees Problem Set",
                    subjectCode = "CS-302",
                    subjectName = "Database Management Systems",
                    category = "Assignment",
                    type = "ASSIGNMENT",
                    dueDate = "Tomorrow, Oct 25",
                    dueTime = "11:59 PM",
                    priority = "HIGH",
                    weightage = "10% Internal",
                    isCompleted = false,
                    progressPercent = 40,
                    faculty = "Dr. Ananya Roy",
                    note = "Questions 4 to 8 on B+ Tree insertion overflow & division in relational algebra",
                    dueTimestamp = now + (30 * hour),
                    createdAt = now - (3 * day)
                ),
                TaskEntity(
                    id = "task-3",
                    title = "Mid-Term Examination: Operating Systems Core",
                    subjectCode = "CS-304",
                    subjectName = "Operating Systems",
                    category = "Exam",
                    type = "EXAM_PREP",
                    dueDate = "Oct 30",
                    dueTime = "09:30 AM",
                    priority = "URGENT",
                    weightage = "30% Mid-Sem",
                    isCompleted = false,
                    progressPercent = 50,
                    faculty = "Prof. K. Muralidhar",
                    note = "Chapters 3-7: Processes, Threads, CPU Scheduling & Deadlock Prevention",
                    dueTimestamp = now + (5 * day),
                    createdAt = now - (4 * day)
                ),
                TaskEntity(
                    id = "task-4",
                    title = "Operating Systems Semaphore Synchronization Project",
                    subjectCode = "CS-304",
                    subjectName = "Operating Systems",
                    category = "Project",
                    type = "PROJECT",
                    dueDate = "Nov 02",
                    dueTime = "11:59 PM",
                    priority = "MEDIUM",
                    weightage = "20% Project",
                    isCompleted = false,
                    progressPercent = 55,
                    faculty = "Prof. K. Muralidhar",
                    note = "Dining Philosophers & Producer-Consumer problem in C with POSIX pthreads",
                    dueTimestamp = now + (8 * day),
                    createdAt = now - (5 * day)
                ),
                TaskEntity(
                    id = "task-5",
                    title = "Full-Stack Microservices Architecture Milestone 1",
                    subjectCode = "CS-308",
                    subjectName = "Software Engineering & Agile",
                    category = "Project",
                    type = "PROJECT",
                    dueDate = "Nov 06",
                    dueTime = "5:00 PM",
                    priority = "MEDIUM",
                    weightage = "25% Capstone",
                    isCompleted = false,
                    progressPercent = 30,
                    faculty = "Prof. Neha Gupta",
                    note = "Sprint burndown metrics and Dockerized service deployment",
                    dueTimestamp = now + (12 * day),
                    createdAt = now - (1 * day)
                ),
                TaskEntity(
                    id = "task-6",
                    title = "Web Engineering Lab Practical & Viva Exam",
                    subjectCode = "CS-310",
                    subjectName = "Web Engineering",
                    category = "Exam",
                    type = "EXAM_PREP",
                    dueDate = "Nov 10",
                    dueTime = "02:00 PM",
                    priority = "HIGH",
                    weightage = "20% Exam",
                    isCompleted = false,
                    progressPercent = 20,
                    faculty = "Prof. S. Sengupta",
                    note = "Hands-on REST API implementation and security viva",
                    dueTimestamp = now + (16 * day),
                    createdAt = now - (2 * day)
                )
            )
            dao.insertTasks(initialTasks)
        }
    }

    suspend fun insertTask(task: AcademicTask) {
        dao.insertTask(
            TaskEntity(
                id = task.id,
                title = task.title.trim(),
                subjectCode = task.subjectCode.trim().uppercase(),
                subjectName = task.subjectName.trim(),
                category = task.category,
                type = task.type,
                dueDate = task.dueDate.trim(),
                dueTime = task.dueTime.trim(),
                priority = task.priority,
                weightage = task.weightage.trim(),
                isCompleted = task.isCompleted,
                progressPercent = task.progressPercent,
                faculty = task.faculty.trim(),
                note = task.note.trim(),
                dueTimestamp = task.dueTimestamp,
                createdAt = task.createdAt
            )
        )
    }

    suspend fun setTaskCompleted(id: String, isCompleted: Boolean) {
        dao.setTaskCompleted(id, isCompleted)
    }

    suspend fun toggleTaskCompletion(id: String): Boolean {
        return dao.toggleTaskCompletion(id)
    }

    suspend fun deleteTask(id: String) {
        dao.deleteTaskById(id)
    }

    suspend fun updateTaskDueDate(id: String, dueDate: String, dueTimestamp: Long) {
        dao.updateTaskDueDate(id, dueDate.trim(), dueTimestamp)
    }
}
