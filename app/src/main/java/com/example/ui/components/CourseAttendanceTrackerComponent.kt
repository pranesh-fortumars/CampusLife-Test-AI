package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AttendanceStatus
import com.example.model.CourseAttendance
import com.example.model.CourseAttendanceSummary
import com.example.model.Subject
import com.example.model.toCourseAttendanceList
import com.example.ui.theme.*
import java.util.Locale

/**
 * Overload of CourseAttendanceTrackerComponent that accepts a list of Subjects directly.
 */
@Composable
fun CourseAttendanceTrackerComponent(
    subjects: List<Subject>,
    onQuickAttend: (String) -> Unit,
    onQuickMiss: (String) -> Unit,
    onCourseClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Course Attendance Progress",
    subtitle: String = "Live percentage tracking & progress towards 75% requirement",
    showSummaryHeader: Boolean = true
) {
    val courses = remember(subjects) { subjects.toCourseAttendanceList() }
    CourseAttendanceTracker(
        courses = courses,
        onQuickAttend = onQuickAttend,
        onQuickMiss = onQuickMiss,
        onCourseClick = onCourseClick,
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        showSummaryHeader = showSummaryHeader
    )
}

/**
 * Dedicated Material 3 UI component to track class attendance percentages for different subjects,
 * showing animated progress bars for each course, target threshold indicators, and quick action logging.
 */
@Composable
fun CourseAttendanceTracker(
    courses: List<CourseAttendance>,
    onQuickAttend: (String) -> Unit,
    onQuickMiss: (String) -> Unit,
    onCourseClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Course Attendance Progress",
    subtitle: String = "Live percentage tracking & progress towards 75% requirement",
    showSummaryHeader: Boolean = true
) {
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Attention", "Safe"
    var sortBy by remember { mutableStateOf("Default") } // "Default", "Lowest", "Highest"

    val summary = remember(courses) { CourseAttendanceSummary(courses) }

    val filteredCourses = remember(courses, selectedFilter, sortBy) {
        val filtered = when (selectedFilter) {
            "Attention" -> courses.filter { !it.isSafe }
            "Safe" -> courses.filter { it.isSafe }
            else -> courses
        }
        when (sortBy) {
            "Lowest" -> filtered.sortedBy { it.attendancePercentage }
            "Highest" -> filtered.sortedByDescending { it.attendancePercentage }
            else -> filtered
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("course_attendance_tracker")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Title and Quick Filter / Sort
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Sort toggle button
                IconButton(
                    onClick = {
                        sortBy = when (sortBy) {
                            "Default" -> "Lowest"
                            "Lowest" -> "Highest"
                            else -> "Default"
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("sort_attendance_btn")
                ) {
                    Icon(
                        imageVector = when (sortBy) {
                            "Lowest" -> Icons.Default.ArrowUpward
                            "Highest" -> Icons.Default.ArrowDownward
                            else -> Icons.Default.Sort
                        },
                        contentDescription = "Sort order: $sortBy",
                        tint = if (sortBy != "Default") Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Optional Summary Header showing overall progress across all subjects
            if (showSummaryHeader && courses.isNotEmpty()) {
                OverallAttendanceSummaryCard(summary = summary)
            }

            // Filter Chips Carousel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val filterOptions = listOf(
                    "All" to "${courses.size} Courses",
                    "Safe" to "${summary.safeCount} Safe (≥75%)",
                    "Attention" to "${courses.size - summary.safeCount} At Risk (<75%)"
                )
                items(filterOptions) { (key, label) ->
                    val isSelected = selectedFilter == key
                    val chipColor = when (key) {
                        "Safe" -> Tertiary
                        "Attention" -> Error
                        else -> Primary
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipColor,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(100),
                        modifier = Modifier.testTag("attendance_filter_$key")
                    )
                }
            }

            // Course Attendance Progress List
            if (filteredCourses.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedFilter == "Attention") "🎉 All courses meet the 75% attendance criteria!" else "No courses match the selected filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filteredCourses.forEach { course ->
                        CourseAttendanceProgressBarItem(
                            course = course,
                            onQuickAttend = { onQuickAttend(course.courseCode) },
                            onQuickMiss = { onQuickMiss(course.courseCode) },
                            onClick = { onCourseClick(course.courseCode) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Summary card displaying the aggregated overall attendance percentage with a progress bar and status badges.
 */
@Composable
private fun OverallAttendanceSummaryCard(summary: CourseAttendanceSummary) {
    val animatedProgress by animateFloatAsState(
        targetValue = summary.overallProgressFraction,
        label = "overall_progress"
    )
    val isOverallSafe = summary.overallPercentage >= 75.0

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "OVERALL ATTENDANCE",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${summary.totalAttended} of ${summary.totalClasses} Total Classes Attended",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(100),
                    color = if (isOverallSafe) TertiaryContainer else ErrorContainer,
                    border = BorderStroke(1.dp, if (isOverallSafe) Tertiary.copy(alpha = 0.3f) else Error.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isOverallSafe) Tertiary else Error)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = summary.formattedOverallPercentage,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isOverallSafe) OnTertiaryContainer else OnErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Overall Progress Bar with 75% target line
            Box(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (isOverallSafe) Tertiary else Error,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Goal: 75.0% required",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (isOverallSafe) "Status: In Good Standing" else "Status: Attendance Warning",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverallSafe) Tertiary else Error
                )
            }
        }
    }
}

/**
 * Individual course card displaying course title, percentage, status badge, progress bar,
 * metrics details, and direct attendance logging actions.
 */
@Composable
fun CourseAttendanceProgressBarItem(
    course: CourseAttendance,
    onQuickAttend: () -> Unit,
    onQuickMiss: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = course.progressFraction,
        label = "course_progress_${course.courseCode}"
    )

    val (statusColor, containerColor, onContainerColor) = when (course.status) {
        AttendanceStatus.SAFE -> Triple(Tertiary, TertiaryContainer, OnTertiaryContainer)
        AttendanceStatus.WARNING -> Triple(Warning, WarningContainer, OnWarningContainer)
        AttendanceStatus.CRITICAL -> Triple(Error, ErrorContainer, OnErrorContainer)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, if (!course.isSafe) statusColor.copy(alpha = 0.35f) else OutlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("course_card_${course.courseCode}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Course Info & Attendance Percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PrimaryContainer
                        ) {
                            Text(
                                text = course.courseCode,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${course.credits} Credits",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = course.courseName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Percentage Badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = course.formattedPercentage,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Surface(
                        shape = RoundedCornerShape(100),
                        color = containerColor
                    ) {
                        Text(
                            text = if (course.isSafe) "${course.safeBunksLeft} safe bunks" else "Needs +${course.neededToReachTarget} classes",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = onContainerColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Attendance Progress Bar with 75% target threshold guide
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val fullWidth = maxWidth

                Column {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .testTag("course_progress_bar_${course.courseCode}"),
                            color = statusColor,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )

                        // 75% Target Line Marker
                        val targetOffset = fullWidth * (course.targetPercentage.toFloat() / 100f)
                        Box(
                            modifier = Modifier
                                .offset(x = targetOffset - 1.dp)
                                .width(2.dp)
                                .height(8.dp)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${course.attendedClasses} attended • ${course.missedClasses} missed",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "75% Target line",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: Quick Action Logging Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("attend_btn_${course.courseCode}")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Attended", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Quick + Missed Button
                FilledTonalButton(
                    onClick = onQuickMiss,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = ErrorContainer,
                        contentColor = Error
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("miss_btn_${course.courseCode}")
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Missed", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // View Details Icon Button
                OutlinedIconButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, OutlineVariant),
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("details_btn_${course.courseCode}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Details for ${course.courseCode}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
