package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AttendanceRecord
import com.example.model.AttendanceType
import com.example.model.Subject
import com.example.viewmodel.CampusLifeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen to log daily class attendance and calculate the overall percentage for each course.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyAttendanceScreen(
    viewModel: CampusLifeViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val subjects by viewModel.subjects.collectAsState()
    val attendanceHistory by viewModel.attendanceHistory.collectAsState()

    var selectedFilter by remember { mutableStateOf("All Courses") }
    var showNoteDialogForCode by remember { mutableStateOf<String?>(null) }
    var noteText by remember { mutableStateOf("") }
    var noteType by remember { mutableStateOf(AttendanceType.ATTENDED) }

    val todayFormatted = remember {
        SimpleDateFormat("EEEE, MMM d, yyyy", Locale.US).format(Date())
    }

    // Overall aggregated metrics
    val totalAttended = subjects.sumOf { it.attendedClasses }
    val totalClasses = subjects.sumOf { it.totalClasses }
    val overallPercentage = if (totalClasses == 0) 100.0 else (totalAttended.toDouble() / totalClasses) * 100.0
    val safeCoursesCount = subjects.count { it.attendancePercentage >= it.targetPercentage }
    val atRiskCoursesCount = subjects.count { it.attendancePercentage < it.targetPercentage }

    val filteredSubjects = remember(subjects, selectedFilter) {
        when (selectedFilter) {
            "Below 75%" -> subjects.filter { it.attendancePercentage < it.targetPercentage }
            "Safe (≥ 75%)" -> subjects.filter { it.attendancePercentage >= it.targetPercentage }
            else -> subjects
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Daily Attendance Logger",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = todayFormatted,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("daily_attendance_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("daily_attendance_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overall Academic Attendance Card
            item {
                OverallAttendanceSummaryCard(
                    overallPercentage = overallPercentage,
                    totalAttended = totalAttended,
                    totalClasses = totalClasses,
                    safeCourses = safeCoursesCount,
                    atRiskCourses = atRiskCoursesCount,
                    totalCourses = subjects.size
                )
            }

            // Quick Batch Actions & Filter Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Batch mark all present button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                subjects.forEach { sub ->
                                    viewModel.quickLogClass(sub.code, isAttended = true)
                                }
                            }
                            .testTag("mark_all_present_button"),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DoneAll,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "One-Tap Full Day Log",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Mark all ${subjects.size} courses as Present today",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            FilledTonalButton(
                                onClick = {
                                    subjects.forEach { sub ->
                                        viewModel.quickLogClass(sub.code, isAttended = true)
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Present All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Filter chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All Courses", "Below 75%", "Safe (≥ 75%)").forEach { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter, fontSize = 12.sp) },
                                modifier = Modifier.testTag("filter_chip_$filter")
                            )
                        }
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Courses Attendance (${filteredSubjects.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap to log daily session",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Course Attendance Items
            items(filteredSubjects, key = { it.code }) { subject ->
                DailyCourseAttendanceCard(
                    subject = subject,
                    onLog = { type ->
                        viewModel.logAttendance(subject.code, type)
                    },
                    onOpenNoteDialog = { type ->
                        showNoteDialogForCode = subject.code
                        noteType = type
                        noteText = ""
                    },
                    onUndo = {
                        viewModel.undoLastAttendance(subject.code)
                    }
                )
            }

            // Recent Daily History Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Daily Log History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${attendanceHistory.size} logged sessions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (attendanceHistory.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EventAvailable,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "No daily attendance logged yet today",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Tap 'Present (+1)' or 'Absent' on any course above to record your attendance.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(attendanceHistory.take(8), key = { it.id }) { record ->
                    DailyAttendanceHistoryRow(
                        record = record,
                        onUndo = {
                            viewModel.undoLastAttendance(record.subjectCode)
                        }
                    )
                }
            }
        }
    }

    // Optional dialog for logging attendance with custom note
    showNoteDialogForCode?.let { code ->
        val subject = subjects.find { it.code == code }
        AlertDialog(
            onDismissRequest = { showNoteDialogForCode = null },
            title = {
                Text(
                    text = "Log ${noteType.name.lowercase().replaceFirstChar { it.uppercase() }} with Note",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Course: ${subject?.name ?: code} ($code)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Session Note (Optional)") },
                        placeholder = { Text("e.g. Covered Chapter 5, Substitute faculty") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("attendance_note_input"),
                        singleLine = false,
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.logAttendance(code, noteType, note = noteText)
                        showNoteDialogForCode = null
                    },
                    modifier = Modifier.testTag("confirm_note_log_button")
                ) {
                    Text("Record Log")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialogForCode = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Top summary card showing overall calculated attendance percentage across all courses.
 */
@Composable
fun OverallAttendanceSummaryCard(
    overallPercentage: Double,
    totalAttended: Int,
    totalClasses: Int,
    safeCourses: Int,
    atRiskCourses: Int,
    totalCourses: Int
) {
    val animatedPercent by animateFloatAsState(
        targetValue = overallPercentage.toFloat(),
        label = "overallPercentAnim"
    )

    val isSafe = overallPercentage >= 75.0
    val cardColor = if (isSafe) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val onCardColor = if (isSafe) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("overall_attendance_summary_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "OVERALL ATTENDANCE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = onCardColor.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f%%", animatedPercent),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = onCardColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSafe) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isSafe) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isSafe) "Above 75% Safe" else "Attendance Shortage",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Progress bar with 75% target line
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(onCardColor.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (overallPercentage / 100.0).toFloat().coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (isSafe) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Statutory NAAC Target: 75.0%",
                        style = MaterialTheme.typography.labelSmall,
                        color = onCardColor.copy(alpha = 0.75f)
                    )
                    Text(
                        text = "$totalAttended / $totalClasses Classes",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = onCardColor
                    )
                }
            }

            HorizontalDivider(color = onCardColor.copy(alpha = 0.15f))

            // Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MetricColumn(
                    value = "$safeCourses",
                    label = "Safe Courses",
                    color = Color(0xFF2E7D32)
                )
                MetricColumn(
                    value = "$atRiskCourses",
                    label = "At-Risk Courses",
                    color = if (atRiskCourses > 0) MaterialTheme.colorScheme.error else onCardColor
                )
                MetricColumn(
                    value = "$totalCourses",
                    label = "Total Courses",
                    color = onCardColor
                )
            }
        }
    }
}

@Composable
private fun MetricColumn(
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Individual course card showing calculated course attendance percentage and quick log actions.
 */
@Composable
fun DailyCourseAttendanceCard(
    subject: Subject,
    onLog: (AttendanceType) -> Unit,
    onOpenNoteDialog: (AttendanceType) -> Unit,
    onUndo: () -> Unit
) {
    val percentage = subject.attendancePercentage
    val isSafe = percentage >= subject.targetPercentage
    val targetPct = subject.targetPercentage

    val statusColor = when {
        percentage >= targetPct -> Color(0xFF2E7D32)
        percentage >= targetPct - 5.0 -> Color(0xFFE65100)
        else -> MaterialTheme.colorScheme.error
    }

    val statusText = when {
        percentage >= targetPct -> "Safe"
        percentage >= targetPct - 5.0 -> "Near 75%"
        else -> "Shortage"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("course_attendance_card_${subject.code}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Code, Name, Credits, and Current Percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = subject.code,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "${subject.credits} Credits",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${subject.faculty} • ${subject.room}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Calculated Course Percentage Badge
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f%%", percentage),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = statusColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Calculated Attendance Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (percentage / 100.0).toFloat().coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(statusColor)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${subject.attendedClasses} attended / ${subject.totalClasses} total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isSafe) {
                            "Can bunk ${subject.safeBunksLeft} more"
                        } else {
                            "Need ${subject.neededToReach75} classes to 75%"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSafe) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Action Buttons Row: Present, Absent, Cancelled, Undo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Present (+1) Button
                FilledTonalButton(
                    onClick = { onLog(AttendanceType.ATTENDED) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("log_present_${subject.code}"),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF2E7D32).copy(alpha = 0.15f),
                        contentColor = Color(0xFF2E7D32)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Present (+1)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Absent (+1 Missed) Button
                FilledTonalButton(
                    onClick = { onLog(AttendanceType.MISSED) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("log_absent_${subject.code}"),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Absent",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // More Options: Note or Undo
                IconButton(
                    onClick = { onOpenNoteDialog(AttendanceType.ATTENDED) },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("log_note_${subject.code}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.EditNote,
                        contentDescription = "Log with note",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onUndo,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("undo_log_${subject.code}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo last log",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Row displaying a single logged attendance record.
 */
@Composable
fun DailyAttendanceHistoryRow(
    record: AttendanceRecord,
    onUndo: () -> Unit
) {
    val statusColor = when (record.type) {
        AttendanceType.ATTENDED -> Color(0xFF2E7D32)
        AttendanceType.MISSED -> MaterialTheme.colorScheme.error
        AttendanceType.CANCELLED -> MaterialTheme.colorScheme.outline
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("attendance_history_row_${record.id}"),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Column {
                    Text(
                        text = "${record.subjectCode} • ${record.subjectName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "${record.date}, ${record.time}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (record.note.isNotBlank()) {
                            Text(
                                text = "• \"${record.note}\"",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = statusColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = record.type.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}
