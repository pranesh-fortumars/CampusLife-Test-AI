package com.example.data.repository

import com.example.data.local.AcademicProjectDao
import com.example.data.local.AcademicProjectEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for Academic Projects and Capstones.
 * Mediates data operations between the Room database and ViewModels.
 */
class AcademicProjectRepository(private val dao: AcademicProjectDao) {

    val allProjects: Flow<List<AcademicProjectEntity>> = dao.getAllProjects()
    val activeProjects: Flow<List<AcademicProjectEntity>> = dao.getActiveProjects()
    val completedProjects: Flow<List<AcademicProjectEntity>> = dao.getCompletedProjects()

    suspend fun insertProject(project: AcademicProjectEntity) {
        dao.insertProject(project)
    }

    suspend fun updateProject(project: AcademicProjectEntity) {
        dao.updateProject(project)
    }

    suspend fun toggleProjectCompletion(id: String): Boolean {
        return dao.toggleProjectCompletion(id)
    }

    suspend fun deleteProjectById(id: String) {
        dao.deleteProjectById(id)
    }

    suspend fun seedInitialProjectsIfEmpty() {
        val count = dao.getProjectCount()
        if (count == 0) {
            val now = System.currentTimeMillis()
            val day = 86400000L

            val initialProjects = listOf(
                AcademicProjectEntity(
                    id = "proj-1",
                    title = "Distributed In-Memory Cache with Raft Consensus",
                    subjectCode = "CS-302",
                    subjectName = "Database Management Systems",
                    description = "High-performance partitioned key-value store with leader election and log replication.",
                    techStack = "Kotlin, Netty, Raft Consensus, Docker",
                    deadlineDate = "Nov 12, 2026",
                    dueTimestamp = now + (18 * day),
                    milestonesCompleted = 3,
                    totalMilestones = 5,
                    status = "IN_PROGRESS",
                    weightage = "25% Major Capstone",
                    githubUrl = "https://github.com/rahul-dtu/raft-kv-store",
                    teamMembers = "Rahul Sharma (Lead), Sneha Patel, Vikram Joshi",
                    isCompleted = false,
                    createdAt = now - (10 * day)
                ),
                AcademicProjectEntity(
                    id = "proj-2",
                    title = "Process Scheduling & IPC Semaphore Visualizer",
                    subjectCode = "CS-304",
                    subjectName = "Operating Systems",
                    description = "Interactive desktop visualizer for Round Robin, SRTF, and dining philosophers synchronization.",
                    techStack = "C++, Qt6, POSIX Pthreads, Valgrind",
                    deadlineDate = "Nov 02, 2026",
                    dueTimestamp = now + (8 * day),
                    milestonesCompleted = 2,
                    totalMilestones = 3,
                    status = "IN_PROGRESS",
                    weightage = "20% Term Project",
                    githubUrl = "https://github.com/rahul-dtu/os-scheduler-sim",
                    teamMembers = "Rahul Sharma, Amit Verma",
                    isCompleted = false,
                    createdAt = now - (8 * day)
                ),
                AcademicProjectEntity(
                    id = "proj-3",
                    title = "Real-Time Campus Life Hub & Peer Network",
                    subjectCode = "CS-308",
                    subjectName = "Software Engineering & Agile",
                    description = "Android mobile app tracking class attendance, academic deadlines, and campus collaboration.",
                    techStack = "Kotlin, Jetpack Compose, Room, WorkManager",
                    deadlineDate = "Nov 20, 2026",
                    dueTimestamp = now + (26 * day),
                    milestonesCompleted = 4,
                    totalMilestones = 6,
                    status = "IN_PROGRESS",
                    weightage = "30% Capstone",
                    githubUrl = "https://github.com/rahul-dtu/campus-life-hub",
                    teamMembers = "Rahul Sharma (CR), Team Alpha",
                    isCompleted = false,
                    createdAt = now - (15 * day)
                ),
                AcademicProjectEntity(
                    id = "proj-4",
                    title = "Packet Sniffer & TCP Congestion Control Analyzer",
                    subjectCode = "CS-306",
                    subjectName = "Computer Networks",
                    description = "Raw socket packet dissection tool capturing TCP Reno vs Cubic flow throughput.",
                    techStack = "Python, Scapy, Wireshark, Matplotlib",
                    deadlineDate = "Oct 24, 2026",
                    dueTimestamp = now - (1 * day),
                    milestonesCompleted = 4,
                    totalMilestones = 4,
                    status = "COMPLETED",
                    weightage = "15% Lab Capstone",
                    githubUrl = "https://github.com/rahul-dtu/tcp-analyzer",
                    teamMembers = "Rahul Sharma, Priya Sen",
                    isCompleted = true,
                    createdAt = now - (20 * day)
                )
            )

            dao.insertProjects(initialProjects)
        }
    }
}
