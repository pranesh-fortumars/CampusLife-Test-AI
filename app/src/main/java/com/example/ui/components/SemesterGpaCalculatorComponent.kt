package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CourseGrade
import com.example.model.GradeScale
import com.example.model.SemesterGrades
import com.example.ui.theme.*
import java.util.Locale
import java.util.UUID

/**
 * Temporary item used for dynamic calculation in the Semester GPA Calculator.
 */
data class CalculatorCourseItem(
    val id: String = UUID.randomUUID().toString(),
    val courseCode: String,
    val courseName: String,
    val credits: Int,
    val grade: String
) {
    val gradePoint: Double
        get() = GradeScale.getPoint(grade)

    val qualityPoints: Double
        get() = credits * gradePoint
}

/**
 * Interactive Semester GPA Calculator that enables students to input course grades
 * and credit hours to dynamically calculate their term SGPA and overall cumulative GPA (CGPA).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemesterGpaCalculatorComponent(
    existingSemesters: List<SemesterGrades> = emptyList(),
    onSaveToRecords: ((semesterNumber: Int, courses: List<CourseGrade>) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Determine default prior credits & CGPA from completed semesters
    val priorCompleted = remember(existingSemesters) {
        existingSemesters.filter { !it.isCurrentSemester && it.totalCredits > 0 }
    }
    val defaultPriorCredits = priorCompleted.sumOf { it.totalCredits }
    val defaultPriorPoints = priorCompleted.sumOf { it.totalEarnedPoints }
    val defaultPriorCgpa = if (defaultPriorCredits > 0) defaultPriorPoints / defaultPriorCredits else 8.78

    // Prior academic records state
    var priorCreditsInput by remember { mutableStateOf(if (defaultPriorCredits > 0) defaultPriorCredits.toString() else "64") }
    var priorCgpaInput by remember { mutableStateOf(String.format(Locale.US, "%.2f", defaultPriorCgpa)) }
    var selectedSemesterNumber by remember { mutableStateOf(6) }

    // Dynamic list of courses for the term
    var courses by remember {
        mutableStateOf(
            listOf(
                CalculatorCourseItem(courseCode = "CS-301", courseName = "Distributed Systems & Cloud", credits = 4, grade = "A+"),
                CalculatorCourseItem(courseCode = "CS-302", courseName = "Database Management Systems", credits = 4, grade = "O"),
                CalculatorCourseItem(courseCode = "CS-303", courseName = "Compiler Design & Automata", credits = 3, grade = "A"),
                CalculatorCourseItem(courseCode = "CS-304", courseName = "Artificial Intelligence & ML", credits = 4, grade = "A+"),
                CalculatorCourseItem(courseCode = "CS-305", courseName = "Capstone Lab & Seminar", credits = 2, grade = "O")
            )
        )
    }

    var showPriorConfig by remember { mutableStateOf(false) }
    var savedSuccessFeedback by remember { mutableStateOf<String?>(null) }

    // Dynamic Computations
    val termTotalCredits = courses.sumOf { it.credits }
    val termTotalPoints = courses.sumOf { it.qualityPoints }
    val termSgpa = if (termTotalCredits > 0) termTotalPoints / termTotalCredits else 0.0

    val parsedPriorCredits = priorCreditsInput.toIntOrNull()?.coerceAtLeast(0) ?: 0
    val parsedPriorCgpa = priorCgpaInput.toDoubleOrNull()?.coerceIn(0.0, 10.0) ?: 0.0
    val priorTotalPoints = parsedPriorCredits * parsedPriorCgpa

    val cumulativeTotalCredits = parsedPriorCredits + termTotalCredits
    val cumulativeTotalPoints = priorTotalPoints + termTotalPoints
    val cumulativeGpa = if (cumulativeTotalCredits > 0) cumulativeTotalPoints / cumulativeTotalCredits else termSgpa

    val cgpaDifference = cumulativeGpa - parsedPriorCgpa
    val formattedSgpa = String.format(Locale.US, "%.2f", termSgpa)
    val formattedCgpa = String.format(Locale.US, "%.2f", cumulativeGpa)
    val formattedDiff = if (cgpaDifference >= 0) "+${String.format(Locale.US, "%.2f", cgpaDifference)}" else String.format(Locale.US, "%.2f", cgpaDifference)

    val aictePercentage = ((cumulativeGpa - 0.75) * 10.0).coerceAtLeast(0.0)
    val formattedPercentage = String.format(Locale.US, "%.1f%%", aictePercentage)

    val academicClassification = when {
        termSgpa >= 9.0 -> "Outstanding (O Grade)"
        termSgpa >= 8.0 -> "First Class with Distinction"
        termSgpa >= 6.5 -> "First Class"
        termSgpa >= 5.0 -> "Second Class"
        termSgpa >= 4.0 -> "Pass"
        else -> "Needs Improvement"
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("semester_gpa_calculator")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Semester GPA Calculator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Input grades & credit hours for dynamic SGPA/CGPA",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(100),
                    color = TertiaryContainer,
                    modifier = Modifier.clickable { showPriorConfig = !showPriorConfig }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Tertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showPriorConfig) "Hide CGPA Base" else "Edit CGPA Base",
                            style = MaterialTheme.typography.labelSmall,
                            color = Tertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Expandable Prior CGPA & Credits Input Section
            AnimatedVisibility(visible = showPriorConfig) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Cumulative Baseline (Prior Semesters)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (defaultPriorCredits > 0) {
                                TextButton(
                                    onClick = {
                                        priorCreditsInput = defaultPriorCredits.toString()
                                        priorCgpaInput = String.format(Locale.US, "%.2f", defaultPriorCgpa)
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Reset to Transcript", fontSize = 11.sp)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = priorCreditsInput,
                                onValueChange = { priorCreditsInput = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Prior Credits", fontSize = 11.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("prior_credits_input")
                            )

                            OutlinedTextField(
                                value = priorCgpaInput,
                                onValueChange = { priorCgpaInput = it },
                                label = { Text("Prior CGPA (10.0)", fontSize = 11.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("prior_cgpa_input")
                            )
                        }

                        Text(
                            text = "Cumulative GPA combines your prior $priorCreditsInput credits ($priorCgpaInput CGPA) with this semester's credits.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Real-Time Dynamic Result Cards
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
                        // Term SGPA Block
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.testTag("calculated_sgpa_display")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Primary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TERM SGPA",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = formattedSgpa,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = " / 10.0",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                                )
                            }
                        }

                        // Divider
                        Box(
                            modifier = Modifier
                                .height(44.dp)
                                .width(1.dp)
                                .background(OutlineVariant.copy(alpha = 0.5f))
                        )

                        // Overall Cumulative CGPA Block
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.testTag("calculated_cgpa_display")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "OVERALL CGPA",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Tertiary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Tertiary)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = formattedCgpa,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = " / 10.0",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary Metrics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = academicClassification,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Term Credits: $termTotalCredits",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("•", color = OutlineVariant)
                            Text(
                                text = "AICTE Eq: $formattedPercentage",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("•", color = OutlineVariant)
                            Text(
                                text = "Delta: $formattedDiff",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (cgpaDifference >= 0) Tertiary else Error
                            )
                        }
                    }
                }
            }

            // Semester Selection & Course List Label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Courses in Term",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(100),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = "${courses.size} subjects • $termTotalCredits credits",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                // Quick presets or Term switch
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Sem $selectedSemesterNumber",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
                    )
                }
            }

            // Course Rows List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                courses.forEachIndexed { index, course ->
                    CourseCalculatorRow(
                        item = course,
                        onUpdate = { updated ->
                            courses = courses.toMutableList().also { it[index] = updated }
                        },
                        onDelete = {
                            if (courses.size > 1) {
                                courses = courses.toMutableList().also { it.removeAt(index) }
                            }
                        },
                        canDelete = courses.size > 1,
                        rowIndex = index
                    )
                }
            }

            // Add Course & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        val newNumber = courses.size + 1
                        val newCode = "CS-30$newNumber"
                        courses = courses + CalculatorCourseItem(
                            courseCode = newCode,
                            courseName = "Elective Subject $newNumber",
                            credits = 3,
                            grade = "A"
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("add_course_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Course", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = {
                        // Quick reset to 5 default courses
                        courses = listOf(
                            CalculatorCourseItem(courseCode = "CS-301", courseName = "Distributed Systems", credits = 4, grade = "A+"),
                            CalculatorCourseItem(courseCode = "CS-302", courseName = "Database Management", credits = 4, grade = "O"),
                            CalculatorCourseItem(courseCode = "CS-303", courseName = "Compiler Design", credits = 3, grade = "A"),
                            CalculatorCourseItem(courseCode = "CS-304", courseName = "Machine Learning", credits = 4, grade = "A+"),
                            CalculatorCourseItem(courseCode = "CS-305", courseName = "Capstone Lab", credits = 2, grade = "O")
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("reset_courses_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", style = MaterialTheme.typography.labelMedium)
                }

                if (onSaveToRecords != null) {
                    Button(
                        onClick = {
                            val converted = courses.map {
                                CourseGrade(
                                    courseCode = it.courseCode,
                                    courseName = it.courseName,
                                    credits = it.credits,
                                    grade = it.grade,
                                    gradePoint = it.gradePoint
                                )
                            }
                            onSaveToRecords(selectedSemesterNumber, converted)
                            savedSuccessFeedback = "Saved ${courses.size} courses to Semester $selectedSemesterNumber!"
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("save_grades_button")
                    ) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Term", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Success feedback message
            AnimatedVisibility(visible = savedSuccessFeedback != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TertiaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Tertiary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = savedSuccessFeedback ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Tertiary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Formula Explanation footer
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Formula: SGPA = Σ(Credits × Grade Points) / ΣCredits",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Grade scale: O=10, A+=9, A=8, B+=7, B=6, C=5, P=4, F=0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

/**
 * Single Course Item row within the Semester GPA Calculator.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseCalculatorRow(
    item: CalculatorCourseItem,
    onUpdate: (CalculatorCourseItem) -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean,
    rowIndex: Int
) {
    var gradeMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("course_row_$rowIndex")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Code, Title & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PrimaryContainer
                    ) {
                        Text(
                            text = item.courseCode,
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.courseName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_course_btn_$rowIndex")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove course",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Row 2: Credit Hours Stepper & Grade Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Credit Hours Selector Stepper
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("credit_stepper_$rowIndex")
                ) {
                    Text(
                        text = "Credits:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    FilledTonalIconButton(
                        onClick = {
                            if (item.credits > 1) {
                                onUpdate(item.copy(credits = item.credits - 1))
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("credit_minus_$rowIndex")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease credit", modifier = Modifier.size(16.dp))
                    }

                    Text(
                        text = "${item.credits}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    FilledTonalIconButton(
                        onClick = {
                            if (item.credits < 6) {
                                onUpdate(item.copy(credits = item.credits + 1))
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("credit_plus_$rowIndex")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase credit", modifier = Modifier.size(16.dp))
                    }
                }

                // Grade Selector Dropdown & Row Quality Points
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quality Points for this subject
                    Text(
                        text = "${String.format(Locale.US, "%.1f", item.qualityPoints)} pts",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )

                    // Grade Picker Box
                    Box {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (item.grade) {
                                "O" -> PrimaryContainer
                                "A+" -> TertiaryContainer
                                "A" -> MaterialTheme.colorScheme.surfaceContainerHigh
                                "F" -> ErrorContainer
                                else -> MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            border = BorderStroke(1.dp, Primary.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .clickable { gradeMenuExpanded = true }
                                .testTag("grade_picker_$rowIndex")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${item.grade} (${item.gradePoint.toInt()} pts)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = when (item.grade) {
                                        "O" -> Primary
                                        "A+" -> Tertiary
                                        "F" -> Error
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = gradeMenuExpanded,
                            onDismissRequest = { gradeMenuExpanded = false }
                        ) {
                            GradeScale.availableGrades.forEach { g ->
                                val point = GradeScale.getPoint(g)
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = g,
                                                fontWeight = FontWeight.Bold,
                                                color = if (g == item.grade) Primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Text(
                                                text = "${point.toInt()} pts",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        onUpdate(item.copy(grade = g))
                                        gradeMenuExpanded = false
                                    },
                                    modifier = Modifier.testTag("grade_option_${g}_$rowIndex")
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
