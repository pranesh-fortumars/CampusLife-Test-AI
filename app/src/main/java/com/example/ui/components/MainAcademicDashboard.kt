package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
    onAddTask: (title: String, subjectCode: String, subjectName: String, type: String, dueDate: String, dueTime: String, priority: String, weightage: String, note: String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onNavigateToTrack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Pending", "Assignments", "Projects", "Completed"

    val pendingTasks = remember(tasks) { tasks.filter { !it.isCompleted } }
    val completedTasks = remember(tasks) { tasks.filter { it.isCompleted } }
    val dueSoonTasks = remember(tasks) {
        tasks.filter { !it.isCompleted && (it.priority == "URGENT" || it.dueDate.contains("Today", ignoreCase = true) || it.dueDate.contains("Tomorrow", ignoreCase = true)) }
    }
    val projectTasks = remember(tasks) { tasks.filter { it.type == "PROJECT" } }

    val nextDueTask = remember(pendingTasks) {
        pendingTasks.minByOrNull { it.dueTimestamp }
    }

    val filteredTasks = remember(tasks, selectedFilter) {
        when (selectedFilter) {
            "Pending" -> tasks.filter { !it.isCompleted }
            "Assignments" -> tasks.filter { it.type == "ASSIGNMENT" || it.type == "LAB" }
            "Projects" -> tasks.filter { it.type == "PROJECT" }
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
        // 1. Material 3 Greeting Section
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                        Text(
                            text = "Good morning, ${profile.name.split(" ").first()} 👋",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${profile.title} • DTU",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = Primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Thursday, 24 October • Semester 6 Core",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Primary
                            )
                        }
                    }

                    // Attendance & Status Badge
                    Surface(
                        shape = RoundedCornerShape(100),
                        color = if (profile.overallAttendance >= 75.0) TertiaryFixed else ErrorContainer,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
                                color = if (profile.overallAttendance >= 75.0) OnTertiaryFixed else OnErrorContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Summary Cards Grid for Upcoming Deadlines
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pending Deadlines
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AssignmentLate, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PENDING", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${pendingTasks.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Primary)
                            Text("Active tasks", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Due Soon (< 48 hrs)
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = if (dueSoonTasks.isNotEmpty()) ErrorContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HourglassTop, contentDescription = null, tint = if (dueSoonTasks.isNotEmpty()) Error else Primary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("DUE SOON", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = if (dueSoonTasks.isNotEmpty()) Error else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${dueSoonTasks.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (dueSoonTasks.isNotEmpty()) Error else MaterialTheme.colorScheme.onSurface)
                            Text("Next 48 hrs", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Major Projects
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Code, contentDescription = null, tint = Secondary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PROJECTS", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${projectTasks.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Secondary)
                            Text("Capstones", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Completed
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = TertiaryFixed.copy(alpha = 0.35f)
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

        // 2. Hero Card for the Nearest Impending Academic Deadline
        if (nextDueTask != null) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
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
                            color = if (nextDueTask.priority == "URGENT") ErrorContainer else SecondaryFixed
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (nextDueTask.priority == "URGENT") Icons.Default.PriorityHigh else Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = if (nextDueTask.priority == "URGENT") Error else Secondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "NEXT DUE: ${nextDueTask.dueDate.uppercase()} • ${nextDueTask.dueTime}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (nextDueTask.priority == "URGENT") Error else OnSecondaryFixed
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryFixed
                        ) {
                            Text(
                                text = nextDueTask.subjectCode,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
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
                            Text("Mark Complete")
                        }

                        FilledTonalButton(
                            onClick = onNavigateToTrack,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("All Subjects")
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
                    Text("Add Deadline", style = MaterialTheme.typography.labelSmall)
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
                    "Assignments" to tasks.count { it.type == "ASSIGNMENT" || it.type == "LAB" },
                    "Projects" to projectTasks.size,
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
                        )
                    )
                }
            }

            // Task Deadline Cards List
            if (filteredTasks.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
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
            onConfirm = { title, subjectCode, subjectName, type, dueDate, dueTime, priority, weightage, note ->
                onAddTask(title, subjectCode, subjectName, type, dueDate, dueTime, priority, weightage, note)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AcademicDeadlineItemCard(
    task: AcademicTask,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val isUrgent = task.priority == "URGENT" || task.priority == "HIGH"
    val priorityColor = when (task.priority) {
        "URGENT" -> Error
        "HIGH" -> Color(0xFFC2410C) // Amber / Orange
        "MEDIUM" -> Secondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceContainerLowest
        ),
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (task.type == "PROJECT") SecondaryFixed else PrimaryFixed
                    ) {
                        Text(
                            text = task.type,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (task.type == "PROJECT") Secondary else Primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
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

                    if (!task.isCompleted && isUrgent) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ErrorContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = task.priority,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = priorityColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = task.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            )

            if (task.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = if (!task.isCompleted && isUrgent) Error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${task.dueDate} • ${task.dueTime}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        fontWeight = if (!task.isCompleted && isUrgent) FontWeight.Bold else FontWeight.Normal,
                        color = if (!task.isCompleted && isUrgent) Error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("•", color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        text = task.weightage,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = onToggleComplete,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (task.isCompleted) TertiaryFixed else MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = if (task.isCompleted) Tertiary else MaterialTheme.colorScheme.onSurface
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("task_toggle_${task.id}")
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (task.isCompleted) "Done" else "Mark Done",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AddAcademicDeadlineDialog(
    subjects: List<Subject>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, subjectCode: String, subjectName: String, type: String, dueDate: String, dueTime: String, priority: String, weightage: String, note: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedSubjectCode by remember { mutableStateOf(subjects.firstOrNull()?.code ?: "CS-302") }
    var selectedType by remember { mutableStateOf("ASSIGNMENT") }
    var dueDate by remember { mutableStateOf("Tomorrow, Oct 25") }
    var dueTime by remember { mutableStateOf("11:59 PM") }
    var selectedPriority by remember { mutableStateOf("HIGH") }
    var weightage by remember { mutableStateOf("15% Internal") }
    var note by remember { mutableStateOf("") }

    val selectedSubject = subjects.find { it.code == selectedSubjectCode }
    val subjectName = selectedSubject?.name ?: "Academic Coursework"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Academic Deadline", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Assignment or Project Title") },
                    placeholder = { Text("e.g. Distributed Consensus Lab 3") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Select Subject Chips
                Text("Select Subject:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(subjects) { sub ->
                        val isSelected = sub.code == selectedSubjectCode
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSubjectCode = sub.code },
                            label = { Text(sub.code, fontSize = 11.sp) }
                        )
                    }
                }

                // Type Chips: ASSIGNMENT, PROJECT, LAB
                Text("Deadline Category:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("ASSIGNMENT", "PROJECT", "LAB").forEach { t ->
                        val isSelected = selectedType == t
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedType = t },
                            label = { Text(t, fontSize = 11.sp) }
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )
                    OutlinedTextField(
                        value = dueTime,
                        onValueChange = { dueTime = it },
                        label = { Text("Time") },
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                }

                // Priority Chips
                Text("Priority Level:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("URGENT", "HIGH", "MEDIUM", "LOW").forEach { p ->
                        val isSelected = selectedPriority == p
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPriority = p },
                            label = { Text(p, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = weightage,
                    onValueChange = { weightage = it },
                    label = { Text("Grade Weightage (e.g. 15% Lab)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Notes / Instructions (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, selectedSubjectCode, subjectName, selectedType, dueDate, dueTime, selectedPriority, weightage, note)
                    }
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(8.dp)
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
}
