package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CourseEntity
import com.example.data.local.StudyDateRange
import com.example.data.local.SubjectComparisonReport
import com.example.data.local.SubjectMasteryEntity
import com.example.data.local.SubjectStudyStats
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
import kotlin.math.abs
import kotlin.math.max

enum class CompareOverlayMetric(val label: String) {
    HOURS("Study Hours"),
    MASTERY("4-Wk Mastery")
}

val CompareSubjectAColor = IndigoPrimary
val CompareSubjectBColor = Color(0xFFEA580C)

@Composable
fun SubjectComparisonOverlaySection(
    report: SubjectComparisonReport,
    availableCourses: List<CourseEntity>,
    studyStats: List<SubjectStudyStats>,
    masteryMap: Map<String, List<SubjectMasteryEntity>>,
    defaultMasteryProvider: (String) -> List<SubjectMasteryEntity>,
    dateRange: StudyDateRange,
    onSelectSubjectA: (String) -> Unit,
    onSelectSubjectB: (String) -> Unit,
    onSwapSubjects: () -> Unit,
    onBoostLaggingSubject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var overlayMetric by remember { mutableStateOf(CompareOverlayMetric.HOURS) }
    var selectedBucketIndex by remember(report.dateRange, report.subjectA.courseCode, report.subjectB.courseCode, overlayMetric) {
        mutableStateOf<Int?>(null)
    }
    var showMenuA by remember { mutableStateOf(false) }
    var showMenuB by remember { mutableStateOf(false) }

    val subjectACode = report.subjectA.courseCode
    val subjectBCode = report.subjectB.courseCode
    val courseChoices = remember(availableCourses, studyStats) {
        val codes = (availableCourses.map { it.code } + studyStats.map { it.courseCode }).distinct()
        codes.map { code ->
            val name = availableCourses.firstOrNull { it.code == code }?.name ?: code
            val hrs = (studyStats.firstOrNull { it.courseCode == code }?.totalHours) ?: 0.0
            Triple(code, name, hrs)
        }
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
        ),
        border = BorderStroke(1.2.dp, VioletSecondary.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("compare_subjects_panel")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Section Header + Overlay Metric Switcher
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
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(VioletSecondary.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = "Subject Comparison Overlay",
                            tint = VioletSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Compare Subjects Overlay",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${dateRange.label} • Side-by-side efficiency gap check",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Hours vs Mastery Overlay Metric Toggle
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    CompareOverlayMetric.values().forEach { metric ->
                        val active = overlayMetric == metric
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = if (active) VioletSecondary else Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(100))
                                .clickable { overlayMetric = metric }
                                .testTag("compare_overlay_mode_${metric.name.lowercase()}")
                        ) {
                            Text(
                                text = metric.label,
                                fontSize = 10.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // Dual Subject Selector Row (Subject A <-> Swap <-> Subject B)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("compare_subjects_selector_bar"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Subject A Picker
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CompareSubjectAColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.2.dp, CompareSubjectAColor.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showMenuA = true }
                            .testTag("selector_compare_subject_a")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(CompareSubjectAColor)
                                )
                                Column {
                                    Text(
                                        text = "Subject A",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = CompareSubjectAColor
                                    )
                                    Text(
                                        text = "$subjectACode (${String.format(Locale.US, "%.1f", report.subjectA.loggedHours)}h)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Subject A",
                                tint = CompareSubjectAColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showMenuA,
                        onDismissRequest = { showMenuA = false }
                    ) {
                        courseChoices.forEach { (code, name, hrs) ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "$code • ${String.format(Locale.US, "%.1f", hrs)}h",
                                            fontWeight = if (code == subjectACode) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = name,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                },
                                onClick = {
                                    onSelectSubjectA(code)
                                    showMenuA = false
                                },
                                modifier = Modifier.testTag("compare_a_option_$code")
                            )
                        }
                    }
                }

                // Swap Button (48dp touch target)
                FilledTonalIconButton(
                    onClick = onSwapSubjects,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_swap_compare_subjects")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Swap compared subjects",
                        tint = VioletSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Subject B Picker
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CompareSubjectBColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.2.dp, CompareSubjectBColor.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showMenuB = true }
                            .testTag("selector_compare_subject_b")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(CompareSubjectBColor)
                                )
                                Column {
                                    Text(
                                        text = "Subject B",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = CompareSubjectBColor
                                    )
                                    Text(
                                        text = "$subjectBCode (${String.format(Locale.US, "%.1f", report.subjectB.loggedHours)}h)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Subject B",
                                tint = CompareSubjectBColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showMenuB,
                        onDismissRequest = { showMenuB = false }
                    ) {
                        courseChoices.forEach { (code, name, hrs) ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "$code • ${String.format(Locale.US, "%.1f", hrs)}h",
                                            fontWeight = if (code == subjectBCode) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = name,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                },
                                onClick = {
                                    onSelectSubjectB(code)
                                    showMenuB = false
                                },
                                modifier = Modifier.testTag("compare_b_option_$code")
                            )
                        }
                    }
                }
            }

            // Prepare series points for either Hours Overlay (period buckets) or Mastery Overlay (W1-W4 Room DB)
            data class OverlayPoint(
                val key: String,
                val label: String,
                val valueA: Double,
                val valueB: Double,
                val unit: String
            )

            val overlayPoints = remember(report, overlayMetric, masteryMap) {
                if (overlayMetric == CompareOverlayMetric.HOURS) {
                    report.bucketPoints.map { pt ->
                        OverlayPoint(
                            key = pt.bucketKey,
                            label = pt.bucketLabel,
                            valueA = pt.subjectAHours,
                            valueB = pt.subjectBHours,
                            unit = "h"
                        )
                    }
                } else {
                    val mA = (masteryMap[subjectACode]?.takeIf { it.isNotEmpty() } ?: defaultMasteryProvider(subjectACode))
                        .sortedBy { it.weekNumber }
                    val mB = (masteryMap[subjectBCode]?.takeIf { it.isNotEmpty() } ?: defaultMasteryProvider(subjectBCode))
                        .sortedBy { it.weekNumber }
                    val weeks = listOf(1, 2, 3, 4)
                    weeks.map { wk ->
                        val recA = mA.firstOrNull { it.weekNumber == wk }
                        val recB = mB.firstOrNull { it.weekNumber == wk }
                        OverlayPoint(
                            key = "W$wk",
                            label = "Week $wk Mastery",
                            valueA = recA?.masteryPercentage ?: 0.0,
                            valueB = recB?.masteryPercentage ?: 0.0,
                            unit = "%"
                        )
                    }
                }
            }

            // Dual-Curve Overlaid Performance Canvas Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("compare_subjects_overlay_chart")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Overlay Legend Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(14.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(CompareSubjectAColor)
                                )
                                Text(
                                    text = subjectACode,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CompareSubjectAColor
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(14.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(CompareSubjectBColor)
                                )
                                Text(
                                    text = subjectBCode,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CompareSubjectBColor
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(VioletSecondary.copy(alpha = 0.25f))
                                        .border(0.8.dp, VioletSecondary.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                                )
                                Text(
                                    text = "Gap Band",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = if (overlayMetric == CompareOverlayMetric.HOURS) "Hours / bucket" else "Target: 85%",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Overlay Line & Gap Fill Canvas
                    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                    val gapFillColor = VioletSecondary.copy(alpha = 0.14f)
                    val maxAxisVal = remember(overlayPoints, overlayMetric) {
                        if (overlayMetric == CompareOverlayMetric.MASTERY) {
                            100.0
                        } else {
                            max(2.0, (overlayPoints.maxOfOrNull { max(it.valueA, it.valueB) } ?: 2.0) * 1.2)
                        }
                    }
                    val minAxisVal = if (overlayMetric == CompareOverlayMetric.MASTERY) 35.0 else 0.0

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(145.dp)
                            .testTag("compare_subjects_overlay_canvas")
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            if (overlayPoints.size >= 2) {
                                val padLeft = 28.dp.toPx()
                                val padRight = 14.dp.toPx()
                                val padTop = 12.dp.toPx()
                                val padBottom = 14.dp.toPx()
                                val plotW = (size.width - padLeft - padRight).coerceAtLeast(1f)
                                val plotH = (size.height - padTop - padBottom).coerceAtLeast(1f)
                                val stepX = plotW / (overlayPoints.size - 1)
                                val valueSpan = (maxAxisVal - minAxisVal).coerceAtLeast(1.0)

                                // Horizontal Grid lines
                                val gridCount = 3
                                for (i in 0..gridCount) {
                                    val y = padTop + plotH - (plotH * (i.toFloat() / gridCount))
                                    drawLine(
                                        color = gridColor,
                                        start = Offset(padLeft, y),
                                        end = Offset(size.width - padRight, y),
                                        strokeWidth = 1.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                                    )
                                }

                                // 85% Mastery target line in Mastery mode
                                if (overlayMetric == CompareOverlayMetric.MASTERY) {
                                    val targetNorm = ((85.0 - minAxisVal) / valueSpan).toFloat().coerceIn(0f, 1f)
                                    val targetY = padTop + plotH - (targetNorm * plotH)
                                    drawLine(
                                        color = WarningAmber.copy(alpha = 0.8f),
                                        start = Offset(padLeft, targetY),
                                        end = Offset(size.width - padRight, targetY),
                                        strokeWidth = 1.3.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                                    )
                                }

                                val pointsA = overlayPoints.mapIndexed { idx, pt ->
                                    val x = padLeft + idx * stepX
                                    val norm = ((pt.valueA - minAxisVal) / valueSpan).toFloat().coerceIn(0f, 1f)
                                    val y = padTop + plotH - (norm * plotH)
                                    Offset(x, y)
                                }
                                val pointsB = overlayPoints.mapIndexed { idx, pt ->
                                    val x = padLeft + idx * stepX
                                    val norm = ((pt.valueB - minAxisVal) / valueSpan).toFloat().coerceIn(0f, 1f)
                                    val y = padTop + plotH - (norm * plotH)
                                    Offset(x, y)
                                }

                                // Selected bucket vertical guide
                                selectedBucketIndex?.let { selIdx ->
                                    if (selIdx in pointsA.indices) {
                                        val selX = pointsA[selIdx].x
                                        drawLine(
                                            color = VioletSecondary.copy(alpha = 0.45f),
                                            start = Offset(selX, padTop),
                                            end = Offset(selX, padTop + plotH),
                                            strokeWidth = 2.dp.toPx()
                                        )
                                    }
                                }

                                // Shaded Efficiency Gap Polygon between Curve A and Curve B
                                val gapPath = Path().apply {
                                    moveTo(pointsA.first().x, pointsA.first().y)
                                    for (i in 1 until pointsA.size) {
                                        lineTo(pointsA[i].x, pointsA[i].y)
                                    }
                                    for (i in pointsB.indices.reversed()) {
                                        lineTo(pointsB[i].x, pointsB[i].y)
                                    }
                                    close()
                                }
                                drawPath(path = gapPath, color = gapFillColor)

                                // Subject B subtle fill under curve
                                val fillB = Path().apply {
                                    moveTo(pointsB.first().x, padTop + plotH)
                                    pointsB.forEach { lineTo(it.x, it.y) }
                                    lineTo(pointsB.last().x, padTop + plotH)
                                    close()
                                }
                                drawPath(
                                    path = fillB,
                                    brush = Brush.verticalGradient(
                                        listOf(CompareSubjectBColor.copy(alpha = 0.14f), Color.Transparent)
                                    )
                                )

                                // Subject A Line (Solid Indigo)
                                val pathA = Path().apply {
                                    moveTo(pointsA.first().x, pointsA.first().y)
                                    for (i in 1 until pointsA.size) lineTo(pointsA[i].x, pointsA[i].y)
                                }
                                drawPath(
                                    path = pathA,
                                    color = CompareSubjectAColor,
                                    style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )

                                // Subject B Line (Vibrant Coral with crisp dash accent)
                                val pathB = Path().apply {
                                    moveTo(pointsB.first().x, pointsB.first().y)
                                    for (i in 1 until pointsB.size) lineTo(pointsB[i].x, pointsB[i].y)
                                }
                                drawPath(
                                    path = pathB,
                                    color = CompareSubjectBColor,
                                    style = Stroke(
                                        width = 2.5.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 6f), 0f)
                                    )
                                )

                                // Data nodes
                                pointsA.forEachIndexed { idx, pt ->
                                    val highlighted = selectedBucketIndex == idx
                                    drawCircle(
                                        color = CompareSubjectAColor.copy(alpha = if (highlighted) 0.35f else 0.2f),
                                        radius = if (highlighted) 7.dp.toPx() else 5.dp.toPx(),
                                        center = pt
                                    )
                                    drawCircle(
                                        color = CompareSubjectAColor,
                                        radius = if (highlighted) 4.2.dp.toPx() else 3.2.dp.toPx(),
                                        center = pt
                                    )
                                    drawCircle(color = Color.White, radius = 1.5.dp.toPx(), center = pt)
                                }

                                pointsB.forEachIndexed { idx, pt ->
                                    val highlighted = selectedBucketIndex == idx
                                    drawCircle(
                                        color = CompareSubjectBColor.copy(alpha = if (highlighted) 0.35f else 0.2f),
                                        radius = if (highlighted) 7.dp.toPx() else 5.dp.toPx(),
                                        center = pt
                                    )
                                    drawCircle(
                                        color = CompareSubjectBColor,
                                        radius = if (highlighted) 4.2.dp.toPx() else 3.2.dp.toPx(),
                                        center = pt
                                    )
                                    drawCircle(color = Color.White, radius = 1.5.dp.toPx(), center = pt)
                                }
                            }
                        }

                        // Y-axis mini scale hints on left
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            val unit = if (overlayMetric == CompareOverlayMetric.HOURS) "h" else "%"
                            Text(
                                text = "${String.format(Locale.US, "%.0f", maxAxisVal)}$unit",
                                fontSize = 8.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.0f", (maxAxisVal + minAxisVal) / 2.0)}$unit",
                                fontSize = 8.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.0f", minAxisVal)}$unit",
                                fontSize = 8.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Interactive Tappable Bucket Labels along X-Axis
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        overlayPoints.forEachIndexed { idx, pt ->
                            val isSelected = selectedBucketIndex == idx
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) VioletSecondary.copy(alpha = 0.18f) else Color.Transparent,
                                modifier = Modifier
                                    .clickable {
                                        selectedBucketIndex = if (isSelected) null else idx
                                    }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                    .testTag("compare_bucket_${pt.key}")
                            ) {
                                Text(
                                    text = pt.key,
                                    fontSize = 9.5.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) VioletSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    // Tapped Bucket Detail Callout
                    val activePoint = selectedBucketIndex?.let { overlayPoints.getOrNull(it) }
                    AnimatedVisibility(visible = activePoint != null, enter = fadeIn(), exit = fadeOut()) {
                        activePoint?.let { pt ->
                            val diff = pt.valueA - pt.valueB
                            val diffStr = "${if (diff >= 0) "+" else ""}${String.format(Locale.US, "%.1f", diff)}${pt.unit}"
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("compare_bucket_callout")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = pt.label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$subjectACode ${String.format(Locale.US, "%.1f", pt.valueA)}${pt.unit} vs $subjectBCode ${String.format(Locale.US, "%.1f", pt.valueB)}${pt.unit} (Δ $diffStr)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = VioletSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Overlaid Dual-Track Performance Metrics (Goal Attainment, Mastery, Focus Depth)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("compare_subjects_metrics_overlay")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Head-to-Head Overlaid Metrics (${dateRange.label})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OverlaidDualBarMetricRow(
                        metricTitle = "Goal Attainment",
                        subjectACode = subjectACode,
                        subjectAValueText = "${report.subjectA.goalAttainmentPct}% (${String.format(Locale.US, "%.1f", report.subjectA.loggedHours)}/${String.format(Locale.US, "%.0f", report.subjectA.goalHours)}h)",
                        subjectAFraction = (report.subjectA.goalAttainmentPct / 100f).coerceIn(0.03f, 1f),
                        subjectBCode = subjectBCode,
                        subjectBValueText = "${report.subjectB.goalAttainmentPct}% (${String.format(Locale.US, "%.1f", report.subjectB.loggedHours)}/${String.format(Locale.US, "%.0f", report.subjectB.goalHours)}h)",
                        subjectBFraction = (report.subjectB.goalAttainmentPct / 100f).coerceIn(0.03f, 1f),
                        gapBadge = "${report.attainmentGapPct}% gap"
                    )

                    OverlaidDualBarMetricRow(
                        metricTitle = "Mastery Level (Room 4w)",
                        subjectACode = subjectACode,
                        subjectAValueText = "${report.subjectA.latestMasteryPct.toInt()}% (+${report.subjectA.masteryGainPct.toInt()}%)",
                        subjectAFraction = (report.subjectA.latestMasteryPct / 100.0).toFloat().coerceIn(0.03f, 1f),
                        subjectBCode = subjectBCode,
                        subjectBValueText = "${report.subjectB.latestMasteryPct.toInt()}% (+${report.subjectB.masteryGainPct.toInt()}%)",
                        subjectBFraction = (report.subjectB.latestMasteryPct / 100.0).toFloat().coerceIn(0.03f, 1f),
                        gapBadge = "${String.format(Locale.US, "%.0f", report.masteryGapPct)}% gap"
                    )

                    OverlaidDualBarMetricRow(
                        metricTitle = "Focus Depth & Yield",
                        subjectACode = subjectACode,
                        subjectAValueText = "${report.subjectA.avgSessionMinutes}m/blk • ${report.subjectA.sessionCount} sess",
                        subjectAFraction = (report.subjectA.avgSessionMinutes / 60f).coerceIn(0.05f, 1f),
                        subjectBCode = subjectBCode,
                        subjectBValueText = "${report.subjectB.avgSessionMinutes}m/blk • ${report.subjectB.sessionCount} sess",
                        subjectBFraction = (report.subjectB.avgSessionMinutes / 60f).coerceIn(0.05f, 1f),
                        gapBadge = "${report.avgSessionGapMins}m gap"
                    )
                }
            }

            // Efficiency Gap Diagnostic Insight Card
            val isBalanced = report.efficiencyGapType == "Balanced Pace"
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isBalanced) EmeraldTertiaryContainer.copy(alpha = 0.45f) else WarningContainer.copy(alpha = 0.52f),
                border = BorderStroke(
                    1.dp,
                    if (isBalanced) EmeraldTertiary.copy(alpha = 0.4f) else WarningAmber.copy(alpha = 0.45f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("efficiency_gap_diagnostic_card")
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isBalanced) Icons.Default.TrendingUp else Icons.Default.Insights,
                                contentDescription = null,
                                tint = if (isBalanced) EmeraldTertiary else OnWarningContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Efficiency Gap Analysis",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(100),
                            color = if (isBalanced) EmeraldTertiary else WarningAmber
                        ) {
                            Text(
                                text = report.efficiencyGapType,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // 3 Gap Stat Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ComparisonDeltaPill(
                            label = "Hours Gap",
                            value = "${String.format(Locale.US, "%.1f", report.hoursGap)}h",
                            modifier = Modifier.weight(1f)
                        )
                        ComparisonDeltaPill(
                            label = "Goal Gap",
                            value = "${report.attainmentGapPct}%",
                            modifier = Modifier.weight(1f)
                        )
                        ComparisonDeltaPill(
                            label = "Mastery Gap",
                            value = "${String.format(Locale.US, "%.0f", report.masteryGapPct)}%",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(
                        text = report.efficiencyGapSummary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        modifier = Modifier.testTag("efficiency_gap_summary_text")
                    )

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
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Yield: ${subjectACode} ${report.subjectA.masteryPerHour} vs ${subjectBCode} ${report.subjectB.masteryPerHour} pts/hr",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (!isBalanced) {
                            Button(
                                onClick = { onBoostLaggingSubject(report.laggingCourseCode) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(100),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (report.laggingCourseCode == subjectBCode) CompareSubjectBColor else CompareSubjectAColor
                                ),
                                modifier = Modifier
                                    .heightIn(min = 34.dp)
                                    .testTag("btn_boost_lagging_subject")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreTime,
                                    contentDescription = "Add 30 minutes to lagging subject",
                                    modifier = Modifier.size(13.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+30m ${report.laggingCourseCode}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverlaidDualBarMetricRow(
    metricTitle: String,
    subjectACode: String,
    subjectAValueText: String,
    subjectAFraction: Float,
    subjectBCode: String,
    subjectBValueText: String,
    subjectBFraction: Float,
    gapBadge: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = metricTitle,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                shape = RoundedCornerShape(100),
                color = VioletSecondary.copy(alpha = 0.14f)
            ) {
                Text(
                    text = gapBadge,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = VioletSecondary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }
        }

        // Overlaid Dual Progress Track
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(100))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(subjectAFraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(100))
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF818CF8), CompareSubjectAColor))
                        )
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(100))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(subjectBFraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(100))
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFFFB923C), CompareSubjectBColor))
                        )
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$subjectACode: $subjectAValueText",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = CompareSubjectAColor
            )
            Text(
                text = "$subjectBCode: $subjectBValueText",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = CompareSubjectBColor
            )
        }
    }
}

@Composable
private fun ComparisonDeltaPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 8.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = value,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}
