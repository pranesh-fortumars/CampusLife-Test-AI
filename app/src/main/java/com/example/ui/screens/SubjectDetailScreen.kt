package com.example.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AttendanceType
import com.example.model.Subject
import com.example.ui.components.LogClassSessionDialog
import com.example.ui.components.QuickAdjustAttendanceDialog
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel
import com.example.viewmodel.SubScreen

@Composable
fun SubjectDetailScreen(
    subject: Subject,
    viewModel: CampusLifeViewModel,
    onBack: () -> Unit
) {
    // Keep subject live from ViewModel state
    val subjects by viewModel.subjects.collectAsState()
    val currentSubject = subjects.find { it.code == subject.code } ?: subject

    val allRecords by viewModel.attendanceHistory.collectAsState()
    val subjectRecords = remember(allRecords, currentSubject.code) {
        allRecords.filter { it.subjectCode == currentSubject.code }
    }

    var showLogDialog by remember { mutableStateOf(false) }
    var showAdjustDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Overview") }
    val tabs = listOf("Overview", "Attendance Logs (${subjectRecords.size})", "Study Materials (14)", "Assignments (3)", "Exam Prep")

    val pct = currentSubject.attendancePercentage
    val isCritical = pct < currentSubject.targetPercentage
    val statusColor = when {
        pct >= 85.0 -> Tertiary
        pct >= 75.0 -> Secondary
        else -> Error
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("subject_detail_screen")
    ) {
        // App bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("subject_detail_back_btn")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text("Track > Enrolled Subject", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(currentSubject.code, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = RoundedCornerShape(100), color = PrimaryContainer) {
                                Text(
                                    currentSubject.code,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            IconButton(onClick = {}) {
                                Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark")
                            }
                        }

                        Text(currentSubject.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("${currentSubject.faculty} • ${currentSubject.department}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(shape = RoundedCornerShape(100), color = TertiaryFixed.copy(alpha = 0.5f)) {
                                Text("${currentSubject.credits} Credits", style = MaterialTheme.typography.labelSmall, color = Tertiary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                            Surface(shape = RoundedCornerShape(100), color = SecondaryFixed.copy(alpha = 0.5f)) {
                                Text("Lab Included", style = MaterialTheme.typography.labelSmall, color = Secondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                    }
                }
            }

            // Interactive Attendance Logging & Calculation Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("attendance_tracking_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FactCheck, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Attendance Tracking", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Surface(
                                shape = RoundedCornerShape(100),
                                color = if (isCritical) ErrorContainer else TertiaryFixed
                            ) {
                                Text(
                                    text = if (isCritical) "Debar Risk (< ${currentSubject.targetPercentage.toInt()}%)" else "Safe Zone",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCritical) Error else Tertiary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Large percentage and statistics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text("Current Attendance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = String.format(java.util.Locale.US, "%.1f%%", pct),
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = statusColor
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Target: ${currentSubject.targetPercentage.toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${currentSubject.attendedClasses} / ${currentSubject.totalClasses}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Classes Attended", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { (pct / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = statusColor,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Counts breakdown grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Attended
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = TertiaryFixed.copy(alpha = 0.35f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("ATTENDED", style = MaterialTheme.typography.labelSmall, color = Tertiary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    Text("${currentSubject.attendedClasses}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Tertiary)
                                }
                            }
                            // Missed
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = ErrorContainer.copy(alpha = 0.35f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("MISSED", style = MaterialTheme.typography.labelSmall, color = Error, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    Text("${currentSubject.missedClasses}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Error)
                                }
                            }
                            // Total Classes
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("TOTAL HELD", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    Text("${currentSubject.totalClasses}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Advisory Banner
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCritical) ErrorContainer.copy(alpha = 0.3f) else TertiaryFixed.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isCritical) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isCritical) Error else Tertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isCritical)
                                        "Debar Risk: You must attend the next ${currentSubject.neededToReach75} consecutive classes to cross 75%!"
                                    else
                                        "Safe: You can safely miss up to ${currentSubject.safeBunksLeft} classes without dropping below 75%.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isCritical) Error else Tertiary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Log Class Attendance:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))

                        // 1-Tap Quick Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.quickLogClass(currentSubject.code, isAttended = true)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Tertiary,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("detail_quick_attend_btn")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Attended")
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.quickLogClass(currentSubject.code, isAttended = false)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Error
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("detail_quick_miss_btn")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Missed")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Secondary actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { showLogDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("detail_open_log_dialog_btn")
                            ) {
                                Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Log Session Details", fontSize = 12.sp)
                            }

                            FilledTonalButton(
                                onClick = { showAdjustDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("detail_open_adjust_dialog_btn")
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Calibrate Counts", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Next Lecture Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Next Lecture Schedule", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                            Text(currentSubject.nextClassTime, style = MaterialTheme.typography.bodySmall, color = Primary, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Location: ${currentSubject.room} • Faculty: ${currentSubject.faculty}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Attendance Logs History Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Attendance Log History (${subjectRecords.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (subjectRecords.isNotEmpty()) {
                                TextButton(
                                    onClick = { viewModel.undoLastAttendance(currentSubject.code) },
                                    modifier = Modifier.testTag("detail_undo_attendance_btn")
                                ) {
                                    Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Undo Last", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        if (subjectRecords.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No recent class records logged yet. Tap + Attended or + Missed above to start logging!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                subjectRecords.take(5).forEach { record ->
                                    val isAttended = record.type == AttendanceType.ATTENDED
                                    val isMissed = record.type == AttendanceType.MISSED

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerLow
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isAttended) TertiaryFixed else if (isMissed) ErrorContainer else MaterialTheme.colorScheme.surfaceVariant
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = if (isAttended) Icons.Default.Check else if (isMissed) Icons.Default.Close else Icons.Default.EventBusy,
                                                        contentDescription = null,
                                                        tint = if (isAttended) Tertiary else if (isMissed) Error else Primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = if (record.note.isNotBlank()) record.note else record.type.label,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Text(
                                                        text = "${record.date} • ${record.time}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isAttended) TertiaryFixed else if (isMissed) ErrorContainer else MaterialTheme.colorScheme.surfaceVariant
                                            ) {
                                                Text(
                                                    text = record.type.label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isAttended) Tertiary else if (isMissed) Error else Primary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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

            // Copilot Banner
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PrimaryContainer.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(PrimaryContainer), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ask Academic Copilot", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text("Calculate attendance waivers & medical leaves", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = { viewModel.openSubScreen(SubScreen.CAMPUS_COPILOT) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Launch")
                        }
                    }
                }
            }

            // Tabs
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tabs) { t ->
                        val isSelected = selectedTab == t
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = if (isSelected) PrimaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
                            modifier = Modifier.clickable { selectedTab = t }
                        ) {
                            Text(
                                t,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Syllabus Breakdown Units
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Syllabus Breakdown (Mid-Sem 68% accomplished)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    val units = listOf(
                        Triple("Unit 1: Foundations & Architecture", "Core structures, Models, Relational mapping", 100),
                        Triple("Unit 2: Implementation & Systems", "Queries, Views, Materialization, Stored procedures", 100),
                        Triple("Unit 3: Optimization & Dependencies", "Normal forms, Lossless joins, Decomposition", 75),
                        Triple("Unit 4: Concurrency & Storage", "Transactions, ACID properties, Recovery", 0)
                    )

                    units.forEach { (unitTitle, unitDesc, progressPct) ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(unitTitle, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Text("$progressPct%", style = MaterialTheme.typography.labelSmall, color = if (progressPct == 100) Tertiary else Primary, fontWeight = FontWeight.Bold)
                                }
                                Text(unitDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { progressPct / 100f },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = if (progressPct == 100) Tertiary else Primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showLogDialog) {
        LogClassSessionDialog(
            subject = currentSubject,
            onDismiss = { showLogDialog = false },
            onConfirm = { type, note ->
                viewModel.logAttendance(currentSubject.code, type, note)
                showLogDialog = false
            }
        )
    }

    if (showAdjustDialog) {
        QuickAdjustAttendanceDialog(
            subject = currentSubject,
            onDismiss = { showAdjustDialog = false },
            onSave = { attended, missed, targetPct ->
                viewModel.updateSubjectCounts(currentSubject.code, attended, missed, targetPct)
                showAdjustDialog = false
            }
        )
    }
}
