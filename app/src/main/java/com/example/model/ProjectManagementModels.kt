package com.example.model

import java.util.UUID

enum class GroupTaskStatus(val displayName: String, val emoji: String) {
    TODO("To Do", "⏳"),
    IN_PROGRESS("In Progress", "⚡"),
    COMPLETED("Completed", "✅")
}

enum class GroupTaskPriority(val displayName: String) {
    URGENT("Urgent"),
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low")
}

/**
 * Task assigned to a specific student group member within an academic project.
 */
data class ProjectGroupTask(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val title: String,
    val assignedMember: String,
    val status: GroupTaskStatus = GroupTaskStatus.TODO,
    val priority: GroupTaskPriority = GroupTaskPriority.HIGH,
    val dueDate: String,
    val milestoneTag: String = "M1"
)

/**
 * Milestone within an academic project with deadline and percentage completion.
 */
data class ProjectMilestoneItem(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val milestoneNumber: Int,
    val title: String,
    val description: String,
    val deadlineDate: String,
    val isCompleted: Boolean = false,
    val completionPercentage: Int = 0
)

/**
 * Comprehensive academic group project data model supporting group tasks,
 * deadlines, milestones, and team member tracking.
 */
data class AcademicGroupProject(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subjectCode: String,
    val subjectName: String,
    val description: String,
    val techStack: String,
    val finalDeadlineDate: String,
    val teamMembers: List<String>,
    val githubUrl: String = "",
    val weightage: String = "25% Evaluation",
    val milestones: List<ProjectMilestoneItem> = emptyList(),
    val groupTasks: List<ProjectGroupTask> = emptyList()
) {
    val completedMilestonesCount: Int
        get() = milestones.count { it.isCompleted || it.completionPercentage == 100 }

    val totalMilestonesCount: Int
        get() = milestones.size

    val milestoneCompletionPercentage: Int
        get() = if (milestones.isEmpty()) 0 else {
            val total = milestones.sumOf { it.completionPercentage }
            total / milestones.size
        }

    val completedTasksCount: Int
        get() = groupTasks.count { it.status == GroupTaskStatus.COMPLETED }

    val totalTasksCount: Int
        get() = groupTasks.size

    val taskCompletionPercentage: Int
        get() = if (groupTasks.isEmpty()) 0 else ((completedTasksCount.toFloat() / groupTasks.size.toFloat()) * 100).toInt()
}

/**
 * Initial sample project data with group tasks, deadlines, and milestone completion percentages.
 */
object ProjectManagementMockData {
    val defaultProjects: List<AcademicGroupProject> = listOf(
        AcademicGroupProject(
            id = "proj-1",
            title = "Cloud Microservices Raft Orchestrator",
            subjectCode = "CS-301",
            subjectName = "Distributed Systems",
            description = "High-availability distributed consensus cluster with Raft leader election and container orchestration.",
            techStack = "Go, Docker, Kubernetes, gRPC, etcd",
            finalDeadlineDate = "Nov 14, 2026",
            teamMembers = listOf("Tanmay G. (Lead)", "Priya N.", "Rahul K."),
            githubUrl = "https://github.com/campus-team/raft-orchestrator",
            weightage = "25% Internal Evaluation",
            milestones = listOf(
                ProjectMilestoneItem(
                    projectId = "proj-1",
                    milestoneNumber = 1,
                    title = "Architecture & Consensus Spec",
                    description = "Formal system design document and Raft state machine specifications.",
                    deadlineDate = "Oct 24, 2026",
                    isCompleted = true,
                    completionPercentage = 100
                ),
                ProjectMilestoneItem(
                    projectId = "proj-1",
                    milestoneNumber = 2,
                    title = "RPC Protocol & Heartbeat Engine",
                    description = "gRPC client-server communication and election timeout handlers.",
                    deadlineDate = "Nov 02, 2026",
                    isCompleted = true,
                    completionPercentage = 100
                ),
                ProjectMilestoneItem(
                    projectId = "proj-1",
                    milestoneNumber = 3,
                    title = "Log Replication & Log Compaction",
                    description = "Persistent Write-Ahead Log (WAL) and snapshot sync across nodes.",
                    deadlineDate = "Nov 09, 2026",
                    isCompleted = false,
                    completionPercentage = 60
                ),
                ProjectMilestoneItem(
                    projectId = "proj-1",
                    milestoneNumber = 4,
                    title = "Benchmarking & Chaos Testing",
                    description = "Network partition fault injection and throughput profiling report.",
                    deadlineDate = "Nov 14, 2026",
                    isCompleted = false,
                    completionPercentage = 0
                )
            ),
            groupTasks = listOf(
                ProjectGroupTask(
                    projectId = "proj-1",
                    title = "Implement AppendEntries RPC and log index validation",
                    assignedMember = "Tanmay G.",
                    status = GroupTaskStatus.COMPLETED,
                    priority = GroupTaskPriority.HIGH,
                    dueDate = "Nov 02",
                    milestoneTag = "M2"
                ),
                ProjectGroupTask(
                    projectId = "proj-1",
                    title = "Build persistent WAL disk snapshot serialization",
                    assignedMember = "Priya N.",
                    status = GroupTaskStatus.IN_PROGRESS,
                    priority = GroupTaskPriority.URGENT,
                    dueDate = "Nov 07",
                    milestoneTag = "M3"
                ),
                ProjectGroupTask(
                    projectId = "proj-1",
                    title = "Configure Docker Compose 5-node cluster networking",
                    assignedMember = "Rahul K.",
                    status = GroupTaskStatus.IN_PROGRESS,
                    priority = GroupTaskPriority.HIGH,
                    dueDate = "Nov 08",
                    milestoneTag = "M3"
                ),
                ProjectGroupTask(
                    projectId = "proj-1",
                    title = "Write Chaos Mesh network latency test suite",
                    assignedMember = "Tanmay G.",
                    status = GroupTaskStatus.TODO,
                    priority = GroupTaskPriority.MEDIUM,
                    dueDate = "Nov 12",
                    milestoneTag = "M4"
                )
            )
        ),
        AcademicGroupProject(
            id = "proj-2",
            title = "Smart Campus Library IoT Seat Navigator",
            subjectCode = "CS-302",
            subjectName = "Database Management Systems & IoT",
            description = "Real-time library seat occupancy tracker with pressure sensors, RFID check-in, and PostgreSQL backend.",
            techStack = "Kotlin, PostgreSQL, MQTT, Node.js, ESP32",
            finalDeadlineDate = "Nov 22, 2026",
            teamMembers = listOf("Kabir V. (Lead)", "Aarav S.", "Sneha M."),
            githubUrl = "https://github.com/campus-team/smart-library-iot",
            weightage = "30% Capstone Lab",
            milestones = listOf(
                ProjectMilestoneItem(
                    projectId = "proj-2",
                    milestoneNumber = 1,
                    title = "Hardware Sensor Breadboard Setup",
                    description = "ESP32 microcontrollers and load cell sensor calibration.",
                    deadlineDate = "Oct 28, 2026",
                    isCompleted = true,
                    completionPercentage = 100
                ),
                ProjectMilestoneItem(
                    projectId = "proj-2",
                    milestoneNumber = 2,
                    title = "PostgreSQL Spatial Schema & Indexing",
                    description = "Tables for study zones, seats, reservations, and real-time sensor pings.",
                    deadlineDate = "Nov 05, 2026",
                    isCompleted = true,
                    completionPercentage = 100
                ),
                ProjectMilestoneItem(
                    projectId = "proj-2",
                    milestoneNumber = 3,
                    title = "WebSocket & MQTT Gateway",
                    description = "Stream live sensor readings with under 200ms latency to the campus client.",
                    deadlineDate = "Nov 14, 2026",
                    isCompleted = false,
                    completionPercentage = 40
                ),
                ProjectMilestoneItem(
                    projectId = "proj-2",
                    milestoneNumber = 4,
                    title = "Mobile App Interactive Heatmap UI",
                    description = "Floor plan view showing available, occupied, and reserved seats.",
                    deadlineDate = "Nov 22, 2026",
                    isCompleted = false,
                    completionPercentage = 10
                )
            ),
            groupTasks = listOf(
                ProjectGroupTask(
                    projectId = "proj-2",
                    title = "Optimize PostGIS spatial query for nearest quiet desk",
                    assignedMember = "Kabir V.",
                    status = GroupTaskStatus.COMPLETED,
                    priority = GroupTaskPriority.HIGH,
                    dueDate = "Nov 04",
                    milestoneTag = "M2"
                ),
                ProjectGroupTask(
                    projectId = "proj-2",
                    title = "Bridge MQTT sensor queue to PostgreSQL connection pool",
                    assignedMember = "Aarav S.",
                    status = GroupTaskStatus.IN_PROGRESS,
                    priority = GroupTaskPriority.URGENT,
                    dueDate = "Nov 11",
                    milestoneTag = "M3"
                ),
                ProjectGroupTask(
                    projectId = "proj-2",
                    title = "Design SVG library floor plan seating canvas",
                    assignedMember = "Sneha M.",
                    status = GroupTaskStatus.IN_PROGRESS,
                    priority = GroupTaskPriority.HIGH,
                    dueDate = "Nov 16",
                    milestoneTag = "M4"
                ),
                ProjectGroupTask(
                    projectId = "proj-2",
                    title = "RFID student ID scanner tap-in integration",
                    assignedMember = "Kabir V.",
                    status = GroupTaskStatus.TODO,
                    priority = GroupTaskPriority.MEDIUM,
                    dueDate = "Nov 19",
                    milestoneTag = "M4"
                )
            )
        )
    )
}
