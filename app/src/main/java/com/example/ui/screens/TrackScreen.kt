package com.example.ui.screens

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.MockData
import com.example.model.AttendanceType
import com.example.model.Subject
import com.example.ui.components.AcademicTasksUpcomingDueListComponent
import com.example.ui.components.AddNewSubjectDialog
import com.example.ui.components.CumulativeGpaTrackerComponent
import com.example.ui.components.LogClassSessionDialog
import com.example.ui.components.QuickAdjustAttendanceDialog
import com.example.ui.components.CourseLectureAttendanceCard
import com.example.ui.components.AcademicProjectsAndDeadlinesComponent
import com.example.model.toCourseAttendance
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel
import com.example.viewmodel.SubScreen
import java.util.Locale

@Composable
fun TrackScreen(
    viewModel: CampusLifeViewModel,
    onOpenSubjectDetail: (Subject) -> Unit
) {
    val activeSubTab by viewModel.trackSubTab.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val academicTasks by viewModel.academicTasks.collectAsState()
    val academicProjects by viewModel.academicProjects.collectAsState()
    val assignments by viewModel.assignments.collectAsState()
    val simulatorCount by viewModel.simulatorCount.collectAsState()
    val simulatorSubjectCode by viewModel.simulatorSubjectCode.collectAsState()
    val profile by viewModel.profile.collectAsState()

    var loggingSubject by remember { mutableStateOf<Subject?>(null) }
    var adjustingSubject by remember { mutableStateOf<Subject?>(null) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var subjectFilter by remember { mutableStateOf("All") } // "All", "Below 75%", "Safe"
    var isLectureCardView by remember { mutableStateOf(true) }

    val subTabs = listOf("Academics", "Timetable", "Assignments", "Exams", "CGPA")

    val filteredSubjects = remember(subjects, subjectFilter) {
        when (subjectFilter) {
            "Below 75%" -> subjects.filter { it.attendancePercentage < it.targetPercentage }
            "Safe" -> subjects.filter { it.attendancePercentage >= it.targetPercentage }
            else -> subjects
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("track_screen")
        ) {
        // Sub-tab Navigation Pill Carousel
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(subTabs) { tab ->
                val isSelected = tab == activeSubTab
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setTrackSubTab(tab) },
                    label = { Text(tab, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    leadingIcon = {
                        val icon = when (tab) {
                            "Academics" -> Icons.Default.School
                            "Timetable" -> Icons.Default.CalendarToday
                            "Assignments" -> Icons.Default.Assignment
                            "CGPA" -> Icons.Default.Grade
                            else -> Icons.Default.EventNote
                        }
                        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    shape = RoundedCornerShape(100)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (activeSubTab) {
                "Academics" -> {
                    // 1. Student Academic Snapshot with dynamic calculated overall attendance
                    item {
                        AcademicSnapshotCard(overallAttendance = profile.overallAttendance)
                    }

                    // 2. Attendance What-If Simulator with selectable subjects
                    item {
                        AttendanceSimulatorCard(
                            subjects = subjects,
                            selectedSubjectCode = simulatorSubjectCode,
                            onSelectSubject = { viewModel.setSimulatorSubject(it) },
                            consecutiveClasses = simulatorCount,
                            onIncrement = { viewModel.incrementSimulator() },
                            onDecrement = { viewModel.decrementSimulator() },
                            onReset = { viewModel.resetSimulator() }
                        )
                    }

                    // 3. Enrolled Subjects Header with Quick Actions
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Attendance & Subjects (${subjects.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Tap + Present or + Missed to log classes directly",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { viewModel.openSubScreen(SubScreen.DAILY_ATTENDANCE_LOGGER) },
                                        shape = RoundedCornerShape(100),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("track_open_daily_attendance_btn")
                                    ) {
                                        Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Daily Logger", style = MaterialTheme.typography.labelSmall)
                                    }

                                    Button(
                                        onClick = { showAddSubjectDialog = true },
                                        shape = RoundedCornerShape(100),
                                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("add_subject_btn")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Subject", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }

                            // Filter Pills: All, Critical (< 75%), Safe (>= 75%) & View Format Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val belowCount = subjects.count { it.attendancePercentage < it.targetPercentage }
                                    val safeCount = subjects.count { it.attendancePercentage >= it.targetPercentage }

                                    FilterChip(
                                        selected = subjectFilter == "All",
                                        onClick = { subjectFilter = "All" },
                                        label = { Text("All (${subjects.size})") }
                                    )
                                    FilterChip(
                                        selected = subjectFilter == "Below 75%",
                                        onClick = { subjectFilter = "Below 75%" },
                                        label = { Text("Below 75% ($belowCount)") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = ErrorContainer,
                                            selectedLabelColor = Error
                                        )
                                    )
                                    FilterChip(
                                        selected = subjectFilter == "Safe",
                                        onClick = { subjectFilter = "Safe" },
                                        label = { Text("Safe ($safeCount)") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = TertiaryFixed,
                                            selectedLabelColor = Tertiary
                                        )
                                    )
                                }

                                IconButton(
                                    onClick = { isLectureCardView = !isLectureCardView },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("track_toggle_lecture_view_btn")
                                ) {
                                    Icon(
                                        imageVector = if (isLectureCardView) Icons.Default.ViewAgenda else Icons.Default.ViewStream,
                                        contentDescription = "Toggle View",
                                        tint = Primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Subject Cards List
                    items(filteredSubjects, key = { it.code }) { subject ->
                        if (isLectureCardView) {
                            CourseLectureAttendanceCard(
                                course = subject.toCourseAttendance(),
                                onQuickAttend = {
                                    viewModel.quickLogClass(subject.code, isAttended = true)
                                },
                                onQuickMiss = {
                                    viewModel.quickLogClass(subject.code, isAttended = false)
                                },
                                onLogSession = {
                                    loggingSubject = subject
                                },
                                onCardClick = { onOpenSubjectDetail(subject) }
                            )
                        } else {
                            SubjectItemCard(
                                subject = subject,
                                onClick = { onOpenSubjectDetail(subject) },
                                onQuickAttend = {
                                    viewModel.quickLogClass(subject.code, isAttended = true)
                                },
                                onQuickMiss = {
                                    viewModel.quickLogClass(subject.code, isAttended = false)
                                },
                                onOpenLogDialog = {
                                    loggingSubject = subject
                                },
                                onOpenAdjustDialog = {
                                    adjustingSubject = subject
                                }
                            )
                        }
                    }
                }

                "Timetable" -> {
                    item {
                        TimetableDayView(viewModel = viewModel)
                    }
                }

                "Assignments" -> {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { viewModel.openSubScreen(SubScreen.TASKS_LIST) }
                                .testTag("track_open_full_tasks_screen_btn"),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Launch,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Column {
                                        Text(
                                            text = "Open Full Tasks Screen",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = "Dedicated view with Material 3 cards, priority filters & sorting",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item {
                        AcademicProjectsAndDeadlinesComponent(
                            tasks = academicTasks,
                            projects = academicProjects,
                            subjects = subjects,
                            onToggleTaskComplete = { viewModel.toggleAcademicTaskComplete(it) },
                            onDeleteTask = { viewModel.deleteAcademicTask(it) },
                            onToggleProjectComplete = { viewModel.toggleAcademicProjectComplete(it) },
                            onDeleteProject = { viewModel.deleteAcademicProject(it) },
                            onAddTask = { title, subjectCode, subjectName, category, type, dueDate, dueTime, priority, weightage, note ->
                                viewModel.addAcademicTask(title, subjectCode, subjectName, category, type, dueDate, dueTime, priority, weightage, note)
                            },
                            onAddProject = { title, subjectCode, subjectName, description, techStack, deadlineDate, totalMilestones, weightage, githubUrl, teamMembers ->
                                viewModel.addAcademicProject(title, subjectCode, subjectName, description, techStack, deadlineDate, totalMilestones, weightage, githubUrl, teamMembers)
                            },
                            title = "Academic Projects & Deadlines",
                            subtitle = "Room SQLite persistent capstone projects & task deadlines"
                        )
                    }
                    item {
                        AssignmentsView(viewModel = viewModel)
                    }
                }

                "Exams" -> {
                    item {
                        AcademicTasksUpcomingDueListComponent(
                            tasks = academicTasks.filter { it.category.equals("Exam", ignoreCase = true) || it.type == "EXAM_PREP" },
                            subjects = subjects,
                            onToggleTaskComplete = { viewModel.toggleAcademicTaskComplete(it) },
                            onDeleteTask = { viewModel.deleteAcademicTask(it) },
                            onAddTask = { title, subjectCode, subjectName, category, type, dueDate, dueTime, priority, weightage, note ->
                                viewModel.addAcademicTask(title, subjectCode, subjectName, category, type, dueDate, dueTime, priority, weightage, note)
                            },
                            title = "Exams & Tests Schedule",
                            subtitle = "Upcoming examinations and practical tests"
                        )
                    }
                    item {
                        ExaminationsView(viewModel = viewModel)
                    }
                }

                "CGPA" -> {
                    item {
                        val semesterGrades by viewModel.semesterGrades.collectAsState()
                        CumulativeGpaTrackerComponent(
                            semesters = semesterGrades,
                            onUpdateGrade = { sem, code, grade -> viewModel.updateCourseGrade(sem, code, grade) },
                            onAddCourse = { sem, course -> viewModel.addCourseGrade(sem, course) },
                            title = "Cumulative GPA (CGPA) Tracker",
                            subtitle = "Interactive grade adjustments, semester credit breakdown & CGPA forecasting"
                        )
                    }
                }
            }
        }
    }

    if (activeSubTab == "Assignments" || activeSubTab == "Exams") {
        com.example.ui.components.AddTaskFabAndDialog(
            viewModel = viewModel,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
        )
    }
}

    // Modal dialogs
    if (loggingSubject != null) {
        LogClassSessionDialog(
            subject = loggingSubject!!,
            onDismiss = { loggingSubject = null },
            onConfirm = { type, note ->
                viewModel.logAttendance(loggingSubject!!.code, type, note)
                loggingSubject = null
            }
        )
    }

    if (adjustingSubject != null) {
        QuickAdjustAttendanceDialog(
            subject = adjustingSubject!!,
            onDismiss = { adjustingSubject = null },
            onSave = { attended, missed, targetPct ->
                viewModel.updateSubjectCounts(adjustingSubject!!.code, attended, missed, targetPct)
                adjustingSubject = null
            }
        )
    }

    if (showAddSubjectDialog) {
        AddNewSubjectDialog(
            onDismiss = { showAddSubjectDialog = false },
            onConfirm = { code, name, credits, faculty, attended, missed, target ->
                viewModel.addNewSubject(code, name, credits, faculty, attended, missed, target)
                showAddSubjectDialog = false
            }
        )
    }
}

@Composable
fun AcademicSnapshotCard(overallAttendance: Double) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Semester 6", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("• Spring 2024", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    shape = RoundedCornerShape(100),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active", style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // CGPA
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("CGPA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("8.64", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Primary)
                        Text("Cumulative", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    }
                }
                // SGPA
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("SGPA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("8.78", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("Sem 5 Record", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    }
                }
                // Calculated Overall Attendance
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("OVERALL ATT.", style = MaterialTheme.typography.labelSmall, color = if (overallAttendance >= 75.0) Tertiary else Error, fontWeight = FontWeight.Bold)
                        Text(
                            text = String.format(Locale.US, "%.1f%%", overallAttendance),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (overallAttendance >= 75.0) Tertiary else Error
                        )
                        Text("Min 75.0% bar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = Tertiary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Attendance recalculated in real time from logged records", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun AttendanceSimulatorCard(
    subjects: List<Subject>,
    selectedSubjectCode: String,
    onSelectSubject: (String) -> Unit,
    consecutiveClasses: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onReset: () -> Unit
) {
    val targetSub = subjects.find { it.code == selectedSubjectCode } ?: subjects.firstOrNull()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = Secondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Attendance Percentage Simulator", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Text("Simulate future attends or bunks per subject", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    shape = RoundedCornerShape(100),
                    color = SecondaryFixed
                ) {
                    Text("What-If Engine", style = MaterialTheme.typography.labelSmall, color = OnSecondaryFixed, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subject selector chips
            Text("Select Subject to Simulate:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(subjects) { sub ->
                    val isSelected = sub.code == selectedSubjectCode
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectSubject(sub.code) },
                        label = { Text(sub.code, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (targetSub != null) {
                Spacer(modifier = Modifier.height(10.dp))
                // Subject banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${targetSub.name} (${targetSub.code})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("${targetSub.attendedClasses} attended / ${targetSub.totalClasses} total held", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (targetSub.attendancePercentage >= targetSub.targetPercentage) TertiaryFixed else ErrorContainer
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.1f%% Current", targetSub.attendancePercentage),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (targetSub.attendancePercentage >= targetSub.targetPercentage) Tertiary else Error,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                // Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Consecutive classes to attend:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onDecrement, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = "$consecutiveClasses",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(onClick = onIncrement, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                // Projected attended calculation
                val projectedAttended = targetSub.attendedClasses + consecutiveClasses
                val projectedTotal = targetSub.totalClasses + consecutiveClasses
                val projectedPct = (projectedAttended.toDouble() / projectedTotal) * 100.0

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = TertiaryContainer.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(TertiaryFixed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Tertiary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "If you attend next $consecutiveClasses classes:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Attendance rises to ${String.format(Locale.US, "%.1f%%", projectedPct)} (${projectedAttended}/${projectedTotal})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                // Projected Bunk
                val bunkTotal = targetSub.totalClasses + 1
                val bunkPct = (targetSub.attendedClasses.toDouble() / bunkTotal) * 100.0

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ErrorContainer.copy(alpha = 0.25f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(ErrorContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = Error, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "If you miss next class: Drops to ${String.format(Locale.US, "%.1f%%", bunkPct)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Error
                            )
                            Text(
                                text = if (bunkPct >= targetSub.targetPercentage)
                                    "Safe margin remains above statutory minimum."
                                else
                                    "Danger! Drops below 75% threshold and risks semester debarment.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Formula: Attended / Total × 100%", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = onReset) {
                    Text("Reset Simulator", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun SubjectItemCard(
    subject: Subject,
    onClick: () -> Unit,
    onQuickAttend: () -> Unit,
    onQuickMiss: () -> Unit,
    onOpenLogDialog: () -> Unit,
    onOpenAdjustDialog: () -> Unit
) {
    val pct = subject.attendancePercentage
    val isCritical = pct < subject.targetPercentage

    val statusColor = when {
        pct >= 85.0 -> Tertiary
        pct >= 75.0 -> Secondary
        else -> Error
    }

    val statusBg = when {
        pct >= 85.0 -> TertiaryFixed
        pct >= 75.0 -> SecondaryFixed
        else -> ErrorContainer
    }

    val statusText = when {
        pct >= 85.0 -> "Safe"
        pct >= 75.0 -> "Borderline"
        else -> "Low Attention"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subject_card_${subject.code}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${subject.code} • ${subject.credits} CREDITS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subject.faculty,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(100),
                    color = statusBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(statusText, style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format(Locale.US, "%.1f%%", pct),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${subject.attendedClasses} / ${subject.totalClasses} attended)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isCritical) ErrorContainer.copy(alpha = 0.5f) else TertiaryFixed.copy(alpha = 0.4f)
                ) {
                    Text(
                        text = if (isCritical) "Needs ${subject.neededToReach75} attend to reach 75%" else "Can bunk ${subject.safeBunksLeft} classes",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCritical) Error else Tertiary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (pct / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Direct 1-Tap Attendance Logging Buttons & Detail link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick + Attended Button
                FilledTonalButton(
                    onClick = onQuickAttend,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = TertiaryFixed,
                        contentColor = Tertiary
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("attend_btn_${subject.code}")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Present", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Quick + Missed Button
                FilledTonalButton(
                    onClick = onQuickMiss,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = ErrorContainer,
                        contentColor = Error
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("miss_btn_${subject.code}")
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Missed", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Log Session Modal Button
                IconButton(
                    onClick = onOpenLogDialog,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .testTag("log_modal_btn_${subject.code}")
                ) {
                    Icon(Icons.Default.EditCalendar, contentDescription = "Log with Note", tint = Primary, modifier = Modifier.size(18.dp))
                }

                // Calibrate / Adjust Counts Button
                IconButton(
                    onClick = onOpenAdjustDialog,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .testTag("adjust_btn_${subject.code}")
                ) {
                    Icon(Icons.Default.Tune, contentDescription = "Adjust counts", tint = Secondary, modifier = Modifier.size(18.dp))
                }

                // Details Button
                IconButton(
                    onClick = onClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .testTag("detail_btn_${subject.code}")
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Subject Details", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}


@Composable
fun TimetableDayView(viewModel: CampusLifeViewModel) {
    val days = listOf("21" to "Mon", "22" to "Tue", "23" to "Wed", "24" to "Thu", "25" to "Fri", "26" to "Sat")
    var selectedDay by remember { mutableStateOf("24") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Week Days Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { (date, day) ->
                val isSelected = date == selectedDay
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                        .clickable { selectedDay = date },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) PrimaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(day, style = MaterialTheme.typography.labelSmall, color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(date, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        // Live status banner
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(TertiaryFixed))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("IN SESSION RIGHT NOW", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Text("32 mins left", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("DBMS: Normalization & Indexing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Text("10:30 – 11:45 AM • CS Lab 304 (3rd Floor)", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
            }
        }

        // Timeline slots
        MockData.thursdaySlots.forEach { slot ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Time
                Column(
                    modifier = Modifier.width(55.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(slot.timeStart, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(slot.timeEnd, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (slot.isLiveNow) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (slot.isLiveNow) 2.dp else 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(slot.code, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Primary)
                            if (slot.isAttended) {
                                Surface(shape = RoundedCornerShape(100), color = TertiaryFixed) {
                                    Text("Attended", style = MaterialTheme.typography.labelSmall, color = Tertiary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            } else if (slot.isLiveNow) {
                                Surface(shape = RoundedCornerShape(100), color = Primary) {
                                    Text("Live Now", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(slot.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(slot.faculty, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(slot.room, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                        }

                        if (slot.isLiveNow) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.markTodayAttended() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Mark Attendance", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = {},
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Take Notes", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssignmentsView(viewModel: CampusLifeViewModel) {
    val assignments by viewModel.assignments.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Urgent Hero Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ErrorContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Alarm, contentDescription = null, tint = Error, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("URGENT SUBMISSION", style = MaterialTheme.typography.labelSmall, color = Error, fontWeight = FontWeight.Bold)
                    }
                    Surface(shape = RoundedCornerShape(100), color = MaterialTheme.colorScheme.surfaceContainerLowest) {
                        Text("1h 45m left", style = MaterialTheme.typography.labelSmall, color = Error, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text("Computer Networks Lab Assignment 4", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnErrorContainer)
                Text("Due Today • 5:00 PM • In Progress", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.8f)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("packet_trace_wireshark.pcap", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            Text("4.2 MB • Saved draft", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Tertiary, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.toggleAssignmentComplete("asgn-1") },
                    colors = ButtonDefaults.buttonColors(containerColor = Error),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Final Submission")
                }
            }
        }

        Text("Assignment Queue (${assignments.size} active)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        assignments.forEach { asgn ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = if (asgn.priority == "High Priority" || asgn.priority == "Urgent") ErrorContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                asgn.priority,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (asgn.priority == "High Priority" || asgn.priority == "Urgent") Error else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(asgn.internalWeightage, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(asgn.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("${asgn.subject} • ${asgn.faculty}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Due: ${asgn.dueDate} • ${asgn.dueTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        IconButton(onClick = { viewModel.toggleAssignmentComplete(asgn.id) }) {
                            Icon(
                                imageVector = if (asgn.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (asgn.isCompleted) Tertiary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExaminationsView(viewModel: CampusLifeViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("EXAMINATION SCHEDULE", style = MaterialTheme.typography.labelSmall, color = Secondary, fontWeight = FontWeight.Bold)
                Text("Mid-Sem Exams start in 11 days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Semester IV Theory Modules • Nov 4 - Nov 8", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MockData.examSchedule.forEach { exam ->
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(exam.date, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text(exam.subjectName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Primary)
                                Text(exam.time, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hall: Academic Block B (LH-302)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { viewModel.openSubScreen(SubScreen.REVISION_PLAN) }) {
                        Text("Open Revision Plan →", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
