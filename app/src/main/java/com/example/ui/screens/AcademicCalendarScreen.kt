package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AcademicCalendarEventEntity
import com.example.ui.components.TaskDatePickerDialog
import com.example.ui.components.TaskDateUtils
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dedicated Compose UI screen to manage and view the semester academic calendar
 * with deadlines for exams, assignments, projects, labs, and academic holidays,
 * backed by a Room database.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicCalendarScreen(
    viewModel: CampusLifeViewModel,
    onBack: () -> Unit
) {
    val events by viewModel.calendarEvents.collectAsState()

    var selectedTypeFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var reschedulingEventId by remember { mutableStateOf<String?>(null) }
    var viewMode by remember { mutableStateOf("TIMELINE") } // "TIMELINE" or "MONTH"
    var selectedCalendarDay by remember { mutableStateOf<Int?>(null) }

    val filterTypes = listOf(
        "ALL" to "All Deadlines",
        "EXAM" to "Exams 📄",
        "ASSIGNMENT" to "Assignments 📝",
        "PROJECT" to "Projects 💻",
        "LAB" to "Labs 🧪",
        "HOLIDAY" to "Holidays 🏖️"
    )

    // Filtered events
    val filteredEvents = remember(events, selectedTypeFilter, searchQuery, selectedCalendarDay, viewMode) {
        events.filter { event ->
            val matchesType = when (selectedTypeFilter) {
                "ALL" -> true
                "EXAM" -> event.isExam
                "ASSIGNMENT" -> event.isAssignment
                "PROJECT" -> event.isProject
                "LAB" -> event.isLab
                "HOLIDAY" -> event.isHoliday
                else -> event.eventType.equals(selectedTypeFilter, ignoreCase = true)
            }
            val matchesQuery = if (searchQuery.isBlank()) true else {
                event.title.contains(searchQuery, ignoreCase = true) ||
                        event.courseCode.contains(searchQuery, ignoreCase = true) ||
                        event.courseName.contains(searchQuery, ignoreCase = true) ||
                        event.locationOrRoom.contains(searchQuery, ignoreCase = true)
            }
            val matchesDay = if (viewMode == "MONTH" && selectedCalendarDay != null) {
                val cal = Calendar.getInstance().apply { timeInMillis = event.timestamp }
                cal.get(Calendar.DAY_OF_MONTH) == selectedCalendarDay
            } else true

            matchesType && matchesQuery && matchesDay
        }
    }

    // Nearest upcoming exam calculation
    val nearestExam = remember(events) {
        val now = System.currentTimeMillis()
        events
            .filter { it.isExam && !it.isCompleted && it.timestamp >= now - (12 * 60 * 60 * 1000L) }
            .minByOrNull { it.timestamp }
    }

    val totalEvents = events.size
    val completedEvents = events.count { it.isCompleted }
    val examCount = events.count { it.isExam && !it.isCompleted }
    val assignmentCount = events.count { it.isAssignment && !it.isCompleted }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Academic Calendar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Semester 5 • Exam & Assignment Schedule",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("academic_calendar_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewMode = if (viewMode == "TIMELINE") "MONTH" else "TIMELINE"
                        },
                        modifier = Modifier.testTag("calendar_toggle_view_btn")
                    ) {
                        Icon(
                            imageVector = if (viewMode == "TIMELINE") Icons.Default.CalendarMonth else Icons.Default.ViewAgenda,
                            contentDescription = if (viewMode == "TIMELINE") "Switch to Month View" else "Switch to Timeline View",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddEventDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Deadline", fontWeight = FontWeight.SemiBold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_calendar_event_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("academic_calendar_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nearest Exam Countdown Banner
            if (nearestExam != null) {
                item {
                    NearestExamCountdownCard(nearestExam = nearestExam)
                }
            }

            // Summary Metrics Card
            item {
                AcademicCalendarMetricsCard(
                    totalEvents = totalEvents,
                    examCount = examCount,
                    assignmentCount = assignmentCount,
                    completedEvents = completedEvents
                )
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by subject, exam, or deadline...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("calendar_search_input")
                )
            }

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filterTypes) { (typeKey, label) ->
                        val isSelected = selectedTypeFilter == typeKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTypeFilter = typeKey },
                            label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(100),
                            modifier = Modifier.testTag("calendar_filter_$typeKey")
                        )
                    }
                }
            }

            // Month Calendar View (if active)
            if (viewMode == "MONTH") {
                item {
                    MonthCalendarGridCard(
                        events = events,
                        selectedDay = selectedCalendarDay,
                        onSelectDay = { day ->
                            selectedCalendarDay = if (selectedCalendarDay == day) null else day
                        }
                    )
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val countText = "${filteredEvents.size} ${if (filteredEvents.size == 1) "Deadline" else "Deadlines"}"
                    Text(
                        text = if (selectedCalendarDay != null) "Deadlines on Nov $selectedCalendarDay ($countText)" else "Semester Deadlines ($countText)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (selectedCalendarDay != null) {
                        TextButton(onClick = { selectedCalendarDay = null }) {
                            Text("Show All Days", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Empty State
            if (filteredEvents.isEmpty()) {
                item {
                    AcademicCalendarEmptyState(
                        isFiltered = selectedTypeFilter != "ALL" || searchQuery.isNotEmpty() || selectedCalendarDay != null,
                        onResetFilters = {
                            selectedTypeFilter = "ALL"
                            searchQuery = ""
                            selectedCalendarDay = null
                        }
                    )
                }
            } else {
                // List of Deadlines
                items(filteredEvents, key = { it.id }) { event ->
                    AcademicDeadlineItemCard(
                        event = event,
                        onToggleCompleted = { viewModel.toggleCalendarEventCompleted(event.id) },
                        onReschedule = { reschedulingEventId = event.id },
                        onDelete = { viewModel.deleteCalendarEvent(event.id) }
                    )
                }
            }
        }
    }

    // Add Deadline Dialog
    if (showAddEventDialog) {
        AddAcademicCalendarEventDialog(
            onDismiss = { showAddEventDialog = false },
            onAdd = { title, courseCode, courseName, eventType, date, time, timestamp, location, weightage, desc ->
                viewModel.addCalendarEvent(
                    title = title,
                    courseCode = courseCode,
                    courseName = courseName,
                    eventType = eventType,
                    date = date,
                    time = time,
                    timestamp = timestamp,
                    locationOrRoom = location,
                    weightage = weightage,
                    description = desc
                )
                showAddEventDialog = false
            }
        )
    }

    // Reschedule Date Picker Dialog
    if (reschedulingEventId != null) {
        val targetEvent = events.find { it.id == reschedulingEventId }
        TaskDatePickerDialog(
            initialSelectedDateMillis = targetEvent?.timestamp,
            onDateSelected = { newTimestamp, formattedDate ->
                targetEvent?.let {
                    viewModel.rescheduleCalendarEvent(it.id, formattedDate, newTimestamp)
                }
                reschedulingEventId = null
            },
            onDismiss = { reschedulingEventId = null }
        )
    }
}

/**
 * Hero Card highlighting the nearest upcoming critical exam countdown.
 */
@Composable
fun NearestExamCountdownCard(nearestExam: AcademicCalendarEventEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("nearest_exam_countdown_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(2.dp)
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
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.error
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = nearestExam.urgencyLabel.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = nearestExam.weightage,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = nearestExam.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${nearestExam.date} • ${nearestExam.time}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = nearestExam.locationOrRoom,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            if (nearestExam.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Syllabus: ${nearestExam.description}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Summary metrics card showing aggregate counts of calendar deadlines.
 */
@Composable
fun AcademicCalendarMetricsCard(
    totalEvents: Int,
    examCount: Int,
    assignmentCount: Int,
    completedEvents: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calendar_metrics_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MetricStatItem(
                count = examCount.toString(),
                label = "Upcoming Exams",
                color = MaterialTheme.colorScheme.error
            )
            Divider(modifier = Modifier.height(32.dp).width(1.dp))
            MetricStatItem(
                count = assignmentCount.toString(),
                label = "Assignments",
                color = MaterialTheme.colorScheme.primary
            )
            Divider(modifier = Modifier.height(32.dp).width(1.dp))
            MetricStatItem(
                count = "$completedEvents/$totalEvents",
                label = "Completed",
                color = SuccessGreen
            )
        }
    }
}

@Composable
fun MetricStatItem(count: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Month Calendar Grid showing the current semester month dates with deadline indicators.
 */
@Composable
fun MonthCalendarGridCard(
    events: List<AcademicCalendarEventEntity>,
    selectedDay: Int?,
    onSelectDay: (Int) -> Unit
) {
    // Current month days for Nov 2026 (30 days, Nov 1 is Sunday)
    val totalDays = 30
    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")

    // Map days with events
    val dayEventMap = remember(events) {
        val map = mutableMapOf<Int, MutableList<AcademicCalendarEventEntity>>()
        events.forEach { event ->
            val cal = Calendar.getInstance().apply { timeInMillis = event.timestamp }
            val day = cal.get(Calendar.DAY_OF_MONTH)
            map.getOrPut(day) { mutableListOf() }.add(event)
        }
        map
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("month_calendar_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                Text(
                    text = "November 2026",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap a day to view deadlines",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day labels
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                daysOfWeek.forEach { dayName ->
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grid of days (1 to 30)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (row in 0..4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (col in 0..6) {
                            val dayNum = row * 7 + col + 1
                            if (dayNum <= totalDays) {
                                val dayEvents = dayEventMap[dayNum] ?: emptyList()
                                val hasExams = dayEvents.any { it.isExam }
                                val hasAssignments = dayEvents.any { it.isAssignment || it.isProject }
                                val isSelected = selectedDay == dayNum

                                val bgColor = when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    hasExams -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                                    hasAssignments -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    dayEvents.isNotEmpty() -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                    else -> Color.Transparent
                                }

                                val textColor = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    hasExams -> MaterialTheme.colorScheme.error
                                    hasAssignments -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(bgColor)
                                        .clickable { onSelectDay(dayNum) }
                                        .testTag("calendar_day_$dayNum"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = dayNum.toString(),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (dayEvents.isNotEmpty() || isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = textColor
                                        )
                                        if (dayEvents.isNotEmpty() && !isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(if (hasExams) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual deadline / exam item card with complete metadata and Room actions.
 */
@Composable
fun AcademicDeadlineItemCard(
    event: AcademicCalendarEventEntity,
    onToggleCompleted: () -> Unit,
    onReschedule: () -> Unit,
    onDelete: () -> Unit
) {
    val typeBadgeColor = when {
        event.isExam -> MaterialTheme.colorScheme.error
        event.isAssignment -> MaterialTheme.colorScheme.primary
        event.isProject -> Primary
        event.isLab -> InfoBlue
        event.isHoliday -> AmberAlert
        else -> MaterialTheme.colorScheme.secondary
    }

    val typeLabel = when {
        event.isExam -> "Exam 📄"
        event.isAssignment -> "Assignment 📝"
        event.isProject -> "Project 💻"
        event.isLab -> "Lab 🧪"
        event.isHoliday -> "Holiday 🏖️"
        else -> event.eventType
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calendar_event_card_${event.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (event.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            1.dp,
            if (event.isCompleted) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(if (event.isCompleted) 0.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Checkbox, Title & Type Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = event.isCompleted,
                    onCheckedChange = { onToggleCompleted() },
                    modifier = Modifier.testTag("event_checkbox_${event.id}")
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = typeBadgeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = typeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = typeBadgeColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (!event.isCompleted) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (event.daysRemaining <= 3) MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = event.urgencyLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (event.daysRemaining <= 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (event.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (event.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "${event.courseCode} • ${event.courseName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details Row: Date, Time, Room & Weightage
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 44.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.date,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (event.locationOrRoom.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.locationOrRoom,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (event.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 44.dp)
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 44.dp, top = 6.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onReschedule,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("reschedule_event_${event.id}")
                ) {
                    Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reschedule", style = MaterialTheme.typography.labelSmall)
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_event_${event.id}")
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Empty State for the academic calendar when no deadlines match filters.
 */
@Composable
fun AcademicCalendarEmptyState(
    isFiltered: Boolean,
    onResetFilters: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.EventAvailable,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isFiltered) "No matching deadlines found" else "No deadlines scheduled",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isFiltered) "Try adjusting your search or category filter" else "Tap '+ Add Deadline' to schedule your first exam or assignment.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (isFiltered) {
                Spacer(modifier = Modifier.height(12.dp))
                FilledTonalButton(onClick = onResetFilters) {
                    Text("Reset Filters")
                }
            }
        }
    }
}

/**
 * Dialog to add a new academic calendar event or deadline to Room database.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAcademicCalendarEventDialog(
    onDismiss: () -> Unit,
    onAdd: (
        title: String,
        courseCode: String,
        courseName: String,
        eventType: String,
        date: String,
        time: String,
        timestamp: Long,
        location: String,
        weightage: String,
        description: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("CS-302") }
    var courseName by remember { mutableStateOf("Database Management Systems") }
    var eventType by remember { mutableStateOf("EXAM") }
    var date by remember { mutableStateOf("Nov 15, 2026") }
    var time by remember { mutableStateOf("10:00 AM - 12:00 PM") }
    var timestamp by remember { mutableStateOf(System.currentTimeMillis() + (17 * 86_400_000L)) }
    var location by remember { mutableStateOf("LH-302") }
    var weightage by remember { mutableStateOf("25% Mid-Term") }
    var description by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val courseOptions = listOf(
        "CS-302" to "Database Management Systems",
        "CS-304" to "Operating Systems",
        "CS-306" to "Computer Networks",
        "CS-308" to "Software Engineering",
        "CS-312" to "Machine Learning",
        "CAMPUS" to "University Holiday / Event"
    )

    val eventTypes = listOf(
        "EXAM" to "Exam 📄",
        "ASSIGNMENT" to "Assignment 📝",
        "PROJECT" to "Project 💻",
        "LAB" to "Lab Practicum 🧪",
        "HOLIDAY" to "Holiday 🏖️"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Academic Deadline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Examination") },
                    placeholder = { Text("e.g. Mid-Term Exam, Assignment 3") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_event_title_input")
                )

                // Event Type Selection
                Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(eventTypes) { (typeKey, label) ->
                        val isSelected = eventType == typeKey
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                eventType = typeKey
                                weightage = when (typeKey) {
                                    "EXAM" -> "25% Mid-Term"
                                    "ASSIGNMENT" -> "10% Internal"
                                    "PROJECT" -> "20% Capstone"
                                    "LAB" -> "20% Lab Exam"
                                    "HOLIDAY" -> "Statutory Break"
                                    else -> "Graded"
                                }
                            },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                // Course Code Picker
                Text("Associated Course", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(courseOptions) { (code, name) ->
                        val isSelected = courseCode == code
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                courseCode = code
                                courseName = name
                            },
                            label = { Text(code, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                // Date Picker trigger button
                OutlinedCard(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth().testTag("pick_deadline_date_btn")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Deadline Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(date, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                        TextButton(onClick = { showDatePicker = true }) {
                            Text("Change")
                        }
                    }
                }

                // Time & Location Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time Slot") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Room / Link") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Weightage & Description
                OutlinedTextField(
                    value = weightage,
                    onValueChange = { weightage = it },
                    label = { Text("Grade Weightage") },
                    placeholder = { Text("e.g. 25% Mid-Term") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Syllabus / Notes (Optional)") },
                    placeholder = { Text("Units covered, guidelines, format") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(
                            title.trim(),
                            courseCode,
                            courseName,
                            eventType,
                            date,
                            time,
                            timestamp,
                            location.trim(),
                            weightage.trim(),
                            description.trim()
                        )
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_calendar_event_btn")
            ) {
                Text("Save to Calendar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_calendar_event_btn")
            ) {
                Text("Cancel")
            }
        }
    )

    if (showDatePicker) {
        TaskDatePickerDialog(
            initialSelectedDateMillis = timestamp,
            onDateSelected = { selectedMillis, formattedDate ->
                timestamp = selectedMillis
                date = formattedDate
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}
