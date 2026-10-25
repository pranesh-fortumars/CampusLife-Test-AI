package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AcademicProjectEntity
import com.example.model.AcademicTask
import com.example.model.Subject
import com.example.ui.theme.*
import com.example.worker.TaskNotificationScheduler

/**
 * Dedicated Material 3 UI component to view and manage academic projects and deadlines
 * persisted in the Room database.
 */
@Composable
fun AcademicProjectsAndDeadlinesComponent(
    tasks: List<AcademicTask>,
    projects: List<AcademicProjectEntity>,
    subjects: List<Subject> = emptyList(),
    onToggleTaskComplete: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onToggleProjectComplete: (String) -> Unit,
    onDeleteProject: (String) -> Unit,
    onAddTask: (
        title: String,
        subjectCode: String,
        subjectName: String,
        category: String,
        type: String,
        dueDate: String,
        dueTime: String,
        priority: String,
        weightage: String,
        note: String
    ) -> Unit,
    onAddProject: (
        title: String,
        subjectCode: String,
        subjectName: String,
        description: String,
        techStack: String,
        deadlineDate: String,
        totalMilestones: Int,
        weightage: String,
        githubUrl: String,
        teamMembers: String
    ) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Academic Projects & Deadlines",
    subtitle: String = "Room Database persistent workload tracking & capstone milestones"
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("All Deadlines") } // "All Deadlines", "Projects", "Assignments", "Exams", "Completed"
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val pendingTasks = remember(tasks) { tasks.filter { !it.isCompleted } }
    val activeProjects = remember(projects) { projects.filter { !it.isCompleted } }
    val urgentTasks = remember(tasks) {
        tasks.filter { !it.isCompleted && (it.priority == "URGENT" || it.dueDate.contains("Today", ignoreCase = true)) }
    }

    val totalItems = tasks.size + projects.size
    val completedItems = tasks.count { it.isCompleted } + projects.count { it.isCompleted }
    val overallProgress = if (totalItems == 0) 0f else completedItems.toFloat() / totalItems.toFloat()

    val filteredTasks = remember(tasks, selectedTab, searchQuery) {
        var list = when (selectedTab) {
            "All Deadlines" -> tasks.filter { !it.isCompleted }
            "Assignments" -> tasks.filter { it.category.equals("Assignment", ignoreCase = true) }
            "Exams" -> tasks.filter { it.category.equals("Exam", ignoreCase = true) }
            "Projects" -> tasks.filter { it.category.equals("Project", ignoreCase = true) }
            "Completed" -> tasks.filter { it.isCompleted }
            else -> tasks
        }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) ||
                it.subjectCode.lowercase().contains(q) ||
                it.subjectName.lowercase().contains(q) ||
                it.note.lowercase().contains(q)
            }
        }
        list.sortedBy { it.dueTimestamp }
    }

    val filteredProjects = remember(projects, selectedTab, searchQuery) {
        var list = when (selectedTab) {
            "All Deadlines", "Projects" -> projects.filter { !it.isCompleted }
            "Completed" -> projects.filter { it.isCompleted }
            else -> emptyList()
        }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) ||
                it.subjectCode.lowercase().contains(q) ||
                it.techStack.lowercase().contains(q) ||
                it.description.lowercase().contains(q)
            }
        }
        list.sortedBy { it.dueTimestamp }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("academic_projects_and_deadlines_component")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Title, Subtitle & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TaskAlt,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = {
                            TaskNotificationScheduler.triggerImmediateDueCheck(context)
                            Toast.makeText(context, "WorkManager: Checking upcoming deadlines...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("workmanager_reminder_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Trigger Reminder Check",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    FilledTonalButton(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = PrimaryContainer,
                            contentColor = Primary
                        ),
                        modifier = Modifier.testTag("open_add_project_deadline_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Summary Workload Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ACTIVE WORKLOAD",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${pendingTasks.size} Deadlines • ${activeProjects.size} Capstones",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (urgentTasks.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(100),
                                color = ErrorContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = Error, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${urgentTasks.size} Urgent Due",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = OnErrorContainer
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { overallProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${(overallProgress * 100).toInt()}% items completed",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$completedItems of $totalItems Done",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter by title, course code, tech stack...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("projects_deadlines_search")
            )

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val filters = listOf(
                    "All Deadlines" to "${pendingTasks.size + activeProjects.size}",
                    "Projects" to "${activeProjects.size}",
                    "Assignments" to "${tasks.count { it.category == "Assignment" && !it.isCompleted }}",
                    "Exams" to "${tasks.count { it.category == "Exam" && !it.isCompleted }}",
                    "Completed" to "$completedItems"
                )

                items(filters) { (tab, count) ->
                    val isSelected = selectedTab == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        label = {
                            Text(
                                text = "$tab ($count)",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(100),
                        modifier = Modifier.testTag("filter_tab_$tab")
                    )
                }
            }

            // Projects Section (if tab is All Deadlines or Projects)
            if (filteredProjects.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Academic Projects & Capstones (${filteredProjects.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Room Table: academic_projects",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    filteredProjects.forEach { project ->
                        AcademicProjectCard(
                            project = project,
                            onToggleComplete = { onToggleProjectComplete(project.id) },
                            onDelete = { onDeleteProject(project.id) }
                        )
                    }
                }
            }

            // Deadlines Section
            if (filteredTasks.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Course Deadlines & Tasks (${filteredTasks.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    filteredTasks.forEach { task ->
                        AcademicTaskDeadlineCard(
                            task = task,
                            onToggleComplete = { onToggleTaskComplete(task.id) },
                            onDelete = { onDeleteTask(task.id) }
                        )
                    }
                }
            }

            // Empty State
            if (filteredProjects.isEmpty() && filteredTasks.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No tasks or projects match \"$searchQuery\"." else "🎉 All academic deliverables and deadlines completed!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Modal Add Dialog
    if (showAddDialog) {
        AddProjectOrDeadlineDialog(
            subjects = subjects,
            onDismiss = { showAddDialog = false },
            onAddTask = { title, code, name, category, type, date, time, priority, weight, note ->
                onAddTask(title, code, name, category, type, date, time, priority, weight, note)
                showAddDialog = false
            },
            onAddProject = { title, code, name, desc, tech, deadline, milestones, weight, github, team ->
                onAddProject(title, code, name, desc, tech, deadline, milestones, weight, github, team)
                showAddDialog = false
            }
        )
    }
}

/**
 * Material 3 Card displaying an Academic Project deliverable.
 */
@Composable
fun AcademicProjectCard(
    project: AcademicProjectEntity,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (project.isCompleted) MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("project_card_${project.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Badges, Course Code & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SecondaryContainer
                    ) {
                        Text(
                            text = project.subjectCode,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = project.weightage,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = project.isCompleted,
                        onCheckedChange = { onToggleComplete() },
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("checkbox_project_${project.id}")
                    )
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("delete_project_${project.id}")
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            // Project Title
            Text(
                text = project.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textDecoration = if (project.isCompleted) TextDecoration.LineThrough else null
            )

            if (project.description.isNotBlank()) {
                Text(
                    text = project.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Milestones Progress
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Milestones: ${project.milestonesCompleted} of ${project.totalMilestones} Completed",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${project.progressPercentage}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (project.isCompleted) Tertiary else Primary
                    )
                }

                LinearProgressIndicator(
                    progress = { project.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (project.isCompleted) Tertiary else Primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            }

            // Tech Stack & Deadline Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Text(
                        text = project.techStack,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PrimaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Event, contentDescription = null, tint = Primary, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Due ${project.deadlineDate}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Material 3 Card displaying an Academic Task / Deadline item.
 */
@Composable
fun AcademicTaskDeadlineCard(
    task: AcademicTask,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUrgent = task.priority == "URGENT" || task.dueDate.contains("Today", ignoreCase = true)
    val priorityColor = when (task.priority.uppercase()) {
        "URGENT" -> Error
        "HIGH" -> Warning
        "MEDIUM" -> Primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            1.dp,
            if (isUrgent && !task.isCompleted) Error.copy(alpha = 0.4f) else OutlineVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_deadline_card_${task.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleComplete() },
                modifier = Modifier
                    .size(24.dp)
                    .testTag("checkbox_task_${task.id}")
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = task.subjectCode,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = priorityColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = task.priority,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "• ${task.weightage}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                )

                if (task.note.isNotBlank()) {
                    Text(
                        text = task.note,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Primary, modifier = Modifier.size(12.dp))
                    Text(
                        text = "${task.dueDate} at ${task.dueTime}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isUrgent) Error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("delete_task_${task.id}")
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

/**
 * Dialog to add either a new Task/Deadline or a Capstone Academic Project to the Room DB.
 */
@Composable
fun AddProjectOrDeadlineDialog(
    subjects: List<Subject>,
    onDismiss: () -> Unit,
    onAddTask: (String, String, String, String, String, String, String, String, String, String) -> Unit,
    onAddProject: (String, String, String, String, String, String, Int, String, String, String) -> Unit
) {
    var isProjectMode by remember { mutableStateOf(false) }

    // Common fields
    var title by remember { mutableStateOf("") }
    var selectedSubjectCode by remember { mutableStateOf(subjects.firstOrNull()?.code ?: "CS-302") }
    val selectedSubject = subjects.find { it.code == selectedSubjectCode }
    val subjectName = selectedSubject?.name ?: "Database Management Systems"

    // Task fields
    var category by remember { mutableStateOf("Assignment") }
    var dueDate by remember { mutableStateOf("Nov 05") }
    var dueTime by remember { mutableStateOf("11:59 PM") }
    var priority by remember { mutableStateOf("HIGH") }
    var weightage by remember { mutableStateOf("15% Internal") }
    var note by remember { mutableStateOf("") }

    // Project fields
    var description by remember { mutableStateOf("") }
    var techStack by remember { mutableStateOf("Kotlin, Compose, Room") }
    var totalMilestones by remember { mutableIntStateOf(4) }
    var githubUrl by remember { mutableStateOf("https://github.com/rahul-dtu/new-project") }
    var teamMembers by remember { mutableStateOf("Rahul Sharma") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (isProjectMode) "New Academic Project" else "New Task / Deadline",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Persists directly to Room SQLite database",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_item_dialog_content"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Segmented Mode Selector
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = !isProjectMode,
                        onClick = { isProjectMode = false },
                        label = { Text("Task / Deadline") },
                        modifier = Modifier.weight(1f).testTag("select_task_mode_chip")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    FilterChip(
                        selected = isProjectMode,
                        onClick = { isProjectMode = true },
                        label = { Text("Academic Project") },
                        modifier = Modifier.weight(1f).testTag("select_project_mode_chip")
                    )
                }

                // Course Code Selector Chips
                Text("Course:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val codes = if (subjects.isNotEmpty()) subjects.map { it.code } else listOf("CS-302", "CS-304", "CS-306", "CS-308")
                    items(codes) { code ->
                        val isSelected = selectedSubjectCode == code
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSubjectCode = code },
                            label = { Text(code, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isProjectMode) "Project Title" else "Task / Assignment Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_item_title_field")
                )

                if (isProjectMode) {
                    OutlinedTextField(
                        value = techStack,
                        onValueChange = { techStack = it },
                        label = { Text("Tech Stack (e.g. C++, ROS2 / Kotlin, Room)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description & Deliverables") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = dueDate,
                            onValueChange = { dueDate = it },
                            label = { Text("Due Date") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = dueTime,
                            onValueChange = { dueTime = it },
                            label = { Text("Time") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Notes / Submission instructions") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        if (isProjectMode) {
                            onAddProject(
                                title,
                                selectedSubjectCode,
                                subjectName,
                                description,
                                techStack,
                                dueDate,
                                totalMilestones,
                                weightage,
                                githubUrl,
                                teamMembers
                            )
                        } else {
                            onAddTask(
                                title,
                                selectedSubjectCode,
                                subjectName,
                                category,
                                if (category == "Exam") "EXAM_PREP" else "ASSIGNMENT",
                                dueDate,
                                dueTime,
                                priority,
                                weightage,
                                note
                            )
                        }
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_item_btn")
            ) {
                Text("Save to Room DB", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
