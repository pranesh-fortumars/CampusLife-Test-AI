package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AcademicTask
import com.example.ui.theme.*
import kotlinx.coroutines.delay

enum class PomodoroMode(val displayName: String, val defaultMinutes: Int, val emoji: String) {
    FOCUS("Focus Session", 25, "🎯"),
    SHORT_BREAK("Short Break", 5, "☕"),
    LONG_BREAK("Long Break", 15, "🛋️")
}

/**
 * Material 3 Pomodoro-style study timer component allowing students to start, pause,
 * and reset focus sessions specifically linked to their academic tasks.
 */
@Composable
fun PomodoroTaskTimerComponent(
    tasks: List<AcademicTask> = emptyList(),
    initialSelectedTaskId: String? = null,
    onSessionCompleted: ((taskId: String?, durationMinutes: Int) -> Unit)? = null,
    onToggleTaskComplete: ((taskId: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Current Pomodoro mode (Focus 25m, Short Break 5m, Long Break 15m)
    var currentMode by remember { mutableStateOf(PomodoroMode.FOCUS) }

    // Selected academic task
    var selectedTaskId by remember {
        mutableStateOf(initialSelectedTaskId ?: tasks.firstOrNull { !it.isCompleted }?.id)
    }
    val activeTask = remember(tasks, selectedTaskId) {
        tasks.find { it.id == selectedTaskId }
    }

    // Timer state
    var totalSeconds by remember(currentMode) { mutableIntStateOf(currentMode.defaultMinutes * 60) }
    var remainingSeconds by remember(currentMode) { mutableIntStateOf(currentMode.defaultMinutes * 60) }
    var isRunning by remember { mutableStateOf(false) }

    // Session cycle counters
    var completedSessionsToday by remember { mutableIntStateOf(3) }
    var currentCycleIndex by remember { mutableIntStateOf(1) } // 1 to 4
    val maxCycle = 4

    var showTaskPicker by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    // Coroutine timer ticker
    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
        } else if (isRunning && remainingSeconds == 0) {
            isRunning = false
            if (currentMode == PomodoroMode.FOCUS) {
                completedSessionsToday += 1
                onSessionCompleted?.invoke(activeTask?.id, currentMode.defaultMinutes)
                feedbackMessage = "Focus session completed! Great work on ${activeTask?.title ?: "academics"} 🎉"
            } else {
                feedbackMessage = "Break is over! Ready for the next focus sprint? ⚡"
            }
        }
    }

    // Formatted time MM:SS
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    // Progress for circular indicator
    val progress = if (totalSeconds > 0) {
        1f - (remainingSeconds.toFloat() / totalSeconds.toFloat())
    } else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "pomodoro_progress")

    // Theme color based on mode
    val modeColor = when (currentMode) {
        PomodoroMode.FOCUS -> Primary
        PomodoroMode.SHORT_BREAK -> Tertiary
        PomodoroMode.LONG_BREAK -> Secondary
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("pomodoro_study_timer_component")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Component Title and Streak Summary
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
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🍅", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Pomodoro Study Timer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Linked to Academic Tasks & Deadlines",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(100),
                    color = TertiaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Tertiary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$completedSessionsToday done today",
                            style = MaterialTheme.typography.labelSmall,
                            color = Tertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Linked Academic Task Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTaskPicker = true }
                    .testTag("pomodoro_task_target_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(modeColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ACTIVE ACADEMIC TASK",
                                style = MaterialTheme.typography.labelSmall,
                                color = modeColor,
                                fontWeight = FontWeight.Bold
                            )
                            if (activeTask != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = PrimaryContainer) {
                                    Text(
                                        text = activeTask.subjectCode,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeTask?.title ?: "General Campus Study & Problem Solving",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (activeTask != null) "Due: ${activeTask.dueDate} • Priority: ${activeTask.priority}" else "Tap to attach a specific course deadline or project",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.testTag("change_task_button")
                    ) {
                        Text(
                            text = "Change ▾",
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Mode Selector Tabs (Focus, Short Break, Long Break)
            Surface(
                shape = RoundedCornerShape(100),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PomodoroMode.values().forEach { mode ->
                        val isSelected = currentMode == mode
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = if (isSelected) modeColor else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (currentMode != mode) {
                                        currentMode = mode
                                        totalSeconds = mode.defaultMinutes * 60
                                        remainingSeconds = mode.defaultMinutes * 60
                                        isRunning = false
                                    }
                                }
                                .testTag("pomodoro_mode_${mode.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(mode.emoji, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${mode.displayName.substringBefore(" ")} (${mode.defaultMinutes}m)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Circular Countdown Display
            Box(
                modifier = Modifier
                    .size(210.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background & Animated progress arc
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12.dp.toPx()
                    // Track circle
                    drawCircle(
                        color = modeColor.copy(alpha = 0.12f),
                        style = Stroke(width = strokeWidth)
                    )
                    // Animated progress arc
                    val sweepAngle = animatedProgress * 360f
                    drawArc(
                        color = modeColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("pomodoro_timer_display")
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Surface(
                        shape = RoundedCornerShape(100),
                        color = if (isRunning) TertiaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = when {
                                !isRunning && remainingSeconds == totalSeconds -> "READY"
                                isRunning -> "• IN FLOW"
                                else -> "PAUSED"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isRunning) Tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Cycle indicator (e.g. 🍅 🍅 ⚪ ⚪)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..maxCycle) {
                            Text(
                                text = if (i <= currentCycleIndex) "🍅" else "⚪",
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "($currentCycleIndex/$maxCycle)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Primary Control Buttons (Start, Pause, Reset, Skip)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                FilledTonalIconButton(
                    onClick = {
                        isRunning = false
                        remainingSeconds = totalSeconds
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("pomodoro_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset Timer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Primary Start / Pause Button
                Button(
                    onClick = { isRunning = !isRunning },
                    shape = RoundedCornerShape(100),
                    colors = ButtonDefaults.buttonColors(containerColor = modeColor),
                    modifier = Modifier
                        .height(52.dp)
                        .padding(horizontal = 8.dp)
                        .testTag(if (isRunning) "pomodoro_pause_button" else "pomodoro_start_button")
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRunning) "Pause Focus" else if (remainingSeconds < totalSeconds) "Resume" else "Start Focus",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Skip to Next Phase Button
                FilledTonalIconButton(
                    onClick = {
                        isRunning = false
                        // Advance to next cycle or break
                        if (currentMode == PomodoroMode.FOCUS) {
                            if (currentCycleIndex < maxCycle) {
                                currentMode = PomodoroMode.SHORT_BREAK
                            } else {
                                currentMode = PomodoroMode.LONG_BREAK
                            }
                        } else {
                            currentCycleIndex = if (currentCycleIndex >= maxCycle) 1 else currentCycleIndex + 1
                            currentMode = PomodoroMode.FOCUS
                        }
                        totalSeconds = currentMode.defaultMinutes * 60
                        remainingSeconds = totalSeconds
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("pomodoro_skip_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Session",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Task Actions & Completion Options
            if (activeTask != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            onToggleTaskComplete?.invoke(activeTask.id)
                            feedbackMessage = "Marked '${activeTask.title}' as completed! ✅"
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("mark_task_complete_btn")
                    ) {
                        Icon(
                            imageVector = if (activeTask.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (activeTask.isCompleted) Tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (activeTask.isCompleted) "Completed" else "Mark Complete",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    TextButton(
                        onClick = {
                            onSessionCompleted?.invoke(activeTask.id, 25)
                            feedbackMessage = "Logged 25m study time to ${activeTask.title}"
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("pomodoro_log_session_button")
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log 25m Focus", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // In-component status feedback
            AnimatedVisibility(visible = feedbackMessage != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TertiaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Celebration, contentDescription = null, tint = Tertiary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feedbackMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Tertiary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { feedbackMessage = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }

    // Modal Task Picker Dialog
    if (showTaskPicker) {
        Dialog(onDismissRequest = { showTaskPicker = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .testTag("task_picker_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Focus Task",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showTaskPicker = false }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Text(
                        text = "Choose an academic assignment, project, or exam topic to dedicate your Pomodoro focus session to:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Option: Unlinked General Study
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedTaskId == null) PrimaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, if (selectedTaskId == null) Primary else OutlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTaskId = null
                                showTaskPicker = false
                            }
                            .testTag("task_option_general")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = Primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("General Study & Self-Learning", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Unlinked session • Tracked under general study time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // List of Academic Tasks
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        items(tasks) { task ->
                            val isSelected = task.id == selectedTaskId
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) PrimaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                                border = BorderStroke(1.dp, if (isSelected) Primary else OutlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedTaskId = task.id
                                        showTaskPicker = false
                                    }
                                    .testTag("task_option_${task.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                                            ) {
                                                Text(
                                                    text = task.subjectCode,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = when (task.priority) {
                                                    "URGENT" -> ErrorContainer
                                                    "HIGH" -> TertiaryContainer
                                                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                                                }
                                            ) {
                                                Text(
                                                    text = task.priority,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (task.priority) {
                                                        "URGENT" -> Error
                                                        "HIGH" -> Tertiary
                                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                    },
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Due: ${task.dueDate} • ${task.category}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
