package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.CareerEventEntity
import com.example.ui.components.CareerKanbanBoardComponent
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel

/**
 * Career Tracker Screen featuring:
 * 1. Kanban Board to manage application stages (Applied, Interviewing, Offer).
 * 2. Room Database driven Upcoming Career Events & Internship Deadlines with notification triggers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerTrackerScreen(
    viewModel: CampusLifeViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Kanban, 1: Events & Deadlines
    val applications by viewModel.careerApplications.collectAsState()
    val careerEvents by viewModel.careerEvents.collectAsState()

    var showAddEventDialog by remember { mutableStateOf(false) }
    var selectedEventFilter by remember { mutableStateOf("ALL") }

    val filteredEvents = remember(careerEvents, selectedEventFilter) {
        if (selectedEventFilter == "ALL") careerEvents
        else careerEvents.filter { it.eventType.equals(selectedEventFilter, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (selectedTab == 0) "Career & Internship Kanban" else "Career Events & Deadlines",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (selectedTab == 0) "Applied, Interviewing, and Offer milestones" else "Upcoming internship deadlines & notification triggers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("career_tracker_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (selectedTab == 1) {
                        IconButton(
                            onClick = { showAddEventDialog = true },
                            modifier = Modifier.testTag("add_career_event_icon_btn")
                        ) {
                            Icon(Icons.Default.AddAlert, contentDescription = "Add Deadline", tint = Primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                ExtendedFloatingActionButton(
                    onClick = { showAddEventDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add Deadline", fontWeight = FontWeight.SemiBold) },
                    containerColor = Primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_career_event_fab")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("career_tracker_screen")
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.ViewKanban, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Kanban Board (${applications.size})")
                        }
                    },
                    modifier = Modifier.testTag("career_tab_kanban")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Deadlines 🔔 (${careerEvents.size})")
                        }
                    },
                    modifier = Modifier.testTag("career_tab_deadlines")
                )
            }

            if (selectedTab == 0) {
                // Kanban Board
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 40.dp)
                ) {
                    item {
                        CareerKanbanBoardComponent(
                            applications = applications,
                            onMoveStatus = { id, newStatus ->
                                viewModel.moveCareerApplicationStatus(id, newStatus)
                            },
                            onAddApplication = { newApp ->
                                viewModel.addCareerApplication(newApp)
                            },
                            onDeleteApplication = { id ->
                                viewModel.deleteCareerApplication(id)
                            }
                        )
                    }
                }
            } else {
                // Room Career Events & Internship Deadlines
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header Banner
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("career_deadlines_banner"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Internship & Drive Alerts",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Notifications trigger via High-Importance Career Channel. Stored locally in Room.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.AlarmOn,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }

                    // Filter chips row
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedEventFilter == "ALL",
                                    onClick = { selectedEventFilter = "ALL" },
                                    label = { Text("All Opportunities") },
                                    modifier = Modifier.testTag("filter_career_all")
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedEventFilter == "INTERNSHIP_DEADLINE",
                                    onClick = { selectedEventFilter = "INTERNSHIP_DEADLINE" },
                                    label = { Text("Internship Deadlines") },
                                    modifier = Modifier.testTag("filter_career_internships")
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedEventFilter == "CAREER_TALK",
                                    onClick = { selectedEventFilter = "CAREER_TALK" },
                                    label = { Text("Campus Talks") },
                                    modifier = Modifier.testTag("filter_career_talks")
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedEventFilter == "ONLINE_ASSESSMENT",
                                    onClick = { selectedEventFilter = "ONLINE_ASSESSMENT" },
                                    label = { Text("Online Assessments") },
                                    modifier = Modifier.testTag("filter_career_oa")
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedEventFilter == "PLACEMENT_DRIVE",
                                    onClick = { selectedEventFilter = "PLACEMENT_DRIVE" },
                                    label = { Text("Placement Drives") },
                                    modifier = Modifier.testTag("filter_career_drives")
                                )
                            }
                        }
                    }

                    // Events List
                    items(filteredEvents, key = { it.id }) { event ->
                        CareerEventCard(
                            event = event,
                            onToggleReminder = {
                                viewModel.toggleCareerReminder(event.id, context)
                            },
                            onTriggerAlert = {
                                viewModel.triggerCareerDeadlineNotification(event, context)
                            },
                            onStatusChange = { newStatus ->
                                viewModel.updateCareerEventStatus(event.id, newStatus)
                            },
                            onDelete = {
                                viewModel.deleteCareerEvent(event.id)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddEventDialog) {
        AddCareerEventDialog(
            onDismiss = { showAddEventDialog = false },
            onSubmit = { title, company, role, type, deadlineDate, stipend, eligibility, location, reminder ->
                viewModel.addCareerEvent(
                    title = title,
                    company = company,
                    role = role,
                    eventType = type,
                    deadlineDate = deadlineDate,
                    stipendOrPackage = stipend,
                    eligibility = eligibility,
                    locationOrUrl = location,
                    reminderEnabled = reminder,
                    context = context
                )
                showAddEventDialog = false
            }
        )
    }
}

/**
 * Card for an individual Room-persisted Career Event / Internship Deadline with notification trigger.
 */
@Composable
fun CareerEventCard(
    event: CareerEventEntity,
    onToggleReminder: () -> Unit,
    onTriggerAlert: () -> Unit,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    val urgency = event.getUrgencyBadge()
    val isUrgent = urgency.contains("Today") || urgency.contains("Tomorrow") || urgency.contains("days")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("career_event_card_${event.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Type chip, Urgency badge, Delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = when (event.eventType) {
                        "INTERNSHIP_DEADLINE" -> Primary.copy(alpha = 0.12f)
                        "ONLINE_ASSESSMENT" -> Warning.copy(alpha = 0.15f)
                        "PLACEMENT_DRIVE" -> SuccessGreen.copy(alpha = 0.15f)
                        else -> Secondary.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = event.displayType,
                        color = when (event.eventType) {
                            "INTERNSHIP_DEADLINE" -> Primary
                            "ONLINE_ASSESSMENT" -> Warning
                            "PLACEMENT_DRIVE" -> SuccessGreen
                            else -> Secondary
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = if (isUrgent) Warning.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = urgency,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isUrgent) Warning else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("delete_career_event_${event.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Company & Role
            Column {
                Text(
                    text = event.company,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = event.role,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
            }

            // Details metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(event.deadlineDate, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (event.stipendOrPackage.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(14.dp), tint = SuccessGreen)
                        Text(event.stipendOrPackage, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SuccessGreen)
                    }
                }
            }

            if (event.eligibility.isNotBlank()) {
                Text(
                    text = "Eligibility: ${event.eligibility}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (event.description.isNotBlank()) {
                Text(
                    text = event.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.8.dp)

            // Reminder & Action Triggers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reminder switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.clickable { onToggleReminder() }
                ) {
                    Switch(
                        checked = event.reminderEnabled,
                        onCheckedChange = { onToggleReminder() },
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("toggle_reminder_${event.id}")
                    )
                    Text(
                        text = if (event.reminderEnabled) "Alert Active" else "Reminder Off",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (event.reminderEnabled) Primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Explicit "Trigger Alert Now" action button
                FilledTonalButton(
                    onClick = onTriggerAlert,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Primary.copy(alpha = 0.12f),
                        contentColor = Primary
                    ),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("trigger_notification_btn_${event.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Trigger Notification",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Trigger Alert", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/**
 * Dialog to add a new internship deadline or career event to Room SQLite.
 */
@Composable
fun AddCareerEventDialog(
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        company: String,
        role: String,
        type: String,
        deadlineDate: String,
        stipend: String,
        eligibility: String,
        location: String,
        reminder: Boolean
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var eventType by remember { mutableStateOf("INTERNSHIP_DEADLINE") }
    var deadlineDate by remember { mutableStateOf("Nov 18, 2026") }
    var stipend by remember { mutableStateOf("₹1.20L / mo") }
    var eligibility by remember { mutableStateOf("B.Tech CSE/IT, CGPA >= 7.5") }
    var location by remember { mutableStateOf("Career Portal / Campus Auditorium") }
    var reminderEnabled by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_career_event_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Add Career Deadline",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = company,
                    onValueChange = {
                        company = it
                        if (hasError) hasError = false
                    },
                    label = { Text("Company Name *") },
                    placeholder = { Text("e.g. Adobe, Uber, Cisco") },
                    singleLine = true,
                    isError = hasError && company.isBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_event_company")
                )

                OutlinedTextField(
                    value = role,
                    onValueChange = {
                        role = it
                        if (hasError) hasError = false
                    },
                    label = { Text("Job Role / Internship Title *") },
                    placeholder = { Text("e.g. Software Engineer Intern") },
                    singleLine = true,
                    isError = hasError && role.isBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_event_role")
                )

                // Event Type Row
                Text("Opportunity Type:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val types = listOf(
                        "INTERNSHIP_DEADLINE" to "Internship",
                        "ONLINE_ASSESSMENT" to "Online Assessment",
                        "CAREER_TALK" to "Talk",
                        "PLACEMENT_DRIVE" to "Drive"
                    )
                    items(types) { (typeKey, label) ->
                        FilterChip(
                            selected = eventType == typeKey,
                            onClick = { eventType = typeKey },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = deadlineDate,
                        onValueChange = { deadlineDate = it },
                        label = { Text("Deadline Date") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_event_date")
                    )
                    OutlinedTextField(
                        value = stipend,
                        onValueChange = { stipend = it },
                        label = { Text("Stipend / Package") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = eligibility,
                    onValueChange = { eligibility = it },
                    label = { Text("Eligibility Criteria") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable Reminder Notification", fontSize = 13.sp)
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it }
                    )
                }

                if (hasError) {
                    Text("Please provide company and role.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (company.isBlank() || role.isBlank()) {
                                hasError = true
                            } else {
                                val fullTitle = if (title.isBlank()) "$company $role Application" else title
                                onSubmit(
                                    fullTitle,
                                    company.trim(),
                                    role.trim(),
                                    eventType,
                                    deadlineDate.trim(),
                                    stipend.trim(),
                                    eligibility.trim(),
                                    location.trim(),
                                    reminderEnabled
                                )
                            }
                        },
                        modifier = Modifier.testTag("submit_career_event_btn")
                    ) {
                        Text("Save Deadline")
                    }
                }
            }
        }
    }
}
