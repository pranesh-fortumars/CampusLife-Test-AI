package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityEventAggregatorComponent(
    viewModel: CampusLifeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val events by viewModel.campusEvents.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(EventCategory.ALL) }
    var selectedEventForDetails by remember { mutableStateOf<CampusEvent?>(null) }
    var showHostEventDialog by remember { mutableStateOf(false) }

    // Filter events by category and query
    val filteredEvents = remember(events, searchQuery, selectedCategory) {
        events.filter { event ->
            val matchesCategory = when (selectedCategory) {
                EventCategory.ALL -> true
                EventCategory.CLUB_MEETINGS -> event.eventType == CommunityEventType.CLUB_MEETING
                EventCategory.ACADEMIC_SEMINARS -> event.eventType == CommunityEventType.ACADEMIC_SEMINAR
                EventCategory.WORKSHOPS -> event.eventType == CommunityEventType.WORKSHOP
                EventCategory.MY_RSVPS -> event.rsvpStatus == RsvpStatus.GOING || event.rsvpStatus == RsvpStatus.INTERESTED
            }

            val query = searchQuery.trim().lowercase()
            val matchesSearch = query.isEmpty() ||
                    event.title.lowercase().contains(query) ||
                    event.organizer.lowercase().contains(query) ||
                    event.speakerOrHost.lowercase().contains(query) ||
                    event.venue.lowercase().contains(query) ||
                    event.tags.any { it.lowercase().contains(query) }

            matchesCategory && matchesSearch
        }
    }

    val totalRsvps = remember(events) {
        events.count { it.rsvpStatus == RsvpStatus.GOING || it.rsvpStatus == RsvpStatus.INTERESTED }
    }
    val seminarCount = remember(events) {
        events.count { it.eventType == CommunityEventType.ACADEMIC_SEMINAR }
    }
    val clubMeetingCount = remember(events) {
        events.count { it.eventType == CommunityEventType.CLUB_MEETING }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("event_aggregator_component")
    ) {
        // Search & Action Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search seminars, club meetings, speakers...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Primary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.testTag("clear_event_search")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    focusedBorderColor = Primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("event_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Stats & Host Button Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "⭐ $totalRsvps My RSVPs",
                            style = MaterialTheme.typography.labelMedium,
                            color = Primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF4F46E5).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "🎓 $seminarCount Seminars",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF4F46E5),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Button(
                    onClick = { showHostEventDialog = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier.testTag("btn_host_event")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Host Event", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Pills
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(EventCategory.values()) { category ->
                    val isSelected = selectedCategory == category
                    val count = when (category) {
                        EventCategory.ALL -> events.size
                        EventCategory.CLUB_MEETINGS -> clubMeetingCount
                        EventCategory.ACADEMIC_SEMINARS -> seminarCount
                        EventCategory.WORKSHOPS -> events.count { it.eventType == CommunityEventType.WORKSHOP }
                        EventCategory.MY_RSVPS -> totalRsvps
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                "${category.iconEmoji} ${category.label} ($count)",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("filter_chip_${category.name}")
                    )
                }
            }
        }

        // Events Feed List
        if (filteredEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.EventBusy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No events found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Try adjusting your search keywords." else "Check back later or propose a new meeting!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val featuredEvent = filteredEvents.firstOrNull { it.isFeatured }
                if (featuredEvent != null && searchQuery.isBlank() && selectedCategory == EventCategory.ALL) {
                    item {
                        FeaturedEventHeroCard(
                            event = featuredEvent,
                            onEventClick = { selectedEventForDetails = featuredEvent },
                            onRsvpClick = { status ->
                                viewModel.updateEventRsvp(featuredEvent.id, status, context)
                            },
                            onToggleReminder = {
                                viewModel.toggleEventReminder(featuredEvent.id, context)
                            }
                        )
                    }
                }

                items(filteredEvents) { event ->
                    EventCardItem(
                        event = event,
                        onEventClick = { selectedEventForDetails = event },
                        onRsvpClick = { status ->
                            viewModel.updateEventRsvp(event.id, status, context)
                        },
                        onToggleReminder = {
                            viewModel.toggleEventReminder(event.id, context)
                        }
                    )
                }
            }
        }
    }

    // Event Detail Dialog
    selectedEventForDetails?.let { event ->
        EventDetailDialog(
            event = event,
            onDismiss = { selectedEventForDetails = null },
            onRsvpChange = { newStatus ->
                viewModel.updateEventRsvp(event.id, newStatus, context)
                selectedEventForDetails = event.copy(rsvpStatus = newStatus)
            },
            onToggleReminder = {
                viewModel.toggleEventReminder(event.id, context)
                selectedEventForDetails = event.copy(isReminderSet = !event.isReminderSet)
            },
            onTriggerTestNotification = {
                viewModel.testTriggerEventNotification(context, event)
                Toast.makeText(context, "Notification sent for ${event.title}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Host Event Dialog
    if (showHostEventDialog) {
        HostCampusEventDialog(
            onDismiss = { showHostEventDialog = false },
            onEventCreated = { newEvent ->
                viewModel.addCampusEvent(newEvent)
                showHostEventDialog = false
                Toast.makeText(context, "Event submitted: ${newEvent.title}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

/**
 * Hero Card for the premier/featured campus event.
 */
@Composable
private fun FeaturedEventHeroCard(
    event: CampusEvent,
    onEventClick: () -> Unit,
    onRsvpClick: (RsvpStatus) -> Unit,
    onToggleReminder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onEventClick)
            .testTag("featured_event_card_${event.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(100),
                    color = Primary
                ) {
                    Text(
                        "🔥 FEATURED HIGHLIGHT",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(100),
                    color = Color(event.eventType.badgeColorHex).copy(alpha = 0.15f)
                ) {
                    Text(
                        event.eventType.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(event.eventType.badgeColorHex),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Hosted by ${event.organizer}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (event.speakerOrHost.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${event.speakerOrHost} • ${event.speakerAffiliation}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Time & Venue Row
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "${event.dateFormatted} • ${event.timeFormatted}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        "👥 ${event.attendeeCount} Going",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick RSVP actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isGoing = event.rsvpStatus == RsvpStatus.GOING
                Button(
                    onClick = { onRsvpClick(if (isGoing) RsvpStatus.NONE else RsvpStatus.GOING) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isGoing) Success else Primary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("featured_rsvp_going_${event.id}")
                ) {
                    Icon(
                        if (isGoing) Icons.Default.CheckCircle else Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isGoing) "Going ✔" else "RSVP Going")
                }

                // Reminder bell toggle
                IconButton(
                    onClick = onToggleReminder,
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (event.isReminderSet) Primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerLowest,
                            CircleShape
                        )
                        .testTag("featured_reminder_bell_${event.id}")
                ) {
                    Icon(
                        imageVector = if (event.isReminderSet) Icons.Default.NotificationsActive else Icons.Outlined.Notifications,
                        contentDescription = "Toggle Reminder",
                        tint = if (event.isReminderSet) Primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Standard card representing a campus club meeting or academic research seminar.
 */
@Composable
private fun EventCardItem(
    event: CampusEvent,
    onEventClick: () -> Unit,
    onRsvpClick: (RsvpStatus) -> Unit,
    onToggleReminder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onEventClick)
            .testTag("event_card_${event.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(event.eventType.badgeColorHex).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = event.eventType.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(event.eventType.badgeColorHex),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (event.isOnlineOrHybrid) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = "🌐 Hybrid / Online",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "👥 ${event.attendeeCount}${if (event.capacity != null) "/${event.capacity}" else ""} attending",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onToggleReminder,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("event_reminder_btn_${event.id}")
                    ) {
                        Icon(
                            imageVector = if (event.isReminderSet) Icons.Default.NotificationsActive else Icons.Outlined.Notifications,
                            contentDescription = "Toggle notification reminder",
                            tint = if (event.isReminderSet) Primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title & Organizer
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${event.organizer} • ${event.organizerType}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )

            // Speaker info if present
            if (event.speakerOrHost.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${event.speakerOrHost}${if (event.speakerAffiliation.isNotBlank()) " (${event.speakerAffiliation})" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Venue and Time details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${event.dateFormatted}, ${event.timeFormatted}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = Tertiary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        event.venue,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Tags row
            if (event.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(event.tags) { tag ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(10.dp))

            // Interactive RSVP Options Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isGoing = event.rsvpStatus == RsvpStatus.GOING
                OutlinedButton(
                    onClick = { onRsvpClick(if (isGoing) RsvpStatus.NONE else RsvpStatus.GOING) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isGoing) Primary else Color.Transparent,
                        contentColor = if (isGoing) Color.White else Primary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Primary)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("rsvp_going_${event.id}")
                ) {
                    Icon(
                        if (isGoing) Icons.Default.Check else Icons.Outlined.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isGoing) "Going ✔" else "Going", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                val isInterested = event.rsvpStatus == RsvpStatus.INTERESTED
                OutlinedButton(
                    onClick = { onRsvpClick(if (isInterested) RsvpStatus.NONE else RsvpStatus.INTERESTED) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isInterested) Color(0xFFF59E0B).copy(alpha = 0.15f) else Color.Transparent,
                        contentColor = if (isInterested) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("rsvp_interested_${event.id}")
                ) {
                    Icon(
                        if (isInterested) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Interested", fontSize = 12.sp)
                }

                TextButton(
                    onClick = onEventClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("event_details_btn_${event.id}")
                ) {
                    Text("Details", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

/**
 * Detailed Modal Bottom Sheet / Dialog showing full seminar abstract, venue directions, and notification controls.
 */
@Composable
private fun EventDetailDialog(
    event: CampusEvent,
    onDismiss: () -> Unit,
    onRsvpChange: (RsvpStatus) -> Unit,
    onToggleReminder: () -> Unit,
    onTriggerTestNotification: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .testTag("event_detail_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(100),
                        color = Color(event.eventType.badgeColorHex).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = event.eventType.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(event.eventType.badgeColorHex),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_detail_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Hosted by ${event.organizer} (${event.organizerType})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "${event.dateFormatted} • ${event.timeFormatted}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Tertiary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(event.venue, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                if (event.meetingLinkOrStream != null) {
                                    Text(
                                        "Stream: ${event.meetingLinkOrStream}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Primary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        if (event.speakerOrHost.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(event.speakerOrHost, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    if (event.speakerAffiliation.isNotBlank()) {
                                        Text(event.speakerAffiliation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("About This Event", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (event.prereqsOrNotes != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = event.prereqsOrNotes,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notification Reminder Toggle Row
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (event.isReminderSet) Primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (event.isReminderSet) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = if (event.isReminderSet) Primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Event Reminder Alert", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Notify 1 hour before start time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Switch(
                            checked = event.isReminderSet,
                            onCheckedChange = { onToggleReminder() },
                            modifier = Modifier.testTag("dialog_reminder_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Test Alert Button
                OutlinedButton(
                    onClick = onTriggerTestNotification,
                    modifier = Modifier.fillMaxWidth().testTag("btn_test_notification"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send Notification Alert Now")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Select RSVP Status:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onRsvpChange(RsvpStatus.GOING) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (event.rsvpStatus == RsvpStatus.GOING) Success else MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = if (event.rsvpStatus == RsvpStatus.GOING) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f).testTag("dialog_rsvp_going")
                    ) {
                        Text(if (event.rsvpStatus == RsvpStatus.GOING) "Going ✔" else "Going")
                    }

                    Button(
                        onClick = { onRsvpChange(RsvpStatus.INTERESTED) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (event.rsvpStatus == RsvpStatus.INTERESTED) Color(0xFFF59E0B) else MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = if (event.rsvpStatus == RsvpStatus.INTERESTED) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f).testTag("dialog_rsvp_interested")
                    ) {
                        Text(if (event.rsvpStatus == RsvpStatus.INTERESTED) "Interested ⭐" else "Interested")
                    }

                    OutlinedButton(
                        onClick = { onRsvpChange(RsvpStatus.DECLINED) },
                        modifier = Modifier.weight(0.8f).testTag("dialog_rsvp_decline")
                    ) {
                        Text("Can't Go", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

/**
 * Dialog allowing club leads or academic coordinators to host or submit a new campus event.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HostCampusEventDialog(
    onDismiss: () -> Unit,
    onEventCreated: (CampusEvent) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var organizer by remember { mutableStateOf("ACM DTU Chapter") }
    var eventType by remember { mutableStateOf(CommunityEventType.CLUB_MEETING) }
    var dateFormatted by remember { mutableStateOf("Friday, Nov 7") }
    var timeFormatted by remember { mutableStateOf("4:30 PM - 6:00 PM") }
    var venue by remember { mutableStateOf("CS Seminar Hall 301") }
    var speakerOrHost by remember { mutableStateOf("") }
    var speakerAffiliation by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("AI, Coding, Meetup") }
    var isHybrid by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .testTag("host_event_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Host / Submit Campus Event",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Event Type", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        CommunityEventType.CLUB_MEETING to "🎪 Club",
                        CommunityEventType.ACADEMIC_SEMINAR to "🎓 Seminar",
                        CommunityEventType.WORKSHOP to "⚡ Workshop"
                    ).forEach { (type, label) ->
                        val isSelected = eventType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { eventType = type },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(type.badgeColorHex),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title *") },
                    placeholder = { Text("e.g. Neural Architecture Colloquium") },
                    modifier = Modifier.fillMaxWidth().testTag("input_event_title")
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = organizer,
                    onValueChange = { organizer = it },
                    label = { Text("Organizer Club or Department *") },
                    modifier = Modifier.fillMaxWidth().testTag("input_event_organizer")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dateFormatted,
                        onValueChange = { dateFormatted = it },
                        label = { Text("Date") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = timeFormatted,
                        onValueChange = { timeFormatted = it },
                        label = { Text("Time") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = venue,
                    onValueChange = { venue = it },
                    label = { Text("Venue / Room *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = speakerOrHost,
                    onValueChange = { speakerOrHost = it },
                    label = { Text("Speaker or Host Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Event Abstract / Description *") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("input_event_desc")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank() && organizer.isNotBlank()) {
                            val parsedTags = tagsInput.split(",")
                                .map { it.trim().removePrefix("#") }
                                .filter { it.isNotBlank() }

                            val created = CampusEvent(
                                title = title.trim(),
                                organizer = organizer.trim(),
                                organizerType = if (eventType == CommunityEventType.ACADEMIC_SEMINAR) "Academic Department" else "Campus Club",
                                eventType = eventType,
                                dateFormatted = dateFormatted.trim(),
                                timeFormatted = timeFormatted.trim(),
                                venue = venue.trim(),
                                isOnlineOrHybrid = isHybrid,
                                speakerOrHost = speakerOrHost.trim(),
                                speakerAffiliation = speakerAffiliation.trim(),
                                description = if (description.isNotBlank()) description.trim() else "Campus community session hosted by $organizer.",
                                tags = if (parsedTags.isNotEmpty()) parsedTags else listOf("Campus", "DTU"),
                                attendeeCount = 1,
                                rsvpStatus = RsvpStatus.GOING,
                                isReminderSet = true
                            )
                            onEventCreated(created)
                        }
                    },
                    enabled = title.isNotBlank() && organizer.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier.fillMaxWidth().testTag("btn_submit_event")
                ) {
                    Icon(Icons.Default.Publish, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Publish to Campus Aggregator", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
