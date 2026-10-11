package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun ExecutiveDeskScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("executive_desk_screen")
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
                Text("DTU Central Council / Overview", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Executive Council Desk", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Surface(shape = RoundedCornerShape(100), color = TertiaryFixed) {
                Text("ODD SEM 24-25", style = MaterialTheme.typography.labelSmall, color = Tertiary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Metrics Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Verified Students", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("14,820", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Primary)
                            Text("↑ 12% vs last sem", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = Tertiary)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Active Clubs", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("48", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Secondary)
                            Text("12 events this wk", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Pending Verif.", style = MaterialTheme.typography.labelSmall, color = Error)
                            Text("142", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Error)
                            Text("Avg: 4.2h TAT", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Student Verification Queue Table Preview
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Recent Verification Queue", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("View All (142) →", style = MaterialTheme.typography.labelSmall, color = Primary)
                        }

                        listOf(
                            Triple("Rahul Sharma (21BCS084)", "B.Tech CSE • 3rd Yr", "Approved"),
                            Triple("Ananya Sen (22ECE112)", "B.Tech ECE • 2nd Yr", "Review Pending"),
                            Triple("Vikram Malhotra (23MEC045)", "B.Tech ME • 1st Yr", "Review Pending")
                        ).forEach { (student, dept, status) ->
                            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(student, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        Text(dept, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(100),
                                        color = if (status == "Approved") TertiaryFixed else ErrorContainer
                                    ) {
                                        Text(
                                            status,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (status == "Approved") Tertiary else Error,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Urgent Incident Alert
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ErrorContainer.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Error, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Urgent Moderation: Resell Price Gouging", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Error)
                        }
                        Text(
                            "Marketplace listing: Duplicate textbook resell claim marked 400% above standard DTU Co-op rate.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Error), shape = RoundedCornerShape(8.dp)) {
                                Text("Delist Item")
                            }
                            OutlinedButton(onClick = {}, shape = RoundedCornerShape(8.dp)) {
                                Text("Inspect")
                            }
                        }
                    }
                }
            }
        }
    }
}
