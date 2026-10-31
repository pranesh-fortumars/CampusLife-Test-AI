package com.example.ui.components.charts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Subject
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.RowChart
import ir.ehsannarmani.compose_charts.models.Bars

enum class ChartOrientation(val label: String) {
    VERTICAL("Vertical Columns"),
    HORIZONTAL("Horizontal Bars")
}

/**
 * Visualizes Course Attendance Percentages from the Room Database using the Compose-Charts charting library.
 * Features:
 * - Real-time Room DB reactive data binding.
 * - Dynamic color thresholds (Safe >= 75% in Emerald Green, At Risk < 75% in Crimson Red / Amber).
 * - Statutory 75% minimum university threshold indicator and benchmark badge.
 * - Toggleable orientations: Vertical Column Chart vs Horizontal Row Chart.
 * - Interactive course inspection chip and statistics summary.
 */
@Composable
fun CourseAttendanceBarChart(
    subjects: List<Subject>,
    modifier: Modifier = Modifier,
    onCourseSelected: ((Subject) -> Unit)? = null
) {
    var selectedOrientation by remember { mutableStateOf(ChartOrientation.VERTICAL) }
    var selectedCourseCode by remember { mutableStateOf<String?>(null) }

    val safeColor = Color(0xFF10B981) // Emerald Green for >= 75%
    val atRiskColor = Color(0xFFEF4444) // Coral Red for < 75%
    val benchmarkColor = Color(0xFFF59E0B) // Amber for threshold line

    // Selected course details
    val activeSubject = remember(selectedCourseCode, subjects) {
        subjects.find { it.code == selectedCourseCode } ?: subjects.firstOrNull()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("attendance_bar_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Chart Header & Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Chart",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Course Attendance Visualizer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Room SQLite Database Live Feed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Chart Orientation Selector
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(2.dp)
                ) {
                    ChartOrientation.values().forEach { orientation ->
                        val isSelected = selectedOrientation == orientation
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else Color.Transparent
                                )
                                .clickable { selectedOrientation = orientation }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("chart_mode_${orientation.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (orientation == ChartOrientation.VERTICAL) "Columns" else "Bars",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Statutory Minimum Benchmark Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(benchmarkColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Statutory Requirement: 75.0% Minimum",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(safeColor)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Safe (≥75%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(atRiskColor)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "At Risk (<75%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (subjects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No course attendance data found in Room database.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Prepare Chart Data for ir.ehsannarmani.compose_charts
                val chartBars = remember(subjects) {
                    subjects.map { subject ->
                        val pct = subject.attendancePercentage
                        val isSafe = pct >= 75.0
                        val barBrush: Brush = if (isSafe) {
                            SolidColor(safeColor)
                        } else {
                            SolidColor(atRiskColor)
                        }

                        Bars(
                            label = subject.code,
                            values = listOf(
                                Bars.Data(
                                    label = "${subject.code} (${String.format(java.util.Locale.US, "%.1f", pct)}%)",
                                    value = pct.coerceIn(0.0, 100.0),
                                    color = barBrush
                                )
                            )
                        )
                    }
                }

                // Render Chart Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (selectedOrientation == ChartOrientation.VERTICAL) 220.dp else (subjects.size * 42).coerceAtLeast(200).dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp)
                        .testTag("attendance_chart_canvas_container")
                ) {
                    if (selectedOrientation == ChartOrientation.VERTICAL) {
                        ColumnChart(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("attendance_column_chart"),
                            data = chartBars
                        )
                    } else {
                        RowChart(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("attendance_row_chart"),
                            data = chartBars
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Course Quick-Selection Chips for Interactive Inspection
                Text(
                    text = "Tap a course to inspect attendance status:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subjects.take(5).forEach { sub ->
                        val isSelected = sub.code == activeSubject?.code
                        val isSafe = sub.attendancePercentage >= 75.0
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCourseCode = sub.code
                                onCourseSelected?.invoke(sub)
                            },
                            label = {
                                Text(
                                    text = "${sub.code}: ${String.format(java.util.Locale.US, "%.0f", sub.attendancePercentage)}%",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isSafe) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isSafe) safeColor else atRiskColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            modifier = Modifier.testTag("chart_course_chip_${sub.code}")
                        )
                    }
                }

                // Detailed inspection banner for active course
                activeSubject?.let { sub ->
                    Spacer(modifier = Modifier.height(10.dp))
                    val isSafe = sub.attendancePercentage >= 75.0
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSafe) safeColor.copy(alpha = 0.1f) else atRiskColor.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSafe) safeColor.copy(alpha = 0.4f) else atRiskColor.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${sub.name} (${sub.code})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${sub.attendedClasses} attended of ${sub.totalClasses} total lectures",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = String.format(java.util.Locale.US, "%.1f%%", sub.attendancePercentage),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSafe) safeColor else atRiskColor
                                )
                                Text(
                                    text = if (isSafe) "${sub.safeBunksLeft} safe bunks left" else "Need ${sub.neededToReach75} classes",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSafe) safeColor else atRiskColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
