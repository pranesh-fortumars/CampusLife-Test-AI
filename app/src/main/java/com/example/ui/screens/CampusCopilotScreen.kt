package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel

@Composable
fun CampusCopilotScreen(
    viewModel: CampusLifeViewModel,
    onBack: () -> Unit
) {
    val messages by viewModel.copilotMessages.collectAsState()
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("campus_copilot_screen")
    ) {
        // Top Header
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Campus Copilot", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Tertiary))
                }
                Text("Rahul's Personal Academic Model • 81.4% Attendance Context", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(shape = RoundedCornerShape(100), color = SecondaryFixed) {
                Text("ONLINE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Secondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }

        // Suggested query chips
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Plan study routine for DBMS", "Can I safely miss CN?").forEach { chip ->
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            shadowElevation = 1.dp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                "⚡ $chip",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            items(messages) { msg ->
                if (msg.isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                            color = Primary,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(
                                msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Column(modifier = Modifier.widthIn(max = 320.dp)) {
                            Surface(
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                shadowElevation = 1.dp
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(msg.text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

                                    if (msg.hasLayoutCard) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        // Afternoon Layout Schedule
                                        Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text("🕒 1:15 PM • L-214: Computer Networks Lecture", style = MaterialTheme.typography.bodySmall)
                                                Text("💻 2:45 PM – 4:45 PM • Lab 2: Software Engineering Agile Lab", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        // Attendance Check
                                        Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Attendance Check: Software Eng.", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                    Text("Safe Buffer", style = MaterialTheme.typography.labelSmall, color = Tertiary, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("90.0% (27/30 held) → Post-skip: 87.1% (27/31)", style = MaterialTheme.typography.bodySmall)
                                                Text("Safe buffer: +12.1% above 75.0% threshold", style = MaterialTheme.typography.labelSmall, color = Tertiary)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        // Warning Notice
                                        Surface(shape = RoundedCornerShape(10.dp), color = ErrorContainer.copy(alpha = 0.5f)) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text("High Evaluation Impact", style = MaterialTheme.typography.labelSmall, color = Error, fontWeight = FontWeight.Bold)
                                                Text(
                                                    "Prof. Neha Gupta is evaluating the Sprint 2 Retro submission today in person (10% internal marks).\n\nRecommendation: Attend today's lab to secure internal grading marks!",
                                                    style = MaterialTheme.typography.bodySmall
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

        // Input bottom bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask Copilot about courses, attendance, exams...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(100),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Primary
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendCopilotQuery(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Primary)
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    }
}
