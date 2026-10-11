package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Subject
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel
import com.example.viewmodel.SubScreen

@Composable
fun SubjectDetailScreen(
    subject: Subject,
    viewModel: CampusLifeViewModel,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("Overview") }
    val tabs = listOf("Overview", "Study Materials (14)", "Assignments (3)", "Exam Prep & PYQs")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("subject_detail_screen")
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
            Column {
                Text("Track > Subject", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(subject.code, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = RoundedCornerShape(100), color = PrimaryContainer) {
                                Text(subject.code, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                            IconButton(onClick = {}) {
                                Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark")
                            }
                        }

                        Text(subject.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("${subject.faculty} • ${subject.department}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(shape = RoundedCornerShape(100), color = TertiaryFixed.copy(alpha = 0.5f)) {
                                Text("Semester V Core", style = MaterialTheme.typography.labelSmall, color = Tertiary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                            Surface(shape = RoundedCornerShape(100), color = SecondaryFixed.copy(alpha = 0.5f)) {
                                Text("Lab Included", style = MaterialTheme.typography.labelSmall, color = Secondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                    }
                }
            }

            // Attendance & Next Class Card
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Attendance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format("%.1f%%", subject.attendancePercentage), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("${subject.attendedClasses}/${subject.totalClasses} attended", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("✈ 3 safe bunks left", style = MaterialTheme.typography.labelSmall, color = Tertiary, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Next Lecture", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Today, 10:30 AM", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(subject.room, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            FilledTonalButton(onClick = {}, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Text("Find Room", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Copilot Banner
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PrimaryContainer.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(PrimaryContainer), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ask DBMS Copilot", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text("Instant SQL & BCNF query tutor", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = { viewModel.openSubScreen(SubScreen.CAMPUS_COPILOT) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Launch")
                        }
                    }
                }
            }

            // Tabs
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tabs) { t ->
                        val isSelected = selectedTab == t
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = if (isSelected) PrimaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
                            modifier = Modifier.clickable { selectedTab = t }
                        ) {
                            Text(
                                t,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Syllabus Breakdown Units
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Syllabus Breakdown (Mid-Sem 68% accomplished)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    val units = listOf(
                        Triple("Unit 1: ER Modeling & Relational Algebra", "Entities, Keys, ER-to-Relational mapping, Tuple Calculus", 100),
                        Triple("Unit 2: SQL, Complex Joins & Triggers", "Subqueries, View Materialization, Stored Procedures, PL/SQL", 100),
                        Triple("Unit 3: Normalization & Dependencies", "1NF, 2NF, 3NF, BCNF, Lossless Join & Dependency Preservation", 75),
                        Triple("Unit 4: Transaction Processing & ACID", "Concurrency Control, 2PL, Serializability, Deadlocks", 0)
                    )

                    units.forEach { (unitTitle, unitDesc, pct) ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(unitTitle, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Text("$pct%", style = MaterialTheme.typography.labelSmall, color = if (pct == 100) Tertiary else Primary, fontWeight = FontWeight.Bold)
                                }
                                Text(unitDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { pct / 100f },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = if (pct == 100) Tertiary else Primary
                                )
                            }
                        }
                    }
                }
            }

            // Faculty & Peer Repository
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Faculty & Peer Repository", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    listOf(
                        "Prof. Roy Official Lecture Slides (Units 1-3).pdf" to "12.4 MB • Verified Faculty",
                        "Normalization & Functional Dependency Solved Examples" to "4.2 MB • By Priya Nair (CR)",
                        "Previous 5 Years Mid-Sem & End-Sem Papers with Answer Key" to "18.1 MB • Solved Solutions"
                    ).forEach { (docTitle, docMeta) ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(docTitle, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                    Text(docMeta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = {}) {
                                    Icon(Icons.Default.Download, contentDescription = "Download", tint = Primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
