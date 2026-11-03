package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SemesterEntity
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel

@Composable
fun SemesterProgressComponent(
    viewModel: CampusLifeViewModel,
    modifier: Modifier = Modifier
) {
    val currentSemester by viewModel.currentSemester.collectAsState()
    val allSemesters by viewModel.allSemesters.collectAsState()

    var showSemesterDialog by remember { mutableStateOf(false) }

    val semester = currentSemester ?: allSemesters.firstOrNull()

    if (semester != null) {
        val now = remember { System.currentTimeMillis() }
        val rawProgress = remember(semester) { semester.calculateProgress(now) }
        val animatedProgress by animateFloatAsState(
            targetValue = rawProgress,
            animationSpec = tween(durationMillis = 1000),
            label = "semester_progress"
        )
        val percent = (rawProgress * 100).toInt()
        val daysElapsed = remember(semester) { semester.daysElapsed(now) }
        val daysRemaining = remember(semester) { semester.daysRemaining(now) }
        val currentWeek = remember(semester) { semester.currentWeek(now) }

        Card(
            modifier = modifier
                .fillMaxWidth()
                .testTag("semester_progress_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row: Semester title & Switcher Chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = IndigoPrimaryContainer
                            ) {
                                Text(
                                    text = semester.code,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IndigoOnPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "Week $currentWeek of ${semester.totalWeeks}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = semester.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Switch / Details Pill
                    Surface(
                        shape = RoundedCornerShape(100),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(100))
                            .clickable { showSemesterDialog = true }
                            .testTag("btn_switch_semester")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.SwapHoriz,
                                contentDescription = "Switch Semester",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Terms",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Progress Bar with Percentage Tag
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Semester Completion",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$percent%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Custom styled progress bar container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(100))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("semester_progress_bar")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(100))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            IndigoPrimary,
                                            VioletSecondary
                                        )
                                    )
                                )
                        )
                    }

                    // Start and End date labels below progress bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Starts ${semester.startDate}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Ends ${semester.endDate}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 0.8.dp)

                // Key metrics row: Days Elapsed, Days Left, Target GPA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricChip(
                        title = "Elapsed",
                        value = "${daysElapsed}d",
                        subtitle = "in class",
                        icon = Icons.Default.CheckCircle,
                        color = EmeraldTertiary,
                        containerColor = EmeraldTertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )

                    MetricChip(
                        title = "Remaining",
                        value = "${daysRemaining}d",
                        subtitle = "to finals",
                        icon = Icons.Default.HourglassBottom,
                        color = IndigoPrimary,
                        containerColor = IndigoPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )

                    MetricChip(
                        title = "Target",
                        value = String.format("%.2f", semester.targetGpa),
                        subtitle = "GPA goal",
                        icon = Icons.Default.Star,
                        color = WarningAmber,
                        containerColor = WarningContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // Semester Switcher Dialog
    if (showSemesterDialog) {
        AlertDialog(
            onDismissRequest = { showSemesterDialog = false },
            title = { Text("Select Academic Semester") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allSemesters.forEach { term ->
                        val isSelected = term.isCurrent
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.switchCurrentSemester(term.id)
                                    showSemesterDialog = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) IndigoPrimaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, IndigoPrimary) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(term.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        if (term.isCurrent) {
                                            Surface(shape = RoundedCornerShape(100), color = IndigoPrimary) {
                                                Text(
                                                    "Active",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        "${term.startDate} - ${term.endDate} (${term.totalWeeks} wks)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSemesterDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun MetricChip(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor.copy(alpha = 0.45f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                Text(title, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = color)
            }
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
