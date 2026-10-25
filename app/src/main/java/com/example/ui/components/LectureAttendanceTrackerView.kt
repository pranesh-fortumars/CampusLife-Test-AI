package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

/**
 * Complete, standalone and embeddable UI component to track lecture attendance percentage
 * for different academic courses with filtering, sorting, real-time recalculations,
 * and session logging.
 */
@Composable
fun LectureAttendanceTrackerView(
    courses: List<CourseAttendance>,
    onQuickAttend: (String) -> Unit,
    onQuickMiss: (String) -> Unit,
    onLogSession: (String, LectureAttendanceStatus, LectureType, String) -> Unit,
    onCourseDetailClick: (String) -> Unit,
    onOpenDailyLogger: () -> Unit,
    modifier: Modifier = Modifier,
    showHeader: Boolean = true
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Shortage", "Borderline", "Safe"
    var sortBy by remember { mutableStateOf("Lowest") } // "Lowest", "Highest", "Name"
    var loggingCourse by remember { mutableStateOf<CourseAttendance?>(null) }

    val summary = remember(courses) { CourseAttendanceSummary(courses) }

    val filteredCourses = remember(courses, searchQuery, selectedFilter, sortBy) {
        val searchFiltered = if (searchQuery.isBlank()) {
            courses
        } else {
            courses.filter {
                it.courseCode.contains(searchQuery, ignoreCase = true) ||
                it.courseName.contains(searchQuery, ignoreCase = true) ||
                it.faculty.contains(searchQuery, ignoreCase = true)
            }
        }

        val categoryFiltered = when (selectedFilter) {
            "Shortage" -> searchFiltered.filter { it.status == AttendanceStatus.CRITICAL }
            "Borderline" -> searchFiltered.filter { it.status == AttendanceStatus.WARNING }
            "Safe" -> searchFiltered.filter { it.status == AttendanceStatus.SAFE }
            else -> searchFiltered
        }

        when (sortBy) {
            "Lowest" -> categoryFiltered.sortedBy { it.attendancePercentage }
            "Highest" -> categoryFiltered.sortedByDescending { it.attendancePercentage }
            "Name" -> categoryFiltered.sortedBy { it.courseCode }
            else -> categoryFiltered
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("lecture_attendance_tracker_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Semester Overview Summary Header
        if (showHeader && courses.isNotEmpty()) {
            LectureAttendanceOverviewHeader(
                summary = summary,
                onOpenDailyLogger = onOpenDailyLogger
            )
        }

        // 2. Filter & Sort Bar
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Courses Attendance (${filteredCourses.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Sort Chip Toggle
                Surface(
                    shape = RoundedCornerShape(100),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.clickable {
                        sortBy = when (sortBy) {
                            "Lowest" -> "Highest"
                            "Highest" -> "Name"
                            else -> "Lowest"
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("sort_attendance_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when (sortBy) {
                                "Lowest" -> Icons.Default.ArrowUpward
                                "Highest" -> Icons.Default.ArrowDownward
                                else -> Icons.Default.SortByAlpha
                            },
                            contentDescription = "Sort",
                            modifier = Modifier.size(14.dp),
                            tint = Primary
                        )
                        Text(
                            text = "Sort: $sortBy",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
            }

            // Filter Chips Carousel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val filters = listOf(
                    "All" to "All (${courses.size})",
                    "Shortage" to "Shortage (${summary.criticalCount})",
                    "Borderline" to "Borderline (${summary.warningCount})",
                    "Safe" to "Safe (${summary.safeCount})"
                )

                items(filters) { (key, label) ->
                    val isSelected = selectedFilter == key
                    val chipColor = when (key) {
                        "Shortage" -> Error
                        "Borderline" -> Warning
                        "Safe" -> Tertiary
                        else -> Primary
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = key },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipColor,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(100),
                        modifier = Modifier.testTag("attendance_filter_chip_$key")
                    )
                }
            }
        }

        // 3. Course Cards List
        if (filteredCourses.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedFilter == "Shortage") {
                            "🎉 No courses with attendance shortage! All meet institutional minimums."
                        } else {
                            "No courses match the selected criteria."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredCourses.forEach { course ->
                    CourseLectureAttendanceCard(
                        course = course,
                        onQuickAttend = { onQuickAttend(course.courseCode) },
                        onQuickMiss = { onQuickMiss(course.courseCode) },
                        onLogSession = { loggingCourse = course },
                        onCardClick = { onCourseDetailClick(course.courseCode) }
                    )
                }
            }
        }
    }

    // Modal Session Logging Dialog
    loggingCourse?.let { targetCourse ->
        MarkLectureSessionDialog(
            course = targetCourse,
            onDismiss = { loggingCourse = null },
            onConfirm = { status, type, topic ->
                onLogSession(targetCourse.courseCode, status, type, topic)
                loggingCourse = null
            }
        )
    }
}
