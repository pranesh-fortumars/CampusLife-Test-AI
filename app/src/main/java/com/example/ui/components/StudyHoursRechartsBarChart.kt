package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SubjectStudyStats
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel
import kotlin.math.max

@Composable
fun StudyHoursRechartsBarChart(
    viewModel: CampusLifeViewModel,
    modifier: Modifier = Modifier
) {
    val studyStats by viewModel.studyStatsPerSubject.collectAsState()
    val totalFocusMinutes by viewModel.totalFocusMinutes.collectAsState()
    val courses by viewModel.courses.collectAsState()

    var selectedStat by remember { mutableStateOf<SubjectStudyStats?>(null) }
    val goalHours = 5.0 // Weekly target goal per subject

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recharts_study_hours_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Title & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = CircleShape,
                            color = IndigoPrimaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = IndigoPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = "Study Hours per Subject",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Recharts-style analytics from Room database",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(100),
                    color = EmeraldTertiaryContainer
                ) {
                    Text(
                        text = "${String.format("%.1f", totalFocusMinutes / 60.0)}h Total",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldOnTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Recharts Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(IndigoPrimary)
                    )
                    Text("Hours Logged", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(2.dp)
                            .background(WarningAmber)
                    )
                    Text("Target Goal (${goalHours.toInt()}h)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Interactive Tooltip Card if a bar is selected
            AnimatedVisibility(
                visible = selectedStat != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedStat?.let { stat ->
                    val courseName = courses.firstOrNull { it.code == stat.courseCode }?.name ?: "Subject Course"
                    val progressPct = ((stat.totalHours / goalHours) * 100).toInt()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recharts_active_tooltip"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(shape = RoundedCornerShape(4.dp), color = IndigoPrimary) {
                                        Text(
                                            stat.courseCode,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        courseName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    "${String.format("%.1f", stat.totalHours)} Hours • ${stat.sessionCount} Focus Sessions",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(100),
                                    color = if (progressPct >= 100) EmeraldTertiaryContainer else WarningContainer
                                ) {
                                    Text(
                                        text = "$progressPct% of Goal",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (progressPct >= 100) EmeraldOnTertiaryContainer else OnWarningContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { selectedStat = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Close tooltip",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Main Bar Chart Viewport
            if (studyStats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No study sessions logged yet. Complete a Pomodoro block to populate your chart!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                val maxHoursVal = max(8.0, (studyStats.maxOfOrNull { it.totalHours } ?: 6.0) + 1.5)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .testTag("recharts_bar_chart_viewport")
                ) {
                    // Background Canvas: Grid Lines and Goal Reference Line
                    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    val goalLineColor = WarningAmber.copy(alpha = 0.8f)

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val bottomAxisY = size.height - 35.dp.toPx()
                        val topY = 20.dp.toPx()
                        val chartHeight = bottomAxisY - topY

                        // 4 horizontal grid lines (0h, 2h, 4h, 6h, etc.)
                        val gridSteps = 4
                        for (i in 0..gridSteps) {
                            val y = bottomAxisY - (chartHeight * (i.toFloat() / gridSteps))
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        }

                        // Target Goal dashed reference line
                        val goalY = bottomAxisY - (chartHeight * (goalHours.toFloat() / maxHoursVal.toFloat()))
                        if (goalY in topY..bottomAxisY) {
                            drawLine(
                                color = goalLineColor,
                                start = Offset(0f, goalY),
                                end = Offset(size.width, goalY),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                            )
                        }
                    }

                    // Foreground Bars Row
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        studyStats.forEach { stat ->
                            val isSelected = selectedStat?.courseCode == stat.courseCode
                            val barHeightFraction = (stat.totalHours / maxHoursVal).toFloat().coerceIn(0.04f, 1f)

                            val animatedHeight by animateFloatAsState(
                                targetValue = barHeightFraction,
                                animationSpec = tween(durationMillis = 800),
                                label = "bar_${stat.courseCode}"
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                                    .clickable { selectedStat = stat }
                                    .testTag("bar_item_${stat.courseCode}")
                            ) {
                                // Value label above bar
                                Text(
                                    text = "${String.format("%.1f", stat.totalHours)}h",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )

                                // Vertical Bar Column with rounded top
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .fillMaxHeight(animatedHeight * 0.75f)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    if (isSelected) VioletSecondary else IndigoPrimary,
                                                    if (isSelected) IndigoPrimary else Color(0xFF6366F1)
                                                )
                                            )
                                        )
                                        .then(
                                            if (isSelected) Modifier.border(
                                                2.dp,
                                                MaterialTheme.colorScheme.primary,
                                                RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                            ) else Modifier
                                        )
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Course Code label below bar (X-Axis)
                                Text(
                                    text = stat.courseCode,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    color = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 0.8.dp)

            // Bottom Summary Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val mostStudied = studyStats.maxByOrNull { it.totalMinutes }

                SummaryMiniCard(
                    title = "Most Studied",
                    value = mostStudied?.courseCode ?: "None",
                    subtitle = if (mostStudied != null) "${String.format("%.1f", mostStudied.totalHours)} hrs" else "-",
                    icon = Icons.Default.EmojiEvents,
                    color = WarningAmber,
                    containerColor = WarningContainer,
                    modifier = Modifier.weight(1f)
                )

                SummaryMiniCard(
                    title = "Total Sessions",
                    value = "${studyStats.sumOf { it.sessionCount }}",
                    subtitle = "focus blocks",
                    icon = Icons.Default.CheckCircle,
                    color = EmeraldTertiary,
                    containerColor = EmeraldTertiaryContainer,
                    modifier = Modifier.weight(1f)
                )

                SummaryMiniCard(
                    title = "Avg / Subject",
                    value = if (studyStats.isNotEmpty()) "${String.format("%.1f", (totalFocusMinutes / 60.0) / studyStats.size)}h" else "0h",
                    subtitle = "per course",
                    icon = Icons.Default.Analytics,
                    color = IndigoPrimary,
                    containerColor = IndigoPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SummaryMiniCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor.copy(alpha = 0.35f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
                Text(title, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = color)
            }
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
