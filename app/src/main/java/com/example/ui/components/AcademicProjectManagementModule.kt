package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.ui.theme.*
import java.util.UUID

/**
 * Dedicated Project Management Module for academic group projects to track:
 * 1. Group tasks with assigned team members
 * 2. Project submission deadlines
 * 3. Milestone completion percentages
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicProjectManagementModule(
    initialProjects: List<AcademicGroupProject> = ProjectManagementMockData.defaultProjects,
    modifier: Modifier = Modifier
) {
    var projectsState by remember { mutableStateOf(initialProjects) }
    var selectedProjectId by remember {
        mutableStateOf(projectsState.firstOrNull()?.id ?: "proj-1")
    }

    val activeProject = remember(projectsState, selectedProjectId) {
        projectsState.find { it.id == selectedProjectId } ?: projectsState.first()
    }

    var selectedSection by remember { mutableStateOf("ALL") } // "ALL", "TASKS", "MILESTONES"
    var taskStatusFilter by remember { mutableStateOf<GroupTaskStatus?>(null) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddProjectDialog by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    val filteredGroupTasks = remember(activeProject.groupTasks, taskStatusFilter) {
        if (taskStatusFilter == null) activeProject.groupTasks
        else activeProject.groupTasks.filter { it.status == taskStatusFilter }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("academic_project_management_module")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Academic Project Management",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Group tasks, deadlines & milestone completion",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { showAddProjectDialog = true },
                    shape = RoundedCornerShape(100),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_academic_project_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Project", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }

            // Project Selector Carousel
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(projectsState) { proj ->
                    val isSelected = proj.id == selectedProjectId
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Primary else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, if (isSelected) Primary else OutlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clickable { selectedProjectId = proj.id }
                            .testTag("project_tab_${proj.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = proj.subjectCode,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Primary
                                )
                                Text(
                                    text = proj.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.widthIn(max = 160.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(100),
                                color = if (isSelected) Color.White.copy(alpha = 0.25f) else TertiaryContainer
                            ) {
                                Text(
                                    text = "${proj.milestoneCompletionPercentage}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Tertiary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Active Project Spotlight Overview Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Title & Deadline Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(4.dp), color = PrimaryContainer) {
                                    Text(
                                        text = activeProject.subjectCode,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = activeProject.subjectName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = activeProject.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Final Deadline Pill
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = TertiaryContainer,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    tint = Tertiary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(
                                        text = "Due: ${activeProject.finalDeadlineDate}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Tertiary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = activeProject.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Team Members & Tech Stack
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Team: ${activeProject.teamMembers.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Milestone Completion Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Milestones Completion: ${activeProject.completedMilestonesCount}/${activeProject.totalMilestonesCount} (${activeProject.milestoneCompletionPercentage}%)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Text(
                            text = "Tasks: ${activeProject.completedTasksCount}/${activeProject.totalTasksCount} Done",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (activeProject.milestoneCompletionPercentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(100)),
                        color = Primary,
                        trackColor = Primary.copy(alpha = 0.15f)
                    )
                }
            }

            // View Mode Segment: "All", "Group Tasks", "Milestones"
            Surface(
                shape = RoundedCornerShape(100),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("ALL" to "Full Overview", "TASKS" to "Group Tasks (${activeProject.totalTasksCount})", "MILESTONES" to "Milestones (${activeProject.totalMilestonesCount})").forEach { (secKey, secLabel) ->
                        val isSelected = selectedSection == secKey
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = if (isSelected) Primary else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedSection = secKey }
                                .testTag("section_tab_${secKey.lowercase()}")
                        ) {
                            Text(
                                text = secLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Section 1: Milestone Progressions (if ALL or MILESTONES)
            if (selectedSection == "ALL" || selectedSection == "MILESTONES") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Project Milestones & Deadlines",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${activeProject.milestoneCompletionPercentage}% Completed",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Tertiary
                        )
                    }

                    activeProject.milestones.forEachIndexed { index, milestone ->
                        MilestoneCard(
                            milestone = milestone,
                            onToggleComplete = {
                                val newStatus = !milestone.isCompleted
                                val newPercentage = if (newStatus) 100 else 50
                                projectsState = projectsState.map { proj ->
                                    if (proj.id == activeProject.id) {
                                        proj.copy(
                                            milestones = proj.milestones.map {
                                                if (it.id == milestone.id) it.copy(isCompleted = newStatus, completionPercentage = newPercentage) else it
                                            }
                                        )
                                    } else proj
                                }
                                feedbackMessage = if (newStatus) "Marked Milestone ${milestone.milestoneNumber} as Completed! 🎉" else "Updated Milestone ${milestone.milestoneNumber}"
                            },
                            index = index
                        )
                    }
                }
            }

            // Section 2: Group Tasks Tracker (if ALL or TASKS)
            if (selectedSection == "ALL" || selectedSection == "TASKS") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Group Tasks & Assignees",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(shape = RoundedCornerShape(100), color = PrimaryContainer) {
                                Text(
                                    text = "${activeProject.completedTasksCount}/${activeProject.totalTasksCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Button(
                            onClick = { showAddTaskDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("add_group_task_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Assign Task", fontSize = 11.sp)
                        }
                    }

                    // Status Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = taskStatusFilter == null,
                            onClick = { taskStatusFilter = null },
                            label = { Text("All Tasks", fontSize = 11.sp) }
                        )
                        GroupTaskStatus.values().forEach { status ->
                            FilterChip(
                                selected = taskStatusFilter == status,
                                onClick = {
                                    taskStatusFilter = if (taskStatusFilter == status) null else status
                                },
                                label = { Text("${status.emoji} ${status.displayName}", fontSize = 11.sp) }
                            )
                        }
                    }

                    // Task Cards List
                    filteredGroupTasks.forEachIndexed { index, task ->
                        GroupTaskItemCard(
                            task = task,
                            onCycleStatus = {
                                val nextStatus = when (task.status) {
                                    GroupTaskStatus.TODO -> GroupTaskStatus.IN_PROGRESS
                                    GroupTaskStatus.IN_PROGRESS -> GroupTaskStatus.COMPLETED
                                    GroupTaskStatus.COMPLETED -> GroupTaskStatus.TODO
                                }
                                projectsState = projectsState.map { proj ->
                                    if (proj.id == activeProject.id) {
                                        proj.copy(
                                            groupTasks = proj.groupTasks.map {
                                                if (it.id == task.id) it.copy(status = nextStatus) else it
                                            }
                                        )
                                    } else proj
                                }
                                feedbackMessage = "Updated '${task.title}' -> ${nextStatus.displayName}"
                            },
                            onDelete = {
                                projectsState = projectsState.map { proj ->
                                    if (proj.id == activeProject.id) {
                                        proj.copy(groupTasks = proj.groupTasks.filter { it.id != task.id })
                                    } else proj
                                }
                                feedbackMessage = "Removed task"
                            },
                            index = index
                        )
                    }
                }
            }

            // Feedback Message Bar
            AnimatedVisibility(visible = feedbackMessage != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TertiaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Tertiary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feedbackMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Tertiary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { feedbackMessage = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }

    // Modal Add Group Task Dialog
    if (showAddTaskDialog) {
        AddGroupTaskDialog(
            projectId = activeProject.id,
            teamMembers = activeProject.teamMembers,
            onDismiss = { showAddTaskDialog = false },
            onAdd = { newTask ->
                projectsState = projectsState.map { proj ->
                    if (proj.id == activeProject.id) {
                        proj.copy(groupTasks = proj.groupTasks + newTask)
                    } else proj
                }
                showAddTaskDialog = false
                feedbackMessage = "Assigned task to ${newTask.assignedMember}!"
            }
        )
    }

    // Modal Add Project Dialog
    if (showAddProjectDialog) {
        AddAcademicProjectDialog(
            onDismiss = { showAddProjectDialog = false },
            onAdd = { newProj ->
                projectsState = projectsState + newProj
                selectedProjectId = newProj.id
                showAddProjectDialog = false
                feedbackMessage = "Created academic project '${newProj.title}'!"
            }
        )
    }
}

/**
 * Individual Milestone card displaying deadline and completion percentage.
 */
@Composable
fun MilestoneCard(
    milestone: ProjectMilestoneItem,
    onToggleComplete: () -> Unit,
    index: Int
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, if (milestone.isCompleted) Tertiary else OutlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("milestone_card_$index")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = milestone.isCompleted,
                onCheckedChange = { onToggleComplete() },
                modifier = Modifier.testTag("milestone_check_$index")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "M${milestone.milestoneNumber}: ${milestone.title}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (milestone.isCompleted) Tertiary else MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(100),
                        color = if (milestone.isCompleted) TertiaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = "${milestone.completionPercentage}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (milestone.isCompleted) Tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = milestone.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Target Deadline: ${milestone.deadlineDate}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Individual Group Task Card showing assignee, priority, status pill, and one-tap status cycle.
 */
@Composable
fun GroupTaskItemCard(
    task: ProjectGroupTask,
    onCycleStatus: () -> Unit,
    onDelete: () -> Unit,
    index: Int
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("group_task_card_$index")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (task.priority) {
                            GroupTaskPriority.URGENT -> ErrorContainer
                            GroupTaskPriority.HIGH -> TertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceContainerHigh
                        }
                    ) {
                        Text(
                            text = task.priority.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (task.priority) {
                                GroupTaskPriority.URGENT -> Error
                                GroupTaskPriority.HIGH -> Tertiary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Milestone ${task.milestoneTag}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Assigned: ${task.assignedMember}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary,
                        fontSize = 11.sp
                    )
                    Text(" • Due ${task.dueDate}", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Status Pill (Tap to cycle To Do -> In Progress -> Completed)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (task.status) {
                    GroupTaskStatus.COMPLETED -> TertiaryContainer
                    GroupTaskStatus.IN_PROGRESS -> PrimaryContainer
                    GroupTaskStatus.TODO -> MaterialTheme.colorScheme.surfaceContainerHigh
                },
                modifier = Modifier
                    .clickable { onCycleStatus() }
                    .testTag("task_status_pill_$index")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(task.status.emoji, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = task.status.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (task.status) {
                            GroupTaskStatus.COMPLETED -> Tertiary
                            GroupTaskStatus.IN_PROGRESS -> Primary
                            GroupTaskStatus.TODO -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp).testTag("delete_group_task_$index")) {
                Icon(Icons.Default.Close, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

/**
 * Modal Dialog to assign a group task to a team member.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGroupTaskDialog(
    projectId: String,
    teamMembers: List<String>,
    onDismiss: () -> Unit,
    onAdd: (ProjectGroupTask) -> Unit
) {
    var taskTitle by remember { mutableStateOf("Design frontend UI mockups & flows") }
    var assignedMember by remember { mutableStateOf(teamMembers.firstOrNull() ?: "Priya N.") }
    var priority by remember { mutableStateOf(GroupTaskPriority.HIGH) }
    var dueDate by remember { mutableStateOf("Nov 10") }
    var milestoneTag by remember { mutableStateOf("M3") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_group_task_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Assign Group Task", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("Task Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_task_title")
                )

                // Assignee Selector Chips
                Text("Assign to Team Member:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(teamMembers) { member ->
                        val isSelected = assignedMember == member
                        FilterChip(
                            selected = isSelected,
                            onClick = { assignedMember = member },
                            label = { Text(member, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Priority & Milestone Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = milestoneTag,
                        onValueChange = { milestoneTag = it },
                        label = { Text("Milestone Tag") },
                        singleLine = true,
                        modifier = Modifier.weight(0.4f).testTag("input_milestone_tag")
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date") },
                        singleLine = true,
                        modifier = Modifier.weight(0.6f).testTag("input_task_due_date")
                    )
                }

                // Priority Segment
                Text("Priority:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    GroupTaskPriority.values().forEach { prio ->
                        val isSelected = priority == prio
                        FilterChip(
                            selected = isSelected,
                            onClick = { priority = prio },
                            label = { Text(prio.displayName, fontSize = 11.sp) }
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (taskTitle.isNotBlank()) {
                                onAdd(
                                    ProjectGroupTask(
                                        projectId = projectId,
                                        title = taskTitle.trim(),
                                        assignedMember = assignedMember.trim(),
                                        priority = priority,
                                        dueDate = dueDate.trim(),
                                        milestoneTag = milestoneTag.trim()
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier.testTag("save_group_task_btn")
                    ) {
                        Text("Assign Task")
                    }
                }
            }
        }
    }
}

/**
 * Modal Dialog to add a new academic group project.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAcademicProjectDialog(
    onDismiss: () -> Unit,
    onAdd: (AcademicGroupProject) -> Unit
) {
    var title by remember { mutableStateOf("AI Knowledge Graph Visualizer") }
    var subjectCode by remember { mutableStateOf("CS-310") }
    var subjectName by remember { mutableStateOf("Artificial Intelligence & ML") }
    var description by remember { mutableStateOf("Extract relational entities from research papers and render interactive 3D knowledge graphs.") }
    var techStack by remember { mutableStateOf("Python, Neo4j, FastApi, Three.js") }
    var deadlineDate by remember { mutableStateOf("Dec 15, 2026") }
    var teamMembersInput by remember { mutableStateOf("Priya N. (Lead), Kabir V., Tanmay G.") }
    var weightage by remember { mutableStateOf("30% Capstone Lab") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_project_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("New Academic Project", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Project Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_project_title")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = subjectCode,
                        onValueChange = { subjectCode = it },
                        label = { Text("Subject Code") },
                        singleLine = true,
                        modifier = Modifier.weight(0.4f).testTag("input_project_subject_code")
                    )
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("Subject Name") },
                        singleLine = true,
                        modifier = Modifier.weight(0.6f).testTag("input_project_subject_name")
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Goals") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("input_project_description")
                )

                OutlinedTextField(
                    value = teamMembersInput,
                    onValueChange = { teamMembersInput = it },
                    label = { Text("Team Members (Comma-separated)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_project_team_members")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = deadlineDate,
                        onValueChange = { deadlineDate = it },
                        label = { Text("Final Submission") },
                        singleLine = true,
                        modifier = Modifier.weight(0.5f).testTag("input_project_deadline")
                    )
                    OutlinedTextField(
                        value = weightage,
                        onValueChange = { weightage = it },
                        label = { Text("Weightage") },
                        singleLine = true,
                        modifier = Modifier.weight(0.5f).testTag("input_project_weightage")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val membersList = teamMembersInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                onAdd(
                                    AcademicGroupProject(
                                        title = title.trim(),
                                        subjectCode = subjectCode.trim().uppercase(),
                                        subjectName = subjectName.trim(),
                                        description = description.trim(),
                                        techStack = techStack.trim(),
                                        finalDeadlineDate = deadlineDate.trim(),
                                        teamMembers = if (membersList.isNotEmpty()) membersList else listOf("You (Lead)"),
                                        weightage = weightage.trim(),
                                        milestones = listOf(
                                            ProjectMilestoneItem(
                                                projectId = UUID.randomUUID().toString(),
                                                milestoneNumber = 1,
                                                title = "Architecture & Requirements",
                                                description = "System design specification document.",
                                                deadlineDate = "Nov 15, 2026",
                                                isCompleted = true,
                                                completionPercentage = 100
                                            ),
                                            ProjectMilestoneItem(
                                                projectId = UUID.randomUUID().toString(),
                                                milestoneNumber = 2,
                                                title = "Core Prototype Implementation",
                                                description = "Working proof-of-concept backend and APIs.",
                                                deadlineDate = "Nov 30, 2026",
                                                isCompleted = false,
                                                completionPercentage = 40
                                            ),
                                            ProjectMilestoneItem(
                                                projectId = UUID.randomUUID().toString(),
                                                milestoneNumber = 3,
                                                title = "Final Evaluation & Presentation",
                                                description = "Benchmarking demo, slides, and final lab defense.",
                                                deadlineDate = deadlineDate.trim(),
                                                isCompleted = false,
                                                completionPercentage = 0
                                            )
                                        )
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier.testTag("save_project_button")
                    ) {
                        Text("Create Project")
                    }
                }
            }
        }
    }
}
