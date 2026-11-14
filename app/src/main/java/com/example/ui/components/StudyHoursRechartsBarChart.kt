package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.hoverable
import com.example.data.local.SubjectMasteryEntity
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.local.CourseEntity
import com.example.data.local.DayStudyTrend
import com.example.data.local.StudyDateRange
import com.example.data.local.SubjectStudyStats
import com.example.data.local.WeeklySubjectContribution
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

enum class ChartViewMode(val label: String) {
    BY_SUBJECT("By Subject"),
    WEEKLY_TREND("Weekly Trend"),
    MONTHLY("Monthly")
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun StudyHoursRechartsBarChart(
    viewModel: CampusLifeViewModel,
    modifier: Modifier = Modifier
) {
    val studyStats by viewModel.studyStatsPerSubject.collectAsState()
    val weeklyTrends by viewModel.weeklyStudyTrends.collectAsState()
    val weeklySubjectContributions by viewModel.weeklySubjectContributions.collectAsState()
    val totalFocusMinutes by viewModel.totalFocusMinutes.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val dailyGoalHours by viewModel.dailyStudyGoalHours.collectAsState()
    val subjectWeeklyGoals by viewModel.subjectWeeklyStudyGoals.collectAsState()
    val selectedSessionTagFilter by viewModel.selectedSessionTagFilter.collectAsState()
    val selectedDateRange by viewModel.selectedDateRange.collectAsState()
    val currentSemester by viewModel.currentSemester.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val allStudySessions by viewModel.allStudySessions.collectAsState()
    val previousWeeklyTrends by viewModel.previousWeeklyStudyTrends.collectAsState()
    val previousSubjectStats by viewModel.previousWeekSubjectStats.collectAsState()
    val weekComparisonSummary by viewModel.weekComparisonSummary.collectAsState()
    val subjectPriorities by viewModel.subjectPriorities.collectAsState()
    val subjectMasteryRecords by viewModel.subjectMasteryRecords.collectAsState()
    val weeklyDayNotes by viewModel.weeklyDayNotes.collectAsState()
    val compareSubjectsEnabled by viewModel.compareSubjectsEnabled.collectAsState()
    val subjectComparisonReport by viewModel.subjectComparisonReport.collectAsState()
    val monthlyTrendReport by viewModel.monthlyTrendReport.collectAsState()
    val monthlySubjectFilter by viewModel.monthlySubjectFilter.collectAsState()
    val context = LocalContext.current
    val currentDayKey = remember { viewModel.getCurrentDayKey() }
    val currentMonthKey = remember { SimpleDateFormat("MMM", Locale.US).format(Date()) }

    var viewMode by remember { mutableStateOf(ChartViewMode.BY_SUBJECT) }
    var showTargetOverlay by remember { mutableStateOf(true) }
    var showWeeklySubjectBreakdown by remember { mutableStateOf(false) }
    var selectedWeeklySubjectFilter by remember { mutableStateOf<String?>(null) }
    var compareWithPreviousWeek by remember { mutableStateOf(false) }
    var onlyHighPriorityFilter by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showGoalSettingsModal by remember { mutableStateOf(false) }
    var showSubjectGoalsModal by remember { mutableStateOf(false) }
    var preselectedSubjectForGoalModal by remember { mutableStateOf<String?>(null) }

    var selectedStat by remember { mutableStateOf<SubjectStudyStats?>(null) }
    var hoveredStat by remember { mutableStateOf<SubjectStudyStats?>(null) }
    var selectedDayTrend by remember { mutableStateOf<DayStudyTrend?>(null) }

    fun getSubjectColor(courseCode: String): Color {
        return when (courseCode) {
            "CS201" -> IndigoPrimary
            "CS310" -> VioletSecondary
            "MATH204" -> EmeraldTertiary
            "PHY102" -> Color(0xFF0284C7)
            "ENG215" -> Color(0xFFEA580C)
            else -> when (Math.abs(courseCode.hashCode()) % 5) {
                0 -> IndigoPrimary
                1 -> VioletSecondary
                2 -> EmeraldTertiary
                3 -> Color(0xFF0284C7)
                else -> Color(0xFFEA580C)
            }
        }
    }

    fun getPriorityForCourse(courseCode: String): String {
        return subjectPriorities[courseCode] ?: viewModel.getSubjectPriority(courseCode)
    }

    fun getGoalForCourse(courseCode: String): Double {
        val weeklyBase = subjectWeeklyGoals[courseCode] ?: viewModel.defaultSubjectWeeklyGoals[courseCode] ?: 5.0
        return weeklyBase * selectedDateRange.goalScaleMultiplier
    }

    val periodBucketGoalHours = when (selectedDateRange) {
        StudyDateRange.LAST_7_DAYS -> dailyGoalHours
        StudyDateRange.THIS_MONTH -> dailyGoalHours * 3.0
        StudyDateRange.SEMESTER -> dailyGoalHours * 7.0
    }
    val periodGoalUnitLabel = when (selectedDateRange) {
        StudyDateRange.LAST_7_DAYS -> "h/day"
        StudyDateRange.THIS_MONTH -> "h/wk"
        StudyDateRange.SEMESTER -> "h/mo"
    }

    val displayedStudyStats = remember(studyStats, onlyHighPriorityFilter, subjectPriorities) {
        if (onlyHighPriorityFilter) {
            studyStats.filter { getPriorityForCourse(it.courseCode).equals("High", ignoreCase = true) }
        } else {
            studyStats
        }
    }

    val activeSelectedStat = selectedStat?.let { sel ->
        displayedStudyStats.firstOrNull { it.courseCode == sel.courseCode } ?: sel
    }
    val activeHoveredStat = hoveredStat?.let { hov ->
        displayedStudyStats.firstOrNull { it.courseCode == hov.courseCode } ?: hov
    }
    val activeSelectedDayTrend = selectedDayTrend?.let { sel ->
        weeklyTrends.firstOrNull { it.dayKey == sel.dayKey } ?: sel
    }

    val monthlyWeeklyGoalHours = if (monthlySubjectFilter != null) {
        subjectWeeklyGoals[monthlySubjectFilter] ?: viewModel.defaultSubjectWeeklyGoals[monthlySubjectFilter] ?: 5.0
    } else {
        dailyGoalHours * 3.0
    }

    val quickAddDayKey = if (selectedDateRange == StudyDateRange.LAST_7_DAYS) {
        activeSelectedDayTrend?.dayKey ?: currentDayKey
    } else {
        currentDayKey
    }
    val quickAddCourseCode = when (viewMode) {
        ChartViewMode.BY_SUBJECT -> {
            activeSelectedStat?.courseCode
                ?: activeHoveredStat?.courseCode
                ?: displayedStudyStats.firstOrNull()?.courseCode
                ?: courses.firstOrNull()?.code
                ?: "CS201"
        }
        ChartViewMode.MONTHLY -> {
            monthlySubjectFilter
                ?: monthlyTrendReport.weeks.lastOrNull()?.primarySubject?.takeIf { it.isNotBlank() && it != "General" }
                ?: courses.firstOrNull()?.code
                ?: "CS201"
        }
        ChartViewMode.WEEKLY_TREND -> {
            selectedWeeklySubjectFilter
                ?: activeSelectedDayTrend?.primarySubject?.takeIf { it.isNotBlank() && it != "General" }
                ?: courses.firstOrNull()?.code
                ?: "CS201"
        }
    }
    val quickAddDateDisplay = if (
        viewMode == ChartViewMode.BY_SUBJECT ||
        viewMode == ChartViewMode.MONTHLY ||
        selectedDateRange != StudyDateRange.LAST_7_DAYS ||
        quickAddDayKey == currentDayKey
    ) {
        "Today"
    } else {
        quickAddDayKey
    }

    val subjectsBelow50Pct = remember(displayedStudyStats, subjectWeeklyGoals, selectedDateRange) {
        displayedStudyStats.filter { stat ->
            val goal = getGoalForCourse(stat.courseCode)
            stat.totalHours < (goal * 0.5)
        }
    }

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
                                    when (viewMode) {
                                        ChartViewMode.WEEKLY_TREND -> Icons.Default.TrendingUp
                                        ChartViewMode.MONTHLY -> Icons.Default.ShowChart
                                        ChartViewMode.BY_SUBJECT -> Icons.Default.BarChart
                                    },
                                    contentDescription = null,
                                    tint = IndigoPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = when (viewMode) {
                                ChartViewMode.WEEKLY_TREND -> when (selectedDateRange) {
                                    StudyDateRange.LAST_7_DAYS -> "Weekly Consistency Trend"
                                    StudyDateRange.THIS_MONTH -> "Monthly Study Trend"
                                    StudyDateRange.SEMESTER -> "Semester Study Trend"
                                }
                                ChartViewMode.MONTHLY -> "Monthly 4-Week Trend"
                                ChartViewMode.BY_SUBJECT -> "Subject Analytics: Study Hours"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = when (viewMode) {
                            ChartViewMode.WEEKLY_TREND -> {
                                val baseDesc = when (selectedDateRange) {
                                    StudyDateRange.LAST_7_DAYS -> "Last 7 Days • Day-by-day consistency"
                                    StudyDateRange.THIS_MONTH -> "This Month (30d) • Weekly breakdown"
                                    StudyDateRange.SEMESTER -> "${currentSemester?.name ?: "Semester"} • Monthly progression"
                                }
                                if (selectedSessionTagFilter == "All") baseDesc else "$baseDesc ($selectedSessionTagFilter)"
                            }
                            ChartViewMode.MONTHLY -> {
                                val subjectPart = monthlySubjectFilter?.let { "$it • " } ?: ""
                                val baseDesc = "${subjectPart}Wk 1–Wk 4 weekly totals • Long-term trend"
                                if (selectedSessionTagFilter == "All") baseDesc else "$baseDesc ($selectedSessionTagFilter)"
                            }
                            ChartViewMode.BY_SUBJECT -> {
                                val baseDesc = "${selectedDateRange.label} • Room study analytics"
                                if (selectedSessionTagFilter == "All") baseDesc else "$baseDesc • '$selectedSessionTagFilter'"
                            }
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(100),
                    color = EmeraldTertiaryContainer
                ) {
                    Text(
                        text = if (viewMode == ChartViewMode.MONTHLY) {
                            "${String.format(Locale.US, "%.1f", monthlyTrendReport.totalMonthHours)}h • 4W"
                        } else if (selectedSessionTagFilter == "All") {
                            "${String.format(Locale.US, "%.1f", totalFocusMinutes / 60.0)}h • ${selectedDateRange.shortLabel}"
                        } else {
                            "${String.format(Locale.US, "%.1f", totalFocusMinutes / 60.0)}h ($selectedSessionTagFilter)"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldOnTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Date Range Selector ('Last 7 Days', 'This Month', 'Semester')
            AnimatedVisibility(
                visible = viewMode != ChartViewMode.MONTHLY,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Date Range:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (selectedDateRange == StudyDateRange.SEMESTER && currentSemester != null) {
                        Text(
                            text = "• ${currentSemester!!.name}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = IndigoPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        .padding(4.dp)
                        .testTag("date_range_selector"),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StudyDateRange.values().forEach { range ->
                        val isSelected = selectedDateRange == range
                        val rangeIcon = when (range) {
                            StudyDateRange.LAST_7_DAYS -> Icons.Default.DateRange
                            StudyDateRange.THIS_MONTH -> Icons.Default.CalendarMonth
                            StudyDateRange.SEMESTER -> Icons.Default.School
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) IndigoPrimary else Color.Transparent,
                            shadowElevation = if (isSelected) 2.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.selectDateRange(range)
                                    selectedStat = null
                                    hoveredStat = null
                                    selectedDayTrend = null
                                }
                                .testTag("date_range_${range.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = rangeIcon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = range.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
            }

            // Quick Add +30m Header Action Bar
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = IndigoPrimaryContainer.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.2f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chart_quick_add_header_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(IndigoPrimary.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = IndigoPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = when (viewMode) {
                                    ChartViewMode.BY_SUBJECT -> "Focused Subject: $quickAddCourseCode"
                                    ChartViewMode.MONTHLY -> "4-Week Trend (Wk 4): $quickAddCourseCode"
                                    ChartViewMode.WEEKLY_TREND -> if (selectedDateRange == StudyDateRange.LAST_7_DAYS) {
                                        val todayMarker = if (quickAddDayKey == currentDayKey) " (Today)" else ""
                                        "Focused Day: $quickAddDayKey$todayMarker"
                                    } else {
                                        "Active Range: ${selectedDateRange.label}"
                                    }
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = when (viewMode) {
                                    ChartViewMode.BY_SUBJECT -> {
                                        val focusedHours = displayedStudyStats.firstOrNull { it.courseCode == quickAddCourseCode }?.totalHours ?: 0.0
                                        "${String.format(Locale.US, "%.1f", focusedHours)}h in ${selectedDateRange.label} • Tap +30m"
                                    }
                                    ChartViewMode.MONTHLY -> {
                                        val w4Hours = monthlyTrendReport.weeks.lastOrNull()?.totalHours ?: 0.0
                                        "Wk 4: ${String.format(Locale.US, "%.1f", w4Hours)}h • Logs +30m to $quickAddCourseCode"
                                    }
                                    ChartViewMode.WEEKLY_TREND -> if (selectedDateRange == StudyDateRange.LAST_7_DAYS) {
                                        val dayHours = weeklyTrends.firstOrNull { it.dayKey == quickAddDayKey }?.totalHours ?: 0.0
                                        "${String.format(Locale.US, "%.1f", dayHours)}h logged • Logs to $quickAddCourseCode"
                                    } else {
                                        val totalRangeHours = weeklyTrends.sumOf { it.totalMinutes } / 60.0
                                        "${String.format(Locale.US, "%.1f", totalRangeHours)}h total • Logs +30m to $quickAddCourseCode"
                                    }
                                },
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            viewModel.quickAddThirtyMinutes(
                                courseCode = quickAddCourseCode,
                                dateDisplay = quickAddDateDisplay
                            )
                            val targetLabel = if (viewMode != ChartViewMode.WEEKLY_TREND || selectedDateRange != StudyDateRange.LAST_7_DAYS) {
                                quickAddCourseCode
                            } else {
                                "$quickAddDayKey ($quickAddCourseCode)"
                            }
                            Toast.makeText(
                                context,
                                "+30m study time added for $targetLabel",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(100),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        modifier = Modifier
                            .heightIn(min = 38.dp)
                            .testTag("btn_quick_add_30m")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreTime,
                            contentDescription = "Quick Add 30 minutes",
                            modifier = Modifier.size(15.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Quick Add +30m",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Interactive View Mode Switcher Toggle & Option Controls
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 3-Mode View Switcher: By Subject | Weekly Trend | Monthly
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(100))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(3.dp)
                            .testTag("chart_view_mode_switcher"),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ChartViewMode.values().forEach { mode ->
                            val isSelected = viewMode == mode
                            val dynamicModeLabel = when (mode) {
                                ChartViewMode.WEEKLY_TREND -> when (selectedDateRange) {
                                    StudyDateRange.LAST_7_DAYS -> "Weekly Trend"
                                    StudyDateRange.THIS_MONTH -> "Monthly Trend"
                                    StudyDateRange.SEMESTER -> "Semester Trend"
                                }
                                ChartViewMode.MONTHLY -> "Monthly"
                                ChartViewMode.BY_SUBJECT -> mode.label
                            }
                            Surface(
                                shape = RoundedCornerShape(100),
                                color = if (isSelected) IndigoPrimary else Color.Transparent,
                                shadowElevation = if (isSelected) 2.dp else 0.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(100))
                                    .clickable {
                                        viewMode = mode
                                        selectedStat = null
                                        selectedDayTrend = null
                                        if (mode != ChartViewMode.WEEKLY_TREND) {
                                            selectedWeeklySubjectFilter = null
                                        }
                                    }
                                    .testTag("toggle_chart_${mode.name.lowercase()}")
                            ) {
                                Text(
                                    text = dynamicModeLabel,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Button to open Goal Customization Modal
                            FilledTonalIconButton(
                                onClick = {
                                    if (viewMode == ChartViewMode.WEEKLY_TREND || viewMode == ChartViewMode.MONTHLY) {
                                        showGoalSettingsModal = true
                                    } else {
                                        preselectedSubjectForGoalModal = selectedStat?.courseCode
                                        showSubjectGoalsModal = true
                                    }
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("btn_open_goal_settings_modal")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = if (viewMode == ChartViewMode.BY_SUBJECT) "Customize subject study goals" else "Customize daily study goal",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            if (viewMode == ChartViewMode.BY_SUBJECT) {
                                Button(
                                    onClick = {
                                        preselectedSubjectForGoalModal = selectedStat?.courseCode
                                        showSubjectGoalsModal = true
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(100),
                                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                    modifier = Modifier
                                        .heightIn(min = 32.dp)
                                        .testTag("btn_edit_goals")
                                        .testTag("btn_open_subject_goals_modal")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Goals",
                                        modifier = Modifier.size(13.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Edit Goals",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Export Study Logs Button (Available across all views)
                            Button(
                                onClick = { showExportDialog = true },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(100),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary),
                                modifier = Modifier
                                    .heightIn(min = 32.dp)
                                    .testTag("btn_export_study_logs")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Export Study Logs",
                                    modifier = Modifier.size(13.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Export",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Session Category / Tag Filter Chips Row
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Filter by Session Tag:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (selectedSessionTagFilter != "All") {
                            Text(
                                text = "Showing '$selectedSessionTagFilter' • Reset",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndigoPrimary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { viewModel.selectSessionTagFilter("All") }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                    .testTag("btn_reset_tag_filter")
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        viewModel.availableSessionTags.forEach { tag ->
                            val isSelected = selectedSessionTagFilter.equals(tag, ignoreCase = true)
                            val tagIcon = when (tag) {
                                "Exam Prep" -> Icons.Default.Quiz
                                "Homework" -> Icons.Default.EditNote
                                "Research" -> Icons.Default.Science
                                "Project" -> Icons.Default.Code
                                "Revision" -> Icons.Default.Refresh
                                else -> Icons.Default.AllInclusive
                            }
                            val tagColor = when (tag) {
                                "Exam Prep" -> IndigoPrimary
                                "Homework" -> EmeraldTertiary
                                "Research" -> Color(0xFF0284C7)
                                "Project" -> VioletSecondary
                                "Revision" -> Color(0xFFEA580C)
                                else -> IndigoPrimary
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.selectSessionTagFilter(tag)
                                    selectedStat = null
                                    selectedDayTrend = null
                                },
                                label = {
                                    Text(
                                        text = tag,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        tagIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                },
                                shape = RoundedCornerShape(100),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = tagColor.copy(alpha = 0.16f),
                                    selectedLabelColor = tagColor,
                                    selectedLeadingIconColor = tagColor
                                ),
                                border = if (isSelected) BorderStroke(1.2.dp, tagColor) else null,
                                modifier = Modifier
                                    .heightIn(min = 32.dp)
                                    .testTag("filter_chip_tag_${tag.lowercase().replace(" ", "_")}")
                            )
                        }
                    }
                }

                // Priority Filter Row (Available in Subject Analytics mode)
                if (viewMode == ChartViewMode.BY_SUBJECT) {
                    val highPriorityCount = remember(studyStats, subjectPriorities) {
                        studyStats.count { getPriorityForCourse(it.courseCode).equals("High", ignoreCase = true) }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Flag,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Filter Priority:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        FilterChip(
                            selected = !onlyHighPriorityFilter,
                            onClick = {
                                onlyHighPriorityFilter = false
                                selectedStat = null
                            },
                            label = { Text("All Priorities (${studyStats.size})", fontSize = 11.sp, fontWeight = if (!onlyHighPriorityFilter) FontWeight.Bold else FontWeight.Medium) },
                            shape = RoundedCornerShape(100),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                selectedLabelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .heightIn(min = 30.dp)
                                .testTag("filter_priority_all")
                        )

                        FilterChip(
                            selected = onlyHighPriorityFilter,
                            onClick = {
                                onlyHighPriorityFilter = true
                                selectedStat = null
                            },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("High Priority Only ($highPriorityCount)", fontSize = 11.sp, fontWeight = if (onlyHighPriorityFilter) FontWeight.Bold else FontWeight.Medium)
                                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(12.dp))
                                }
                            },
                            shape = RoundedCornerShape(100),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFEF4444).copy(alpha = 0.18f),
                                selectedLabelColor = Color(0xFFDC2626),
                                selectedLeadingIconColor = Color(0xFFDC2626)
                            ),
                            border = if (onlyHighPriorityFilter) BorderStroke(1.2.dp, Color(0xFFDC2626)) else null,
                            modifier = Modifier
                                .heightIn(min = 30.dp)
                                .testTag("filter_high_priority_only")
                                .testTag("filter_priority_high")
                        )

                        if (onlyHighPriorityFilter) {
                            Text(
                                text = "Reset",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndigoPrimary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { onlyHighPriorityFilter = false }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                    .testTag("btn_reset_priority_filter")
                            )
                        }
                    }
                }

                // Interactive Filters & Toggles Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Weekly Trend: Subject Breakdown Toggle
                    if (viewMode == ChartViewMode.WEEKLY_TREND) {
                        FilterChip(
                            selected = showWeeklySubjectBreakdown,
                            onClick = {
                                showWeeklySubjectBreakdown = !showWeeklySubjectBreakdown
                                if (!showWeeklySubjectBreakdown) {
                                    selectedWeeklySubjectFilter = null
                                }
                            },
                            label = {
                                Text(
                                    if (showWeeklySubjectBreakdown) "Subject Breakdown: ON" else "Subject Breakdown",
                                    fontSize = 10.sp,
                                    fontWeight = if (showWeeklySubjectBreakdown) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    if (showWeeklySubjectBreakdown) Icons.Default.PieChart else Icons.Default.Category,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                            },
                            shape = RoundedCornerShape(100),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VioletSecondary.copy(alpha = 0.2f),
                                selectedLabelColor = VioletSecondary,
                                selectedLeadingIconColor = VioletSecondary
                            ),
                            modifier = Modifier
                                .heightIn(min = 30.dp)
                                .testTag("toggle_weekly_subject_breakdown")
                        )
                    }

                    // Overlay Goal Target Toggle
                    FilterChip(
                        selected = showTargetOverlay,
                        onClick = { showTargetOverlay = !showTargetOverlay },
                        label = {
                            Text(
                                when (viewMode) {
                                    ChartViewMode.WEEKLY_TREND -> "Target Goal (${String.format(Locale.US, "%.1f", periodBucketGoalHours)}$periodGoalUnitLabel)"
                                    ChartViewMode.MONTHLY -> "Weekly Goal (${String.format(Locale.US, "%.1f", monthlyWeeklyGoalHours)}h/wk)"
                                    ChartViewMode.BY_SUBJECT -> if (activeSelectedStat != null) {
                                        "Goal (${String.format(Locale.US, "%.1f", getGoalForCourse(activeSelectedStat.courseCode))}h: ${activeSelectedStat.courseCode})"
                                    } else {
                                        if (selectedDateRange == StudyDateRange.LAST_7_DAYS) "Subject Goals"
                                        else "Goals (${selectedDateRange.goalScaleMultiplier.toInt()}x weekly)"
                                    }
                                },
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
                            .heightIn(min = 30.dp)
                            .testTag("toggle_target_overlay")
                    )

                    // Compare Previous Period Toggle (By Subject & Weekly Trend)
                    if (viewMode != ChartViewMode.MONTHLY) {
                        FilterChip(
                            selected = compareWithPreviousWeek,
                            onClick = { compareWithPreviousWeek = !compareWithPreviousWeek },
                            label = {
                                val compareLabel = when (selectedDateRange) {
                                    StudyDateRange.LAST_7_DAYS -> if (compareWithPreviousWeek) "vs Previous Week: ON" else "vs Prev Week"
                                    StudyDateRange.THIS_MONTH -> if (compareWithPreviousWeek) "vs Prior Month: ON" else "vs Prior 30d"
                                    StudyDateRange.SEMESTER -> if (compareWithPreviousWeek) "vs Baseline: ON" else "vs Baseline"
                                }
                                Text(
                                    compareLabel,
                                    fontSize = 10.sp,
                                    fontWeight = if (compareWithPreviousWeek) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    if (compareWithPreviousWeek) Icons.Default.CompareArrows else Icons.Default.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                            },
                            shape = RoundedCornerShape(100),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7).copy(alpha = 0.18f),
                                selectedLabelColor = Color(0xFF0284C7),
                                selectedLeadingIconColor = Color(0xFF0284C7)
                            ),
                            border = if (compareWithPreviousWeek) BorderStroke(1.2.dp, Color(0xFF0284C7)) else null,
                            modifier = Modifier
                                .heightIn(min = 30.dp)
                                .testTag("toggle_compare_previous_week")
                        )
                    }

                    // Compare Subjects Toggle (Subject Analytics View)
                    if (viewMode == ChartViewMode.BY_SUBJECT) {
                        FilterChip(
                            selected = compareSubjectsEnabled,
                            onClick = { viewModel.toggleCompareSubjects() },
                            label = {
                                Text(
                                    text = if (compareSubjectsEnabled) "Compare Subjects: ON" else "Compare Subjects",
                                    fontSize = 10.sp,
                                    fontWeight = if (compareSubjectsEnabled) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Compare,
                                    contentDescription = "Compare Subjects",
                                    modifier = Modifier.size(13.dp)
                                )
                            },
                            shape = RoundedCornerShape(100),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VioletSecondary.copy(alpha = 0.18f),
                                selectedLabelColor = VioletSecondary,
                                selectedLeadingIconColor = VioletSecondary
                            ),
                            border = if (compareSubjectsEnabled) BorderStroke(1.2.dp, VioletSecondary) else null,
                            modifier = Modifier
                                .heightIn(min = 30.dp)
                                .testTag("toggle_compare_subjects")
                        )
                    }
                }
            }

            // Comparative Summary Banner
            AnimatedVisibility(
                visible = compareWithPreviousWeek && viewMode != ChartViewMode.MONTHLY,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (weekComparisonSummary.isIncreased) EmeraldTertiaryContainer.copy(alpha = 0.5f) else WarningContainer.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, if (weekComparisonSummary.isIncreased) EmeraldTertiary.copy(alpha = 0.35f) else WarningAmber.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("week_comparison_summary_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(if (weekComparisonSummary.isIncreased) EmeraldTertiary.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (weekComparisonSummary.isIncreased) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = if (weekComparisonSummary.isIncreased) EmeraldTertiary else WarningAmber,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = when (selectedDateRange) {
                                        StudyDateRange.LAST_7_DAYS -> "Week-over-Week Performance"
                                        StudyDateRange.THIS_MONTH -> "Month-over-Month Performance"
                                        StudyDateRange.SEMESTER -> "Semester vs Baseline Performance"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${selectedDateRange.label}: ${String.format(Locale.US, "%.1f", weekComparisonSummary.currentWeekTotalHours)}h • Prior: ${String.format(Locale.US, "%.1f", weekComparisonSummary.previousWeekTotalHours)}h",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(100),
                            color = if (weekComparisonSummary.isIncreased) EmeraldTertiary else WarningAmber
                        ) {
                            Text(
                                text = "${if (weekComparisonSummary.isIncreased) "+" else ""}${String.format(Locale.US, "%.1f", weekComparisonSummary.percentChange)}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Conditional Alert Indicator Banner for Subjects Below 50% of Goal
            AnimatedVisibility(
                visible = viewMode == ChartViewMode.BY_SUBJECT && subjectsBelow50Pct.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ErrorContainer.copy(alpha = 0.75f),
                    border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chart_low_study_alert_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(ErrorRed.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Alert",
                                    tint = ErrorRed,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(
                                    text = "${subjectsBelow50Pct.size} Subject${if (subjectsBelow50Pct.size > 1) "s" else ""} Below 50% Target",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnErrorContainer
                                )
                                Text(
                                    text = "Needs attention: ${subjectsBelow50Pct.joinToString(", ") { 
                                        val g = getGoalForCourse(it.courseCode)
                                        "${it.courseCode} (${String.format(Locale.US, "%.1f", it.totalHours)}h / ${String.format(Locale.US, "%.1f", g)}h goal)"
                                    }}",
                                    fontSize = 10.sp,
                                    color = OnErrorContainer.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(100),
                            color = ErrorRed,
                            modifier = Modifier.clickable {
                                preselectedSubjectForGoalModal = subjectsBelow50Pct.firstOrNull()?.courseCode
                                showSubjectGoalsModal = true
                            }
                        ) {
                            Text(
                                text = "Adjust Goals",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Dual-Subject Overlay Panel for identifying efficiency gaps
            AnimatedVisibility(
                visible = viewMode == ChartViewMode.BY_SUBJECT && compareSubjectsEnabled && subjectComparisonReport != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                subjectComparisonReport?.let { report ->
                    SubjectComparisonOverlaySection(
                        report = report,
                        availableCourses = courses,
                        studyStats = studyStats,
                        masteryMap = subjectMasteryRecords,
                        defaultMasteryProvider = viewModel::getDefaultMasteryForCourse,
                        dateRange = selectedDateRange,
                        onSelectSubjectA = viewModel::selectCompareSubjectA,
                        onSelectSubjectB = viewModel::selectCompareSubjectB,
                        onSwapSubjects = viewModel::swapCompareSubjects,
                        onBoostLaggingSubject = { laggingCode ->
                            viewModel.quickAddThirtyMinutes(laggingCode, "Today")
                            Toast.makeText(
                                context,
                                "+30m added to $laggingCode to close efficiency gap",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }

            // Legend Row with Conditional Alert Indicator (By Subject & Weekly Trend)
            if (viewMode != ChartViewMode.MONTHLY) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Primary Bar Legend
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (viewMode == ChartViewMode.WEEKLY_TREND) EmeraldTertiary else IndigoPrimary)
                    )
                    Text(
                        if (compareWithPreviousWeek) selectedDateRange.label
                        else if (viewMode == ChartViewMode.WEEKLY_TREND) {
                            when (selectedDateRange) {
                                StudyDateRange.LAST_7_DAYS -> "Daily Study"
                                StudyDateRange.THIS_MONTH -> "Weekly Total"
                                StudyDateRange.SEMESTER -> "Monthly Total"
                            }
                        } else "≥50% Goal",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Comparison Bar Legend when compareWithPreviousWeek is enabled
                if (compareWithPreviousWeek) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.testTag("legend_previous_week_indicator")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF94A3B8))
                        )
                        Text(
                            when (selectedDateRange) {
                                StudyDateRange.LAST_7_DAYS -> "Previous Week"
                                StudyDateRange.THIS_MONTH -> "Prior 30 Days"
                                StudyDateRange.SEMESTER -> "Term Baseline"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Alert Bar Legend for Subject Mode
                if (viewMode == ChartViewMode.BY_SUBJECT) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Brush.verticalGradient(listOf(Color(0xFFFB923C), ErrorRed)))
                        )
                        Text(
                            "<50% Alert ⚠️",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ErrorRed
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(100))
                                .background(Brush.horizontalGradient(listOf(Color(0xFF818CF8), IndigoPrimary)))
                        )
                        Text(
                            "Goal Progress",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Target Reference Lines Legend
                if (showTargetOverlay) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(
                            modifier = Modifier
                                .width(14.dp)
                                .height(2.dp)
                                .background(WarningAmber)
                        )
                        Text(
                            if (viewMode == ChartViewMode.WEEKLY_TREND) {
                                "${String.format(Locale.US, "%.1f", periodBucketGoalHours)}$periodGoalUnitLabel Goal"
                            } else if (activeSelectedStat != null) {
                                val g = getGoalForCourse(activeSelectedStat.courseCode)
                                "${activeSelectedStat.courseCode} Goal: ${String.format(Locale.US, "%.1f", g)}h (50% @ ${String.format(Locale.US, "%.1f", g * 0.5)}h)"
                            } else {
                                "${selectedDateRange.label} Targets (50% threshold)"
                            },
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            }

            // Detailed Breakdown Card for Subject Mode (Revealed on subject selection/tap)
            val activeTooltipStat = activeSelectedStat
            AnimatedVisibility(
                visible = viewMode == ChartViewMode.BY_SUBJECT && activeTooltipStat != null,
                enter = fadeIn(animationSpec = tween(180)),
                exit = fadeOut(animationSpec = tween(180))
            ) {
                activeTooltipStat?.let { stat ->
                    val courseName = courses.firstOrNull { it.code == stat.courseCode }?.name ?: "Subject Course"
                    val customGoal = getGoalForCourse(stat.courseCode)
                    val targetThreshold50Pct = customGoal * 0.5
                    val progressPct = ((stat.totalHours / customGoal) * 100).toInt()
                    val isBelow50 = stat.totalHours < targetThreshold50Pct
                    val isGoalMet = stat.totalHours >= customGoal
                    val deficitHours = (targetThreshold50Pct - stat.totalHours).coerceAtLeast(0.0)

                    // Sessions & Average Duration calculations
                    val subjectSessions = allStudySessions.filter { it.courseCode == stat.courseCode }
                    val totalSessions = if (stat.sessionCount > 0) stat.sessionCount else subjectSessions.size
                    val avgDurationMins = if (totalSessions > 0) (stat.totalMinutes / totalSessions) else 0

                    // Pending assignments for this course
                    val courseTasks = tasks.filter { it.courseCode == stat.courseCode }
                    val pendingAssignments = courseTasks.filter { !it.isCompleted }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recharts_subject_tooltip"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
                        border = BorderStroke(
                            1.5.dp,
                            if (isGoalMet) EmeraldTertiary.copy(alpha = 0.8f)
                            else if (isBelow50) ErrorRed.copy(alpha = 0.7f)
                            else IndigoPrimary.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isGoalMet) EmeraldTertiary else if (isBelow50) ErrorRed else IndigoPrimary
                                    ) {
                                        Text(
                                            stat.courseCode,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            courseName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (selectedSessionTagFilter != "All") {
                                            Text(
                                                "Category Tag: $selectedSessionTagFilter",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = IndigoPrimary
                                            )
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(100),
                                        color = if (hoveredStat != null && selectedStat?.courseCode != stat.courseCode) {
                                            IndigoPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    ) {
                                        Text(
                                            text = if (hoveredStat != null && selectedStat?.courseCode != stat.courseCode) "Mouseover Active" else "Pinned",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (hoveredStat != null && selectedStat?.courseCode != stat.courseCode) IndigoOnPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(100),
                                        color = if (isGoalMet) EmeraldTertiaryContainer else if (isBelow50) ErrorContainer else IndigoPrimaryContainer
                                    ) {
                                        Text(
                                            text = if (isGoalMet) "Goal Met 🎉" else if (isBelow50) "<50% Behind ⚠️" else "$progressPct% of Goal",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isGoalMet) EmeraldOnTertiaryContainer else if (isBelow50) OnErrorContainer else IndigoOnPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            preselectedSubjectForGoalModal = stat.courseCode
                                            showSubjectGoalsModal = true
                                        },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .testTag("btn_edit_goal_${stat.courseCode}")
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit subject goal",
                                            tint = IndigoPrimary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            selectedStat = null
                                            hoveredStat = null
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Close breakdown tooltip",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }

                            // 3 Metric Cards: Total Study Sessions | Average Duration | Pending Assignments
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Metric 1: Total Study Sessions
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("metric_total_sessions_${stat.courseCode}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(13.dp), tint = IndigoPrimary)
                                            Text("Sessions", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text(
                                            "$totalSessions total",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            "${String.format(Locale.US, "%.1f", stat.totalHours)}h focus",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Metric 2: Average Duration
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("metric_avg_duration_${stat.courseCode}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Default.HourglassBottom, contentDescription = null, modifier = Modifier.size(13.dp), tint = VioletSecondary)
                                            Text("Avg Duration", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text(
                                            "${avgDurationMins} min",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            "per session",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Metric 3: Pending Assignments
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (pendingAssignments.isNotEmpty()) WarningContainer.copy(alpha = 0.6f) else EmeraldTertiaryContainer.copy(alpha = 0.6f),
                                    border = BorderStroke(1.dp, if (pendingAssignments.isNotEmpty()) WarningAmber.copy(alpha = 0.4f) else EmeraldTertiary.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("metric_pending_assignments_${stat.courseCode}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(
                                                Icons.Default.Assignment,
                                                contentDescription = null,
                                                modifier = Modifier.size(13.dp),
                                                tint = if (pendingAssignments.isNotEmpty()) OnWarningContainer else EmeraldTertiary
                                            )
                                            Text("Pending Tasks", fontSize = 10.sp, color = if (pendingAssignments.isNotEmpty()) OnWarningContainer else EmeraldOnTertiaryContainer)
                                        }
                                        Text(
                                            "${pendingAssignments.size} pending",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (pendingAssignments.isNotEmpty()) OnWarningContainer else EmeraldOnTertiaryContainer
                                        )
                                        Text(
                                            if (pendingAssignments.isEmpty()) "All caught up ✓" else "Needs work",
                                            fontSize = 9.sp,
                                            color = if (pendingAssignments.isNotEmpty()) OnWarningContainer.copy(alpha = 0.8f) else EmeraldOnTertiaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }

                            // Week-over-Week Subject Performance Card when comparison toggle is active
                            if (compareWithPreviousWeek) {
                                val prevSubject = previousSubjectStats.firstOrNull { it.courseCode == stat.courseCode }
                                val prevHours = prevSubject?.totalHours ?: 0.0
                                val deltaHours = stat.totalHours - prevHours
                                val isIncreased = deltaHours >= 0
                                val pctDelta = if (prevHours > 0) ((deltaHours / prevHours) * 100).toInt() else if (stat.totalHours > 0) 100 else 0

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isIncreased) EmeraldTertiaryContainer.copy(alpha = 0.5f) else WarningContainer.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, if (isIncreased) EmeraldTertiary.copy(alpha = 0.4f) else WarningAmber.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("subject_comparison_card_${stat.courseCode}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(
                                                imageVector = if (isIncreased) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = if (isIncreased) EmeraldTertiary else WarningAmber
                                            )
                                            Column {
                                                Text(
                                                    text = "Vs Previous Week (${stat.courseCode})",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "This Week: ${String.format(Locale.US, "%.1f", stat.totalHours)}h • Prev Week: ${String.format(Locale.US, "%.1f", prevHours)}h (${if (isIncreased) "+" else ""}${String.format(Locale.US, "%.1f", deltaHours)}h)",
                                                    fontSize = 9.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(100),
                                            color = if (isIncreased) EmeraldTertiary else WarningAmber
                                        ) {
                                            Text(
                                                text = "${if (isIncreased) "+" else ""}$pctDelta%",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Progress Bar toward this subject's custom target
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${selectedDateRange.label} Target Progress (${String.format(Locale.US, "%.1f", customGoal)}h goal)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", stat.totalHours)} / ${String.format(Locale.US, "%.1f", customGoal)} hrs ($progressPct%)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isGoalMet) EmeraldTertiary else if (isBelow50) ErrorRed else IndigoPrimary
                                    )
                                }

                                val progressFraction = (stat.totalHours / customGoal).toFloat().coerceIn(0f, 1f)
                                LinearProgressIndicator(
                                    progress = { progressFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(100)),
                                    color = if (isGoalMet) EmeraldTertiary else if (isBelow50) ErrorRed else IndigoPrimary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                if (isBelow50) {
                                    Text(
                                        text = "Alert: ${String.format(Locale.US, "%.1f", deficitHours)}h below 50% target threshold",
                                        fontSize = 9.sp,
                                        color = ErrorRed,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Detailed Pending Assignments List
                            if (pendingAssignments.isNotEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Pending Assignments for ${stat.courseCode} (${pendingAssignments.size}):",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    pendingAssignments.take(3).forEach { task ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                            border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(
                                                        Icons.Default.PendingActions,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(12.dp),
                                                        tint = if (task.priority == "URGENT" || task.priority == "HIGH") ErrorRed else WarningAmber
                                                    )
                                                    Text(
                                                        text = task.title,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = if (task.priority == "URGENT" || task.priority == "HIGH") ErrorContainer else WarningContainer
                                                    ) {
                                                        Text(
                                                            text = task.priority,
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (task.priority == "URGENT" || task.priority == "HIGH") OnErrorContainer else OnWarningContainer,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                    Text(
                                                        text = "Due ${task.dueDate}",
                                                        fontSize = 9.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = EmeraldTertiaryContainer.copy(alpha = 0.35f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldTertiary, modifier = Modifier.size(13.dp))
                                        Text(
                                            "No pending assignments for ${stat.courseCode}! All coursework is up to date.",
                                            fontSize = 10.sp,
                                            color = EmeraldOnTertiaryContainer,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Active Recharts Tooltip for Weekly Trend Mode
            AnimatedVisibility(
                visible = viewMode == ChartViewMode.WEEKLY_TREND && activeSelectedDayTrend != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                activeSelectedDayTrend?.let { day ->
                    val goalPct = if (periodBucketGoalHours > 0) ((day.totalHours / periodBucketGoalHours) * 100).toInt() else 0
                    val courseTitle = courses.firstOrNull { it.code == day.primarySubject }?.name ?: "Course Study"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recharts_weekly_tooltip"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldTertiary.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        "${day.dayLabel} • ${String.format(Locale.US, "%.1f", day.totalHours)} Hours",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (compareWithPreviousWeek) {
                                        val prevDay = previousWeeklyTrends.firstOrNull { it.dayKey == day.dayKey }
                                        val prevDayH = prevDay?.totalHours ?: 0.0
                                        val deltaDayH = day.totalHours - prevDayH
                                        val isDayInc = deltaDayH >= 0
                                        Text(
                                            "Prior (${day.dayKey}): ${String.format(Locale.US, "%.1f", prevDayH)}h • Delta: ${if (isDayInc) "+" else ""}${String.format(Locale.US, "%.1f", deltaDayH)}h (${if (isDayInc) "▲" else "▼"})",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isDayInc) EmeraldTertiary else WarningAmber
                                        )
                                    }
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
                                            text = if (goalPct >= 100) "Goal Reached ($goalPct%)" else "$goalPct% of ${String.format(java.util.Locale.US, "%.1f", periodBucketGoalHours)}h goal",
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

                            if (day.quickNote.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = IndigoPrimaryContainer.copy(alpha = 0.7f),
                                    border = BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.3f)),
                                    modifier = Modifier.testTag("tooltip_day_quick_note_${day.dayKey}")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.StickyNote2,
                                            contentDescription = "Quick Note",
                                            tint = IndigoPrimary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "Note: ${day.quickNote}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = IndigoOnPrimaryContainer
                                        )
                                    }
                                }
                            }

                            // Day subject breakdown chips
                            if (day.subjectBreakdown.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    day.subjectBreakdown.entries.filter { it.value > 0 }.sortedByDescending { it.value }.forEach { (sub, mins) ->
                                        val isSubSelected = selectedWeeklySubjectFilter == sub
                                        val subColor = getSubjectColor(sub)
                                        Surface(
                                            shape = RoundedCornerShape(100),
                                            color = if (isSubSelected) subColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(1.dp, if (isSubSelected) subColor else subColor.copy(alpha = 0.35f)),
                                            modifier = Modifier.clickable {
                                                selectedWeeklySubjectFilter = if (isSubSelected) null else sub
                                            }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(subColor)
                                                )
                                                Text(
                                                    text = "$sub: ${String.format(Locale.US, "%.1f", mins / 60.0)}h",
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSubSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSubSelected) subColor else MaterialTheme.colorScheme.onSurface
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

            // Main Chart Viewport
            val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            val goalLineColor = WarningAmber.copy(alpha = 0.85f)

            if (viewMode == ChartViewMode.MONTHLY) {
                MonthlyTrendLineChartSection(
                    report = monthlyTrendReport,
                    courses = courses,
                    weeklyGoalHours = monthlyWeeklyGoalHours,
                    showTargetOverlay = showTargetOverlay,
                    selectedSubjectFilter = monthlySubjectFilter,
                    onSelectSubjectFilter = viewModel::selectMonthlySubjectFilter,
                    subjectColorProvider = ::getSubjectColor
                )
            } else if (viewMode == ChartViewMode.BY_SUBJECT) {
                // By Subject View
                if (displayedStudyStats.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (onlyHighPriorityFilter && studyStats.isNotEmpty()) {
                                "No High Priority subjects to display. You can set subject priority levels in 'Edit Goals'!"
                            } else {
                                "No study sessions logged yet. Complete a Pomodoro block to populate your chart!"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    val maxSubjectGoal = displayedStudyStats.maxOfOrNull { getGoalForCourse(it.courseCode) } ?: 5.0
                    val maxHoursVal = max(
                        max(max(8.0, maxSubjectGoal + 1.5), (displayedStudyStats.maxOfOrNull { it.totalHours } ?: 6.0) + 1.5),
                        if (compareWithPreviousWeek) (previousSubjectStats.maxOfOrNull { it.totalHours } ?: 0.0) + 1.5 else 0.0
                    )

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(310.dp)
                            .testTag("recharts_subject_viewport")
                    ) {
                        val totalWidth = maxWidth
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val bottomAxisY = size.height - 80.dp.toPx()
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

                            // Goal Reference Line for selected subject or overall target
                            if (showTargetOverlay) {
                                val targetGoalHours = if (activeSelectedStat != null) {
                                    getGoalForCourse(activeSelectedStat.courseCode)
                                } else {
                                    displayedStudyStats.map { getGoalForCourse(it.courseCode) }.average().takeIf { !it.isNaN() } ?: 5.0
                                }
                                val goalY = bottomAxisY - (chartHeight * (targetGoalHours.toFloat() / maxHoursVal.toFloat()))
                                if (goalY in topY..bottomAxisY) {
                                    drawLine(
                                        color = goalLineColor,
                                        start = Offset(0f, goalY),
                                        end = Offset(size.width, goalY),
                                        strokeWidth = 1.5.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                                    )
                                }

                                // 50% Target Alert Threshold Reference Line
                                val threshold50Y = bottomAxisY - (chartHeight * ((targetGoalHours * 0.5f).toFloat() / maxHoursVal.toFloat()))
                                if (threshold50Y in topY..bottomAxisY) {
                                    drawLine(
                                        color = ErrorRed.copy(alpha = 0.55f),
                                        start = Offset(0f, threshold50Y),
                                        end = Offset(size.width, threshold50Y),
                                        strokeWidth = 1.2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                                    )
                                }
                            }
                        }

                        // Bars Row
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            displayedStudyStats.forEach { stat ->
                                val isHovered = hoveredStat?.courseCode == stat.courseCode
                                val isSelected = selectedStat?.courseCode == stat.courseCode
                                val isHighlighted = isHovered || isSelected
                                val subjectGoal = getGoalForCourse(stat.courseCode)
                                val targetThreshold50Pct = subjectGoal * 0.5
                                val isBelow50 = stat.totalHours < targetThreshold50Pct
                                val isGoalMet = stat.totalHours >= subjectGoal
                                val pctOfGoal = ((stat.totalHours / subjectGoal) * 100).toInt()
                                val goalProgressFraction = (stat.totalHours / subjectGoal).toFloat().coerceIn(0f, 1f)
                                val animatedGoalProgress by animateFloatAsState(
                                    targetValue = goalProgressFraction,
                                    animationSpec = tween(durationMillis = 800),
                                    label = "progress_${stat.courseCode}"
                                )
                                val barHeightFraction = (stat.totalHours / maxHoursVal).toFloat().coerceIn(0.04f, 1f)
                                val animatedHeight by animateFloatAsState(
                                    targetValue = barHeightFraction,
                                    animationSpec = tween(durationMillis = 800),
                                    label = "bar_${stat.courseCode}"
                                )
                                val animatedScale by animateFloatAsState(
                                    targetValue = if (isHovered) 1.06f else if (isSelected) 1.03f else 1.0f,
                                    animationSpec = tween(durationMillis = 150),
                                    label = "scale_${stat.courseCode}"
                                )

                                val interactionSource = remember(stat.courseCode) { MutableInteractionSource() }
                                val isSystemHovered by interactionSource.collectIsHoveredAsState()

                                LaunchedEffect(isSystemHovered) {
                                    if (isSystemHovered) {
                                        hoveredStat = stat
                                    } else if (hoveredStat?.courseCode == stat.courseCode) {
                                        hoveredStat = null
                                    }
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                        .hoverable(interactionSource = interactionSource)
                                        .pointerInput(stat.courseCode) {
                                            awaitPointerEventScope {
                                                while (true) {
                                                    val event = awaitPointerEvent()
                                                    when (event.type) {
                                                        PointerEventType.Enter, PointerEventType.Move -> {
                                                            hoveredStat = stat
                                                        }
                                                        PointerEventType.Exit -> {
                                                            if (hoveredStat?.courseCode == stat.courseCode) {
                                                                hoveredStat = null
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        .clickable {
                                            selectedStat = if (selectedStat?.courseCode == stat.courseCode) null else stat
                                        }
                                        .testTag("bar_subject_${stat.courseCode}")
                                ) {
                                    // Mini Hover Indicator Pill
                                    if (isHovered) {
                                        Surface(
                                            shape = RoundedCornerShape(100),
                                            color = IndigoPrimary,
                                            modifier = Modifier
                                                .padding(bottom = 2.dp)
                                                .testTag("hover_badge_${stat.courseCode}")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Visibility,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(8.dp)
                                                )
                                                Text(
                                                    text = "Details",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    } else if (isGoalMet) {
                                        Surface(
                                            shape = RoundedCornerShape(100),
                                            color = EmeraldTertiaryContainer,
                                            border = BorderStroke(0.8.dp, EmeraldTertiary.copy(alpha = 0.6f)),
                                            modifier = Modifier
                                                .padding(bottom = 2.dp)
                                                .testTag("goal_met_indicator_${stat.courseCode}")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Target Met",
                                                    tint = EmeraldTertiary,
                                                    modifier = Modifier.size(9.dp)
                                                )
                                                Text(
                                                    text = "100%",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = EmeraldOnTertiaryContainer
                                                )
                                            }
                                        }
                                    } else if (isBelow50) {
                                        Surface(
                                            shape = RoundedCornerShape(100),
                                            color = ErrorContainer,
                                            border = BorderStroke(0.8.dp, ErrorRed.copy(alpha = 0.6f)),
                                            modifier = Modifier
                                                .padding(bottom = 2.dp)
                                                .testTag("alert_indicator_${stat.courseCode}")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = "Below 50% Target Alert",
                                                    tint = ErrorRed,
                                                    modifier = Modifier.size(9.dp)
                                                )
                                                Text(
                                                    text = "<50%",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = OnErrorContainer
                                                )
                                            }
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(100),
                                            color = IndigoPrimaryContainer.copy(alpha = 0.6f),
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        ) {
                                            Text(
                                                text = "$pctOfGoal%",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = IndigoOnPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }

                                    val prevStat = if (compareWithPreviousWeek) {
                                        previousSubjectStats.firstOrNull { it.courseCode == stat.courseCode }
                                    } else null
                                    val prevH = prevStat?.totalHours ?: 0.0
                                    val prevHeightFraction = (prevH / maxHoursVal).toFloat().coerceIn(0.04f, 1f)
                                    val prevAnimatedHeight by animateFloatAsState(
                                        targetValue = prevHeightFraction,
                                        animationSpec = tween(durationMillis = 800),
                                        label = "bar_prev_${stat.courseCode}"
                                    )

                                    // Hours value and target label
                                    if (compareWithPreviousWeek) {
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", stat.totalHours)}h | ${String.format(Locale.US, "%.1f", prevH)}h",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isGoalMet) EmeraldTertiary else if (isBelow50) ErrorRed else if (isHighlighted) IndigoPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "This | Prev",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", stat.totalHours)}h",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isGoalMet) EmeraldTertiary else if (isBelow50) ErrorRed else if (isHighlighted) IndigoPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "/${String.format(Locale.US, "%.0f", subjectGoal)}h",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                    }

                                    // Bar with Gradient reflecting progress towards target & hover scaling
                                    if (compareWithPreviousWeek) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .fillMaxHeight(0.68f),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            // Current Week Bar
                                            Box(
                                                modifier = Modifier
                                                    .width(13.dp)
                                                    .fillMaxHeight(animatedHeight)
                                                    .graphicsLayer {
                                                        scaleX = animatedScale
                                                        scaleY = animatedScale
                                                    }
                                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                    .background(
                                                        Brush.verticalGradient(
                                                            if (isHovered) listOf(Color(0xFF818CF8), IndigoPrimary)
                                                            else if (isSelected) listOf(VioletSecondary, IndigoPrimary)
                                                            else if (isGoalMet) listOf(Color(0xFF34D399), EmeraldTertiary)
                                                            else if (isBelow50) listOf(Color(0xFFFB923C), ErrorRed)
                                                            else listOf(IndigoPrimary, Color(0xFF6366F1))
                                                        )
                                                    )
                                                    .then(
                                                        if (isHovered) Modifier.border(2.dp, IndigoPrimary, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                        else if (isSelected) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                        else if (isBelow50) Modifier.border(1.dp, ErrorRed.copy(alpha = 0.5f), RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                        else Modifier
                                                    )
                                                    .testTag("bar_subject_curr_${stat.courseCode}")
                                            )

                                            Spacer(modifier = Modifier.width(3.dp))

                                            // Previous Week Bar
                                            Box(
                                                modifier = Modifier
                                                    .width(13.dp)
                                                    .fillMaxHeight(prevAnimatedHeight)
                                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                    .background(
                                                        Brush.verticalGradient(
                                                            listOf(Color(0xFF94A3B8), Color(0xFF64748B))
                                                        )
                                                    )
                                                    .testTag("bar_subject_prev_${stat.courseCode}")
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(if (isHighlighted) 0.68f else 0.6f)
                                                .fillMaxHeight(animatedHeight * 0.68f)
                                                .graphicsLayer {
                                                    scaleX = animatedScale
                                                    scaleY = animatedScale
                                                }
                                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                                .background(
                                                    Brush.verticalGradient(
                                                        if (isHovered) {
                                                            listOf(Color(0xFF818CF8), IndigoPrimary)
                                                        } else if (isSelected) {
                                                            listOf(VioletSecondary, IndigoPrimary)
                                                        } else if (isGoalMet) {
                                                            listOf(Color(0xFF34D399), EmeraldTertiary)
                                                        } else if (isBelow50) {
                                                            listOf(Color(0xFFFB923C), ErrorRed)
                                                        } else {
                                                            listOf(IndigoPrimary, Color(0xFF6366F1))
                                                        }
                                                    )
                                                )
                                                .then(
                                                    if (isHovered) Modifier.border(
                                                        2.5.dp,
                                                        IndigoPrimary,
                                                        RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                                    ) else if (isSelected) Modifier.border(
                                                        2.dp,
                                                        if (isGoalMet) EmeraldTertiary else if (isBelow50) ErrorRed else MaterialTheme.colorScheme.primary,
                                                        RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                                    ) else if (isBelow50) Modifier.border(
                                                        1.dp,
                                                        ErrorRed.copy(alpha = 0.5f),
                                                        RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                                    ) else Modifier
                                                )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(5.dp))

                                    // Visual Progress Bar beneath the subject bar
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(if (isHighlighted) 0.88f else 0.82f)
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(100))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
                                            .then(
                                                if (isHighlighted) Modifier.border(1.dp, IndigoPrimary, RoundedCornerShape(100))
                                                else if (isBelow50) Modifier.border(0.8.dp, ErrorRed.copy(alpha = 0.4f), RoundedCornerShape(100))
                                                else Modifier
                                            )
                                            .testTag("progress_bar_subject_${stat.courseCode}")
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(animatedGoalProgress.coerceAtLeast(0.04f))
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(100))
                                                .background(
                                                    Brush.horizontalGradient(
                                                        if (isGoalMet) listOf(Color(0xFF34D399), EmeraldTertiary)
                                                        else if (isBelow50) listOf(Color(0xFFFB923C), ErrorRed)
                                                        else listOf(Color(0xFF818CF8), IndigoPrimary)
                                                    )
                                                )
                                                .testTag("progress_bar_fill_${stat.courseCode}")
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    // Percentage of custom goal completed label
                                    Text(
                                        text = "$pctOfGoal%",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isGoalMet) EmeraldTertiary else if (isBelow50) ErrorRed else if (isHighlighted) IndigoPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.testTag("progress_percent_label_${stat.courseCode}")
                                    )

                                    Spacer(modifier = Modifier.height(3.dp))

                                    // Small line chart visualizing 4-week mastery trend from Room database
                                    val masteryTrend = subjectMasteryRecords[stat.courseCode]
                                        ?: viewModel.getDefaultMasteryForCourse(stat.courseCode)
                                    val barSubjectColor = getSubjectColor(stat.courseCode)

                                    SubjectMasteryMiniLineChart(
                                        masteryRecords = masteryTrend,
                                        subjectColor = barSubjectColor,
                                        isHighlighted = isHighlighted,
                                        modifier = Modifier
                                            .testTag("mastery_line_chart_${stat.courseCode}")
                                            .testTag("mastery_mini_chart_${stat.courseCode}")
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    val compareBadgeTag = if (viewMode == ChartViewMode.BY_SUBJECT && compareSubjectsEnabled && subjectComparisonReport != null) {
                                        when (stat.courseCode) {
                                            subjectComparisonReport?.subjectA?.courseCode -> "A"
                                            subjectComparisonReport?.subjectB?.courseCode -> "B"
                                            else -> null
                                        }
                                    } else null

                                    // Course Code with Conditional Alert Dot or Compare Badge
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        modifier = Modifier.testTag("label_subject_${stat.courseCode}")
                                    ) {
                                        if (compareBadgeTag != null) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (compareBadgeTag == "A") CompareSubjectAColor else CompareSubjectBColor,
                                                modifier = Modifier.testTag("compare_badge_${compareBadgeTag}_${stat.courseCode}")
                                            ) {
                                                Text(
                                                    text = compareBadgeTag,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = stat.courseCode,
                                            fontSize = 11.sp,
                                            fontWeight = if (isHighlighted || isBelow50 || compareBadgeTag != null) FontWeight.ExtraBold else FontWeight.SemiBold,
                                            color = if (compareBadgeTag == "A") CompareSubjectAColor
                                            else if (compareBadgeTag == "B") CompareSubjectBColor
                                            else if (isBelow50) ErrorRed
                                            else if (isHighlighted) IndigoPrimary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (isBelow50 && compareBadgeTag == null) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(ErrorRed)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Small Hover Breakdown Popup Card anchored to the hovered subject bar
                        val activePopupStat = activeHoveredStat
                        if (activePopupStat != null) {
                            val statIndex = displayedStudyStats.indexOfFirst { it.courseCode == activePopupStat.courseCode }
                                if (statIndex >= 0 && displayedStudyStats.isNotEmpty()) {
                                    val colWidth = totalWidth / displayedStudyStats.size
                                    val barCenterX = (colWidth * statIndex) + (colWidth / 2)
                                    val cardWidth = 210.dp
                                    val halfCard = cardWidth / 2
                                    val leftOffset = (barCenterX - halfCard).coerceIn(4.dp, (totalWidth - cardWidth - 4.dp).coerceAtLeast(4.dp))

                                    val courseName = courses.firstOrNull { it.code == activePopupStat.courseCode }?.name ?: "Course Study"
                                    val subjectGoal = getGoalForCourse(activePopupStat.courseCode)
                                    val progressPct = ((activePopupStat.totalHours / subjectGoal) * 100).toInt()
                                    val isMet = activePopupStat.totalHours >= subjectGoal
                                    val isBelow50 = activePopupStat.totalHours < (subjectGoal * 0.5)
                                    val subjectColor = getSubjectColor(activePopupStat.courseCode)

                                    Box(
                                        modifier = Modifier
                                            .offset(x = leftOffset, y = 6.dp)
                                            .width(cardWidth)
                                            .zIndex(30f)
                                            .testTag("subject_hover_popup_card")
                                            .testTag("subject_hover_popup_${activePopupStat.courseCode}")
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            shadowElevation = 8.dp,
                                            border = BorderStroke(1.2.dp, subjectColor.copy(alpha = 0.7f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                // Header: Subject Code, Name & Status Badge
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    ) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = subjectColor
                                                        ) {
                                                            Text(
                                                                text = activePopupStat.courseCode,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = Color.White,
                                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                        Text(
                                                            text = courseName,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        val priority = getPriorityForCourse(activePopupStat.courseCode)
                                                        Surface(
                                                            shape = RoundedCornerShape(100),
                                                            color = when (priority.lowercase(Locale.US)) {
                                                                "high" -> Color(0xFFFEE2E2)
                                                                "medium" -> Color(0xFFFEF3C7)
                                                                else -> Color(0xFFD1FAE5)
                                                            }
                                                        ) {
                                                            Text(
                                                                text = priority,
                                                                fontSize = 8.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = when (priority.lowercase(Locale.US)) {
                                                                    "high" -> Color(0xFFDC2626)
                                                                    "medium" -> Color(0xFFD97706)
                                                                    else -> Color(0xFF059669)
                                                                },
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }

                                                        Surface(
                                                            shape = RoundedCornerShape(100),
                                                            color = if (isMet) EmeraldTertiaryContainer else if (isBelow50) ErrorContainer else IndigoPrimaryContainer
                                                        ) {
                                                            Text(
                                                                text = if (isMet) "Met 🎉" else if (isBelow50) "<50% ⚠️" else "$progressPct%",
                                                                fontSize = 8.5.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isMet) EmeraldOnTertiaryContainer else if (isBelow50) OnErrorContainer else IndigoOnPrimaryContainer,
                                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 0.8.dp)

                                                // 2 Key Breakdown Columns: Total Pomodoro Sessions & Total Hours Logged
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    // Metric 1: Total Pomodoro Sessions
                                                    Column(
                                                        modifier = Modifier.testTag("hover_total_sessions_${activePopupStat.courseCode}")
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                                            Icon(
                                                                Icons.Default.Timer,
                                                                contentDescription = null,
                                                                tint = IndigoPrimary,
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                            Text(
                                                                "Pomodoros",
                                                                fontSize = 9.sp,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                        Text(
                                                            text = "${activePopupStat.sessionCount} sessions",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        val avgMins = if (activePopupStat.sessionCount > 0) activePopupStat.totalMinutes / activePopupStat.sessionCount else 0
                                                        Text(
                                                            text = "~${avgMins}m / session",
                                                            fontSize = 8.5.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }

                                                    // Metric 2: Total Hours Logged
                                                    Column(
                                                        horizontalAlignment = Alignment.End,
                                                        modifier = Modifier.testTag("hover_total_hours_${activePopupStat.courseCode}")
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                                            Icon(
                                                                Icons.Default.Schedule,
                                                                contentDescription = null,
                                                                tint = subjectColor,
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                            Text(
                                                                "Hours Logged",
                                                                fontSize = 9.sp,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                        Text(
                                                            text = "${String.format(Locale.US, "%.1f", activePopupStat.totalHours)} hrs",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = subjectColor
                                                        )
                                                        Text(
                                                            text = "${activePopupStat.totalMinutes} mins total",
                                                            fontSize = 8.5.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                // Progress vs Goal Mini Bar
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "Goal: ${String.format(Locale.US, "%.1f", subjectGoal)}h ($progressPct%)",
                                                        fontSize = 8.5.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.testTag("hover_goal_progress_${activePopupStat.courseCode}")
                                                    )
                                                    val remaining = (subjectGoal - activePopupStat.totalHours).coerceAtLeast(0.0)
                                                    if (remaining > 0) {
                                                        Text(
                                                            text = "${String.format(Locale.US, "%.1f", remaining)}h left",
                                                            fontSize = 8.5.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = if (isBelow50) ErrorRed else IndigoPrimary
                                                        )
                                                    } else {
                                                        Text(
                                                            text = "Goal Completed! 🏆",
                                                            fontSize = 8.5.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = EmeraldTertiary
                                                        )
                                                    }
                                                }

                                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 0.8.dp)

                                                // 4-Week Mastery Trend breakdown from Room Database
                                                val courseMastery = subjectMasteryRecords[activePopupStat.courseCode]
                                                    ?: viewModel.getDefaultMasteryForCourse(activePopupStat.courseCode)

                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .testTag("hover_mastery_trend_${activePopupStat.courseCode}"),
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                                            Icon(Icons.Default.Insights, contentDescription = null, tint = subjectColor, modifier = Modifier.size(11.dp))
                                                            Text("4-Week Mastery (Room DB)", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                        }
                                                        val firstM = courseMastery.firstOrNull()?.masteryPercentage ?: 0.0
                                                        val lastM = courseMastery.lastOrNull()?.masteryPercentage ?: 0.0
                                                        val delta = (lastM - firstM).toInt()
                                                        Text(
                                                            text = "${if (delta >= 0) "+$delta%" else "$delta%"}",
                                                            fontSize = 8.5.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = if (delta >= 0) EmeraldTertiary else ErrorRed
                                                        )
                                                    }

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                    ) {
                                                        courseMastery.sortedBy { it.weekNumber }.forEach { record ->
                                                            val isCurrentWeek = record.weekNumber == 4
                                                            Surface(
                                                                shape = RoundedCornerShape(4.dp),
                                                                color = if (isCurrentWeek) subjectColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                                border = if (isCurrentWeek) BorderStroke(1.dp, subjectColor) else null,
                                                                modifier = Modifier.weight(1f)
                                                            ) {
                                                                Column(
                                                                    modifier = Modifier.padding(vertical = 2.dp),
                                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                                ) {
                                                                    Text(record.weekLabel, fontSize = 7.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                                    Text(
                                                                        "${record.masteryPercentage.toInt()}%",
                                                                        fontSize = 8.5.sp,
                                                                        fontWeight = if (isCurrentWeek) FontWeight.Bold else FontWeight.Medium,
                                                                        color = if (isCurrentWeek) subjectColor else MaterialTheme.colorScheme.onSurface
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
                            }
                        }
                    }
                } else {
                // Trend View: Adapts across Last 7 Days (Mon-Sun), This Month (Wk 1-4), and Semester (Months)
                val maxDailyHours = max(
                    max(max(4.0, periodBucketGoalHours + 1.2), (weeklyTrends.maxOfOrNull { it.totalHours } ?: 3.5) + 0.8),
                    if (compareWithPreviousWeek) (previousWeeklyTrends.maxOfOrNull { it.totalHours } ?: 0.0) + 0.8 else 0.0
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(255.dp)
                        .testTag("recharts_weekly_viewport")
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val bottomAxisY = size.height - 52.dp.toPx()
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

                        // Period Goal Target Reference Line
                        if (showTargetOverlay) {
                            val goalY = bottomAxisY - (chartHeight * (periodBucketGoalHours.toFloat() / maxDailyHours.toFloat()))
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

                    // Dynamic Period Bars
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyTrends.forEach { dayTrend ->
                            val isSelected = selectedDayTrend?.dayKey == dayTrend.dayKey
                            val reachesGoal = dayTrend.totalHours >= periodBucketGoalHours
                            val isToday = when (selectedDateRange) {
                                StudyDateRange.LAST_7_DAYS -> dayTrend.dayKey == currentDayKey
                                StudyDateRange.THIS_MONTH -> dayTrend.dayKey == "Wk 4"
                                StudyDateRange.SEMESTER -> dayTrend.dayKey.equals(currentMonthKey, ignoreCase = true)
                            }

                            // Subject filter logic
                            val subjectFilterMins = if (selectedWeeklySubjectFilter != null) {
                                dayTrend.subjectBreakdown[selectedWeeklySubjectFilter] ?: 0
                            } else null
                            val subjectFilterHours = subjectFilterMins?.let { it / 60.0 }

                            val targetFraction = if (selectedWeeklySubjectFilter != null) {
                                ((subjectFilterHours ?: 0.0) / maxDailyHours).toFloat().coerceIn(if ((subjectFilterMins ?: 0) > 0) 0.06f else 0.02f, 1f)
                            } else {
                                (dayTrend.totalHours / maxDailyHours).toFloat().coerceIn(0.04f, 1f)
                            }

                            val prevDayTrend = if (compareWithPreviousWeek) {
                                previousWeeklyTrends.firstOrNull { it.dayKey == dayTrend.dayKey }
                            } else null
                            val prevDayHours = prevDayTrend?.totalHours ?: 0.0
                            val prevTargetFraction = (prevDayHours / maxDailyHours).toFloat().coerceIn(0.04f, 1f)
                            val prevAnimatedHeight by animateFloatAsState(
                                targetValue = prevTargetFraction,
                                animationSpec = tween(durationMillis = 650),
                                label = "trend_prev_${dayTrend.dayKey}"
                            )

                            val animatedHeight by animateFloatAsState(
                                targetValue = targetFraction,
                                animationSpec = tween(durationMillis = 650),
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
                                val labelText = if (compareWithPreviousWeek) {
                                    "${String.format(Locale.US, "%.1f", dayTrend.totalHours)}h | ${String.format(Locale.US, "%.1f", prevDayHours)}h"
                                } else if (selectedWeeklySubjectFilter != null) {
                                    "${String.format(Locale.US, "%.1f", subjectFilterHours ?: 0.0)}h"
                                } else {
                                    "${String.format(Locale.US, "%.1f", dayTrend.totalHours)}h"
                                }

                                val labelColor = if (compareWithPreviousWeek) {
                                    if (reachesGoal) EmeraldTertiary else if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.onSurface
                                } else if (selectedWeeklySubjectFilter != null) {
                                    if ((subjectFilterMins ?: 0) > 0) getSubjectColor(selectedWeeklySubjectFilter!!)
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                } else if (reachesGoal) EmeraldTertiary else if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.onSurface

                                Text(
                                    text = labelText,
                                    fontSize = if (compareWithPreviousWeek) 9.sp else 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = labelColor,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )

                                // Vertical Bar
                                if (compareWithPreviousWeek) {
                                    // Paired Bars: This Week & Previous Week
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(0.75f),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        // Current Week Bar
                                        Box(
                                            modifier = Modifier
                                                .width(11.dp)
                                                .fillMaxHeight(animatedHeight)
                                                .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
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
                                                        RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)
                                                    ) else Modifier
                                                )
                                                .testTag("bar_day_curr_${dayTrend.dayKey}")
                                        )

                                        Spacer(modifier = Modifier.width(3.dp))

                                        // Previous Week Bar
                                        Box(
                                            modifier = Modifier
                                                .width(11.dp)
                                                .fillMaxHeight(prevAnimatedHeight)
                                                .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(Color(0xFF94A3B8), Color(0xFF64748B))
                                                    )
                                                )
                                                .testTag("bar_day_prev_${dayTrend.dayKey}")
                                        )
                                    }
                                } else if (showWeeklySubjectBreakdown && selectedWeeklySubjectFilter == null) {
                                    // Stacked Subject Breakdown Bar
                                    val validEntries = dayTrend.subjectBreakdown.entries.filter { it.value > 0 }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.65f)
                                            .fillMaxHeight(animatedHeight * 0.75f)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                            .then(
                                                if (isToday) Modifier.border(
                                                    1.5.dp,
                                                    WarningAmber,
                                                    RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                                ) else Modifier
                                            )
                                    ) {
                                        if (validEntries.isNotEmpty() && dayTrend.totalMinutes > 0) {
                                            Column(modifier = Modifier.fillMaxSize()) {
                                                validEntries.forEach { entry ->
                                                    val weight = entry.value.toFloat() / dayTrend.totalMinutes.toFloat()
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .weight(weight.coerceAtLeast(0.01f))
                                                            .background(getSubjectColor(entry.key))
                                                    )
                                                }
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                            )
                                        }
                                    }
                                } else if (selectedWeeklySubjectFilter != null) {
                                    // Single Filtered Subject Bar
                                    val hasHours = (subjectFilterMins ?: 0) > 0
                                    val subColor = getSubjectColor(selectedWeeklySubjectFilter!!)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.65f)
                                            .fillMaxHeight(animatedHeight * 0.75f)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                            .background(
                                                if (hasHours) subColor
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            )
                                            .then(
                                                if (isToday) Modifier.border(
                                                    1.5.dp,
                                                    WarningAmber,
                                                    RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                                ) else Modifier
                                            )
                                    )
                                } else {
                                    // Standard Gradient Bar
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
                                }

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

                                // Fixed-height Quick Note badge slot below every day bar so baselines align
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(18.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (dayTrend.quickNote.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (isSelected) IndigoPrimary else IndigoPrimaryContainer,
                                            border = BorderStroke(
                                                0.6.dp,
                                                if (isSelected) IndigoPrimary else IndigoPrimary.copy(alpha = 0.4f)
                                            ),
                                            modifier = Modifier
                                                .padding(horizontal = 1.dp)
                                                .testTag("day_note_badge_${dayTrend.dayKey}")
                                        ) {
                                            Text(
                                                text = dayTrend.quickNote,
                                                fontSize = 7.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else IndigoOnPrimaryContainer,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Weekly Trend Quick Note Editor Card (shown in Last 7 Days daily view)
            AnimatedVisibility(
                visible = viewMode == ChartViewMode.WEEKLY_TREND && selectedDateRange == StudyDateRange.LAST_7_DAYS,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                var quickNoteTargetDay by remember(currentDayKey) { mutableStateOf(currentDayKey) }
                LaunchedEffect(selectedDayTrend?.dayKey) {
                    selectedDayTrend?.dayKey?.let { quickNoteTargetDay = it }
                }
                var quickNoteInput by remember { mutableStateOf(weeklyDayNotes[quickNoteTargetDay] ?: "") }
                LaunchedEffect(quickNoteTargetDay, weeklyDayNotes) {
                    quickNoteInput = weeklyDayNotes[quickNoteTargetDay] ?: ""
                }
                val presetLabels = remember {
                    listOf("Exam Prep", "Group Study", "Lab Sprint", "Mock Test", "Project Push")
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weekly_quick_note_panel")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(IndigoPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.StickyNote2,
                                        contentDescription = null,
                                        tint = IndigoPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Quick Note for Study Spike",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Attach a short label (e.g., 'Exam Prep', 'Group Study') to $quickNoteTargetDay",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Day selector chips (Mon..Sun)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            weeklyTrends.forEach { dayItem ->
                                val isDaySelected = quickNoteTargetDay == dayItem.dayKey
                                val hasNote = dayItem.quickNote.isNotBlank()
                                FilterChip(
                                    selected = isDaySelected,
                                    onClick = {
                                        quickNoteTargetDay = dayItem.dayKey
                                        selectedDayTrend = dayItem
                                    },
                                    label = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = if (dayItem.dayKey == currentDayKey) "${dayItem.dayKey}*" else dayItem.dayKey,
                                                fontSize = 11.sp,
                                                fontWeight = if (isDaySelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                            if (hasNote) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isDaySelected) IndigoPrimary else EmeraldTertiary)
                                                )
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(100),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = IndigoPrimaryContainer,
                                        selectedLabelColor = IndigoOnPrimaryContainer
                                    ),
                                    modifier = Modifier
                                        .heightIn(min = 30.dp)
                                        .testTag("quick_note_day_${dayItem.dayKey}")
                                )
                            }
                        }

                        // Preset label chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            presetLabels.forEach { preset ->
                                val isPresetActive = quickNoteInput.equals(preset, ignoreCase = true)
                                AssistChip(
                                    onClick = {
                                        quickNoteInput = preset
                                        viewModel.saveQuickNoteForDay(quickNoteTargetDay, preset)
                                        Toast.makeText(
                                            context,
                                            "Saved '$preset' for $quickNoteTargetDay",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    label = {
                                        Text(
                                            text = preset,
                                            fontSize = 10.sp,
                                            fontWeight = if (isPresetActive) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (isPresetActive) IndigoPrimaryContainer else MaterialTheme.colorScheme.surface
                                    ),
                                    modifier = Modifier
                                        .heightIn(min = 30.dp)
                                        .testTag("quick_note_preset_${preset.lowercase().replace(" ", "_")}")
                                )
                            }
                        }

                        // Input field + Save / Clear actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = quickNoteInput,
                                onValueChange = { if (it.length <= 40) quickNoteInput = it },
                                placeholder = {
                                    Text(
                                        "e.g., Exam Prep, Group Study",
                                        fontSize = 12.sp
                                    )
                                },
                                label = { Text("Note for $quickNoteTargetDay", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_weekly_quick_note")
                            )

                            Button(
                                onClick = {
                                    viewModel.saveQuickNoteForDay(quickNoteTargetDay, quickNoteInput)
                                    val msg = if (quickNoteInput.isBlank()) {
                                        "Cleared note for $quickNoteTargetDay"
                                    } else {
                                        "Note saved for $quickNoteTargetDay"
                                    }
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_save_quick_note")
                            ) {
                                Text("Save", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            if (!weeklyDayNotes[quickNoteTargetDay].isNullOrBlank() || quickNoteInput.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        quickNoteInput = ""
                                        viewModel.saveQuickNoteForDay(quickNoteTargetDay, "")
                                        Toast.makeText(
                                            context,
                                            "Cleared note for $quickNoteTargetDay",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("btn_clear_quick_note")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear Quick Note",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Interactive Legend: Weekly Subject Breakdown
            AnimatedVisibility(
                visible = viewMode == ChartViewMode.WEEKLY_TREND && showWeeklySubjectBreakdown,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weekly_subject_breakdown_legend")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Legend Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(VioletSecondary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PieChart,
                                        contentDescription = null,
                                        tint = VioletSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Weekly Subject Contributions",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Ranked by study hours • Tap to highlight on chart",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (selectedWeeklySubjectFilter != null) {
                                Surface(
                                    shape = RoundedCornerShape(100),
                                    color = IndigoPrimaryContainer,
                                    modifier = Modifier
                                        .clickable { selectedWeeklySubjectFilter = null }
                                        .testTag("btn_clear_subject_filter")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "Reset Filter",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = IndigoPrimary
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = IndigoPrimary,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Proportional Stacked Ribbon
                        val totalWeekMinutes = weeklyTrends.sumOf { it.totalMinutes }
                        if (totalWeekMinutes > 0 && weeklySubjectContributions.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(100))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                weeklySubjectContributions.forEach { item ->
                                    val weight = item.percentageOfTotal.toFloat().coerceAtLeast(1f)
                                    val isHighlighted = selectedWeeklySubjectFilter == null || selectedWeeklySubjectFilter == item.courseCode
                                    val subColor = getSubjectColor(item.courseCode)
                                    Box(
                                        modifier = Modifier
                                            .weight(weight)
                                            .fillMaxHeight()
                                            .background(
                                                if (isHighlighted) subColor
                                                else subColor.copy(alpha = 0.25f)
                                            )
                                            .clickable {
                                                selectedWeeklySubjectFilter = if (selectedWeeklySubjectFilter == item.courseCode) null else item.courseCode
                                            }
                                    )
                                }
                            }
                        }

                        // Interactive Subject Legend List Items
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            weeklySubjectContributions.forEachIndexed { index, item ->
                                val isSelected = selectedWeeklySubjectFilter == item.courseCode
                                val subjectColor = getSubjectColor(item.courseCode)
                                val courseObj = courses.firstOrNull { it.code == item.courseCode }
                                val courseGoal = getGoalForCourse(item.courseCode)
                                val goalProgressPct = if (courseGoal > 0) ((item.totalHours / courseGoal) * 100).toInt() else 0
                                val rankLabel = if (index == 0) "#1 🏆" else "#${index + 1}"

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) subjectColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) subjectColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedWeeklySubjectFilter = if (isSelected) null else item.courseCode
                                        }
                                        .testTag("legend_subject_${item.courseCode}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (index == 0) WarningAmber.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier.padding(end = 2.dp)
                                            ) {
                                                Text(
                                                    text = rankLabel,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (index == 0) WarningAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(subjectColor)
                                            )

                                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Text(
                                                        text = item.courseCode,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = if (isSelected) subjectColor else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = courseObj?.name ?: "Course Study",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }

                                                // Active days badges
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                ) {
                                                    Text("Active:", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { d ->
                                                        val isActive = item.daysActive.contains(d)
                                                        Box(
                                                            modifier = Modifier
                                                                .size(14.dp)
                                                                .clip(CircleShape)
                                                                .background(
                                                                    if (isActive) subjectColor.copy(alpha = 0.2f)
                                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = d.take(1),
                                                                fontSize = 8.sp,
                                                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                                                color = if (isActive) subjectColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Hours & Percentage
                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = "${String.format(Locale.US, "%.1f", item.totalHours)}h",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(100),
                                                    color = subjectColor.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = "${item.percentageOfTotal.roundToInt()}%",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = subjectColor,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "Goal: ${String.format(Locale.US, "%.1f", courseGoal)}h ($goalProgressPct%)",
                                                fontSize = 9.sp,
                                                color = if (item.totalHours >= courseGoal) EmeraldTertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = if (item.totalHours >= courseGoal) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Selected Subject Spotlight Box
                        if (selectedWeeklySubjectFilter != null) {
                            val selectedItem = weeklySubjectContributions.firstOrNull { it.courseCode == selectedWeeklySubjectFilter }
                            if (selectedItem != null) {
                                val subColor = getSubjectColor(selectedItem.courseCode)
                                val targetHours = getGoalForCourse(selectedItem.courseCode)

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = subColor.copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, subColor.copy(alpha = 0.35f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("weekly_subject_spotlight_card")
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(Icons.Default.Insights, contentDescription = null, tint = subColor, modifier = Modifier.size(15.dp))
                                                Text(
                                                    text = "${selectedItem.courseCode} Daily Breakdown",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = subColor
                                                )
                                            }
                                            Text(
                                                text = "Total: ${String.format(Locale.US, "%.1f", selectedItem.totalHours)}h (${selectedItem.percentageOfTotal.roundToInt()}%)",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        // Day by day distribution for this subject
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            weeklyTrends.forEach { day ->
                                                val mins = day.subjectBreakdown[selectedItem.courseCode] ?: 0
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(day.dayKey, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text(
                                                        text = if (mins > 0) "${String.format(Locale.US, "%.1f", mins / 60.0)}h" else "-",
                                                        fontSize = 10.sp,
                                                        fontWeight = if (mins > 0) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (mins > 0) subColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                    )
                                                }
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TextButton(
                                                onClick = {
                                                    preselectedSubjectForGoalModal = selectedItem.courseCode
                                                    showSubjectGoalsModal = true
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Icon(Icons.Default.TrackChanges, contentDescription = null, modifier = Modifier.size(12.dp), tint = subColor)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Adjust ${selectedItem.courseCode} Goal (${String.format(Locale.US, "%.1f", targetHours)}h)", fontSize = 10.sp, color = subColor)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 0.8.dp)

            // Bottom Summary Metrics Row: Dynamic based on viewMode and selectedDateRange
            if (viewMode == ChartViewMode.MONTHLY) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryMiniCard(
                        title = "4-Week Total",
                        value = "${String.format(Locale.US, "%.1f", monthlyTrendReport.totalMonthHours)}h",
                        subtitle = "${monthlyTrendReport.totalSessionCount} focus blocks",
                        icon = Icons.Default.CheckCircle,
                        color = EmeraldTertiary,
                        containerColor = EmeraldTertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )

                    SummaryMiniCard(
                        title = "Weekly Average",
                        value = "${String.format(Locale.US, "%.1f", monthlyTrendReport.weeklyAverageHours)}h",
                        subtitle = "hours / week (4w)",
                        icon = Icons.Default.Timeline,
                        color = IndigoPrimary,
                        containerColor = IndigoPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )

                    val peakWk = monthlyTrendReport.peakWeek
                    SummaryMiniCard(
                        title = "Peak Week",
                        value = peakWk?.weekKey ?: "None",
                        subtitle = if (peakWk != null) "${String.format(Locale.US, "%.1f", peakWk.totalHours)}h logged 🔥" else "-",
                        icon = Icons.Default.EmojiEvents,
                        color = WarningAmber,
                        containerColor = WarningContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else if (viewMode == ChartViewMode.WEEKLY_TREND) {
                val totalPeriodMins = weeklyTrends.sumOf { it.totalMinutes }
                val totalBucketsCount = weeklyTrends.size.coerceAtLeast(1)
                val activeBucketsCount = weeklyTrends.count { it.totalMinutes > 0 }
                val consistentBuckets = weeklyTrends.count { it.totalHours >= periodBucketGoalHours }
                val consistencyPct = ((consistentBuckets.toDouble() / totalBucketsCount.toDouble()) * 100).toInt()
                val unitPlural = when (selectedDateRange) {
                    StudyDateRange.LAST_7_DAYS -> "Days"
                    StudyDateRange.THIS_MONTH -> "Wks"
                    StudyDateRange.SEMESTER -> "Mos"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryMiniCard(
                        title = if (selectedDateRange == StudyDateRange.LAST_7_DAYS) "Active Streak" else "Active Periods",
                        value = "$activeBucketsCount / $totalBucketsCount $unitPlural",
                        subtitle = if (selectedDateRange == StudyDateRange.LAST_7_DAYS) "consistent streak 🔥" else "with logged study 🔥",
                        icon = Icons.Default.LocalFireDepartment,
                        color = Color(0xFFEF4444),
                        containerColor = Color(0xFFFEE2E2),
                        modifier = Modifier.weight(1f)
                    )

                    SummaryMiniCard(
                        title = "Consistency",
                        value = "$consistencyPct%",
                        subtitle = "met ${String.format(Locale.US, "%.1f", periodBucketGoalHours)}$periodGoalUnitLabel goal",
                        icon = Icons.Default.CheckCircle,
                        color = EmeraldTertiary,
                        containerColor = EmeraldTertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )

                    SummaryMiniCard(
                        title = when (selectedDateRange) {
                            StudyDateRange.LAST_7_DAYS -> "Daily Average"
                            StudyDateRange.THIS_MONTH -> "Weekly Average"
                            StudyDateRange.SEMESTER -> "Monthly Average"
                        },
                        value = "${String.format(Locale.US, "%.1f", (totalPeriodMins / 60.0) / totalBucketsCount.toDouble())}h",
                        subtitle = when (selectedDateRange) {
                            StudyDateRange.LAST_7_DAYS -> "hours / day"
                            StudyDateRange.THIS_MONTH -> "hours / week"
                            StudyDateRange.SEMESTER -> "hours / month"
                        },
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

                    val metCount = studyStats.count { it.totalHours >= getGoalForCourse(it.courseCode) }
                    if (subjectsBelow50Pct.isNotEmpty()) {
                        SummaryMiniCard(
                            title = "Below 50% Goal",
                            value = "${subjectsBelow50Pct.size} Alert${if (subjectsBelow50Pct.size > 1) "s" else ""}",
                            subtitle = "needs study time ⚠️",
                            icon = Icons.Default.Warning,
                            color = ErrorRed,
                            containerColor = ErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        SummaryMiniCard(
                            title = "Goal Progress",
                            value = "$metCount / ${studyStats.size} Met",
                            subtitle = if (metCount == studyStats.size) "All targets met! 🎉" else "≥50% on track 👍",
                            icon = Icons.Default.CheckCircle,
                            color = EmeraldTertiary,
                            containerColor = EmeraldTertiaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    if (showGoalSettingsModal) {
        CustomizeDailyGoalDialog(
            currentGoalHours = dailyGoalHours,
            onDismiss = { showGoalSettingsModal = false },
            onSaveGoal = { newGoal ->
                viewModel.updateDailyStudyGoalHours(newGoal)
                showGoalSettingsModal = false
            }
        )
    }

    if (showSubjectGoalsModal) {
        CustomizeSubjectGoalsDialog(
            courses = courses,
            studyStats = studyStats,
            currentGoals = subjectWeeklyGoals,
            preselectedCourseCode = preselectedSubjectForGoalModal,
            currentPriorities = subjectPriorities,
            onDismiss = {
                showSubjectGoalsModal = false
                preselectedSubjectForGoalModal = null
            },
            onSaveGoal = { courseCode, newGoal ->
                viewModel.updateSubjectWeeklyStudyGoal(courseCode, newGoal)
            },
            onUpdatePriority = { courseCode, priority ->
                viewModel.updateSubjectPriority(courseCode, priority)
            },
            onResetDefaults = {
                viewModel.resetSubjectWeeklyStudyGoals()
            }
        )
    }

    if (showExportDialog) {
        val scaledGoalsForExport = courses.associate { c -> c.code to getGoalForCourse(c.code) } +
            subjectWeeklyGoals.mapValues { (_, v) -> v * selectedDateRange.goalScaleMultiplier }
        ExportStudyLogsDialog(
            studyStats = studyStats,
            courses = courses,
            subjectGoals = scaledGoalsForExport,
            subjectPriorities = subjectPriorities,
            tagFilter = selectedSessionTagFilter,
            dateRangeLabel = selectedDateRange.label,
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
fun SubjectMasteryMiniLineChart(
    masteryRecords: List<SubjectMasteryEntity>,
    subjectColor: Color,
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val sortedRecords = remember(masteryRecords) {
        masteryRecords.sortedBy { it.weekNumber }
    }
    val latestMastery = sortedRecords.lastOrNull()?.masteryPercentage ?: 0.0
    val firstMastery = sortedRecords.firstOrNull()?.masteryPercentage ?: 0.0
    val isUpward = latestMastery >= firstMastery

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(52.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isHighlighted) subjectColor.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
            .padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        // Sparkline Canvas with Area Fill and Point Dots
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
        ) {
            if (sortedRecords.size >= 2) {
                val minVal = 30.0
                val maxVal = 100.0
                val range = (maxVal - minVal).toFloat()

                val paddingX = 4.dp.toPx()
                val availableWidth = size.width - (paddingX * 2)
                val stepX = availableWidth / (sortedRecords.size - 1)

                val points = sortedRecords.mapIndexed { index, record ->
                    val x = paddingX + (index * stepX)
                    val normalized = ((record.masteryPercentage - minVal).toFloat() / range).coerceIn(0.05f, 0.95f)
                    val y = size.height - (normalized * size.height)
                    Offset(x, y)
                }

                // 1. Fill Path below the line chart
                val fillPath = Path().apply {
                    moveTo(points.first().x, size.height)
                    lineTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                    lineTo(points.last().x, size.height)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            subjectColor.copy(alpha = if (isHighlighted) 0.45f else 0.28f),
                            subjectColor.copy(alpha = 0.02f)
                        )
                    )
                )

                // 2. Stroke line
                val strokePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }
                drawPath(
                    path = strokePath,
                    color = if (isHighlighted) subjectColor else subjectColor.copy(alpha = 0.85f),
                    style = Stroke(
                        width = if (isHighlighted) 2.dp.toPx() else 1.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // 3. Dots at data points (W1, W2, W3, W4)
                points.forEachIndexed { i, pt ->
                    val isLast = i == points.size - 1
                    if (isLast) {
                        // Highlight W4 with glowing outer circle
                        drawCircle(
                            color = subjectColor.copy(alpha = 0.35f),
                            radius = 4.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = subjectColor,
                            radius = 2.5.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 1.2.dp.toPx(),
                            center = pt
                        )
                    } else {
                        drawCircle(
                            color = subjectColor.copy(alpha = 0.75f),
                            radius = 1.6.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }
        }

        // Mini Mastery Trend Label below sparkline
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = "4w:",
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${latestMastery.toInt()}%",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (latestMastery >= 85.0) EmeraldTertiary else if (latestMastery < 50.0) ErrorRed else subjectColor
            )
            Icon(
                imageVector = if (isUpward) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                contentDescription = null,
                modifier = Modifier.size(8.dp),
                tint = if (isUpward) EmeraldTertiary else ErrorRed
            )
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

@Composable
fun CustomizeDailyGoalDialog(
    currentGoalHours: Double,
    onDismiss: () -> Unit,
    onSaveGoal: (Double) -> Unit
) {
    var goalHoursInput by remember { mutableStateOf(String.format(Locale.US, "%.1f", currentGoalHours)) }
    var sliderValue by remember { mutableFloatStateOf(currentGoalHours.coerceIn(0.5, 8.0).toFloat()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Quick presets
    val presets = listOf(1.0, 1.5, 2.0, 2.5, 3.0, 4.0, 5.0)

    fun applyGoal(value: Double) {
        val clamped = (value * 10).roundToInt() / 10.0
        goalHoursInput = String.format(Locale.US, "%.1f", clamped)
        sliderValue = clamped.coerceIn(0.5, 8.0).toFloat()
        errorMessage = null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("customize_daily_goal_dialog"),
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(IndigoPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = IndigoPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text = "Daily Study Goal",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Set your daily target study hours. Chart threshold lines and consistency metrics will adapt instantly.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Current Goal Display Card & Motivation Banner
                val currentParsed = goalHoursInput.toDoubleOrNull() ?: currentGoalHours
                val weeklyTotal = currentParsed * 7.0

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Weekly Commitment",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.1f", weeklyTotal)} hrs / week",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndigoPrimary
                            )
                        }

                        // Badge based on intensity
                        val (badgeText, badgeColor, badgeBg) = when {
                            currentParsed < 1.5 -> Triple("Light", Color(0xFF6B7280), Color(0xFFF3F4F6))
                            currentParsed <= 2.5 -> Triple("Balanced", EmeraldTertiary, EmeraldTertiaryContainer)
                            currentParsed <= 4.0 -> Triple("Focused", IndigoPrimary, IndigoPrimaryContainer)
                            else -> Triple("Intense", WarningAmber, WarningContainer)
                        }

                        Surface(
                            shape = RoundedCornerShape(100),
                            color = badgeBg,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Stepper + Direct Input Field
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Target Hours Per Day",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Minus Button
                        FilledTonalIconButton(
                            onClick = {
                                val current = goalHoursInput.toDoubleOrNull() ?: currentGoalHours
                                if (current > 0.5) {
                                    applyGoal(current - 0.5)
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_stepper_minus")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease goal by 0.5 hours")
                        }

                        // Text Field for direct input
                        OutlinedTextField(
                            value = goalHoursInput,
                            onValueChange = { input ->
                                goalHoursInput = input
                                val parsed = input.toDoubleOrNull()
                                if (parsed == null) {
                                    errorMessage = "Please enter a valid number"
                                } else if (parsed < 0.5 || parsed > 12.0) {
                                    errorMessage = "Goal must be between 0.5h and 12.0h"
                                } else {
                                    errorMessage = null
                                    sliderValue = parsed.coerceIn(0.5, 8.0).toFloat()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_daily_goal_hours"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            trailingIcon = {
                                Text(
                                    text = "hours",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            },
                            isError = errorMessage != null,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Plus Button
                        FilledTonalIconButton(
                            onClick = {
                                val current = goalHoursInput.toDoubleOrNull() ?: currentGoalHours
                                if (current < 12.0) {
                                    applyGoal(current + 0.5)
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_stepper_plus")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase goal by 0.5 hours")
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                // Interactive Slider
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Slider Adjust (0.5h – 8.0h)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.1f", sliderValue)}h",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPrimary
                        )
                    }

                    Slider(
                        value = sliderValue,
                        onValueChange = { newValue ->
                            val stepped = ((newValue * 2f).roundToInt() / 2.0f)
                            sliderValue = stepped
                            goalHoursInput = String.format(Locale.US, "%.1f", stepped)
                            errorMessage = null
                        },
                        valueRange = 0.5f..8.0f,
                        steps = 14,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("slider_daily_goal"),
                        colors = SliderDefaults.colors(
                            thumbColor = IndigoPrimary,
                            activeTrackColor = IndigoPrimary,
                            inactiveTrackColor = IndigoPrimary.copy(alpha = 0.2f)
                        )
                    )
                }

                // Quick Preset Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Quick Presets",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.take(4).forEach { preset ->
                            val isSelected = (goalHoursInput.toDoubleOrNull() ?: 0.0) == preset
                            SuggestionChip(
                                onClick = { applyGoal(preset) },
                                label = {
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", preset)}h",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (isSelected) IndigoPrimaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    labelColor = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.onSurface
                                ),
                                border = if (isSelected) BorderStroke(1.dp, IndigoPrimary) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chip_preset_${String.format(Locale.US, "%.1f", preset).replace('.', '_')}")
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.drop(4).forEach { preset ->
                            val isSelected = (goalHoursInput.toDoubleOrNull() ?: 0.0) == preset
                            SuggestionChip(
                                onClick = { applyGoal(preset) },
                                label = {
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", preset)}h",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (isSelected) IndigoPrimaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    labelColor = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.onSurface
                                ),
                                border = if (isSelected) BorderStroke(1.dp, IndigoPrimary) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chip_preset_${String.format(Locale.US, "%.1f", preset).replace('.', '_')}")
                            )
                        }
                        // Default 2.0h Reset chip
                        SuggestionChip(
                            onClick = { applyGoal(2.0) },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(10.dp))
                                    Text("Reset (2h)", fontSize = 10.sp)
                                }
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = WarningContainer.copy(alpha = 0.5f),
                                labelColor = OnWarningContainer
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_reset_daily_goal")
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = goalHoursInput.toDoubleOrNull()
                    if (parsed != null && parsed in 0.5..12.0) {
                        onSaveGoal(parsed)
                    } else {
                        errorMessage = "Enter a value between 0.5 and 12.0"
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                modifier = Modifier.testTag("btn_save_daily_goal")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Goal", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_daily_goal")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CustomizeSubjectGoalsDialog(
    courses: List<CourseEntity>,
    studyStats: List<SubjectStudyStats>,
    currentGoals: Map<String, Double>,
    preselectedCourseCode: String?,
    onDismiss: () -> Unit,
    onSaveGoal: (courseCode: String, goalHours: Double) -> Unit,
    onResetDefaults: () -> Unit,
    currentPriorities: Map<String, String> = emptyMap(),
    onUpdatePriority: (courseCode: String, priority: String) -> Unit = { _, _ -> }
) {
    val availableCourses = remember(courses, studyStats) {
        val courseCodes = (courses.map { it.code } + studyStats.map { it.courseCode }).distinct()
        courseCodes.map { code ->
            val course = courses.firstOrNull { it.code == code }
            Pair(code, course?.name ?: "Subject $code")
        }
    }

    var selectedCourseCode by remember(preselectedCourseCode, availableCourses) {
        mutableStateOf(
            preselectedCourseCode?.takeIf { code -> availableCourses.any { it.first == code } }
                ?: availableCourses.firstOrNull()?.first
                ?: "CS201"
        )
    }

    val currentCourseGoal = currentGoals[selectedCourseCode] ?: 5.0
    var goalHoursInput by remember(selectedCourseCode, currentGoals) {
        mutableStateOf(String.format(Locale.US, "%.1f", currentGoals[selectedCourseCode] ?: 5.0))
    }
    var sliderValue by remember(selectedCourseCode, currentGoals) {
        mutableFloatStateOf((currentGoals[selectedCourseCode] ?: 5.0).coerceIn(1.0, 15.0).toFloat())
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    fun applyGoalValue(value: Double, autoSave: Boolean = true) {
        val clamped = (value * 10).roundToInt() / 10.0
        goalHoursInput = String.format(Locale.US, "%.1f", clamped)
        sliderValue = clamped.coerceIn(1.0, 15.0).toFloat()
        errorMessage = null
        if (autoSave) {
            onSaveGoal(selectedCourseCode, clamped)
            saveSuccessMessage = "Updated $selectedCourseCode target to ${String.format(Locale.US, "%.1f", clamped)}h! Chart updated ✓"
        } else {
            saveSuccessMessage = null
        }
    }

    val presets = listOf(2.0, 3.0, 4.0, 5.0, 6.0, 8.0, 10.0)

    val activeStat = studyStats.firstOrNull { it.courseCode == selectedCourseCode }
    val loggedHours = activeStat?.totalHours ?: 0.0
    val activeCourseObj = courses.firstOrNull { it.code == selectedCourseCode }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .testTag("customize_subject_goals_dialog")
            .testTag("edit_goals_modal")
            .testTag("edit_subject_goals_dialog"),
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(IndigoPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Goals",
                    tint = IndigoPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text = "Edit Subject Goals",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Adjust weekly study hour targets for each subject. SVG chart bars, threshold lines, and progress indicators update dynamically.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Horizontal Subject Picker Chips
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Select Course to Configure:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableCourses.forEach { (code, name) ->
                            val isSelected = code == selectedCourseCode
                            val courseTarget = currentGoals[code] ?: 5.0
                            val stat = studyStats.firstOrNull { it.courseCode == code }
                            val hours = stat?.totalHours ?: 0.0
                            val isMet = hours >= courseTarget

                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCourseCode = code
                                    applyGoalValue(currentGoals[code] ?: 5.0, autoSave = false)
                                },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "$code (${String.format(Locale.US, "%.1f", courseTarget)}h)",
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        )
                                        if (isMet) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(10.dp), tint = EmeraldTertiary)
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(100),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IndigoPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("chip_select_subject_$code")
                            )
                        }
                    }
                }

                // Active Subject Detail Card with Current Target & Progress
                val parsedGoal = goalHoursInput.toDoubleOrNull() ?: currentCourseGoal
                val progressPct = if (parsedGoal > 0) ((loggedHours / parsedGoal) * 100).toInt() else 0
                val isGoalMet = loggedHours >= parsedGoal
                val isBelow50 = loggedHours < (parsedGoal * 0.5)

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, if (isGoalMet) EmeraldTertiary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = IndigoPrimary
                                    ) {
                                        Text(
                                            text = selectedCourseCode,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = activeCourseObj?.name ?: "Subject Course",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = "${activeCourseObj?.instructor ?: "Faculty"} • ${activeCourseObj?.room ?: "Room"}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Progress Tag
                            Surface(
                                shape = RoundedCornerShape(100),
                                color = if (isGoalMet) EmeraldTertiaryContainer else if (isBelow50) ErrorContainer else IndigoPrimaryContainer
                            ) {
                                Text(
                                    text = if (isGoalMet) "Target Met 🎉" else if (isBelow50) "<50% Behind ⚠️" else "$progressPct% Progress",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isGoalMet) EmeraldOnTertiaryContainer else if (isBelow50) OnErrorContainer else IndigoOnPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Progress Bar toward target
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Logged: ${String.format(Locale.US, "%.1f", loggedHours)}h",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Target: ${String.format(Locale.US, "%.1f", parsedGoal)}h / week",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IndigoPrimary
                                )
                            }
                            val progressFraction = if (parsedGoal > 0) (loggedHours / parsedGoal).toFloat().coerceIn(0f, 1f) else 0f
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(100)),
                                color = if (isGoalMet) EmeraldTertiary else if (isBelow50) ErrorRed else IndigoPrimary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }

                // Priority Level Selector (High, Medium, Low) for Selected Subject
                val activePriority = currentPriorities[selectedCourseCode] ?: "Medium"
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Priority Level for $selectedCourseCode:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = when (activePriority.lowercase(Locale.US)) {
                                "high" -> Color(0xFFFEE2E2)
                                "medium" -> Color(0xFFFEF3C7)
                                else -> Color(0xFFD1FAE5)
                            }
                        ) {
                            Text(
                                text = "$activePriority Priority",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (activePriority.lowercase(Locale.US)) {
                                    "high" -> Color(0xFFDC2626)
                                    "medium" -> Color(0xFFD97706)
                                    else -> Color(0xFF059669)
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("High", "Medium", "Low").forEach { priorityOption ->
                            val isSelected = activePriority.equals(priorityOption, ignoreCase = true)
                            val optionColor = when (priorityOption) {
                                "High" -> Color(0xFFEF4444)
                                "Medium" -> Color(0xFFF59E0B)
                                else -> Color(0xFF10B981)
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    onUpdatePriority(selectedCourseCode, priorityOption)
                                    saveSuccessMessage = "Set $selectedCourseCode priority to $priorityOption!"
                                },
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            priorityOption,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                        if (priorityOption == "High") {
                                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(100),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = optionColor.copy(alpha = 0.2f),
                                    selectedLabelColor = optionColor
                                ),
                                border = if (isSelected) BorderStroke(1.5.dp, optionColor) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 34.dp)
                                    .testTag("priority_${priorityOption.lowercase()}_$selectedCourseCode")
                                    .testTag("btn_priority_${priorityOption.lowercase()}")
                            )
                        }
                    }
                }

                // Stepper + Direct Input for Selected Subject
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Weekly Target for $selectedCourseCode",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalIconButton(
                            onClick = {
                                val current = goalHoursInput.toDoubleOrNull() ?: currentCourseGoal
                                if (current > 0.5) {
                                    applyGoalValue(current - 0.5)
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_stepper_subject_minus")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease goal by 0.5 hours")
                        }

                        OutlinedTextField(
                            value = goalHoursInput,
                            onValueChange = { input ->
                                goalHoursInput = input
                                val parsed = input.toDoubleOrNull()
                                if (parsed == null) {
                                    errorMessage = "Please enter a valid number"
                                } else if (parsed < 0.5 || parsed > 25.0) {
                                    errorMessage = "Goal must be between 0.5h and 25.0h"
                                } else {
                                    errorMessage = null
                                    sliderValue = parsed.coerceIn(1.0, 15.0).toFloat()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_subject_goal_hours"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            trailingIcon = {
                                Text(
                                    text = "hrs/wk",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            },
                            isError = errorMessage != null,
                            shape = RoundedCornerShape(12.dp)
                        )

                        FilledTonalIconButton(
                            onClick = {
                                val current = goalHoursInput.toDoubleOrNull() ?: currentCourseGoal
                                if (current < 25.0) {
                                    applyGoalValue(current + 0.5)
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_stepper_subject_plus")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase goal by 0.5 hours")
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    if (saveSuccessMessage != null) {
                        Text(
                            text = saveSuccessMessage ?: "",
                            fontSize = 11.sp,
                            color = EmeraldTertiary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                // Slider
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Slider Adjust (1h – 15h)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.1f", sliderValue)}h",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPrimary
                        )
                    }

                    Slider(
                        value = sliderValue,
                        onValueChange = { newValue ->
                            val stepped = ((newValue * 2f).roundToInt() / 2.0f)
                            sliderValue = stepped
                            goalHoursInput = String.format(Locale.US, "%.1f", stepped)
                            errorMessage = null
                            onSaveGoal(selectedCourseCode, stepped.toDouble())
                            saveSuccessMessage = "Live synced $selectedCourseCode target: ${String.format(Locale.US, "%.1f", stepped)}h ✓"
                        },
                        valueRange = 1.0f..15.0f,
                        steps = 27,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("slider_subject_goal"),
                        colors = SliderDefaults.colors(
                            thumbColor = IndigoPrimary,
                            activeTrackColor = IndigoPrimary,
                            inactiveTrackColor = IndigoPrimary.copy(alpha = 0.2f)
                        )
                    )
                }

                // Preset Chips
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Quick Presets:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        presets.take(4).forEach { preset ->
                            val isSelected = (goalHoursInput.toDoubleOrNull() ?: 0.0) == preset
                            SuggestionChip(
                                onClick = { applyGoalValue(preset) },
                                label = { Text("${preset.toInt()}h", fontSize = 11.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (isSelected) IndigoPrimaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    labelColor = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.onSurface
                                ),
                                border = if (isSelected) BorderStroke(1.dp, IndigoPrimary) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chip_subject_preset_${preset.toInt()}")
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        presets.drop(4).forEach { preset ->
                            val isSelected = (goalHoursInput.toDoubleOrNull() ?: 0.0) == preset
                            SuggestionChip(
                                onClick = { applyGoalValue(preset) },
                                label = { Text("${preset.toInt()}h", fontSize = 11.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (isSelected) IndigoPrimaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    labelColor = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.onSurface
                                ),
                                border = if (isSelected) BorderStroke(1.dp, IndigoPrimary) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chip_subject_preset_${preset.toInt()}")
                            )
                        }
                        SuggestionChip(
                            onClick = { applyGoalValue(5.0) },
                            label = { Text("5h (Def)", fontSize = 10.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = WarningContainer.copy(alpha = 0.5f),
                                labelColor = OnWarningContainer
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = goalHoursInput.toDoubleOrNull()
                    if (parsed != null && parsed in 0.5..25.0) {
                        onSaveGoal(selectedCourseCode, parsed)
                        saveSuccessMessage = "Saved ${String.format(Locale.US, "%.1f", parsed)}h goal for $selectedCourseCode! Chart updated ✓"
                    } else {
                        errorMessage = "Enter a value between 0.5 and 25.0"
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                modifier = Modifier
                    .testTag("btn_save_subject_goal")
                    .testTag("btn_apply_subject_goal")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Apply Goal for $selectedCourseCode", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = onResetDefaults,
                    modifier = Modifier.testTag("btn_reset_subject_goals")
                ) {
                    Text("Reset All", color = MaterialTheme.colorScheme.error)
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("btn_close_subject_goals")
                ) {
                    Text("Done")
                }
            }
        }
    )
}

enum class ExportFormat {
    JSON, CSV
}

@Composable
fun ExportStudyLogsDialog(
    studyStats: List<SubjectStudyStats>,
    courses: List<CourseEntity>,
    subjectGoals: Map<String, Double>,
    subjectPriorities: Map<String, String>,
    tagFilter: String,
    dateRangeLabel: String = "Last 7 Days",
    onDismiss: () -> Unit
) {
    var format by remember { mutableStateOf(ExportFormat.JSON) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val jsonExportText = remember(studyStats, courses, subjectGoals, subjectPriorities, tagFilter, dateRangeLabel) {
        val totalMins = studyStats.sumOf { it.totalMinutes }
        val totalHours = totalMins / 60.0
        val totalSessions = studyStats.sumOf { it.sessionCount }

        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"periodSummary\": {\n")
        sb.append("    \"dateRange\": \"$dateRangeLabel\",\n")
        sb.append("    \"totalLoggedHours\": ${String.format(Locale.US, "%.1f", totalHours)},\n")
        sb.append("    \"totalSessions\": $totalSessions,\n")
        sb.append("    \"sessionTagFilter\": \"$tagFilter\"\n")
        sb.append("  },\n")
        sb.append("  \"subjects\": [\n")
        studyStats.forEachIndexed { index, stat ->
            val courseName = courses.firstOrNull { it.code == stat.courseCode }?.name ?: "Subject ${stat.courseCode}"
            val goal = subjectGoals[stat.courseCode] ?: 5.0
            val priority = subjectPriorities[stat.courseCode] ?: "Medium"
            val pct = if (goal > 0) ((stat.totalHours / goal) * 100).toInt() else 0
            sb.append("    {\n")
            sb.append("      \"courseCode\": \"${stat.courseCode}\",\n")
            sb.append("      \"subjectName\": \"${courseName.replace("\"", "\\\"")}\",\n")
            sb.append("      \"loggedHours\": ${String.format(Locale.US, "%.1f", stat.totalHours)},\n")
            sb.append("      \"goalHours\": ${String.format(Locale.US, "%.1f", goal)},\n")
            sb.append("      \"priority\": \"$priority\",\n")
            sb.append("      \"sessionCount\": ${stat.sessionCount},\n")
            sb.append("      \"completionPercentage\": $pct\n")
            sb.append("    }${if (index < studyStats.size - 1) "," else ""}\n")
        }
        sb.append("  ]\n")
        sb.append("}")
        sb.toString()
    }

    val csvExportText = remember(studyStats, courses, subjectGoals, subjectPriorities, tagFilter, dateRangeLabel) {
        val totalMins = studyStats.sumOf { it.totalMinutes }
        val totalHours = totalMins / 60.0
        val totalSessions = studyStats.sumOf { it.sessionCount }

        val sb = StringBuilder()
        sb.append("Course Code,Subject Name,Logged Hours,Goal Hours,Priority,Session Count,Completion Pct\n")
        studyStats.forEach { stat ->
            val courseName = courses.firstOrNull { it.code == stat.courseCode }?.name ?: "Subject ${stat.courseCode}"
            val goal = subjectGoals[stat.courseCode] ?: 5.0
            val priority = subjectPriorities[stat.courseCode] ?: "Medium"
            val pct = if (goal > 0) ((stat.totalHours / goal) * 100).toInt() else 0
            val safeName = if (courseName.contains(",")) "\"$courseName\"" else courseName
            sb.append("${stat.courseCode},$safeName,${String.format(Locale.US, "%.1f", stat.totalHours)},${String.format(Locale.US, "%.1f", goal)},$priority,${stat.sessionCount},$pct%\n")
        }
        sb.append("\nSummary,Date Range,$dateRangeLabel,Total Logged Hours,${String.format(Locale.US, "%.1f", totalHours)},Total Sessions,$totalSessions,Filter,$tagFilter\n")
        sb.toString()
    }

    val currentExportText = if (format == ExportFormat.JSON) jsonExportText else csvExportText

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("export_study_logs_dialog"),
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(EmeraldTertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Export Study Logs",
                    tint = EmeraldTertiary,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text = "Export Study Logs",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Export your weekly study hours, subject targets, and priorities as JSON or CSV summary.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Format Selector Tabs (JSON / CSV)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(100))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ExportFormat.values().forEach { fmt ->
                        val isSelected = format == fmt
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = if (isSelected) EmeraldTertiary else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(100))
                                .clickable { format = fmt }
                                .testTag("tab_export_${fmt.name.lowercase()}")
                        ) {
                            Text(
                                text = fmt.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                // Monospace Preview Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                            .testTag("export_data_preview")
                    ) {
                        Text(
                            text = currentExportText,
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Copy to Clipboard
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(currentExportText))
                        Toast.makeText(context, "Exported ${format.name} data copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_copy_export_data")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy", fontWeight = FontWeight.SemiBold)
                }

                // Share via Intent
                Button(
                    onClick = {
                        val mimeType = if (format == ExportFormat.JSON) "application/json" else "text/csv"
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = mimeType
                            putExtra(Intent.EXTRA_SUBJECT, "CampusLife Weekly Study Logs (${format.name})")
                            putExtra(Intent.EXTRA_TEXT, currentExportText)
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share Study Logs Summary")
                        context.startActivity(shareIntent)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary),
                    modifier = Modifier.testTag("btn_share_export_data")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_close_export_dialog")
            ) {
                Text("Close")
            }
        }
    )
}


