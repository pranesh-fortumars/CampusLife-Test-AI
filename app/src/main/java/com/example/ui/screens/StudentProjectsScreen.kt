package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
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
import com.example.data.local.AcademicProjectEntity
import com.example.data.local.ProjectStatus
import com.example.viewmodel.CampusLifeViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen component to manage student projects.
 * Supports:
 * - Room database persistence (AcademicProjectEntity & AcademicProjectDao).
 * - Full status tracking: Planned, In Progress, Done.
 * - Deadline management with days remaining calculation, urgency alerts, and deadline editing.
 * - Interactive milestone progress and project creation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProjectsScreen(
    viewModel: CampusLifeViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val projects by viewModel.academicProjects.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    var selectedStatusFilter by remember { mutableStateOf("ALL") } // "ALL", "PLANNED", "IN_PROGRESS", "DONE"
    var searchQuery by remember { mutableStateOf("") }
    var sortByDeadline by remember { mutableStateOf(true) }

    var showAddProjectDialog by remember { mutableStateOf(false) }
    var editingDeadlineProject by remember { mutableStateOf<AcademicProjectEntity?>(null) }

    // Metrics
    val totalCount = projects.size
    val plannedCount = projects.count { it.normalizedStatus == ProjectStatus.PLANNED }
    val inProgressCount = projects.count { it.normalizedStatus == ProjectStatus.IN_PROGRESS }
    val doneCount = projects.count { it.normalizedStatus == ProjectStatus.DONE }
    val now = System.currentTimeMillis()
    val dueSoonCount = projects.count { !it.isCompleted && it.getDaysRemaining(now) in 0..7 }

    // Filtered & Sorted Projects
    val filteredProjects = remember(projects, selectedStatusFilter, searchQuery, sortByDeadline) {
        projects.filter { proj ->
            val matchesFilter = when (selectedStatusFilter) {
                "PLANNED" -> proj.normalizedStatus == ProjectStatus.PLANNED
                "IN_PROGRESS" -> proj.normalizedStatus == ProjectStatus.IN_PROGRESS
                "DONE" -> proj.normalizedStatus == ProjectStatus.DONE
                else -> true
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                proj.title.contains(searchQuery, ignoreCase = true) ||
                        proj.subjectCode.contains(searchQuery, ignoreCase = true) ||
                        proj.techStack.contains(searchQuery, ignoreCase = true)
            }
            matchesFilter && matchesSearch
        }.let { list ->
            if (sortByDeadline) {
                list.sortedWith(compareBy({ it.isCompleted }, { it.dueTimestamp }))
            } else {
                list.sortedBy { it.title.lowercase() }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Student Projects",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Room SQLite • Planned, In Progress & Done",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("projects_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { showAddProjectDialog = true },
                        shape = RoundedCornerShape(100),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_project_header_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Project", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddProjectDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Project") },
                modifier = Modifier.testTag("add_project_fab")
            )
        },
        modifier = Modifier.testTag("student_projects_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Projects Status Summary Banner
            item {
                ProjectsSummaryBanner(
                    total = totalCount,
                    planned = plannedCount,
                    inProgress = inProgressCount,
                    done = doneCount,
                    dueSoon = dueSoonCount
                )
            }

            // 2. Search & Sort Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("project_search_input"),
                        placeholder = { Text("Search projects, courses, tech...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    )

                    IconButton(
                        onClick = { sortByDeadline = !sortByDeadline },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .testTag("toggle_sort_button")
                    ) {
                        Icon(
                            imageVector = if (sortByDeadline) Icons.Default.Event else Icons.Default.SortByAlpha,
                            contentDescription = "Sort",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 3. Status Tracking Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedStatusFilter == "ALL",
                        onClick = { selectedStatusFilter = "ALL" },
                        label = { Text("All ($totalCount)") },
                        modifier = Modifier.testTag("filter_status_all")
                    )
                    FilterChip(
                        selected = selectedStatusFilter == "PLANNED",
                        onClick = { selectedStatusFilter = "PLANNED" },
                        label = { Text("Planned ($plannedCount)") },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B82F6))
                            )
                        },
                        modifier = Modifier.testTag("filter_status_planned")
                    )
                    FilterChip(
                        selected = selectedStatusFilter == "IN_PROGRESS",
                        onClick = { selectedStatusFilter = "IN_PROGRESS" },
                        label = { Text("In Progress ($inProgressCount)") },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B))
                            )
                        },
                        modifier = Modifier.testTag("filter_status_in_progress")
                    )
                    FilterChip(
                        selected = selectedStatusFilter == "DONE",
                        onClick = { selectedStatusFilter = "DONE" },
                        label = { Text("Done ($doneCount)") },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                        },
                        modifier = Modifier.testTag("filter_status_done")
                    )
                }
            }

            // 4. Projects List
            if (filteredProjects.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No projects match '$searchQuery'"
                                else "No projects in this category",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap '+ New Project' to create one saved directly to Room SQLite.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredProjects, key = { it.id }) { project ->
                    StudentProjectCard(
                        project = project,
                        onStatusChange = { newStatus ->
                            viewModel.updateAcademicProjectStatus(project.id, newStatus.code)
                        },
                        onEditDeadline = {
                            editingDeadlineProject = project
                        },
                        onToggleComplete = {
                            viewModel.toggleAcademicProjectComplete(project.id)
                        },
                        onDelete = {
                            viewModel.deleteAcademicProject(project.id)
                        },
                        onStartFocusTimer = {
                            viewModel.openFocusTimerForProject(project)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    // Dialog: Add New Project
    if (showAddProjectDialog) {
        AddProjectDialog(
            subjects = subjects.map { "${it.code} - ${it.name}" },
            onDismiss = { showAddProjectDialog = false },
            onSave = { title, courseStr, description, techStack, deadlineDate, dueTimestamp, milestones, weightage, github, team, status ->
                val code = courseStr.substringBefore(" - ").trim()
                val name = courseStr.substringAfter(" - ", courseStr).trim()
                viewModel.addAcademicProject(
                    title = title,
                    subjectCode = code,
                    subjectName = name,
                    description = description,
                    techStack = techStack,
                    deadlineDate = deadlineDate,
                    totalMilestones = milestones,
                    weightage = weightage,
                    githubUrl = github,
                    teamMembers = team,
                    dueTimestamp = dueTimestamp,
                    status = status
                )
                showAddProjectDialog = false
            }
        )
    }

    // Dialog: Edit Deadline
    editingDeadlineProject?.let { project ->
        EditProjectDeadlineDialog(
            project = project,
            onDismiss = { editingDeadlineProject = null },
            onConfirmDeadline = { newDateStr, newTimestamp ->
                viewModel.updateAcademicProjectDeadline(project.id, newDateStr, newTimestamp)
                editingDeadlineProject = null
            }
        )
    }
}

/**
 * Summary Banner with counters for Planned, In Progress, and Done.
 */
@Composable
private fun ProjectsSummaryBanner(
    total: Int,
    planned: Int,
    inProgress: Int,
    done: Int,
    dueSoon: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("projects_summary_banner"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Projects & Capstones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$total projects managed across semesters",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                    )
                }

                if (dueSoon > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$dueSoon due this week",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3-Segment Metric Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusMetricBox(
                    label = "Planned",
                    count = planned,
                    color = Color(0xFF3B82F6),
                    icon = Icons.Default.Schedule,
                    modifier = Modifier.weight(1f)
                )
                StatusMetricBox(
                    label = "In Progress",
                    count = inProgress,
                    color = Color(0xFFF59E0B),
                    icon = Icons.Default.Bolt,
                    modifier = Modifier.weight(1f)
                )
                StatusMetricBox(
                    label = "Done",
                    count = done,
                    color = Color(0xFF10B981),
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatusMetricBox(
    label: String,
    count: Int,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
    }
}

/**
 * Individual Student Project Card with Status Tracking and Deadline Management.
 */
@Composable
private fun StudentProjectCard(
    project: AcademicProjectEntity,
    onStatusChange: (ProjectStatus) -> Unit,
    onEditDeadline: () -> Unit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onStartFocusTimer: () -> Unit
) {
    var expandedStatusMenu by remember { mutableStateOf(false) }

    val statusColor = when (project.normalizedStatus) {
        ProjectStatus.PLANNED -> Color(0xFF3B82F6)
        ProjectStatus.IN_PROGRESS -> Color(0xFFF59E0B)
        ProjectStatus.DONE -> Color(0xFF10B981)
    }

    val daysLeft = project.getDaysRemaining()
    val isOverdue = !project.isCompleted && daysLeft < 0
    val isDueSoon = !project.isCompleted && daysLeft in 0..3

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("project_card_${project.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = BorderStroke(
            1.dp,
            if (project.isCompleted) Color(0xFF10B981).copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Course badge, Status pill with dropdown menu, and Delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Course Code Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = project.subjectCode,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status Badge with Dropdown Changer
                    Box {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { expandedStatusMenu = true }
                                .testTag("status_selector_${project.id}"),
                            color = statusColor.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(statusColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = project.displayStatus,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Change Status",
                                    tint = statusColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = expandedStatusMenu,
                            onDismissRequest = { expandedStatusMenu = false }
                        ) {
                            ProjectStatus.values().forEach { status ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val color = when (status) {
                                                ProjectStatus.PLANNED -> Color(0xFF3B82F6)
                                                ProjectStatus.IN_PROGRESS -> Color(0xFFF59E0B)
                                                ProjectStatus.DONE -> Color(0xFF10B981)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = status.displayName,
                                                fontWeight = if (project.normalizedStatus == status) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    },
                                    onClick = {
                                        onStatusChange(status)
                                        expandedStatusMenu = false
                                    },
                                    modifier = Modifier.testTag("status_option_${status.name.lowercase()}_${project.id}")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_project_${project.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Project Title & Course Name
            Text(
                text = project.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (project.subjectName.isNotEmpty() && project.subjectName != project.subjectCode) {
                Text(
                    text = project.subjectName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (project.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = project.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tech Stack & Weightage chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (project.techStack.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "🛠️ ${project.techStack}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (project.weightage.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = project.weightage,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                if (project.totalFocusMinutes > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "⏱️ ${project.formattedFocusTime}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Deadline Management Card with Edit Action
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onEditDeadline() }
                    .testTag("deadline_box_${project.id}"),
                color = if (isOverdue) Color(0xFFEF4444).copy(alpha = 0.1f)
                else if (isDueSoon) Color(0xFFF59E0B).copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(
                    1.dp,
                    if (isOverdue) Color(0xFFEF4444).copy(alpha = 0.35f)
                    else if (isDueSoon) Color(0xFFF59E0B).copy(alpha = 0.35f)
                    else Color.Transparent
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = if (isOverdue) Color(0xFFEF4444) else if (isDueSoon) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Deadline: ${project.deadlineDate}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap to change due date",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Urgency Badge
                    Surface(
                        shape = RoundedCornerShape(100),
                        color = when {
                            project.isCompleted -> Color(0xFF10B981).copy(alpha = 0.2f)
                            isOverdue -> Color(0xFFEF4444).copy(alpha = 0.2f)
                            isDueSoon -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        }
                    ) {
                        Text(
                            text = project.getDeadlineStatus(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                project.isCompleted -> Color(0xFF10B981)
                                isOverdue -> Color(0xFFEF4444)
                                isDueSoon -> Color(0xFFD97706)
                                else -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Milestones Progress Indicator
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Milestones: ${project.milestonesCompleted} of ${project.totalMilestones}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${project.progressPercentage}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { project.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(100)),
                    color = statusColor,
                    trackColor = statusColor.copy(alpha = 0.15f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Quick Actions: Toggle Done & Quick Status Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // One-tap mark as completed / in progress toggle
                OutlinedButton(
                    onClick = onToggleComplete,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (project.isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("toggle_complete_${project.id}")
                ) {
                    Icon(
                        imageVector = if (project.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (project.isCompleted) "Mark Incomplete" else "Mark Done",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = onStartFocusTimer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("start_timer_btn_${project.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Start Focus Timer",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Quick Status Advance Pill
                    when (project.normalizedStatus) {
                        ProjectStatus.PLANNED -> {
                            Button(
                                onClick = { onStatusChange(ProjectStatus.IN_PROGRESS) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF59E0B)
                                ),
                                modifier = Modifier.testTag("quick_start_${project.id}")
                            ) {
                                Text("Start Work ⚡", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                        ProjectStatus.IN_PROGRESS -> {
                            Button(
                                onClick = { onStatusChange(ProjectStatus.DONE) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF10B981)
                                ),
                                modifier = Modifier.testTag("quick_done_${project.id}")
                            ) {
                                Text("Complete ✅", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                        ProjectStatus.DONE -> {
                            FilledTonalButton(
                                onClick = { onStatusChange(ProjectStatus.IN_PROGRESS) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("reopen_${project.id}")
                            ) {
                                Text("Reopen ↩", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog to add a new project and persist it directly to Room.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddProjectDialog(
    subjects: List<String>,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        course: String,
        description: String,
        techStack: String,
        deadlineDate: String,
        dueTimestamp: Long,
        milestones: Int,
        weightage: String,
        github: String,
        team: String,
        status: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var course by remember { mutableStateOf(subjects.firstOrNull() ?: "CS-302 - Database Systems") }
    var description by remember { mutableStateOf("") }
    var techStack by remember { mutableStateOf("Kotlin, Jetpack Compose, Room") }
    var deadlineDate by remember { mutableStateOf("Nov 25, 2026") }
    var statusChoice by remember { mutableStateOf("IN_PROGRESS") } // "PLANNED", "IN_PROGRESS", "DONE"
    var totalMilestones by remember { mutableStateOf("4") }
    var weightage by remember { mutableStateOf("25% Capstone") }
    var githubUrl by remember { mutableStateOf("") }
    var teamMembers by remember { mutableStateOf("") }
    var isTitleError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add Student Project",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Persisted directly to local Room SQLite database.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Project Title
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) isTitleError = false
                    },
                    label = { Text("Project Title *") },
                    placeholder = { Text("e.g. Distributed In-Memory Cache") },
                    isError = isTitleError,
                    supportingText = if (isTitleError) { { Text("Title cannot be empty") } } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_project_title_input")
                )

                // Course / Subject
                OutlinedTextField(
                    value = course,
                    onValueChange = { course = it },
                    label = { Text("Course / Subject") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_project_course_input")
                )

                // Initial Status Tracking
                Column {
                    Text(
                        text = "Project Status *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple("Planned", "PLANNED", Color(0xFF3B82F6)),
                            Triple("In Progress", "IN_PROGRESS", Color(0xFFF59E0B)),
                            Triple("Done", "DONE", Color(0xFF10B981))
                        ).forEach { (label, code, color) ->
                            val isSelected = statusChoice == code
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { statusChoice = code },
                                color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, if (isSelected) color else Color.Transparent)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Deadline Management Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = deadlineDate,
                        onValueChange = { deadlineDate = it },
                        label = { Text("Deadline Date") },
                        placeholder = { Text("e.g. Nov 25, 2026") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_project_deadline_input")
                    )

                    OutlinedTextField(
                        value = totalMilestones,
                        onValueChange = { totalMilestones = it },
                        label = { Text("Milestones") },
                        singleLine = true,
                        modifier = Modifier
                            .width(100.dp)
                            .testTag("new_project_milestones_input")
                    )
                }

                // Tech Stack
                OutlinedTextField(
                    value = techStack,
                    onValueChange = { techStack = it },
                    label = { Text("Tech Stack") },
                    placeholder = { Text("e.g. Kotlin, Compose, Room, C++") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Notes") },
                    placeholder = { Text("Key objectives, deliverables, and architecture...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                isTitleError = true
                                return@Button
                            }
                            val day = 86400000L
                            val dueTimestamp = System.currentTimeMillis() + (14 * day)
                            onSave(
                                title.trim(),
                                course.trim(),
                                description.trim(),
                                techStack.trim(),
                                deadlineDate.trim(),
                                dueTimestamp,
                                totalMilestones.toIntOrNull() ?: 4,
                                weightage.trim(),
                                githubUrl.trim(),
                                teamMembers.trim(),
                                statusChoice
                            )
                        },
                        modifier = Modifier.testTag("btn_save_project")
                    ) {
                        Text("Save Project")
                    }
                }
            }
        }
    }
}

/**
 * Dialog to adjust and edit project deadline directly in Room SQLite.
 */
@Composable
private fun EditProjectDeadlineDialog(
    project: AcademicProjectEntity,
    onDismiss: () -> Unit,
    onConfirmDeadline: (String, Long) -> Unit
) {
    var customDeadline by remember { mutableStateOf(project.deadlineDate) }
    val calendar = Calendar.getInstance()
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Manage Project Deadline",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Project: ${project.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Current Deadline
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Current Due Date: ${project.deadlineDate}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(project.getDeadlineStatus(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Text(
                    text = "Quick Extend Due Date:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Quick buttons: +3 days, +1 week, +2 weeks, +1 month
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "+3 Days" to 3,
                        "+1 Week" to 7,
                        "+2 Weeks" to 14,
                        "+1 Month" to 30
                    ).forEach { (label, days) ->
                        FilledTonalButton(
                            onClick = {
                                calendar.time = Date()
                                calendar.add(Calendar.DAY_OF_YEAR, days)
                                customDeadline = sdf.format(calendar.time)
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }

                // Custom Date Input
                OutlinedTextField(
                    value = customDeadline,
                    onValueChange = { customDeadline = it },
                    label = { Text("Deadline Date String") },
                    placeholder = { Text("e.g. Dec 05, 2026") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_deadline_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedTimestamp = try {
                                sdf.parse(customDeadline)?.time ?: (System.currentTimeMillis() + 7 * 86400000L)
                            } catch (e: Exception) {
                                System.currentTimeMillis() + 7 * 86400000L
                            }
                            onConfirmDeadline(customDeadline, parsedTimestamp)
                        },
                        modifier = Modifier.testTag("btn_confirm_deadline")
                    ) {
                        Text("Save Deadline")
                    }
                }
            }
        }
    }
}
