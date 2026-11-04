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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DayStudyTrend
import com.example.data.local.SubjectStudyStats
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel
import kotlin.math.max

enum class ChartViewMode(val label: String) {
    BY_SUBJECT("By Subject"),
    WEEKLY_TREND("Weekly Trend")
}

@Composable
fun StudyHoursRechartsBarChart(
    viewModel: CampusLifeViewModel,
    modifier: Modifier = Modifier
) {
    val studyStats by viewModel.studyStatsPerSubject.collectAsState()
    val weeklyTrends by viewModel.weeklyStudyTrends.collectAsState()
    val totalFocusMinutes by viewModel.totalFocusMinutes.collectAsState()
    val courses by viewModel.courses.collectAsState()

    var viewMode by remember { mutableStateOf(ChartViewMode.BY_SUBJECT) }
    var showTargetOverlay by remember { mutableStateOf(true) }

    var selectedStat by remember { mutableStateOf<SubjectStudyStats?>(null) }
    var selectedDayTrend by remember { mutableStateOf<DayStudyTrend?>(null) }

    val subjectGoalHours = 5.0 // Weekly goal per subject
    val dailyGoalHours = 2.0   // Daily consistency goal

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
                                    if (viewMode == ChartViewMode.WEEKLY_TREND) Icons.Default.TrendingUp else Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = IndigoPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = if (viewMode == ChartViewMode.WEEKLY_TREND) "Weekly Consistency Trend" else "Study Hours per Subject",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = if (viewMode == ChartViewMode.WEEKLY_TREND) "Day-by-day consistency from Room study records" else "Recharts-style analytics from Room database",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(100),
                    color = EmeraldTertiaryContainer
                ) {
                    Text(
                        text = "${String.format("%.1f", totalFocusMinutes / 60.0)}h Logged",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldOnTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Interactive View Mode Switcher Toggle & Target Overlay Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Segmented Toggle: By Subject vs Weekly Trend
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ChartViewMode.values().forEach { mode ->
                        val isSelected = viewMode == mode
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = if (isSelected) IndigoPrimary else Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(100))
                                .clickable {
                                    viewMode = mode
                                    selectedStat = null
                                    selectedDayTrend = null
                                }
                                .testTag("toggle_chart_${mode.name.lowercase()}")
                        ) {
                            Text(
                                text = mode.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Overlay Goal Target Toggle
                FilterChip(
                    selected = showTargetOverlay,
                    onClick = { showTargetOverlay = !showTargetOverlay },
                    label = {
                        Text(
                            if (viewMode == ChartViewMode.WEEKLY_TREND) "Goal (2h/day)" else "Goal (5h)",
                            fontSize = 10.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            if (showTargetOverlay) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                    },
                    shape = RoundedCornerShape(100),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WarningContainer,
                        selectedLabelColor = OnWarningContainer,
                        selectedLeadingIconColor = OnWarningContainer
                    ),
                    modifier = Modifier
                        .heightIn(min = 32.dp)
                        .testTag("toggle_target_overlay")
                )
            }

            // Legend Row
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
                    Text(
                        if (viewMode == ChartViewMode.WEEKLY_TREND) "Daily Study Hours" else "Subject Hours",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (showTargetOverlay) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(2.dp)
                                .background(WarningAmber)
                        )
                        Text(
                            if (viewMode == ChartViewMode.WEEKLY_TREND) "Daily Target (2.0h)" else "Subject Goal (5.0h)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Active Recharts Tooltip for Subject Mode
            AnimatedVisibility(
                visible = viewMode == ChartViewMode.BY_SUBJECT && selectedStat != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedStat?.let { stat ->
                    val courseName = courses.firstOrNull { it.code == stat.courseCode }?.name ?: "Subject Course"
                    val progressPct = ((stat.totalHours / subjectGoalHours) * 100).toInt()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recharts_subject_tooltip"),
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
                                IconButton(onClick = { selectedStat = null }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Active Recharts Tooltip for Weekly Trend Mode
            AnimatedVisibility(
                visible = viewMode == ChartViewMode.WEEKLY_TREND && selectedDayTrend != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedDayTrend?.let { day ->
                    val goalPct = ((day.totalHours / dailyGoalHours) * 100).toInt()
                    val courseTitle = courses.firstOrNull { it.code == day.primarySubject }?.name ?: "Course Study"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recharts_weekly_tooltip"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldTertiary.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    "${day.dayLabel} • ${String.format("%.1f", day.totalHours)} Hours",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Primary: ${day.primarySubject} ($courseTitle) • ${day.sessionCount} sessions",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(100),
                                    color = if (goalPct >= 100) EmeraldTertiaryContainer else WarningContainer
                                ) {
                                    Text(
                                        text = if (goalPct >= 100) "Goal Reached ($goalPct%)" else "$goalPct% of 2h goal",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (goalPct >= 100) EmeraldOnTertiaryContainer else OnWarningContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                IconButton(onClick = { selectedDayTrend = null }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Main Bar Chart Viewport
            val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            val goalLineColor = WarningAmber.copy(alpha = 0.85f)

            if (viewMode == ChartViewMode.BY_SUBJECT) {
                // By Subject View
                if (studyStats.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                        Text("No study sessions logged yet. Complete a Pomodoro block to populate your chart!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                } else {
                    val maxHoursVal = max(8.0, (studyStats.maxOfOrNull { it.totalHours } ?: 6.0) + 1.5)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .testTag("recharts_subject_viewport")
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val bottomAxisY = size.height - 35.dp.toPx()
                            val topY = 20.dp.toPx()
                            val chartHeight = bottomAxisY - topY

                            // Horizontal Grid Lines
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

                            // Goal Reference Line
                            if (showTargetOverlay) {
                                val goalY = bottomAxisY - (chartHeight * (subjectGoalHours.toFloat() / maxHoursVal.toFloat()))
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
                        }

                        // Bars Row
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
                                        .testTag("bar_subject_${stat.courseCode}")
                                ) {
                                    Text(
                                        text = "${String.format("%.1f", stat.totalHours)}h",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )

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
            } else {
                // Weekly Trend View: 7 Days (Mon - Sun)
                val maxDailyHours = max(4.0, (weeklyTrends.maxOfOrNull { it.totalHours } ?: 3.5) + 0.8)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .testTag("recharts_weekly_viewport")
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val bottomAxisY = size.height - 35.dp.toPx()
                        val topY = 20.dp.toPx()
                        val chartHeight = bottomAxisY - topY

                        // Grid lines
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

                        // Daily Goal Target Reference Line (2.0h)
                        if (showTargetOverlay) {
                            val goalY = bottomAxisY - (chartHeight * (dailyGoalHours.toFloat() / maxDailyHours.toFloat()))
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
                    }

                    // 7 Daily Bars
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyTrends.forEach { dayTrend ->
                            val isSelected = selectedDayTrend?.dayKey == dayTrend.dayKey
                            val reachesGoal = dayTrend.totalHours >= dailyGoalHours
                            val isToday = dayTrend.dayKey == "Tue"
                            val barHeightFraction = (dayTrend.totalHours / maxDailyHours).toFloat().coerceIn(0.04f, 1f)

                            val animatedHeight by animateFloatAsState(
                                targetValue = barHeightFraction,
                                animationSpec = tween(durationMillis = 800),
                                label = "trend_${dayTrend.dayKey}"
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                                    .padding(horizontal = 3.dp)
                                    .clickable { selectedDayTrend = dayTrend }
                                    .testTag("bar_day_${dayTrend.dayKey}")
                            ) {
                                // Value label
                                Text(
                                    text = "${String.format("%.1f", dayTrend.totalHours)}h",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (reachesGoal) EmeraldTertiary else if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )

                                // Vertical Bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.65f)
                                        .fillMaxHeight(animatedHeight * 0.75f)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    if (isSelected) VioletSecondary else if (reachesGoal) EmeraldTertiary else IndigoPrimary,
                                                    if (isSelected) IndigoPrimary else if (reachesGoal) Color(0xFF059669) else Color(0xFF6366F1)
                                                )
                                            )
                                        )
                                        .then(
                                            if (isToday) Modifier.border(
                                                1.5.dp,
                                                WarningAmber,
                                                RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                            ) else Modifier
                                        )
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Day label (Mon, Tue, etc.)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = dayTrend.dayKey,
                                        fontSize = 11.sp,
                                        fontWeight = if (isToday || isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isToday) WarningAmber else if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 0.8.dp)

            // Bottom Summary Metrics Row: Dynamic based on viewMode
            if (viewMode == ChartViewMode.WEEKLY_TREND) {
                val totalWeeklyMins = weeklyTrends.sumOf { it.totalMinutes }
                val activeDaysCount = weeklyTrends.count { it.totalMinutes > 0 }
                val consistentDays = weeklyTrends.count { it.totalHours >= dailyGoalHours }
                val consistencyPct = ((consistentDays / 7.0) * 100).toInt()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryMiniCard(
                        title = "Active Streak",
                        value = "$activeDaysCount / 7 Days",
                        subtitle = "consistent streak 🔥",
                        icon = Icons.Default.LocalFireDepartment,
                        color = Color(0xFFEF4444),
                        containerColor = Color(0xFFFEE2E2),
                        modifier = Modifier.weight(1f)
                    )

                    SummaryMiniCard(
                        title = "Consistency",
                        value = "$consistencyPct%",
                        subtitle = "met 2h/day goal",
                        icon = Icons.Default.CheckCircle,
                        color = EmeraldTertiary,
                        containerColor = EmeraldTertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )

                    SummaryMiniCard(
                        title = "Daily Average",
                        value = "${String.format("%.1f", (totalWeeklyMins / 60.0) / 7.0)}h",
                        subtitle = "hours / day",
                        icon = Icons.Default.Timeline,
                        color = IndigoPrimary,
                        containerColor = IndigoPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                val mostStudied = studyStats.maxByOrNull { it.totalMinutes }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
