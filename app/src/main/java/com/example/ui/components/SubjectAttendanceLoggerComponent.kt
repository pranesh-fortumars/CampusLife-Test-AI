package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import com.example.model.Subject
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel
import java.util.Locale

/**
 * Convenience overload accepting CampusLifeViewModel.
 */
@Composable
fun SubjectAttendanceLoggerComponent(
    viewModel: CampusLifeViewModel,
    modifier: Modifier = Modifier,
    onCourseClick: ((Subject) -> Unit)? = null
) {
    val subjects by viewModel.subjects.collectAsState()
    SubjectAttendanceLoggerComponent(
        subjects = subjects,
        onLogAttended = { code -> viewModel.logClassAttended(code) },
        onLogMissed = { code -> viewModel.logClassMissed(code) },
        modifier = modifier,
        onCourseClick = onCourseClick
    )
}

/**
 * UI component using Jetpack Compose that allows students to log and view
 * their attendance percentage for different subjects with real-time percentage
 * recalculation, threshold warnings (75% statutory requirement), and safe bunks forecasting.
 */
@Composable
fun SubjectAttendanceLoggerComponent(
    subjects: List<Subject>,
    onLogAttended: (subjectCode: String) -> Unit,
    onLogMissed: (subjectCode: String) -> Unit,
    modifier: Modifier = Modifier,
    onCourseClick: ((Subject) -> Unit)? = null
) {
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "ATTENTION", "SAFE"
    var searchQuery by remember { mutableStateOf("") }

    val totalAttended = subjects.sumOf { it.attendedClasses }
    val totalClasses = subjects.sumOf { it.totalClasses }
    val overallPercentage = if (totalClasses == 0) 100.0 else (totalAttended.toDouble() / totalClasses) * 100.0
    val safeCount = subjects.count { it.attendancePercentage >= it.targetPercentage }
    val atRiskCount = subjects.count { it.attendancePercentage < it.targetPercentage }

    val filteredSubjects = remember(subjects, selectedFilter, searchQuery) {
        subjects.filter { subject ->
            val matchesFilter = when (selectedFilter) {
                "ATTENTION" -> subject.attendancePercentage < subject.targetPercentage
                "SAFE" -> subject.attendancePercentage >= subject.targetPercentage
                else -> true
            }
            val matchesQuery = searchQuery.isBlank() ||
                    subject.code.contains(searchQuery, ignoreCase = true) ||
                    subject.name.contains(searchQuery, ignoreCase = true) ||
                    subject.faculty.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("subject_attendance_logger_component"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Overall Attendance Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("overall_attendance_banner"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.8f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Semester Attendance Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Minimum 75% required for university end-term eligibility",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Large Overall Percentage Badge
                    val isOverallSafe = overallPercentage >= 75.0
                    Surface(
                        color = if (isOverallSafe) TertiaryContainer else ErrorContainer,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isOverallSafe) Tertiary.copy(alpha = 0.3f) else Error.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.1f%%", overallPercentage),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isOverallSafe) OnTertiaryContainer else OnErrorContainer
                            )
                            Text(
                                text = if (isOverallSafe) "Overall Safe" else "Critical",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOverallSafe) OnTertiaryContainer else OnErrorContainer
                            )
                        }
                    }
                }

                // Stats breakdown row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AttendanceStatPill(
                        label = "Total Attended",
                        value = "$totalAttended / $totalClasses",
                        icon = Icons.Default.CheckCircle,
                        color = Primary,
                        modifier = Modifier.weight(1f)
                    )
                    AttendanceStatPill(
                        label = "Safe Courses",
                        value = "$safeCount",
                        icon = Icons.Default.Verified,
                        color = Tertiary,
                        modifier = Modifier.weight(1f)
                    )
                    AttendanceStatPill(
                        label = "Needs Attention",
                        value = "$atRiskCount",
                        icon = Icons.Default.Warning,
                        color = if (atRiskCount > 0) Warning else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Search & Filter Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search course or faculty...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("attendance_search_field"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Filter chips row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" },
                label = { Text("All Courses (${subjects.size})") },
                modifier = Modifier.testTag("chip_filter_all")
            )
            FilterChip(
                selected = selectedFilter == "ATTENTION",
                onClick = { selectedFilter = "ATTENTION" },
                label = { Text("Below 75% ($atRiskCount)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ErrorContainer,
                    selectedLabelColor = OnErrorContainer
                ),
                modifier = Modifier.testTag("chip_filter_attention")
            )
            FilterChip(
                selected = selectedFilter == "SAFE",
                onClick = { selectedFilter = "SAFE" },
                label = { Text("Safe Attendance ($safeCount)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TertiaryContainer,
                    selectedLabelColor = OnTertiaryContainer
                ),
                modifier = Modifier.testTag("chip_filter_safe")
            )
        }

        // Subject List
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            filteredSubjects.forEach { subject ->
                SubjectAttendanceCard(
                    subject = subject,
                    onLogAttended = { onLogAttended(subject.code) },
                    onLogMissed = { onLogMissed(subject.code) },
                    onClick = { onCourseClick?.invoke(subject) }
                )
            }
        }
    }
}

/**
 * Individual Course Card with real-time percentage progress bar,
 * safe bunks indicator, and "+ Present" / "+ Missed" logging actions.
 */
@Composable
fun SubjectAttendanceCard(
    subject: Subject,
    onLogAttended: () -> Unit,
    onLogMissed: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pct = subject.attendancePercentage
    val isSafe = pct >= subject.targetPercentage
    val isCritical = pct < 70.0

    val animatedPct by animateFloatAsState(
        targetValue = (pct / 100.0).toFloat().coerceIn(0f, 1f),
        label = "attendance_pct_anim"
    )

    val statusColor = when {
        isSafe -> Tertiary
        isCritical -> Error
        else -> Warning
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("attendance_card_${subject.code}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, if (!isSafe) statusColor.copy(alpha = 0.4f) else OutlineVariant.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Course Code, Name, and Attendance Percentage
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
                            color = Primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = subject.code,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "${subject.credits} Credits",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Percentage Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when {
                        isSafe -> TertiaryContainer
                        isCritical -> ErrorContainer
                        else -> WarningContainer
                    }
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1f%%", pct),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                isSafe -> OnTertiaryContainer
                                isCritical -> OnErrorContainer
                                else -> OnWarningContainer
                            }
                        )
                        Text(
                            text = "Goal: 75%",
                            fontSize = 10.sp,
                            color = when {
                                isSafe -> OnTertiaryContainer.copy(alpha = 0.8f)
                                isCritical -> OnErrorContainer.copy(alpha = 0.8f)
                                else -> OnWarningContainer.copy(alpha = 0.8f)
                            }
                        )
                    }
                }
            }

            // Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { animatedPct },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = statusColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${subject.attendedClasses} attended of ${subject.totalClasses} classes (${subject.missedClasses} missed)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = if (isSafe) "✓ Safe" else "⚠️ Action Required",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
            }

            // Safe Bunks / Needed Classes Pill
            Surface(
                color = if (isSafe) Tertiary.copy(alpha = 0.1f) else Error.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isSafe) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isSafe) Tertiary else Error,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (isSafe) {
                                "${subject.safeBunksLeft} safe bunk${if (subject.safeBunksLeft == 1) "" else "s"} remaining before dropping below 75%"
                            } else {
                                "Must attend next ${subject.neededToReach75} class${if (subject.neededToReach75 == 1) "" else "es"} consecutively to reach 75%"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSafe) Tertiary else Error
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 0.8.dp
            )

            // Logging Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Log Present Button (Touch target >= 48dp)
                FilledTonalButton(
                    onClick = onLogAttended,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Tertiary.copy(alpha = 0.15f),
                        contentColor = Tertiary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_log_present_${subject.code}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Log Attended",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+ Present",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Log Missed Button (Touch target >= 48dp)
                OutlinedButton(
                    onClick = onLogMissed,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Error.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Error
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_log_missed_${subject.code}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Log Missed",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+ Missed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AttendanceStatPill(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
