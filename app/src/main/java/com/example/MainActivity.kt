package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.Subject
import com.example.ui.components.CampusLifeBottomNav
import com.example.ui.components.CampusLifeTopBar
import com.example.ui.screens.*
import com.example.ui.theme.CampusLifeTheme
import com.example.viewmodel.AppDestination
import com.example.viewmodel.CampusLifeViewModel
import com.example.viewmodel.SubScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.example.worker.TaskNotificationHelper.createNotificationChannel(this)
        com.example.worker.ScheduleReminderHelper.createNotificationChannel(this)
        val navigateToTasks = intent.getStringExtra("EXTRA_NAVIGATE_TO") == "TASKS"
        val navigateToTimetable = intent.getStringExtra("EXTRA_NAVIGATE_TO") == "TIMETABLE"

        setContent {
            CampusLifeTheme {
                CampusLifeApp(
                    initialOpenTasks = navigateToTasks,
                    initialOpenTimetable = navigateToTimetable
                )
            }
        }
    }
}

@Composable
fun CampusLifeApp(
    initialOpenTasks: Boolean = false,
    initialOpenTimetable: Boolean = false,
    viewModel: CampusLifeViewModel = run {
        val context = androidx.compose.ui.platform.LocalContext.current
        val app = context.applicationContext as android.app.Application
        viewModel(factory = CampusLifeViewModel.provideFactory(app))
    }
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val subScreen by viewModel.subScreen.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val userFeedbackMessage by viewModel.userFeedbackMessage.collectAsState()
    val snackbarHostState = androidx.compose.runtime.remember { androidx.compose.material3.SnackbarHostState() }

    // Request notification permission on Android 13+ (API 33+)
    val context = androidx.compose.ui.platform.LocalContext.current
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                com.example.worker.TaskNotificationScheduler.schedulePeriodicTaskReminder(context)
            }
        }
    )

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (!com.example.worker.TaskNotificationHelper.hasNotificationPermission(context)) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (initialOpenTasks) {
            viewModel.openSubScreen(SubScreen.TASKS_LIST)
        } else if (initialOpenTimetable) {
            viewModel.openSubScreen(SubScreen.COURSE_SCHEDULE)
        }
    }

    androidx.compose.runtime.LaunchedEffect(userFeedbackMessage) {
        val msg = userFeedbackMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(
                message = msg,
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
            viewModel.clearFeedbackMessage()
        }
    }

    // Handle back button when inside a subscreen
    BackHandler(enabled = subScreen != SubScreen.NONE) {
        viewModel.closeSubScreen()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
        topBar = {
            if (subScreen == SubScreen.NONE) {
                val subtitle = when (currentTab) {
                    AppDestination.HOME -> "Home"
                    AppDestination.TRACK -> "Track & Timetable"
                    AppDestination.COMMUNITY -> "Community & Clubs"
                    AppDestination.GROWTH -> "Growth & Portfolio"
                    AppDestination.ME -> "Me & Settings"
                }
                CampusLifeTopBar(
                    subtitle = subtitle,
                    onSearchClick = {},
                    onNotificationsClick = {
                        viewModel.triggerUpcomingTasksReminder()
                    },
                    onProfileClick = { viewModel.selectTab(AppDestination.ME) },
                    onExecutiveDeskClick = { viewModel.openSubScreen(SubScreen.EXECUTIVE_DESK) }
                )
            }
        },
        bottomBar = {
            if (subScreen == SubScreen.NONE) {
                CampusLifeBottomNav(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (subScreen) {
                SubScreen.FOCUS_TIMER -> {
                    FocusTimerScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.HACKATHON_PASS -> {
                    HackathonPassScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.SUBJECT_DETAIL -> {
                    val activeSubject = selectedSubject ?: subjects.firstOrNull { it.code == "CS-302" } ?: subjects.first()
                    SubjectDetailScreen(
                        subject = activeSubject,
                        viewModel = viewModel,
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.ID_VERIFICATION -> {
                    IdVerificationScreen(
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.CAMPUS_COPILOT -> {
                    CampusCopilotScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.EXECUTIVE_DESK -> {
                    ExecutiveDeskScreen(
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.REVISION_PLAN -> {
                    ExamRevisionPlanScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.DAILY_ATTENDANCE_LOGGER -> {
                    DailyAttendanceScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.TASKS_LIST -> {
                    TasksListScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.ACADEMIC_CALENDAR -> {
                    AcademicCalendarScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.GPA_CALCULATOR -> {
                    GpaCalculatorScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.CAREER_KANBAN -> {
                    CareerTrackerScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.COURSE_SCHEDULE -> {
                    CourseScheduleManagerScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.closeSubScreen() }
                    )
                }
                SubScreen.NONE -> {
                    when (currentTab) {
                        AppDestination.HOME -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToTrack = { viewModel.selectTab(AppDestination.TRACK) },
                                onOpenCopilot = { viewModel.openSubScreen(SubScreen.CAMPUS_COPILOT) },
                                onOpenExecutiveDesk = { viewModel.openSubScreen(SubScreen.EXECUTIVE_DESK) }
                            )
                        }
                        AppDestination.TRACK -> {
                            TrackScreen(
                                viewModel = viewModel,
                                onOpenSubjectDetail = { subject ->
                                    viewModel.selectSubjectDetail(subject)
                                }
                            )
                        }
                        AppDestination.COMMUNITY -> {
                            CommunityScreen(viewModel = viewModel)
                        }
                        AppDestination.GROWTH -> {
                            GrowthScreen(viewModel = viewModel)
                        }
                        AppDestination.ME -> {
                            MeScreen(
                                viewModel = viewModel,
                                onOpenExecutiveDesk = { viewModel.openSubScreen(SubScreen.EXECUTIVE_DESK) }
                            )
                        }
                    }
                }
            }
        }
    }
}
