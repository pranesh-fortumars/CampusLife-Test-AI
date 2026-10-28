package com.example.ui.components

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
import com.example.model.StudentProfile
import com.example.model.Subject
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun MainAcademicDashboard(
    profile: StudentProfile,
    subjects: List<Subject>,
    tasks: List<AcademicTask>,
    onToggleTaskComplete: (String) -> Unit,
    onAddTask: (title: String, subjectCode: String, subjectName: String, category: String, type: String, dueDate: String, dueTime: String, priority: String, weightage: String, note: String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onNavigateToTrack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Pending") } // Default to "Pending" so user sees what's next!

    val pendingTasks = remember(tasks) { tasks.filter { !it.isCompleted } }
    val completedTasks = remember(tasks) { tasks.filter { it.isCompleted } }
    val dueSoonTasks = remember(tasks) {
        tasks.filter { !it.isCompleted && (it.priority == "URGENT" || it.dueDate.contains("Today", ignoreCase = true) || it.dueDate.contains("Tomorrow", ignoreCase = true)) }
    }
    val examTasks = remember(tasks) { tasks.filter { it.category.equals("Exam", ignoreCase = true) } }
    val assignmentTasks = remember(tasks) { tasks.filter { it.category.equals("Assignment", ignoreCase = true) } }
    val projectTasks = remember(tasks) { tasks.filter { it.category.equals("Project", ignoreCase = true) } }

    val nextDueTask = remember(pendingTasks) {
        pendingTasks.minByOrNull { it.dueTimestamp }
    }

    val filteredTasks = remember(tasks, selectedFilter) {
        when (selectedFilter) {
            "Pending" -> tasks.filter { !it.isCompleted }
            "Exam" -> tasks.filter { it.category.equals("Exam", ignoreCase = true) }
            "Assignment" -> tasks.filter { it.category.equals("Assignment", ignoreCase = true) }
            "Project" -> tasks.filter { it.category.equals("Project", ignoreCase = true) }
            "Completed" -> tasks.filter { it.isCompleted }
            else -> tasks
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("main_academic_dashboard"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Material 3 Greeting & Student Profile Snapshot
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.7f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Good morning, ${profile.name.split(" ").first()} 👋",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${profile.title} • DTU",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = Primary
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Thursday, 24 October • Semester 6 Core",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Attendance & Status Badge
                    Surface(
                        shape = RoundedCornerShape(100),
                        color = if (profile.overallAttendance >= 75.0) TertiaryContainer else ErrorContainer,
                        border = BorderStroke(1.dp, if (profile.overallAttendance >= 75.0) Tertiary.copy(alpha = 0.3f) else Error.copy(alpha = 0.3f)),
                        modifier = Modifier.clickable { onNavigateToTrack() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (profile.overallAttendance >= 75.0) Tertiary else Error)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${String.format(Locale.US, "%.1f%%", profile.overallAttendance)} ATT.",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (profile.overallAttendance >= 75.0) OnTertiaryContainer else OnErrorContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Summary Cards Grid for Academic Categories & Progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Exams
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedFilter = if (selectedFilter == "Exam") "All" else "Exam" },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedFilter == "Exam") Color(0xFFFFE4E6) else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, if (selectedFilter == "Exam") Color(0xFFBE123C) else OutlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFFBE123C), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("EXAMS", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = Color(0xFFBE123C), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${examTasks.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFFBE123C))
                            Text("Tests & Viva", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Assignments
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedFilter = if (selectedFilter == "Assignment") "All" else "Assignment" },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedFilter == "Assignment") PrimaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, if (selectedFilter == "Assignment") Primary else OutlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Assignment, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ASSIGN.", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = Primary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${assignmentTasks.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Primary)
                            Text("Tasks & Labs", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Major Projects
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedFilter = if (selectedFilter == "Project") "All" else "Project" },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedFilter == "Project") Color(0xFFCCFBF1) else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, if (selectedFilter == "Project") Color(0xFF0F766E) else OutlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF0F766E), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PROJECTS", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = Color(0xFF0F766E), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${projectTasks.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
                            Text("Milestones", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Completed
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedFilter = if (selectedFilter == "Completed") "All" else "Completed" },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedFilter == "Completed") TertiaryContainer else TertiaryContainer.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, if (selectedFilter == "Completed") Tertiary else Tertiary.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Tertiary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("DONE", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = Tertiary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${completedTasks.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Tertiary)
                            Text("Finished", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // 2. Hero Card for the Nearest Impending Academic Deadline (if any pending)
        if (nextDueTask != null) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                border = BorderStroke(1.dp, if (nextDueTask.priority == "URGENT") Error.copy(alpha = 0.4f) else OutlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_next_deadline_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = if (nextDueTask.priority == "URGENT") ErrorContainer else PrimaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (nextDueTask.priority == "URGENT") Icons.Default.PriorityHigh else Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = if (nextDueTask.priority == "URGENT") Error else Primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "NEXT DUE: ${nextDueTask.dueDate.uppercase()} • ${nextDueTask.dueTime}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (nextDueTask.priority == "URGENT") Error else OnPrimaryContainer
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = nextDueTask.subjectCode,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = nextDueTask.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${nextDueTask.subjectName} • ${nextDueTask.weightage}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (nextDueTask.note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = nextDueTask.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { onToggleTaskComplete(nextDueTask.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Tertiary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("hero_complete_btn")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark Complete", fontWeight = FontWeight.SemiBold)
                        }

                        FilledTonalButton(
                            onClick = onNavigateToTrack,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("All Deadlines")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        // 3. Upcoming Academic Deadlines Section with Filter Chips & Add Action
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Academic Deadlines (${tasks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Assignments, Labs & Project milestones in Room DB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(100),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier.testTag("add_deadline_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Deadline", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            // Filter Chips Carousel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val filters = listOf(
                    "All" to tasks.size,
                    "Pending" to pendingTasks.size,
                    "Exam" to examTasks.size,
                    "Assignment" to assignmentTasks.size,
                    "Project" to projectTasks.size,
                    "Completed" to completedTasks.size
                )
                items(filters) { (filterName, count) ->
                    val isSelected = selectedFilter == filterName
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filterName },
                        label = { Text("$filterName ($count)", fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(100),
                        modifier = Modifier.testTag("filter_chip_$filterName")
                    )
                }
            }

            // Task Deadline Cards List (showing filtered tasks)
            if (filteredTasks.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No deadlines in this view. Tap '+ Add Deadline' to create one!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Show items cleanly
                filteredTasks.forEach { task ->
                    AcademicDeadlineItemCard(
                        task = task,
                        onToggleComplete = { onToggleTaskComplete(task.id) },
                        onDelete = { onDeleteTask(task.id) }
                    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicDeadlineItemCard(
    task: AcademicTask,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val isUrgent = task.priority == "URGENT" || task.priority == "HIGH"
    val priorityColor = when (task.priority) {
        "URGENT" -> Error
        "HIGH" -> Warning
        "MEDIUM" -> Secondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val (categoryColor, categoryContainerColor, categoryIcon) = when (task.category.lowercase()) {
        "exam" -> Triple(Color(0xFFBE123C), Color(0xFFFFE4E6), Icons.Default.School)
        "project" -> Triple(Color(0xFF0F766E), Color(0xFFCCFBF1), Icons.Default.Code)
        else -> Triple(Color(0xFF4338CA), Color(0xFFEEF2FF), Icons.Default.Assignment)
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
            val bgColor = if (isCompleting) Tertiary else Warning
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
                        contentDescription = "Swipe to toggle completion",
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
        modifier = Modifier.testTag("swipe_task_card_${task.id}")
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            border = BorderStroke(1.dp, if (isUrgent && !task.isCompleted) Error.copy(alpha = 0.35f) else OutlineVariant.copy(alpha = 0.6f)),
            elevation = CardDefaults.cardElevation(defaultElevation = if (task.isCompleted) 0.dp else 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("task_card_${task.id}")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp),
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
                    Column {
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
                            
                            // Category Tag Badge
                            Surface(
                                shape = RoundedCornerShape(100),
                                color = categoryContainerColor,
                                border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.3f)),
                                modifier = Modifier.testTag("task_category_${task.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = categoryIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(11.dp),
                                        tint = categoryColor
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = task.category.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = categoryColor
                                    )
                                }
                            }

                            Text("•", color = MaterialTheme.colorScheme.outlineVariant)
                            Text(
                                text = task.weightage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Due date pill
                Surface(
                    shape = RoundedCornerShape(100),
                    color = if (task.isCompleted) MaterialTheme.colorScheme.surfaceContainerHigh else if (isUrgent) ErrorContainer else MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
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

                // Type & Priority badge
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = task.type,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                }
            }
        }
    }
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAcademicDeadlineDialog(
    subjects: List<Subject>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, subjectCode: String, subjectName: String, category: String, type: String, dueDate: String, dueTime: String, priority: String, weightage: String, note: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull() ?: Subject(code = "CS-302", name = "DBMS", credits = 4, faculty = "Dr. Roy", attendedClasses = 26, totalClasses = 30)) }
    var category by remember { mutableStateOf("Assignment") } // "Exam", "Assignment", "Project"
    var type by remember { mutableStateOf("ASSIGNMENT") } // ASSIGNMENT, PROJECT, LAB, QUIZ, EXAM_PREP
    var dueDate by remember { mutableStateOf("Tomorrow") }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var dueTime by remember { mutableStateOf("11:59 PM") }
    var priority by remember { mutableStateOf("HIGH") }
    var weightage by remember { mutableStateOf("15% Weightage") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Academic Task & Deadline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task / Deadline Title") },
                    placeholder = { Text("e.g., Mid-Term Prep or Mini Project") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input")
                )

                // Category Tagging Selector ('Exam', 'Assignment', 'Project')
                Column {
                    Text(
                        text = "Tag Category",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val categoryOptions = listOf(
                            Triple("Exam", Icons.Default.School, Color(0xFFBE123C)),
                            Triple("Assignment", Icons.Default.Assignment, Primary),
                            Triple("Project", Icons.Default.Code, Color(0xFF0F766E))
                        )
                        categoryOptions.forEach { (catName, catIcon, catAccent) ->
                            val isSelected = category.equals(catName, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) catAccent else MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(1.dp, if (isSelected) catAccent else OutlineVariant.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        category = catName
                                        when (catName) {
                                            "Exam" -> {
                                                type = "EXAM_PREP"
                                                weightage = "30% Exam"
                                            }
                                            "Assignment" -> {
                                                type = "ASSIGNMENT"
                                                weightage = "15% Weightage"
                                            }
                                            "Project" -> {
                                                type = "PROJECT"
                                                weightage = "25% Project"
                                            }
                                        }
                                    }
                                    .testTag("category_tag_$catName")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = catIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp),
                                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = catName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Subject picker
                Column {
                    Text("Subject", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(subjects) { subj ->
                            val isSelected = subj.code == selectedSubject.code
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedSubject = subj },
                                label = { Text(subj.code) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Type picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ASSIGNMENT", "PROJECT", "LAB", "EXAM_PREP").forEach { t ->
                        val isSelected = type == t
                        FilterChip(
                            selected = isSelected,
                            onClick = { type = t },
                            label = { Text(if (t == "EXAM_PREP") "EXAM" else t, fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
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
                        trailingIcon = {
                            IconButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.testTag("btn_open_date_picker_dialog")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Open Date Picker",
                                    tint = Primary
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("task_due_date_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dueTime,
                        onValueChange = { dueTime = it },
                        label = { Text("Time") },
                        modifier = Modifier
                            .weight(0.9f)
                            .testTag("task_due_time_input"),
                        singleLine = true
                    )
                }

                // Quick Date Preset Chips & DatePicker Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = { showDatePicker = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_pick_date_preset")
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

                // Priority
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("URGENT", "HIGH", "MEDIUM").forEach { p ->
                        val isSelected = priority == p
                        FilterChip(
                            selected = isSelected,
                            onClick = { priority = p },
                            label = { Text(p, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            title.trim(),
                            selectedSubject.code,
                            selectedSubject.name,
                            category,
                            type,
                            dueDate.trim(),
                            dueTime.trim(),
                            priority,
                            weightage,
                            note.trim()
                        )
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_task_btn")
            ) {
                Text("Save Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
