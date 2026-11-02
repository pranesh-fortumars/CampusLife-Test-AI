package com.example.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CourseEntity
import com.example.ui.theme.*
import com.example.viewmodel.AppDestination
import com.example.viewmodel.CampusLifeViewModel
import com.example.viewmodel.SearchCategory

@Composable
fun HomeScreen(
    viewModel: CampusLifeViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToCommunity: () -> Unit
) {
    val courses by viewModel.courses.collectAsState()
    val tasks by viewModel.tasks.collectAsState()

    val pendingTasks = tasks.filter { !it.isCompleted }
    val lowAttendanceCourses = courses.filter { it.attendancePercentage < 75.0 }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Hero Header & Quick Search Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Welcome back, Devon!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Semester 4 • Computer Science & Eng.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = "Profile",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // Prominent Global Search Bar
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onNavigateToSearch() }
                            .testTag("home_search_bar_trigger")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = IndigoPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                "Find courses, notes, questions...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SlateOnSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = IndigoPrimaryContainer,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    "Quick Search",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IndigoOnPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Campus Buzz Announcements Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = WarningContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(22.dp))
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Campus Buzz • Midterm Schedule Released", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            "Engineering block exams begin Nov 18. Library hours extended to 2:00 AM.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Low Attendance Warning Alert if any
        if (lowAttendanceCourses.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ErrorContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(24.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Attendance Warning (< 75%)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = OnErrorContainer
                            )
                            val names = lowAttendanceCourses.joinToString { "${it.code} (${String.format("%.1f", it.attendancePercentage)}%)" }
                            Text(
                                "Attend upcoming classes in $names to avoid debarment.",
                                fontSize = 11.sp,
                                color = OnErrorContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }

        // Quick Category Nav Row
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("Search by Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryTile(
                        title = "Courses",
                        icon = Icons.Default.MenuBook,
                        color = IndigoPrimary,
                        containerColor = IndigoPrimaryContainer,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.selectCategory(SearchCategory.COURSES)
                            onNavigateToSearch()
                        }
                    )
                    CategoryTile(
                        title = "Notes",
                        icon = Icons.Default.FolderOpen,
                        color = EmeraldTertiary,
                        containerColor = EmeraldTertiaryContainer,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.selectCategory(SearchCategory.MATERIALS)
                            onNavigateToSearch()
                        }
                    )
                    CategoryTile(
                        title = "Peer Feed",
                        icon = Icons.Default.Forum,
                        color = VioletSecondary,
                        containerColor = VioletSecondaryContainer,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.selectCategory(SearchCategory.POSTS)
                            onNavigateToSearch()
                        }
                    )
                    CategoryTile(
                        title = "Exams & Cal",
                        icon = Icons.Default.Event,
                        color = Color(0xFFBE123C),
                        containerColor = Color(0xFFFFF1F2),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToSchedule
                    )
                }
            }
        }

        // Upcoming Academic Milestones & Exam Schedules Preview
        item {
            val calendarEvents by viewModel.calendarEvents.collectAsState()
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Exams & Semester Dates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onNavigateToSchedule) {
                        Text("View All", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                calendarEvents.take(2).forEach { event ->
                    com.example.ui.components.AcademicCalendarEventCard(
                        event = event,
                        onToggleReminder = { viewModel.toggleEventReminder(event) },
                        onDelete = { viewModel.deleteAcademicEvent(event) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }


        // Today's Timetable Section
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Today's Classes & Rooms", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onNavigateToSchedule) {
                        Text("View Timetable", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                courses.take(3).forEach { course ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(course.code, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                    Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(course.room, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(course.name, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                Text(course.timeSlot, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (course.attendancePercentage >= 75.0) EmeraldTertiaryContainer else ErrorContainer
                            ) {
                                Text(
                                    text = "${String.format("%.0f", course.attendancePercentage)}%",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (course.attendancePercentage >= 75.0) EmeraldOnTertiaryContainer else OnErrorContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Pending Deadlines Section
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Text("Upcoming Deadlines & Tasks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                pendingTasks.take(3).forEach { task ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = { viewModel.toggleTask(task) }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(task.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("${task.courseCode} • Due ${task.dueDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryTile(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(88.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = containerColor,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
