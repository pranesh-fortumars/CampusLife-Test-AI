package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import com.example.model.CourseAttendance
import com.example.model.LectureAttendanceStatus
import com.example.model.LectureType
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modal dialog to log a lecture session attendance entry with institutional status,
 * lecture type classification, covered topic, and real-time attendance percentage impact preview.
 */
@Composable
fun MarkLectureSessionDialog(
    course: CourseAttendance,
    onDismiss: () -> Unit,
    onConfirm: (LectureAttendanceStatus, LectureType, String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(LectureAttendanceStatus.PRESENT) }
    var selectedType by remember { mutableStateOf(LectureType.THEORY) }
    var topicText by remember { mutableStateOf("") }

    val todayFormatted = remember {
        SimpleDateFormat("EEE, MMM d, yyyy", Locale.US).format(Date())
    }

    // Compute live projected outcome
    val projectedAttended = when (selectedStatus) {
        LectureAttendanceStatus.PRESENT, LectureAttendanceStatus.DUTY_LEAVE, LectureAttendanceStatus.MEDICAL_LEAVE ->
            course.attendedClasses + 1
        else -> course.attendedClasses
    }

    val projectedTotal = when (selectedStatus) {
        LectureAttendanceStatus.CANCELLED -> course.totalClasses
        else -> course.totalClasses + 1
    }

    val projectedPercentage = if (projectedTotal == 0) 100.0 else (projectedAttended.toDouble() / projectedTotal) * 100.0
    val deltaPct = projectedPercentage - course.attendancePercentage
    val isProjectedSafe = projectedPercentage >= course.targetPercentage

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
                        text = "Log Lecture Attendance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PrimaryContainer
                    ) {
                        Text(
                            text = course.courseCode,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = course.courseName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mark_lecture_dialog_content"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Date & Time Banner
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(14.dp), tint = Primary)
                        Text(
                            text = "$todayFormatted • ${course.nextClassTime}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 1. Attendance Status Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Attendance Status:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            LectureAttendanceStatus.PRESENT to Tertiary,
                            LectureAttendanceStatus.ABSENT to Error,
                            LectureAttendanceStatus.DUTY_LEAVE to Secondary
                        ).forEach { (status, activeColor) ->
                            val isSelected = selectedStatus == status
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) activeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainer,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) activeColor else OutlineVariant
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedStatus = status }
                                    .testTag("status_chip_${status.name.lowercase()}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = when (status) {
                                            LectureAttendanceStatus.PRESENT -> Icons.Default.CheckCircle
                                            LectureAttendanceStatus.ABSENT -> Icons.Default.Cancel
                                            else -> Icons.Default.WorkspacePremium
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = status.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Lecture Type Selector (Theory / Lab / Tutorial)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Session Type:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(LectureType.THEORY, LectureType.LAB, LectureType.TUTORIAL).forEach { type ->
                            val isSelected = selectedType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedType = type },
                                label = { Text(type.shortLabel, fontSize = 11.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("type_chip_${type.name.lowercase()}"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // 3. Topic Covered Input
                OutlinedTextField(
                    value = topicText,
                    onValueChange = { topicText = it },
                    label = { Text("Topic covered / Session notes (optional)") },
                    placeholder = { Text("e.g. B-Trees & Indexing in SQL") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lecture_topic_input")
                )

                // 4. Live Impact Preview Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isProjectedSafe) TertiaryContainer else ErrorContainer,
                    border = BorderStroke(
                        1.dp,
                        if (isProjectedSafe) Tertiary.copy(alpha = 0.3f) else Error.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ATTENDANCE IMPACT",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isProjectedSafe) OnTertiaryContainer else OnErrorContainer
                            )
                            Text(
                                text = "${course.formattedPercentage} → ${String.format(Locale.US, "%.1f%%", projectedPercentage)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isProjectedSafe) Tertiary else Error
                            )
                        }

                        val deltaSign = if (deltaPct >= 0) "+" else ""
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isProjectedSafe) TertiaryFixed else ErrorContainer
                        ) {
                            Text(
                                text = "$deltaSign${String.format(Locale.US, "%.1f%%", deltaPct)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isProjectedSafe) Tertiary else Error,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedStatus, selectedType, topicText) },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_mark_lecture_btn")
            ) {
                Text("Confirm Log", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_mark_lecture_btn")
            ) {
                Text("Cancel")
            }
        }
    )
}
