package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AssessmentType
import com.example.model.ClassGradeSummary
import com.example.model.CourseAssessmentItem
import com.example.model.CourseGradeMockData
import com.example.ui.theme.*
import java.util.Locale

/**
 * Interactive Course Grade Tracker component that enables students to input assignments
 * and exams with custom weights to calculate their current GPA and required final scores for each class.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseGradeTrackerComponent(
    initialClasses: List<ClassGradeSummary> = CourseGradeMockData.defaultClassesWithAssessments,
    modifier: Modifier = Modifier
) {
    var classesState by remember { mutableStateOf(initialClasses) }
    var selectedCourseCode by remember {
        mutableStateOf(classesState.firstOrNull()?.courseCode ?: "CS-302")
    }

    val activeClass = remember(classesState, selectedCourseCode) {
        classesState.find { it.courseCode == selectedCourseCode } ?: classesState.first()
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    // Target Grade Goal calculation (e.g. required score on pending final exam for Grade O / A+)
    val requiredForO = activeClass.requiredScoreForTarget(90.0)
    val requiredForAPlus = activeClass.requiredScoreForTarget(80.0)

    val animatedGpaProgress by animateFloatAsState(
        targetValue = (activeClass.currentClassGpa / 10.0).toFloat().coerceIn(0f, 1f),
        label = "class_gpa_progress"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("course_grade_tracker_component")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Course Grade & GPA Tracker",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Weighted assignments, exams & class GPA calculator",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(100),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_course_assessment_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Item", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }

            // Class Selector Chips (Horizontal Scroll)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(classesState) { item ->
                    val isSelected = item.courseCode == selectedCourseCode
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Primary else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, if (isSelected) Primary else OutlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clickable { selectedCourseCode = item.courseCode }
                            .testTag("class_tab_${item.courseCode.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = item.courseCode,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Primary
                                )
                                Text(
                                    text = "GPA ${item.formattedGpa}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(100),
                                color = if (isSelected) Color.White.copy(alpha = 0.25f) else TertiaryContainer
                            ) {
                                Text(
                                    text = item.currentGradeLetter,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Tertiary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // High-Impact Class GPA Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, Primary.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Primary))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${activeClass.courseCode} • ${activeClass.courseName}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = activeClass.formattedGpa,
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.testTag("active_class_gpa_text")
                                )
                                Text(
                                    text = " / 10.0 GPA",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Primary,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    Text(
                                        text = "Grade ${activeClass.currentGradeLetter}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Graded Score",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = activeClass.formattedPercentage,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Tertiary
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.0f", activeClass.totalWeightGraded)}% weight graded",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress bar representing graded percentage
                    LinearProgressIndicator(
                        progress = { (activeClass.totalWeightGraded / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(100)),
                        color = Primary,
                        trackColor = Primary.copy(alpha = 0.15f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Target Forecaster
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (requiredForO != null) {
                                        "Target Grade O (10 GPA): Score >= ${String.format(Locale.US, "%.1f%%", requiredForO)} on pending final exam (${String.format(Locale.US, "%.0f%%", activeClass.totalWeightPending)} weight)"
                                    } else if (requiredForAPlus != null) {
                                        "Target Grade A+ (9 GPA): Score >= ${String.format(Locale.US, "%.1f%%", requiredForAPlus)} on remaining assignments"
                                    } else {
                                        "All major coursework evaluated! Final semester grade secured."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Assessment List Label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Assessments, Quizzes & Exams (${activeClass.assessments.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Total Weight: ${String.format(Locale.US, "%.0f%%", activeClass.totalWeightAll)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Assessment Items List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activeClass.assessments.forEachIndexed { index, assessment ->
                    AssessmentItemCard(
                        item = assessment,
                        onDelete = {
                            classesState = classesState.map { cls ->
                                if (cls.courseCode == activeClass.courseCode) {
                                    cls.copy(assessments = cls.assessments.filter { it.id != assessment.id })
                                } else cls
                            }
                            feedbackMessage = "Removed '${assessment.title}'"
                        },
                        onToggleCompleted = {
                            classesState = classesState.map { cls ->
                                if (cls.courseCode == activeClass.courseCode) {
                                    cls.copy(
                                        assessments = cls.assessments.map {
                                            if (it.id == assessment.id) it.copy(isCompleted = !it.isCompleted) else it
                                        }
                                    )
                                } else cls
                            }
                        },
                        index = index
                    )
                }
            }

            // In-component status feedback
            AnimatedVisibility(visible = feedbackMessage != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TertiaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Tertiary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feedbackMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Tertiary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { feedbackMessage = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }

    // Modal Add Assessment Dialog
    if (showAddDialog) {
        AddAssessmentDialog(
            courseCode = activeClass.courseCode,
            onDismiss = { showAddDialog = false },
            onAdd = { newItem ->
                classesState = classesState.map { cls ->
                    if (cls.courseCode == activeClass.courseCode) {
                        cls.copy(assessments = cls.assessments + newItem)
                    } else cls
                }
                showAddDialog = false
                feedbackMessage = "Added ${newItem.type.displayName} '${newItem.title}' (${newItem.formattedWeight} weight)!"
            }
        )
    }
}

/**
 * Individual Card representing an assignment or exam row with weight and marks.
 */
@Composable
fun AssessmentItemCard(
    item: CourseAssessmentItem,
    onDelete: () -> Unit,
    onToggleCompleted: () -> Unit,
    index: Int
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("assessment_card_$index")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon & Category
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (item.isCompleted) PrimaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Text(item.type.iconEmoji, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Main Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = "${item.formattedWeight} Weight",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    if (item.isCompleted) {
                        Text(
                            text = "Score: ${item.formattedScore} (${item.formattedPercentage})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    } else {
                        Text(
                            text = "Pending • Due ${item.dueDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Tertiary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Weighted Points Contribution & Delete
            Column(horizontalAlignment = Alignment.End) {
                if (item.isCompleted) {
                    Text(
                        text = "+${String.format(Locale.US, "%.1f", item.weightedContribution)} pts",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Tertiary
                    )
                } else {
                    Text(
                        text = "Upcoming",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp).testTag("delete_assessment_$index")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete assessment",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Modal Dialog for students to input new assignments, mid-terms, and exams with weights.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssessmentDialog(
    courseCode: String,
    onDismiss: () -> Unit,
    onAdd: (CourseAssessmentItem) -> Unit
) {
    var title by remember { mutableStateOf("Assignment 2: Query Optimization") }
    var selectedType by remember { mutableStateOf(AssessmentType.ASSIGNMENT) }
    var weightInput by remember { mutableStateOf("15") }
    var scoreInput by remember { mutableStateOf("18.5") }
    var maxScoreInput by remember { mutableStateOf("20") }
    var isCompleted by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_assessment_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Add Assessment / Exam",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "For Course $courseCode",
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Type Segment
                Text("Category:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(AssessmentType.values()) { type ->
                        val isSelected = selectedType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedType = type
                                weightInput = String.format(Locale.US, "%.0f", type.defaultWeight)
                            },
                            label = { Text("${type.iconEmoji} ${type.displayName}", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Assessment Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_assessment_title")
                )

                // Weight % Input
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Weight Percentage (% of Course Grade)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("input_assessment_weight")
                )

                // Score Earned & Max Score
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = scoreInput,
                        onValueChange = { scoreInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Score Earned") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("input_score_earned")
                    )
                    OutlinedTextField(
                        value = maxScoreInput,
                        onValueChange = { maxScoreInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Max Score") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("input_max_score")
                    )
                }

                // Graded Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = { isCompleted = it },
                        modifier = Modifier.testTag("checkbox_is_completed")
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Already Graded (Include in Current GPA)", style = MaterialTheme.typography.bodySmall)
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val weight = weightInput.toDoubleOrNull() ?: 15.0
                            val score = scoreInput.toDoubleOrNull() ?: 0.0
                            val max = maxScoreInput.toDoubleOrNull() ?: 100.0
                            if (title.isNotBlank() && max > 0) {
                                onAdd(
                                    CourseAssessmentItem(
                                        courseCode = courseCode,
                                        title = title.trim(),
                                        type = selectedType,
                                        weightPercentage = weight,
                                        scoreEarned = score,
                                        maxScore = max,
                                        isCompleted = isCompleted,
                                        dueDate = if (isCompleted) "Graded" else "Upcoming"
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier.testTag("save_assessment_button")
                    ) {
                        Text("Save Assessment")
                    }
                }
            }
        }
    }
}
