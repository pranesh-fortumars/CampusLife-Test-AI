package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ApplicationStatus
import com.example.model.CareerApplication
import com.example.model.CareerItemType
import com.example.ui.theme.*

/**
 * Kanban-style board component to manage job applications, internships, and networking milestones,
 * allowing users to seamlessly move cards between 'Applied', 'Interviewing', and 'Offer' statuses.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerKanbanBoardComponent(
    applications: List<CareerApplication>,
    onMoveStatus: (applicationId: String, newStatus: ApplicationStatus) -> Unit,
    onAddApplication: (CareerApplication) -> Unit,
    onDeleteApplication: (applicationId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedColumnFilter by remember { mutableStateOf("ALL") } // "ALL", "APPLIED", "INTERVIEWING", "OFFER"
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTypeFilter by remember { mutableStateOf("ALL") } // "ALL", "INTERNSHIP", "FULL_TIME", "NETWORKING"
    var inspectApplication by remember { mutableStateOf<CareerApplication?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Filter by type and search query
    val filteredApplications = remember(applications, selectedTypeFilter, searchQuery) {
        applications.filter { app ->
            val matchesType = when (selectedTypeFilter) {
                "ALL" -> true
                "INTERNSHIP" -> app.type == CareerItemType.INTERNSHIP
                "FULL_TIME" -> app.type == CareerItemType.FULL_TIME
                "NETWORKING" -> app.type == CareerItemType.NETWORKING
                else -> true
            }
            val matchesQuery = if (searchQuery.isBlank()) true else {
                app.companyName.contains(searchQuery, ignoreCase = true) ||
                        app.roleTitle.contains(searchQuery, ignoreCase = true) ||
                        app.location.contains(searchQuery, ignoreCase = true) ||
                        app.tags.any { it.contains(searchQuery, ignoreCase = true) }
            }
            matchesType && matchesQuery
        }
    }

    val appliedList = remember(filteredApplications) {
        filteredApplications.filter { it.status == ApplicationStatus.APPLIED }
    }
    val interviewingList = remember(filteredApplications) {
        filteredApplications.filter { it.status == ApplicationStatus.INTERVIEWING }
    }
    val offerList = remember(filteredApplications) {
        filteredApplications.filter { it.status == ApplicationStatus.OFFER }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("career_kanban_board_component"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Kanban Pipeline Metrics Header Card
        KanbanMetricsHeaderCard(
            totalApplied = appliedList.size,
            totalInterviewing = interviewingList.size,
            totalOffers = offerList.size,
            onAddClick = { showAddDialog = true }
        )

        // 2. Search & Category Filters Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search company or role...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("kanban_search_input")
            )

            // Quick Add Icon Button for fast action
            FilledTonalButton(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier
                    .height(50.dp)
                    .testTag("kanban_add_top_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Card", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Category Type Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val typeFilters = listOf(
                "ALL" to "All Streams",
                "INTERNSHIP" to "Internships 💼",
                "FULL_TIME" to "Full-Time 🚀",
                "NETWORKING" to "Networking 🤝"
            )
            items(typeFilters) { (key, label) ->
                val isSelected = selectedTypeFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTypeFilter = key },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(100),
                    modifier = Modifier.testTag("kanban_type_filter_$key")
                )
            }
        }

        // 3. Column View Switcher Carousel (All Board, Applied, Interviewing, Offer)
        Surface(
            shape = RoundedCornerShape(100),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val tabs = listOf(
                    "ALL" to "All Columns (${filteredApplications.size})",
                    "APPLIED" to "Applied (${appliedList.size})",
                    "INTERVIEWING" to "Interview (${interviewingList.size})",
                    "OFFER" to "Offer (${offerList.size}) 🎉"
                )

                tabs.forEach { (tabKey, label) ->
                    val isSelected = selectedColumnFilter == tabKey
                    val tabColor = when (tabKey) {
                        "APPLIED" -> Primary
                        "INTERVIEWING" -> Warning
                        "OFFER" -> Success
                        else -> Primary
                    }

                    Surface(
                        shape = RoundedCornerShape(100),
                        color = if (isSelected) tabColor else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedColumnFilter = tabKey }
                            .testTag("kanban_tab_$tabKey")
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // 4. Kanban Columns Container
        when (selectedColumnFilter) {
            "ALL" -> {
                // Horizontal scrolling multi-column Kanban Board
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Column 1: APPLIED
                    KanbanColumnView(
                        title = "Applied",
                        status = ApplicationStatus.APPLIED,
                        count = appliedList.size,
                        color = Primary,
                        applications = appliedList,
                        onMoveStatus = onMoveStatus,
                        onInspect = { inspectApplication = it },
                        modifier = Modifier.width(310.dp)
                    )

                    // Column 2: INTERVIEWING
                    KanbanColumnView(
                        title = "Interviewing",
                        status = ApplicationStatus.INTERVIEWING,
                        count = interviewingList.size,
                        color = Warning,
                        applications = interviewingList,
                        onMoveStatus = onMoveStatus,
                        onInspect = { inspectApplication = it },
                        modifier = Modifier.width(310.dp)
                    )

                    // Column 3: OFFER
                    KanbanColumnView(
                        title = "Offer",
                        status = ApplicationStatus.OFFER,
                        count = offerList.size,
                        color = Success,
                        applications = offerList,
                        onMoveStatus = onMoveStatus,
                        onInspect = { inspectApplication = it },
                        modifier = Modifier.width(310.dp)
                    )
                }
            }
            "APPLIED" -> {
                KanbanColumnView(
                    title = "Applied Applications",
                    status = ApplicationStatus.APPLIED,
                    count = appliedList.size,
                    color = Primary,
                    applications = appliedList,
                    onMoveStatus = onMoveStatus,
                    onInspect = { inspectApplication = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            "INTERVIEWING" -> {
                KanbanColumnView(
                    title = "Active Interviews",
                    status = ApplicationStatus.INTERVIEWING,
                    count = interviewingList.size,
                    color = Warning,
                    applications = interviewingList,
                    onMoveStatus = onMoveStatus,
                    onInspect = { inspectApplication = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            "OFFER" -> {
                KanbanColumnView(
                    title = "Offers & Confirmed Milestones",
                    status = ApplicationStatus.OFFER,
                    count = offerList.size,
                    color = Success,
                    applications = offerList,
                    onMoveStatus = onMoveStatus,
                    onInspect = { inspectApplication = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Add Application Dialog
    if (showAddDialog) {
        AddCareerApplicationDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { newApp ->
                onAddApplication(newApp)
                showAddDialog = false
            }
        )
    }

    // Inspect & Status Move Modal Dialog
    if (inspectApplication != null) {
        CareerApplicationDetailModal(
            application = inspectApplication!!,
            onDismiss = { inspectApplication = null },
            onMoveStatus = { newStatus ->
                onMoveStatus(inspectApplication!!.id, newStatus)
                inspectApplication = null
            },
            onDelete = {
                onDeleteApplication(inspectApplication!!.id)
                inspectApplication = null
            }
        )
    }
}

/**
 * Header Card summarizing the student's placement & internship pipeline counts.
 */
@Composable
fun KanbanMetricsHeaderCard(
    totalApplied: Int,
    totalInterviewing: Int,
    totalOffers: Int,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("kanban_metrics_header_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CAREER & INTERNSHIP BOARD",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Placement Kanban Pipeline",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(100),
                    color = PrimaryContainer
                ) {
                    Text(
                        text = "${totalApplied + totalInterviewing + totalOffers} Tracked",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OnPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Kanban Stat Pillars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                KanbanStatPillar(
                    count = totalApplied.toString(),
                    label = "Applied",
                    color = Primary
                )
                HorizontalDivider(modifier = Modifier.height(30.dp).width(1.dp))
                KanbanStatPillar(
                    count = totalInterviewing.toString(),
                    label = "Interviewing",
                    color = Warning
                )
                HorizontalDivider(modifier = Modifier.height(30.dp).width(1.dp))
                KanbanStatPillar(
                    count = totalOffers.toString(),
                    label = "Offers 🎉",
                    color = Success
                )
            }
        }
    }
}

@Composable
fun KanbanStatPillar(count: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
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
 * A distinct Kanban Column displaying all cards within a given status.
 */
@Composable
fun KanbanColumnView(
    title: String,
    status: ApplicationStatus,
    count: Int,
    color: Color,
    applications: List<CareerApplication>,
    onMoveStatus: (applicationId: String, newStatus: ApplicationStatus) -> Unit,
    onInspect: (CareerApplication) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.testTag("kanban_column_${status.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Column Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(100),
                    color = color.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Cards in this column
            if (applications.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp, horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No cards in ${status.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                applications.forEach { app ->
                    CareerApplicationCard(
                        application = app,
                        onMoveStatus = onMoveStatus,
                        onClick = { onInspect(app) }
                    )
                }
            }
        }
    }
}

/**
 * Individual Kanban Card representing a Job, Internship, or Networking application.
 * Features single-tap status transition actions to move cards between columns.
 */
@Composable
fun CareerApplicationCard(
    application: CareerApplication,
    onMoveStatus: (applicationId: String, newStatus: ApplicationStatus) -> Unit,
    onClick: () -> Unit
) {
    val statusColor = when (application.status) {
        ApplicationStatus.APPLIED -> Primary
        ApplicationStatus.INTERVIEWING -> Warning
        ApplicationStatus.OFFER -> Success
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("kanban_card_${application.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Company + Category Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Company Initial Avatar
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = application.companyName.take(2).uppercase(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusColor
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = application.companyName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = application.roleTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Text(
                        text = "${application.type.iconEmoji} ${application.type.label}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata: Compensation & Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = TertiaryContainer
                ) {
                    Text(
                        text = application.compensation,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OnTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = application.location,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Milestone / Interview Stage Card
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (application.status) {
                            ApplicationStatus.APPLIED -> Icons.Default.Send
                            ApplicationStatus.INTERVIEWING -> Icons.Default.Schedule
                            ApplicationStatus.OFFER -> Icons.Default.Stars
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = statusColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = application.nextMilestone,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${application.interviewRound} • ${application.nextMilestoneDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Tags row
            if (application.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    application.tags.take(3).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Single-Tap Kanban Stage Transition Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Backward Move button
                if (application.canMoveBackward && application.prevStatus != null) {
                    FilledTonalButton(
                        onClick = { onMoveStatus(application.id, application.prevStatus!!) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("move_back_${application.id}")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = application.prevStatus!!.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(10.dp))
                }

                // Forward Move button
                if (application.canMoveForward && application.nextStatus != null) {
                    Button(
                        onClick = { onMoveStatus(application.id, application.nextStatus!!) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (application.nextStatus!!) {
                                ApplicationStatus.INTERVIEWING -> Warning
                                ApplicationStatus.OFFER -> Success
                                else -> Primary
                            }
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("move_forward_${application.id}")
                    ) {
                        Text(
                            text = "To ${application.nextStatus!!.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(13.dp))
                    }
                } else if (application.status == ApplicationStatus.OFFER) {
                    Surface(
                        shape = RoundedCornerShape(100),
                        color = Success.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Celebration, contentDescription = null, tint = Success, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Offer Secured",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Success
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Detailed modal inspecting application, with 3-status transition switcher and notes.
 */
@Composable
fun CareerApplicationDetailModal(
    application: CareerApplication,
    onDismiss: () -> Unit,
    onMoveStatus: (ApplicationStatus) -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = application.companyName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = application.roleTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_app_modal_btn")) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Application", tint = MaterialTheme.colorScheme.error)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Change Kanban Status Segmented Control
                Text(
                    text = "Current Kanban Stage",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ApplicationStatus.values().forEach { st ->
                        val isSelected = application.status == st
                        val btnColor = when (st) {
                            ApplicationStatus.APPLIED -> Primary
                            ApplicationStatus.INTERVIEWING -> Warning
                            ApplicationStatus.OFFER -> Success
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) btnColor else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onMoveStatus(st) }
                                .testTag("modal_status_btn_${st.name.lowercase()}")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = st.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Detailed Key-Value pairs
                DetailInfoRow("Category", "${application.type.iconEmoji} ${application.type.label}")
                DetailInfoRow("Package / Stipend", application.compensation)
                DetailInfoRow("Location", application.location)
                DetailInfoRow("Applied Date", application.appliedDate)
                DetailInfoRow("Interview Round", application.interviewRound)
                DetailInfoRow("Next Milestone", "${application.nextMilestone} (${application.nextMilestoneDate})")
                DetailInfoRow("Referral Source", application.referralContact)

                if (application.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Prep Notes & Checklist", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = application.notes,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.testTag("close_app_detail_btn")) {
                Text("Done")
            }
        }
    )
}

@Composable
fun DetailInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

/**
 * Dialog to add a new Job Application, Internship, or Networking milestone card to the Kanban board.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCareerApplicationDialog(
    onDismiss: () -> Unit,
    onAdd: (CareerApplication) -> Unit
) {
    var companyName by remember { mutableStateOf("") }
    var roleTitle by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(CareerItemType.INTERNSHIP) }
    var status by remember { mutableStateOf(ApplicationStatus.APPLIED) }
    var location by remember { mutableStateOf("Bangalore, India") }
    var compensation by remember { mutableStateOf("₹1.25L / mo") }
    var interviewRound by remember { mutableStateOf("Round 1: Screening") }
    var nextMilestone by remember { mutableStateOf("Online Coding Assessment") }
    var nextMilestoneDate by remember { mutableStateOf("Nov 10") }
    var referralContact by remember { mutableStateOf("Alumni Network") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add to Kanban Pipeline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Company Name
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Company / Organization") },
                    placeholder = { Text("e.g. Google, Amazon, Microsoft") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_app_company_input")
                )

                // Role Title
                OutlinedTextField(
                    value = roleTitle,
                    onValueChange = { roleTitle = it },
                    label = { Text("Role Title") },
                    placeholder = { Text("e.g. SDE Intern, Backend Engineer") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_app_role_input")
                )

                // Category Type Chips
                Text("Category", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(CareerItemType.values()) { t ->
                        val isSelected = type == t
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                type = t
                                compensation = when (t) {
                                    CareerItemType.INTERNSHIP -> "₹1.25L / mo"
                                    CareerItemType.FULL_TIME -> "₹42 LPA"
                                    CareerItemType.NETWORKING -> "Referral Milestone"
                                }
                            },
                            label = { Text("${t.iconEmoji} ${t.label}", style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                // Initial Status
                Text("Initial Kanban Column", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ApplicationStatus.values().forEach { st ->
                        val isSelected = status == st
                        FilterChip(
                            selected = isSelected,
                            onClick = { status = st },
                            label = { Text(st.displayName, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (st) {
                                    ApplicationStatus.APPLIED -> Primary
                                    ApplicationStatus.INTERVIEWING -> Warning
                                    ApplicationStatus.OFFER -> Success
                                },
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Location & Compensation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = compensation,
                        onValueChange = { compensation = it },
                        label = { Text("CTC / Stipend") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Next milestone & Target date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = nextMilestone,
                        onValueChange = { nextMilestone = it },
                        label = { Text("Next Milestone") },
                        singleLine = true,
                        modifier = Modifier.weight(1.3f)
                    )
                    OutlinedTextField(
                        value = nextMilestoneDate,
                        onValueChange = { nextMilestoneDate = it },
                        label = { Text("Date") },
                        singleLine = true,
                        modifier = Modifier.weight(0.7f)
                    )
                }

                // Notes / Referral
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Referral Contact & Prep Notes") },
                    placeholder = { Text("e.g. Referred by alum, focus on concurrency") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (companyName.isNotBlank() && roleTitle.isNotBlank()) {
                        val newApp = CareerApplication(
                            companyName = companyName.trim(),
                            roleTitle = roleTitle.trim(),
                            type = type,
                            status = status,
                            location = location.trim(),
                            compensation = compensation.trim(),
                            nextMilestone = nextMilestone.trim(),
                            nextMilestoneDate = nextMilestoneDate.trim(),
                            interviewRound = interviewRound.trim(),
                            referralContact = referralContact.trim(),
                            notes = notes.trim(),
                            tags = listOf(
                                when (type) {
                                    CareerItemType.INTERNSHIP -> "Internship"
                                    CareerItemType.FULL_TIME -> "Full-Time"
                                    CareerItemType.NETWORKING -> "Networking"
                                },
                                "CampusLife"
                            )
                        )
                        onAdd(newApp)
                    }
                },
                enabled = companyName.isNotBlank() && roleTitle.isNotBlank(),
                modifier = Modifier.testTag("save_new_app_btn")
            ) {
                Text("Add Card to Board")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
