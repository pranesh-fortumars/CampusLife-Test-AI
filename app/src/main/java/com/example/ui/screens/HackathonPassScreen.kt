package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel

@Composable
fun HackathonPassScreen(
    viewModel: CampusLifeViewModel,
    onBack: () -> Unit
) {
    var tokenString by remember { mutableStateOf("HC24-X91A") }
    var countdownSec by remember { mutableStateOf(14 * 3600 + 28 * 60 + 10) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("hackathon_pass_screen")
    ) {
        // Top Banner / Event Cover Media
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            AsyncImage(
                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuAagt5qactFAjBVptOCBnin4UYDAG6Ohfxo6haDxtC0XAI-4VLov0XoSr3nXTxPIxyEdu6pn4dtssHdcycH3TmQv__quYzpKAEcHOTeT4HGrHsrZeKrCExSa_QMk4Kiqv4lloJQUAUbvQRkBKHVwouPWyt1JoyGFi97ayU2V4tt4EH3FLfjH3ILf9o9kdDK5G3EwD2jUG5PvG0IFdqu-3cYa0og7drLSoOPRbwkfAF9TqdnC2KN_5fpuQ",
                contentDescription = "Event banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )

            // Header actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Surface(
                    shape = RoundedCornerShape(100),
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Tertiary))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirmed Attendee", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Primary)
                    }
                }
            }

            // Bottom title
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(100), color = SecondaryContainer) {
                        Text("ACM DTU CHAPTER", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("⏱ 36h Sprint", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.9f))
                }
                Text("HackCampus 2024", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Annual National Collegiate Hackathon & Innovation Expo", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Digital Pass (The Delight Ticket Card)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Ticket Header
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PrimaryContainer)
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("OFFICIAL ENTRY PASS", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                                        Text("Hacker Pass (Tier 1)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                    Surface(shape = RoundedCornerShape(100), color = Color.White.copy(alpha = 0.2f)) {
                                        Text("All Access", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Attendee", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                                        Text("Rahul Sharma", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Roll Number", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                                        Text("21BCS084", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }

                        // Perforation & QR section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(shape = RoundedCornerShape(100), color = MaterialTheme.colorScheme.surfaceContainer) {
                                Text("Check-in opens in: 14h 28m 10s", style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Stylized QR Box
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.QrCode2,
                                        contentDescription = "QR Code",
                                        modifier = Modifier.size(110.dp),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Live Rotating Token: $tokenString", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Tap to boost screen brightness for kiosk", style = MaterialTheme.typography.bodySmall, color = Primary, fontWeight = FontWeight.Medium)

                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                TextButton(onClick = {}) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save PDF")
                                }
                                TextButton(onClick = {}) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share Pass")
                                }
                            }
                        }
                    }
                }
            }

            // Quick Info Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Date & Time", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Primary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Nov 16-17, 2024", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("09:00 AM IST (36h)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = Secondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Main Venue", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Secondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Main Auditorium", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("& CS Labs (Block 3)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Hacker Essentials
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Campus Hacker Essentials", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Wifi, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Wi-Fi Credentials", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Text("SSID: DTU_GUEST_HACK • Pass: hackcampus2024", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Restaurant, contentDescription = null, tint = Tertiary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Meal Pass Tokens (5 Available)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Text("3 Full Meals + 2 Midnight Refreshments", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
