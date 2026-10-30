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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.model.ScheduleSlot
import com.example.model.ScheduleSlotType
import com.example.ui.theme.*

/**
 * Material 3 Course Schedule Manager component.
 * Allows students to input and manage their daily class schedule with room locations and times,
 * including a rich reminder feature with Android push notification integration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScheduleManagerComponent(
    schedules: List<ScheduleSlot>,
    onAddSchedule: (ScheduleSlot) -> Unit,
    onDeleteSchedule: (String) -> Unit,
    onToggleReminder: (slotId: String, enabled: Boolean, minutesBefore: Int) -> Unit,
    onTestReminder: (ScheduleSlot) -> Unit,
    onToggleAttendedToday: (slotId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDay by remember { mutableStateOf(DayOfWeekEnum.THU) } // Default Thursday
    var filterType by remember { mutableStateOf<ScheduleSlotType?>(null) } // null = All
    var showAddDialog by remember { mutableStateOf(false) }
    var slotToDelete by remember { mutableStateOf<ScheduleSlot?>(null) }
    var slotToEditReminder by remember { mutableStateOf<ScheduleSlot?>(null) }

    // Slots for the selected day
    val daySlots = remember(schedules, selectedDay, filterType) {
        schedules.filter { it.occursOn(selectedDay) }
            .filter { filterType == null || it.slotType == filterType }
    }

    // Counts for selected day
    val totalLecturesToday = remember(schedules, selectedDay) {
        schedules.count { it.occursOn(selectedDay) && it.slotType == ScheduleSlotType.LECTURE }
    }
    val totalLabsToday = remember(schedules, selectedDay) {
        schedules.count { it.occursOn(selectedDay) && it.slotType == ScheduleSlotType.LAB }
    }
    val remindersActiveCount = remember(schedules, selectedDay) {
        schedules.count { it.occursOn(selectedDay) && it.reminderEnabled }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("course_schedule_manager_component")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title and "+ Input Class" Button
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
                            text = "Course Schedule Manager",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Daily classes, room locations & reminders",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(100),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_input_class_schedule")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Class", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            // Quick Day Metrics Summary
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${selectedDay.fullDayName} Schedule",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$totalLecturesToday Lectures • $totalLabsToday Labs • $remindersActiveCount Reminders Active",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(100),
                        color = Primary
                    ) {
                        Text(
                            text = "${daySlots.size} Classes",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Day of Week Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DayOfWeekEnum.values().forEach { day ->
                    val isDayActive = selectedDay == day
                    val dayClassCount = schedules.count { it.occursOn(day) }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDayActive) Primary else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(
                            1.dp,
                            if (isDayActive) Primary else OutlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedDay = day }
                            .testTag("schedule_day_btn_${day.shortCode.lowercase()}")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = day.shortCode,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDayActive) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$dayClassCount",
                                fontSize = 10.sp,
                                color = if (isDayActive) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Session Type Filter Chips (All, Lectures, Labs, Seminars)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    FilterChip(
                        selected = filterType == null,
                        onClick = { filterType = null },
                        label = { Text("All (${schedules.count { it.occursOn(selectedDay) }})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("filter_slot_all")
                    )
                }

                items(ScheduleSlotType.values()) { type ->
                    val count = schedules.count { it.occursOn(selectedDay) && it.slotType == type }
                    if (count > 0 || filterType == type) {
                        FilterChip(
                            selected = filterType == type,
                            onClick = { filterType = if (filterType == type) null else type },
                            label = { Text("${type.iconEmoji} ${type.shortName} ($count)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Tertiary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_slot_${type.name.lowercase()}")
                        )
                    }
                }
            }

            // Daily Class Slots List
            if (daySlots.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "No classes scheduled on ${selectedDay.fullDayName}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap '+ Add Class' above to input lectures or labs with room locations and reminders.",
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
                        CourseScheduleSlotCard(
                            slot = slot,
                            index = index,
                            onToggleAttended = { onToggleAttendedToday(slot.id) },
                            onToggleReminder = { enabled, mins -> onToggleReminder(slot.id, enabled, mins) },
                            onTestReminder = { onTestReminder(slot) },
                            onEditReminder = { slotToEditReminder = slot },
                            onDelete = { slotToDelete = slot }
                        )
                    }
                }
            }
        }
    }

    // Input New Class Schedule Dialog
    if (showAddDialog) {
        InputClassScheduleDialog(
            defaultDay = selectedDay,
            onDismiss = { showAddDialog = false },
            onAdd = { newSlot ->
                onAddSchedule(newSlot)
                showAddDialog = false
            }
        )
    }

    // Delete Schedule Confirmation Dialog
    if (slotToDelete != null) {
        AlertDialog(
            onDismissRequest = { slotToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Class Schedule?") },
            text = { Text("Remove ${slotToDelete?.courseCode} (${slotToDelete?.courseName}) at ${slotToDelete?.room} from your timetable?") },
            confirmButton = {
                Button(
                    onClick = {
                        slotToDelete?.id?.let { onDeleteSchedule(it) }
                        slotToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_schedule_btn")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { slotToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Reminder Settings Dialog
    if (slotToEditReminder != null) {
        val slot = slotToEditReminder!!
        var reminderEnabled by remember { mutableStateOf(slot.reminderEnabled) }
        var reminderMinutes by remember { mutableIntStateOf(slot.reminderMinutesBefore) }

        AlertDialog(
            onDismissRequest = { slotToEditReminder = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Class Reminder Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "${slot.courseCode}: ${slot.courseName}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Location: ${slot.room} • Starts at ${slot.timeStart}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Notification Alert", fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { reminderEnabled = it },
                            modifier = Modifier.testTag("switch_reminder_dialog")
                        )
                    }

                    if (reminderEnabled) {
                        Text("Alert timing before class:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(10, 15, 30).forEach { mins ->
                                val isSelected = reminderMinutes == mins
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) Primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { reminderMinutes = mins }
                                ) {
                                    Text(
                                        text = "$mins mins",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onToggleReminder(slot.id, reminderEnabled, reminderMinutes)
                        slotToEditReminder = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier.testTag("save_reminder_settings_btn")
                ) {
                    Text("Save Reminder")
                }
            },
            dismissButton = {
                TextButton(onClick = { slotToEditReminder = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Single Course Schedule Slot Card with room location badge, timing, instructor,
 * reminder toggle & test trigger, and attendance marker.
 */
@Composable
fun CourseScheduleSlotCard(
    slot: ScheduleSlot,
    index: Int,
    onToggleAttended: () -> Unit,
    onToggleReminder: (enabled: Boolean, minutes: Int) -> Unit,
    onTestReminder: () -> Unit,
    onEditReminder: () -> Unit,
    onDelete: () -> Unit
) {
    val isLab = slot.slotType == ScheduleSlotType.LAB
    val typeColor = when (slot.slotType) {
        ScheduleSlotType.LAB -> Tertiary
        ScheduleSlotType.LECTURE -> Primary
        ScheduleSlotType.SEMINAR -> Warning
        ScheduleSlotType.TUTORIAL -> Color(0xFF0284C7)
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("course_schedule_slot_card_$index")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Time, Type Badge, Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time Duration Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = typeColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = typeColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = slot.durationText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = typeColor,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Type Badge
                    Surface(
                        shape = RoundedCornerShape(100),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = "${slot.slotType.iconEmoji} ${slot.slotType.shortName}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("delete_schedule_slot_$index")
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

            Spacer(modifier = Modifier.height(6.dp))

            // Course Code & Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = slot.courseCode,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = typeColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = slot.courseName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Prominent Room Location & Instructor Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Room Location Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.testTag("room_badge_$index")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isLab) Icons.Default.Science else Icons.Default.Room,
                            contentDescription = "Room Location",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = slot.room,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 11.sp
                        )
                    }
                }

                // Instructor
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = slot.faculty,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Actions: Reminder Feature Controls & Attendance Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reminder Feature: Bell chip with test action
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(100),
                        color = if (slot.reminderEnabled) PrimaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(
                            1.dp,
                            if (slot.reminderEnabled) Primary.copy(alpha = 0.5f) else OutlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .clickable { onEditReminder() }
                            .testTag("schedule_reminder_chip_$index")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (slot.reminderEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                contentDescription = "Reminder",
                                tint = if (slot.reminderEnabled) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (slot.reminderEnabled) "${slot.reminderMinutesBefore}m alert" else "Reminder Off",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (slot.reminderEnabled) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }

                    if (slot.reminderEnabled) {
                        Spacer(modifier = Modifier.width(4.dp))
                        TextButton(
                            onClick = onTestReminder,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(26.dp)
                                .testTag("test_reminder_btn_$index")
                        ) {
                            Text("Test Alert", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Attendance Status Toggle
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (slot.isAttendedToday) TertiaryContainer else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        if (slot.isAttendedToday) Tertiary else OutlineVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .clickable { onToggleAttended() }
                        .testTag("schedule_attendance_btn_$index")
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

/**
 * Dialog enabling students to input their daily class schedule with room locations and times,
 * including a reminder feature.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputClassScheduleDialog(
    defaultDay: DayOfWeekEnum,
    onDismiss: () -> Unit,
    onAdd: (ScheduleSlot) -> Unit
) {
    var courseCode by remember { mutableStateOf("CS-314") }
    var courseName by remember { mutableStateOf("Cloud Systems & DevOps Practicum") }
    var slotType by remember { mutableStateOf(ScheduleSlotType.LAB) }
    var selectedDays by remember { mutableStateOf(setOf(defaultDay)) }
    var timeStart by remember { mutableStateOf("02:00 PM") }
    var timeEnd by remember { mutableStateOf("04:30 PM") }
    var room by remember { mutableStateOf("Cloud Systems Lab (Room 302)") }
    var faculty by remember { mutableStateOf("Prof. Amit Sharma") }
    var notes by remember { mutableStateOf("Kubernetes clusters & CI/CD pipeline") }

    // Reminder feature controls
    var reminderEnabled by remember { mutableStateOf(true) }
    var reminderMinutesBefore by remember { mutableIntStateOf(15) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("input_class_schedule_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AddAlarm, contentDescription = null, tint = Primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Input Class Schedule",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Session Type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ScheduleSlotType.values().forEach { type ->
                        val isChosen = slotType == type
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isChosen) PrimaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, if (isChosen) Primary else OutlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { slotType = type }
                        ) {
                            Text(
                                text = "${type.iconEmoji} ${type.shortName}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isChosen) Primary else MaterialTheme.colorScheme.onSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
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
                        label = { Text("Code *") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(0.35f)
                            .testTag("input_schedule_code")
                    )
                    OutlinedTextField(
                        value = courseName,
                        onValueChange = { courseName = it },
                        label = { Text("Course Title *") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(0.65f)
                            .testTag("input_schedule_name")
                    )
                }

                // Room Location (with quick suggestions)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = room,
                        onValueChange = { room = it },
                        label = { Text("Room Location * (e.g. LH-102, Lab 304)") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Room, contentDescription = null, tint = Primary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_schedule_room")
                    )

                    // Quick room chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val roomSuggestions = listOf("LH-102 (Block A)", "CS Lab 304", "Systems Lab", "LH-201 (Block B)", "Seminar Hall B")
                        items(roomSuggestions) { suggestion ->
                            Surface(
                                shape = RoundedCornerShape(100),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.clickable { room = suggestion }
                            ) {
                                Text(
                                    text = suggestion,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Times: Start Time & End Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = timeStart,
                        onValueChange = { timeStart = it },
                        label = { Text("Start Time *") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_schedule_time_start")
                    )
                    OutlinedTextField(
                        value = timeEnd,
                        onValueChange = { timeEnd = it },
                        label = { Text("End Time *") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_schedule_time_end")
                    )
                }

                // Days of Week Selection
                Text(
                    text = "Days of Week:",
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
                                .testTag("pick_day_${day.shortCode.lowercase()}")
                        ) {
                            Text(
                                text = day.shortCode,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDaySelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                // Faculty / Instructor
                OutlinedTextField(
                    value = faculty,
                    onValueChange = { faculty = it },
                    label = { Text("Faculty / Instructor") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_schedule_faculty")
                )

                // Reminder Feature Section
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Class Reminder Alert",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Switch(
                                checked = reminderEnabled,
                                onCheckedChange = { reminderEnabled = it },
                                modifier = Modifier.testTag("switch_schedule_reminder")
                            )
                        }

                        if (reminderEnabled) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Alert before class:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                listOf(10, 15, 30).forEach { mins ->
                                    val isSelected = reminderMinutesBefore == mins
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) Primary else MaterialTheme.colorScheme.surfaceContainerLowest,
                                        modifier = Modifier
                                            .clickable { reminderMinutesBefore = mins }
                                            .testTag("reminder_mins_${mins}")
                                    ) {
                                        Text(
                                            text = "$mins m",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

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
                            if (courseCode.isNotBlank() && room.isNotBlank() && selectedDays.isNotEmpty()) {
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
                                        notes = notes.trim(),
                                        reminderEnabled = reminderEnabled,
                                        reminderMinutesBefore = reminderMinutesBefore
                                    )
                                )
                            }
                        },
                        enabled = courseCode.isNotBlank() && room.isNotBlank() && selectedDays.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier.testTag("save_class_schedule_btn")
                    ) {
                        Text("Save to Schedule")
                    }
                }
            }
        }
    }
}
