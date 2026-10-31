package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.AcademicProjectEntity
import com.example.data.local.ProjectFocusSessionEntity
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel
import kotlinx.coroutines.delay
import java.util.Locale

enum class TimerMode(val title: String, val minutes: Int) {
    POMODORO("Pomodoro", 25),
    DEEP_WORK("Deep Work", 50),
    SPRINT("Sprint", 15),
    CUSTOM("Custom", 30)
}

/**
 * Focus Timer Screen that tracks study sessions and logs total time spent per project in Room SQLite.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerScreen(
    viewModel: CampusLifeViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val projects by viewModel.academicProjects.collectAsState()
    val preSelectedProject by viewModel.selectedProjectForTimer.collectAsState()
    val focusSessions by viewModel.projectFocusSessions.collectAsState()
    val projectTimeSummaries by viewModel.projectTimeSummaries.collectAsState()
    val totalMinutesOverall by viewModel.totalFocusMinutesOverall.collectAsState()

    // Active Selected Project for the timer
    var selectedProject by remember(projects, preSelectedProject) {
        mutableStateOf(
            preSelectedProject ?: projects.firstOrNull { it.normalizedStatus.name != "DONE" } ?: projects.firstOrNull()
        )
    }

    var selectedMode by remember { mutableStateOf(TimerMode.POMODORO) }
    var targetMinutes by remember { mutableIntStateOf(25) }
    var secondsRemaining by remember { mutableIntStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var sessionNotes by remember { mutableStateOf("") }
    var showLogDialog by remember { mutableStateOf(false) }
    var showProjectDropdown by remember { mutableStateOf(false) }
    var currentSound by remember { mutableStateOf("Library Silence") }

    // Update secondsRemaining when targetMinutes changes while not running
    LaunchedEffect(targetMinutes) {
        if (!isRunning) {
            secondsRemaining = targetMinutes * 60
        }
    }

    // Active Timer Coroutine
    LaunchedEffect(isRunning, secondsRemaining) {
        if (isRunning && secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining -= 1
        } else if (isRunning && secondsRemaining == 0) {
            isRunning = false
            showLogDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Focus Study Timer",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Room SQLite • Total Time Logged Per Project",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("focus_timer_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = Modifier.testTag("focus_timer_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Overall Focus Metrics Banner
            item {
                FocusOverviewBanner(
                    totalMinutes = totalMinutesOverall,
                    sessionCount = focusSessions.size,
                    projectsCount = projects.size
                )
            }

            // 2. Active Focus Timer Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("active_focus_timer_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Project Selector Row
                        Text(
                            text = "Project Linked to Session:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { showProjectDropdown = true }
                                    .testTag("timer_project_selector"),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FolderSpecial,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = selectedProject?.title ?: "Select a Project",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = selectedProject?.let {
                                                    "${it.subjectCode} • ${it.formattedFocusTime} logged so far"
                                                } ?: "Select which project to log time for",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showProjectDropdown,
                                onDismissRequest = { showProjectDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                projects.forEach { proj ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(proj.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(
                                                    "${proj.subjectCode} • ${proj.formattedFocusTime} total",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedProject = proj
                                            viewModel.selectProjectForTimer(proj)
                                            showProjectDropdown = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        },
                                        modifier = Modifier.testTag("select_project_${proj.id}")
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Timer Mode Selector Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TimerMode.values().forEach { mode ->
                                val isSelected = selectedMode == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (!isRunning) {
                                            selectedMode = mode
                                            targetMinutes = mode.minutes
                                            secondsRemaining = mode.minutes * 60
                                        }
                                    },
                                    label = { Text("${mode.title} (${mode.minutes}m)", fontSize = 12.sp) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("mode_${mode.name.lowercase()}"),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Circular Progress Indicator & Countdown
                        val totalSeconds = targetMinutes * 60
                        val progress = if (totalSeconds > 0) {
                            (totalSeconds - secondsRemaining).toFloat() / totalSeconds.toFloat()
                        } else 0f

                        val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(220.dp)
                                .testTag("timer_dial")
                        ) {
                            CircularProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.size(200.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 10.dp,
                                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                strokeCap = StrokeCap.Round
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val minutes = secondsRemaining / 60
                                val seconds = secondsRemaining % 60
                                Text(
                                    text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.testTag("timer_countdown_text")
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(100),
                                    color = if (isRunning) Color(0xFF10B981).copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = if (isRunning) "Deep Focus Active ⚡" else "Ready to Focus 🎯",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isRunning) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Controls: Start/Pause, Reset, Complete Session
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Reset Button
                            OutlinedButton(
                                onClick = {
                                    isRunning = false
                                    secondsRemaining = targetMinutes * 60
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("timer_reset_btn")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reset")
                            }

                            // Start / Pause Primary Button
                            Button(
                                onClick = { isRunning = !isRunning },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isRunning) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(48.dp)
                                    .testTag("timer_toggle_btn")
                            ) {
                                Icon(
                                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isRunning) "Pause" else "Start Focus")
                            }

                            // Complete & Log Session Early Button
                            FilledTonalButton(
                                onClick = {
                                    isRunning = false
                                    showLogDialog = true
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(48.dp)
                                    .testTag("timer_complete_session_btn")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Log Room")
                            }
                        }
                    }
                }
            }

            // 3. Total Time Spent Per Project (Room SQLite Aggregation)
            item {
                ProjectTimeBreakdownCard(
                    projects = projects,
                    summaries = projectTimeSummaries,
                    onSelectProject = { proj ->
                        selectedProject = proj
                        viewModel.selectProjectForTimer(proj)
                    }
                )
            }

            // 4. Logged Study Sessions History
            item {
                SessionsHistoryCard(
                    sessions = focusSessions,
                    onDeleteSession = { sessionId ->
                        viewModel.deleteProjectFocusSession(sessionId)
                    }
                )
            }

            // 5. Ambient Sounds Card
            item {
                AtmosphericSoundCard(
                    currentSound = currentSound,
                    onSelectSound = { currentSound = it }
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Dialog: Log Study Session to Room DB
    if (showLogDialog) {
        val elapsedSeconds = (targetMinutes * 60) - secondsRemaining
        val actualMinutes = (elapsedSeconds / 60).coerceAtLeast(1)

        LogSessionDialog(
            projectTitle = selectedProject?.title ?: "General Study",
            suggestedMinutes = actualMinutes,
            initialNotes = sessionNotes,
            onDismiss = { showLogDialog = false },
            onConfirmLog = { duration, notes, sessionType ->
                selectedProject?.let { proj ->
                    viewModel.logProjectFocusSession(
                        projectId = proj.id,
                        projectTitle = proj.title,
                        subjectCode = proj.subjectCode,
                        durationMinutes = duration,
                        notes = notes,
                        sessionType = sessionType
                    )
                } ?: run {
                    val fallback = projects.firstOrNull()
                    if (fallback != null) {
                        viewModel.logProjectFocusSession(
                            projectId = fallback.id,
                            projectTitle = fallback.title,
                            subjectCode = fallback.subjectCode,
                            durationMinutes = duration,
                            notes = notes,
                            sessionType = sessionType
                        )
                    }
                }
                secondsRemaining = targetMinutes * 60
                showLogDialog = false
            }
        )
    }
}

/**
 * Top banner displaying overall focus statistics.
 */
@Composable
private fun FocusOverviewBanner(
    totalMinutes: Int,
    sessionCount: Int,
    projectsCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("focus_overview_banner"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Project Focus Tracker",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Real-time session logging persisted in Room SQLite",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Total Focus Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        val hours = totalMinutes / 60
                        val mins = totalMinutes % 60
                        val formatted = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
                        Text(formatted, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Study Sessions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$sessionCount logged", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Tracked Projects", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$projectsCount active", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color(0xFFF59E0B))
                    }
                }
            }
        }
    }
}

/**
 * Card detailing total time spent per project, queried directly from Room SQLite.
 */
@Composable
private fun ProjectTimeBreakdownCard(
    projects: List<AcademicProjectEntity>,
    summaries: List<com.example.data.local.ProjectTimeSpentSummary>,
    onSelectProject: (AcademicProjectEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("project_time_breakdown_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Total Time Spent Per Project",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(100),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Room SQLite",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (projects.isEmpty()) {
                Text(
                    text = "No projects found in Room database.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val maxMinutes = projects.maxOfOrNull { it.totalFocusMinutes }?.coerceAtLeast(1) ?: 1

                projects.forEach { proj ->
                    val fraction = (proj.totalFocusMinutes.toFloat() / maxMinutes.toFloat()).coerceIn(0.05f, 1f)

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectProject(proj) },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = proj.subjectCode,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = proj.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = proj.formattedFocusTime,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { fraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(100)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card listing logged sessions in Room with notes and timestamps.
 */
@Composable
private fun SessionsHistoryCard(
    sessions: List<ProjectFocusSessionEntity>,
    onDeleteSession: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sessions_history_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recent Study Sessions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${sessions.size} sessions",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (sessions.isEmpty()) {
                Text(
                    text = "No study sessions recorded yet. Start the timer to log your first session!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                sessions.take(6).forEach { session ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
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
                                    text = session.projectTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (session.sessionNotes.isNotEmpty()) {
                                    Text(
                                        text = session.sessionNotes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = "${session.sessionDate} • ${session.sessionType}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(100),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = session.formattedDuration,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { onDeleteSession(session.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
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

/**
 * Atmospheric background sounds deck.
 */
@Composable
private fun AtmosphericSoundCard(
    currentSound: String,
    onSelectSound: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Atmospheric Study Ambience", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val sounds = listOf("Library Silence", "Gentle Rain", "Campus Cafe", "Alpha 40Hz Waves")
                items(sounds) { s ->
                    val selected = currentSound == s
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectSound(s) },
                        label = { Text(s, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary, selectedLabelColor = Color.White)
                    )
                }
            }
        }
    }
}

/**
 * Dialog to log completed session to Room DB with notes and duration confirmation.
 */
@Composable
private fun LogSessionDialog(
    projectTitle: String,
    suggestedMinutes: Int,
    initialNotes: String,
    onDismiss: () -> Unit,
    onConfirmLog: (duration: Int, notes: String, sessionType: String) -> Unit
) {
    var durationText by remember { mutableStateOf(suggestedMinutes.toString()) }
    var notes by remember { mutableStateOf(initialNotes) }
    var sessionType by remember { mutableStateOf("DEEP_WORK") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Log Study Session to Room",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Project: $projectTitle",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it },
                    label = { Text("Duration (minutes)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("log_duration_input")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Session Accomplishments / Notes") },
                    placeholder = { Text("e.g. Implemented consensus algorithms") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("log_notes_input")
                )

                // Session type chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("FOCUS", "DEEP_WORK", "SPRINT").forEach { type ->
                        val isSelected = sessionType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { sessionType = type },
                            label = { Text(type, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val mins = durationText.toIntOrNull() ?: suggestedMinutes
                            onConfirmLog(mins.coerceAtLeast(1), notes.trim(), sessionType)
                        },
                        modifier = Modifier.testTag("btn_confirm_log_session")
                    ) {
                        Text("Save to Room DB")
                    }
                }
            }
        }
    }
}
