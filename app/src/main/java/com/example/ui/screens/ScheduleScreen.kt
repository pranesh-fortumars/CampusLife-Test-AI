package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CourseEntity
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel

@Composable
fun ScheduleScreen(viewModel: CampusLifeViewModel) {
    val courses by viewModel.courses.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Academics & Schedule",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Timetables, exam schedules & holiday reminders",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("schedule_tabs")
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Timetable & Bunks", fontWeight = FontWeight.SemiBold) },
                            icon = { Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.testTag("tab_timetable")
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Calendar & Exams", fontWeight = FontWeight.SemiBold) },
                            icon = { Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.testTag("tab_academic_calendar")
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (selectedTab == 1) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
                    .testTag("academic_calendar_tab_view"),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {

                    com.example.ui.components.ExamCountdownComponent(
                        viewModel = viewModel
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
                item {
                    com.example.ui.components.SemesterProgressComponent(
                        viewModel = viewModel
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
                item {
                    com.example.ui.components.AcademicCalendarComponent(
                        viewModel = viewModel
                    )
                }
            }
        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
                    .testTag("schedule_screen"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

            // Overview banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = IndigoPrimaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "🎓 Attendance & Safe Bunk Policy",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = IndigoOnPrimaryContainer
                        )
                        Text(
                            "Your safe bunks indicate how many classes you can skip right now while remaining safely at or above 75.0%. Log every lecture to maintain precise records.",
                            fontSize = 12.sp,
                            color = IndigoOnPrimaryContainer.copy(alpha = 0.85f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            items(courses, key = { it.id }) { course ->
                ScheduleCourseCard(
                    course = course,
                    onLogPresent = { viewModel.logAttendance(course, isPresent = true) },
                    onLogMissed = { viewModel.logAttendance(course, isPresent = false) }
                )
            }
        }
    }
}

@Composable
fun ScheduleCourseCard(
    course: CourseEntity,
    onLogPresent: () -> Unit,
    onLogMissed: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("schedule_card_${course.code}")
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
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        course.code,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                val isSafe = course.attendancePercentage >= 75.0
                Surface(
                    shape = RoundedCornerShape(100),
                    color = if (isSafe) EmeraldTertiaryContainer else ErrorContainer
                ) {
                    Text(
                        text = "${String.format("%.1f", course.attendancePercentage)}% (${course.attendedClasses}/${course.totalClasses})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isSafe) EmeraldOnTertiaryContainer else OnErrorContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                course.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Room and instructor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.MeetingRoom, contentDescription = null, modifier = Modifier.size(16.dp), tint = IndigoPrimary)
                    Text(course.room, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(course.instructor, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Timetable slot
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "${course.dayOfWeek} • ${course.timeSlot}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 0.8.dp)

            // Safe bunk calculation & Quick actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Safe Bunks Available", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        if (course.safeBunks > 0) "${course.safeBunks} lectures safe to miss" else "0 (Attend next class!)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (course.safeBunks > 0) EmeraldTertiary else ErrorRed
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = onLogMissed,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Missed (-1)", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onLogPresent,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Present", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
