package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
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
import com.example.model.Subject
import com.example.ui.components.charts.CourseAttendanceBarChart
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel

/**
 * Simple UI screen that displays a list of courses and tracks attendance percentages
 * for each, using the Room database.
 * Supports real-time attendance logging (+ Present, + Missed) and percentage recalculation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseAttendanceScreen(
    viewModel: CampusLifeViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val subjects by viewModel.subjects.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Below 75%", "Safe"
    var showAddCourseDialog by remember { mutableStateOf(false) }

    // Overall aggregate metrics
    val totalAttended = subjects.sumOf { it.attendedClasses }
    val totalClasses = subjects.sumOf { it.totalClasses }
    val overallPercentage = if (totalClasses == 0) 100.0 else (totalAttended.toDouble() / totalClasses) * 100.0
    val safeCount = subjects.count { it.attendancePercentage >= it.targetPercentage }
    val atRiskCount = subjects.count { it.attendancePercentage < it.targetPercentage }

    val filteredSubjects = remember(subjects, selectedFilter) {
        when (selectedFilter) {
            "Below 75%" -> subjects.filter { it.attendancePercentage < it.targetPercentage }
            "Safe" -> subjects.filter { it.attendancePercentage >= it.targetPercentage }
            else -> subjects
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Course Attendance",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Room SQLite Database • Real-Time % Tracking",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("course_attendance_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { showAddCourseDialog = true },
                        shape = RoundedCornerShape(100),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_add_course_header")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Course", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        modifier = Modifier.testTag("course_attendance_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Overall Attendance Summary Card
            item {
                OverallAttendanceSummaryBanner(
                    overallPercentage = overallPercentage,
                    totalAttended = totalAttended,
                    totalClasses = totalClasses,
                    safeCount = safeCount,
                    atRiskCount = atRiskCount,
                    totalCourses = subjects.size
                )
            }

            // 2. Bar Chart Data Visualization using Charting Library
            item {
                CourseAttendanceBarChart(
                    subjects = subjects,
                    onCourseSelected = { sub ->
                        viewModel.selectSubjectDetail(sub)
                    }
                )
            }

            // 3. Filter Pills
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedFilter == "All",
                        onClick = { selectedFilter = "All" },
                        label = { Text("All Courses (${subjects.size})") },
                        modifier = Modifier.testTag("filter_all_courses")
                    )
                    FilterChip(
                        selected = selectedFilter == "Below 75%",
                        onClick = { selectedFilter = "Below 75%" },
                        label = { Text("Below 75% ($atRiskCount)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.testTag("filter_below_75")
                    )
                    FilterChip(
                        selected = selectedFilter == "Safe",
                        onClick = { selectedFilter = "Safe" },
                        label = { Text("Safe ($safeCount)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.tertiary
                        ),
                        modifier = Modifier.testTag("filter_safe")
                    )
                }
            }

            // 3. Courses List
            if (filteredSubjects.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
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
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "No courses match the selected filter.",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                items(filteredSubjects, key = { it.code }) { subject ->
                    CourseAttendanceCard(
                        subject = subject,
                        onQuickPresent = {
                            viewModel.quickLogClass(subject.code, isAttended = true)
                        },
                        onQuickMissed = {
                            viewModel.quickLogClass(subject.code, isAttended = false)
                        }
                    )
                }
            }
        }
    }

    if (showAddCourseDialog) {
        AddCourseDialog(
            onDismiss = { showAddCourseDialog = false },
            onAddCourse = { code, name, credits, faculty, attended, missed, target ->
                viewModel.addNewSubject(
                    code = code,
                    name = name,
                    credits = credits,
                    faculty = faculty,
                    attended = attended,
                    missed = missed,
                    targetPercentage = target
                )
                showAddCourseDialog = false
            }
        )
    }
}

/**
 * Top summary banner showing aggregate attendance percentage across all courses in Room.
 */
@Composable
fun OverallAttendanceSummaryBanner(
    overallPercentage: Double,
    totalAttended: Int,
    totalClasses: Int,
    safeCount: Int,
    atRiskCount: Int,
    totalCourses: Int
) {
    val isSafe = overallPercentage >= 75.0
    val cardColor = if (isSafe) PrimaryContainer else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
    val accentColor = if (isSafe) Primary else MaterialTheme.colorScheme.error

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("overall_attendance_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Overall Attendance",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.1f", overallPercentage)}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(100),
                    color = accentColor
                ) {
                    Text(
                        text = if (isSafe) "Safe (≥75%)" else "Critical Alert (<75%)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Progress bar
            LinearProgressIndicator(
                progress = { (overallPercentage / 100.0).toFloat().coerceIn(0f, 1f) },
                color = accentColor,
                trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(100))
            )

            // Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$totalAttended attended of $totalClasses classes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$safeCount of $totalCourses courses safe",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSafe) Primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Course item card showing attendance percentage, attended/missed classes,
 * safe bunk margin or needed classes, and quick action logging buttons.
 */
@Composable
fun CourseAttendanceCard(
    subject: Subject,
    onQuickPresent: () -> Unit,
    onQuickMissed: () -> Unit
) {
    val pct = subject.attendancePercentage
    val isSafe = pct >= subject.targetPercentage
    val statusColor = if (isSafe) SuccessGreen else MaterialTheme.colorScheme.error
    val statusContainer = if (isSafe) SuccessGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("course_card_${subject.code.lowercase()}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Code, Name, Percentage Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryContainer
                    ) {
                        Text(
                            text = subject.code,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Percentage Badge
                Surface(
                    shape = RoundedCornerShape(100),
                    color = statusContainer,
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.1f", pct)}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            // Faculty & Class Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${subject.credits} Credits • ${subject.faculty}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                Text(
                    text = "${subject.attendedClasses} / ${subject.totalClasses} Classes",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Attendance Percentage Progress Bar
            LinearProgressIndicator(
                progress = { (pct / 100.0).toFloat().coerceIn(0f, 1f) },
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(100))
            )

            // Status Notice (Safe Bunks vs Needed Classes)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSafe) {
                        "✓ Safe: Can miss ${subject.safeBunksLeft} more classes"
                    } else {
                        "⚠️ At Risk: Need next ${subject.neededToReach75} classes to hit 75%"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor,
                    fontSize = 11.sp
                )

                // Quick Action Buttons (+ Present, + Missed)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = onQuickPresent,
                        shape = RoundedCornerShape(100),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = SuccessGreen.copy(alpha = 0.15f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("btn_present_${subject.code.lowercase()}")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Present", style = MaterialTheme.typography.labelSmall, color = SuccessGreen, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = onQuickMissed,
                        shape = RoundedCornerShape(100),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("btn_missed_${subject.code.lowercase()}")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Missed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Dialog to add a new course to the Room database.
 */
@Composable
fun AddCourseDialog(
    onDismiss: () -> Unit,
    onAddCourse: (
        code: String,
        name: String,
        credits: Int,
        faculty: String,
        attended: Int,
        missed: Int,
        targetPercentage: Double
    ) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var creditsText by remember { mutableStateOf("4") }
    var faculty by remember { mutableStateOf("") }
    var attendedText by remember { mutableStateOf("20") }
    var totalText by remember { mutableStateOf("24") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_course_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Course to Database",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Course Code (e.g. CS-316)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_course_code")
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Course Name (e.g. Compiler Design)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_course_name")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = creditsText,
                        onValueChange = { creditsText = it },
                        label = { Text("Credits") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = faculty,
                        onValueChange = { faculty = it },
                        label = { Text("Instructor / Faculty") },
                        singleLine = true,
                        modifier = Modifier.weight(2f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = attendedText,
                        onValueChange = { attendedText = it },
                        label = { Text("Attended Classes") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = totalText,
                        onValueChange = { totalText = it },
                        label = { Text("Total Classes") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

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
                            val attended = attendedText.toIntOrNull() ?: 0
                            val total = totalText.toIntOrNull() ?: 0
                            val missed = (total - attended).coerceAtLeast(0)
                            val credits = creditsText.toIntOrNull() ?: 3
                            if (code.isNotBlank() && name.isNotBlank()) {
                                onAddCourse(
                                    code.trim().uppercase(),
                                    name.trim(),
                                    credits,
                                    faculty.trim(),
                                    attended,
                                    missed,
                                    75.0
                                )
                            }
                        },
                        enabled = code.isNotBlank() && name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier.testTag("btn_save_course_to_db")
                    ) {
                        Text("Save to Room DB")
                    }
                }
            }
        }
    }
}
