package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.ui.theme.*

@Composable
fun LogClassSessionDialog(
    subject: Subject,
    onDismiss: () -> Unit,
    onConfirm: (AttendanceType, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(AttendanceType.ATTENDED) }
    var sessionNote by remember { mutableStateOf("") }

    val projectedPercentage = when (selectedType) {
        AttendanceType.ATTENDED -> subject.calculateProjected(addAttended = 1, addMissed = 0)
        AttendanceType.MISSED -> subject.calculateProjected(addAttended = 0, addMissed = 1)
        AttendanceType.CANCELLED -> subject.attendancePercentage
    }

    val projectedAttended = when (selectedType) {
        AttendanceType.ATTENDED -> subject.attendedClasses + 1
        else -> subject.attendedClasses
    }

    val projectedTotal = when (selectedType) {
        AttendanceType.CANCELLED -> subject.totalClasses
        else -> subject.totalClasses + 1
    }

    val pctDiff = projectedPercentage - subject.attendancePercentage
    val diffSign = if (pctDiff >= 0) "+" else ""
    val diffStr = String.format("%.1f%%", pctDiff)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Log Class Attendance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(shape = RoundedCornerShape(100), color = PrimaryFixed) {
                        Text(
                            subject.code,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = subject.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Session Status Selection Tabs
                Text(
                    text = "Select Class Outcome:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Attended option
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedType = AttendanceType.ATTENDED }
                            .testTag("dialog_option_attended"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedType == AttendanceType.ATTENDED) TertiaryFixed else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = if (selectedType == AttendanceType.ATTENDED) androidx.compose.foundation.BorderStroke(2.dp, Tertiary) else null
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (selectedType == AttendanceType.ATTENDED) Tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Attended",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedType == AttendanceType.ATTENDED) Tertiary else MaterialTheme.colorScheme.onSurface
                            )
                            Text("+1 Present", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Missed option
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedType = AttendanceType.MISSED }
                            .testTag("dialog_option_missed"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedType == AttendanceType.MISSED) ErrorContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = if (selectedType == AttendanceType.MISSED) androidx.compose.foundation.BorderStroke(2.dp, Error) else null
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (selectedType == AttendanceType.MISSED) Error else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Missed",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedType == AttendanceType.MISSED) Error else MaterialTheme.colorScheme.onSurface
                            )
                            Text("+1 Absent", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Cancelled option
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedType = AttendanceType.CANCELLED },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedType == AttendanceType.CANCELLED) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = if (selectedType == AttendanceType.CANCELLED) androidx.compose.foundation.BorderStroke(2.dp, Primary) else null
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.EventBusy,
                                contentDescription = null,
                                tint = if (selectedType == AttendanceType.CANCELLED) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Cancelled",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedType == AttendanceType.CANCELLED) Primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text("No Class", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Live Projected Calculation Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Projected Percentage:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    String.format("%.1f%%", projectedPercentage),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (projectedPercentage >= 75.0) Tertiary else Error
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "($diffSign$diffStr)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (pctDiff >= 0) Tertiary else Error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "New Record: $projectedAttended / $projectedTotal classes attended",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (selectedType != AttendanceType.CANCELLED) {
                            val tempSubject = subject.copy(
                                attendedClasses = projectedAttended,
                                totalClasses = projectedTotal
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (tempSubject.attendancePercentage >= 75.0) {
                                Text(
                                    text = "✔ Safe: ${tempSubject.safeBunksLeft} safe bunks left before dropping below 75%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Tertiary,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text(
                                    text = "⚠ Low: Need ${tempSubject.neededToReach75} consecutive classes to reach 75%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Error,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Optional Topic / Reason Note
                OutlinedTextField(
                    value = sessionNote,
                    onValueChange = { sessionNote = it },
                    label = { Text("Topic or Reason (Optional)") },
                    placeholder = { Text("e.g. Unit 3 BCNF proofs, Sick leave, Lab experiment") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedType, sessionNote) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_log_btn")
            ) {
                Text("Save Attendance Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun QuickAdjustAttendanceDialog(
    subject: Subject,
    onDismiss: () -> Unit,
    onSave: (attended: Int, missed: Int, targetPercentage: Double) -> Unit
) {
    var attended by remember { mutableIntStateOf(subject.attendedClasses) }
    var missed by remember { mutableIntStateOf(subject.missedClasses) }
    var targetPct by remember { mutableDoubleStateOf(subject.targetPercentage) }

    val total = (attended + missed).coerceAtLeast(0)
    val calculatedPct = if (total == 0) 100.0 else (attended.toDouble() / total) * 100.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Adjust Counts: ${subject.code}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Correct past attended & missed records directly", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Calculated live summary
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Current Percentage:", style = MaterialTheme.typography.labelSmall)
                            Text(
                                String.format("%.1f%%", calculatedPct),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (calculatedPct >= targetPct) Tertiary else Error
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Classes:", style = MaterialTheme.typography.labelSmall)
                            Text("$total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Attended Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Classes Attended:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Classes you were present in", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (attended > 0) attended-- },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        Text(
                            text = "$attended",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        IconButton(
                            onClick = { attended++ },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }

                // Missed Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Classes Missed:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Classes you were absent/bunked", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (missed > 0) missed-- },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        Text(
                            text = "$missed",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        IconButton(
                            onClick = { missed++ },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }

                // Target threshold preset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Target Threshold:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(75.0, 80.0, 85.0).forEach { pct ->
                            FilterChip(
                                selected = targetPct == pct,
                                onClick = { targetPct = pct },
                                label = { Text("${pct.toInt()}%", fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(attended, missed, targetPct) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddNewSubjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (code: String, name: String, credits: Int, faculty: String, attended: Int, missed: Int, target: Double) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var creditsText by remember { mutableStateOf("4") }
    var faculty by remember { mutableStateOf("") }
    var attendedText by remember { mutableStateOf("0") }
    var missedText by remember { mutableStateOf("0") }
    var targetPct by remember { mutableDoubleStateOf(75.0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add New Subject to Track", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Subject Code (e.g. CS-310)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Name (e.g. Cloud Computing)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = faculty,
                        onValueChange = { faculty = it },
                        label = { Text("Faculty / Professor") },
                        singleLine = true,
                        modifier = Modifier.weight(1.5f)
                    )
                    OutlinedTextField(
                        value = creditsText,
                        onValueChange = { creditsText = it },
                        label = { Text("Credits") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = attendedText,
                        onValueChange = { attendedText = it },
                        label = { Text("Attended") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = missedText,
                        onValueChange = { missedText = it },
                        label = { Text("Missed") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (code.isNotBlank() && name.isNotBlank()) {
                        val c = creditsText.toIntOrNull() ?: 4
                        val a = attendedText.toIntOrNull() ?: 0
                        val m = missedText.toIntOrNull() ?: 0
                        onConfirm(code, name, c, faculty, a, m, targetPct)
                    }
                },
                enabled = code.isNotBlank() && name.isNotBlank(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Add Subject")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
