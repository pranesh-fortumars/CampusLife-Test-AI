package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.window.Dialog
import com.example.model.AcademicTask
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel
import java.util.Locale

/**
 * Main Dashboard UI Component using Jetpack Compose that displays a list of
 * upcoming academic tasks and deadlines directly queried from the local Room database.
 * Supports task completion toggling, real-time progress metrics, deadline countdowns,
 * category filtering, search, and creating new tasks into SQLite via Room.
 */
@Composable
fun UpcomingTasksDashboardComponent(
    viewModel: CampusLifeViewModel,
    modifier: Modifier = Modifier,
    onNavigateToTrack: (() -> Unit)? = null
) {
    val tasks by viewModel.academicTasks.collectAsState()

    UpcomingTasksDashboardComponent(
        tasks = tasks,
        onToggleTaskComplete = { id -> viewModel.toggleAcademicTaskComplete(id) },
        onAddTask = { title, subjectCode, subjectName, category, type, dueDate, dueTime, priority, weightage, note ->
            viewModel.addAcademicTask(
                title = title,
                subjectCode = subjectCode,
                subjectName = subjectName,
                category = category,
                type = type,
                dueDate = dueDate,
                dueTime = dueTime,
                priority = priority,
                weightage = weightage,
                note = note
            )
        },
        onDeleteTask = { id -> viewModel.deleteAcademicTask(id) },
        onTriggerReminder = { viewModel.triggerUpcomingTasksReminder() },
        onNavigateToTrack = onNavigateToTrack,
        onRescheduleTaskDueDate = { id, newDate, newTimestamp ->
            viewModel.rescheduleTaskDueDate(id, newDate, newTimestamp)
        },
        modifier = modifier
    )
}

/**
 * Stateless / decoupled variant of UpcomingTasksDashboardComponent for testing and flexible hosting.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingTasksDashboardComponent(
    tasks: List<AcademicTask>,
    onToggleTaskComplete: (String) -> Unit,
    onAddTask: (title: String, subjectCode: String, subjectName: String, category: String, type: String, dueDate: String, dueTime: String, priority: String, weightage: String, note: String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onTriggerReminder: (() -> Unit)? = null,
    onNavigateToTrack: (() -> Unit)? = null,
    onRescheduleTaskDueDate: ((taskId: String, newDueDate: String, newTimestamp: Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Pending") } // Default to pending tasks
    var searchQuery by remember { mutableStateOf("") }
    var expandedTaskId by remember { mutableStateOf<String?>(null) }
    var taskToDelete by remember { mutableStateOf<AcademicTask?>(null) }

    // Derived Task Metrics
    val pendingTasks = remember(tasks) { tasks.filter { !it.isCompleted } }
    val completedTasks = remember(tasks) { tasks.filter { it.isCompleted } }
    val urgentTasks = remember(tasks) {
        tasks.filter { !it.isCompleted && (it.priority.equals("URGENT", ignoreCase = true) || it.dueDate.contains("Today", ignoreCase = true)) }
    }
    val dueSoonTasks = remember(tasks) {
        tasks.filter { !it.isCompleted && (it.dueDate.contains("Today", ignoreCase = true) || it.dueDate.contains("Tomorrow", ignoreCase = true)) }
    }
    val examTasks = remember(tasks) { tasks.filter { it.category.equals("Exam", ignoreCase = true) } }
    val assignmentTasks = remember(tasks) { tasks.filter { it.category.equals("Assignment", ignoreCase = true) } }
    val projectTasks = remember(tasks) { tasks.filter { it.category.equals("Project", ignoreCase = true) } }

    val totalCount = tasks.size
    val completionRatio = if (totalCount > 0) completedTasks.size.toFloat() / totalCount else 0f
    val completionPercent = (completionRatio * 100).toInt()

    // Imminent upcoming task (earliest due timestamp among pending)
    val nextDueTask = remember(pendingTasks) {
        pendingTasks.minByOrNull { it.dueTimestamp }
    }

    // Filter and Search logic
    val filteredTasks = remember(tasks, selectedFilter, searchQuery) {
        val baseList = when (selectedFilter) {
            "Pending" -> tasks.filter { !it.isCompleted }
            "Urgent" -> tasks.filter { !it.isCompleted && (it.priority.equals("URGENT", ignoreCase = true) || it.dueDate.contains("Today", ignoreCase = true)) }
            "Due Soon" -> tasks.filter { !it.isCompleted && (it.dueDate.contains("Today", ignoreCase = true) || it.dueDate.contains("Tomorrow", ignoreCase = true)) }
            "Assignments" -> tasks.filter { it.category.equals("Assignment", ignoreCase = true) }
            "Exams" -> tasks.filter { it.category.equals("Exam", ignoreCase = true) }
            "Projects" -> tasks.filter { it.category.equals("Project", ignoreCase = true) }
            "Completed" -> tasks.filter { it.isCompleted }
            else -> tasks
        }

        if (searchQuery.isBlank()) {
            baseList
        } else {
            val q = searchQuery.trim().lowercase()
            baseList.filter {
                it.title.lowercase().contains(q) ||
                        it.subjectCode.lowercase().contains(q) ||
                        it.subjectName.lowercase().contains(q) ||
                        it.note.lowercase().contains(q)
            }
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("upcoming_tasks_dashboard_component")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Dashboard Header & Room Database Status Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Upcoming Tasks",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = Primary.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Primary)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Room DB (${pendingTasks.size} due)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = Primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        text = "Real-time deadline tracking & submission pipeline",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Add Task Button
                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier.testTag("btn_dashboard_add_task")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Task", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // 2. Real-Time Room Database Progress Bar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Completion Velocity",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${completedTasks.size} of $totalCount Done • $completionPercent%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (completionPercent >= 75) Success else Primary
                        )
                    }

                    LinearProgressIndicator(
                        progress = { completionRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(100)),
                        color = if (completionPercent >= 75) Success else Primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                }
            }

            // 3. Quick Metric Counter Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pending Count Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedFilter == "Pending") Primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, if (selectedFilter == "Pending") Primary else OutlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedFilter = "Pending" }
                        .testTag("stat_card_pending")
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PENDING", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Primary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${pendingTasks.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Primary)
                        Text("Active Tasks", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Urgent Count Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedFilter == "Urgent") Error.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, if (selectedFilter == "Urgent") Error else OutlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedFilter = "Urgent" }
                        .testTag("stat_card_urgent")
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = Error, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("URGENT", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Error, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${urgentTasks.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Error)
                        Text("Critical Due", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Exams Count Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedFilter == "Exams") Color(0xFF4F46E5).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, if (selectedFilter == "Exams") Color(0xFF4F46E5) else OutlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedFilter = "Exams" }
                        .testTag("stat_card_exams")
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("EXAMS", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color(0xFF4F46E5), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${examTasks.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5))
                        Text("Tests & Viva", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Done Count Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedFilter == "Completed") Success.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, if (selectedFilter == "Completed") Success else OutlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedFilter = "Completed" }
                        .testTag("stat_card_done")
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Success, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("DONE", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Success, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${completedTasks.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Success)
                        Text("Completed", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // 4. Imminent Next Due Hero Highlight Card (if any pending deadline)
            if (nextDueTask != null && (selectedFilter == "Pending" || selectedFilter == "All" || selectedFilter == "Urgent")) {
                HeroNearestDeadlineCard(
                    task = nextDueTask,
                    onToggleComplete = { onToggleTaskComplete(nextDueTask.id) },
                    onNavigateToTrack = onNavigateToTrack
                )
            }

            // 5. Search Bar & Filter Chips Carousel
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter tasks by name, course code, notes...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("task_search_input")
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val filterChips = listOf(
                        "Pending" to pendingTasks.size,
                        "All" to tasks.size,
                        "Urgent" to urgentTasks.size,
                        "Due Soon" to dueSoonTasks.size,
                        "Assignments" to assignmentTasks.size,
                        "Exams" to examTasks.size,
                        "Projects" to projectTasks.size,
                        "Completed" to completedTasks.size
                    )

                    items(filterChips) { (label, count) ->
                        val isSelected = selectedFilter == label
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = label },
                            label = {
                                Text(
                                    text = "$label ($count)",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(100),
                            modifier = Modifier.testTag("filter_chip_$label")
                        )
                    }
                }
            }

            // 6. Upcoming Tasks List Items
            if (filteredTasks.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (selectedFilter == "Completed") Icons.Outlined.CheckCircleOutline else Icons.Outlined.TaskAlt,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No tasks match '$searchQuery'" else "All caught up in this view!",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap '+ Add Task' above to log a new assignment or lab into the Room DB.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Swipe, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Swipe card or tap checkbox to toggle completion in Room DB",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    filteredTasks.forEach { task ->
                        UpcomingTaskItemCard(
                            task = task,
                            isExpanded = expandedTaskId == task.id,
                            onToggleComplete = { onToggleTaskComplete(task.id) },
                            onToggleExpand = {
                                expandedTaskId = if (expandedTaskId == task.id) null else task.id
                            },
                            onDelete = { taskToDelete = task },
                            onTriggerReminder = onTriggerReminder,
                            onRescheduleDueDate = if (onRescheduleTaskDueDate != null) {
                                { newDate, newTs -> onRescheduleTaskDueDate(task.id, newDate, newTs) }
                            } else null
                        )
                    }
                }
            }

            // Footer note indicating local persistence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Backed by Room SQLite Database (Reactive Flow)",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (onNavigateToTrack != null) {
                    TextButton(
                        onClick = onNavigateToTrack,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_view_all_deadlines")
                    ) {
                        Text("View Track", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Primary)
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(12.dp), tint = Primary)
                    }
                }
            }
        }
    }

    // Add Task Dialog Modal
    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, subjectCode, subjectName, category, type, dueDate, dueTime, priority, weightage, note ->
                onAddTask(title, subjectCode, subjectName, category, type, dueDate, dueTime, priority, weightage, note)
                showAddDialog = false
                Toast.makeText(context, "Saved task '$title' to Room DB", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Confirmation Dialog
    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Delete Deadline") },
            text = { Text("Are you sure you want to remove '${task.title}' from the Room database?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTask(task.id)
                        taskToDelete = null
                        Toast.makeText(context, "Removed from Room DB", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Hero Card highlighting the imminent next academic deadline.
 */
@Composable
private fun HeroNearestDeadlineCard(
    task: AcademicTask,
    onToggleComplete: () -> Unit,
    onNavigateToTrack: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val isUrgent = task.priority.equals("URGENT", ignoreCase = true) || task.dueDate.contains("Today", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUrgent) Error.copy(alpha = 0.08f) else Primary.copy(alpha = 0.08f)
        ),
        border = BorderStroke(
            1.dp,
            if (isUrgent) Error.copy(alpha = 0.4f) else Primary.copy(alpha = 0.3f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_nearest_deadline_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(100),
                    color = if (isUrgent) Error else Primary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isUrgent) Icons.Default.PriorityHigh else Icons.Default.Alarm,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "IMMINENT DUE: ${task.dueDate.uppercase()} • ${task.dueTime}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = task.subjectCode,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${task.subjectName} • ${task.weightage}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onToggleComplete,
                    colors = ButtonDefaults.buttonColors(containerColor = Success),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("hero_task_mark_complete_btn")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mark Completed", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (onNavigateToTrack != null) {
                    OutlinedButton(
                        onClick = onNavigateToTrack,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Details", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/**
 * Standard card representing a single task item from the Room database.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpcomingTaskItemCard(
    task: AcademicTask,
    isExpanded: Boolean,
    onToggleComplete: () -> Unit,
    onToggleExpand: () -> Unit,
    onDelete: () -> Unit,
    onTriggerReminder: (() -> Unit)?,
    onRescheduleDueDate: ((newDate: String, newTimestamp: Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showRescheduleDatePicker by remember { mutableStateOf(false) }
    val isUrgent = task.priority.equals("URGENT", ignoreCase = true)

    val priorityColor = when (task.priority.uppercase()) {
        "URGENT" -> Error
        "HIGH" -> Warning
        "MEDIUM" -> Primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    // Material 3 Swipe-to-Dismiss Gesture State for marking completion in Room DB
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onToggleComplete()
                false // Rebound smoothly while updating completion state in Room DB
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val isCompleting = !task.isCompleted
            val bgColor = if (isCompleting) Success else Warning
            val alignment = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCompleting) Icons.Default.CheckCircle else Icons.Default.Replay,
                        contentDescription = "Swipe to mark completion",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCompleting) "Mark Completed ✔" else "Mark Pending ⏳",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        },
        modifier = modifier.testTag("task_swipe_dismiss_${task.id}")
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (task.isCompleted) MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(
                1.dp,
                if (isUrgent && !task.isCompleted) Error.copy(alpha = 0.35f) else OutlineVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpand)
                .testTag("task_item_${task.id}")
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Material 3 Checkbox with 48dp minimum accessible touch target
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("task_checkbox_container_${task.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { onToggleComplete() },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Success,
                                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                checkmarkColor = Color.White
                            ),
                            modifier = Modifier.testTag("task_checkbox_${task.id}")
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        // Category & Priority Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Priority Badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = priorityColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = task.priority.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = priorityColor,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }

                            // Category Badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest
                            ) {
                                Text(
                                    text = task.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }

                            // Subject Code
                            Text(
                                text = task.subjectCode,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Title
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Due date & Time
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Event,
                                contentDescription = null,
                                tint = if (isUrgent && !task.isCompleted) Error else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${task.dueDate} • ${task.dueTime}",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                fontWeight = if (isUrgent && !task.isCompleted) FontWeight.Bold else FontWeight.Normal,
                                color = if (isUrgent && !task.isCompleted) Error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (task.weightage.isNotBlank()) {
                                Text("•", fontSize = 11.sp, color = MaterialTheme.colorScheme.outlineVariant)
                                Text(
                                    text = task.weightage,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Delete Button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("task_delete_${task.id}")
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Delete task",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Expanded details & note preview
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 42.dp, top = 8.dp, end = 4.dp)
                    ) {
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Course: ${task.subjectName}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        if (task.note.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Note: ${task.note}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Status: ${if (task.isCompleted) "Completed ✔" else "Pending"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (task.isCompleted) Success else Warning,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (onRescheduleDueDate != null) {
                                    OutlinedButton(
                                        onClick = { showRescheduleDatePicker = true },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("btn_reschedule_${task.id}")
                                    ) {
                                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Change Due Date", fontSize = 10.sp)
                                    }
                                }

                                if (onTriggerReminder != null) {
                                    TextButton(
                                        onClick = onTriggerReminder,
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Check Alerts", fontSize = 10.sp)
                                    }
                                }
                            }
                        }

                        if (showRescheduleDatePicker) {
                            TaskDatePickerDialog(
                                initialSelectedDateMillis = task.dueTimestamp,
                                onDateSelected = { millis, formattedDate ->
                                    onRescheduleDueDate?.invoke(formattedDate, millis)
                                    showRescheduleDatePicker = false
                                },
                                onDismiss = { showRescheduleDatePicker = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog to add a new academic task directly into Room Database.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (
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
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("CS-306" to "Computer Networks") }
    var category by remember { mutableStateOf("Assignment") }
    var priority by remember { mutableStateOf("HIGH") }
    var dueDate by remember { mutableStateOf("Tomorrow") }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var dueTime by remember { mutableStateOf("11:59 PM") }
    var weightage by remember { mutableStateOf("15% Internal") }
    var note by remember { mutableStateOf("") }

    val subjects = listOf(
        "CS-302" to "Database Management Systems",
        "CS-304" to "Operating Systems",
        "CS-306" to "Computer Networks",
        "CS-308" to "Software Engineering",
        "HU-302" to "Engineering Economics"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .testTag("add_task_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Academic Task",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    placeholder = { Text("e.g., Lab 5 Socket Programming") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_task_title")
                )

                // Course Code Picker
                Text("Subject / Course", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(subjects) { (code, name) ->
                        val isSelected = selectedSubject.first == code
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSubject = code to name },
                            label = { Text(code, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Category & Priority Rows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Category", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Assignment", "Exam", "Project").forEach { cat ->
                                val isSelected = category == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { category = cat },
                                    label = { Text(cat.take(4), fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // Priority
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Priority", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("URGENT", "HIGH", "MED").forEach { pri ->
                                val isSelected = priority == pri
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { priority = pri },
                                    label = { Text(pri, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (pri == "URGENT") Error else Primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // Due Date & Time (Material Design 3 DatePicker)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = {
                            dueDate = it
                            selectedDateMillis = null
                        },
                        label = { Text("Due Date *") },
                        placeholder = { Text("e.g. Oct 30, 2026") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.testTag("btn_open_date_picker_dashboard")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Pick Date",
                                    tint = Primary
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("task_due_date_input")
                    )
                    OutlinedTextField(
                        value = dueTime,
                        onValueChange = { dueTime = it },
                        label = { Text("Due Time") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(0.9f)
                            .testTag("task_due_time_input")
                    )
                }

                // Quick Date Preset Chips & DatePicker Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = { showDatePicker = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_pick_date_preset_dashboard")
                    ) {
                        Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pick Date", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    listOf("Today", "Tomorrow", "In 3 Days").forEach { preset ->
                        val isSelected = dueDate == preset
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) Primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(1.dp, if (isSelected) Primary else OutlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    dueDate = preset
                                    selectedDateMillis = TaskDateUtils.parseDateStringToEpochMillis(preset)
                                }
                        ) {
                            Text(
                                text = preset,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                if (showDatePicker) {
                    TaskDatePickerDialog(
                        initialSelectedDateMillis = selectedDateMillis ?: TaskDateUtils.parseDateStringToEpochMillis(dueDate),
                        onDateSelected = { millis, formatted ->
                            selectedDateMillis = millis
                            dueDate = formatted
                            showDatePicker = false
                        },
                        onDismiss = { showDatePicker = false }
                    )
                }

                // Weightage & Notes
                OutlinedTextField(
                    value = weightage,
                    onValueChange = { weightage = it },
                    label = { Text("Weightage / Marks") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Optional Notes") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onConfirm(
                                title.trim(),
                                selectedSubject.first,
                                selectedSubject.second,
                                category,
                                category.uppercase(),
                                dueDate.trim(),
                                dueTime.trim(),
                                priority,
                                weightage.trim(),
                                note.trim()
                            )
                        }
                    },
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_submit_add_task")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save to Room Database", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
