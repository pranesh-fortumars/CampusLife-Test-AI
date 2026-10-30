package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.CourseScheduleManagerComponent
import com.example.viewmodel.CampusLifeViewModel

/**
 * Dedicated screen for managing the university course schedule.
 * Allows students to input daily class schedules with room locations and times,
 * with customizable reminder alerts and Room database persistence.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScheduleManagerScreen(
    viewModel: CampusLifeViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val schedules by viewModel.courseSchedules.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Course Schedule Manager",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Daily lectures, room locations & reminders",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("course_schedule_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val active = schedules.firstOrNull()
                            if (active != null) {
                                viewModel.testClassScheduleReminder(active)
                            }
                        },
                        modifier = Modifier.testTag("schedule_top_reminder_test_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Test Reminder Notification",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CourseScheduleManagerComponent(
                    schedules = schedules,
                    onAddSchedule = { slot -> viewModel.addCourseSchedule(slot) },
                    onDeleteSchedule = { slotId -> viewModel.deleteCourseSchedule(slotId) },
                    onToggleReminder = { slotId, enabled, mins ->
                        viewModel.toggleScheduleReminder(slotId, enabled, mins)
                    },
                    onTestReminder = { slot -> viewModel.testClassScheduleReminder(slot) },
                    onToggleAttendedToday = { slotId -> viewModel.toggleScheduleAttendedToday(slotId) }
                )
            }
        }
    }
}
