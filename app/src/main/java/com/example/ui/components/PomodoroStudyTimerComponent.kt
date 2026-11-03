package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CourseEntity
import com.example.data.local.SubjectStudyStats
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel
import com.example.worker.TaskNotificationHelper
import kotlinx.coroutines.delay

enum class TimerMode(val title: String, val defaultMinutes: Int) {
    FOCUS_25("Focus (25m)", 25),
    FOCUS_50("Deep Focus (50m)", 50),
    SHORT_BREAK("Short Break (5m)", 5),
    LONG_BREAK("Long Break (15m)", 15)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroStudyTimerComponent(
    viewModel: CampusLifeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val courses by viewModel.courses.collectAsState()
    val studyStats by viewModel.studyStatsPerSubject.collectAsState()
    val totalFocusMinutes by viewModel.totalFocusMinutes.collectAsState()

    var selectedMode by remember { mutableStateOf(TimerMode.FOCUS_25) }
    var selectedCourse by remember { mutableStateOf<CourseEntity?>(null) }

    // Default to first course if none selected
    LaunchedEffect(courses) {
        if (selectedCourse == null && courses.isNotEmpty()) {
            selectedCourse = courses.first()
        }
    }

    var totalSeconds by remember(selectedMode) { mutableIntStateOf(selectedMode.defaultMinutes * 60) }
    var secondsRemaining by remember(selectedMode) { mutableIntStateOf(selectedMode.defaultMinutes * 60) }
    var isRunning by remember { mutableStateOf(false) }

    // Active ticking timer coroutine
    LaunchedEffect(isRunning, secondsRemaining) {
        if (isRunning && secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining -= 1
        } else if (isRunning && secondsRemaining == 0) {
            isRunning = false
            val course = selectedCourse
            val courseCode = course?.code ?: "STUDY"
            val subjectName = course?.name ?: "General Academics"

            if (selectedMode == TimerMode.FOCUS_25 || selectedMode == TimerMode.FOCUS_50) {
                // Save study session to Room
                viewModel.recordStudySession(
                    courseCode = courseCode,
                    subjectName = subjectName,
                    durationMinutes = selectedMode.defaultMinutes,
                    mode = "FOCUS"
                )
                // Fire notification
                TaskNotificationHelper.showFocusSessionFinishedNotification(
                    context = context,
                    courseCode = courseCode,
                    subjectName = subjectName,
                    durationMinutes = selectedMode.defaultMinutes
                )
            } else {
                TaskNotificationHelper.showBreakFinishedNotification(context)
            }

            // Reset for next block
            secondsRemaining = selectedMode.defaultMinutes * 60
        }
    }

    val progress = remember(secondsRemaining, totalSeconds) {
        if (totalSeconds == 0) 0f else (secondsRemaining.toFloat() / totalSeconds.toFloat())
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "pomodoro_progress"
    )

    val timerColor = when (selectedMode) {
        TimerMode.FOCUS_25, TimerMode.FOCUS_50 -> IndigoPrimary
        TimerMode.SHORT_BREAK -> EmeraldTertiary
        TimerMode.LONG_BREAK -> VioletSecondary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pomodoro_study_timer"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Title and Mode Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = timerColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (selectedMode == TimerMode.SHORT_BREAK || selectedMode == TimerMode.LONG_BREAK) Icons.Default.Coffee else Icons.Default.Timer,
                                contentDescription = null,
                                tint = timerColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Pomodoro Study Timer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Focus sessions with notification alerts",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(100),
                    color = timerColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isRunning) "Running" else "Idle",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = timerColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Mode Selector Pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(TimerMode.values()) { mode ->
                    val isSelected = selectedMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (!isRunning) {
                                selectedMode = mode
                                totalSeconds = mode.defaultMinutes * 60
                                secondsRemaining = mode.defaultMinutes * 60
                            }
                        },
                        label = { Text(mode.title, fontSize = 11.sp) },
                        shape = RoundedCornerShape(100),
                        modifier = Modifier.heightIn(min = 36.dp)
                    )
                }
            }

            // Subject Selector for Focus Session
            if (selectedMode == TimerMode.FOCUS_25 || selectedMode == TimerMode.FOCUS_50) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Subject to Log:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(courses) { course ->
                            val isSelected = selectedCourse?.id == course.id
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) IndigoPrimaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, IndigoPrimary) else null,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedCourse = course }
                                    .testTag("chip_course_${course.code}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = course.code,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) IndigoOnPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = IndigoPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Circular Radial Progress Gauge with Digital Time
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp)
            ) {
                val trackColor = MaterialTheme.colorScheme.surfaceVariant
                val activeColor = timerColor

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    val arcSize = Size(diameter, diameter)

                    // Background track
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active animated progress arc
                    drawArc(
                        color = activeColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Digital Clock Inside Ring
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val minutes = secondsRemaining / 60
                    val seconds = secondsRemaining % 60
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = selectedCourse?.code ?: "General",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = timerColor
                    )
                }
            }

            // Controls: Start/Pause, Reset, Finish & Log
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                IconButton(
                    onClick = {
                        isRunning = false
                        secondsRemaining = selectedMode.defaultMinutes * 60
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("btn_timer_reset")
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reset timer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Start / Pause Main Action Pill
                Button(
                    onClick = { isRunning = !isRunning },
                    colors = ButtonDefaults.buttonColors(containerColor = timerColor),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_timer_start_pause")
                ) {
                    Icon(
                        if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRunning) "Pause Session" else "Start Timer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Complete & Log Early Button
                if (selectedMode == TimerMode.FOCUS_25 || selectedMode == TimerMode.FOCUS_50) {
                    IconButton(
                        onClick = {
                            val elapsedMinutes = (totalSeconds - secondsRemaining) / 60
                            val loggedMins = if (elapsedMinutes >= 1) elapsedMinutes else selectedMode.defaultMinutes
                            val course = selectedCourse
                            val courseCode = course?.code ?: "CS201"
                            val subjectName = course?.name ?: "Course Study"

                            viewModel.recordStudySession(
                                courseCode = courseCode,
                                subjectName = subjectName,
                                durationMinutes = loggedMins,
                                mode = "FOCUS"
                            )
                            TaskNotificationHelper.showFocusSessionFinishedNotification(
                                context,
                                courseCode,
                                subjectName,
                                loggedMins
                            )
                            isRunning = false
                            secondsRemaining = selectedMode.defaultMinutes * 60
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(EmeraldTertiaryContainer)
                            .testTag("btn_timer_log_session")
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Log Completed Session",
                            tint = EmeraldTertiary
                        )
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 0.8.dp)

            // Total Study Hours per Subject Breakdown Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("subject_study_stats_card"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Subject Study Hours (Room DB)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tracked deep focus minutes per subject",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val totalHours = totalFocusMinutes / 60.0
                    Surface(
                        shape = RoundedCornerShape(100),
                        color = EmeraldTertiaryContainer
                    ) {
                        Text(
                            text = "${String.format("%.1f", totalHours)} hrs total",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldOnTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                if (studyStats.isEmpty()) {
                    Text(
                        "No study sessions logged yet. Complete a focus block above to start tracking!",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val maxMinutes = (studyStats.maxOfOrNull { it.totalMinutes } ?: 60).coerceAtLeast(60)

                    studyStats.forEach { stat ->
                        val hours = stat.totalMinutes / 60.0
                        val barFraction = (stat.totalMinutes.toFloat() / maxMinutes.toFloat()).coerceIn(0.1f, 1f)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = IndigoPrimaryContainer
                                    ) {
                                        Text(
                                            text = stat.courseCode,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = IndigoOnPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = "${stat.sessionCount} sessions",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = "${String.format("%.1f", hours)} hrs (${stat.totalMinutes}m)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Horizontal Bar Representation
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(100))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(barFraction)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(100))
                                        .background(IndigoPrimary)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
