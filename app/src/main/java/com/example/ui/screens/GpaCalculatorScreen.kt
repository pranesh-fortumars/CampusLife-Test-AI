package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.ui.components.CalculatorCourseItem
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel
import java.util.Locale
import java.util.UUID

/**
 * Dedicated Compose UI screen for calculating term GPA and tracking cumulative performance (CGPA)
 * over the semester by inputting course grades and credit hours.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpaCalculatorScreen(
    viewModel: CampusLifeViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val semesterGrades by viewModel.semesterGrades.collectAsState()
    var selectedSemesterNumber by remember { mutableStateOf(5) }
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showPriorConfigDialog by remember { mutableStateOf(false) }
    var showGradeScaleGuide by remember { mutableStateOf(false) }

    // Prior academic records state
    val priorCompleted = remember(semesterGrades, selectedSemesterNumber) {
        semesterGrades.filter { it.semesterNumber < selectedSemesterNumber && it.totalCredits > 0 }
    }
    val defaultPriorCredits = priorCompleted.sumOf { it.totalCredits }.let { if (it > 0) it else 64 }
    val defaultPriorPoints = priorCompleted.sumOf { it.totalEarnedPoints }.let { if (it > 0) it else (64 * 8.78) }
    val defaultPriorCgpa = if (defaultPriorCredits > 0) defaultPriorPoints / defaultPriorCredits else 8.78

    var priorCreditsInput by remember { mutableStateOf(defaultPriorCredits.toString()) }
    var priorCgpaInput by remember { mutableStateOf(String.format(Locale.US, "%.2f", defaultPriorCgpa)) }

    // Courses for the active semester
    var courses by remember {
        mutableStateOf(
            listOf(
                CalculatorCourseItem(courseCode = "CS-301", courseName = "Distributed Systems & Cloud", credits = 4, grade = "A+"),
                CalculatorCourseItem(courseCode = "CS-302", courseName = "Database Management Systems", credits = 4, grade = "O"),
                CalculatorCourseItem(courseCode = "CS-304", courseName = "Operating Systems", credits = 4, grade = "A+"),
                CalculatorCourseItem(courseCode = "CS-306", courseName = "Computer Networks", credits = 3, grade = "A"),
                CalculatorCourseItem(courseCode = "CS-308", courseName = "Software Engineering & Agile", credits = 3, grade = "O")
            )
        )
    }

    // Target CGPA simulator state
    var targetCgpaInput by remember { mutableStateOf("9.00") }

    // Synchronize when switching semester
    LaunchedEffect(selectedSemesterNumber, semesterGrades) {
        val existingSem = semesterGrades.find { it.semesterNumber == selectedSemesterNumber }
        if (existingSem != null && existingSem.courses.isNotEmpty()) {
            courses = existingSem.courses.map {
                CalculatorCourseItem(
                    id = it.id,
                    courseCode = it.courseCode,
                    courseName = it.courseName,
                    credits = it.credits,
                    grade = it.grade
                )
            }
        }
    }

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

    val aictePercentage = ((cumulativeGpa - 0.75) * 10.0).coerceAtLeast(0.0)
    val usScaleGpa = (cumulativeGpa / 10.0) * 4.0

    val parsedTargetCgpa = targetCgpaInput.toDoubleOrNull()?.coerceIn(0.0, 10.0) ?: 9.00
    val neededTotalPoints = (parsedPriorCredits + termTotalCredits) * parsedTargetCgpa
    val neededTermPoints = neededTotalPoints - priorTotalPoints
    val requiredSgpa = if (termTotalCredits > 0) neededTermPoints / termTotalCredits else 0.0

    val honorsClassification = when {
        termSgpa >= 9.0 -> "Outstanding (O Grade)"
        termSgpa >= 8.0 -> "First Class with Distinction"
        termSgpa >= 6.5 -> "First Class"
        termSgpa >= 5.0 -> "Second Class"
        termSgpa >= 4.0 -> "Pass"
        else -> "Needs Improvement"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "GPA Calculator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Term SGPA & Cumulative Performance",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("gpa_calculator_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showGradeScaleGuide = true },
                        modifier = Modifier.testTag("gpa_grade_scale_btn")
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Grade Scale Reference",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("gpa_calculator_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Semester Selector Carousel
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Academic Semester",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { showPriorConfigDialog = true }) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Calibrate Prior CGPA", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items((1..8).toList()) { sem ->
                        val isSelected = sem == selectedSemesterNumber
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSemesterNumber = sem },
                            label = {
                                Text(
                                    text = if (sem == 5) "Sem $sem (Active)" else "Sem $sem",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(100),
                            modifier = Modifier.testTag("gpa_sem_chip_$sem")
                        )
                    }
                }
            }

            // Hero SGPA & Cumulative Performance Cards
            item {
                TermSgpaHeroCard(
                    termSgpa = termSgpa,
                    termCredits = termTotalCredits,
                    honorsClassification = honorsClassification,
                    cumulativeGpa = cumulativeGpa,
                    totalCumulativeCredits = cumulativeTotalCredits,
                    aictePercentage = aictePercentage,
                    usScaleGpa = usScaleGpa
                )
            }

            // Target CGPA What-If Planner
            item {
                TargetCgpaPlannerCard(
                    targetCgpa = targetCgpaInput,
                    onTargetCgpaChange = { targetCgpaInput = it },
                    requiredSgpa = requiredSgpa,
                    currentCumulativeGpa = cumulativeGpa
                )
            }

            // Section Header: Course Input List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Course Grades & Credit Hours",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${courses.size} Courses • $termTotalCredits Total Credit Hours",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = { showAddCourseDialog = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_course_to_gpa_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Course", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Course Rows
            items(courses, key = { it.id }) { course ->
                GpaCourseInputRowCard(
                    course = course,
                    onGradeChange = { newGrade ->
                        courses = courses.map {
                            if (it.id == course.id) it.copy(grade = newGrade) else it
                        }
                    },
                    onCreditsChange = { newCredits ->
                        courses = courses.map {
                            if (it.id == course.id) it.copy(credits = newCredits.coerceIn(1, 8)) else it
                        }
                    },
                    onDelete = {
                        courses = courses.filter { it.id != course.id }
                    }
                )
            }

            // Persistence & Action Buttons
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val domainCourses = courses.map {
                                CourseGrade(
                                    id = it.id,
                                    courseCode = it.courseCode,
                                    courseName = it.courseName,
                                    credits = it.credits,
                                    grade = it.grade,
                                    gradePoint = it.gradePoint
                                )
                            }
                            viewModel.saveSemesterCourseGrades(selectedSemesterNumber, domainCourses)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_gpa_to_db_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Semester $selectedSemesterNumber to Records",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            courses = listOf(
                                CalculatorCourseItem(courseCode = "CS-301", courseName = "Distributed Systems & Cloud", credits = 4, grade = "A+"),
                                CalculatorCourseItem(courseCode = "CS-302", courseName = "Database Management Systems", credits = 4, grade = "O"),
                                CalculatorCourseItem(courseCode = "CS-304", courseName = "Operating Systems", credits = 4, grade = "A+"),
                                CalculatorCourseItem(courseCode = "CS-306", courseName = "Computer Networks", credits = 3, grade = "A"),
                                CalculatorCourseItem(courseCode = "CS-308", courseName = "Software Engineering & Agile", credits = 3, grade = "O")
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset to Standard Semester Curriculum")
                    }
                }
            }
        }
    }

    // Add Course Dialog
    if (showAddCourseDialog) {
        AddNewCourseGradeDialog(
            onDismiss = { showAddCourseDialog = false },
            onAdd = { code, name, credits, grade ->
                val newItem = CalculatorCourseItem(
                    courseCode = code,
                    courseName = name,
                    credits = credits,
                    grade = grade
                )
                courses = courses + newItem
                showAddCourseDialog = false
            }
        )
    }

    // Prior Academic Transcript Calibration Dialog
    if (showPriorConfigDialog) {
        PriorRecordsCalibrationDialog(
            currentCredits = priorCreditsInput,
            currentCgpa = priorCgpaInput,
            onDismiss = { showPriorConfigDialog = false },
            onSave = { newCredits, newCgpa ->
                priorCreditsInput = newCredits
                priorCgpaInput = newCgpa
                showPriorConfigDialog = false
            }
        )
    }

    // Grade Scale Guide Dialog
    if (showGradeScaleGuide) {
        GradeScaleGuideModal(onDismiss = { showGradeScaleGuide = false })
    }
}

/**
 * Hero Card displaying live Semester SGPA and overall Cumulative CGPA.
 */
@Composable
fun TermSgpaHeroCard(
    termSgpa: Double,
    termCredits: Int,
    honorsClassification: String,
    cumulativeGpa: Double,
    totalCumulativeCredits: Int,
    aictePercentage: Double,
    usScaleGpa: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("term_sgpa_hero_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // SGPA Main Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TERM SGPA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = String.format(Locale.US, "%.2f", termSgpa),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "$termCredits Credits",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = honorsClassification,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(14.dp))

            // Cumulative Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Cumulative CGPA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.2f", cumulativeGpa)} / 10.0",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$totalCumulativeCredits Total Credits",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "AICTE Conversion",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f%%", aictePercentage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Percentage",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "US 4.0 Scale",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = String.format(Locale.US, "%.2f", usScaleGpa),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Equivalent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Target CGPA What-If Planner Card.
 */
@Composable
fun TargetCgpaPlannerCard(
    targetCgpa: String,
    onTargetCgpaChange: (String) -> Unit,
    requiredSgpa: Double,
    currentCumulativeGpa: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("target_cgpa_planner_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.TrackChanges,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Target CGPA Goal Planner",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedTextField(
                    value = targetCgpa,
                    onValueChange = onTargetCgpaChange,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .width(90.dp)
                        .testTag("target_cgpa_input"),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val statusText = when {
                requiredSgpa <= 0.0 -> "Goal already achieved! Any passing grade maintains this target."
                requiredSgpa > 10.0 -> "Target requires ${String.format(Locale.US, "%.2f", requiredSgpa)} SGPA, which exceeds maximum 10.0 scale."
                else -> "You need an SGPA of ${String.format(Locale.US, "%.2f", requiredSgpa)} this semester to achieve your target CGPA of $targetCgpa."
            }

            val statusColor = when {
                requiredSgpa <= 0.0 -> SuccessGreen
                requiredSgpa > 10.0 -> MaterialTheme.colorScheme.error
                requiredSgpa <= 9.0 -> MaterialTheme.colorScheme.primary
                else -> AmberAlert
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = statusColor
            )
        }
    }
}

/**
 * Individual course input card with credit hours stepper and grade chips.
 */
@Composable
fun GpaCourseInputRowCard(
    course: CalculatorCourseItem,
    onGradeChange: (String) -> Unit,
    onCreditsChange: (Int) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gpa_course_card_${course.courseCode}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Code, Name & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.courseCode,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = course.courseName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("delete_course_${course.courseCode}")
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove Course",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Credit hours & Quality points indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Credits Stepper
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Credits:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { onCreditsChange(course.credits - 1) },
                        enabled = course.credits > 1,
                        modifier = Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease Credits", modifier = Modifier.size(14.dp))
                    }

                    Text(
                        text = "${course.credits} cr",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    IconButton(
                        onClick = { onCreditsChange(course.credits + 1) },
                        enabled = course.credits < 8,
                        modifier = Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase Credits", modifier = Modifier.size(14.dp))
                    }
                }

                // Points earned
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "${String.format(Locale.US, "%.1f", course.qualityPoints)} Points",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Grade Selector Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(GradeScale.availableGrades) { gradeKey ->
                    val isSelected = course.grade.equals(gradeKey, ignoreCase = true)
                    val points = GradeScale.getPoint(gradeKey)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onGradeChange(gradeKey) },
                        label = {
                            Text(
                                text = "$gradeKey (${points.toInt()})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("grade_chip_${course.courseCode}_$gradeKey")
                    )
                }
            }
        }
    }
}

/**
 * Dialog to add a new custom course to the term GPA calculator.
 */
@Composable
fun AddNewCourseGradeDialog(
    onDismiss: () -> Unit,
    onAdd: (code: String, name: String, credits: Int, grade: String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var credits by remember { mutableStateOf(4) }
    var grade by remember { mutableStateOf("A+") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Add Course for Term", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Course Code") },
                    placeholder = { Text("e.g. CS-310") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_course_code_input")
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Course Title") },
                    placeholder = { Text("e.g. Mobile Computing") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_course_name_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Credit Hours", style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (credits > 1) credits-- },
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "$credits cr",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        IconButton(
                            onClick = { if (credits < 8) credits++ },
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Text("Expected Grade", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(GradeScale.availableGrades) { g ->
                        val isSelected = grade == g
                        FilterChip(
                            selected = isSelected,
                            onClick = { grade = g },
                            label = { Text(g, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (code.isNotBlank() && name.isNotBlank()) {
                        onAdd(code.trim().uppercase(), name.trim(), credits, grade)
                    }
                },
                enabled = code.isNotBlank() && name.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_course_btn")
            ) {
                Text("Add Course")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog to adjust prior completed credits and prior CGPA.
 */
@Composable
fun PriorRecordsCalibrationDialog(
    currentCredits: String,
    currentCgpa: String,
    onDismiss: () -> Unit,
    onSave: (credits: String, cgpa: String) -> Unit
) {
    var credits by remember { mutableStateOf(currentCredits) }
    var cgpa by remember { mutableStateOf(currentCgpa) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Calibrate Prior Academic Record", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Synchronize with your university transcript numbers up to the previous semester.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = credits,
                    onValueChange = { credits = it },
                    label = { Text("Prior Total Credits Completed") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("prior_credits_input")
                )

                OutlinedTextField(
                    value = cgpa,
                    onValueChange = { cgpa = it },
                    label = { Text("Prior Cumulative CGPA (up to previous sem)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("prior_cgpa_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(credits, cgpa) },
                modifier = Modifier.testTag("save_prior_records_btn")
            ) {
                Text("Update Records")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Reference modal explaining standard grade points and performance brackets.
 */
@Composable
fun GradeScaleGuideModal(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("UGC / AICTE 10-Point Grade Scale", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val scales = listOf(
                    Triple("O", "10.0", "Outstanding (90% - 100%)"),
                    Triple("A+", "9.0", "Excellent (80% - 89%)"),
                    Triple("A", "8.0", "Very Good (70% - 79%)"),
                    Triple("B+", "7.0", "Good (60% - 69%)"),
                    Triple("B", "6.0", "Above Average (50% - 59%)"),
                    Triple("C", "5.0", "Average (40% - 49%)"),
                    Triple("P", "4.0", "Passing Grade (Pass Mark)"),
                    Triple("F", "0.0", "Failed / Arrear (< 40%)")
                )

                scales.forEach { (grade, point, desc) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "$grade = $point",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
