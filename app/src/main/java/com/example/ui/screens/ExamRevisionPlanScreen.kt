package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.RevisionSprint
import com.example.model.RevisionTopic
import com.example.model.SubjectTargetMarks
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel
import com.example.viewmodel.SubScreen

@Composable
fun ExamRevisionPlanScreen(
    viewModel: CampusLifeViewModel,
    onBack: () -> Unit
) {
    var selectedTopTab by remember { mutableStateOf("CGPA") } // "CGPA", "Revision"
    val simulatedSgpa by viewModel.simulatedSem6Sgpa.collectAsState()

    // Calculated Overall CGPA from:
    // 112 credits at 8.64 CGPA + 22 active credits at simulatedSgpa
    // (112 * 8.64 + 22 * simulatedSgpa) / 134
    val resultingCgpa = (112 * 8.64f + 22 * simulatedSgpa) / 134f
    val resultingCgpaStr = String.format("%.2f", resultingCgpa)

    val subjectMarks = listOf(
        SubjectTargetMarks(
            code = "CS-302",
            name = "DBMS",
            credits = 4,
            targetGrade = "O",
            internalScored = 18,
            midSemMin = 26,
            endSemMin = 44,
            statusNote = "✓ +4 marks safe margin buffer",
            isPriority = false,
            isSafe = true
        ),
        SubjectTargetMarks(
            code = "CS-304",
            name = "Operating Systems",
            credits = 4,
            targetGrade = "A+",
            internalScored = 15,
            midSemMin = 24,
            endSemMin = 41,
            statusNote = "High priority — First exam in 11 days. Needs intensive revision.",
            isPriority = true,
            isSafe = false
        ),
        SubjectTargetMarks(
            code = "CS-306",
            name = "Computer Networks",
            credits = 3,
            targetGrade = "A+",
            internalScored = 17,
            midSemMin = 25,
            endSemMin = 42,
            statusNote = "85% Steady",
            isPriority = false,
            isSafe = true
        ),
        SubjectTargetMarks(
            code = "CS-308",
            name = "Software Engineering",
            credits = 3,
            targetGrade = "O",
            internalScored = 19,
            midSemMin = 27,
            endSemMin = 45,
            statusNote = "95% Excellent",
            isPriority = false,
            isSafe = true
        )
    )

    var topicStates by remember {
        mutableStateOf(
            mutableMapOf(
                "os-1" to true,
                "os-2" to false,
                "os-3" to false,
                "dbms-1" to true,
                "dbms-2" to true,
                "dbms-3" to false
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("exam_revision_plan_screen")
    ) {
        // App bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Track > Exam Revision Planner", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("CGPA & Revision Hub", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Segmented Tabs Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedTopTab == "CGPA",
                onClick = { selectedTopTab = "CGPA" },
                label = { Text("CGPA & Marks Simulator") },
                leadingIcon = { Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp)) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary, selectedLabelColor = Color.White),
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedTopTab == "Revision",
                onClick = { selectedTopTab = "Revision" },
                label = { Text("Revision Plan") },
                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp)) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary, selectedLabelColor = Color.White),
                modifier = Modifier.weight(1f)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Countdown banner
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Mid-Semester Exam Window", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text("Nov 4 – Nov 12 • 11 Days countdown", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Surface(shape = RoundedCornerShape(100), color = TertiaryFixed) {
                            Text("ON SCHEDULE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Tertiary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                }
            }

            // Target CGPA Simulator Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("TARGET PROJECTION HUB", style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.Bold)
                                Text("Target CGPA Simulator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Surface(shape = RoundedCornerShape(100), color = SecondaryFixed) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Secondary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Top 5% Honors", style = MaterialTheme.typography.labelSmall, color = Secondary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        // 3 Column Metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Current CGPA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("8.64", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Primary)
                                    Text("112 Credits (Sem 1-5)", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Grad Target", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("8.80", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Text("+0.16 Needed", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = Primary, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Sem 6 Target", style = MaterialTheme.typography.labelSmall, color = Secondary, fontWeight = FontWeight.Bold)
                                    Text(String.format("%.2f", simulatedSgpa), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Secondary)
                                    Text("22 Active Credits", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        // Slider Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Tune, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Simulate Sem 6 SGPA:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                    }
                                    Surface(shape = RoundedCornerShape(6.dp), color = Primary) {
                                        Text(String.format("%.2f", simulatedSgpa), style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                    }
                                }

                                Slider(
                                    value = simulatedSgpa,
                                    onValueChange = { viewModel.updateSimulatedSem6Sgpa(it) },
                                    valueRange = 8.00f..10.00f,
                                    steps = 19,
                                    colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("8.00 (Minimum)", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("9.00 (Honors)", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("10.00 (Max O Grade)", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Resulting Overall CGPA:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(resultingCgpaStr, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Primary)
                                    }

                                    Surface(shape = RoundedCornerShape(100), color = TertiaryFixed) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Tertiary, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("SAFE MILESTONE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Tertiary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Subject Target Marks Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Subject Target Marks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Minimum scores required to guarantee target letter grade", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(shape = RoundedCornerShape(100), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                        Text("Weightage ▾", style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }

            // Subject Target Marks Cards
            items(subjectMarks) { target ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (target.isPriority) ErrorContainer else PrimaryFixed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (target.isPriority) Icons.Default.Settings else Icons.Default.Storage,
                                        contentDescription = null,
                                        tint = if (target.isPriority) Error else Primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(target.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("${target.code} • ${target.credits} Credits", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Surface(shape = RoundedCornerShape(100), color = if (target.targetGrade == "O") TertiaryFixed else SecondaryFixed) {
                                Text("Target: '${target.targetGrade}'", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (target.targetGrade == "O") Tertiary else Secondary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Internal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${target.internalScored}/${target.internalMax}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Mid-Sem Min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${target.midSemMin}/${target.midSemMax}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Primary)
                            }
                            Column {
                                Text("End-Sem Min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${target.endSemMin}/${target.endSemMax}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (target.isPriority) ErrorContainer.copy(alpha = 0.5f) else TertiaryFixed.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                target.statusNote,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (target.isPriority) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (target.isPriority) Error else Tertiary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Revision Roadmap Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Revision Roadmap", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Mid-Sem Exam Roadmap • Starts Nov 4 (11 Days Left)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    // Sprint 1: OS Core
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(shape = RoundedCornerShape(100), color = ErrorContainer) {
                                    Text("FIRST PAPER • NOV 4", style = MaterialTheme.typography.labelSmall, color = Error, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                }
                                Text("60% Readiness", style = MaterialTheme.typography.labelSmall, color = Error, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Sprint: Operating Systems Core", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Sprint Days 4–6 • Focus on critical scoring topics", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(modifier = Modifier.height(10.dp))
                            listOf(
                                Triple("os-1", "Process Sync & Race Conditions", null),
                                Triple("os-2", "Deadlock Prevention Algorithms (Banker's)", "Est. 1.5h"),
                                Triple("os-3", "Paging & Segmentation Address Calc", "Est. 2h")
                            ).forEach { (id, title, est) ->
                                val isDone = topicStates[id] ?: false
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            topicStates = topicStates.toMutableMap().apply { this[id] = !isDone }
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isDone,
                                        onCheckedChange = {
                                            topicStates = topicStates.toMutableMap().apply { this[id] = it }
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = Primary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                    if (est != null) {
                                        Text(est, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    } else {
                                        Text("Done", style = MaterialTheme.typography.labelSmall, color = Tertiary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Sprint 2: DBMS
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(shape = RoundedCornerShape(100), color = PrimaryFixed) {
                                    Text("EXAM 2 • NOV 6", style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                }
                                Text("75% Readiness", style = MaterialTheme.typography.labelSmall, color = Tertiary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Sprint: DBMS & Relational Models", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Sprint Days 1–3 • 75% prepared", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(modifier = Modifier.height(10.dp))
                            listOf(
                                Triple("dbms-1", "B+ Trees & Indexing Mechanisms", null),
                                Triple("dbms-2", "Relational Algebra & Advanced SQL", null),
                                Triple("dbms-3", "3NF & BCNF Decomposition Proofs", "Est. 2h Left")
                            ).forEach { (id, title, est) ->
                                val isDone = topicStates[id] ?: false
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            topicStates = topicStates.toMutableMap().apply { this[id] = !isDone }
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isDone,
                                        onCheckedChange = {
                                            topicStates = topicStates.toMutableMap().apply { this[id] = it }
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = Primary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                    if (est != null) {
                                        Text(est, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    } else {
                                        Text("Done", style = MaterialTheme.typography.labelSmall, color = Tertiary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Study Session Streak & Timer Trigger
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            AsyncImage(
                                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDU36-4WUi_wGzd4FFWXxzN2wyhWiFOLFo-mwUTeVpPlMkvBHvw8Ngg6s5uLhkckkLI7LsGZMsPrOJa21fBhKG0ZO_lf-Aw1wkZ1FskaF0IIhHPbpUrLNy9yHBeuREQxkeUmPnsVNJa-qni9lG1vImvyNd7ZDkqly3ntr6LDsuNXjVqqZOJlm6xMQ1Pj-FMBfftbmDiOSPJw4L4kTgnQY7PoPJG-euKXndk27IFqclsne52yX5cQuuPNA",
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Study Session Streak: 4 Days 🔥", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Daily target: 3 hours revision completed today", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(
                                onClick = { viewModel.openSubScreen(SubScreen.FOCUS_TIMER) },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Start Timer")
                            }
                        }
                    }
                }
            }

            // High Yield Tools
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("High-Yield Tools & Export", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Quiz, contentDescription = null, tint = Secondary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Generate Mock Practice Test", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text("AI generated 15-question speed quiz", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Button(onClick = { viewModel.openSubScreen(SubScreen.CAMPUS_COPILOT) }, shape = RoundedCornerShape(8.dp)) {
                                    Text("Start Quiz", fontSize = 12.sp)
                                }
                            }
                        }

                        Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Past 5 Years Solved Mid-Sem PDF", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text("Verified DTU marking scheme breakdown", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                FilledTonalButton(onClick = {}, shape = RoundedCornerShape(8.dp)) {
                                    Text("PDF ⬇", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
