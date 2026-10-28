package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AcademicTask
import com.example.model.Subject
import com.example.ui.theme.*

/**
 * Material 3 UI component to list academic tasks like assignments, exams, and project milestones
 * sorted by upcoming due dates with category tags, urgency indicators, and interactive completion tracking.
 */
@Composable
fun AcademicTasksUpcomingDueListComponent(
    tasks: List<AcademicTask>,
    subjects: List<Subject>,
    onToggleTaskComplete: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onAddTask: (title: String, subjectCode: String, subjectName: String, category: String, type: String, dueDate: String, dueTime: String, priority: String, weightage: String, note: String) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Upcoming Academic Tasks & Deadlines",
    subtitle: String = "Assignments, exams, and projects sorted by due date"
) {
    var selectedFilter by remember { mutableStateOf("All Due") } // "All Due", "Exams", "Assignments", "Projects", "Due Soon", "Completed"
    var showAddDialog by remember { mutableStateOf(false) }

    val pendingTasks = remember(tasks) { tasks.filter { !it.isCompleted }.sortedBy { it.dueTimestamp } }
    val completedTasks = remember(tasks) { tasks.filter { it.isCompleted }.sortedByDescending { it.dueTimestamp } }
    val examTasks = remember(tasks) { tasks.filter { !it.isCompleted && it.category.equals("Exam", ignoreCase = true) } }
    val assignmentTasks = remember(tasks) { tasks.filter { !it.isCompleted && it.category.equals("Assignment", ignoreCase = true) } }
    val projectTasks = remember(tasks) { tasks.filter { !it.isCompleted && it.category.equals("Project", ignoreCase = true) } }
    val dueSoonTasks = remember(tasks) {
        tasks.filter {
            !it.isCompleted && (it.priority == "URGENT" || it.dueDate.contains("Today", ignoreCase = true) || it.dueDate.contains("Tomorrow", ignoreCase = true))
        }
    }

    val displayTasks = remember(tasks, selectedFilter) {
        when (selectedFilter) {
            "All Due" -> pendingTasks
            "Exams" -> tasks.filter { it.category.equals("Exam", ignoreCase = true) }.sortedBy { it.dueTimestamp }
            "Assignments" -> tasks.filter { it.category.equals("Assignment", ignoreCase = true) }.sortedBy { it.dueTimestamp }
            "Projects" -> tasks.filter { it.category.equals("Project", ignoreCase = true) }.sortedBy { it.dueTimestamp }
            "Due Soon" -> dueSoonTasks
            "Completed" -> completedTasks
            else -> tasks.sortedBy { it.dueTimestamp }
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("upcoming_academic_tasks_list")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Icon, Title, and Add Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(100),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier.testTag("add_academic_task_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Task", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            // Quick Category Overview Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Exams count
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedFilter = if (selectedFilter == "Exams") "All Due" else "Exams" },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedFilter == "Exams") Color(0xFFFFE4E6) else MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, if (selectedFilter == "Exams") Color(0xFFBE123C) else OutlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFFBE123C), modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("EXAMS", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBE123C))
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${examTasks.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFFBE123C))
                        Text("Upcoming", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Assignments count
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedFilter = if (selectedFilter == "Assignments") "All Due" else "Assignments" },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedFilter == "Assignments") PrimaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, if (selectedFilter == "Assignments") Primary else OutlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Assignment, contentDescription = null, tint = Primary, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ASSIGN.", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Primary)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${assignmentTasks.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Primary)
                        Text("Active", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Projects count
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedFilter = if (selectedFilter == "Projects") "All Due" else "Projects" },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedFilter == "Projects") Color(0xFFCCFBF1) else MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, if (selectedFilter == "Projects") Color(0xFF0F766E) else OutlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF0F766E), modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PROJECTS", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${projectTasks.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
                        Text("Milestones", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Due Soon (<48h)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedFilter = if (selectedFilter == "Due Soon") "All Due" else "Due Soon" },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedFilter == "Due Soon" || dueSoonTasks.isNotEmpty()) ErrorContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, if (dueSoonTasks.isNotEmpty()) Error.copy(alpha = 0.35f) else OutlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HourglassTop, contentDescription = null, tint = if (dueSoonTasks.isNotEmpty()) Error else Primary, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("DUE SOON", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (dueSoonTasks.isNotEmpty()) Error else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${dueSoonTasks.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (dueSoonTasks.isNotEmpty()) Error else MaterialTheme.colorScheme.onSurface)
                        Text("< 48 hours", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Filter Chips Carousel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val filters = listOf(
                    "All Due" to pendingTasks.size,
                    "Exams" to examTasks.size,
                    "Assignments" to assignmentTasks.size,
                    "Projects" to projectTasks.size,
                    "Due Soon" to dueSoonTasks.size,
                    "Completed" to completedTasks.size
                )
                items(filters) { (filterName, count) ->
                    val isSelected = selectedFilter == filterName
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filterName },
                        label = { Text("$filterName ($count)", fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(100),
                        modifier = Modifier.testTag("filter_due_$filterName")
                    )
                }
            }

            // List of Academic Task Cards sorted by upcoming due date
            if (displayTasks.isEmpty()) {
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Tertiary, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "All deadlines caught up in this view!",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap '+ Add Task' to record a new exam, assignment, or project deadline.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    displayTasks.forEach { task ->
                        UpcomingAcademicTaskItemCard(
                            task = task,
                            onToggleComplete = { onToggleTaskComplete(task.id) },
                            onDelete = { onDeleteTask(task.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAcademicDeadlineDialog(
            subjects = subjects,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, subjectCode, subjectName, category, type, dueDate, dueTime, priority, weightage, note ->
                onAddTask(title, subjectCode, subjectName, category, type, dueDate, dueTime, priority, weightage, note)
                showAddDialog = false
            }
        )
    }
}

/**
 * Material 3 Card item for an upcoming academic deadline, featuring due date badge,
 * category pill, subject information, weightage, and completion toggle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingAcademicTaskItemCard(
    task: AcademicTask,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUrgent = !task.isCompleted && (task.priority == "URGENT" || task.dueDate.contains("Today", ignoreCase = true))

    val (categoryColor, categoryContainerColor, categoryIcon) = when (task.category.lowercase()) {
        "exam" -> Triple(Color(0xFFBE123C), Color(0xFFFFE4E6), Icons.Default.School)
        "project" -> Triple(Color(0xFF0F766E), Color(0xFFCCFBF1), Icons.Default.Code)
        else -> Triple(Color(0xFF4338CA), Color(0xFFEEF2FF), Icons.Default.Assignment)
    }

    val priorityColor = when (task.priority) {
        "URGENT" -> Error
        "HIGH" -> Warning
        "MEDIUM" -> Secondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onToggleComplete()
                false
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
                    .clip(RoundedCornerShape(14.dp))
                    .background(bgColor)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCompleting) Icons.Default.CheckCircle else Icons.Default.Replay,
                        contentDescription = "Swipe to mark complete",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCompleting) "Mark Completed" else "Mark Pending",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_swipe_dismiss_${task.id}")
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceContainerLow
            ),
            border = BorderStroke(
                1.dp,
                if (isUrgent) Error.copy(alpha = 0.4f) else OutlineVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (task.isCompleted) 0.dp else 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("academic_task_card_${task.id}")
        ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Checkbox, Title, and Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggleComplete() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("task_checkbox_box_${task.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { onToggleComplete() },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Tertiary,
                                uncheckedColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.testTag("task_checkbox_${task.id}")
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = task.subjectCode,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                            Text("•", color = MaterialTheme.colorScheme.outlineVariant)
                            Text(
                                text = task.subjectName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("delete_task_btn_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete task",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Badges (Due Date, Category, Weightage & Priority)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Due Date Badge
                Surface(
                    shape = RoundedCornerShape(100),
                    color = if (task.isCompleted) MaterialTheme.colorScheme.surfaceContainerHigh else if (isUrgent) ErrorContainer else MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isUrgent) Icons.Default.PriorityHigh else Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else if (isUrgent) Error else Primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Due: ${task.dueDate} • ${task.dueTime}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else if (isUrgent) Error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Category & Priority Tags
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Category Tag
                    Surface(
                        shape = RoundedCornerShape(100),
                        color = categoryContainerColor,
                        border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.25f)),
                        modifier = Modifier.testTag("tag_badge_${task.category}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                                tint = categoryColor
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = task.category,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = categoryColor
                            )
                        }
                    }

                    // Weightage Badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = task.weightage,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Priority Badge
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
                }
            }

            if (task.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
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
}
}

