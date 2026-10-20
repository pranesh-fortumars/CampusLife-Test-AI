package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CourseAttendanceTrackerComponent
import com.example.ui.components.MainAcademicDashboard
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel
import com.example.viewmodel.SubScreen
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: CampusLifeViewModel,
    onNavigateToTrack: () -> Unit,
    onOpenCopilot: () -> Unit,
    onOpenExecutiveDesk: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val academicTasks by viewModel.academicTasks.collectAsState()
    val homeTasks by viewModel.homeTaskState.collectAsState()
    val todayAttended by viewModel.todayAttendanceLogged.collectAsState()

    val nextSubject = subjects.find { it.code == "CS-302" } ?: subjects.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Main Academic Dashboard (Greeting, Profile Snapshot & Deadlines Hub)
        item {
            MainAcademicDashboard(
                profile = profile,
                subjects = subjects,
                tasks = academicTasks,
                onToggleTaskComplete = { viewModel.toggleAcademicTaskComplete(it) },
                onAddTask = { title, subjectCode, subjectName, category, type, dueDate, dueTime, priority, weightage, note ->
                    viewModel.addAcademicTask(title, subjectCode, subjectName, category, type, dueDate, dueTime, priority, weightage, note)
                },
                onDeleteTask = { viewModel.deleteAcademicTask(it) },
                onNavigateToTrack = onNavigateToTrack
            )
        }

        // 2. Today's Next Class Card (Spotlight Schedule & 1-Tap Attendance Logging)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("next_class_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = PrimaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Primary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Starts in 22 mins",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OnPrimaryContainer
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = "Lecture 2 of 4",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Database Management Systems (DBMS)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "10:30 AM – 11:45 AM",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text("•", color = MaterialTheme.colorScheme.outlineVariant)
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "CS Lab 304, Block B",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Dr. Ananya Roy • Dept. of CSE",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (nextSubject != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val isSafe = nextSubject.attendancePercentage >= nextSubject.targetPercentage
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSafe) TertiaryContainer else ErrorContainer,
                            border = BorderStroke(1.dp, if (isSafe) Tertiary.copy(alpha = 0.25f) else Error.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Attendance: ${String.format(Locale.US, "%.1f%%", nextSubject.attendancePercentage)} (${nextSubject.attendedClasses}/${nextSubject.totalClasses})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSafe) OnTertiaryContainer else OnErrorContainer
                                )
                                Text(
                                    if (isSafe) "${nextSubject.safeBunksLeft} safe bunks" else "Needs ${nextSubject.neededToReach75} to 75%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSafe) OnTertiaryContainer else OnErrorContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.markTodayAttended() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Tertiary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("mark_attended_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Present", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.markTodayMissed() },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Error
                            ),
                            border = BorderStroke(1.dp, Error.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("mark_missed_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Missed", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = {
                                viewModel.openSubScreen(SubScreen.DAILY_ATTENDANCE_LOGGER)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("open_daily_attendance_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FactCheck,
                                contentDescription = "Daily Attendance Logger",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Log Daily", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = {
                                viewModel.setTrackSubTab("Timetable")
                                onNavigateToTrack()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            modifier = Modifier.testTag("open_timetable_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Timetable",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 3. Course Attendance Tracker (Percentages & Progress Bars for each course)
        item {
            CourseAttendanceTrackerComponent(
                subjects = subjects,
                onQuickAttend = { courseCode ->
                    viewModel.quickLogClass(courseCode, isAttended = true)
                },
                onQuickMiss = { courseCode ->
                    viewModel.quickLogClass(courseCode, isAttended = false)
                },
                onCourseClick = { courseCode ->
                    val sub = subjects.find { it.code == courseCode }
                    if (sub != null) {
                        viewModel.selectSubjectDetail(sub)
                    } else {
                        viewModel.setTrackSubTab("Academics")
                        onNavigateToTrack()
                    }
                }
            )
        }

        // 4. Smart Student Tools (2x2 Grid)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Smart Campus Tools",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tool 1: Focus Timer
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.openSubScreen(SubScreen.FOCUS_TIMER) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Focus Timer", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Pomodoro & Study", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Tool 2: Attendance Simulator
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                viewModel.setTrackSubTab("Academics")
                                onNavigateToTrack()
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TertiaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Calculate, contentDescription = null, tint = Tertiary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Attendance Sim", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Forecast & Bunks", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tool 3: Revision Planner
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.openSubScreen(SubScreen.REVISION_PLAN) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SecondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoStories, contentDescription = null, tint = Secondary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Revision Plan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Exam Mastery", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Tool 4: Executive Desk / Copilot
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenExecutiveDesk() },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("CR Desk", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Class Notices & Rep", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // 4. Daily Student Routine Checklist
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val completedCount = homeTasks.values.count { it }
                    val totalTasks = 4
                    val progress = completedCount.toFloat() / totalTasks.toFloat()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Today's Routine Checklist",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$completedCount of $totalTasks completed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val tasksList = listOf(
                        "Attend DBMS Lab & Execute SQL Joins (10:30 AM)",
                        "Submit OS Process Scheduling Code by 5:00 PM",
                        "Review Computer Networks Unit 3 Notes",
                        "GDSC Hackathon Team Sync at Canteen Huddle (4:30 PM)"
                    )

                    tasksList.forEachIndexed { index, taskText ->
                        val isChecked = homeTasks[index] ?: false
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleHomeTask(index) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { viewModel.toggleHomeTask(index) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Primary,
                                    uncheckedColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = taskText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                textDecoration = if (isChecked) androidx.compose.ui.text.style.TextDecoration.LineThrough else androidx.compose.ui.text.style.TextDecoration.None
                            )
                        }
                    }
                }
            }
        }

        // 5. Campus Quick Links & Portals
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Campus Quick Links",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val links = listOf(
                            Triple("ERP Portal", Icons.Default.Language, "Active"),
                            Triple("LMS Portal", Icons.Default.LaptopMac, "Active"),
                            Triple("Results", Icons.Default.Grade, "Sem 5"),
                            Triple("Library", Icons.Default.MenuBook, "0 Due"),
                            Triple("Wi-Fi", Icons.Default.Wifi, "Connected")
                        )

                        links.forEach { (name, icon, status) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { }
                                    .padding(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, contentDescription = name, tint = Primary, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(name, style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Text(status, style = MaterialTheme.typography.bodySmall, fontSize = 9.sp, color = Tertiary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
