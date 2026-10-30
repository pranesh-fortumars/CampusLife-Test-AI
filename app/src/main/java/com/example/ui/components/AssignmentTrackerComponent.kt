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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AcademicTask
import com.example.model.Subject
import com.example.ui.screens.TaskSortOption
import com.example.ui.theme.*

/**
 * Material 3 Assignment Tracker component with Room database persistence.
 * Tracks assignments with due dates, priorities, completion status, course associations,
 * and integrated Material 3 DatePicker rescheduling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentTrackerComponent(
    tasks: List<AcademicTask>,
    subjects: List<Subject> = emptyList(),
    onToggleComplete: (String) -> Unit,
    onRescheduleDueDate: (taskId: String, newDueDate: String, newTimestamp: Long) -> Unit,
    onDeleteTask: (String) -> Unit,
    onAddTask: (
        title: String,
        courseCode: String,
        courseName: String,
        category: String,
        type: String,
        dueDate: String,
        dueTime: String,
        priority: String,
        weightage: String,
        note: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Pending") } // "Pending", "All", "Completed", "Urgent"
    var selectedCourseFilter by remember { mutableStateOf<String?>(null) } // null = all courses
    var currentSort by remember { mutableStateOf(TaskSortOption.DUE_DATE) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var reschedulingTaskId by remember { mutableStateOf<String?>(null) }
    var taskToDelete by remember { mutableStateOf<AcademicTask?>(null) }

    // Aggregate statistics
    val totalCount = tasks.size
    val pendingCount = tasks.count { !it.isCompleted }
    val completedCount = tasks.count { it.isCompleted }
    val urgentCount = tasks.count { !it.isCompleted && (it.priority == "URGENT" || it.dueDate.contains("Today", ignoreCase = true)) }
    val completionRatio = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()

    // Filtered and Sorted list
    val filteredAssignments = remember(tasks, selectedFilter, selectedCourseFilter, searchQuery, currentSort) {
        var list = when (selectedFilter) {
            "Pending" -> tasks.filter { !it.isCompleted }
            "Completed" -> tasks.filter { it.isCompleted }
            "Urgent" -> tasks.filter { !it.isCompleted && (it.priority == "URGENT" || it.priority == "HIGH") }
            else -> tasks
        }

        if (selectedCourseFilter != null) {
            list = list.filter { it.subjectCode.equals(selectedCourseFilter, ignoreCase = true) }
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

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("assignment_tracker_component")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with title and Add Assignment button
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
                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Assignment Tracker",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Local SQLite persistence via Room database",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(100),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("assignment_tracker_add_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Task", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            // Overview Metric Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pending Count Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Pending",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$pendingCount",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }

                // Urgent Due Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Urgent / High",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$urgentCount",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Completed Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$completedCount / $totalCount",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Tertiary
                        )
                    }
                }
            }

            // Completion Progress Indicator
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Completion Rate",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(completionRatio * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
                LinearProgressIndicator(
                    progress = { completionRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(100)),
                    color = Primary,
                    trackColor = OutlineVariant.copy(alpha = 0.4f)
                )
            }

            // Search Bar & Sort
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search assignments...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("assignment_tracker_search_field")
                )

                // Sort Dropdown Button
                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("assignment_tracker_sort_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        TaskSortOption.values().forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option.label,
                                        fontWeight = if (currentSort == option) FontWeight.Bold else FontWeight.Normal,
                                        color = if (currentSort == option) Primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    currentSort = option
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Status Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val filters = listOf("Pending", "All", "Urgent", "Completed")
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            val count = when (filter) {
                                "Pending" -> pendingCount
                                "All" -> totalCount
                                "Urgent" -> urgentCount
                                "Completed" -> completedCount
                                else -> 0
                            }
                            Text("$filter ($count)", fontSize = 11.sp)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("assignment_filter_${filter.lowercase()}")
                    )
                }

                // Course Code quick filters
                val courseCodes = tasks.map { it.subjectCode }.distinct()
                items(courseCodes) { code ->
                    val isCourseSelected = selectedCourseFilter == code
                    FilterChip(
                        selected = isCourseSelected,
                        onClick = {
                            selectedCourseFilter = if (isCourseSelected) null else code
                        },
                        label = { Text(code, fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(12.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Tertiary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("assignment_course_filter_${code.lowercase()}")
                    )
                }
            }

            // Assignment List Content
            if (filteredAssignments.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No assignments match '$searchQuery'" else "All caught up! No assignments found.",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap '+ New Task' above to add an assignment to Room database.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    filteredAssignments.forEachIndexed { index, task ->
                        AssignmentItemCard(
                            task = task,
                            index = index,
                            onToggleComplete = { onToggleComplete(task.id) },
                            onReschedule = { reschedulingTaskId = task.id },
                            onDelete = { taskToDelete = task }
                        )
                    }
                }
            }
        }
    }

    // Material 3 DatePickerDialog for Rescheduling Due Date
    if (reschedulingTaskId != null) {
        val activeTask = tasks.find { it.id == reschedulingTaskId }
        TaskDatePickerDialog(
            initialSelectedDateMillis = activeTask?.dueTimestamp,
            onDateSelected = { newDateMillis, formattedDate ->
                if (reschedulingTaskId != null) {
                    onRescheduleDueDate(reschedulingTaskId!!, formattedDate, newDateMillis)
                }
                reschedulingTaskId = null
            },
            onDismiss = { reschedulingTaskId = null }
        )
    }

    // Delete Confirmation Dialog
    if (taskToDelete != null) {
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Assignment?") },
            text = { Text("Are you sure you want to delete '${taskToDelete?.title}' from your Room database?") },
            confirmButton = {
                Button(
                    onClick = {
                        taskToDelete?.id?.let { onDeleteTask(it) }
                        taskToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_assignment_btn")
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

    // Add New Assignment Dialog
    if (showAddDialog) {
        AddTaskDialogWithDatePicker(
            subjects = subjects,
            onDismiss = { showAddDialog = false },
            onAddTask = { title, code, name, category, type, date, time, priority, weight, note ->
                onAddTask(title, code, name, category, type, date, time, priority, weight, note)
                showAddDialog = false
            }
        )
    }
}

/**
 * Single Assignment Item Card with interactive completion checkbox,
 * priority badge, course code, due date, rescheduling button, and notes.
 */
@Composable
fun AssignmentItemCard(
    task: AcademicTask,
    index: Int,
    onToggleComplete: () -> Unit,
    onReschedule: () -> Unit,
    onDelete: () -> Unit
) {
    val isDone = task.isCompleted

    val priorityColor = when (task.priority.uppercase()) {
        "URGENT" -> MaterialTheme.colorScheme.error
        "HIGH" -> Warning
        "MEDIUM" -> Primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val priorityContainer = when (task.priority.uppercase()) {
        "URGENT" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        "HIGH" -> Warning.copy(alpha = 0.15f)
        "MEDIUM" -> PrimaryContainer.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isDone) MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            1.dp,
            if (isDone) OutlineVariant.copy(alpha = 0.3f) else OutlineVariant.copy(alpha = 0.6f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("assignment_card_$index")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Top Row: Checkbox, Course & Priority Badges, Delete Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Checkbox(
                        checked = isDone,
                        onCheckedChange = { onToggleComplete() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("assignment_checkbox_$index")
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Course Code Tag
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = task.subjectCode,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Priority Tag
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = priorityContainer
                    ) {
                        Text(
                            text = task.priority.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("assignment_delete_btn_$index")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Title
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier.padding(start = 42.dp, end = 8.dp)
            )

            // Subject name & Weightage
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 42.dp, top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.subjectName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (task.weightage.isNotBlank()) {
                    Text("•", color = OutlineVariant, fontSize = 10.sp)
                    Text(
                        text = task.weightage,
                        style = MaterialTheme.typography.labelSmall,
                        color = Tertiary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                }
            }

            if (task.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 42.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Row: Due Date & Reschedule DatePicker button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 42.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Due Date Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = if (task.priority == "URGENT") MaterialTheme.colorScheme.error else Primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Due: ${task.dueDate} at ${task.dueTime}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (task.priority == "URGENT") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp
                    )
                }

                // Reschedule with DatePicker Button
                TextButton(
                    onClick = onReschedule,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("assignment_reschedule_btn_$index")
                ) {
                    Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reschedule", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Add Task Dialog with Material 3 DatePicker integration for due date selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialogWithDatePicker(
    subjects: List<Subject> = emptyList(),
    onDismiss: () -> Unit,
    onAddTask: (
        title: String,
        courseCode: String,
        courseName: String,
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
    var selectedSubjectCode by remember { mutableStateOf(subjects.firstOrNull()?.code ?: "CS-302") }
    var selectedSubjectName by remember {
        mutableStateOf(subjects.firstOrNull()?.name ?: "Database Management Systems")
    }
    var priority by remember { mutableStateOf("HIGH") }
    var dueDate by remember { mutableStateOf("Tomorrow") }
    var dueTimestamp by remember { mutableStateOf(System.currentTimeMillis() + 86400000L) }
    var dueTime by remember { mutableStateOf("11:59 PM") }
    var weightage by remember { mutableStateOf("15% Internal") }
    var note by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddTask, contentDescription = null, tint = Primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Academic Assignment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Assignment Title *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_assignment_title")
                )

                // Course Code / Subject
                if (subjects.isNotEmpty()) {
                    Text("Select Course:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(subjects) { sub ->
                            val isChosen = selectedSubjectCode == sub.code
                            FilterChip(
                                selected = isChosen,
                                onClick = {
                                    selectedSubjectCode = sub.code
                                    selectedSubjectName = sub.name
                                },
                                label = { Text(sub.code, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Priority Selection
                Text("Priority Level:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val priorities = listOf("URGENT", "HIGH", "MEDIUM", "LOW")
                    priorities.forEach { p ->
                        val isChosen = priority == p
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isChosen) Primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { priority = p }
                        ) {
                            Text(
                                text = p,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isChosen) Color.White else MaterialTheme.colorScheme.onSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                // Due Date with DatePicker Picker Trigger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Pick Date", tint = Primary)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_assignment_due_date")
                    )

                    OutlinedTextField(
                        value = dueTime,
                        onValueChange = { dueTime = it },
                        label = { Text("Time") },
                        singleLine = true,
                        modifier = Modifier.width(110.dp)
                    )
                }

                // Weightage
                OutlinedTextField(
                    value = weightage,
                    onValueChange = { weightage = it },
                    label = { Text("Weightage / Grade Contribution") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Notes
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Instructions / Notes") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddTask(
                            title.trim(),
                            selectedSubjectCode,
                            selectedSubjectName,
                            "Assignment",
                            "ASSIGNMENT",
                            dueDate.trim(),
                            dueTime.trim(),
                            priority,
                            weightage.trim(),
                            note.trim()
                        )
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier.testTag("save_assignment_btn")
            ) {
                Text("Save to Room DB")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showDatePicker) {
        TaskDatePickerDialog(
            initialSelectedDateMillis = dueTimestamp,
            onDateSelected = { millis, formatted ->
                dueTimestamp = millis
                dueDate = formatted
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}
