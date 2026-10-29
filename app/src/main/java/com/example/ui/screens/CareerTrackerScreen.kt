package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.CareerKanbanBoardComponent
import com.example.viewmodel.CampusLifeViewModel

/**
 * Dedicated full-screen destination for the Career & Job Application Kanban Board.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerTrackerScreen(
    viewModel: CampusLifeViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val applications by viewModel.careerApplications.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Career & Internship Kanban",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Manage Applied, Interviewing, and Offer milestones",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("career_tracker_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .testTag("career_tracker_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 40.dp)
        ) {
            item {
                CareerKanbanBoardComponent(
                    applications = applications,
                    onMoveStatus = { id, newStatus ->
                        viewModel.moveCareerApplicationStatus(id, newStatus)
                    },
                    onAddApplication = { newApp ->
                        viewModel.addCareerApplication(newApp)
                    },
                    onDeleteApplication = { id ->
                        viewModel.deleteCareerApplication(id)
                    }
                )
            }
        }
    }
}
