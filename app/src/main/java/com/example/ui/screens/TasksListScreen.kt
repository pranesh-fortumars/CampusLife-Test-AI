package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AcademicTask
import com.example.model.Subject
import com.example.viewmodel.CampusLifeViewModel
import com.example.viewmodel.TaskViewModel

enum class TaskSortOption(val label: String) {
    DUE_DATE("Earliest Due Date"),
    PRIORITY("Highest Priority"),
    SUBJECT("By Course Code"),
    RECENTLY_ADDED("Recently Added")
}

/**
 * Screen layout for viewing and managing tasks from the Room database in a list,
 * using [TaskViewModel] for state management and CRUD operations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksListScreen(
    taskViewModel: TaskViewModel,
    subjects: List<Subject> = emptyList(),
    onBack: () -> Unit
) {
    val tasks by taskViewModel.academicTasks.collectAsState()

    TasksListScreenContent(
        tasks = tasks,
        subjects = subjects,
        onToggleComplete = { id -> taskViewModel.toggleTaskCompleted(id) },
        onAddTask = { title, code, name, category, type, date, time, priority, weight, note ->
            taskViewModel.addTask(
                title = title,
                subjectCode = code,
                subjectName = name,
                category = category,
                type = type,
                dueDate = date,
                dueTime = time,
                priority = priority,
                weightage = weight,
                note = note
            )
        },
        onDeleteTask = { id -> taskViewModel.deleteTask(id) },
        onBack = onBack
    )
}

/**
 * Screen layout for viewing and managing tasks from the Room database in a list,
 * using [CampusLifeViewModel] for state management and CRUD operations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksListScreen(
    viewModel: CampusLifeViewModel,
    onBack: () -> Unit
) {
    val tasks by viewModel.academicTasks.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    TasksListScreenContent(
        tasks = tasks,
        subjects = subjects,
        onToggleComplete = { id -> viewModel.toggleAcademicTaskComplete(id) },
        onAddTask = { title, code, name, category, type, date, time, priority, weight, note ->
            viewModel.addAcademicTask(
                title = title,
                subjectCode = code,
                subjectName = name,
                category = category,
                type = type,
                dueDate = date,
                dueTime = time,
                priority = priority,
                weightage = weight,
                note = note
            )
        },
        onDeleteTask = { id -> viewModel.deleteAcademicTask(id) },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksListScreenContent(
    tasks: List<AcademicTask>,
    subjects: List<Subject>,
    onToggleComplete: (String) -> Unit,
    onAddTask: (String, String, String, String, String, String, String, String, String, String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All Due") }
    var currentSort by remember { mutableStateOf(TaskSortOption.DUE_DATE) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<AcademicTask?>(null) }

    // Aggregate statistics
    val totalCount = tasks.size
    val pendingCount = tasks.count { !it.isCompleted }
    val completedCount = tasks.count { it.isCompleted }
    val urgentCount = tasks.count { !it.isCompleted && (it.priority == "URGENT" || it.dueDate.contains("Today", ignoreCase = true)) }
    val completionRatio = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()

    // Filter and Sort logic
    val filteredTasks = remember(tasks, selectedFilter, searchQuery, currentSort) {
        var list = when (selectedFilter) {
            "All Due" -> tasks.filter { !it.isCompleted }
            "All Tasks" -> tasks
            "Assignments" -> tasks.filter { it.category.equals("Assignment", ignoreCase = true) }
            "Exams" -> tasks.filter { it.category.equals("Exam", ignoreCase = true) }
            "Projects" -> tasks.filter { it.category.equals("Project", ignoreCase = true) }
            "Urgent" -> tasks.filter { !it.isCompleted && (it.priority == "URGENT" || it.dueDate.contains("Today", ignoreCase = true)) }
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

        when (currentSort) {
            TaskSortOption.DUE_DATE -> list.sortedWith(
                compareBy<AcademicTask> { it.isCompleted }.thenBy { it.dueTimestamp }
            )
            TaskSortOption.PRIORITY -> list.sortedWith(
                compareBy<AcademicTask> { it.isCompleted }.thenByDescending {
                    when (it.priority.uppercase()) {
                        "URGENT" -> 4
                        "HIGH" -> 3
                        "MEDIUM" -> 2
                        else -> 1
                    }
                }
            )
            TaskSortOption.SUBJECT -> list.sortedWith(
                compareBy<AcademicTask> { it.isCompleted }.thenBy { it.subjectCode }
            )
            TaskSortOption.RECENTLY_ADDED -> list.sortedWith(
                compareBy<AcademicTask> { it.isCompleted }.thenByDescending { it.createdAt }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Tasks & Deadlines",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Database storage • $pendingCount pending, $completedCount done",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("tasks_screen_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    IconButton(
                        onClick = {
                            com.example.worker.TaskNotificationScheduler.triggerImmediateDueCheck(context)
                            android.widget.Toast.makeText(context, "WorkManager: Checking upcoming deadlines...", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("workmanager_reminder_action_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Trigger WorkManager task reminder",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.testTag("sort_menu_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort tasks"
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            TaskSortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    leadingIcon = {
                                        if (currentSort == option) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    onClick = {
                                        currentSort = option
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            com.example.ui.components.AddTaskFloatingActionButton(
                onClick = { showAddDialog = true }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("tasks_screen_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Stats Overview Card (Material 3 Elevated Card)
            item {
                TaskStatsOverviewCard(
                    total = totalCount,
                    pending = pendingCount,
                    completed = completedCount,
                    urgent = urgentCount,
                    completionRatio = completionRatio
                )
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tasks_search_field"),
                    placeholder = { Text("Search by task title, course code, or tag...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // Filter Chips Carousel
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        "All Due",
                        "All Tasks",
                        "Assignments",
                        "Exams",
                        "Projects",
                        "Urgent",
                        "Completed"
                    )
                    items(filters) { filter ->
                        val isSelected = selectedFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 12.sp) },
                            leadingIcon = {
                                when (filter) {
                                    "Assignments" -> Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, modifier = Modifier.size(16.dp))
                                    "Exams" -> Icon(Icons.Default.EventNote, contentDescription = null, modifier = Modifier.size(16.dp))
                                    "Projects" -> Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                                    "Urgent" -> Icon(Icons.Default.PriorityHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    "Completed" -> Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                                    else -> null
                                }
                            },
                            modifier = Modifier.testTag("task_filter_$filter")
                        )
                    }
                }
            }

            // List Title and Active Count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$selectedFilter (${filteredTasks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sorted by: ${currentSort.label}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Empty State
            if (filteredTasks.isEmpty()) {
                item {
                    EmptyTasksCard(
                        filter = selectedFilter,
                        searchQuery = searchQuery,
                        onAddTaskClick = { showAddDialog = true }
                    )
                }
            } else {
                // Task Items rendered using Material 3 Cards
                items(filteredTasks, key = { it.id }) { task ->
                    Material3TaskCard(
                        task = task,
                        onToggleComplete = { onToggleComplete(task.id) },
                        onDelete = { taskToDelete = task }
                    )
                }
            }
        }
    }

    // Add New Task Dialog
    if (showAddDialog) {
        val context = androidx.compose.ui.platform.LocalContext.current
        com.example.ui.components.AddTaskDialog(
            subjects = subjects,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, code, name, category, type, date, time, priority, weight, note, scheduleReminder ->
                onAddTask(
                    title,
                    code,
                    name,
                    category,
                    type,
                    date,
                    time,
                    priority,
                    weight,
                    note
                )
                if (scheduleReminder) {
                    com.example.worker.TaskNotificationScheduler.triggerImmediateDueCheck(context)
                }
                showAddDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete \"${task.title}\" from the database?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTask(task.id)
                        taskToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_task_btn")
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
 * Top Material 3 card summarizing tasks stored in the Room database.
 */
@Composable
fun TaskStatsOverviewCard(
    total: Int,
    pending: Int,
    completed: Int,
    urgent: Int,
    completionRatio: Float
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_stats_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ACADEMIC TASK WORKLOAD",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$pending Tasks Pending",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (urgent > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PriorityHigh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "$urgent Urgent",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Completion Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { completionRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${(completionRatio * 100).toInt()}% Completed ($completed of $total)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$total Total Tracked",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Material 3 Card representing a single academic task item.
 */
@Composable
fun Material3TaskCard(
    task: AcademicTask,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val isUrgent = task.priority.equals("URGENT", ignoreCase = true) || task.dueDate.contains("Today", ignoreCase = true)

    val priorityColor = when (task.priority.uppercase()) {
        "URGENT" -> MaterialTheme.colorScheme.error
        "HIGH" -> Color(0xFFE65100)
        "MEDIUM" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val categoryColor = when (task.category.uppercase()) {
        "EXAM" -> MaterialTheme.colorScheme.tertiaryContainer
        "PROJECT" -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    val onCategoryColor = when (task.category.uppercase()) {
        "EXAM" -> MaterialTheme.colorScheme.onTertiaryContainer
        "PROJECT" -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    val cardBorder = if (isUrgent && !task.isCompleted) {
        BorderStroke(1.2.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
    } else {
        null
    }

    val cardAlpha = if (task.isCompleted) 0.7f else 1.0f

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (task.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = cardBorder ?: BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Subject Code, Category Badge, Priority Badge, and Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Subject Code Pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = task.subjectCode,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    // Category Badge (Assignment, Exam, Project)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = categoryColor
                    ) {
                        Text(
                            text = task.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = onCategoryColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    // Priority Pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = priorityColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = task.priority,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    // Completion Status Badge with quick-toggle tap
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (task.isCompleted) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier
                            .clickable { onToggleComplete() }
                            .testTag("task_status_pill_${task.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (task.isCompleted) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (task.isCompleted) "Completed" else "Pending",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (task.isCompleted) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Delete Icon
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("delete_task_btn_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete task",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Main Content Row: Checkbox and Task Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleComplete() }
                    .testTag("task_row_toggle_${task.id}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Interactive Checkbox with 48dp minimum touch target
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("toggle_task_checkbox_${task.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { onToggleComplete() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            checkmarkColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Bold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = task.subjectName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Optional Note / Details
            if (task.note.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = task.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Footer Row: Due Date & Time, Weightage, and Quick Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (isUrgent && !task.isCompleted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Due: ${task.dueDate} • ${task.dueTime}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isUrgent && !task.isCompleted) FontWeight.Bold else FontWeight.Normal,
                        color = if (isUrgent && !task.isCompleted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (task.weightage.isNotBlank()) {
                    Text(
                        text = task.weightage,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Card displayed when no tasks match the active filter or search query.
 */
@Composable
fun EmptyTasksCard(
    filter: String,
    searchQuery: String,
    onAddTaskClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("empty_tasks_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AssignmentTurnedIn,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (searchQuery.isNotEmpty()) {
                    "No tasks matching \"$searchQuery\""
                } else if (filter == "Completed") {
                    "No completed tasks yet"
                } else {
                    "No $filter found"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Tasks added to the Room database appear here with live deadline tracking and completion status.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onAddTaskClick,
                shape = RoundedCornerShape(100),
                modifier = Modifier.testTag("empty_card_add_task_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add New Task")
            }
        }
    }
}

/**
 * Material 3 Dialog to create and insert an academic task into the Room database.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewTaskDialog(
    subjects: List<Subject>,
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
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull()?.code ?: "CS-302") }
    var category by remember { mutableStateOf("Assignment") } // Assignment, Exam, Project, Lab
    var dueDate by remember { mutableStateOf("Tomorrow") }
    var dueTime by remember { mutableStateOf("11:59 PM") }
    var priority by remember { mutableStateOf("HIGH") }
    var weightage by remember { mutableStateOf("15% Internal") }
    var note by remember { mutableStateOf("") }

    val subjectName = subjects.find { it.code == selectedSubject }?.name ?: "Course"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Academic Task",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    placeholder = { Text("e.g. Lab 4 Implementation, Quiz 2") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_task_title_input")
                )

                // Course selector row
                Text("Course", style = MaterialTheme.typography.labelSmall)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(subjects) { sub ->
                        FilterChip(
                            selected = selectedSubject == sub.code,
                            onClick = { selectedSubject = sub.code },
                            label = { Text(sub.code, fontSize = 11.sp) }
                        )
                    }
                }

                // Category selector row
                Text("Category", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Assignment", "Exam", "Project").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                // Due Date & Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dueTime,
                        onValueChange = { dueTime = it },
                        label = { Text("Time") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Priority selector
                Text("Priority", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("URGENT", "HIGH", "MEDIUM", "LOW").forEach { prio ->
                        FilterChip(
                            selected = priority == prio,
                            onClick = { priority = prio },
                            label = { Text(prio, fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Notes / Instructions") },
                    placeholder = { Text("Optional submission details...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val type = when (category.uppercase()) {
                            "EXAM" -> "EXAM_PREP"
                            "PROJECT" -> "PROJECT"
                            else -> "ASSIGNMENT"
                        }
                        onConfirm(
                            title.trim(),
                            selectedSubject,
                            subjectName,
                            category,
                            type,
                            dueDate.trim(),
                            dueTime.trim(),
                            priority,
                            weightage.trim(),
                            note.trim()
                        )
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("submit_new_task_btn")
            ) {
                Text("Save to DB")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
