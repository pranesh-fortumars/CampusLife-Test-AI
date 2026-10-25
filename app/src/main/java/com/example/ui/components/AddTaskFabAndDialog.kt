package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Subject
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel
import com.example.viewmodel.TaskViewModel
import com.example.worker.TaskNotificationScheduler

/**
 * High-level Composable providing a Floating Action Button and an interactive
 * Material 3 Dialog to compose and insert new academic tasks directly into the Room database.
 */
@Composable
fun AddTaskFabAndDialog(
    subjects: List<Subject> = emptyList(),
    modifier: Modifier = Modifier,
    onTaskAdded: (
        title: String,
        subjectCode: String,
        subjectName: String,
        category: String,
        type: String,
        dueDate: String,
        dueTime: String,
        priority: String,
        weightage: String,
        note: String,
        scheduleReminder: Boolean
    ) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        AddTaskFloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier.testTag("fab_add_task_global")
        )

        if (showDialog) {
            AddTaskDialog(
                subjects = subjects,
                onDismiss = { showDialog = false },
                onConfirm = { title, code, name, category, type, date, time, priority, weight, note, reminder ->
                    onTaskAdded(title, code, name, category, type, date, time, priority, weight, note, reminder)
                    showDialog = false
                }
            )
        }
    }
}

/**
 * Overload wired directly to [CampusLifeViewModel] to insert into Room database
 * and optionally trigger WorkManager upcoming deadlines reminders.
 */
@Composable
fun AddTaskFabAndDialog(
    viewModel: CampusLifeViewModel,
    modifier: Modifier = Modifier
) {
    val subjects by viewModel.subjects.collectAsState()
    val context = LocalContext.current

    AddTaskFabAndDialog(
        subjects = subjects,
        modifier = modifier,
        onTaskAdded = { title, code, name, category, type, date, time, priority, weight, note, scheduleReminder ->
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
            if (scheduleReminder) {
                TaskNotificationScheduler.triggerImmediateDueCheck(context)
                Toast.makeText(context, "Task saved & WorkManager reminder triggered", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Task saved to database", Toast.LENGTH_SHORT).show()
            }
        }
    )
}

/**
 * Overload wired directly to [TaskViewModel] to insert into Room database
 * and optionally trigger WorkManager upcoming deadlines reminders.
 */
@Composable
fun AddTaskFabAndDialog(
    taskViewModel: TaskViewModel,
    subjects: List<Subject> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AddTaskFabAndDialog(
        subjects = subjects,
        modifier = modifier,
        onTaskAdded = { title, code, name, category, type, date, time, priority, weight, note, scheduleReminder ->
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
            if (scheduleReminder) {
                TaskNotificationScheduler.triggerImmediateDueCheck(context)
                Toast.makeText(context, "Task saved & WorkManager reminder triggered", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Task saved to database", Toast.LENGTH_SHORT).show()
            }
        }
    )
}

/**
 * Material 3 Extended Floating Action Button for task creation.
 */
@Composable
fun AddTaskFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "New Task",
    expanded: Boolean = true
) {
    if (expanded) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task Icon",
                    modifier = Modifier.size(20.dp)
                )
            },
            text = {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            ),
            modifier = modifier
                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                .testTag("fab_add_task")
        )
    } else {
        FloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            ),
            modifier = modifier
                .size(56.dp)
                .testTag("fab_add_task_compact")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Task Icon",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Comprehensive Material 3 Dialog for configuring and inserting a new academic task into SQLite via Room.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    subjects: List<Subject> = emptyList(),
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
        note: String,
        scheduleReminder: Boolean
    ) -> Unit
) {
    val defaultSubjectList = if (subjects.isNotEmpty()) {
        subjects
    } else {
        listOf(
            Subject("CS-302", "Database Management Systems", 4, "Dr. Ananya Roy", attendedClasses = 22, totalClasses = 26),
            Subject("CS-304", "Operating Systems", 4, "Prof. K. Muralidhar", attendedClasses = 19, totalClasses = 26),
            Subject("CS-306", "Computer Networks", 4, "Dr. R. Venkat", attendedClasses = 21, totalClasses = 26),
            Subject("CS-308", "Software Engineering", 3, "Prof. Neha Gupta", attendedClasses = 24, totalClasses = 26),
            Subject("CS-310", "Web Engineering", 3, "Prof. S. Sengupta", attendedClasses = 20, totalClasses = 26)
        )
    }

    var title by remember { mutableStateOf("") }
    var selectedSubjectCode by remember { mutableStateOf(defaultSubjectList.first().code) }
    var category by remember { mutableStateOf("Assignment") } // Assignment, Exam, Project, Lab, Quiz
    var priority by remember { mutableStateOf("HIGH") } // URGENT, HIGH, MEDIUM, LOW
    var dueDate by remember { mutableStateOf("Tomorrow") }
    var dueTime by remember { mutableStateOf("11:59 PM") }
    var weightage by remember { mutableStateOf("15% Internal") }
    var note by remember { mutableStateOf("") }
    var scheduleWorkManagerReminder by remember { mutableStateOf(true) }

    val activeSubject = defaultSubjectList.find { it.code == selectedSubjectCode } ?: defaultSubjectList.first()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 680.dp)
                .testTag("add_task_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircleOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "New Academic Task",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Persistent Room DB storage",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("dialog_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close dialog",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Form Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Task Title Input
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title *") },
                        placeholder = { Text("e.g. Socket Lab 4, DBMS Problem Set") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_task_title_input")
                    )

                    // Course / Subject Selection
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Course: ${activeSubject.name}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(defaultSubjectList) { sub ->
                                val isSelected = sub.code == selectedSubjectCode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedSubjectCode = sub.code },
                                    label = { Text(sub.code, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.testTag("chip_subject_${sub.code}")
                                )
                            }
                        }
                    }

                    // Category Selection Chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val categories = listOf("Assignment", "Exam", "Project", "Lab")
                            categories.forEach { cat ->
                                val isSelected = category == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { category = cat },
                                    label = { Text(cat, fontSize = 11.sp) },
                                    leadingIcon = {
                                        when (cat) {
                                            "Assignment" -> Icon(Icons.Default.Assignment, null, modifier = Modifier.size(14.dp))
                                            "Exam" -> Icon(Icons.Default.EventNote, null, modifier = Modifier.size(14.dp))
                                            "Project" -> Icon(Icons.Default.Code, null, modifier = Modifier.size(14.dp))
                                            else -> Icon(Icons.Default.Science, null, modifier = Modifier.size(14.dp))
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    modifier = Modifier.testTag("chip_category_$cat")
                                )
                            }
                        }
                    }

                    // Priority Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Priority Level",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val priorities = listOf("URGENT", "HIGH", "MEDIUM", "LOW")
                            priorities.forEach { prio ->
                                val isSelected = priority == prio
                                val prioColor = when (prio) {
                                    "URGENT" -> MaterialTheme.colorScheme.error
                                    "HIGH" -> Color(0xFFE65100)
                                    "MEDIUM" -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) prioColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    border = if (isSelected) BorderStroke(1.2.dp, prioColor) else null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .defaultMinSize(minHeight = 36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { priority = prio }
                                        .testTag("priority_btn_$prio")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = prio,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) prioColor else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Due Date & Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = dueDate,
                            onValueChange = { dueDate = it },
                            label = { Text("Due Date") },
                            placeholder = { Text("e.g. Tomorrow, Oct 30") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("task_due_date_input")
                        )
                        OutlinedTextField(
                            value = dueTime,
                            onValueChange = { dueTime = it },
                            label = { Text("Due Time") },
                            placeholder = { Text("11:59 PM") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("task_due_time_input")
                        )
                    }

                    // Quick Date Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Today", "Tomorrow", "In 3 Days", "Next Week").forEach { preset ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { dueDate = preset }
                            ) {
                                Text(
                                    text = preset,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Weightage Field
                    OutlinedTextField(
                        value = weightage,
                        onValueChange = { weightage = it },
                        label = { Text("Weightage / Grade Value") },
                        placeholder = { Text("e.g. 15% Internal, 30% Mid-Sem") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_weightage_input")
                    )

                    // Notes / Instructions Field
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Notes / Submission Details") },
                        placeholder = { Text("Optional instructions, repository link, questions...") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_note_input"),
                        maxLines = 3
                    )

                    // WorkManager Reminder Trigger Switch
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "WorkManager Reminder",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Trigger notification for upcoming due date",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = scheduleWorkManagerReminder,
                                onCheckedChange = { scheduleWorkManagerReminder = it },
                                modifier = Modifier.testTag("toggle_workmanager_reminder")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                // Dialog Action Buttons with 48dp minimum touch target
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("cancel_add_task_btn")
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val resolvedType = when (category.uppercase()) {
                                    "EXAM" -> "EXAM_PREP"
                                    "PROJECT" -> "PROJECT"
                                    "LAB" -> "LAB"
                                    else -> "ASSIGNMENT"
                                }
                                onConfirm(
                                    title.trim(),
                                    selectedSubjectCode,
                                    activeSubject.name,
                                    category,
                                    resolvedType,
                                    dueDate.trim().ifBlank { "Tomorrow" },
                                    dueTime.trim().ifBlank { "11:59 PM" },
                                    priority,
                                    weightage.trim(),
                                    note.trim(),
                                    scheduleWorkManagerReminder
                                )
                            }
                        },
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(100),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("dialog_submit_task_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save to Database",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
