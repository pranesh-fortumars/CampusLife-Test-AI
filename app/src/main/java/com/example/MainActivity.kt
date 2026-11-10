package com.example

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.CampusLifeTheme
import com.example.viewmodel.AppDestination
import com.example.viewmodel.CampusLifeViewModel
import com.example.worker.DeadlineWorkScheduler
import com.example.worker.ProjectAssignment24hNotificationHelper
import com.example.worker.ProjectAssignmentDeadlineService
import com.example.worker.TaskNotificationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize WorkManager notification channels and periodic deadline checks
        TaskNotificationHelper.createNotificationChannel(this)
        ProjectAssignment24hNotificationHelper.createNotificationChannel(this)
        DeadlineWorkScheduler.schedulePeriodicDeadlineChecks(this)
        DeadlineWorkScheduler.schedule24HourPeriodicChecks(this)

        // Prime the 24-hour background deadline service scan from Room database
        ProjectAssignmentDeadlineService.startDeadlineScan(this)

        val navigateToTasks = intent?.getStringExtra("EXTRA_NAVIGATE_TO") == "TASKS"

        setContent {
            val viewModel: CampusLifeViewModel = viewModel()
            val isDarkThemePref by viewModel.isDarkMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val effectiveDark = isDarkThemePref ?: systemDark

            CampusLifeTheme(darkTheme = effectiveDark) {
                val context = LocalContext.current
                val currentDestination by viewModel.currentDestination.collectAsState()

                // Request POST_NOTIFICATIONS runtime permission on Android 13+
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        DeadlineWorkScheduler.schedulePeriodicDeadlineChecks(context)
                        DeadlineWorkScheduler.schedule24HourPeriodicChecks(context)
                        ProjectAssignmentDeadlineService.startDeadlineScan(context)
                    }
                }

                LaunchedEffect(Unit) {
                    if (navigateToTasks) {
                        viewModel.navigateTo(AppDestination.SEARCH)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                // System BackHandler: if on non-Home tab, back returns to Home
                BackHandler(enabled = currentDestination != AppDestination.HOME) {
                    viewModel.navigateTo(AppDestination.HOME)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("main_bottom_nav_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentDestination == AppDestination.HOME,
                                onClick = { viewModel.navigateTo(AppDestination.HOME) },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text("Home") },
                                modifier = Modifier.testTag("nav_item_home")
                            )

                            NavigationBarItem(
                                selected = currentDestination == AppDestination.SEARCH,
                                onClick = { viewModel.navigateTo(AppDestination.SEARCH) },
                                icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                label = { Text("Search") },
                                modifier = Modifier.testTag("nav_item_search")
                            )

                            NavigationBarItem(
                                selected = currentDestination == AppDestination.SCHEDULE,
                                onClick = { viewModel.navigateTo(AppDestination.SCHEDULE) },
                                icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Schedule") },
                                label = { Text("Schedule") },
                                modifier = Modifier.testTag("nav_item_schedule")
                            )

                            NavigationBarItem(
                                selected = currentDestination == AppDestination.COMMUNITY,
                                onClick = { viewModel.navigateTo(AppDestination.COMMUNITY) },
                                icon = { Icon(Icons.Default.Forum, contentDescription = "Community") },
                                label = { Text("Community") },
                                modifier = Modifier.testTag("nav_item_community")
                            )

                            NavigationBarItem(
                                selected = currentDestination == AppDestination.ME,
                                onClick = { viewModel.navigateTo(AppDestination.ME) },
                                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                label = { Text("Me") },
                                modifier = Modifier.testTag("nav_item_me")
                            )
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentDestination) {
                            AppDestination.HOME -> HomeScreen(
                                viewModel = viewModel,
                                onNavigateToSearch = { viewModel.navigateTo(AppDestination.SEARCH) },
                                onNavigateToSchedule = { viewModel.navigateTo(AppDestination.SCHEDULE) },
                                onNavigateToCommunity = { viewModel.navigateTo(AppDestination.COMMUNITY) }
                            )
                            AppDestination.SEARCH -> SearchScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateTo(AppDestination.HOME) }
                            )
                            AppDestination.SCHEDULE -> ScheduleScreen(viewModel = viewModel)
                            AppDestination.COMMUNITY -> CommunityScreen(viewModel = viewModel)
                            AppDestination.ME -> MeScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
