package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.window.Dialog
import com.example.model.DayOfWeekEnum
import com.example.model.ScheduleMockData
import com.example.model.ScheduleSlot
import com.example.model.ScheduleSlotType
import com.example.ui.theme.*

/**
 * Material 3 Weekly Class Schedule component where students can view their daily lectures
 * and labs, with complete support for recurring weekly time slots.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyClassScheduleComponent(
    initialSlots: List<ScheduleSlot> = ScheduleMockData.defaultScheduleSlots,
    onLogAttendance: ((courseCode: String, isAttended: Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var allSlots by remember { mutableStateOf(initialSlots) }
    var selectedDay by remember { mutableStateOf(DayOfWeekEnum.THU) } // Default Thursday
    var viewMode by remember { mutableStateOf("DAY_AGENDA") } // "DAY_AGENDA" or "WEEKLY_MATRIX"
    var filterType by remember { mutableStateOf<ScheduleSlotType?>(null) } // null = All
    var showAddDialog by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    // Slots for the selected day
    val daySlots = remember(allSlots, selectedDay, filterType) {
        allSlots.filter { it.occursOn(selectedDay) }
            .filter { filterType == null || it.slotType == filterType }
    }

    // Counts for day
    val totalLecturesToday = remember(allSlots, selectedDay) {
        allSlots.count { it.occursOn(selectedDay) && it.slotType == ScheduleSlotType.LECTURE }
    }
    val totalLabsToday = remember(allSlots, selectedDay) {
        allSlots.count { it.occursOn(selectedDay) && it.slotType == ScheduleSlotType.LAB }
    }

    // Live session currently active
    val liveSlot = remember(daySlots) {
        daySlots.find { it.isLiveNow }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_class_schedule_component")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Weekly Class Schedule",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Lectures, practical labs & recurring time slots",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(100),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_recurring_slot_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Slot", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }

            // View Mode Switcher: "Daily Agenda" vs "Weekly Matrix"
            Surface(
                shape = RoundedCornerShape(100),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(100),
                        color = if (viewMode == "DAY_AGENDA") Primary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewMode = "DAY_AGENDA" }
                            .testTag("tab_day_agenda")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ViewDay,
                                contentDescription = null,
                                tint = if (viewMode == "DAY_AGENDA") Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Daily Agenda",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (viewMode == "DAY_AGENDA") Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(100),
                        color = if (viewMode == "WEEKLY_MATRIX") Primary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewMode = "WEEKLY_MATRIX" }
                            .testTag("tab_weekly_matrix")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.GridOn,
                                contentDescription = null,
                                tint = if (viewMode == "WEEKLY_MATRIX") Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Weekly Matrix (Mon–Sat)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (viewMode == "WEEKLY_MATRIX") Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // In Day Agenda View: Day Selector & Filters
            if (viewMode == "DAY_AGENDA") {
                // Day of Week Selector Row (Mon, Tue, Wed, Thu, Fri, Sat)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val dayDates = listOf(
                        DayOfWeekEnum.MON to "26",
                        DayOfWeekEnum.TUE to "27",
                        DayOfWeekEnum.WED to "28",
                        DayOfWeekEnum.THU to "29",
                        DayOfWeekEnum.FRI to "30",
                        DayOfWeekEnum.SAT to "31"
                    )

                    dayDates.forEach { (day, date) ->
                        val isSelected = day == selectedDay
                        val dayClassCount = allSlots.count { it.occursOn(day) }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PrimaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, if (isSelected) Primary else OutlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedDay = day }
                                .testTag("day_tab_${day.shortCode.lowercase()}")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = day.shortCode,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = date,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (dayClassCount > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(100),
                                        color = if (isSelected) Primary else MaterialTheme.colorScheme.surfaceContainerHigh
                                    ) {
                                        Text(
                                            text = "$dayClassCount",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Filter Chips Row (All, Lectures, Labs)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        item {
                            FilterChip(
                                selected = filterType == null,
                                onClick = { filterType = null },
                                label = { Text("All (${daySlots.size})", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("filter_all")
                            )
                        }
                        item {
                            FilterChip(
                                selected = filterType == ScheduleSlotType.LECTURE,
                                onClick = {
                                    filterType = if (filterType == ScheduleSlotType.LECTURE) null else ScheduleSlotType.LECTURE
                                },
                                label = { Text("🎓 Lectures ($totalLecturesToday)", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("filter_lectures")
                            )
                        }
                        item {
                            FilterChip(
                                selected = filterType == ScheduleSlotType.LAB,
                                onClick = {
                                    filterType = if (filterType == ScheduleSlotType.LAB) null else ScheduleSlotType.LAB
                                },
                                label = { Text("🧪 Labs ($totalLabsToday)", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Tertiary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("filter_labs")
                            )
                        }
                    }

                    Text(
                        text = selectedDay.fullDayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }

                // Active Live Session Banner (if applicable for selected day)
                if (liveSlot != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = TertiaryContainer,
                        border = BorderStroke(1.dp, Tertiary.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("live_session_banner")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Tertiary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "LIVE IN SESSION NOW",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Tertiary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surface) {
                                    Text(
                                        text = liveSlot.room,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Tertiary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${liveSlot.courseCode}: ${liveSlot.courseName}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${liveSlot.durationText} • ${liveSlot.faculty} • ${liveSlot.batchOrGroup}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        onLogAttendance?.invoke(liveSlot.courseCode, true)
                                        allSlots = allSlots.map {
                                            if (it.id == liveSlot.id) it.copy(isAttendedToday = true) else it
                                        }
                                        feedbackMessage = "Marked attendance for ${liveSlot.courseCode}!"
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Tertiary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("log_attendance_live_btn")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mark Attended", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Daily Schedule Timeline List
                if (daySlots.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.EventBusy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No classes scheduled for ${selectedDay.fullDayName}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Enjoy your free time, project work, or study sprint!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        daySlots.forEachIndexed { index, slot ->
                            ScheduleSlotCard(
                                slot = slot,
                                onToggleAttended = {
                                    val newStatus = !slot.isAttendedToday
                                    allSlots = allSlots.map {
                                        if (it.id == slot.id) it.copy(isAttendedToday = newStatus) else it
                                    }
                                    if (newStatus) {
                                        onLogAttendance?.invoke(slot.courseCode, true)
                                        feedbackMessage = "Attended ${slot.courseCode}!"
                                    }
                                },
                                onDelete = {
                                    allSlots = allSlots.filter { it.id != slot.id }
                                    feedbackMessage = "Removed recurring slot ${slot.courseCode}"
                                },
                                index = index
                            )
                        }
                    }
                }
            } else {
                // Weekly Matrix Overview
                WeeklyMatrixView(
                    allSlots = allSlots,
                    onSelectDay = { day ->
                        selectedDay = day
                        viewMode = "DAY_AGENDA"
                    }
                )
            }

            // Feedback Message Bar
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
                        Icon(Icons.Default.Info, contentDescription = null, tint = Tertiary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feedbackMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Tertiary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { feedbackMessage = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }

    // Add Recurring Slot Dialog
    if (showAddDialog) {
        AddRecurringSlotDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { newSlot ->
                allSlots = allSlots + newSlot
                showAddDialog = false
                feedbackMessage = "Added ${newSlot.slotType.shortName} for ${newSlot.courseCode} (${newSlot.recurrenceSummary})!"
            }
        )
    }
}

/**
 * Individual Card representing a lecture or lab class slot in the daily timeline.
 */
@Composable
fun ScheduleSlotCard(
    slot: ScheduleSlot,
    onToggleAttended: () -> Unit,
    onDelete: () -> Unit,
    index: Int
) {
    val isLab = slot.slotType == ScheduleSlotType.LAB
    val typeColor = when (slot.slotType) {
        ScheduleSlotType.LAB -> Tertiary
        ScheduleSlotType.LECTURE -> Primary
        ScheduleSlotType.TUTORIAL -> Secondary
        ScheduleSlotType.SEMINAR -> Primary
    }
    val typeContainer = when (slot.slotType) {
        ScheduleSlotType.LAB -> TertiaryContainer
        ScheduleSlotType.LECTURE -> PrimaryContainer
        ScheduleSlotType.TUTORIAL -> SecondaryContainer
        ScheduleSlotType.SEMINAR -> PrimaryContainer
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("schedule_slot_$index"),
        verticalAlignment = Alignment.Top
    ) {
        // Time column on the left
        Column(
            modifier = Modifier
                .width(68.dp)
                .padding(top = 4.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = slot.timeStart,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = slot.timeEnd,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = typeContainer.copy(alpha = 0.6f)
            ) {
                Text(
                    text = slot.slotType.shortName,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = typeColor,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Main slot content card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(
                1.dp,
                if (slot.isLiveNow) Tertiary else if (isLab) Tertiary.copy(alpha = 0.3f) else OutlineVariant.copy(alpha = 0.4f)
            ),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Top row: Code badge, Title & Type
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
                            color = typeContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(slot.slotType.iconEmoji, fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = slot.courseCode,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = typeColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = slot.courseName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("delete_slot_btn_$index")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete slot",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Location Room & Faculty
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isLab) Icons.Default.Science else Icons.Default.Room,
                            contentDescription = null,
                            tint = if (isLab) Tertiary else Primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = slot.room,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp
                        )
                    }

                    Text("•", color = OutlineVariant)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = slot.faculty,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                if (slot.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Topic: ${slot.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Row: Recurrence Badge & Attendance Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(100),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Repeat, contentDescription = null, tint = Primary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = slot.recurrenceSummary,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Attendance Toggle Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (slot.isAttendedToday) TertiaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (slot.isAttendedToday) Tertiary else OutlineVariant.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .clickable { onToggleAttended() }
                            .testTag("attendance_toggle_btn_$index")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (slot.isAttendedToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (slot.isAttendedToday) Tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (slot.isAttendedToday) "Attended" else "Mark Present",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (slot.isAttendedToday) Tertiary else MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Weekly Matrix View showing recurring schedules distributed across all days of the week.
 */
@Composable
fun WeeklyMatrixView(
    allSlots: List<ScheduleSlot>,
    onSelectDay: (DayOfWeekEnum) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weekly_matrix_view"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DayOfWeekEnum.values().forEach { day ->
            val daySlots = allSlots.filter { it.occursOn(day) }
            val lectureCount = daySlots.count { it.slotType == ScheduleSlotType.LECTURE }
            val labCount = daySlots.count { it.slotType == ScheduleSlotType.LAB }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectDay(day) }
                    .testTag("matrix_row_${day.shortCode.lowercase()}")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = day.fullDayName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(shape = RoundedCornerShape(100), color = PrimaryContainer) {
                                Text(
                                    text = "$lectureCount Lec • $labCount Lab",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = Primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "View Agenda ➔",
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (daySlots.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            daySlots.take(4).forEach { slot ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (slot.slotType == ScheduleSlotType.LAB) TertiaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Text(
                                        text = "${slot.courseCode} (${slot.timeStart.substringBefore(" ")})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = if (slot.slotType == ScheduleSlotType.LAB) Tertiary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (daySlots.size > 4) {
                                Text(
                                    text = "+${daySlots.size - 4}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.align(Alignment.CenterVertically)
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
 * Dialog to add a new recurring time slot (Lecture or Lab) with multi-day recurrence selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecurringSlotDialog(
    onDismiss: () -> Unit,
    onAdd: (ScheduleSlot) -> Unit
) {
    var courseCode by remember { mutableStateOf("CS-314") }
    var courseName by remember { mutableStateOf("Cloud Infrastructure Practicum") }
    var slotType by remember { mutableStateOf(ScheduleSlotType.LAB) }
    var selectedDays by remember { mutableStateOf(setOf(DayOfWeekEnum.TUE, DayOfWeekEnum.THU)) }
    var timeStart by remember { mutableStateOf("02:00 PM") }
    var timeEnd by remember { mutableStateOf("04:30 PM") }
    var room by remember { mutableStateOf("Cloud Systems Lab (Room 302)") }
    var faculty by remember { mutableStateOf("Prof. Amit Sharma") }
    var notes by remember { mutableStateOf("Kubernetes & AWS Deployments") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_recurring_slot_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Recurring Class Slot",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Slot Type Segment (Lecture vs Lab)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScheduleSlotType.values().take(3).forEach { type ->
                        val isSelected = slotType == type
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) PrimaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, if (isSelected) Primary else OutlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { slotType = type }
                                .testTag("type_pick_${type.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(type.iconEmoji, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = type.shortName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Course Code & Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = courseCode,
                        onValueChange = { courseCode = it },
                        label = { Text("Code") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(0.4f)
                            .testTag("input_course_code")
                    )
                    OutlinedTextField(
                        value = courseName,
                        onValueChange = { courseName = it },
                        label = { Text("Course Title") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(0.6f)
                            .testTag("input_course_name")
                    )
                }

                // Recurring Days Selector
                Text(
                    text = "Recurring Days of Week:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DayOfWeekEnum.values().forEach { day ->
                        val isDaySelected = selectedDays.contains(day)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDaySelected) Primary else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, if (isDaySelected) Primary else OutlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedDays = if (isDaySelected) {
                                        if (selectedDays.size > 1) selectedDays - day else selectedDays
                                    } else {
                                        selectedDays + day
                                    }
                                }
                                .testTag("day_pick_${day.shortCode.lowercase()}")
                        ) {
                            Text(
                                text = day.shortCode,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDaySelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                // Time Start & End
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = timeStart,
                        onValueChange = { timeStart = it },
                        label = { Text("Start Time") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_time_start")
                    )
                    OutlinedTextField(
                        value = timeEnd,
                        onValueChange = { timeEnd = it },
                        label = { Text("End Time") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_time_end")
                    )
                }

                // Room / Venue & Faculty
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Room / Lab Venue") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_room")
                )

                OutlinedTextField(
                    value = faculty,
                    onValueChange = { faculty = it },
                    label = { Text("Instructor / Faculty") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_faculty")
                )

                // Action Buttons
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
                            if (courseCode.isNotBlank() && selectedDays.isNotEmpty()) {
                                onAdd(
                                    ScheduleSlot(
                                        courseCode = courseCode.trim().uppercase(),
                                        courseName = courseName.trim(),
                                        slotType = slotType,
                                        recurringDays = selectedDays.sortedBy { it.dayNumber },
                                        timeStart = timeStart.trim(),
                                        timeEnd = timeEnd.trim(),
                                        room = room.trim(),
                                        faculty = faculty.trim(),
                                        notes = notes.trim()
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier.testTag("save_recurring_slot_btn")
                    ) {
                        Text("Add to Schedule")
                    }
                }
            }
        }
    }
}
