package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AttendanceStatus
import com.example.model.CourseAttendance
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Material 3 Card component to track lecture attendance percentage for an individual course.
 * Features a circular percentage gauge, threshold progress marker, safe bunk bank / shortage alert,
 * lecture breakdown (Theory & Lab), quick 1-tap logging, and an inline what-if forecast simulator.
 */
@Composable
fun CourseLectureAttendanceCard(
    course: CourseAttendance,
    onQuickAttend: () -> Unit,
    onQuickMiss: () -> Unit,
    onLogSession: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSimulatorExpanded by remember { mutableStateOf(false) }
    var simAttends by remember { mutableIntStateOf(1) }
    var simMisses by remember { mutableIntStateOf(0) }

    val (statusColor, containerColor, onContainerColor) = when (course.status) {
        AttendanceStatus.SAFE -> Triple(Tertiary, TertiaryContainer, OnTertiaryContainer)
        AttendanceStatus.WARNING -> Triple(Warning, WarningContainer, OnWarningContainer)
        AttendanceStatus.CRITICAL -> Triple(Error, ErrorContainer, OnErrorContainer)
    }

    val forecast = remember(course, simAttends, simMisses) {
        course.getForecast(simAttends, simMisses)
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(
            width = 1.dp,
            color = if (!course.isSafe) statusColor.copy(alpha = 0.45f) else OutlineVariant.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("course_attendance_card_${course.courseCode}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Row 1: Course Info Header & Circular Percentage Gauge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Course Metadata
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onCardClick() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryContainer
                        ) {
                            Text(
                                text = course.courseCode,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = "${course.credits} Credits",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(100),
                            color = containerColor
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(statusColor)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (course.status) {
                                        AttendanceStatus.SAFE -> "Safe"
                                        AttendanceStatus.WARNING -> "Borderline"
                                        AttendanceStatus.CRITICAL -> "Shortage"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = onContainerColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = course.courseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (course.faculty.isNotEmpty()) "${course.faculty} • ${course.room}" else course.department,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Radial Percentage Meter
                AttendanceRadialGauge(
                    percentage = course.attendancePercentage,
                    targetPercentage = course.targetPercentage,
                    statusColor = statusColor,
                    modifier = Modifier.testTag("gauge_${course.courseCode}")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: Attendance Metrics & Safe Bunk/Shortage Pill
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LECTURE SESSIONS",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${course.attendedClasses} of ${course.totalClasses} Attended • ${course.missedClasses} Missed",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = containerColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (course.isSafe) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (course.isSafe) {
                                    "${course.safeBunksLeft} safe bunks left"
                                } else {
                                    "Needs +${course.neededToReachTarget} to 75%"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = onContainerColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 3: Component Breakdown (Theory vs Practical Lab)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ComponentProgressChip(
                    label = "Theory",
                    attended = course.theoryAttended,
                    total = course.theoryTotal,
                    modifier = Modifier.weight(1f)
                )
                ComponentProgressChip(
                    label = "Practical Lab",
                    attended = course.labAttended,
                    total = course.labTotal,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 4: Action Buttons (Present, Missed, Log, Simulator toggle)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // + Present Button
                Button(
                    onClick = onQuickAttend,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Tertiary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("quick_attend_btn_${course.courseCode}")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Present", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // + Missed Button
                OutlinedButton(
                    onClick = onQuickMiss,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                    border = BorderStroke(1.dp, Error.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("quick_miss_btn_${course.courseCode}")
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Missed", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Log Session Modal Button
                FilledTonalButton(
                    onClick = onLogSession,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("log_session_btn_${course.courseCode}")
                ) {
                    Icon(Icons.Default.EditCalendar, contentDescription = "Log Session", modifier = Modifier.size(15.dp))
                }

                // Inline Simulator Toggle
                FilledTonalButton(
                    onClick = { isSimulatorExpanded = !isSimulatorExpanded },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isSimulatorExpanded) PrimaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = if (isSimulatorExpanded) Primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("toggle_simulator_btn_${course.courseCode}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "Forecast Calculator",
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // Expandable What-If Forecast Drawer
            AnimatedVisibility(visible = isSimulatorExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(12.dp)
                        .testTag("simulator_drawer_${course.courseCode}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "What-If Attendance Simulator",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = {
                                simAttends = 1
                                simMisses = 0
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset simulator", modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Stepper 1: Future Attended
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Next lectures to attend:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (simAttends > 0) simAttends-- },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(14.dp))
                            }
                            Text(
                                text = "+$simAttends",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Tertiary,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            IconButton(
                                onClick = { if (simAttends < 20) simAttends++ },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    // Stepper 2: Future Missed
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Next lectures to bunk/miss:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (simMisses > 0) simMisses-- },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease misses", modifier = Modifier.size(14.dp))
                            }
                            Text(
                                text = "+$simMisses",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Error,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            IconButton(
                                onClick = { if (simMisses < 15) simMisses++ },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase misses", modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Projected Result Box
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (forecast.isProjectedSafe) TertiaryContainer else ErrorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "PROJECTED OUTCOME",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (forecast.isProjectedSafe) OnTertiaryContainer else OnErrorContainer
                                )
                                Text(
                                    text = "${forecast.projectedAttended} / ${forecast.projectedTotal} lectures",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (forecast.isProjectedSafe) OnTertiaryContainer else OnErrorContainer
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = String.format(Locale.US, "%.1f%%", forecast.projectedPercentage),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (forecast.isProjectedSafe) Tertiary else Error
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val deltaSign = if (forecast.percentageDelta >= 0) "+" else ""
                                Text(
                                    text = "($deltaSign${String.format(Locale.US, "%.1f%%", forecast.percentageDelta)})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (forecast.isProjectedSafe) Tertiary else Error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Circular radial gauge showing percentage with 75% target threshold marker.
 */
@Composable
fun AttendanceRadialGauge(
    percentage: Double,
    targetPercentage: Double,
    statusColor: Color,
    modifier: Modifier = Modifier,
    size: Float = 72f
) {
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage.toFloat().coerceIn(0f, 100f),
        label = "radial_pct"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            val strokeWidth = 6.dp.toPx()
            val diameter = this.size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = Size(diameter, diameter)

            // Background Track
            drawArc(
                color = Color(0xFFE2E8F0),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Attended Progress Arc
            val sweep = (animatedPercentage / 100f) * 360f
            drawArc(
                color = statusColor,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 75% Target Marker Tick
            val targetAngleRad = Math.toRadians((-90.0 + (targetPercentage / 100.0) * 360.0))
            val radius = diameter / 2
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val markerInner = Offset(
                (center.x + (radius - strokeWidth / 2) * cos(targetAngleRad)).toFloat(),
                (center.y + (radius - strokeWidth / 2) * sin(targetAngleRad)).toFloat()
            )
            val markerOuter = Offset(
                (center.x + (radius + strokeWidth / 2 + 2) * cos(targetAngleRad)).toFloat(),
                (center.y + (radius + strokeWidth / 2 + 2) * sin(targetAngleRad)).toFloat()
            )
            drawLine(
                color = Color(0xFF0F172A),
                start = markerInner,
                end = markerOuter,
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Percentage text at center
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format(Locale.US, "%.0f%%", percentage),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = statusColor,
                fontSize = 15.sp
            )
            Text(
                text = "Target 75%",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Micro progress chip for Theory or Practical components.
 */
@Composable
private fun ComponentProgressChip(
    label: String,
    attended: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val pct = if (total <= 0) 100.0 else (attended.toDouble() / total) * 100.0
    val isSafe = pct >= 75.0

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$attended / $total",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = String.format(Locale.US, "%.0f%%", pct),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSafe) Tertiary else Error
            )
        }
    }
}
