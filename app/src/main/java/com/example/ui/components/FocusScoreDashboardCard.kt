package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FocusQualityTier
import com.example.data.local.SubjectFocusScore
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel
import java.util.Locale

private enum class FocusScoreFilter(val label: String) {
    ALL("All"),
    SHALLOW("Shallow (<60%)"),
    DEEP("Deep (80%+)")
}

@Composable
fun FocusScoreDashboardCard(
    viewModel: CampusLifeViewModel,
    modifier: Modifier = Modifier
) {
    val report by viewModel.focusScoreReport.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val context = LocalContext.current

    var selectedFilter by remember { mutableStateOf(FocusScoreFilter.ALL) }
    var expandedCourseCode by remember { mutableStateOf<String?>(null) }
    var showLogQualityModal by remember { mutableStateOf(false) }
    var preselectedCourseCode by remember { mutableStateOf<String?>(null) }
    var preselectedQualityMode by remember { mutableStateOf("SHALLOW") }

    val displayedSubjects = remember(report.subjects, selectedFilter) {
        when (selectedFilter) {
            FocusScoreFilter.ALL -> report.subjects
            FocusScoreFilter.SHALLOW -> report.subjects.filter { it.isShallowStudying }
            FocusScoreFilter.DEEP -> report.subjects.filter { it.tier == FocusQualityTier.DEEP_FOCUS }
        }
    }

    val overallAccent = focusTierAccentColor(report.overallTier)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("focus_score_dashboard_card"),
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(IndigoPrimaryContainer, EmeraldTertiaryContainer)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CenterFocusStrong,
                            contentDescription = "Focus Score Dashboard",
                            tint = IndigoPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Focus Score",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(100),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = report.dateRange.label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Focused study vs. total logged time ratio",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                FilledTonalButton(
                    onClick = {
                        preselectedCourseCode = report.topShallowSubject?.courseCode
                            ?: courses.firstOrNull()?.code
                            ?: "CS201"
                        preselectedQualityMode = "SHALLOW"
                        showLogQualityModal = true
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(100),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("btn_open_log_study_quality")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddChart,
                        contentDescription = "Log focus or shallow session",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Log Block",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Hero Gauge & Ratio Summary
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, overallAccent.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("focus_score_hero_summary")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FocusScoreCircularGauge(
                        scorePct = report.overallFocusScorePct,
                        accentColor = overallAccent,
                        hasData = report.overallTotalLoggedMinutes > 0,
                        modifier = Modifier.size(96.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(100),
                                color = overallAccent.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = report.overallTier.label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = overallAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            if (report.shallowStudyingCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(100),
                                    color = ErrorContainer
                                ) {
                                    Text(
                                        text = "${report.shallowStudyingCount} Shallow",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnErrorContainer,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        FocusMetricRow(
                            dotColor = EmeraldTertiary,
                            label = "Focused Study",
                            valueText = formatMinutesReadable(report.overallFocusedMinutes)
                        )
                        FocusMetricRow(
                            dotColor = WarningAmber,
                            label = "Shallow / Breaks",
                            valueText = formatMinutesReadable(report.overallNonFocusMinutes)
                        )
                        FocusMetricRow(
                            dotColor = IndigoPrimary,
                            label = "Total Logged",
                            valueText = formatMinutesReadable(report.overallTotalLoggedMinutes)
                        )
                    }
                }
            }

            // Shallow Studying Alert Banner
            AnimatedVisibility(
                visible = report.shallowStudyingCount > 0 && report.topShallowSubject != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val topShallow = report.topShallowSubject
                if (topShallow != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ErrorContainer.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shallow_studying_alert_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
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
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(ErrorRed.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = "Shallow studying alert",
                                        tint = ErrorRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                    Text(
                                        text = "Shallow Studying Detected (${report.shallowStudyingCount} ${if (report.shallowStudyingCount == 1) "Subject" else "Subjects"})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnErrorContainer
                                    )
                                    Text(
                                        text = "${topShallow.courseCode} is at ${topShallow.focusScorePct}% focus (${formatMinutesReadable(topShallow.nonFocusMinutes)} shallow/breaks). Try active recall sprints.",
                                        fontSize = 10.sp,
                                        color = OnErrorContainer.copy(alpha = 0.85f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Surface(
                                shape = RoundedCornerShape(100),
                                color = ErrorRed,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100))
                                    .clickable {
                                        expandedCourseCode = if (expandedCourseCode == topShallow.courseCode) null else topShallow.courseCode
                                    }
                                    .testTag("btn_view_shallow_strategy")
                            ) {
                                Text(
                                    text = "Strategies",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Filter Row + Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FocusScoreFilter.values().forEach { filter ->
                    val isSelected = selectedFilter == filter
                    val count = when (filter) {
                        FocusScoreFilter.ALL -> report.subjects.size
                        FocusScoreFilter.SHALLOW -> report.shallowStudyingCount
                        FocusScoreFilter.DEEP -> report.deepFocusCount
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = "${filter.label} ($count)",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        } else null,
                        shape = RoundedCornerShape(100),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (filter) {
                                FocusScoreFilter.SHALLOW -> ErrorContainer
                                FocusScoreFilter.DEEP -> EmeraldTertiaryContainer
                                FocusScoreFilter.ALL -> IndigoPrimaryContainer
                            },
                            selectedLabelColor = when (filter) {
                                FocusScoreFilter.SHALLOW -> OnErrorContainer
                                FocusScoreFilter.DEEP -> EmeraldOnTertiaryContainer
                                FocusScoreFilter.ALL -> IndigoOnPrimaryContainer
                            }
                        ),
                        modifier = Modifier
                            .heightIn(min = 34.dp)
                            .testTag("focus_filter_${filter.name.lowercase()}")
                    )
                }
            }

            // Subject Breakdown List
            if (displayedSubjects.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = EmeraldTertiary,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = when (selectedFilter) {
                                FocusScoreFilter.SHALLOW -> "No shallow studying subjects right now!"
                                FocusScoreFilter.DEEP -> "No subjects at 80%+ Deep Focus yet"
                                FocusScoreFilter.ALL -> "No study sessions logged in this date range yet"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Log a Focus or Break session with the Pomodoro timer or Log Block button.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    displayedSubjects.forEach { subject ->
                        SubjectFocusScoreRow(
                            subject = subject,
                            isExpanded = expandedCourseCode == subject.courseCode,
                            onToggleExpand = {
                                expandedCourseCode =
                                    if (expandedCourseCode == subject.courseCode) null else subject.courseCode
                            },
                            onQuickFocusBoost = {
                                viewModel.recordStudySession(
                                    courseCode = subject.courseCode,
                                    subjectName = subject.courseName,
                                    durationMinutes = 25,
                                    mode = "FOCUS",
                                    notes = "Deep Focus sprint from Focus Score Dashboard",
                                    tag = "Exam Prep",
                                    dateDisplay = "Today"
                                )
                                Toast.makeText(
                                    context,
                                    "+25m Deep Focus logged for ${subject.courseCode}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onOpenLogForSubject = {
                                preselectedCourseCode = subject.courseCode
                                preselectedQualityMode = if (subject.isShallowStudying) "FOCUS" else "SHALLOW"
                                showLogQualityModal = true
                            }
                        )
                    }
                }
            }

            // Compact Ratio Legend Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendSwatch(color = EmeraldTertiary, label = "Focused")
                LegendSwatch(color = WarningAmber, label = "Shallow/Passive")
                LegendSwatch(color = ErrorRed.copy(alpha = 0.65f), label = "Break Drift")
            }
        }
    }

    if (showLogQualityModal) {
        LogStudyQualityDialog(
            courses = courses.map { it.code to it.name }.ifEmpty {
                listOf(
                    "CS201" to "Data Structures & Algorithms",
                    "MATH204" to "Discrete Mathematics",
                    "CS310" to "Operating Systems",
                    "PHY102" to "Applied Electromagnetism",
                    "ENG215" to "Technical Writing"
                )
            },
            initialCourseCode = preselectedCourseCode ?: courses.firstOrNull()?.code ?: "CS201",
            initialMode = preselectedQualityMode,
            onDismiss = { showLogQualityModal = false },
            onSave = { courseCode, courseName, mode, minutes, notes ->
                viewModel.recordStudySession(
                    courseCode = courseCode,
                    subjectName = courseName,
                    durationMinutes = minutes,
                    mode = mode,
                    notes = notes,
                    tag = if (mode == "FOCUS") "Exam Prep" else "Revision",
                    dateDisplay = "Today"
                )
                showLogQualityModal = false
                val modeTitle = when (mode) {
                    "FOCUS" -> "Deep Focus"
                    "SHALLOW" -> "Shallow Review"
                    else -> "Break Time"
                }
                Toast.makeText(
                    context,
                    "Logged ${minutes}m $modeTitle for $courseCode",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}

@Composable
private fun FocusScoreCircularGauge(
    scorePct: Int,
    accentColor: Color,
    hasData: Boolean,
    modifier: Modifier = Modifier
) {
    val targetSweep = if (hasData) (scorePct.coerceIn(0, 100) / 100f) * 360f else 0f
    val animatedSweep by animateFloatAsState(
        targetValue = targetSweep,
        animationSpec = tween(durationMillis = 650),
        label = "focus_gauge_sweep"
    )
    val trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 10.dp.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(size.width - strokePx, size.height - strokePx)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            if (animatedSweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(accentColor, accentColor.copy(alpha = 0.72f), accentColor)
                    ),
                    startAngle = -90f,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (hasData) "$scorePct%" else "--",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (hasData) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "FOCUS",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FocusMetricRow(
    dotColor: Color,
    label: String,
    valueText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = valueText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SubjectFocusScoreRow(
    subject: SubjectFocusScore,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onQuickFocusBoost: () -> Unit,
    onOpenLogForSubject: () -> Unit
) {
    val tierColor = focusTierAccentColor(subject.tier)
    val animatedFocusFraction by animateFloatAsState(
        targetValue = if (subject.totalLoggedMinutes > 0) {
            (subject.focusedMinutes.toFloat() / subject.totalLoggedMinutes.toFloat()).coerceIn(0f, 1f)
        } else 0f,
        animationSpec = tween(durationMillis = 550),
        label = "focus_ratio_${subject.courseCode}"
    )
    val animatedShallowFraction by animateFloatAsState(
        targetValue = if (subject.totalLoggedMinutes > 0) {
            (subject.shallowMinutes.toFloat() / subject.totalLoggedMinutes.toFloat()).coerceIn(0f, 1f)
        } else 0f,
        animationSpec = tween(durationMillis = 550),
        label = "shallow_ratio_${subject.courseCode}"
    )

    val trackBaseColor = MaterialTheme.colorScheme.surfaceVariant
    val breakFillColor = ErrorRed.copy(alpha = 0.65f)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (subject.isShallowStudying) {
            ErrorContainer.copy(alpha = 0.25f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (subject.isShallowStudying) {
                ErrorRed.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onToggleExpand)
            .testTag("focus_subject_card_${subject.courseCode}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Course info + Tier Pill + Score %
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = tierColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = subject.courseCode,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = tierColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = subject.courseName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (subject.totalLoggedMinutes > 0) {
                                "${formatMinutesReadable(subject.focusedMinutes)} focused / ${formatMinutesReadable(subject.totalLoggedMinutes)} total"
                            } else {
                                "No sessions logged yet"
                            },
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(100),
                        color = tierColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = subject.tier.label,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = tierColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = if (subject.totalLoggedMinutes > 0) "${subject.focusScorePct}%" else "--",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = tierColor
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Hide study strategies" else "Show study strategies",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Segmented Ratio Bar: Focused | Shallow | Break
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(100))
            ) {
                drawRoundRect(
                    color = trackBaseColor,
                    size = size,
                    cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                )
                if (subject.totalLoggedMinutes > 0) {
                    val focusW = size.width * animatedFocusFraction
                    val shallowW = size.width * animatedShallowFraction
                    val breakFraction = (1f - animatedFocusFraction - animatedShallowFraction).coerceAtLeast(0f)
                    val breakW = size.width * breakFraction

                    if (focusW > 0f) {
                        drawRect(
                            color = EmeraldTertiary,
                            topLeft = Offset.Zero,
                            size = Size(focusW, size.height)
                        )
                    }
                    if (shallowW > 0f) {
                        drawRect(
                            color = WarningAmber,
                            topLeft = Offset(focusW, 0f),
                            size = Size(shallowW, size.height)
                        )
                    }
                    if (breakW > 0f) {
                        drawRect(
                            color = breakFillColor,
                            topLeft = Offset(focusW + shallowW, 0f),
                            size = Size(breakW, size.height)
                        )
                    }
                }
            }

            // Breakdown line
            if (subject.totalLoggedMinutes > 0) {
                Text(
                    text = "Focused: ${subject.focusedMinutes}m • Shallow: ${subject.shallowMinutes}m • Breaks: ${subject.breakMinutes}m",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expandable Diagnosis & Strategy Tips
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 0.8.dp
                    )

                    Text(
                        text = subject.diagnosis,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 15.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, tierColor.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TipsAndUpdates,
                                    contentDescription = null,
                                    tint = tierColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (subject.isShallowStudying) "Recommended Fixes to Stop Shallow Studying"
                                    else "Recommended Focus Strategies",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tierColor
                                )
                            }

                            subject.recommendedStrategies.forEach { tip ->
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "•",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = tierColor
                                    )
                                    Text(
                                        text = tip,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onQuickFocusBoost,
                            shape = RoundedCornerShape(100),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 38.dp)
                                .testTag("btn_focus_boost_${subject.courseCode}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Add 25 minutes deep focus",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+25m Deep Focus",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = onOpenLogForSubject,
                            shape = RoundedCornerShape(100),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .heightIn(min = 38.dp)
                                .testTag("btn_log_quality_${subject.courseCode}")
                        ) {
                            Text(
                                text = "Log Break/Shallow",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendSwatch(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun LogStudyQualityDialog(
    courses: List<Pair<String, String>>,
    initialCourseCode: String,
    initialMode: String,
    onDismiss: () -> Unit,
    onSave: (courseCode: String, courseName: String, mode: String, minutes: Int, notes: String) -> Unit
) {
    var selectedCode by remember { mutableStateOf(initialCourseCode) }
    var selectedMode by remember { mutableStateOf(initialMode) }
    var selectedMinutes by remember { mutableIntStateOf(25) }
    var notes by remember { mutableStateOf("") }

    val modes = listOf(
        Triple("FOCUS", "Deep Focus", EmeraldTertiary),
        Triple("SHALLOW", "Shallow / Passive", WarningAmber),
        Triple("SHORT_BREAK", "Break Drift", ErrorRed)
    )
    val durations = listOf(15, 25, 30, 50)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Study Block Quality",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Subject",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    courses.forEach { (code, _) ->
                        FilterChip(
                            selected = selectedCode == code,
                            onClick = { selectedCode = code },
                            label = { Text(code, fontSize = 11.sp) }
                        )
                    }
                }

                Text(
                    text = "Session Quality",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    modes.forEach { (modeKey, modeLabel, modeColor) ->
                        val isSelected = selectedMode == modeKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMode = modeKey },
                            label = { Text(modeLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = modeColor.copy(alpha = 0.16f),
                                selectedLabelColor = modeColor
                            ),
                            border = if (isSelected) BorderStroke(1.dp, modeColor) else null
                        )
                    }
                }

                Text(
                    text = "Duration",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    durations.forEach { mins ->
                        FilterChip(
                            selected = selectedMinutes == mins,
                            onClick = { selectedMinutes = mins },
                            label = { Text("${mins}m", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it.take(60) },
                    label = { Text("Note (optional)") },
                    placeholder = { Text("e.g., Distracted by phone, Active recall...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val name = courses.firstOrNull { it.first == selectedCode }?.second ?: selectedCode
                    onSave(selectedCode, name, selectedMode, selectedMinutes, notes.trim())
                },
                modifier = Modifier.testTag("btn_confirm_log_quality")
            ) {
                Text("Save Block")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun focusTierAccentColor(tier: FocusQualityTier): Color {
    return when (tier) {
        FocusQualityTier.DEEP_FOCUS -> EmeraldTertiary
        FocusQualityTier.MODERATE -> WarningAmber
        FocusQualityTier.SHALLOW -> ErrorRed
        FocusQualityTier.NO_DATA -> IndigoPrimary
    }
}

private fun formatMinutesReadable(minutes: Int): String {
    if (minutes <= 0) return "0m"
    val hrs = minutes / 60
    val rem = minutes % 60
    return when {
        hrs > 0 && rem > 0 -> "${hrs}h ${rem}m"
        hrs > 0 -> "${hrs}h"
        else -> "${rem}m"
    }
}
