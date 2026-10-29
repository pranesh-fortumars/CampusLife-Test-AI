package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AttendanceRecord
import com.example.model.AttendanceType
import com.example.model.Subject
import com.example.ui.components.TaskDatePickerDialog
import com.example.ui.components.TaskDateUtils
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Attendance Management Screen
 *
 * Allows students to:
 * 1. Log class sessions (Present, Absent, Cancelled, custom date with M3 DatePicker and notes)
 * 2. Calculate current overall and per-course attendance percentage in real-time
 * 3. Use an interactive "What-If" simulator / Bunk Calculator to project percentage changes
 * 4. Calibrate attended/missed counts directly against college portal numbers
 * 5. Add custom courses and review full session history with undo capability
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceManagementScreen(
    viewModel: CampusLifeViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val subjects by viewModel.subjects.collectAsState()
    val attendanceHistory by viewModel.attendanceHistory.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Courses & Logging, 1: % Calculator, 2: History
    val tabTitles = listOf("Courses & Logging", "Live % Calculator", "Log History")

    // Modals state
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var calibratingSubject by remember { mutableStateOf<Subject?>(null) }
    var customLogSubject by remember { mutableStateOf<Subject?>(null) }

    // Search and filter state
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Below 75%", "Borderline 75-80%", "Safe ≥80%"
    var sortBy by remember { mutableStateOf("Urgent First") } // "Urgent First", "Highest %", "Code"

    val todayFormatted = remember {
        SimpleDateFormat("EEEE, MMM d, yyyy", Locale.US).format(Date())
    }

    // Overall aggregate calculations
    val totalAttended = subjects.sumOf { it.attendedClasses }
    val totalClasses = subjects.sumOf { it.totalClasses }
    val overallPercentage = if (totalClasses == 0) 100.0 else (totalAttended.toDouble() / totalClasses) * 100.0
    val safeCoursesCount = subjects.count { it.attendancePercentage >= it.targetPercentage }
    val atRiskCoursesCount = subjects.count { it.attendancePercentage < it.targetPercentage }
    val totalSafeBunksAcross = subjects.sumOf { it.safeBunksLeft }

    // Filter & sort logic
    val filteredSubjects = remember(subjects, searchQuery, selectedFilter, sortBy) {
        var list = subjects
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                        it.code.lowercase().contains(q) ||
                        it.faculty.lowercase().contains(q)
            }
        }
        list = when (selectedFilter) {
            "Below 75%" -> list.filter { it.attendancePercentage < it.targetPercentage }
            "Borderline 75-80%" -> list.filter { it.attendancePercentage >= it.targetPercentage && it.attendancePercentage < 80.0 }
            "Safe ≥80%" -> list.filter { it.attendancePercentage >= 80.0 }
            else -> list
        }
        when (sortBy) {
            "Urgent First" -> list.sortedBy { it.attendancePercentage }
            "Highest %" -> list.sortedByDescending { it.attendancePercentage }
            "Code" -> list.sortedBy { it.code }
            else -> list
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Attendance Management",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Real-time % Calculator • Class Logger",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("attendance_mgmt_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { showAddSubjectDialog = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(100),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("add_subject_top_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Course", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (activeTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = {
                        subjects.forEach { sub ->
                            viewModel.quickLogClass(sub.code, isAttended = true)
                        }
                    },
                    icon = { Icon(Icons.Default.DoneAll, contentDescription = null) },
                    text = { Text("Log All Present Today") },
                    containerColor = Primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_mark_all_present")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Mode Tabs (Courses & Logging / Live % Calculator / Log History)
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Primary
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        icon = {
                            val icon = when (index) {
                                0 -> if (activeTab == 0) Icons.Filled.FactCheck else Icons.Outlined.FactCheck
                                1 -> if (activeTab == 1) Icons.Filled.Calculate else Icons.Outlined.Calculate
                                else -> if (activeTab == 2) Icons.Filled.History else Icons.Outlined.History
                            }
                            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.testTag("attendance_tab_$index")
                    )
                }
            }

            when (activeTab) {
                0 -> {
                    // TAB 1: COURSES & LOGGING
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("attendance_courses_list"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Overall Calculated Attendance Summary Card
                        item {
                            AttendanceAggregateMetricsCard(
                                overallPercentage = overallPercentage,
                                totalAttended = totalAttended,
                                totalClasses = totalClasses,
                                safeCourses = safeCoursesCount,
                                atRiskCourses = atRiskCoursesCount,
                                totalCourses = subjects.size,
                                totalSafeBunks = totalSafeBunksAcross
                            )
                        }

                        // Search and Filter Tools
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Search course by name, code, faculty...", fontSize = 13.sp) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("attendance_search_input")
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val filters = listOf(
                                        "All" to subjects.size,
                                        "Below 75%" to atRiskCoursesCount,
                                        "Borderline 75-80%" to subjects.count { it.attendancePercentage >= 75.0 && it.attendancePercentage < 80.0 },
                                        "Safe ≥80%" to subjects.count { it.attendancePercentage >= 80.0 }
                                    )
                                    items(filters) { (filter, count) ->
                                        val isSelected = selectedFilter == filter
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedFilter = filter },
                                            label = { Text("$filter ($count)", fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Primary,
                                                selectedLabelColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(100),
                                            modifier = Modifier.testTag("filter_chip_$filter")
                                        )
                                    }
                                }
                            }
                        }

                        // Header with Count
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Enrolled Courses (${filteredSubjects.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = todayFormatted,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (filteredSubjects.isEmpty()) {
                            item {
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
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FilterListOff,
                                            contentDescription = null,
                                            tint = Primary,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Text(
                                            text = "No courses match the selected filter",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Try switching the filter chip or clear the search query.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(filteredSubjects, key = { it.code }) { subject ->
                                AttendanceCourseItemCard(
                                    subject = subject,
                                    onLog = { type -> viewModel.logAttendance(subject.code, type) },
                                    onCustomLog = { customLogSubject = subject },
                                    onCalibrate = { calibratingSubject = subject },
                                    onUndo = { viewModel.undoLastAttendance(subject.code) }
                                )
                            }
                        }

                        // Extra bottom spacing for FAB
                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }

                1 -> {
                    // TAB 2: LIVE % CALCULATOR & WHAT-IF SIMULATOR
                    AttendanceSimulatorTabContent(
                        subjects = subjects,
                        onUpdateCounts = { code, attended, missed ->
                            viewModel.updateSubjectCounts(code, attended, missed)
                        }
                    )
                }

                2 -> {
                    // TAB 3: LOG HISTORY
                    AttendanceLogHistoryTabContent(
                        history = attendanceHistory,
                        onUndo = { code -> viewModel.undoLastAttendance(code) }
                    )
                }
            }
        }
    }

    // Modal 1: Custom Class Log Dialog (with M3 DatePicker, time, and note)
    customLogSubject?.let { subject ->
        CustomClassLogDialog(
            subject = subject,
            onDismiss = { customLogSubject = null },
            onConfirm = { type, date, time, note ->
                viewModel.logAttendance(
                    subjectCode = subject.code,
                    type = type,
                    note = note,
                    customDate = date,
                    customTime = time
                )
                customLogSubject = null
            }
        )
    }

    // Modal 2: Direct Manual Calibration Dialog
    calibratingSubject?.let { subject ->
        CalibrateAttendanceDialog(
            subject = subject,
            onDismiss = { calibratingSubject = null },
            onConfirm = { attended, missed, target ->
                viewModel.updateSubjectCounts(subject.code, attended, missed, target)
                calibratingSubject = null
            }
        )
    }

    // Modal 3: Add New Subject Dialog
    if (showAddSubjectDialog) {
        AddNewSubjectDialog(
            onDismiss = { showAddSubjectDialog = false },
            onConfirm = { code, name, credits, faculty, attended, missed, target ->
                viewModel.addNewSubject(
                    code = code,
                    name = name,
                    credits = credits,
                    faculty = faculty,
                    attended = attended,
                    missed = missed,
                    targetPercentage = target
                )
                showAddSubjectDialog = false
            }
        )
    }
}

/**
 * Top Summary Card calculating and displaying current aggregate attendance across all courses.
 */
@Composable
fun AttendanceAggregateMetricsCard(
    overallPercentage: Double,
    totalAttended: Int,
    totalClasses: Int,
    safeCourses: Int,
    atRiskCourses: Int,
    totalCourses: Int,
    totalSafeBunks: Int
) {
    val animatedPercent by animateFloatAsState(
        targetValue = overallPercentage.toFloat(),
        label = "overallPercentAnim"
    )

    val isSafe = overallPercentage >= 75.0
    val statusColor = if (isSafe) Tertiary else Error
    val cardBg = if (isSafe) TertiaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
    val onCardColor = if (isSafe) OnTertiaryContainer else MaterialTheme.colorScheme.onErrorContainer

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.2.dp, statusColor.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("attendance_overall_summary_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Metric Title, Badge & Big Percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AGGREGATE SEMESTER ATTENDANCE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = onCardColor.copy(alpha = 0.8f),
                            letterSpacing = 0.8.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f%%", animatedPercent),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = onCardColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(100),
                    color = statusColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isSafe) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (isSafe) "Above 75% Safe" else "Shortage Warning",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Progress Bar with 75% Statutory Target Line
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
                            .background(statusColor)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Statutory Target: 75.0% NAAC Minimum",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = onCardColor.copy(alpha = 0.75f)
                    )
                    Text(
                        text = "$totalAttended / $totalClasses Classes",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = onCardColor
                    )
                }
            }

            HorizontalDivider(color = onCardColor.copy(alpha = 0.15f))

            // Aggregated Summary Metric Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                SummaryMetricItem(
                    label = "Safe Courses",
                    value = "$safeCourses / $totalCourses",
                    valueColor = Tertiary
                )
                SummaryMetricItem(
                    label = "At-Risk Courses",
                    value = "$atRiskCourses",
                    valueColor = if (atRiskCourses > 0) Error else onCardColor
                )
                SummaryMetricItem(
                    label = "Safe Bunks Left",
                    value = "$totalSafeBunks Across",
                    valueColor = if (totalSafeBunks > 0) Primary else Warning
                )
            }
        }
    }
}

@Composable
private fun SummaryMetricItem(
    label: String,
    value: String,
    valueColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Individual Course Card in the Attendance Management Screen with live % calculation,
 * progress bar, bunk/recovery insights, and quick logging buttons.
 */
@Composable
fun AttendanceCourseItemCard(
    subject: Subject,
    onLog: (AttendanceType) -> Unit,
    onCustomLog: () -> Unit,
    onCalibrate: () -> Unit,
    onUndo: () -> Unit
) {
    val percentage = subject.attendancePercentage
    val isSafe = percentage >= subject.targetPercentage
    val targetPct = subject.targetPercentage

    val statusColor = when {
        percentage >= 80.0 -> Tertiary
        percentage >= targetPct -> Warning
        else -> Error
    }

    val statusLabel = when {
        percentage >= 80.0 -> "Safe (≥80%)"
        percentage >= targetPct -> "Borderline (75-80%)"
        else -> "Shortage (<75%)"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(
            1.dp,
            if (!isSafe) Error.copy(alpha = 0.4f) else OutlineVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("attendance_course_card_${subject.code}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Row 1: Code Badge, Credits, Name & Live Percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryContainer
                        ) {
                            Text(
                                text = subject.code,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "${subject.credits} Credits",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${subject.faculty} • ${subject.room}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                // Live Calculated Percentage Display
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f%%", percentage),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Progress Bar & Counts
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
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
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isSafe) {
                            "Can bunk ${subject.safeBunksLeft} more safely"
                        } else {
                            "Need ${subject.neededToReach75} classes to 75%"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSafe) Tertiary else Error
                    )
                }
            }

            HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f))

            // Action Buttons: Present (+1), Absent (+1), Custom Log, Calibrate, Undo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Present (+1)
                Button(
                    onClick = { onLog(AttendanceType.ATTENDED) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Tertiary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("log_present_${subject.code}")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Present", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Absent (+1)
                OutlinedButton(
                    onClick = { onLog(AttendanceType.MISSED) },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Error.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("log_absent_${subject.code}")
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Absent", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Custom Log with Date / Note
                IconButton(
                    onClick = onCustomLog,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("log_custom_${subject.code}")
                ) {
                    Icon(
                        imageVector = Icons.Default.EditCalendar,
                        contentDescription = "Log with custom date or note",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Calibrate direct counts
                IconButton(
                    onClick = onCalibrate,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("calibrate_${subject.code}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Calibrate attendance counts",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Undo
                IconButton(
                    onClick = onUndo,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("undo_${subject.code}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo last attendance log",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * TAB 2: Interactive Live Percentage Calculator & What-If Bunk Simulator.
 */
@Composable
fun AttendanceSimulatorTabContent(
    subjects: List<Subject>,
    onUpdateCounts: (code: String, attended: Int, missed: Int) -> Unit
) {
    if (subjects.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No subjects enrolled to simulate.")
        }
        return
    }

    var selectedCode by remember { mutableStateOf(subjects.first().code) }
    var attendSimCount by remember { mutableIntStateOf(3) }
    var missSimCount by remember { mutableIntStateOf(0) }
    var selectedTargetGoal by remember { mutableDoubleStateOf(75.0) }

    val activeSubject = subjects.find { it.code == selectedCode } ?: subjects.first()
    val currentPercentage = activeSubject.attendancePercentage
    val projectedPercentage = activeSubject.calculateProjected(attendSimCount, missSimCount)
    val percentageDelta = projectedPercentage - currentPercentage

    val isProjectedSafe = projectedPercentage >= selectedTargetGoal

    val neededToReachGoal = calculateClassesNeeded(
        attended = activeSubject.attendedClasses,
        total = activeSubject.totalClasses,
        targetPercentage = selectedTargetGoal
    )
    val bunksAllowedUnderGoal = calculateBunksAllowed(
        attended = activeSubject.attendedClasses,
        total = activeSubject.totalClasses,
        targetPercentage = selectedTargetGoal
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("attendance_simulator_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Course Selector Carousel
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "SELECT COURSE TO SIMULATE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(subjects) { sub ->
                        val isSelected = sub.code == selectedCode
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCode = sub.code },
                            label = { Text("${sub.code} (${String.format(Locale.US, "%.0f%%", sub.attendancePercentage)})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(100),
                            modifier = Modifier.testTag("sim_chip_${sub.code}")
                        )
                    }
                }
            }
        }

        // Live Projection Result Hero Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isProjectedSafe) TertiaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.2.dp, if (isProjectedSafe) Tertiary else Error),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sim_projection_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PROJECTED ATTENDANCE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${activeSubject.code} • ${activeSubject.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(100),
                            color = if (isProjectedSafe) Tertiary else Error
                        ) {
                            Text(
                                text = if (isProjectedSafe) "Safe Projected" else "Shortage Projected",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Before vs After Display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Current Baseline", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = String.format(Locale.US, "%.1f%%", currentPercentage),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text("${activeSubject.attendedClasses} / ${activeSubject.totalClasses}", style = MaterialTheme.typography.bodySmall)
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(24.dp)
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Text("After Simulation", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = String.format(Locale.US, "%.1f%%", projectedPercentage),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isProjectedSafe) Tertiary else Error
                            )
                            val deltaSign = if (percentageDelta >= 0) "+" else ""
                            Text(
                                text = "$deltaSign${String.format(Locale.US, "%.1f%%", percentageDelta)} change",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (percentageDelta >= 0) Tertiary else Error
                            )
                        }
                    }
                }
            }
        }

        // Stepper Controls: Attend Next vs Miss Next
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Hypothetical Class Sessions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    // Stepper 1: Attend Next Classes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(TertiaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Tertiary, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Attend Next Classes", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Consecutive lectures present", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalIconButton(
                                onClick = { if (attendSimCount > 0) attendSimCount-- },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("sim_attend_decrement")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrement")
                            }
                            Text(
                                text = "$attendSimCount",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(28.dp),
                                textAlign = TextAlign.Center
                            )
                            FilledTonalIconButton(
                                onClick = { if (attendSimCount < 25) attendSimCount++ },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("sim_attend_increment")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increment")
                            }
                        }
                    }

                    HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f))

                    // Stepper 2: Miss Next Classes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.errorContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Error, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Miss Next Classes (Bunks)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Consecutive lectures absent", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalIconButton(
                                onClick = { if (missSimCount > 0) missSimCount-- },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("sim_miss_decrement")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrement")
                            }
                            Text(
                                text = "$missSimCount",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(28.dp),
                                textAlign = TextAlign.Center
                            )
                            FilledTonalIconButton(
                                onClick = { if (missSimCount < 25) missSimCount++ },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("sim_miss_increment")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increment")
                            }
                        }
                    }
                }
            }
        }

        // Target Goal Planner (75%, 80%, 85%, 90%)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Statutory Target Calculator",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Choose your goal threshold to see the exact number of classes required:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(75.0 to "75% (Min)", 80.0 to "80% (Safe)", 85.0 to "85% (Pro)", 90.0 to "90% (Star)").forEach { (pct, label) ->
                            val isSelected = selectedTargetGoal == pct
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedTargetGoal = pct },
                                label = { Text(label, fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PrimaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                if (currentPercentage < selectedTargetGoal) {
                                    Text(
                                        text = "Must attend $neededToReachGoal consecutive classes",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Error
                                    )
                                    Text(
                                        text = "Attending $neededToReachGoal classes without missing will lift ${activeSubject.code} to ${String.format(Locale.US, "%.1f%%", selectedTargetGoal)}.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text = "You can safely bunk $bunksAllowedUnderGoal classes",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Tertiary
                                    )
                                    Text(
                                        text = "You can miss up to $bunksAllowedUnderGoal more lectures before dropping below ${String.format(Locale.US, "%.1f%%", selectedTargetGoal)}.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * TAB 3: Full Session Log History with type filters and undo support.
 */
@Composable
fun AttendanceLogHistoryTabContent(
    history: List<AttendanceRecord>,
    onUndo: (String) -> Unit
) {
    var typeFilter by remember { mutableStateOf("All") }

    val filteredHistory = remember(history, typeFilter) {
        when (typeFilter) {
            "Present" -> history.filter { it.type == AttendanceType.ATTENDED }
            "Absent" -> history.filter { it.type == AttendanceType.MISSED }
            "Cancelled" -> history.filter { it.type == AttendanceType.CANCELLED }
            else -> history
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Logged Sessions (${filteredHistory.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("All", "Present", "Absent", "Cancelled").forEach { f ->
                    FilterChip(
                        selected = typeFilter == f,
                        onClick = { typeFilter = f },
                        label = { Text(f, fontSize = 10.sp) }
                    )
                }
            }
        }

        if (filteredHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.EventBusy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No attendance logs found", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Tap 'Present' or 'Absent' on any course card to record a session.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredHistory, key = { it.id }) { record ->
                    val statusColor = when (record.type) {
                        AttendanceType.ATTENDED -> Tertiary
                        AttendanceType.MISSED -> Error
                        AttendanceType.CANCELLED -> MaterialTheme.colorScheme.outline
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
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
                                        fontWeight = FontWeight.Bold,
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
                                                text = "• Note: ${record.note}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Primary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = statusColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = record.type.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onUndo(record.subjectCode) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Undo,
                                        contentDescription = "Undo log",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Modal Dialog to log an attendance session with a custom date (via M3 DatePicker), time, and notes.
 */
@Composable
fun CustomClassLogDialog(
    subject: Subject,
    onDismiss: () -> Unit,
    onConfirm: (type: AttendanceType, date: String, time: String, note: String) -> Unit
) {
    var selectedType by remember { mutableStateOf(AttendanceType.ATTENDED) }
    var selectedDate by remember { mutableStateOf(SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date())) }
    var selectedTime by remember { mutableStateOf("10:00 AM") }
    var note by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Session for ${subject.code}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = subject.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Attendance Type Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        AttendanceType.ATTENDED to "Present (+1)",
                        AttendanceType.MISSED to "Absent",
                        AttendanceType.CANCELLED to "Cancelled"
                    ).forEach { (type, label) ->
                        val isSelected = selectedType == type
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedType = type }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Date Picker Field
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { selectedDate = it },
                    label = { Text("Session Date") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Pick Date", tint = Primary)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Time Field
                OutlinedTextField(
                    value = selectedTime,
                    onValueChange = { selectedTime = it },
                    label = { Text("Session Time") },
                    placeholder = { Text("e.g. 10:30 AM") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Optional Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Reason (Optional)") },
                    placeholder = { Text("e.g. Lab viva, Sick leave, Substitute") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedType, selectedDate, selectedTime, note) },
                modifier = Modifier.testTag("confirm_custom_log_btn")
            ) {
                Text("Save Log")
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
            initialSelectedDateMillis = TaskDateUtils.parseDateStringToEpochMillis(selectedDate),
            onDateSelected = { _, formatted ->
                selectedDate = formatted
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

/**
 * Modal Dialog for directly calibrating attended & missed counts against university portal.
 */
@Composable
fun CalibrateAttendanceDialog(
    subject: Subject,
    onDismiss: () -> Unit,
    onConfirm: (attended: Int, missed: Int, target: Double) -> Unit
) {
    var attendedText by remember { mutableStateOf("${subject.attendedClasses}") }
    var missedText by remember { mutableStateOf("${subject.missedClasses}") }
    var targetText by remember { mutableStateOf("${subject.targetPercentage}") }

    val attendedVal = attendedText.toIntOrNull() ?: subject.attendedClasses
    val missedVal = missedText.toIntOrNull() ?: subject.missedClasses
    val totalVal = attendedVal + missedVal
    val previewPct = if (totalVal == 0) 100.0 else (attendedVal.toDouble() / totalVal) * 100.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Calibrate ${subject.code} Attendance", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Sync manually with your official university ERP/portal:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = attendedText,
                        onValueChange = { attendedText = it.filter { char -> char.isDigit() } },
                        label = { Text("Attended") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = missedText,
                        onValueChange = { missedText = it.filter { char -> char.isDigit() } },
                        label = { Text("Missed") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Target % (Standard: 75.0)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Resulting Percentage:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = String.format(Locale.US, "%.1f%% (%d/%d)", previewPct, attendedVal, totalVal),
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val targetVal = targetText.toDoubleOrNull() ?: 75.0
                    onConfirm(attendedVal, missedVal, targetVal)
                },
                modifier = Modifier.testTag("confirm_calibrate_btn")
            ) {
                Text("Update & Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Modal Dialog for adding a new enrolled course to the attendance manager.
 */
@Composable
fun AddNewSubjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (code: String, name: String, credits: Int, faculty: String, attended: Int, missed: Int, target: Double) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var creditsText by remember { mutableStateOf("4") }
    var faculty by remember { mutableStateOf("") }
    var attendedText by remember { mutableStateOf("0") }
    var missedText by remember { mutableStateOf("0") }
    var targetText by remember { mutableStateOf("75.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enroll New Course", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Course Code *") },
                    placeholder = { Text("e.g. CS-312") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Course Name *") },
                    placeholder = { Text("e.g. Machine Learning") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = creditsText,
                        onValueChange = { creditsText = it.filter { char -> char.isDigit() } },
                        label = { Text("Credits") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = faculty,
                        onValueChange = { faculty = it },
                        label = { Text("Faculty") },
                        placeholder = { Text("Dr. Sharma") },
                        singleLine = true,
                        modifier = Modifier.weight(1.5f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = attendedText,
                        onValueChange = { attendedText = it.filter { char -> char.isDigit() } },
                        label = { Text("Initial Attended") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = missedText,
                        onValueChange = { missedText = it.filter { char -> char.isDigit() } },
                        label = { Text("Initial Missed") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (code.isNotBlank() && name.isNotBlank()) {
                        val cr = creditsText.toIntOrNull() ?: 4
                        val att = attendedText.toIntOrNull() ?: 0
                        val mis = missedText.toIntOrNull() ?: 0
                        val tgt = targetText.toDoubleOrNull() ?: 75.0
                        onConfirm(code.trim(), name.trim(), cr, faculty.trim().ifBlank { "Faculty" }, att, mis, tgt)
                    }
                },
                enabled = code.isNotBlank() && name.isNotBlank(),
                modifier = Modifier.testTag("save_new_subject_btn")
            ) {
                Text("Enroll Course")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Mathematical helper to calculate how many consecutive classes are needed to reach target percentage.
 */
private fun calculateClassesNeeded(attended: Int, total: Int, targetPercentage: Double): Int {
    val threshold = targetPercentage / 100.0
    val currentPct = if (total == 0) 100.0 else (attended.toDouble() / total) * 100.0
    if (currentPct >= targetPercentage) return 0
    val diff = (threshold * total - attended)
    if (diff <= 0.0) return 0
    return kotlin.math.ceil(diff / (1.0 - threshold)).toInt()
}

/**
 * Mathematical helper to calculate how many consecutive classes can be missed without dropping below target.
 */
private fun calculateBunksAllowed(attended: Int, total: Int, targetPercentage: Double): Int {
    val threshold = targetPercentage / 100.0
    if (threshold <= 0.0) return 99
    val maxAllowed = ((attended - threshold * total) / threshold).toInt()
    return if (maxAllowed < 0) 0 else maxAllowed
}
