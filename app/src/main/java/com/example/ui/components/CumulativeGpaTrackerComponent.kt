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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

/**
 * Material 3 UI component that calculates and displays the current cumulative GPA (CGPA),
 * semester SGPAs, course grade breakdowns, target CGPA forecasting, and interactive grade adjustments.
 */
@Composable
fun CumulativeGpaTrackerComponent(
    semesters: List<SemesterGrades>,
    onUpdateGrade: (semesterNumber: Int, courseCode: String, newGrade: String) -> Unit,
    onAddCourse: (semesterNumber: Int, course: CourseGrade) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Cumulative GPA (CGPA) Tracker",
    subtitle: String = "Calculated from completed semester credits & grade points"
) {
    val summary = remember(semesters) { AcademicGpaSummary(semesters) }
    var selectedSemesterTab by remember { mutableStateOf<Int?>(null) } // null = All Semesters Overview
    var editingCourse by remember { mutableStateOf<Triple<Int, String, String>?>(null) } // semNumber, code, currentGrade
    var showAddCourseDialog by remember { mutableStateOf(false) }

    val animatedCgpaProgress by animateFloatAsState(
        targetValue = (summary.cumulativeGpa / 10.0).toFloat().coerceIn(0f, 1f),
        label = "cgpa_progress"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("cumulative_gpa_tracker")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = { showAddCourseDialog = true },
                    shape = RoundedCornerShape(100),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier.testTag("add_grade_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Grade", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            // Hero Cumulative GPA Card (Displays the Calculated Current Cumulative GPA)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, Primary.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CURRENT CUMULATIVE GPA",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = summary.formattedCgpa,
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.testTag("cumulative_gpa_value")
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "/ 10.00",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }

                        // Standing Badge
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = TertiaryContainer,
                            border = BorderStroke(1.dp, Tertiary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Tertiary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (summary.cumulativeGpa >= 8.5) "First Class Distinction" else "First Class",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OnTertiaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Bar indicating CGPA on 10.0 scale
                    Box(modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            progress = { animatedCgpaProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .testTag("cgpa_progress_bar"),
                            color = Primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary Metrics (Total credits, US Scale, Percentage)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Credits Earned",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${summary.totalCreditsEarned} Credits (${summary.completedSemesters.size} Semesters)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "US 4.0 Scale",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${summary.formattedUsGpa} / 4.0",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "AICTE Marks %",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = summary.formattedPercentage,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Semester Selector Tabs
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Semester Breakdown & SGPA",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = selectedSemesterTab == null,
                            onClick = { selectedSemesterTab = null },
                            label = { Text("Overview (${semesters.size} Sems)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(100),
                            modifier = Modifier.testTag("filter_sem_overview")
                        )
                    }

                    items(semesters) { sem ->
                        val isSelected = selectedSemesterTab == sem.semesterNumber
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSemesterTab = sem.semesterNumber },
                            label = { Text("Sem ${sem.semesterNumber}: ${sem.formattedSgpa}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(100),
                            modifier = Modifier.testTag("filter_sem_${sem.semesterNumber}")
                        )
                    }
                }
            }

            // Content: Either Overview Cards or Detailed Course List for Selected Semester
            if (selectedSemesterTab == null) {
                // Overview Grid of All Semesters
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    semesters.forEach { sem ->
                        SemesterSummaryItemCard(
                            semester = sem,
                            onClick = { selectedSemesterTab = sem.semesterNumber }
                        )
                    }
                }
            } else {
                // Detailed Semester Courses View with Interactive Grade Editor
                val activeSem = semesters.find { it.semesterNumber == selectedSemesterTab }
                if (activeSem != null) {
                    SemesterDetailCoursesView(
                        semester = activeSem,
                        onCourseClick = { courseCode, currentGrade ->
                            editingCourse = Triple(activeSem.semesterNumber, courseCode, currentGrade)
                        }
                    )
                }
            }
        }
    }

    // Modal to Edit Course Grade
    if (editingCourse != null) {
        val (semNum, courseCode, currentGrade) = editingCourse!!
        EditGradeDialog(
            courseCode = courseCode,
            currentGrade = currentGrade,
            onDismiss = { editingCourse = null },
            onSelectGrade = { newGrade ->
                onUpdateGrade(semNum, courseCode, newGrade)
                editingCourse = null
            }
        )
    }

    // Modal to Add Course to Semester
    if (showAddCourseDialog) {
        val defaultSem = selectedSemesterTab ?: 6
        AddCourseGradeDialog(
            defaultSemester = defaultSem,
            semesters = semesters,
            onDismiss = { showAddCourseDialog = false },
            onConfirm = { semNum, newCourse ->
                onAddCourse(semNum, newCourse)
                showAddCourseDialog = false
            }
        )
    }
}

/**
 * Summary card for an individual semester showing its SGPA and total credits.
 */
@Composable
fun SemesterSummaryItemCard(
    semester: SemesterGrades,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("semester_card_${semester.semesterNumber}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = semester.semesterName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (semester.isCurrentSemester) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = PrimaryContainer
                        ) {
                            Text(
                                text = "ACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${semester.academicYear} • ${semester.totalCredits} Credits • ${semester.courses.size} Subjects",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "SGPA",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = semester.formattedSgpa,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Detailed view of all course grades for a selected semester, allowing quick grade adjustments.
 */
@Composable
fun SemesterDetailCoursesView(
    semester: SemesterGrades,
    onCourseClick: (courseCode: String, currentGrade: String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${semester.semesterName} Courses (${semester.courses.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "SGPA: ${semester.formattedSgpa} (${semester.totalCredits} Credits)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        }

        semester.courses.forEach { course ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCourseClick(course.courseCode, course.grade) }
                    .testTag("course_grade_${course.courseCode}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PrimaryContainer
                        ) {
                            Text(
                                text = course.courseCode,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = course.courseName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${course.credits} Credits • ${(course.creditPoints)} Points",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Grade Badge (Tap to Edit)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (course.grade) {
                            "O" -> TertiaryContainer
                            "A+" -> PrimaryContainer
                            "A" -> SecondaryContainer
                            else -> MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${course.grade} (${course.gradePoint.toInt()})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Grade",
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog to adjust or edit a course's grade letter and point value.
 */
@Composable
fun EditGradeDialog(
    courseCode: String,
    currentGrade: String,
    onDismiss: () -> Unit,
    onSelectGrade: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Grade for $courseCode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select final letter grade on 10-point scale:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GradeScale.availableGrades.take(4).forEach { grade ->
                        val isSelected = grade.equals(currentGrade, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectGrade(grade) },
                            label = { Text("$grade (${GradeScale.getPoint(grade).toInt()})", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GradeScale.availableGrades.drop(4).forEach { grade ->
                        val isSelected = grade.equals(currentGrade, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectGrade(grade) },
                            label = { Text("$grade (${GradeScale.getPoint(grade).toInt()})", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog to add a new course grade entry into a semester.
 */
@Composable
fun AddCourseGradeDialog(
    defaultSemester: Int,
    semesters: List<SemesterGrades>,
    onDismiss: () -> Unit,
    onConfirm: (semesterNumber: Int, CourseGrade) -> Unit
) {
    var selectedSem by remember { mutableStateOf(defaultSemester) }
    var courseCode by remember { mutableStateOf("") }
    var courseName by remember { mutableStateOf("") }
    var creditsText by remember { mutableStateOf("4") }
    var selectedGrade by remember { mutableStateOf("A+") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Course Grade Entry",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Semester selector
                Column {
                    Text("Semester", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(semesters) { sem ->
                            val isSelected = sem.semesterNumber == selectedSem
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedSem = sem.semesterNumber },
                                label = { Text("Sem ${sem.semesterNumber}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = courseCode,
                    onValueChange = { courseCode = it },
                    label = { Text("Course Code") },
                    placeholder = { Text("e.g., CS-314") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("course_code_input")
                )

                OutlinedTextField(
                    value = courseName,
                    onValueChange = { courseName = it },
                    label = { Text("Course Title") },
                    placeholder = { Text("e.g., Cyber Security & Cryptography") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = creditsText,
                        onValueChange = { creditsText = it },
                        label = { Text("Credits") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    // Grade selector
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Grade", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(listOf("O", "A+", "A", "B+")) { g ->
                                val isSelected = g == selectedGrade
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedGrade = g },
                                    label = { Text(g, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cr = creditsText.toIntOrNull() ?: 4
                    if (courseCode.isNotBlank() && courseName.isNotBlank()) {
                        onConfirm(
                            selectedSem,
                            CourseGrade(
                                courseCode = courseCode.trim().uppercase(),
                                courseName = courseName.trim(),
                                credits = cr,
                                grade = selectedGrade,
                                gradePoint = GradeScale.getPoint(selectedGrade)
                            )
                        )
                    }
                },
                enabled = courseCode.isNotBlank() && courseName.isNotBlank(),
                modifier = Modifier.testTag("save_course_grade_btn")
            ) {
                Text("Save Grade")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
