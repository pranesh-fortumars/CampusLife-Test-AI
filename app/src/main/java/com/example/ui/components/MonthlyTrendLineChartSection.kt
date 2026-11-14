package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import com.example.data.local.CourseEntity
import com.example.data.local.MonthlyTrendReport
import com.example.ui.theme.EmeraldOnTertiaryContainer
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.EmeraldTertiaryContainer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.IndigoOnPrimaryContainer
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.IndigoPrimaryContainer
import com.example.ui.theme.OnWarningContainer
import com.example.ui.theme.VioletSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningContainer
import java.util.Locale
import kotlin.math.max

@Composable
fun MonthlyTrendLineChartSection(
    report: MonthlyTrendReport,
    courses: List<CourseEntity>,
    weeklyGoalHours: Double,
    showTargetOverlay: Boolean,
    selectedSubjectFilter: String?,
    onSelectSubjectFilter: (String?) -> Unit,
    subjectColorProvider: (String) -> Color,
    modifier: Modifier = Modifier
) {
    var selectedWeekIndex by remember(report.activeSubjectFilter) { mutableStateOf<Int?>(3) }

    val lineAccentColor = if (selectedSubjectFilter != null) {
        subjectColorProvider(selectedSubjectFilter)
    } else {
        IndigoPrimary
    }

    val isUpward = report.w1ToW4GrowthHours >= 0.5
    val isDeclining = report.w1ToW4GrowthHours <= -0.5

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_trend_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Long-Term Pattern Header & Subject Scope Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .testTag("monthly_subject_filter_row"),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedSubjectFilter == null,
                onClick = { onSelectSubjectFilter(null) },
                label = {
                    Text(
                        text = "All Subjects",
                        fontSize = 11.sp,
                        fontWeight = if (selectedSubjectFilter == null) FontWeight.Bold else FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                },
                shape = RoundedCornerShape(100),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = IndigoPrimaryContainer,
                    selectedLabelColor = IndigoOnPrimaryContainer,
                    selectedLeadingIconColor = IndigoPrimary
                ),
                border = if (selectedSubjectFilter == null) BorderStroke(1.2.dp, IndigoPrimary) else null,
                modifier = Modifier
                    .heightIn(min = 32.dp)
                    .testTag("monthly_filter_all")
            )

            courses.forEach { course ->
                val isSelected = selectedSubjectFilter.equals(course.code, ignoreCase = true)
                val subColor = subjectColorProvider(course.code)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onSelectSubjectFilter(if (isSelected) null else course.code)
                    },
                    label = {
                        Text(
                            text = course.code,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    shape = RoundedCornerShape(100),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = subColor.copy(alpha = 0.16f),
                        selectedLabelColor = subColor
                    ),
                    border = if (isSelected) BorderStroke(1.2.dp, subColor) else null,
                    modifier = Modifier
                        .heightIn(min = 32.dp)
                        .testTag("monthly_filter_${course.code}")
                )
            }
        }

        // 2. Momentum & 4-Week Pattern Insight Banner
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = when {
                isUpward -> EmeraldTertiaryContainer.copy(alpha = 0.48f)
                isDeclining -> WarningContainer.copy(alpha = 0.52f)
                else -> IndigoPrimaryContainer.copy(alpha = 0.45f)
            },
            border = BorderStroke(
                1.dp,
                when {
                    isUpward -> EmeraldTertiary.copy(alpha = 0.4f)
                    isDeclining -> WarningAmber.copy(alpha = 0.45f)
                    else -> IndigoPrimary.copy(alpha = 0.35f)
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("monthly_pattern_insight_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isUpward -> EmeraldTertiary.copy(alpha = 0.18f)
                                        isDeclining -> WarningAmber.copy(alpha = 0.2f)
                                        else -> IndigoPrimary.copy(alpha = 0.18f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    isUpward -> Icons.Default.TrendingUp
                                    isDeclining -> Icons.Default.TrendingDown
                                    else -> Icons.Default.TrendingFlat
                                },
                                contentDescription = report.momentumLabel,
                                tint = when {
                                    isUpward -> EmeraldTertiary
                                    isDeclining -> OnWarningContainer
                                    else -> IndigoPrimary
                                },
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = report.momentumLabel,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(100),
                        color = when {
                            isUpward -> EmeraldTertiary
                            isDeclining -> WarningAmber
                            else -> IndigoPrimary
                        },
                        modifier = Modifier.testTag("monthly_momentum_badge")
                    ) {
                        val sign = if (report.w1ToW4GrowthHours >= 0) "+" else ""
                        Text(
                            text = "${sign}${String.format(Locale.US, "%.1f", report.w1ToW4GrowthHours)}h (${if (report.w1ToW4GrowthPct >= 0) "+" else ""}${report.w1ToW4GrowthPct}% vs Wk 1)",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = report.patternInsight,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                )
            }
        }

        // 3. Legend Row for 4-Week Trend Line
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(lineAccentColor)
                )
                Text(
                    text = if (selectedSubjectFilter != null) "$selectedSubjectFilter Weekly Hrs" else "Weekly Totals (4w)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .height(2.dp)
                        .background(Color(0xFF94A3B8))
                )
                Text(
                    text = "4w Avg (${String.format(Locale.US, "%.1f", report.weeklyAverageHours)}h)",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (showTargetOverlay) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .height(2.dp)
                            .background(WarningAmber)
                    )
                    Text(
                        text = "Goal (${String.format(Locale.US, "%.1f", weeklyGoalHours)}h/wk)",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 4. 4-Week Trend Line Chart Viewport
        val weeks = report.weeks
        val maxHoursAxis = remember(weeks, weeklyGoalHours, showTargetOverlay) {
            val peakHours = weeks.maxOfOrNull { it.totalHours } ?: 0.0
            val goalMax = if (showTargetOverlay) weeklyGoalHours else 0.0
            max(2.0, max(peakHours, goalMax) * 1.25)
        }
        val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        val avgLineColor = Color(0xFF94A3B8)
        val goalLineColor = WarningAmber.copy(alpha = 0.85f)

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("monthly_trend_line_viewport")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Weekly Value Callout Strip aligned over Wk 1..Wk 4
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 26.dp, end = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weeks.forEachIndexed { idx, wk ->
                        val isPeak = report.peakWeek?.weekKey == wk.weekKey
                        val isSelected = selectedWeekIndex == idx
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(58.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(100),
                                color = when {
                                    isSelected -> lineAccentColor
                                    isPeak -> EmeraldTertiaryContainer
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                border = if (!isSelected) BorderStroke(0.8.dp, lineAccentColor.copy(alpha = 0.3f)) else null
                            ) {
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", wk.totalHours)}h",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isSelected -> Color.White
                                        isPeak -> EmeraldOnTertiaryContainer
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                            wk.deltaFromPrevWeekHours?.let { delta ->
                                val pos = delta >= 0
                                Text(
                                    text = "${if (pos) "+" else ""}${String.format(Locale.US, "%.1f", delta)}h",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (pos) EmeraldTertiary else ErrorRed
                                )
                            } ?: Text(
                                text = "Baseline",
                                fontSize = 8.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Trend Line Canvas with Y-Axis Labels
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(165.dp)
                        .testTag("monthly_trend_line_canvas")
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (weeks.size >= 2) {
                            val padLeft = 32.dp.toPx()
                            val padRight = 22.dp.toPx()
                            val padTop = 14.dp.toPx()
                            val padBottom = 14.dp.toPx()
                            val plotW = (size.width - padLeft - padRight).coerceAtLeast(1f)
                            val plotH = (size.height - padTop - padBottom).coerceAtLeast(1f)
                            val stepX = plotW / (weeks.size - 1)

                            // Horizontal Dashed Grid Lines
                            val gridSteps = 3
                            for (i in 0..gridSteps) {
                                val y = padTop + plotH - (plotH * (i.toFloat() / gridSteps))
                                drawLine(
                                    color = gridColor,
                                    start = Offset(padLeft, y),
                                    end = Offset(size.width - padRight, y),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                                )
                            }

                            // 4-Week Average Horizontal Baseline
                            val avgNorm = (report.weeklyAverageHours / maxHoursAxis).toFloat().coerceIn(0f, 1f)
                            val avgY = padTop + plotH - (avgNorm * plotH)
                            drawLine(
                                color = avgLineColor.copy(alpha = 0.75f),
                                start = Offset(padLeft, avgY),
                                end = Offset(size.width - padRight, avgY),
                                strokeWidth = 1.2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            )

                            // Weekly Target Goal Line
                            if (showTargetOverlay) {
                                val goalNorm = (weeklyGoalHours / maxHoursAxis).toFloat().coerceIn(0f, 1f)
                                val goalY = padTop + plotH - (goalNorm * plotH)
                                drawLine(
                                    color = goalLineColor,
                                    start = Offset(padLeft, goalY),
                                    end = Offset(size.width - padRight, goalY),
                                    strokeWidth = 1.6.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 7f), 0f)
                                )
                            }

                            val points = weeks.mapIndexed { idx, wk ->
                                val x = padLeft + idx * stepX
                                val norm = (wk.totalHours / maxHoursAxis).toFloat().coerceIn(0.02f, 0.98f)
                                val y = padTop + plotH - (norm * plotH)
                                Offset(x, y)
                            }

                            // Vertical Selection Guide
                            selectedWeekIndex?.let { selIdx ->
                                if (selIdx in points.indices) {
                                    drawLine(
                                        color = lineAccentColor.copy(alpha = 0.3f),
                                        start = Offset(points[selIdx].x, padTop),
                                        end = Offset(points[selIdx].x, padTop + plotH),
                                        strokeWidth = 2.dp.toPx()
                                    )
                                }
                            }

                            // Smooth Gradient Area Fill below Trend Line
                            val areaPath = Path().apply {
                                moveTo(points.first().x, padTop + plotH)
                                lineTo(points.first().x, points.first().y)
                                for (i in 1 until points.size) {
                                    val prev = points[i - 1]
                                    val cur = points[i]
                                    val midX = (prev.x + cur.x) / 2f
                                    cubicTo(midX, prev.y, midX, cur.y, cur.x, cur.y)
                                }
                                lineTo(points.last().x, padTop + plotH)
                                close()
                            }
                            drawPath(
                                path = areaPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        lineAccentColor.copy(alpha = 0.34f),
                                        lineAccentColor.copy(alpha = 0.03f)
                                    ),
                                    startY = padTop,
                                    endY = padTop + plotH
                                )
                            )

                            // Smooth 4-Week Trend Line Curve
                            val linePath = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 1 until points.size) {
                                    val prev = points[i - 1]
                                    val cur = points[i]
                                    val midX = (prev.x + cur.x) / 2f
                                    cubicTo(midX, prev.y, midX, cur.y, cur.x, cur.y)
                                }
                            }
                            drawPath(
                                path = linePath,
                                color = lineAccentColor,
                                style = Stroke(
                                    width = 3.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )

                            // Data Point Nodes
                            points.forEachIndexed { idx, pt ->
                                val isSelected = selectedWeekIndex == idx
                                val isPeak = report.peakWeek?.weekKey == weeks[idx].weekKey
                                val nodeColor = if (isPeak && !isSelected) EmeraldTertiary else lineAccentColor

                                drawCircle(
                                    color = nodeColor.copy(alpha = if (isSelected) 0.32f else 0.18f),
                                    radius = if (isSelected) 9.dp.toPx() else 6.5.dp.toPx(),
                                    center = pt
                                )
                                drawCircle(
                                    color = nodeColor,
                                    radius = if (isSelected) 5.dp.toPx() else 4.dp.toPx(),
                                    center = pt
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = if (isSelected) 2.2.dp.toPx() else 1.8.dp.toPx(),
                                    center = pt
                                )
                            }
                        }
                    }

                    // Y-Axis Ticks
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${String.format(Locale.US, "%.0f", maxHoursAxis)}h",
                            fontSize = 8.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.0f", maxHoursAxis / 2.0)}h",
                            fontSize = 8.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "0h",
                            fontSize = 8.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Tappable X-Axis Week Nodes (Wk 1 .. Wk 4)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 26.dp, end = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weeks.forEachIndexed { idx, wk ->
                        val isSelected = selectedWeekIndex == idx
                        val isPeak = report.peakWeek?.weekKey == wk.weekKey
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) lineAccentColor.copy(alpha = 0.14f) else Color.Transparent,
                            border = if (isSelected) BorderStroke(1.dp, lineAccentColor.copy(alpha = 0.55f)) else null,
                            modifier = Modifier
                                .width(58.dp)
                                .heightIn(min = 38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedWeekIndex = if (isSelected) null else idx
                                }
                                .testTag("monthly_week_node_${wk.weekKey}")
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = wk.weekKey,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSelected || isPeak) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isSelected) lineAccentColor else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isPeak) {
                                        Icon(
                                            imageVector = Icons.Default.EmojiEvents,
                                            contentDescription = "Peak week",
                                            tint = WarningAmber,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = wk.shortSubtitle,
                                    fontSize = 8.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Selected Week Spotlight Tooltip Card
        val activeWeek = selectedWeekIndex?.let { weeks.getOrNull(it) }
        AnimatedVisibility(
            visible = activeWeek != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            activeWeek?.let { wk ->
                val meetsGoal = wk.totalHours >= weeklyGoalHours
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (meetsGoal) EmeraldTertiary.copy(alpha = 0.5f) else lineAccentColor.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("monthly_week_spotlight_card")
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
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "${wk.weekLabel} • ${String.format(Locale.US, "%.1f", wk.totalHours)}h",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val deltaText = wk.deltaFromPrevWeekHours?.let { d ->
                                    val sign = if (d >= 0) "+" else ""
                                    " • ${sign}${String.format(Locale.US, "%.1f", d)}h vs prior wk"
                                } ?: " • Starting baseline week"
                                Text(
                                    text = "${wk.sessionCount} focus blocks • Lead: ${wk.primarySubject}$deltaText",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = { selectedWeekIndex = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close week details",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        if (wk.subjectBreakdown.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                wk.subjectBreakdown.entries
                                    .filter { it.value > 0 }
                                    .sortedByDescending { it.value }
                                    .forEach { (code, mins) ->
                                        val subColor = subjectColorProvider(code)
                                        val isFiltered = selectedSubjectFilter.equals(code, ignoreCase = true)
                                        Surface(
                                            shape = RoundedCornerShape(100),
                                            color = if (isFiltered) subColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(1.dp, if (isFiltered) subColor else subColor.copy(alpha = 0.35f)),
                                            modifier = Modifier.clickable {
                                                onSelectSubjectFilter(if (isFiltered) null else code)
                                            }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(subColor)
                                                )
                                                Text(
                                                    text = "$code: ${String.format(Locale.US, "%.1f", mins / 60.0)}h",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isFiltered) subColor else MaterialTheme.colorScheme.onSurface
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
