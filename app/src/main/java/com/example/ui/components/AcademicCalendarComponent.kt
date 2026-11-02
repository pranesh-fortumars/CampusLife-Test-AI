package com.example.ui.components

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AcademicCalendarEventEntity
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicCalendarComponent(
    viewModel: CampusLifeViewModel,
    modifier: Modifier = Modifier
) {
    val events by viewModel.filteredAcademicEvents.collectAsState()
    val allEvents by viewModel.calendarEvents.collectAsState()
    val selectedFilter by viewModel.selectedCalendarFilter.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    // Find the closest upcoming event for the countdown hero card
    val nextUpcomingEvent = remember(allEvents) {
        allEvents.filter { it.daysRemaining >= 0 }.minByOrNull { it.daysRemaining }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("academic_calendar_component"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Header with Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Academic Calendar & Exams",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Semester milestones, exam halls & holiday reminders",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FilledTonalButton(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("btn_add_academic_event")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Date", fontSize = 12.sp)
            }
        }

        // Closest Milestone Countdown Hero Card
        nextUpcomingEvent?.let { nextEvent ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (nextEvent.type) {
                        "EXAM" -> Color(0xFFFFF1F2)
                        "HOLIDAY" -> EmeraldTertiaryContainer
                        "DEADLINE" -> WarningContainer
                        else -> IndigoPrimaryContainer
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("next_milestone_hero_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = when (nextEvent.type) {
                            "EXAM" -> Color(0xFFBE123C)
                            "HOLIDAY" -> EmeraldTertiary
                            "DEADLINE" -> WarningAmber
                            else -> IndigoPrimary
                        },
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${nextEvent.daysRemaining}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "DAYS",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when (nextEvent.type) {
                                    "EXAM" -> Color(0xFFE11D48)
                                    "HOLIDAY" -> EmeraldTertiary
                                    "DEADLINE" -> WarningAmber
                                    else -> IndigoPrimary
                                }
                            ) {
                                Text(
                                    text = "NEXT ${nextEvent.type}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = nextEvent.startDate,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = nextEvent.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "${nextEvent.locationOrVenue} • ${nextEvent.description}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleEventReminder(nextEvent) },
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("btn_hero_reminder_toggle")
                    ) {
                        Icon(
                            if (nextEvent.isReminderSet) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                            contentDescription = "Toggle reminder",
                            tint = if (nextEvent.isReminderSet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Filter Chips Row
        val filters = listOf(
            "ALL" to "All Dates",
            "EXAM" to "Exams",
            "HOLIDAY" to "Holidays & Recess",
            "SEMESTER" to "Semester Dates",
            "DEADLINE" to "Deadlines"
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(filters) { (typeKey, label) ->
                val isSelected = selectedFilter == typeKey
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setCalendarFilter(typeKey) },
                    label = { Text(label, fontSize = 12.sp) },
                    shape = RoundedCornerShape(100),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .heightIn(min = 36.dp)
                        .testTag("filter_calendar_$typeKey")
                )
            }
        }

        // List of Events
        if (events.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No events found for this category.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                events.forEach { event ->
                    AcademicCalendarEventCard(
                        event = event,
                        onToggleReminder = { viewModel.toggleEventReminder(event) },
                        onDelete = { viewModel.deleteAcademicEvent(event) }
                    )
                }
            }
        }
    }

    // Add Academic Event Dialog
    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var eventType by remember { mutableStateOf("EXAM") }
        var startDate by remember { mutableStateOf("") }
        var endDate by remember { mutableStateOf("") }
        var location by remember { mutableStateOf("Main Exam Hall") }
        var description by remember { mutableStateOf("") }
        var daysRemainingStr by remember { mutableStateOf("14") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Academic Event / Reminder") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Event Title") },
                        placeholder = { Text("e.g. Physics Midterm Exam") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Type selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("EXAM", "HOLIDAY", "SEMESTER", "DEADLINE").forEach { type ->
                            FilterChip(
                                selected = eventType == type,
                                onClick = { eventType = type },
                                label = { Text(type, fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("Start Date") },
                            placeholder = { Text("Nov 20, 2026") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = { Text("End Date (Opt)") },
                            placeholder = { Text("Nov 22") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Location/Venue") },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f)
                        )
                        OutlinedTextField(
                            value = daysRemainingStr,
                            onValueChange = { daysRemainingStr = it },
                            label = { Text("Days Left") },
                            singleLine = true,
                            modifier = Modifier.weight(0.8f)
                        )
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Notes / Description") },
                        placeholder = { Text("e.g. Chapters 1-4, bring scientific calculator") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && startDate.isNotBlank()) {
                            val days = daysRemainingStr.toIntOrNull() ?: 0
                            viewModel.addAcademicEvent(
                                title = title,
                                type = eventType,
                                startDate = startDate,
                                endDate = endDate,
                                locationOrVenue = location,
                                description = description,
                                daysRemaining = days
                            )
                            showAddDialog = false
                        }
                    },
                    enabled = title.isNotBlank() && startDate.isNotBlank()
                ) {
                    Text("Save Event")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AcademicCalendarEventCard(
    event: AcademicCalendarEventEntity,
    onToggleReminder: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("event_card_${event.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(100),
                        color = when (event.type) {
                            "EXAM" -> Color(0xFFFFF1F2)
                            "HOLIDAY" -> EmeraldTertiaryContainer
                            "DEADLINE" -> WarningContainer
                            else -> IndigoPrimaryContainer
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = when (event.type) {
                                    "EXAM" -> Icons.Default.EditNote
                                    "HOLIDAY" -> Icons.Default.BeachAccess
                                    "DEADLINE" -> Icons.Default.Timer
                                    else -> Icons.Default.CalendarToday
                                },
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = when (event.type) {
                                    "EXAM" -> Color(0xFFBE123C)
                                    "HOLIDAY" -> EmeraldTertiary
                                    "DEADLINE" -> WarningAmber
                                    else -> IndigoPrimary
                                }
                            )
                            Text(
                                text = event.type,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (event.type) {
                                    "EXAM" -> Color(0xFFBE123C)
                                    "HOLIDAY" -> EmeraldOnTertiaryContainer
                                    "DEADLINE" -> OnWarningContainer
                                    else -> IndigoOnPrimaryContainer
                                }
                            )
                        }
                    }

                    // Countdown badge
                    Surface(
                        shape = RoundedCornerShape(100),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (event.daysRemaining == 0) "Today" else "In ${event.daysRemaining} days",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Reminder button
                IconButton(
                    onClick = onToggleReminder,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("btn_reminder_toggle_${event.id}")
                ) {
                    Icon(
                        if (event.isReminderSet) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                        contentDescription = "Toggle reminder",
                        tint = if (event.isReminderSet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (event.description.isNotBlank()) {
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 0.8.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (event.endDate.isNotBlank()) "${event.startDate} - ${event.endDate}" else event.startDate,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = event.locationOrVenue,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
