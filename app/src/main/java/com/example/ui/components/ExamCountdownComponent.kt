package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AcademicCalendarEventEntity
import com.example.ui.theme.*
import com.example.viewmodel.CampusLifeViewModel
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

data class TimeRemaining(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val isExpired: Boolean
)

@Composable
fun ExamCountdownComponent(
    viewModel: CampusLifeViewModel,
    modifier: Modifier = Modifier,
    onViewAllExams: (() -> Unit)? = null
) {
    val nextExam by viewModel.nextScheduledExam.collectAsState()

    // Real-time ticking state updated every second
    val timeRemaining by produceState(
        initialValue = TimeRemaining(0, 0, 0, 0, false),
        key1 = nextExam
    ) {
        val exam = nextExam
        if (exam == null) {
            value = TimeRemaining(0, 0, 0, 0, true)
            return@produceState
        }

        while (true) {
            val now = System.currentTimeMillis()
            val targetMs = if (exam.targetTimestampMs > 0) {
                exam.targetTimestampMs
            } else {
                now + (exam.daysRemaining.toLong() * 24 * 3600 * 1000)
            }
            val diff = targetMs - now

            if (diff <= 0) {
                value = TimeRemaining(0, 0, 0, 0, true)
            } else {
                val d = TimeUnit.MILLISECONDS.toDays(diff)
                val h = TimeUnit.MILLISECONDS.toHours(diff) % 24
                val m = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
                val s = TimeUnit.MILLISECONDS.toSeconds(diff) % 60
                value = TimeRemaining(d, h, m, s, false)
            }
            delay(1000L)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("exam_countdown_component"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1B4B) // Dark Indigo premium container
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        if (nextExam == null) {
            // Empty state
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.School, contentDescription = null, tint = Color.White)
                    }
                }
                Column {
                    Text(
                        "No Scheduled Exams Right Now",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Text(
                        "All current evaluations completed or dates pending announcement.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            val exam = nextExam!!
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE11D48),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = "Next Scheduled Exam",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Urgency badge
                    Surface(
                        shape = RoundedCornerShape(100),
                        color = if (timeRemaining.days <= 3) Color(0xFFE11D48) else Color(0xFF4F46E5)
                    ) {
                        Text(
                            text = if (timeRemaining.isExpired) "Happening Today" else "In ${timeRemaining.days} Days",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }
                }

                // Exam Title & Details
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = exam.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 17.sp
                    )
                    if (exam.description.isNotBlank()) {
                        Text(
                            text = exam.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 2
                        )
                    }
                }

                // Digital Countdown Timer Grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.35f))
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CountdownDigitBlock(
                        value = String.format("%02d", timeRemaining.days),
                        label = "DAYS",
                        testTag = "countdown_days"
                    )
                    Text(":", color = Color.White.copy(alpha = 0.6f), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    CountdownDigitBlock(
                        value = String.format("%02d", timeRemaining.hours),
                        label = "HOURS",
                        testTag = "countdown_hours"
                    )
                    Text(":", color = Color.White.copy(alpha = 0.6f), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    CountdownDigitBlock(
                        value = String.format("%02d", timeRemaining.minutes),
                        label = "MINS",
                        testTag = "countdown_minutes"
                    )
                    Text(":", color = Color.White.copy(alpha = 0.6f), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    CountdownDigitBlock(
                        value = String.format("%02d", timeRemaining.seconds),
                        label = "SECS",
                        testTag = "countdown_seconds"
                    )
                }

                // Location & Dates Footer
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
                            Icons.Default.Place,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = exam.locationOrVenue,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // 1-Tap Reminder toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = if (exam.isReminderSet) Color(0xFF10B981) else Color.White.copy(alpha = 0.15f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(100))
                                .testTag("btn_exam_reminder_toggle")
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    if (exam.isReminderSet) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                                    contentDescription = "Toggle Exam Reminder",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (exam.isReminderSet) "Reminded" else "Notify Me",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        if (onViewAllExams != null) {
                            TextButton(
                                onClick = onViewAllExams,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "All Exams",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA5B4FC)
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
fun CountdownDigitBlock(
    value: String,
    label: String,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.testTag(testTag)
    ) {
        AnimatedContent(
            targetState = value,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "digit_$label"
        ) { textVal ->
            Text(
                text = textVal,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = Color.White
            )
        }
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.65f),
            letterSpacing = 0.5.sp
        )
    }
}
