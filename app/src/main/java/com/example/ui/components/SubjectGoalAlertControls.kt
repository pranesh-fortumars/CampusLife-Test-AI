package com.example.ui.components

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SubjectGoalAlertReport
import com.example.ui.theme.*
import com.example.worker.SubjectGoalNotificationHelper
import java.util.Locale

@Composable
fun SubjectGoalAlertToggleCard(
    alertsEnabled: Boolean,
    alertReport: SubjectGoalAlertReport,
    onSetAlertsEnabled: (Boolean, Boolean) -> Unit,
    onSendCheckNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(SubjectGoalNotificationHelper.hasNotificationPermission(context))
    }
    var pendingEnableAfterGrant by remember { mutableStateOf(false) }
    var permissionDeniedNote by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            permissionDeniedNote = false
            if (pendingEnableAfterGrant) {
                onSetAlertsEnabled(true, true)
                val msg = if (alertReport.hasLaggingSubjects) {
                    "Goal alerts enabled • Alert sent for ${alertReport.laggingSubjects.size} subject(s) behind"
                } else {
                    "Goal alerts enabled • All weekly subject goals are on track"
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        } else {
            permissionDeniedNote = true
            onSetAlertsEnabled(false, false)
            Toast.makeText(
                context,
                "Notification permission denied. Enable notifications in Settings for goal alerts.",
                Toast.LENGTH_LONG
            ).show()
        }
        pendingEnableAfterGrant = false
    }

    val isLagging = alertReport.hasLaggingSubjects
    val containerColor = when {
        alertsEnabled && isLagging -> WarningContainer.copy(alpha = 0.42f)
        alertsEnabled -> EmeraldTertiaryContainer.copy(alpha = 0.45f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
    }
    val borderColor = when {
        alertsEnabled && isLagging -> WarningAmber.copy(alpha = 0.45f)
        alertsEnabled -> EmeraldTertiary.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    }
    val accentColor = when {
        alertsEnabled && isLagging -> OnWarningContainer
        alertsEnabled -> EmeraldTertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .testTag("subject_goal_notifications_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (alertsEnabled) accentColor.copy(alpha = 0.16f)
                                else MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (alertsEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = if (alertsEnabled) "Goal alerts active" else "Goal alerts off",
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Weekly Goal Alerts",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (alertsEnabled) {
                                Surface(
                                    shape = RoundedCornerShape(100),
                                    color = if (isLagging) WarningAmber else EmeraldTertiary
                                ) {
                                    Text(
                                        text = if (isLagging) "${alertReport.laggingSubjects.size} Behind" else "On Track",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        val subtitleText = when {
                            permissionDeniedNote -> "Notification permission required in Android Settings"
                            alertsEnabled && isLagging -> {
                                val names = alertReport.laggingSubjects.take(2).joinToString(", ") {
                                    "${it.courseCode} (${it.attainmentPct}%)"
                                }
                                "Alerting when <${alertReport.laggingThresholdPct}% of goal • Behind: $names"
                            }
                            alertsEnabled -> "Active • All subjects keeping pace (>=${alertReport.laggingThresholdPct}% goal)"
                            else -> "Notify me when falling behind custom weekly subject goals"
                        }

                        Text(
                            text = subtitleText,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Switch(
                    checked = alertsEnabled,
                    onCheckedChange = { wantEnabled ->
                        if (wantEnabled) {
                            val currentlyGranted = SubjectGoalNotificationHelper.hasNotificationPermission(context)
                            hasPermission = currentlyGranted
                            if (currentlyGranted) {
                                permissionDeniedNote = false
                                onSetAlertsEnabled(true, true)
                                val msg = if (alertReport.hasLaggingSubjects) {
                                    "Goal alerts ON • Notification sent for ${alertReport.laggingSubjects.size} lagging subject(s)"
                                } else {
                                    "Goal alerts ON • All weekly subject goals on track"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                pendingEnableAfterGrant = true
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            pendingEnableAfterGrant = false
                            onSetAlertsEnabled(false, false)
                            Toast.makeText(context, "Subject weekly goal alerts paused", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = if (isLagging) WarningAmber else EmeraldTertiary
                    ),
                    modifier = Modifier
                        .testTag("switch_subject_goal_notifications")
                        .semantics {
                            contentDescription = "Toggle weekly goal push notifications for Subject Analytics"
                        }
                )
            }

            AnimatedVisibility(visible = alertsEnabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val topLagging = alertReport.laggingSubjects.firstOrNull()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (topLagging != null) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = OnWarningContainer,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "Top gap: %s (%.1fh / %.1fh)",
                                    topLagging.courseCode,
                                    topLagging.loggedHours,
                                    topLagging.weeklyGoalHours
                                ),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OnWarningContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Text(
                                text = "Background pace checks run every 4h",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = {
                            if (SubjectGoalNotificationHelper.hasNotificationPermission(context)) {
                                onSendCheckNow()
                                val msg = if (alertReport.hasLaggingSubjects) {
                                    "Push alert sent for ${alertReport.laggingSubjects.size} lagging subject(s)"
                                } else {
                                    "Checked • All weekly goals currently on track"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                pendingEnableAfterGrant = true
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(100),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 36.dp)
                            .testTag("btn_send_subject_goal_alert")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Check goal pace and send alert now",
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (hasPermission) "Check Now" else "Allow Alerts",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
